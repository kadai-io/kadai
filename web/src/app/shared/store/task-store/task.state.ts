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

import { Action, State, StateContext, Store } from '@ngxs/store';
import { finalize, take, tap } from 'rxjs/operators';
import { Observable, of } from 'rxjs';
import { inject, Injectable } from '@angular/core';

import { TaskService } from '../../../workplace/services/task.service';
import { Task } from '../../../workplace/models/task';
import { ObjectReference } from '../../../workplace/models/object-reference';
import { SearchType } from '../../../workplace/models/search';
import { Workbasket } from '../../models/workbasket';
import { Page } from '../../models/page';
import { Direction, Sorting, TaskQuerySortParameter } from '../../models/sorting';
import { QueryPagingParameter } from '../../models/query-paging-parameter';
import { TaskQueryFilterParameter } from '../../models/task-query-filter-parameter';
import { NotificationService } from '../../services/notifications/notification.service';
import { RequestInProgressService } from '../../services/request-in-progress/request-in-progress.service';
import { FilterSelectors } from '../filter-store/filter.selectors';
import {
  CreateTask,
  DeleteTask,
  GetTask,
  LoadTasks,
  SelectTask,
  SelectWorkbasket,
  SetPage,
  SetPageSize,
  SetSearchType,
  SetSort,
  UpdateTask
} from './task.actions';

@Injectable({
  providedIn: 'root'
})
@State<TaskWorkflowStateModel>({
  name: 'task',
  defaults: {
    tasks: [],
    page: new Page(),
    selectedTask: undefined,
    selectedWorkbasket: undefined,
    selectedSearchType: SearchType.byWorkbasket,
    sort: {
      'sort-by': TaskQuerySortParameter.PRIORITY,
      order: Direction.ASC
    },
    paging: {
      page: 1,
      'page-size': 9
    }
  }
})
export class TaskWorkflowState {
  private taskService = inject(TaskService);
  private notificationService = inject(NotificationService);
  private requestInProgressService = inject(RequestInProgressService);
  private store = inject(Store);

  private withRequestInProgress<T>(source: Observable<T>): Observable<T> {
    this.requestInProgressService.setRequestInProgress(true);
    return source.pipe(
      take(1),
      finalize(() => this.requestInProgressService.setRequestInProgress(false))
    );
  }

  @Action(LoadTasks)
  loadTasks(ctx: StateContext<TaskWorkflowStateModel>): Observable<any> {
    const state = ctx.getState();

    if (state.selectedSearchType === SearchType.byWorkbasket && !state.selectedWorkbasket) {
      ctx.patchState({ tasks: [], page: new Page() });
      return of(null);
    }

    const filterBy: TaskQueryFilterParameter = {
      ...this.store.selectSnapshot(FilterSelectors.getTaskFilter)
    };

    if (state.selectedSearchType === SearchType.byTypeAndValue) {
      delete filterBy['workbasket-id'];
    } else if (state.selectedWorkbasket?.workbasketId) {
      filterBy['workbasket-id'] = [state.selectedWorkbasket.workbasketId];
    } else {
      delete filterBy['workbasket-id'];
    }

    return this.withRequestInProgress(
      this.taskService.findTasksWithWorkbasket(filterBy, state.sort, state.paging)
    ).pipe(
      tap((taskResource) => {
        ctx.patchState({
          tasks: taskResource.tasks ?? [],
          page: taskResource.page
        });
        if (
          (!taskResource.tasks || taskResource.tasks.length === 0) &&
          state.selectedSearchType === SearchType.byWorkbasket
        ) {
          this.notificationService.showInformation('EMPTY_WORKBASKET');
        }
      })
    );
  }

  @Action(SelectWorkbasket)
  selectWorkbasket(ctx: StateContext<TaskWorkflowStateModel>, action: SelectWorkbasket): Observable<any> {
    ctx.patchState({ selectedWorkbasket: action.workbasket });
    if (ctx.getState().selectedSearchType === SearchType.byWorkbasket) {
      ctx.dispatch(new LoadTasks());
    }
    return of(null);
  }

  @Action(SetSearchType)
  setSearchType(ctx: StateContext<TaskWorkflowStateModel>, action: SetSearchType): Observable<any> {
    ctx.patchState({ selectedSearchType: action.searchType, tasks: [] });
    return of(null);
  }

  @Action(SetSort)
  setSort(ctx: StateContext<TaskWorkflowStateModel>, action: SetSort): Observable<any> {
    ctx.patchState({ sort: action.sort });
    ctx.dispatch(new LoadTasks());
    return of(null);
  }

  @Action(SetPage)
  setPage(ctx: StateContext<TaskWorkflowStateModel>, action: SetPage): Observable<any> {
    ctx.patchState({ paging: { ...ctx.getState().paging, page: action.page } });
    ctx.dispatch(new LoadTasks());
    return of(null);
  }

  @Action(SetPageSize)
  setPageSize(ctx: StateContext<TaskWorkflowStateModel>, action: SetPageSize): Observable<any> {
    ctx.patchState({ paging: { ...ctx.getState().paging, 'page-size': action.pageSize } });
    ctx.dispatch(new LoadTasks());
    return of(null);
  }

  @Action(SelectTask)
  selectTask(ctx: StateContext<TaskWorkflowStateModel>, action: SelectTask): Observable<any> {
    ctx.patchState({ selectedTask: action.task });
    return of(null);
  }

  @Action(GetTask)
  getTask(ctx: StateContext<TaskWorkflowStateModel>, action: GetTask): Observable<any> {
    if (action.taskId === 'new-task') {
      this.requestInProgressService.setRequestInProgress(true);
      const newTask = new Task('', new ObjectReference(), ctx.getState().selectedWorkbasket);
      ctx.patchState({ selectedTask: newTask });
      this.requestInProgressService.setRequestInProgress(false);
      return of(null);
    }
    return this.withRequestInProgress(this.taskService.getTask(action.taskId)).pipe(
      tap((task) => {
        ctx.patchState({ selectedTask: task });
      })
    );
  }

  @Action(CreateTask)
  createTask(ctx: StateContext<TaskWorkflowStateModel>, action: CreateTask): Observable<any> {
    return this.withRequestInProgress(this.taskService.createTask(action.task)).pipe(
      tap((task) => {
        ctx.patchState({ selectedTask: task });
        this.notificationService.showSuccess('TASK_CREATE', { taskName: task.name });
        ctx.dispatch(new LoadTasks());
      })
    );
  }

  @Action(UpdateTask)
  updateTask(ctx: StateContext<TaskWorkflowStateModel>, action: UpdateTask): Observable<any> {
    return this.withRequestInProgress(this.taskService.updateTask(action.task)).pipe(
      tap((task) => {
        ctx.patchState({ selectedTask: task });
        this.notificationService.showSuccess('TASK_UPDATE', { taskName: task.name });
        ctx.dispatch(new LoadTasks());
      })
    );
  }

  @Action(DeleteTask)
  deleteTask(ctx: StateContext<TaskWorkflowStateModel>, action: DeleteTask): Observable<any> {
    return this.withRequestInProgress(this.taskService.deleteTask(action.task)).pipe(
      tap(() => {
        this.notificationService.showSuccess('TASK_DELETE', { taskName: action.task.name });
        ctx.patchState({ selectedTask: undefined });
        ctx.dispatch(new LoadTasks());
      })
    );
  }
}

export interface TaskWorkflowStateModel {
  tasks: Task[];
  page: Page;
  selectedTask: Task | undefined;
  selectedWorkbasket: Workbasket | undefined;
  selectedSearchType: SearchType;
  sort: Sorting<TaskQuerySortParameter>;
  paging: QueryPagingParameter;
}
