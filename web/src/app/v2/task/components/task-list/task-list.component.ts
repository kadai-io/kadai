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

import { Component, inject, OnInit } from '@angular/core';
import { Select } from '@ngxs/store';
import { TaskSummary } from '@task/models/task';
import { TaskFacadeService } from '@task/services/task-facade.service';
import { TaskSelector } from '@task/store/task.selector';
import { Observable } from 'rxjs';
import { AsyncPipe } from '@angular/common';

@Component({
  selector: 'kadai-task-list',
  templateUrl: './task-list.component.html',
  imports: [AsyncPipe],
  styleUrls: ['./task-list.component.scss']
})
export class TaskListComponent implements OnInit {
  @Select(TaskSelector.tasks)
  tasks$: Observable<TaskSummary[]>;
  @Select(TaskSelector.selectedTask) selectedTask$: Observable<Task | null>;
  private taskFacade = inject(TaskFacadeService);

  ngOnInit(): void {}

  selectTask(taskId: string): void {
    if (this.isTaskSelected(taskId)) {
      /**
       * @TODO Add deselectTask to facade
       */
    } else {
      this.taskFacade.selectTask(taskId);
    }
  }

  isTaskSelected(taskId: string): boolean {
    return this.taskFacade.selectedTask()?.taskId === taskId;
  }
}
