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

import { Component, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { Subject, Subscription } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';

import { TaskService } from 'app/workplace/services/task.service';
import { Task } from 'app/workplace/models/task';
import { RequestInProgressService } from 'app/shared/services/request-in-progress/request-in-progress.service';
import { KadaiDate } from 'app/shared/util/kadai.date';
import { Workbasket } from 'app/shared/models/workbasket';
import { WorkplaceService } from 'app/workplace/services/workplace.service';
import { MasterAndDetailService } from 'app/shared/services/master-and-detail/master-and-detail.service';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import { take, takeUntil } from 'rxjs/operators';
import { trimObject } from '../../../shared/util/form-trimmer';
import { ObjectReference } from '../../models/object-reference';

import { MatButton } from '@angular/material/button';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIcon } from '@angular/material/icon';
import { MatMenu, MatMenuItem, MatMenuTrigger } from '@angular/material/menu';
import { MatTab, MatTabGroup } from '@angular/material/tabs';
import { TaskInformationComponent } from '../task-information/task-information.component';
import { TaskStatusDetailsComponent } from '../task-status-details/task-status-details.component';
import { TaskCustomFieldsComponent } from '../task-custom-fields/task-custom-fields.component';
import { TaskAttributeValueComponent } from '../task-attribute-value/task-attribute-value.component';

@Component({
  selector: 'kadai-task-details',
  templateUrl: './task-details.component.html',
  styleUrls: ['./task-details.component.scss'],
  imports: [
    MatButton,
    MatTooltip,
    MatIcon,
    MatMenuTrigger,
    MatMenu,
    MatMenuItem,
    MatTabGroup,
    MatTab,
    TaskInformationComponent,
    TaskStatusDetailsComponent,
    TaskCustomFieldsComponent,
    TaskAttributeValueComponent
  ]
})
export class TaskDetailsComponent implements OnInit, OnDestroy {
  task = signal<Task | undefined>(undefined);
  taskClone?: Task;
  requestInProgress = signal(false);
  tabSelected = 'general';
  currentWorkbasket?: Workbasket;
  currentId = signal<string>('');
  showDetail = false;
  toggleFormValidation = false;
  destroy$ = new Subject<void>();
  private route = inject(ActivatedRoute);
  private taskService = inject(TaskService);
  private workplaceService = inject(WorkplaceService);
  private router = inject(Router);
  private requestInProgressService = inject(RequestInProgressService);
  private notificationService = inject(NotificationService);
  private masterAndDetailService = inject(MasterAndDetailService);
  private routeSubscription!: Subscription;
  private workbasketSubscription!: Subscription;
  private masterAndDetailSubscription!: Subscription;
  private deleteTaskSubscription!: Subscription;

  ngOnInit() {
    this.workbasketSubscription = this.workplaceService.getSelectedWorkbasket().subscribe((workbasket) => {
      this.currentWorkbasket = workbasket;
    });

    this.routeSubscription = this.route.params.subscribe((params) => {
      this.currentId.set(params.id);
      // redirect if user enters through a deep-link
      if (!this.currentWorkbasket && this.currentId() === 'new-task') {
        this.router.navigate([''], { queryParamsHandling: 'merge' });
      }
      this.getTask();
    });

    this.masterAndDetailSubscription = this.masterAndDetailService.getShowDetail().subscribe((showDetail) => {
      this.showDetail = showDetail;
    });

    this.requestInProgressService
      .getRequestInProgress()
      .pipe(takeUntil(this.destroy$))
      .subscribe((value) => {
        this.requestInProgress.set(value);
      });
  }

  resetTask(): void {
    if (!this.taskClone) {
      return;
    }
    const task = { ...this.taskClone };
    task.customAttributes = this.taskClone.customAttributes?.slice(0) || [];
    task.callbackInfo = this.taskClone.callbackInfo?.slice(0) || [];
    task.primaryObjRef = this.taskClone.primaryObjRef ? { ...this.taskClone.primaryObjRef } : undefined;
    this.task.set(task);
    this.notificationService.showSuccess('TASK_RESTORE');
  }

  getTask(): void {
    this.requestInProgressService.setRequestInProgress(true);
    if (this.currentId() === 'new-task') {
      this.requestInProgressService.setRequestInProgress(false);
      this.task.set(new Task('', new ObjectReference(), this.currentWorkbasket));
    } else {
      this.taskService.getTask(this.currentId()).subscribe({
        next: (task) => {
          this.requestInProgressService.setRequestInProgress(false);
          this.task.set(task);
          this.cloneTask();
          this.taskService.selectTask(task);
        },
        error: () => {
          this.requestInProgressService.setRequestInProgress(false);
        }
      });
    }
  }

  openTask() {
    this.router.navigate([{ outlets: { detail: `task/${this.currentId()}` } }], {
      relativeTo: this.route.parent,
      queryParamsHandling: 'merge'
    });
  }

  workOnTaskDisabled(): boolean {
    const task = this.task();
    return task ? task.state === 'COMPLETED' : false;
  }

  deleteTask(): void {
    this.notificationService.showDialog(
      'TASK_DELETE',
      { taskId: this.currentId() },
      this.deleteTaskConfirmation.bind(this)
    );
  }

  deleteTaskConfirmation(): void {
    const taskToDelete = this.task();
    if (!taskToDelete) return;
    this.deleteTaskSubscription = this.taskService
      .deleteTask(taskToDelete)
      .pipe(take(1))
      .subscribe(() => {
        this.notificationService.showSuccess('TASK_DELETE', { taskName: taskToDelete.name });
        this.taskService.publishTaskDeletion();
        this.task.set(undefined);
        this.router.navigate(['kadai/workplace/tasks'], { queryParamsHandling: 'merge' });
      });
  }

  selectTab(tab: string): void {
    this.tabSelected = tab;
  }

  backClicked(): void {
    this.task.set(undefined);
    this.taskService.selectTask(undefined);
    this.router.navigate(['./'], { relativeTo: this.route.parent, queryParamsHandling: 'merge' });
  }

  ngOnDestroy(): void {
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }
    if (this.workbasketSubscription) {
      this.workbasketSubscription.unsubscribe();
    }
    if (this.masterAndDetailSubscription) {
      this.masterAndDetailSubscription.unsubscribe();
    }
    if (this.deleteTaskSubscription) {
      this.deleteTaskSubscription.unsubscribe();
    }

    this.destroy$.next();
    this.destroy$.complete();
  }

  onSave() {
    this.currentId() === 'new-task' ? this.createTask() : this.updateTask();
  }

  private updateTask() {
    const task = this.task();
    if (!task) return;
    this.requestInProgressService.setRequestInProgress(true);
    trimObject(task);
    this.taskService.updateTask(task).subscribe({
      next: (updatedTask) => {
        this.requestInProgressService.setRequestInProgress(false);
        this.task.set(updatedTask);
        this.cloneTask();
        this.taskService.publishUpdatedTask(updatedTask);
        this.notificationService.showSuccess('TASK_UPDATE', { taskName: updatedTask.name });
      },
      error: () => {
        this.requestInProgressService.setRequestInProgress(false);
      }
    });
  }

  private createTask() {
    const task = this.task();
    if (!task) return;
    this.requestInProgressService.setRequestInProgress(true);
    this.addDateToTask();
    trimObject(task);
    this.taskService.createTask(task).subscribe({
      next: (createdTask) => {
        this.requestInProgressService.setRequestInProgress(false);
        this.notificationService.showSuccess('TASK_CREATE', { taskName: createdTask.name });
        this.task.set(createdTask);
        this.taskService.selectTask(createdTask);
        this.taskService.publishUpdatedTask(createdTask);
        this.router.navigate([`../${createdTask.taskId}`], {
          relativeTo: this.route,
          queryParamsHandling: 'merge'
        });
      },
      error: () => {
        this.requestInProgressService.setRequestInProgress(false);
      }
    });
  }

  private addDateToTask() {
    const task = this.task();
    if (!task) return;
    const date = KadaiDate.getDate();
    task.created = date;
    task.modified = date;
  }

  private cloneTask() {
    const task = this.task();
    if (!task) return;
    this.taskClone = { ...task };
    this.taskClone.customAttributes = task.customAttributes?.slice(0) || [];
    this.taskClone.callbackInfo = task.callbackInfo?.slice(0) || [];
    this.taskClone.primaryObjRef = task.primaryObjRef ? { ...task.primaryObjRef } : undefined;
  }
}
