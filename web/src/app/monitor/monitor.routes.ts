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

import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./components/monitor/monitor.component').then((m) => m.MonitorComponent),
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'tasks-priority'
      },
      {
        path: 'tasks-priority',
        loadComponent: () =>
          import('./components/task-priority-report/task-priority-report.component').then(
            (m) => m.TaskPriorityReportComponent
          )
      },
      {
        path: 'tasks-priority/:workbasketKey',
        loadComponent: () =>
          import('./components/task-priority-report/task-priority-report.component').then(
            (m) => m.TaskPriorityReportComponent
          )
      },
      {
        path: 'tasks-status',
        loadComponent: () => import('./components/task-report/task-report.component').then((m) => m.TaskReportComponent)
      },
      {
        path: 'workbaskets',
        loadComponent: () =>
          import('./components/workbasket-report/workbasket-report.component').then((m) => m.WorkbasketReportComponent)
      },
      {
        path: 'classifications',
        loadComponent: () =>
          import('./components/classification-report/classification-report.component').then(
            (m) => m.ClassificationReportComponent
          )
      },
      {
        path: 'timestamp',
        loadComponent: () =>
          import('./components/timestamp-report/timestamp-report.component').then((m) => m.TimestampReportComponent)
      }
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
];
