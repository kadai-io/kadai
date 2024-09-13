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

import { BusinessAdminGuard } from './shared/guards/business-admin.guard';
import { MonitorGuard } from './shared/guards/monitor.guard';
import { UserGuard } from './shared/guards/user.guard';
import { HistoryGuard } from './shared/guards/history.guard';
import { NoAccessComponent } from './shared/components/no-access/no-access.component';

const appRoutes: Routes = [
  {
    path: 'kadai',
    children: [
      {
        canActivate: [BusinessAdminGuard],
        path: 'administration',
        loadChildren: () => import('./administration/administration.module').then((m) => m.AdministrationModule)
      },
      {
        canActivate: [MonitorGuard],
        path: 'monitor',
        loadChildren: () => import('./monitor/monitor.module').then((m) => m.MonitorModule)
      },
      {
        canActivate: [UserGuard],
        path: 'workplace',
        loadChildren: () => import('./workplace/workplace.module').then((m) => m.WorkplaceModule)
      },
      {
        canActivate: [HistoryGuard],
        path: 'history',
        loadChildren: () => import('./history/history.module').then((m) => m.HistoryModule)
      },
      {
        path: 'no-role',
        component: NoAccessComponent
      },
      {
        path: 'administration',
        redirectTo: 'administration/workbaskets'
      },
      {
        canActivate: [BusinessAdminGuard],
        path: 'settings',
        loadChildren: () => import('./settings/settings.module').then((m) => m.SettingsModule)
      },
      {
        canActivate: [BusinessAdminGuard],
        path: '**',
        redirectTo: 'administration/workbaskets'
      }
    ]
  },
  {
    path: 'no-role',
    component: NoAccessComponent
  },
  {
    canActivate: [BusinessAdminGuard],
    path: '**',
    redirectTo: 'kadai/administration/workbaskets'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(appRoutes, { useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule {}
