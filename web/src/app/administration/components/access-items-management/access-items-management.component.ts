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

import { Component, inject, OnInit } from '@angular/core';
import { Store } from '@ngxs/store';
import {
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { Observable, Subject } from 'rxjs';
import { FormsValidatorService } from 'app/shared/services/forms-validator/forms-validator.service';
import { WorkbasketAccessItems } from 'app/shared/models/workbasket-access-items';
import {
  Direction,
  Sorting,
  WORKBASKET_ACCESS_ITEM_SORT_PARAMETER_NAMING,
  WorkbasketAccessItemQuerySortParameter
} from 'app/shared/models/sorting';
import { EngineConfigurationSelectors } from 'app/shared/store/engine-configuration-store/engine-configuration.selectors';
import { takeUntil } from 'rxjs/operators';
import { AccessId } from '../../../shared/models/access-id';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import { AccessItemsCustomisation, CustomField, getCustomFields } from '../../../shared/models/customisation';
import { customFieldCount } from '../../../shared/models/workbasket-access-items';
import {
  GetAccessItems,
  GetGroupsByAccessId,
  GetPermissionsByAccessId,
  RemoveAccessItemsPermissions
} from '../../../shared/store/access-items-management-store/access-items-management.actions';
import { AccessItemsManagementSelector } from '../../../shared/store/access-items-management-store/access-items-management.selector';
import { MatDialog } from '@angular/material/dialog';
import { WorkbasketAccessItemQueryFilterParameter } from '../../../shared/models/workbasket-access-item-query-filter-parameter';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';
import { TypeAheadComponent } from '../../../shared/components/type-ahead/type-ahead.component';
import { AsyncPipe } from '@angular/common';
import { SvgIconComponent } from 'angular-svg-icon';
import { MatExpansionPanel, MatExpansionPanelHeader, MatExpansionPanelTitle } from '@angular/material/expansion';
import {
  MatCell,
  MatCellDef,
  MatColumnDef,
  MatHeaderCell,
  MatHeaderCellDef,
  MatHeaderRow,
  MatHeaderRowDef,
  MatRow,
  MatRowDef,
  MatTable
} from '@angular/material/table';
import { SortComponent } from '../../../shared/components/sort/sort.component';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatTooltip } from '@angular/material/tooltip';
import { MatCheckbox } from '@angular/material/checkbox';
import { MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';

@Component({
  selector: 'kadai-administration-access-items-management',
  templateUrl: './access-items-management.component.html',
  styleUrls: ['./access-items-management.component.scss'],
  imports: [
    TypeAheadComponent,
    SvgIconComponent,
    MatExpansionPanel,
    MatExpansionPanelHeader,
    MatExpansionPanelTitle,
    MatTable,
    MatColumnDef,
    MatHeaderCellDef,
    MatHeaderCell,
    MatCellDef,
    MatCell,
    MatHeaderRowDef,
    MatHeaderRow,
    MatRowDef,
    MatRow,
    FormsModule,
    ReactiveFormsModule,
    SortComponent,
    MatFormField,
    MatLabel,
    MatInput,
    MatTooltip,
    MatCheckbox,
    MatButton,
    MatIcon,
    AsyncPipe
  ]
})
export class AccessItemsManagementComponent implements OnInit {
  dialog = inject(MatDialog);
  accessIdPrevious: string;
  accessIdName: string;
  accessItemsForm: FormGroup;
  accessId: AccessId;
  groups: AccessId[];
  permissions: AccessId[];
  defaultSortBy: WorkbasketAccessItemQuerySortParameter = WorkbasketAccessItemQuerySortParameter.ACCESS_ID;
  sortingFields: Map<WorkbasketAccessItemQuerySortParameter, string> = WORKBASKET_ACCESS_ITEM_SORT_PARAMETER_NAMING;
  sortModel: Sorting<WorkbasketAccessItemQuerySortParameter> = {
    'sort-by': this.defaultSortBy,
    order: Direction.DESC
  };
  accessItems: WorkbasketAccessItems[];
  isGroup: boolean = false;
  accessItemsCustomization$: Observable<AccessItemsCustomisation> = inject(Store).select(
    EngineConfigurationSelectors.accessItemsCustomisation
  );
  groups$: Observable<AccessId[]> = inject(Store).select(AccessItemsManagementSelector.groups);
  customFields$: Observable<CustomField[]>;
  permissions$: Observable<AccessId[]> = inject(Store).select(AccessItemsManagementSelector.permissions);
  destroy$ = new Subject<void>();
  private formBuilder = inject(FormBuilder);
  private formsValidatorService = inject(FormsValidatorService);
  private notificationService = inject(NotificationService);
  private store = inject(Store);
  private requestInProgressService = inject(RequestInProgressService);

  get accessItemsGroups(): FormArray {
    return this.accessItemsForm ? (this.accessItemsForm.get('accessItemsGroups') as FormArray) : null;
  }

  get accessItemsPermissions(): FormArray {
    return this.accessItemsForm ? (this.accessItemsForm.get('accessItemsPermissions') as FormArray) : null;
  }

  ngOnInit() {
    this.groups$.pipe(takeUntil(this.destroy$)).subscribe((groups) => {
      this.groups = groups;
    });

    this.permissions$.pipe(takeUntil(this.destroy$)).subscribe((permissions) => {
      this.permissions = permissions;
    });

    this.requestInProgressService.setRequestInProgress(false);
  }

  onSelectAccessId(selected: AccessId) {
    if (selected) {
      this.accessId = selected;
      if (this.accessIdPrevious !== selected.accessId) {
        this.accessIdPrevious = selected.accessId;
        this.accessIdName = selected.name;
        this.store
          .dispatch(new GetGroupsByAccessId(selected.accessId))
          .pipe(takeUntil(this.destroy$))
          .subscribe(() => {
            this.searchForAccessItemsWorkbaskets();
          });
        this.store
          .dispatch(new GetPermissionsByAccessId(selected.accessId))
          .pipe(takeUntil(this.destroy$))
          .subscribe(() => {
            this.searchForAccessItemsWorkbaskets();
          });
      }
    } else {
      this.accessItemsForm = null;
    }
    this.customFields$ = this.accessItemsCustomization$.pipe(getCustomFields(customFieldCount));
  }

  searchForAccessItemsWorkbaskets() {
    this.removeFocus();
    if (this.permissions == null) {
      const filterParameter: WorkbasketAccessItemQueryFilterParameter = {
        'access-id': [this.accessId, ...this.groups].map((a) => a.accessId)
      };
      this.store
        .dispatch(new GetAccessItems(filterParameter, this.sortModel))
        .pipe(takeUntil(this.destroy$))
        .subscribe((state) => {
          this.setAccessItemsGroups(
            state['accessItemsManagement'].accessItemsResource
              ? state['accessItemsManagement'].accessItemsResource.accessItems
              : []
          );
        });
    } else {
      const filterParameter: WorkbasketAccessItemQueryFilterParameter = {
        'access-id': [this.accessId, ...this.groups, ...this.permissions].map((a) => a.accessId)
      };
      this.store
        .dispatch(new GetAccessItems(filterParameter, this.sortModel))
        .pipe(takeUntil(this.destroy$))
        .subscribe((state) => {
          this.setAccessItemsPermissions(
            state['accessItemsManagement'].accessItemsResource
              ? state['accessItemsManagement'].accessItemsResource.accessItems
              : []
          );
          this.setAccessItemsGroups(
            state['accessItemsManagement'].accessItemsResource
              ? state['accessItemsManagement'].accessItemsResource.accessItems
              : []
          );
        });
    }
  }

  setAccessItemsGroups(accessItems: Array<WorkbasketAccessItems>) {
    const AccessItemsFormGroups = accessItems.map((accessItem) => this.formBuilder.group(accessItem));
    AccessItemsFormGroups.forEach((accessItemGroup) => {
      accessItemGroup.controls.accessId.setValidators(Validators.required);
      Object.keys(accessItemGroup.controls).forEach((key) => {
        accessItemGroup.controls[key].disable();
      });
    });

    const AccessItemsFormArray = this.formBuilder.array(AccessItemsFormGroups);
    if (!this.accessItemsForm) {
      this.accessItemsForm = this.formBuilder.group({});
    }
    this.accessItemsForm.setControl('accessItemsGroups', AccessItemsFormArray);
    if (!this.accessItemsForm.value.workbasketKeyFilter) {
      this.accessItemsForm.addControl('workbasketKeyFilter', new FormControl());
    }
    if (!this.accessItemsForm.value.accessIdFilter) {
      this.accessItemsForm.addControl('accessIdFilter', new FormControl());
    }
    this.accessItems = accessItems;
    if (this.accessItemsForm.value.workbasketKeyFilter || this.accessItemsForm.value.accessIdFilter) {
      this.filterAccessItems();
    }
  }

  setAccessItemsPermissions(accessItems: Array<WorkbasketAccessItems>) {
    const AccessItemsFormPermissions = accessItems.map((accessItem) => this.formBuilder.group(accessItem));
    AccessItemsFormPermissions.forEach((accessItemPermission) => {
      accessItemPermission.controls.accessId.setValidators(Validators.required);
      Object.keys(accessItemPermission.controls).forEach((key) => {
        accessItemPermission.controls[key].disable();
      });
    });

    const AccessItemsFormArray = this.formBuilder.array(AccessItemsFormPermissions);
    if (!this.accessItemsForm) {
      this.accessItemsForm = this.formBuilder.group({});
    }
    this.accessItemsForm.setControl('accessItemsPermissions', AccessItemsFormArray);
    if (!this.accessItemsForm.value.workbasketKeyFilter) {
      this.accessItemsForm.addControl('workbasketKeyFilter', new FormControl());
    }
    if (!this.accessItemsForm.value.accessIdFilter) {
      this.accessItemsForm.addControl('accessIdFilter', new FormControl());
    }
    this.accessItems = accessItems;
    if (this.accessItemsForm.value.workbasketKeyFilter || this.accessItemsForm.value.accessIdFilter) {
      this.filterAccessItems();
    }
  }

  filterAccessItems() {
    if (this.accessItemsForm.value.accessIdFilter) {
      this.accessItems = this.accessItems.filter((value) =>
        value.accessName.toLowerCase().includes(this.accessItemsForm.value.accessIdFilter.toLowerCase())
      );
    }
    if (this.accessItemsForm.value.workbasketKeyFilter) {
      this.accessItems = this.accessItems.filter((value) =>
        value.workbasketKey.toLowerCase().includes(this.accessItemsForm.value.workbasketKeyFilter.toLowerCase())
      );
    }
  }

  revokeAccess() {
    this.notificationService.showDialog(
      'ACCESS_ITEM_MANAGEMENT_REVOKE_ACCESS',
      { accessId: this.accessId.accessId },
      () => {
        this.store
          .dispatch(new RemoveAccessItemsPermissions(this.accessId.accessId))
          .pipe(takeUntil(this.destroy$))
          .subscribe(() => {
            this.searchForAccessItemsWorkbaskets();
          });
      }
    );
  }

  isFieldValid(field: string, index: number): boolean {
    return (
      this.formsValidatorService.isFieldValid(this.accessItemsGroups[index], field) ||
      this.formsValidatorService.isFieldValid(this.accessItemsPermissions[index], field)
    );
  }

  sorting(sort: Sorting<WorkbasketAccessItemQuerySortParameter>) {
    this.sortModel = sort;
    this.searchForAccessItemsWorkbaskets();
  }

  removeFocus() {
    if (document.activeElement instanceof HTMLElement) {
      document.activeElement.focus();
    }
  }

  clearFilter() {
    if (this.accessItemsForm) {
      this.accessItemsForm.patchValue({
        workbasketKeyFilter: '',
        accessIdFilter: ''
      });
      this.searchForAccessItemsWorkbaskets();
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
