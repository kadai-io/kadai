/*
 * Copyright [2025] [envite consulting GmbH]
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
  AfterViewInit,
  Component,
  ElementRef,
  inject,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  QueryList,
  SimpleChanges,
  ViewChildren
} from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { Workbasket } from 'app/shared/models/workbasket';
import { customFieldCount, WorkbasketAccessItems } from 'app/shared/models/workbasket-access-items';
import { WorkbasketAccessItemsRepresentation } from 'app/shared/models/workbasket-access-items-representation';
import { RequestInProgressService } from 'app/shared/services/request-in-progress/request-in-progress.service';
import { highlight } from 'app/shared/animations/validation.animation';
import { FormsValidatorService } from 'app/shared/services/forms-validator/forms-validator.service';
import { AccessId } from 'app/shared/models/access-id';
import { EngineConfigurationSelectors } from 'app/shared/store/engine-configuration-store/engine-configuration.selectors';
import { filter, take, takeUntil, tap } from 'rxjs/operators';
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
import { WorkbasketComponent } from '../../models/workbasket-component';
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
export class WorkbasketAccessItemsComponent implements OnInit, OnChanges, OnDestroy, AfterViewInit, AfterViewChecked {
  formsValidatorService = inject(FormsValidatorService);
  @Input() workbasket: Workbasket;
  @Input() expanded: boolean;
  @ViewChildren('htmlInputElement') inputs: QueryList<ElementRef>;
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
  @Select(WorkbasketSelectors.selectedWorkbasket) selectedWorkbasket$: Observable<Workbasket>;
  @Select(EngineConfigurationSelectors.accessItemsCustomisation)
  accessItemsCustomization$: Observable<AccessItemsCustomisation>;
  @Select(WorkbasketSelectors.workbasketAccessItems)
  accessItemsRepresentation$: Observable<WorkbasketAccessItemsRepresentation>;
  @Select(WorkbasketSelectors.buttonAction) buttonAction$: Observable<ButtonAction>;
  @Select(WorkbasketSelectors.selectedComponent) selectedComponent$: Observable<WorkbasketComponent>;
  private requestInProgressService = inject(RequestInProgressService);
  private formBuilder = inject(FormBuilder);
  AccessItemsForm = this.formBuilder.group({
    accessItemsGroups: this.formBuilder.array<FormGroup>([])
  });
  private notificationsService = inject(NotificationService);
  private store = inject(Store);
  private ngxsActions$ = inject(Actions);

  get accessItemsGroups(): FormArray {
    return this.AccessItemsForm.get('accessItemsGroups') as FormArray;
  }

  ngOnInit() {
    this.init();

    this.selectedComponent$.pipe(takeUntil(this.destroy$)).subscribe((component) => {
      if (component === 1) {
        this.isAccessItemsTabSelected = true;
      }
    });

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

    this.accessItemsRepresentation$.pipe(takeUntil(this.destroy$)).subscribe((accessItemsRepresentation) => {
      if (typeof accessItemsRepresentation !== 'undefined') {
        let accessItems = [...accessItemsRepresentation.accessItems];
        accessItems = this.sortAccessItems(accessItems, 'accessId');

        this.accessItemsRepresentation = {
          accessItems: accessItems,
          _links: accessItemsRepresentation._links
        };
        this.setAccessItemsGroups(accessItems);
        this.accessItemsClone = this.cloneAccessItems();
        this.accessItemsResetClone = this.cloneAccessItems();

        this.isNewAccessItemsFromStore = true;
      }
    });

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

  ngAfterViewInit() {
    this.inputs.changes.pipe(takeUntil(this.destroy$)).subscribe((next) => {
      if (typeof next.last !== 'undefined') {
        if (this.added) next.last.nativeElement.focus();
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

  ngOnChanges(changes?: SimpleChanges) {
    if (this.workbasketClone) {
      if (this.workbasketClone.workbasketId != this.workbasket.workbasketId) {
        this.init();
      }
    }
    this.workbasketClone = this.workbasket;
  }

  init() {
    if (this.workbasket._links?.accessItems) {
      this.requestInProgressService.setRequestInProgress(true);
      this.store.dispatch(new GetWorkbasketAccessItems(this.workbasket._links.accessItems.href)).subscribe(() => {
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
    workbasketAccessItems.workbasketId = this.workbasket.workbasketId;
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
