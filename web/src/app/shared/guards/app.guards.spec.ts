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

import { TestBed } from '@angular/core/testing';
import { CanActivateFn, provideRouter, Router, UrlTree } from '@angular/router';
import { firstValueFrom, Observable, of, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { businessAdminGuard, historyProviderGuard, monitorGuard, userGuard } from './app.guards';
import { KadaiEngineService } from '../services/kadai-engine/kadai-engine.service';
import { KadaiEngineServiceMock } from '../services/kadai-engine/kadai-engine.mock.service';
import { UserInfo } from '../models/user-info';
import { BusinessAdminRoles } from '../roles/business-admin.roles';
import { MonitorRoles } from '../roles/monitor.roles';
import { UserRoles } from '../roles/user.roles';

describe('app guards', () => {
  let session: KadaiEngineServiceMock;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: KadaiEngineService, useClass: KadaiEngineServiceMock }]
    });
    session = TestBed.inject(KadaiEngineService) as unknown as KadaiEngineServiceMock;
  });

  const run = <T>(guard: CanActivateFn) =>
    TestBed.runInInjectionContext(() => guard(null as never, null as never)) as T;

  const serialize = (result: boolean | UrlTree) =>
    result instanceof UrlTree ? TestBed.inject(Router).serializeUrl(result) : result;

  describe('role guards', () => {
    it.each([
      ['businessAdminGuard', businessAdminGuard, Object.values(BusinessAdminRoles)[0], '/kadai/workplace'],
      ['monitorGuard', monitorGuard, Object.values(MonitorRoles)[0], '/kadai/workplace'],
      ['userGuard', userGuard, Object.values(UserRoles)[0], '/kadai/no-role']
    ])('%s should allow a user with role %s and redirect others to %s', (_, guard, role, fallbackUrl) => {
      session.setCurrentUserInfo(new UserInfo('user', [], [role]));
      expect(run<boolean | UrlTree>(guard)).toBe(true);

      session.setCurrentUserInfo(new UserInfo('user', [], ['UNKNOWN_ROLE']));
      expect(serialize(run<boolean | UrlTree>(guard))).toBe(fallbackUrl);
    });

    it('should deny access without a user session', () => {
      session.setCurrentUserInfo(undefined);

      expect(serialize(run<boolean | UrlTree>(userGuard))).toBe('/kadai/no-role');
      expect(serialize(run<boolean | UrlTree>(businessAdminGuard))).toBe('/kadai/workplace');
    });
  });

  describe('historyProviderGuard', () => {
    const runHistoryGuard = (enabled: Observable<boolean>) => {
      vi.spyOn(session, 'isHistoryProviderEnabled').mockReturnValue(enabled);
      return firstValueFrom(run<Observable<boolean | UrlTree>>(historyProviderGuard)).then(serialize);
    };

    it('should allow access when the history provider is enabled', async () => {
      await expect(runHistoryGuard(of(true))).resolves.toBe(true);
    });

    it('should redirect to the workplace when the history provider is disabled', async () => {
      await expect(runHistoryGuard(of(false))).resolves.toBe('/kadai/workplace');
    });

    it('should redirect to the workplace when the check fails', async () => {
      await expect(runHistoryGuard(throwError(() => new Error('failed')))).resolves.toBe('/kadai/workplace');
    });
  });
});
