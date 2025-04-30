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

import { Component, inject, Input, OnChanges, OnDestroy, OnInit, SimpleChanges, ViewChild } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { FormsModule, NgForm } from '@angular/forms';
import { Select, Store } from '@ngxs/store';
import { ACTION } from 'app/shared/models/action';
import { customFieldCount, Workbasket } from 'app/shared/models/workbasket';
import { KadaiDate } from 'app/shared/util/kadai.date';
import { WorkbasketService } from 'app/shared/services/workbasket/workbasket.service';
import { RequestInProgressService } from 'app/shared/services/request-in-progress/request-in-progress.service';
import { FormsValidatorService } from 'app/shared/services/forms-validator/forms-validator.service';
import { filter, map, takeUntil } from 'rxjs/operators';
import { EngineConfigurationSelectors } from 'app/shared/store/engine-configuration-store/engine-configuration.selectors';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import { CustomField, getCustomFields, WorkbasketsCustomisation } from '../../../shared/models/customisation';
import {
  MarkWorkbasketForDeletion,
  RemoveDistributionTarget,
  SaveNewWorkbasket,
  UpdateWorkbasket
} from '../../../shared/store/workbasket-store/workbasket.actions';
import { WorkbasketComponent } from '../../models/workbasket-component';
import { WorkbasketSelectors } from '../../../shared/store/workbasket-store/workbasket.selectors';
import { ButtonAction } from '../../models/button-action';
import { AccessId } from '../../../shared/models/access-id';
import { cloneDeep } from 'lodash';
import { trimForm } from '../../../shared/util/form-trimmer';
import { AsyncPipe, NgFor, NgIf } from '@angular/common';
import { MatDivider } from '@angular/material/divider';
import { MatError, MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { FieldErrorDisplayComponent } from '../../../shared/components/field-error-display/field-error-display.component';
import { TypeAheadComponent } from '../../../shared/components/type-ahead/type-ahead.component';
import { MatSelect, MatSelectTrigger } from '@angular/material/select';
import { IconTypeComponent } from '../type-icon/icon-type.component';
import { MatOption } from '@angular/material/core';
import { CdkTextareaAutosize } from '@angular/cdk/text-field';
import { MapValuesPipe } from '../../../shared/pipes/map-values.pipe';
import { RemoveNoneTypePipe } from '../../../shared/pipes/remove-empty-type.pipe';

@Component({
  selector: 'kadai-administration-workbasket-information',
  templateUrl: './workbasket-information.component.html',
  styleUrls: ['./workbasket-information.component.scss'],
  imports: [
    NgIf,
    FormsModule,
    MatDivider,
    MatFormField,
    MatLabel,
    MatInput,
    FieldErrorDisplayComponent,
    TypeAheadComponent,
    MatSelect,
    MatSelectTrigger,
    IconTypeComponent,
    NgFor,
    MatOption,
    CdkTextareaAutosize,
    MatError,
    AsyncPipe,
    MapValuesPipe,
    RemoveNoneTypePipe
  ]
})
export class WorkbasketInformationComponent implements OnInit, OnChanges, OnDestroy {
  @Input()
  workbasket: Workbasket;
  @Input()
  action: ACTION;
  @ViewChild('WorkbasketForm')
  workbasketForm: NgForm;
  workbasketClone: Workbasket;
  allTypes: Map<string, string>;
  toggleValidationMap = new Map<string, boolean>();
  lookupField = false;
  isOwnerValid: boolean = true;
  readonly lengthError = 'You have reached the maximum length for this field';
  inputOverflowMap = new Map<string, boolean>();
  validateInputOverflow: Function;
  @Select(EngineConfigurationSelectors.workbasketsCustomisation)
  workbasketsCustomisation$: Observable<WorkbasketsCustomisation>;
  @Select(WorkbasketSelectors.buttonAction)
  buttonAction$: Observable<ButtonAction>;
  @Select(WorkbasketSelectors.selectedComponent)
  selectedComponent$: Observable<WorkbasketComponent>;
  customFields$: Observable<CustomField[]>;
  destroy$ = new Subject<void>();
  private workbasketService = inject(WorkbasketService);
  private requestInProgressService = inject(RequestInProgressService);
  private formsValidatorService = inject(FormsValidatorService);
  private notificationService = inject(NotificationService);
  private store = inject(Store);

  ngOnInit() {
    this.allTypes = new Map([
      ['PERSONAL', 'Personal'],
      ['GROUP', 'Group'],
      ['CLEARANCE', 'Clearance'],
      ['TOPIC', 'Topic']
    ]);

    this.customFields$ = this.workbasketsCustomisation$.pipe(
      map((customisation) => customisation.information),
      getCustomFields(customFieldCount)
    );
    this.workbasketsCustomisation$.pipe(takeUntil(this.destroy$)).subscribe((workbasketsCustomization) => {
      if (workbasketsCustomization.information.owner) {
        this.lookupField = workbasketsCustomization.information.owner.lookupField;
      }
    });
    this.formsValidatorService.inputOverflowObservable.pipe(takeUntil(this.destroy$)).subscribe((inputOverflowMap) => {
      this.inputOverflowMap = inputOverflowMap;
    });
    this.validateInputOverflow = (inputFieldModel, maxLength) => {
      if (typeof inputFieldModel.value !== 'undefined') {
        this.formsValidatorService.validateInputOverflow(inputFieldModel, maxLength);
      }
    };
    this.buttonAction$
      .pipe(takeUntil(this.destroy$))
      .pipe(filter((buttonAction) => typeof buttonAction !== 'undefined'))
      .subscribe((button) => {
        switch (button) {
          case ButtonAction.SAVE:
            this.onSubmit();
            break;
          case ButtonAction.UNDO:
            this.onUndo();
            break;
          case ButtonAction.REMOVE_AS_DISTRIBUTION_TARGETS:
            this.removeDistributionTargets();
            break;
          case ButtonAction.DELETE:
            this.removeWorkbasket();
            break;
          default:
            break;
        }
      });
  }

  ngOnChanges(changes?: SimpleChanges) {
    this.workbasketClone = { ...this.workbasket };
  }

  onSubmit() {
    this.formsValidatorService.formSubmitAttempt = true;
    trimForm(this.workbasketForm);
    this.formsValidatorService.validateFormInformation(this.workbasketForm, this.toggleValidationMap).then((value) => {
      if (value && this.isOwnerValid) {
        this.onSave();
      } else {
        this.notificationService.showError('WORKBASKET_SAVE');
      }
    });
  }

  isFieldValid(field: string): boolean {
    return this.formsValidatorService.isFieldValid(this.workbasketForm, field);
  }

  onUndo() {
    this.formsValidatorService.formSubmitAttempt = false;
    this.notificationService.showSuccess('WORKBASKET_RESTORE');
    this.workbasket = { ...this.workbasketClone };
  }

  removeWorkbasket() {
    this.notificationService.showDialog(
      'WORKBASKET_DELETE',
      { workbasketKey: this.workbasket.key },
      this.onRemoveConfirmed.bind(this)
    );
  }

  removeDistributionTargets() {
    this.store.dispatch(new RemoveDistributionTarget(this.workbasket._links.removeDistributionTargets.href));
  }

  onSave() {
    this.beforeRequest();
    if (!this.workbasket.workbasketId) {
      this.postNewWorkbasket();
    } else {
      this.store.dispatch(new UpdateWorkbasket(this.workbasket._links.self.href, this.workbasket)).subscribe(() => {
        this.requestInProgressService.setRequestInProgress(false);
        this.workbasketClone = cloneDeep(this.workbasket);
      });
    }
  }

  beforeRequest() {
    this.requestInProgressService.setRequestInProgress(true);
  }

  afterRequest() {
    this.requestInProgressService.setRequestInProgress(false);
    this.workbasketService.triggerWorkBasketSaved();
  }

  postNewWorkbasket() {
    this.addDateToWorkbasket();
    this.store.dispatch(new SaveNewWorkbasket(this.workbasket)).subscribe(() => {
      this.afterRequest();
    });
  }

  addDateToWorkbasket() {
    const date = KadaiDate.getDate();
    this.workbasket.created = date;
    this.workbasket.modified = date;
  }

  onRemoveConfirmed() {
    this.beforeRequest();
    this.store.dispatch(new MarkWorkbasketForDeletion(this.workbasket._links.self.href)).subscribe(() => {
      this.afterRequest();
    });
  }

  onSelectedOwner(owner: AccessId) {
    this.workbasket.owner = owner.accessId;
  }

  getWorkbasketCustomProperty(custom: number) {
    return `custom${custom}`;
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
