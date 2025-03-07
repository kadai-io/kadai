/*
 * Copyright [2024] [envite consulting GmbH]
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

import { inject, Injectable } from '@angular/core';
import { Action, State, StateContext } from '@ngxs/store';
import { PagedTaskSummary } from '@task/models/paged-task';
import { TaskService } from '@task/services/task.service';
import { take, tap } from 'rxjs';
import { GetTask, GetTasks } from './task.actions';
import { Task } from '@task/models/task';

export interface TaskStateModel {
  pagedTask: PagedTaskSummary | null;
  selectedTask: Task | null;
}

const defaults: TaskStateModel = {
  pagedTask: { tasks: [], page: {} },
  selectedTask: null
};

@State<TaskStateModel>({
  name: 'task',
  defaults
})
@Injectable({
  providedIn: 'root'
})
export class TaskState {
  private taskService = inject(TaskService);

  @Action(GetTasks)
  getTasks(ctx: StateContext<TaskStateModel>) {
    return this.taskService.getTasks().pipe(
      take(1),
      tap((pagedTask) => {
        ctx.patchState({ pagedTask });
      })
    );
  }

  @Action(GetTask)
  getTask(ctx: StateContext<TaskStateModel>, { taskId }: GetTask) {
    return this.taskService.getTask(taskId).pipe(
      take(1),
      tap((selectedTask) => {
        ctx.patchState({ selectedTask });
      })
    );
  }
}
