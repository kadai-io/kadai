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
  AfterViewChecked,
  ChangeDetectionStrategy,
  Component,
  effect,
  ElementRef,
  inject,
  input,
  OnDestroy,
  OnInit,
  output,
  untracked,
  viewChildren
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { distinctUntilChanged, Observable, Subject } from 'rxjs';
import { Actions, ofActionCompleted, Store } from '@ngxs/store';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { Workbasket } from 'app/shared/models/workbasket';
import { customFieldCount, WorkbasketAccessItems } from 'app/shared/models/workbasket-access-items';
import { WorkbasketAccessItemsRepresentation } from 'app/shared/models/workbasket-access-items-representation';
import { RequestInProgressService } from 'app/shared/services/request-in-progress/request-in-progress.service';
import { highlight } from 'app/shared/animations/validation.animation';
import { FormsValidatorService } from 'app/shared/services/forms-validator/forms-validator.service';
import { AccessId } from 'app/shared/models/access-id';
import { EngineConfigurationSelectors } from 'app/shared/store/engine-configuration-store/engine-configuration.selectors';
import { filter, map, startWith, take, takeUntil, tap } from 'rxjs/operators';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import { AccessItemsCustomisation, CustomField, getCustomFields } from '../../../shared/models/customisation';
import {
  GetWorkbasketAccessItems,
  OnButtonPressed,
  SaveNewWorkbasket,
  UpdateWorkbasket,
  UpdateWorkbasketAccessItems
} from '../../../shared/store/workbasket-store/workbasket.actions';
import { WorkbasketSelectors } from '../../../shared/store/workbasket-store/workbasket.selectors';
import { ButtonAction } from '../../models/button-action';
import { AsyncPipe, NgClass, NgStyle } from '@angular/common';
import { MatButton } from '@angular/material/button';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIcon } from '@angular/material/icon';
import { ResizableWidthDirective } from '../../../shared/directives/resizable-width.directive';
import { TypeAheadComponent } from '../../../shared/components/type-ahead/type-ahead.component';
import { MatInput } from '@angular/material/input';

@Component({
  selector: 'kadai-administration-workbasket-access-items',
  templateUrl: './workbasket-access-items.component.html',
  animations: [highlight],
  styleUrls: ['./workbasket-access-items.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    NgStyle,
    MatButton,
    MatTooltip,
    MatIcon,
    ResizableWidthDirective,
    NgClass,
    TypeAheadComponent,
    MatInput,
    AsyncPipe,
    ReactiveFormsModule
  ]
})
export class WorkbasketAccessItemsComponent implements OnInit, OnDestroy, AfterViewChecked {
  formsValidatorService = inject(FormsValidatorService);
  workbasket = input<Workbasket>();
  expanded = input<boolean>();
  accessItemsValidityChanged = output<boolean>();
  inputs = viewChildren<ElementRef>('htmlInputElement');
  selectedRows: number[] = [];
  workbasketClone: Workbasket;
  customFields$: Observable<CustomField[]>;
  keysOfVisibleFields: string[];
  accessItemsRepresentation: WorkbasketAccessItemsRepresentation;
  accessItemsClone: WorkbasketAccessItems[];
  accessItemsResetClone: WorkbasketAccessItems[];
  toggleValidationAccessIdMap = new Map<number, boolean>();
  added = false;
  isNewAccessItemsFromStore = false;
  isAccessItemsTabSelected = false;
  destroy$ = new Subject<void>();
  selectedWorkbasket$: Observable<Workbasket> = inject(Store).select(WorkbasketSelectors.selectedWorkbasket);
  accessItemsCustomization$: Observable<AccessItemsCustomisation> = inject(Store).select(
    EngineConfigurationSelectors.accessItemsCustomisation
  );
  buttonAction$: Observable<ButtonAction> = inject(Store).select(WorkbasketSelectors.buttonAction);
  private selectedComponentSig = toSignal(inject(Store).select(WorkbasketSelectors.selectedComponent));
  private accessItemsRepresentationSig = toSignal(inject(Store).select(WorkbasketSelectors.workbasketAccessItems));
  private requestInProgressService = inject(RequestInProgressService);
  private formBuilder = inject(FormBuilder);
  AccessItemsForm = this.formBuilder.group({
    accessItemsGroups: this.formBuilder.array<FormGroup>([])
  });
  private notificationsService = inject(NotificationService);
  private store = inject(Store);
  private ngxsActions$ = inject(Actions);

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

    effect(() => {
      const inputs = this.inputs();
      untracked(() => {
        if (inputs.length > 0 && this.added) {
          inputs[inputs.length - 1].nativeElement.focus();
        }
      });
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
              takeUntil(this.destroy$)
            )
            .subscribe((isValid) => {
              this.accessItemsValidityChanged.emit(isValid);
            });

          this.accessItemsClone = this.cloneAccessItems();
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
    this.ngxsActions$.pipe(ofActionCompleted(UpdateWorkbasket), takeUntil(this.destroy$)).subscribe(() => {
      this.onSubmit();
    });

    // saving workbasket access items when workbasket was copied or created
    this.ngxsActions$.pipe(ofActionCompleted(SaveNewWorkbasket), takeUntil(this.destroy$)).subscribe(() => {
      this.selectedWorkbasket$.pipe(take(1)).subscribe((workbasket) => {
        this.accessItemsRepresentation._links = { self: { href: workbasket._links.accessItems.href } };
        this.setWorkbasketIdForCopy(workbasket.workbasketId);
        this.onSubmit();
      });
    });

    this.buttonAction$
      .pipe(takeUntil(this.destroy$))
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
          const value = { currentTarget: { checked: true } };
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

  sortAccessItems(accessItems: WorkbasketAccessItems[], sortBy: string): WorkbasketAccessItems[] {
    return accessItems.sort((a, b) => {
      if (a[sortBy] < b[sortBy]) {
        return -1;
      }
      if (a[sortBy] > b[sortBy]) {
        return 1;
      }

      return 0;
    });
  }

  setAccessItemsGroups(accessItems: WorkbasketAccessItems[]) {
    const AccessItemsFormGroups = accessItems.map((accessItem) => this.formBuilder.group(accessItem));
    AccessItemsFormGroups.forEach((accessItemGroup) => {
      accessItemGroup.controls.accessId.setValidators(Validators.required);
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
    workbasketAccessItems.workbasketId = this.workbasket()!.workbasketId;
    workbasketAccessItems.permRead = true;
    const newForm = this.formBuilder.group(workbasketAccessItems);
    newForm.controls.accessId.setValidators(Validators.required);
    this.accessItemsGroups.insert(0, newForm);
    this.accessItemsClone.unshift(workbasketAccessItems);
    this.added = true;
  }

  clear() {
    this.store.dispatch(new OnButtonPressed(undefined));
    this.formsValidatorService.formSubmitAttempt = false;
    this.AccessItemsForm.reset();
    this.setAccessItemsGroups(this.accessItemsResetClone);
    this.accessItemsClone = this.cloneAccessItems();
    this.notificationsService.showSuccess('WORKBASKET_ACCESS_ITEM_RESTORE');
  }

  onSubmit() {
    this.formsValidatorService.formSubmitAttempt = true;

    const shouldSaveWorkbasket = this.formsValidatorService
      .validateFormAccess(this.accessItemsGroups, this.toggleValidationAccessIdMap)
      .then((isFormValid) => isFormValid)
      .catch(() => false);

    if (shouldSaveWorkbasket) {
      this.onSave();
    }
  }

  onSave() {
    this.requestInProgressService.setRequestInProgress(true);
    this.store
      .dispatch(
        new UpdateWorkbasketAccessItems(
          this.accessItemsRepresentation._links.self.href,
          this.AccessItemsForm.value.accessItemsGroups
        )
      )
      .subscribe(() => {
        this.requestInProgressService.setRequestInProgress(false);
      });
  }

  accessItemSelected(accessItem: AccessId, row: number) {
    this.accessItemsGroups.controls[row].get('accessId').setValue(accessItem?.accessId);
    this.accessItemsGroups.controls[row].get('accessName').setValue(accessItem?.name);
  }

  checkAll(row: number, value: any) {
    const checkAll = value.target.checked;
    const accessItem = this.accessItemsGroups.controls[row];
    this.keysOfVisibleFields.forEach((key) => {
      accessItem.get(key).setValue(checkAll);
    });
  }

  setSelectAllCheckbox(row: number, value: any) {
    let areAllCheckboxesSelected = false;

    if (value.currentTarget.checked) {
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
    return this.AccessItemsForm.value.accessItemsGroups.map((accessItems: WorkbasketAccessItems) => ({
      ...accessItems
    }));
  }

  setWorkbasketIdForCopy(workbasketId: string) {
    this.accessItemsGroups.value.forEach((element) => {
      delete element.accessItemId;
      element.workbasketId = workbasketId;
    });
  }

  getAccessItemCustomProperty(customNumber: number): string {
    return `permCustom${customNumber}`;
  }

  selectRow(value: any, index: number) {
    if (value.target.checked) {
      this.selectedRows.push(index);
    } else {
      this.selectedRows = this.selectedRows.filter(function (number) {
        return number != index;
      });
    }
  }

  deleteAccessItems() {
    this.selectedRows.sort(function (a, b) {
      return b - a;
    });
    this.selectedRows.forEach((element) => {
      this.accessItemsGroups.removeAt(element);
      this.accessItemsClone.splice(element, 1);
    });
    this.selectedRows = [];
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
