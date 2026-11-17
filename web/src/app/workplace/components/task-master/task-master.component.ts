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
import { TaskQuerySortParameter } from 'app/shared/models/sorting';
import { OrientationService } from 'app/shared/services/orientation/orientation.service';
import { Subject } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { TaskListToolbarComponent } from 'app/workplace/components/task-list-toolbar/task-list-toolbar.component';
import { RequestInProgressService } from 'app/shared/services/request-in-progress/request-in-progress.service';

import { TaskListComponent } from 'app/workplace/components/task-list/task-list.component';
import { PaginationComponent } from 'app/shared/components/pagination/pagination.component';
import { Store } from '@ngxs/store';
import { TaskSelectors } from 'app/shared/store/task-store/task.selectors';
import { SetPage, SetPageSize } from 'app/shared/store/task-store/task.actions';
import { WorkplaceSelectors } from 'app/shared/store/workplace-store/workplace.selectors';
import { CalculateNumberOfCards } from 'app/shared/store/workplace-store/workplace.actions';

@Component({
  selector: 'kadai-task-master',
  templateUrl: './task-master.component.html',
  styleUrls: ['./task-master.component.scss'],
  imports: [TaskListToolbarComponent, TaskListComponent, PaginationComponent]
})
export class TaskMasterComponent implements OnInit, OnDestroy {
  type = 'tasks';
  taskDefaultSortBy: TaskQuerySortParameter = TaskQuerySortParameter.PRIORITY;
  destroy$ = new Subject();
  private store = inject(Store);
  private orientationService = inject(OrientationService);
  private requestInProgressService = inject(RequestInProgressService);

  tasks = toSignal(this.store.select(TaskSelectors.getTasks));
  tasksPageInformation = toSignal(this.store.select(TaskSelectors.getPage));
  selectedId = toSignal(this.store.select(TaskSelectors.getSelectedTask).pipe(map((task) => task?.taskId ?? '')));
  requestInProgress = toSignal(this.requestInProgressService.getRequestInProgress());
  private cards$ = this.store.select(WorkplaceSelectors.getNumberOfCards);

  ngOnInit() {
    this.cards$.pipe(takeUntil(this.destroy$)).subscribe((cards) => {
      this.store.dispatch(new SetPageSize(cards));
    });

    this.orientationService
      .getOrientation()
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.store.dispatch(new CalculateNumberOfCards());
      });
  }

  changePage(page: number) {
    this.store.dispatch(new SetPage(page));
  }

  ngOnDestroy(): void {
    this.destroy$.next(null);
    this.destroy$.complete();
  }
}
