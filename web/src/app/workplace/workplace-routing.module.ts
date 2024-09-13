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
import { RouterModule, Routes } from '@angular/router';
import { MasterAndDetailComponent } from '../shared/components/master-and-detail/master-and-detail.component';
import { TaskProcessingComponent } from './components/task-processing/task-processing.component';
import { TaskDetailsComponent } from './components/task-details/task-details.component';
import { TaskMasterComponent } from './components/task-master/task-master.component';

const routes: Routes = [
  {
    path: 'tasks',
    component: MasterAndDetailComponent,
    children: [
      {
        path: '',
        component: TaskMasterComponent,
        outlet: 'master'
      },
      {
        path: 'taskdetail/:id',
        component: TaskDetailsComponent,
        outlet: 'detail'
      },
      {
        path: 'task/:id',
        component: TaskProcessingComponent,
        outlet: 'detail'
      }
    ]
  },
  {
    path: '',
    redirectTo: 'tasks',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: 'tasks'
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class WorkplaceRoutingModule {}
