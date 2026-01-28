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

import { inject } from '@angular/core';
import { Router, Routes, UrlTree } from '@angular/router';

import { UserRoles } from './shared/roles/user.roles';

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
        loadChildren: () => import('./monitor/monitor.routes').then((monitor) => monitor.routes)
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
        loadChildren: () => import('./workplace/workplace.routes').then((workplace) => workplace.routes)
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
