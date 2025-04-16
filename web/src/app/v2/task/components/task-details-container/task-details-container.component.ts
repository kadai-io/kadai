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

import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TaskFacadeService } from '@task/services/task-facade.service';
import { TaskDetailsComponent } from '@task/components/task-details/task-details.component';

@Component({
  selector: 'kadai-task-details-container',
  templateUrl: './task-details-container.component.html',
  imports: [TaskDetailsComponent],
  styleUrls: ['./task-details-container.component.scss']
})
export class TaskDetailsContainerComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private taskFacade = inject(TaskFacadeService);

  ngOnInit(): void {
    //TODO add takeuntil destroy, can use https://github.com/ngneat/until-destroy
    this.route.params.pipe().subscribe((param) => {
      const taskId = param.id;
      this.taskFacade.getTask(taskId);
    });
  }
}
