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

import { Component, inject, OnDestroy, OnInit } from '@angular/core';
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
  task: Task;
  taskClone: Task;
  requestInProgress = false;
  tabSelected = 'general';
  currentWorkbasket: Workbasket;
  currentId: string;
  showDetail = false;
  destroy$ = new Subject<void>();
  private route = inject(ActivatedRoute);
  private taskService = inject(TaskService);
  private workplaceService = inject(WorkplaceService);
  private router = inject(Router);
  private requestInProgressService = inject(RequestInProgressService);
  private notificationService = inject(NotificationService);
  private masterAndDetailService = inject(MasterAndDetailService);
  private routeSubscription: Subscription;
  private workbasketSubscription: Subscription;
  private masterAndDetailSubscription: Subscription;
  private deleteTaskSubscription: Subscription;

  ngOnInit() {
    this.workbasketSubscription = this.workplaceService.getSelectedWorkbasket().subscribe((workbasket) => {
      this.currentWorkbasket = workbasket;
    });

    this.routeSubscription = this.route.params.subscribe((params) => {
      this.currentId = params.id;
      // redirect if user enters through a deep-link
      if (!this.currentWorkbasket && this.currentId === 'new-task') {
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
        this.requestInProgress = value;
      });
  }

  resetTask(): void {
    this.task = { ...this.taskClone };
    this.task.customAttributes = this.taskClone.customAttributes.slice(0);
    this.task.callbackInfo = this.taskClone.callbackInfo.slice(0);
    this.task.primaryObjRef = { ...this.taskClone.primaryObjRef };
    this.notificationService.showSuccess('TASK_RESTORE');
  }

  getTask(): void {
    this.requestInProgressService.setRequestInProgress(true);
    if (this.currentId === 'new-task') {
      this.requestInProgressService.setRequestInProgress(false);
      this.task = new Task('', new ObjectReference(), this.currentWorkbasket);
    } else {
      this.taskService.getTask(this.currentId).subscribe({
        next: (task) => {
          this.requestInProgressService.setRequestInProgress(false);
          this.task = task;
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
    this.router.navigate([{ outlets: { detail: `task/${this.currentId}` } }], {
      relativeTo: this.route.parent,
      queryParamsHandling: 'merge'
    });
  }

  workOnTaskDisabled(): boolean {
    return this.task ? this.task.state === 'COMPLETED' : false;
  }

  deleteTask(): void {
    this.notificationService.showDialog(
      'TASK_DELETE',
      { taskId: this.currentId },
      this.deleteTaskConfirmation.bind(this)
    );
  }

  deleteTaskConfirmation(): void {
    this.deleteTaskSubscription = this.taskService
      .deleteTask(this.task)
      .pipe(take(1))
      .subscribe(() => {
        this.notificationService.showSuccess('TASK_DELETE', { taskName: this.task.name });
        this.taskService.publishTaskDeletion();
        this.task = null;
        this.router.navigate(['kadai/workplace/tasks'], { queryParamsHandling: 'merge' });
      });
  }

  selectTab(tab: string): void {
    this.tabSelected = tab;
  }

  backClicked(): void {
    delete this.task;
    this.taskService.selectTask(this.task);
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
    this.currentId === 'new-task' ? this.createTask() : this.updateTask();
  }

  private updateTask() {
    this.requestInProgressService.setRequestInProgress(true);
    trimObject(this.task);
    this.taskService.updateTask(this.task).subscribe({
      next: (task) => {
        this.requestInProgressService.setRequestInProgress(false);
        this.task = task;
        this.cloneTask();
        this.taskService.publishUpdatedTask(task);
        this.notificationService.showSuccess('TASK_UPDATE', { taskName: task.name });
      },
      error: () => {
        this.requestInProgressService.setRequestInProgress(false);
      }
    });
  }

  private createTask() {
    this.requestInProgressService.setRequestInProgress(true);
    this.addDateToTask();
    trimObject(this.task);
    this.taskService.createTask(this.task).subscribe({
      next: (task) => {
        this.requestInProgressService.setRequestInProgress(false);
        this.notificationService.showSuccess('TASK_CREATE', { taskName: task.name });
        this.task = task;
        this.taskService.selectTask(this.task);
        this.taskService.publishUpdatedTask(task);
        this.router.navigate([`../${task.taskId}`], {
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
    const date = KadaiDate.getDate();
    this.task.created = date;
    this.task.modified = date;
  }

  private cloneTask() {
    this.taskClone = { ...this.task };
    this.taskClone.customAttributes = this.task.customAttributes.slice(0);
    this.taskClone.callbackInfo = this.task.callbackInfo.slice(0);
    this.taskClone.primaryObjRef = { ...this.task.primaryObjRef };
  }
}
