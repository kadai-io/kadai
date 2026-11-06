/*
 * Copyright [2026] [envite consulting GmbH]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *
 */

import {
  afterRenderEffect,
  AfterViewChecked,
  Component,
  DestroyRef,
  effect,
  ElementRef,
  inject,
  input,
  OnInit,
  output,
  signal,
  untracked,
  viewChildren
} from '@angular/core';
import { toSignal, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { distinctUntilChanged, firstValueFrom, Observable } from 'rxjs';
import {
  Actions,
  ofActionCompleted,
  ofActionDispatched,
  ofActionErrored,
  ofActionSuccessful,
  Store
} from '@ngxs/store';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { Workbasket } from 'app/shared/models/workbasket';
import {
  customFieldCount,
  WorkbasketAccessItems,
  WorkbasketAccessItemWrite
} from 'app/shared/models/workbasket-access-items';
import { WorkbasketAccessItemsRepresentation } from 'app/shared/models/workbasket-access-items-representation';
import { RequestInProgressService } from 'app/shared/services/request-in-progress/request-in-progress.service';
import { AccessId } from 'app/shared/models/access-id';
import { EngineConfigurationSelectors } from 'app/shared/store/engine-configuration-store/engine-configuration.selectors';
import { filter, map, startWith, tap, timeout } from 'rxjs/operators';
import { NotificationService } from 'app/shared/services/notifications/notification.service';
import { AccessItemsCustomisation, CustomField, CustomFields, getCustomFields } from 'app/shared/models/customisation';
import {
  GetWorkbasketAccessItems,
  OnButtonPressed,
  SaveNewWorkbasket,
  UpdateWorkbasket,
  UpdateWorkbasketAccessItems
} from 'app/shared/store/workbasket-store/workbasket.actions';
import { WorkbasketSelectors } from 'app/shared/store/workbasket-store/workbasket.selectors';
import { ButtonAction } from 'app/administration/models/button-action';
import { AsyncPipe } from '@angular/common';
import { MatButton } from '@angular/material/button';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIcon } from '@angular/material/icon';
import { ResizableWidthDirective } from 'app/shared/directives/resizable-width.directive';
import { TypeAheadComponent } from 'app/shared/components/type-ahead/type-ahead.component';
import { MatInput } from '@angular/material/input';
import { getPermissionWarnings } from 'app/shared/validators/permission-dependency.validator';
import { FormSubmitDirective } from 'app/shared/directives/form-submit.directive';
import { FormFieldSubmitDirective } from 'app/shared/directives/form-field-submit.directive';
import { accessIdExistsValidator } from 'app/shared/validators/access-id-exists.validator';
import { AccessIdsService } from 'app/shared/services/access-ids/access-ids.service';

export const ACCESS_NAME_LOOKUP_TIMEOUT_MS = 3000;

@Component({
  selector: 'kadai-administration-workbasket-access-items',
  templateUrl: './workbasket-access-items.component.html',
  styleUrls: ['./workbasket-access-items.component.scss'],
  imports: [
    MatButton,
    MatTooltip,
    MatIcon,
    ResizableWidthDirective,
    TypeAheadComponent,
    MatInput,
    AsyncPipe,
    ReactiveFormsModule,
    FormSubmitDirective,
    FormFieldSubmitDirective
  ]
})
export class WorkbasketAccessItemsComponent implements OnInit, AfterViewChecked {
  workbasket = input<Workbasket>();
  expanded = input<boolean>();
  accessItemsValidityChanged = output<boolean>();
  inputs = viewChildren<ElementRef>('htmlInputElement');
  selectedRows: number[] = [];
  workbasketClone?: Workbasket;
  customFields$!: Observable<CustomField[]>;
  keysOfVisibleFields: string[] = [];
  accessItemsRepresentation!: WorkbasketAccessItemsRepresentation;
  accessItemsClone = signal<WorkbasketAccessItems[]>([]);
  accessItemsResetClone: WorkbasketAccessItems[] = [];
  added = false;
  isNewAccessItemsFromStore = false;
  isAccessItemsTabSelected = false;
  private destroyRef = inject(DestroyRef);
  private pendingNewWorkbasketAccessItems?: WorkbasketAccessItems[];
  accessItemsCustomization$: Observable<AccessItemsCustomisation | undefined> = inject(Store).select(
    EngineConfigurationSelectors.accessItemsCustomisation
  );
  buttonAction$: Observable<ButtonAction | undefined> = inject(Store).select(WorkbasketSelectors.buttonAction);
  private selectedComponentSig = toSignal(inject(Store).select(WorkbasketSelectors.selectedComponent));
  private accessItemsRepresentationSig = toSignal(inject(Store).select(WorkbasketSelectors.workbasketAccessItems));
  private requestInProgressService = inject(RequestInProgressService);
  private formBuilder = inject(FormBuilder);
  AccessItemsForm = this.formBuilder.group({
    accessItemsGroups: this.formBuilder.array<FormGroup>([])
  });
  private notificationsService = inject(NotificationService);
  private accessIdService = inject(AccessIdsService);
  private store = inject(Store);
  private ngxsActions$ = inject(Actions);
  isSubmitting = false;

  constructor() {
    effect(() => {
      const wb = this.workbasket();
      untracked(() => {
        if (this.workbasketClone && wb && this.workbasketClone.workbasketId !== wb.workbasketId) {
          this.init();
        }
        if (wb) this.workbasketClone = wb;
      });
    });

    afterRenderEffect(() => {
      const inputs = this.inputs();
      if (inputs.length > 0 && untracked(() => this.added)) {
        inputs[inputs.length - 1].nativeElement.focus();
      }
    });

    effect(() => {
      const component = this.selectedComponentSig();
      untracked(() => {
        if (component === 1) {
          this.isAccessItemsTabSelected = true;
        }
      });
    });

    effect(() => {
      const accessItemsRepresentation = this.accessItemsRepresentationSig();
      untracked(() => {
        if (typeof accessItemsRepresentation !== 'undefined') {
          let accessItems = [...accessItemsRepresentation.accessItems];
          accessItems = this.sortAccessItems(accessItems, 'accessId');

          this.accessItemsRepresentation = {
            accessItems: accessItems,
            _links: accessItemsRepresentation._links
          };
          this.setAccessItemsGroups(accessItems);

          this.AccessItemsForm.get('accessItemsGroups')
            ?.statusChanges.pipe(
              startWith(null),
              map(() => this.AccessItemsForm.get('accessItemsGroups')?.valid ?? false),
              distinctUntilChanged(),
              takeUntilDestroyed(this.destroyRef)
            )
            .subscribe((isValid) => {
              this.accessItemsValidityChanged.emit(isValid);
            });

          this.accessItemsClone.set(this.cloneAccessItems());
          this.accessItemsResetClone = this.cloneAccessItems();

          this.isNewAccessItemsFromStore = true;
        }
      });
    });
  }

  get accessItemsGroups(): FormArray {
    return this.AccessItemsForm.get('accessItemsGroups') as FormArray;
  }

  ngOnInit() {
    this.workbasketClone = this.workbasket();
    this.init();

    this.customFields$ = this.accessItemsCustomization$.pipe(
      map((c) => c ?? ({} as CustomFields)),
      getCustomFields(customFieldCount),
      tap((customFields) => {
        const accessItem = this.createWorkbasketAccessItems();
        this.keysOfVisibleFields = [
          'permRead',
          'permOpen',
          'permAppend',
          'permTransfer',
          'permDistribute',
          'permReadTasks',
          'permEditTasks'
        ];
        for (let i = 0; i < customFieldCount; i++) {
          if (customFields[i].visible) {
            this.keysOfVisibleFields.push(Object.keys(accessItem)[i + 12]);
          }
        }
      })
    );

    // saving workbasket access items when workbasket already exists
    this.ngxsActions$.pipe(ofActionCompleted(UpdateWorkbasket), takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.onSubmit();
    });

    this.ngxsActions$.pipe(ofActionDispatched(SaveNewWorkbasket), takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.pendingNewWorkbasketAccessItems = this.snapshotAccessItems();
    });

    // saving workbasket access items when workbasket was copied or created
    this.ngxsActions$.pipe(ofActionSuccessful(SaveNewWorkbasket), takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      const sourceAccessItems = this.pendingNewWorkbasketAccessItems ?? [];
      this.pendingNewWorkbasketAccessItems = undefined;

      const workbasket = this.store.selectSnapshot(WorkbasketSelectors.selectedWorkbasket);
      const workbasketId = workbasket?.workbasketId;
      const accessItemsUrl = workbasket?._links?.accessItems?.href;

      if (!workbasketId || !accessItemsUrl) {
        return;
      }

      void this.onSubmit(this.prepareAccessItemsForNewWorkbasket(sourceAccessItems, workbasketId), accessItemsUrl);
    });

    this.ngxsActions$.pipe(ofActionErrored(SaveNewWorkbasket), takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.pendingNewWorkbasketAccessItems = undefined;
    });

    this.buttonAction$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .pipe(filter((buttonAction) => typeof buttonAction !== 'undefined'))
      .subscribe((button) => {
        switch (button) {
          case ButtonAction.UNDO:
            this.clear();
            break;
          default:
            break;
        }
      });
  }

  ngAfterViewChecked() {
    if (this.isNewAccessItemsFromStore || this.isAccessItemsTabSelected) {
      if (document.getElementById(`checkbox-0-00`)) {
        let row = 0;
        this.accessItemsGroups.controls.forEach(() => {
          const value = { currentTarget: { checked: true } } as unknown as Event;
          this.setSelectAllCheckbox(row, value);
          row = row + 1;
        });
        this.isAccessItemsTabSelected = false;
        this.isNewAccessItemsFromStore = false;
      }
    }
  }

  init() {
    const wb = this.workbasket();
    if (wb?._links?.accessItems) {
      this.requestInProgressService.setRequestInProgress(true);
      this.store.dispatch(new GetWorkbasketAccessItems(wb._links.accessItems.href)).subscribe(() => {
        this.requestInProgressService.setRequestInProgress(false);
      });
    }
  }

  sortAccessItems(accessItems: WorkbasketAccessItems[], sortBy: keyof WorkbasketAccessItems): WorkbasketAccessItems[] {
    return accessItems.sort((a, b) => {
      const valueA = String(a[sortBy] ?? '');
      const valueB = String(b[sortBy] ?? '');
      if (valueA < valueB) {
        return -1;
      }
      if (valueA > valueB) {
        return 1;
      }

      return 0;
    });
  }

  setAccessItemsGroups(accessItems: WorkbasketAccessItems[]) {
    const AccessItemsFormGroups = accessItems.map((accessItem) => {
      const group = this.formBuilder.group(accessItem);
      this.setupAccessIdValidators(group);
      return group;
    });
    const AccessItemsFormArray = this.formBuilder.array(AccessItemsFormGroups);
    this.AccessItemsForm.setControl('accessItemsGroups', AccessItemsFormArray);
  }

  createWorkbasketAccessItems(): WorkbasketAccessItems {
    return {
      accessItemId: '',
      workbasketId: '',
      workbasketKey: '',
      accessId: '',
      accessName: '',
      permRead: false,
      permOpen: false,
      permAppend: false,
      permTransfer: false,
      permDistribute: false,
      permReadTasks: false,
      permEditTasks: false,
      permCustom1: false,
      permCustom2: false,
      permCustom3: false,
      permCustom4: false,
      permCustom5: false,
      permCustom6: false,
      permCustom7: false,
      permCustom8: false,
      permCustom9: false,
      permCustom10: false,
      permCustom11: false,
      permCustom12: false,
      _links: {}
    };
  }

  addAccessItem() {
    const workbasketAccessItems: WorkbasketAccessItems = this.createWorkbasketAccessItems();
    workbasketAccessItems.workbasketId = this.workbasket()!.workbasketId!;
    workbasketAccessItems.permRead = true;
    const newForm = this.formBuilder.group(workbasketAccessItems);
    this.setupAccessIdValidators(newForm);
    this.accessItemsGroups.insert(0, newForm);
    this.accessItemsClone.update((accessItemsClone) => [workbasketAccessItems, ...accessItemsClone]);
    this.added = true;
  }

  clear() {
    this.store.dispatch(new OnButtonPressed(undefined));
    this.AccessItemsForm.reset();
    this.setAccessItemsGroups(this.accessItemsResetClone);
    this.accessItemsClone.set(this.cloneAccessItems());
    this.notificationsService.showSuccess('WORKBASKET_ACCESS_ITEM_RESTORE');
  }

  async onSubmit(
    accessItems?: WorkbasketAccessItemWrite[],
    accessItemsUrl: string | undefined = this.accessItemsRepresentation?._links?.self?.href
  ) {
    if (!accessItemsUrl || this.isSubmitting) {
      return;
    }
    this.isSubmitting = true;

    try {
      const currentAccessItems = accessItems ?? this.snapshotAccessItems();
      const isValid = await this.validateAccessItemsSnapshot(currentAccessItems);
      if (!isValid) {
        this.AccessItemsForm.markAllAsTouched();
        this.notificationsService.showError('OWNER_NOT_VALID', { owner: 'access id' });
        return;
      }
      this.onSave(accessItemsUrl, await this.withAccessNames(currentAccessItems));
    } finally {
      this.isSubmitting = false;
    }
  }

  onSave(accessItemsUrl: string, accessItems: WorkbasketAccessItemWrite[]) {
    this.requestInProgressService.setRequestInProgress(true);
    this.store.dispatch(new UpdateWorkbasketAccessItems(accessItemsUrl, accessItems)).subscribe(() => {
      this.requestInProgressService.setRequestInProgress(false);
    });
  }

  accessItemSelected(accessId: AccessId | null, row: number) {
    this.accessItemsGroups.controls[row].get('accessName')?.setValue(accessId?.name ?? '');
  }

  checkAll(row: number, value: Event) {
    const checkAll = (value.target as HTMLInputElement).checked;
    const accessItem = this.accessItemsGroups.controls[row];
    this.keysOfVisibleFields.forEach((key) => {
      accessItem.get(key)?.setValue(checkAll);
    });
  }

  setSelectAllCheckbox(row: number, value: Event) {
    let areAllCheckboxesSelected = false;

    if ((value.currentTarget as HTMLInputElement).checked) {
      areAllCheckboxesSelected = true;
      const accessItem = this.accessItemsGroups.controls[row].value;

      this.keysOfVisibleFields.forEach((key) => {
        if (accessItem[key] === false) {
          areAllCheckboxesSelected = false;
        }
      });
    }
    const checkbox = document.getElementById(`checkbox-${row}-00`) as HTMLInputElement;
    checkbox.checked = areAllCheckboxesSelected;
  }

  cloneAccessItems(): WorkbasketAccessItems[] {
    return this.snapshotAccessItems();
  }

  prepareAccessItemsForNewWorkbasket(
    sourceAccessItems: WorkbasketAccessItems[],
    workbasketId: string
  ): WorkbasketAccessItemWrite[] {
    return sourceAccessItems.map((sourceAccessItem) => {
      const accessItem: WorkbasketAccessItemWrite = { ...sourceAccessItem, workbasketId };
      delete accessItem.accessItemId;
      return accessItem;
    });
  }

  private snapshotAccessItems(): WorkbasketAccessItems[] {
    return (this.accessItemsGroups.getRawValue() as WorkbasketAccessItems[]).map((accessItem) => ({ ...accessItem }));
  }

  private async withAccessNames<T extends { accessId: string; accessName: string }>(accessItems: T[]): Promise<T[]> {
    return Promise.all(
      accessItems.map(async (accessItem) =>
        accessItem.accessId && !accessItem.accessName
          ? { ...accessItem, accessName: await this.lookupAccessName(accessItem.accessId) }
          : accessItem
      )
    );
  }

  private async lookupAccessName(accessId: string): Promise<string> {
    try {
      const accessIds = await firstValueFrom(
        this.accessIdService.searchForAccessId(accessId).pipe(timeout(ACCESS_NAME_LOOKUP_TIMEOUT_MS))
      );
      return accessIds.find((item) => item.accessId?.toLowerCase() === accessId.toLowerCase())?.name ?? '';
    } catch {
      return '';
    }
  }

  private setupAccessIdValidators(group: FormGroup): void {
    const control = group.get('accessId');
    if (control) {
      control.setValidators(Validators.required);
      control.setAsyncValidators(accessIdExistsValidator(this.accessIdService));
      control.updateValueAndValidity();
    }
  }

  private async validateAccessItemsSnapshot(accessItems: WorkbasketAccessItemWrite[]): Promise<boolean> {
    const tempGroups = accessItems.map((item) => {
      const group = this.formBuilder.group(item);
      this.setupAccessIdValidators(group);
      return group;
    });

    const tempFormArray = this.formBuilder.array(tempGroups);
    tempGroups.forEach((group) => {
      const warnings = getPermissionWarnings(group);
      warnings.forEach((key) => this.notificationsService.showWarning(key));
    });

    tempFormArray.updateValueAndValidity();
    if (tempFormArray.pending) {
      await firstValueFrom(tempFormArray.statusChanges.pipe(filter((status) => status !== 'PENDING')));
    }
    return tempFormArray.valid;
  }

  getAccessItemCustomProperty(customNumber: number): `permCustom${1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12}` {
    return `permCustom${customNumber}` as `permCustom${1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12}`;
  }

  selectRow(value: Event, index: number) {
    if ((value.target as HTMLInputElement).checked) {
      this.selectedRows.push(index);
    } else {
      this.selectedRows = this.selectedRows.filter((number) => number != index);
    }
  }

  deleteAccessItems() {
    this.selectedRows.sort((a, b) => b - a);
    this.selectedRows.forEach((element) => {
      this.accessItemsGroups.removeAt(element);
      this.accessItemsClone.update((accessItemsClone) => accessItemsClone.filter((_, index) => index !== element));
    });
    this.selectedRows = [];
  }
}
