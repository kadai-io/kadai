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
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { firstValueFrom } from 'rxjs';
import { KadaiEngineService, LOGOUT_TIMEOUT_MS, USER_INFO_TIMEOUT_MS } from './kadai-engine.service';
import { RUNTIME_CONFIG_DEFAULTS } from '../runtime-config/runtime-config.service';
import { UserInfo } from '../../models/user-info';

describe('KadaiEngineService', () => {
  const restUrl = 'http://test';
  const logoutUrl = 'http://test/logout';

  let service: KadaiEngineService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClientTesting(),
        {
          provide: RUNTIME_CONFIG_DEFAULTS,
          useValue: { kadaiRestUrl: restUrl, kadaiLogoutUrl: logoutUrl, kadaiLoginUrl: '' }
        }
      ]
    });

    service = TestBed.inject(KadaiEngineService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    vi.useRealTimers();
  });

  const loadUser = async (user: UserInfo) => {
    const promise = firstValueFrom(service.getUserInformation());
    httpMock.expectOne(`${restUrl}/v1/current-user-info`).flush(user);
    await promise;
  };

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getUserInformation', () => {
    it('should make GET request to /v1/current-user-info and set currentUserInfo', async () => {
      const user = new UserInfo('admin', [], ['ADMIN']);
      const promise = firstValueFrom(service.getUserInformation());

      const req = httpMock.expectOne(`${restUrl}/v1/current-user-info`);
      expect(req.request.method).toBe('GET');
      req.flush(user);

      await expect(promise).resolves.toEqual(user);
      expect(service.currentUserInfo()).toEqual(user);
    });

    it('should fail and clear the current user when the request times out', async () => {
      await loadUser(new UserInfo('admin', [], ['ADMIN']));
      vi.useFakeTimers();
      const promise = firstValueFrom(service.getUserInformation());
      const req = httpMock.expectOne(`${restUrl}/v1/current-user-info`);

      vi.advanceTimersByTime(USER_INFO_TIMEOUT_MS);

      await expect(promise).rejects.toThrow();
      expect(req.cancelled).toBe(true);
      expect(service.currentUserInfo()).toBeUndefined();
    });

    it('should clear currentUserInfo and rethrow when the request fails', async () => {
      await loadUser(new UserInfo('admin', [], ['ADMIN']));

      const promise = firstValueFrom(service.getUserInformation());
      httpMock
        .expectOne(`${restUrl}/v1/current-user-info`)
        .flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

      await expect(promise).rejects.toMatchObject({ status: 401 });
      expect(service.currentUserInfo()).toBeUndefined();
    });
  });

  describe('hasRole', () => {
    it('should return false when no user is loaded', () => {
      expect(service.hasRole(['ADMIN'])).toBe(false);
    });

    it('should return false when the user has no roles', async () => {
      await loadUser(new UserInfo('user', [], []));
      expect(service.hasRole(['ADMIN'])).toBe(false);
    });

    it('should return true when the user has a matching role', async () => {
      await loadUser(new UserInfo('admin', [], ['ADMIN', 'USER']));
      expect(service.hasRole(['ADMIN'])).toBe(true);
    });

    it('should return false when the user has no matching role', async () => {
      await loadUser(new UserInfo('user', [], ['USER']));
      expect(service.hasRole(['ADMIN'])).toBe(false);
    });

    it('should return true when one of the roles matches', async () => {
      await loadUser(new UserInfo('monitor', [], ['MONITOR']));
      expect(service.hasRole(['ADMIN', 'MONITOR'])).toBe(true);
    });
  });

  describe('getVersion', () => {
    it('should make GET request to /v1/version', () => {
      service.getVersion().subscribe();

      const req = httpMock.expectOne(`${restUrl}/v1/version`);
      expect(req.request.method).toBe('GET');
      req.flush({ version: '1.0.0' });
    });
  });

  describe('logout', () => {
    let consoleWarnSpy: ReturnType<typeof vi.spyOn>;

    beforeEach(async () => {
      consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});
      await loadUser(new UserInfo('admin', [], ['ADMIN']));
    });

    afterEach(() => {
      consoleWarnSpy.mockRestore();
    });

    it('should POST to the logout URL, complete and clear the current user', async () => {
      const promise = firstValueFrom(service.logout());

      const req = httpMock.expectOne(logoutUrl);
      expect(req.request.method).toBe('POST');
      req.flush('');

      await expect(promise).resolves.toBeUndefined();
      expect(service.currentUserInfo()).toBeUndefined();
      expect(consoleWarnSpy).not.toHaveBeenCalled();
    });

    it('should complete without error and clear the current user when the request fails', async () => {
      const promise = firstValueFrom(service.logout());

      httpMock.expectOne(logoutUrl).flush('Error', { status: 500, statusText: 'Server Error' });

      await expect(promise).resolves.toBeUndefined();
      expect(service.currentUserInfo()).toBeUndefined();
      expect(consoleWarnSpy).toHaveBeenCalled();
    });

    it('should complete without error and clear the current user when the request times out', async () => {
      vi.useFakeTimers();
      const promise = firstValueFrom(service.logout());
      const req = httpMock.expectOne(logoutUrl);

      vi.advanceTimersByTime(LOGOUT_TIMEOUT_MS);

      await expect(promise).resolves.toBeUndefined();
      expect(req.cancelled).toBe(true);
      expect(service.currentUserInfo()).toBeUndefined();
    });
  });

  describe('isCustomRoutingRulesEnabled', () => {
    it('should make GET request to /v1/routing-rules/routing-rest-enabled', () => {
      service.isCustomRoutingRulesEnabled().subscribe();

      const req = httpMock.expectOne(`${restUrl}/v1/routing-rules/routing-rest-enabled`);
      expect(req.request.method).toBe('GET');
      req.flush(true);
    });

    it('should return false when the endpoint returns an error', () => {
      let result: boolean | undefined;
      service.isCustomRoutingRulesEnabled().subscribe((value) => (result = value));

      httpMock
        .expectOne(`${restUrl}/v1/routing-rules/routing-rest-enabled`)
        .flush('Not Found', { status: 404, statusText: 'Not Found' });

      expect(result).toBe(false);
    });
  });

  describe('isHistoryProviderEnabled', () => {
    it('should make GET request to /v1/history-provider-enabled', () => {
      service.isHistoryProviderEnabled().subscribe();

      const req = httpMock.expectOne(`${restUrl}/v1/history-provider-enabled`);
      expect(req.request.method).toBe('GET');
      req.flush(true);
    });
  });
});
