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
import { businessAdminGuard, historyProviderGuard, monitorGuard, userGuard } from './shared/guards/app.guards';

export const appRoutes: Routes = [
  {
    path: 'kadai',
    children: [
      {
        canActivate: [businessAdminGuard],
        path: 'administration',
        loadChildren: () =>
          import('./administration/administration.routes').then((administration) => administration.routes)
      },
      {
        canActivate: [monitorGuard],
        path: 'monitor',
        loadChildren: () => import('./monitor/monitor.routes').then((monitor) => monitor.routes)
      },
      {
        canActivate: [userGuard],
        path: 'workplace',
        loadChildren: () => import('./workplace/workplace.routes').then((workplace) => workplace.routes)
      },
      {
        canActivate: [historyProviderGuard],
        path: 'history',
        loadChildren: () => import('./history/history.routes').then((history) => history.routes)
      },
      {
        path: 'no-role',
        loadComponent: () =>
          import('./shared/components/no-access/no-access.component').then((m) => m.NoAccessComponent)
      },
      {
        path: 'administration',
        redirectTo: 'administration/workbaskets'
      },
      {
        canActivate: [businessAdminGuard],
        path: 'settings',
        loadChildren: () => import('./settings/settings.routes').then((settings) => settings.routes)
      },
      {
        canActivate: [businessAdminGuard],
        path: '**',
        redirectTo: 'administration/workbaskets'
      }
    ]
  },
  {
    path: 'no-role',
    loadComponent: () => import('./shared/components/no-access/no-access.component').then((m) => m.NoAccessComponent)
  },
  {
    canActivate: [businessAdminGuard],
    path: '**',
    redirectTo: 'kadai/administration/workbaskets'
  }
];
