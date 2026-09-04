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

import { Component, computed, inject, isSignal, OnDestroy, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { Task } from 'app/workplace/models/task';
import { Workbasket } from 'app/shared/models/workbasket';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { WorkbasketService } from 'app/shared/services/workbasket/workbasket.service';
import {
  catchError,
  distinctUntilChanged,
  EMPTY,
  filter,
  map,
  Observable,
  of,
  Subject,
  Subscription,
  switchMap,
  take,
  takeUntil
} from 'rxjs';
import { ClassificationsService } from 'app/shared/services/classifications/classifications.service';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';
import { MatButton } from '@angular/material/button';
import { MatTooltip } from '@angular/material/tooltip';
import { MatMenu, MatMenuItem, MatMenuTrigger } from '@angular/material/menu';
import { MatIcon } from '@angular/material/icon';

import { MatDivider } from '@angular/material/divider';
import { Store } from '@ngxs/store';
import {
  CancelClaimTask,
  ClaimTask,
  CompleteTask,
  GetTask,
  ReopenTask,
  TransferTask
} from '../../../shared/store/task-store/task.actions';
import { TaskSelectors } from '../../../shared/store/task-store/task.selectors';
import { NotificationService } from 'app/shared/services/notifications/notification.service';
import { Classification } from 'app/shared/models/classification';

@Component({
  selector: 'kadai-task-processing',
  templateUrl: './task-processing.component.html',
  styleUrls: ['./task-processing.component.scss'],
  imports: [MatButton, MatTooltip, MatMenuTrigger, MatIcon, MatMenu, MatMenuItem, MatDivider]
})
export class TaskProcessingComponent implements OnInit, OnDestroy {
  routeSubscription!: Subscription;
  regex = /\${(.*?)}/g;
  address = 'https://bing.com';
  link = signal<SafeResourceUrl | undefined>(undefined);
  workbaskets = signal<Workbasket[]>([]);
  private workbasketService = inject(WorkbasketService);
  private classificationService = inject(ClassificationsService);
  private requestInProgressService = inject(RequestInProgressService);
  private notificationService = inject(NotificationService);
  private store = inject(Store);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private sanitizer = inject(DomSanitizer);
  task = toSignal<Task | undefined>(this.store.select(TaskSelectors.getSelectedTask));
  canReopenTask = computed(() => {
    const state = this.task()?.state;
    return state === 'COMPLETED' || state === 'CANCELLED';
  });
  canClaimTask = computed(() => {
    const state = this.task()?.state;
    return !!state && state !== 'COMPLETED' && state !== 'CANCELLED' && state !== 'TERMINATED';
  });
  destroy$ = new Subject();

  ngOnInit() {
    this.routeSubscription = this.route.params
      .pipe(
        map((params) => params['id']),
        distinctUntilChanged(),
        switchMap((id) => {
          if (!id) {
            return EMPTY;
          }
          return this.loadAndClaimTask(id).pipe(
            catchError((error) => {
              return EMPTY;
            })
          );
        }),
        takeUntil(this.destroy$)
      )
      .subscribe(({ task, classification }) => {
        this.address = this.extractUrl(classification.applicationEntryPoint!) || `${this.address}?q=${task.name}`;
        this.link.set(this.sanitizer.bypassSecurityTrustResourceUrl(this.address));
        this.getWorkbaskets();
      });
  }

  loadAndClaimTask(id: string): Observable<{ task: Task; classification: Classification }> {
    return this.store.dispatch(new GetTask(id)).pipe(
      switchMap(() => this.store.select(TaskSelectors.getSelectedTask)),
      filter((task): task is Task => !!task),
      take(1),
      switchMap((task) => {
        if (this.canClaimTask()) {
          return this.store.dispatch(new ClaimTask(id)).pipe(map(() => task));
        }
        return of(task);
      }),
      switchMap((task) =>
        this.classificationService
          .getClassification(task.classificationSummary!.classificationId!)
          .pipe(map((classification) => ({ task, classification })))
      )
    );
  }

  getWorkbaskets() {
    this.requestInProgressService.setRequestInProgress(true);
    this.workbasketService.getAllWorkBaskets().subscribe((workbaskets) => {
      this.requestInProgressService.setRequestInProgress(false);
      const workbasketList = [...workbaskets.workbaskets];

      const index = workbasketList.findIndex((workbasket) => workbasket.name === this.task()?.workbasketSummary?.name);
      if (index !== -1) {
        workbasketList.splice(index, 1);
      }
      this.workbaskets.set(workbasketList);
    });
  }

  transferTask(workbasket: Workbasket) {
    this.store.dispatch(new TransferTask(this.task()!.taskId, workbasket.workbasketId!)).subscribe(() => {
      this.navigateBack();
    });
  }

  completeTask() {
    this.store.dispatch(new CompleteTask(this.task()!.taskId)).subscribe(() => {
      this.navigateBack();
    });
  }

  reopenTask() {
    this.notificationService.showDialog('TASK_REOPEN', { taskId: this.task()!.taskId }, () => {
      if (!this.task()!.taskId) return;
      this.store.dispatch(new ReopenTask(this.task()!.taskId)).subscribe(() => {
        this.navigateBack();
      });
    });
  }

  cancelClaimTask() {
    this.store.dispatch(new CancelClaimTask(this.task()!.taskId)).subscribe(() => {
      this.navigateBack();
    });
  }

  navigateBack() {
    this.router.navigate([{ outlets: { detail: `taskdetail/${this.task()!.taskId}` } }], {
      relativeTo: this.route.parent,
      queryParamsHandling: 'merge'
    });
  }

  ngOnDestroy(): void {
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }
  }

  private extractUrl(url: string): string {
    const me = this;
    const extractedExpressions = url.match(this.regex);
    if (!extractedExpressions) {
      return url;
    }
    let extractedUrl = url;
    extractedExpressions.forEach((expression) => {
      const parameter = expression.substring(2, expression.length - 1);
      let objectValue: any = me;
      parameter.split('.').forEach((property) => {
        objectValue = this.getReflectiveProperty(objectValue, property);
        if (isSignal(objectValue)) {
          objectValue = objectValue();
        }
      });
      extractedUrl = extractedUrl.replace(expression, objectValue);
    });
    return extractedUrl;
  }

  private getReflectiveProperty(scope: any, property: string) {
    return Reflect.get(scope, property);
  }
}
