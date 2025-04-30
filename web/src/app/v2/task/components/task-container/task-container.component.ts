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
import { TaskFacadeService } from '@task/services/task-facade.service';
import { RouterOutlet } from '@angular/router';
import { TaskListComponent } from '@task/components/task-list/task-list.component';

@Component({
  selector: 'kadai-task-container',
  templateUrl: './task-container.component.html',
  imports: [RouterOutlet, TaskListComponent],
  styleUrls: ['./task-container.component.scss']
})
export class TaskContainerComponent implements OnInit {
  private taskFacade = inject(TaskFacadeService);

  ngOnInit(): void {
    this.taskFacade.getTasks();
  }
}
