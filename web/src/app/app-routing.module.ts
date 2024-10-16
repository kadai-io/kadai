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

import { inject, NgModule } from '@angular/core';
import { Router, RouterModule, Routes, UrlTree } from '@angular/router';

import { UserRoles } from './shared/roles/user.roles';
import { NoAccessComponent } from './shared/components/no-access/no-access.component';
import { KadaiEngineService } from './shared/services/kadai-engine/kadai-engine.service';
import { MonitorRoles } from './shared/roles/monitor.roles';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { BusinessAdminRoles } from './shared/roles/business-admin.roles';

const businessAdminGuard = (): boolean | UrlTree => {
  const kadaiEngineService = inject(KadaiEngineService);
  const router = inject(Router);

  if (kadaiEngineService.hasRole(Object.values(BusinessAdminRoles))) {
    return true;
  }

  return router.parseUrl('/kadai/workplace');
};

const appRoutes: Routes = [
  {
    path: 'kadai',
    children: [
      {
        canActivate: [businessAdminGuard],
        path: 'administration',
        loadChildren: () => import('./administration/administration.module').then((m) => m.AdministrationModule)
      },
      {
        canActivate: [
          (): boolean | UrlTree => {
            const kadaiEngineService = inject(KadaiEngineService);
            const router = inject(Router);

            if (kadaiEngineService.hasRole(Object.values(MonitorRoles))) {
              return true;
            }

            return router.parseUrl('/kadai/workplace');
          }
        ],
        path: 'monitor',
        loadChildren: () => import('./monitor/monitor.module').then((m) => m.MonitorModule)
      },
      {
        canActivate: [
          (): boolean | UrlTree => {
            const kadaiEngineService = inject(KadaiEngineService);
            const router = inject(Router);

            if (kadaiEngineService.hasRole(Object.values(UserRoles))) {
              return true;
            }

            return router.parseUrl('/kadai/no-role');
          }
        ],
        path: 'workplace',
        loadChildren: () => import('./workplace/workplace.module').then((m) => m.WorkplaceModule)
      },
      {
        canActivate: [
          (): Observable<boolean | UrlTree> => {
            const kadaiEngineService = inject(KadaiEngineService);
            const router = inject(Router);

            return kadaiEngineService.isHistoryProviderEnabled().pipe(
              map((value) => {
                if (value) {
                  return value;
                }
                return router.parseUrl('/kadai/workplace');
              }),
              catchError(() => {
                return of(router.parseUrl('/kadai/workplace'));
              })
            );
          }
        ],
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
        canActivate: [businessAdminGuard],
        path: 'settings',
        loadChildren: () => import('./settings/settings.module').then((m) => m.SettingsModule)
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
    component: NoAccessComponent
  },
  {
    canActivate: [businessAdminGuard],
    path: '**',
    redirectTo: 'kadai/administration/workbaskets'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(appRoutes, { useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule {}
