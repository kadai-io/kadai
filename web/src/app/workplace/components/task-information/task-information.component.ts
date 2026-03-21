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
  ChangeDetectionStrategy,
  Component,
  effect,
  inject,
  input,
  model,
  OnDestroy,
  OnInit,
  output,
  signal,
  untracked,
  viewChild
} from '@angular/core';
import { Task } from 'app/workplace/models/task';
import { FormsValidatorService } from 'app/shared/services/forms-validator/forms-validator.service';
import { FormsModule, NgForm } from '@angular/forms';
import { Observable, Subject } from 'rxjs';
import { EngineConfigurationSelectors } from 'app/shared/store/engine-configuration-store/engine-configuration.selectors';
import { ClassificationsService } from '../../../shared/services/classifications/classifications.service';
import { Classification } from '../../../shared/models/classification';
import { TasksCustomisation } from '../../../shared/models/customisation';
import { takeUntil } from 'rxjs/operators';
import { AccessId } from '../../../shared/models/access-id';
import { AsyncPipe } from '@angular/common';
import { MatFormField, MatLabel, MatSuffix } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatSelect } from '@angular/material/select';
import { MatTooltip } from '@angular/material/tooltip';
import { CdkTextareaAutosize } from '@angular/cdk/text-field';
import { FieldErrorDisplayComponent } from '../../../shared/components/field-error-display/field-error-display.component';
import { TypeAheadComponent } from '../../../shared/components/type-ahead/type-ahead.component';
import { MatNativeDateModule, MatOption } from '@angular/material/core';
import {
  MatDatepicker,
  MatDatepickerInput,
  MatDatepickerModule,
  MatDatepickerToggle
} from '@angular/material/datepicker';
import { Store } from '@ngxs/store';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'kadai-task-information',
  templateUrl: './task-information.component.html',
  styleUrls: ['./task-information.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    MatFormField,
    MatLabel,
    MatInput,
    MatSelect,
    MatOption,
    MatTooltip,
    MatDatepickerInput,
    MatDatepickerToggle,
    MatSuffix,
    MatDatepicker,
    CdkTextareaAutosize,
    AsyncPipe,
    FieldErrorDisplayComponent,
    TypeAheadComponent,
    MatDatepickerModule,
    MatNativeDateModule
  ]
})
export class TaskInformationComponent implements OnInit, OnDestroy {
  task = model<Task>();
  saveToggleTriggered = input<boolean>();
  formValid = output<boolean>();
  taskForm = viewChild<NgForm>('TaskForm');
  toggleValidationMap = new Map<string, boolean>();
  requestInProgress = signal(false);
  classifications = signal<Classification[]>([]);
  isClassificationEmpty: boolean;
  isOwnerValid: boolean = true;
  readonly lengthError = 'You have reached the maximum length';
  inputOverflowMap = toSignal(inject(FormsValidatorService).inputOverflowObservable, {
    initialValue: new Map<string, boolean>()
  });
  validateInputOverflow: Function;
  tasksCustomisation$: Observable<TasksCustomisation> = inject(Store).select(
    EngineConfigurationSelectors.tasksCustomisation
  );
  private classificationService = inject(ClassificationsService);
  private formsValidatorService = inject(FormsValidatorService);
  private destroy$ = new Subject<void>();

  constructor() {
    effect(() => {
      const saveToggle = this.saveToggleTriggered();
      if (saveToggle !== undefined) {
        untracked(() => this.validate());
      }
    });
  }

  ngOnInit() {
    this.getClassificationByDomain();
    this.validateInputOverflow = (inputFieldModel, maxLength) => {
      this.formsValidatorService.validateInputOverflow(inputFieldModel, maxLength);
    };
  }

  isFieldValid(field: string): boolean {
    return this.formsValidatorService.isFieldValid(this.taskForm(), field);
  }

  updateDate($event) {
    const newDate = $event.value;
    if (typeof newDate !== 'undefined' && newDate !== null) {
      const newDateISOString = newDate.toISOString();
      const task = this.task();
      const currentDate = new Date(task.due);
      if (typeof currentDate === 'undefined' || currentDate !== newDate) {
        task.due = newDateISOString;
      }
    }
  }

  changedClassification(itemSelected: Classification) {
    this.task().classificationSummary = itemSelected;
    this.isClassificationEmpty = false;
  }

  onSelectedOwner(owner: AccessId) {
    if (owner?.accessId) {
      this.task().owner = owner.accessId;
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private validate() {
    const task = this.task();
    this.isClassificationEmpty = typeof task.classificationSummary === 'undefined';
    this.formsValidatorService.formSubmitAttempt = true;
    this.formsValidatorService.validateFormInformation(this.taskForm(), this.toggleValidationMap).then((value) => {
      if (value && !this.isClassificationEmpty && this.isOwnerValid) {
        this.formValid.emit(true);
      }
    });
  }

  // TODO: this is currently called for every selected task and is only necessary when we switch the workbasket -> can be optimized.
  private getClassificationByDomain() {
    this.requestInProgress.set(true);
    this.classificationService
      .getClassifications({ domain: [this.task().workbasketSummary.domain] })
      .pipe(takeUntil(this.destroy$))
      .subscribe((classificationPagingList) => {
        this.classifications.set(classificationPagingList.classifications);
        this.requestInProgress.set(false);
      });
  }
}
