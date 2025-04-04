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
import { Navigate } from '@ngxs/router-plugin';
import { Store } from '@ngxs/store';
import { Task } from '@task/models/task';
import { GetTask, GetTasks } from '@task/store/task.actions';
import { TaskSelector } from '@task/store/task.selector';

@Injectable({
  providedIn: 'root'
})
export class TaskFacadeService {
  private store = inject(Store);

  selectedTask(): Task | null {
    return this.store.selectSnapshot(TaskSelector.selectedTask);
  }

  getTasks(): void {
    this.store.dispatch(new GetTasks());
  }

  // distinguish between select a task (what happens to UI?) and the action of getting information of a task from a taskID
  selectTask(taskId: string): void {
    this.store.dispatch(new Navigate([`/kadai/workplace/tasks/taskdetail/${taskId}`]));
  }

  getTask(taskId: string): void {
    this.store.dispatch(new GetTask(taskId));
  }

  /**
   * @TODO Discuss potential method "deselectTask" if feature is present
   */
}
