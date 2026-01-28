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

import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { Task } from 'app/workplace/models/task';
import { TaskService } from 'app/workplace/services/task.service';
import { Observable, Subject } from 'rxjs';
import { Direction, Sorting, TaskQuerySortParameter } from 'app/shared/models/sorting';
import { Workbasket } from 'app/shared/models/workbasket';
import { WorkplaceService } from 'app/workplace/services/workplace.service';
import { OrientationService } from 'app/shared/services/orientation/orientation.service';
import { Page } from 'app/shared/models/page';
import { take, takeUntil } from 'rxjs/operators';
import { Search, TaskListToolbarComponent } from '../task-list-toolbar/task-list-toolbar.component';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import { QueryPagingParameter } from '../../../shared/models/query-paging-parameter';
import { TaskQueryFilterParameter } from '../../../shared/models/task-query-filter-parameter';
import { Store } from '@ngxs/store';
import { FilterSelectors } from '../../../shared/store/filter-store/filter.selectors';
import { WorkplaceSelectors } from '../../../shared/store/workplace-store/workplace.selectors';
import { CalculateNumberOfCards } from '../../../shared/store/workplace-store/workplace.actions';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';

import { TaskListComponent } from '../task-list/task-list.component';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';

@Component({
  selector: 'kadai-task-master',
  templateUrl: './task-master.component.html',
  styleUrls: ['./task-master.component.scss'],
  imports: [TaskListToolbarComponent, TaskListComponent, PaginationComponent]
})
export class TaskMasterComponent implements OnInit, OnDestroy {
  tasks: Task[];
  tasksPageInformation: Page;
  type = 'tasks';
  currentBasket: Workbasket;
  selectedId = '';
  taskDefaultSortBy: TaskQuerySortParameter = TaskQuerySortParameter.PRIORITY;
  sort: Sorting<TaskQuerySortParameter> = {
    'sort-by': this.taskDefaultSortBy,
    order: Direction.ASC
  };
  paging: QueryPagingParameter = {
    page: 1,
    'page-size': 9
  };
  filterBy: TaskQueryFilterParameter = {};
  requestInProgress = false;
  selectedSearchType: Search = Search.byWorkbasket;
  destroy$ = new Subject();
  filter$: Observable<TaskQueryFilterParameter> = inject(Store).select(FilterSelectors.getTaskFilter);
  cards$: Observable<number> = inject(Store).select(WorkplaceSelectors.getNumberOfCards);
  private taskService = inject(TaskService);
  private workplaceService = inject(WorkplaceService);
  private notificationsService = inject(NotificationService);
  private orientationService = inject(OrientationService);
  private store = inject(Store);
  private requestInProgressService = inject(RequestInProgressService);

  ngOnInit() {
    this.cards$.pipe(takeUntil(this.destroy$)).subscribe((cards) => {
      this.paging['page-size'] = cards;
      this.getTasks();
    });

    this.taskService.taskSelectedStream.pipe(takeUntil(this.destroy$)).subscribe((task: Task) => {
      this.selectedId = task ? task.taskId : '';
      if (!this.tasks) {
        this.currentBasket = task.workbasketSummary;
        this.getTasks();
      }
    });

    this.taskService.taskChangedStream.pipe(takeUntil(this.destroy$)).subscribe((task) => {
      this.currentBasket = task.workbasketSummary;
      this.getTasks();
    });

    this.taskService.taskDeletedStream.pipe(takeUntil(this.destroy$)).subscribe(() => {
      this.selectedId = '';
      this.getTasks();
    });

    this.orientationService
      .getOrientation()
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.store.dispatch(new CalculateNumberOfCards());
      });

    this.workplaceService
      .getSelectedWorkbasket()
      .pipe(takeUntil(this.destroy$))
      .subscribe((workbasket) => {
        this.currentBasket = workbasket;
        if (this.selectedSearchType === Search.byWorkbasket) {
          this.getTasks();
        }
      });
  }

  performSorting(sort: Sorting<TaskQuerySortParameter>) {
    this.sort = sort;
    this.getTasks();
  }

  performFilter() {
    this.paging.page = 1;
    this.filter$.pipe(take(1)).subscribe((filter) => {
      this.filterBy = { ...filter };
      this.getTasks();
    });
  }

  selectSearchType(type: Search) {
    this.selectedSearchType = type;
    this.tasks = [];
  }

  changePage(page) {
    this.paging.page = page;
    this.getTasks();
  }

  ngOnDestroy(): void {
    this.destroy$.next(null);
    this.destroy$.complete();
  }

  private getTasks(): void {
    this.requestInProgress = true;
    this.requestInProgressService.setRequestInProgress(true);

    if (this.selectedSearchType === Search.byTypeAndValue) {
      delete this.currentBasket;
    }

    this.filterBy['workbasket-id'] = [this.currentBasket?.workbasketId];

    if (this.selectedSearchType === Search.byWorkbasket && !this.currentBasket) {
      this.requestInProgress = false;
      this.requestInProgressService.setRequestInProgress(false);
      this.tasks = [];
    } else {
      this.taskService
        .findTasksWithWorkbasket(this.filterBy, this.sort, this.paging)
        .pipe(take(1))
        .subscribe((taskResource) => {
          this.requestInProgress = false;
          this.requestInProgressService.setRequestInProgress(false);
          if (taskResource.tasks && taskResource.tasks.length > 0) {
            this.tasks = taskResource.tasks;
          } else {
            this.tasks = [];
            if (this.selectedSearchType === Search.byWorkbasket) {
              this.notificationsService.showInformation('EMPTY_WORKBASKET');
            }
          }
          this.tasksPageInformation = taskResource.page;
        });
    }
  }
}
