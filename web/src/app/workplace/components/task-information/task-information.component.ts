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
  Component,
  effect,
  inject,
  input,
  model,
  OnInit,
  output,
  signal,
  untracked,
  viewChild,
  DestroyRef
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Task } from 'app/workplace/models/task';
import { FormGroup, FormsModule, NgForm } from '@angular/forms';
import { Observable, Subject } from 'rxjs';
import { EngineConfigurationSelectors } from 'app/shared/store/engine-configuration-store/engine-configuration.selectors';
import { ClassificationsService } from 'app/shared/services/classifications/classifications.service';
import { Classification } from 'app/shared/models/classification';
import { TasksCustomisation } from 'app/shared/models/customisation';
import { filter, take, takeUntil } from 'rxjs/operators';
import { AsyncPipe } from '@angular/common';
import { MatFormField, MatLabel, MatSuffix } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatSelect } from '@angular/material/select';
import { MatTooltip } from '@angular/material/tooltip';
import { CdkTextareaAutosize } from '@angular/cdk/text-field';
import { FieldErrorDisplayComponent } from 'app/shared/components/field-error-display/field-error-display.component';
import { TypeAheadComponent } from 'app/shared/components/type-ahead/type-ahead.component';
import { MatNativeDateModule, MatOption } from '@angular/material/core';
import {
  MatDatepicker,
  MatDatepickerInput,
  MatDatepickerInputEvent,
  MatDatepickerModule,
  MatDatepickerToggle
} from '@angular/material/datepicker';
import { Store } from '@ngxs/store';
import { OverflowFeedbackDirective } from 'app/shared/directives/overflow-feedback.directive';
import { FormFieldSubmitDirective } from 'app/shared/directives/form-field-submit.directive';
import { FormSubmitDirective } from 'app/shared/directives/form-submit.directive';
import { AccessIdExistsValidatorDirective } from 'app/shared/directives/access-id-exists-validator.directive';
import { NotificationService } from 'app/shared/services/notifications/notification.service';

@Component({
  selector: 'kadai-task-information',
  templateUrl: './task-information.component.html',
  styleUrls: ['./task-information.component.scss'],
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
    MatNativeDateModule,
    OverflowFeedbackDirective,
    FormSubmitDirective,
    FormFieldSubmitDirective,
    AccessIdExistsValidatorDirective
  ]
})
export class TaskInformationComponent implements OnInit {
  task = model<Task>();
  saveToggleTriggered = input<boolean>();
  formValid = output<boolean>();
  taskForm = viewChild<NgForm>('TaskForm');
  requestInProgress = signal(false);
  classifications = signal<Classification[]>([]);
  isClassificationEmpty!: boolean;
  readonly lengthError = 'You have reached the maximum length';
  tasksCustomisation$: Observable<TasksCustomisation | undefined> = inject(Store).select(
    EngineConfigurationSelectors.tasksCustomisation
  );
  private classificationService = inject(ClassificationsService);
  private notificationService = inject(NotificationService);
  private cancelPendingWait$ = new Subject<void>();
  private destroyRef = inject(DestroyRef);

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
  }

  updateDate($event: MatDatepickerInputEvent<Date>) {
    const newDate = $event.value;
    if (typeof newDate !== 'undefined' && newDate !== null) {
      const newDateISOString = newDate.toISOString();
      const task = this.task();
      if (!task) {
        return;
      }
      const currentDate = new Date(task.due!);
      if (typeof currentDate === 'undefined' || currentDate !== newDate) {
        task.due = newDateISOString;
      }
    }
  }

  changedClassification(itemSelected: Classification) {
    this.task()!.classificationSummary = itemSelected;
    this.isClassificationEmpty = false;
  }

  private validate() {
    const form = this.taskForm();
    if (!form) {
      return;
    }

    const task = this.task();
    this.isClassificationEmpty = !task?.classificationSummary;

    const formGroup = form.control;
    formGroup.markAllAsTouched();

    this.cancelPendingWait$.next();

    if (formGroup.pending) {
      formGroup.statusChanges
        .pipe(
          filter((status) => status !== 'PENDING'),
          take(1),
          takeUntil(this.cancelPendingWait$),
          takeUntilDestroyed(this.destroyRef)
        )
        .subscribe(() => {
          this.checkAndEmitValidity(formGroup);
        });
    } else {
      this.checkAndEmitValidity(formGroup);
    }
  }

  private checkAndEmitValidity(formGroup: FormGroup) {
    const ownerControl = formGroup.controls['task.owner'];

    if (formGroup.valid && !this.isClassificationEmpty) {
      this.formValid.emit(true);
    } else {
      if (ownerControl?.hasError('invalidAccessId') || ownerControl?.hasError('accessIdLookupError')) {
        this.notificationService.showError('OWNER_NOT_VALID', { owner: 'access id' });
      }
    }
  }

  // TODO: this is currently called for every selected task and is only necessary when we switch the workbasket -> can be optimized.
  private getClassificationByDomain() {
    this.requestInProgress.set(true);
    this.classificationService
      .getClassifications({ domain: [this.task()!.workbasketSummary!.domain!] })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((classificationPagingList) => {
        this.classifications.set(classificationPagingList.classifications);
        this.requestInProgress.set(false);
      });
  }
}
