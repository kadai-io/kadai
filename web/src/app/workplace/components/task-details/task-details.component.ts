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
import { toSignal } from '@angular/core/rxjs-interop';
import { Subject, Subscription } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';

import { Task } from 'app/workplace/models/task';
import { RequestInProgressService } from 'app/shared/services/request-in-progress/request-in-progress.service';
import { KadaiDate } from 'app/shared/util/kadai.date';
import { Workbasket } from 'app/shared/models/workbasket';
import { MasterAndDetailService } from 'app/shared/services/master-and-detail/master-and-detail.service';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import { takeUntil } from 'rxjs/operators';
import { trimObject } from '../../../shared/util/form-trimmer';

import { MatButton } from '@angular/material/button';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIcon } from '@angular/material/icon';
import { MatMenu, MatMenuItem, MatMenuTrigger } from '@angular/material/menu';
import { MatTab, MatTabGroup } from '@angular/material/tabs';
import { TaskInformationComponent } from '../task-information/task-information.component';
import { TaskStatusDetailsComponent } from '../task-status-details/task-status-details.component';
import { TaskCustomFieldsComponent } from '../task-custom-fields/task-custom-fields.component';
import { TaskAttributeValueComponent } from '../task-attribute-value/task-attribute-value.component';
import { Store } from '@ngxs/store';
import { TaskSelectors } from '../../../shared/store/task-store/task.selectors';
import { CreateTask, DeleteTask, GetTask, SelectTask, UpdateTask } from '../../../shared/store/task-store/task.actions';

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
  private store = inject(Store);

  task = signal<Task | undefined>(undefined);
  taskClone?: Task;
  requestInProgress = signal(false);
  tabSelected = 'general';
  currentWorkbasket = toSignal<Workbasket | undefined>(this.store.select(TaskSelectors.getSelectedWorkbasket));
  currentId = signal<string>('');
  showDetail = false;
  toggleFormValidation = false;
  destroy$ = new Subject<void>();
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private requestInProgressService = inject(RequestInProgressService);
  private notificationService = inject(NotificationService);
  private masterAndDetailService = inject(MasterAndDetailService);
  private routeSubscription!: Subscription;
  private masterAndDetailSubscription!: Subscription;
  private deleteTaskSubscription!: Subscription;

  ngOnInit() {
    this.store
      .select(TaskSelectors.getSelectedTask)
      .pipe(takeUntil(this.destroy$))
      .subscribe((task) => {
        this.task.set(task);
        if (task) {
          this.cloneTask();
        }
      });

    this.routeSubscription = this.route.params.subscribe((params) => {
      this.currentId.set(params.id);
      // redirect if user enters through a deep-link
      if (!this.currentWorkbasket() && this.currentId() === 'new-task') {
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
    this.store.dispatch(new GetTask(this.currentId()));
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
    const task = this.task();
    if (!task) return;
    this.deleteTaskSubscription = this.store.dispatch(new DeleteTask(task)).subscribe(() => {
      this.router.navigate(['kadai/workplace/tasks'], { queryParamsHandling: 'merge' });
    });
  }

  selectTab(tab: string): void {
    this.tabSelected = tab;
  }

  backClicked(): void {
    this.store.dispatch(new SelectTask(undefined));
    this.router.navigate(['./'], { relativeTo: this.route.parent, queryParamsHandling: 'merge' });
  }

  ngOnDestroy(): void {
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
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
    trimObject(task);
    this.store.dispatch(new UpdateTask(task));
  }

  private createTask() {
    const task = this.task();
    if (!task) return;
    this.addDateToTask();
    trimObject(task);
    this.store.dispatch(new CreateTask(task)).subscribe(() => {
      this.router.navigate([`../${task.taskId}`], {
        relativeTo: this.route,
        queryParamsHandling: 'merge'
      });
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
