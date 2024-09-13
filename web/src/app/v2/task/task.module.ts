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

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { TaskRoutingModule } from './task-routing.module';
import { TaskListComponent } from './components/task-list/task-list.component';
import { SharedModule } from '@shared/shared.module';
import { TaskOverviewComponent } from './components/task-overview/task-overview.component';
import { TaskDetailsComponent } from './components/task-details/task-details.component';
import { TaskContainerComponent } from './components/task-container/task-container.component';
import { NgxsModule } from '@ngxs/store';
import { TaskState } from './store/task.state';
import { TaskDetailsContainerComponent } from './components/task-details-container/task-details-container.component';

@NgModule({
  declarations: [
    TaskListComponent,
    TaskOverviewComponent,
    TaskDetailsComponent,
    TaskContainerComponent,
    TaskDetailsContainerComponent
  ],
  imports: [CommonModule, TaskRoutingModule, SharedModule, NgxsModule.forFeature([TaskState])]
})
export class TaskModule {}
