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
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { KadaiEngineService } from '../services/kadai-engine/kadai-engine.service';
import { BusinessAdminRoles } from '../roles/business-admin.roles';
import { MonitorRoles } from '../roles/monitor.roles';
import { UserRoles } from '../roles/user.roles';

// Grants access if the current user has one of the given roles, otherwise redirects to fallbackUrl.
// Without a user session (loading the current user failed on startup) access is never granted.
export const roleGuard =
  (roles: string[], fallbackUrl: string): CanActivateFn =>
  (): boolean | UrlTree =>
    inject(KadaiEngineService).hasRole(roles) || inject(Router).parseUrl(fallbackUrl);

export const businessAdminGuard = roleGuard(Object.values(BusinessAdminRoles), '/kadai/workplace');
export const monitorGuard = roleGuard(Object.values(MonitorRoles), '/kadai/workplace');
export const userGuard = roleGuard(Object.values(UserRoles), '/kadai/no-role');

export const historyProviderGuard: CanActivateFn = (): Observable<boolean | UrlTree> => {
  const router = inject(Router);
  return inject(KadaiEngineService)
    .isHistoryProviderEnabled()
    .pipe(
      map((enabled) => enabled || router.parseUrl('/kadai/workplace')),
      catchError(() => of(router.parseUrl('/kadai/workplace')))
    );
};
