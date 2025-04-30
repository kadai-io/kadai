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

import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'tasks',
    loadComponent: () =>
      import('../shared/components/master-and-detail/master-and-detail.component').then(
        (m) => m.MasterAndDetailComponent
      ),
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./components/task-master/task-master.component').then((m) => m.TaskMasterComponent),
        outlet: 'master'
      },
      {
        path: 'taskdetail/:id',
        loadComponent: () =>
          import('./components/task-details/task-details.component').then((m) => m.TaskDetailsComponent),
        outlet: 'detail'
      },
      {
        path: 'task/:id',
        loadComponent: () =>
          import('./components/task-processing/task-processing.component').then((m) => m.TaskProcessingComponent),
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
