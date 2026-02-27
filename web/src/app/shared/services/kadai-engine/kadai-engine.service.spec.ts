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
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { KadaiEngineService } from './kadai-engine.service';
import { environment } from 'environments/environment';

describe('KadaiEngineService', () => {
  let service: KadaiEngineService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    environment.kadaiRestUrl = 'http://test';
    environment.kadaiLogoutUrl = 'http://test/logout';

    TestBed.configureTestingModule({
      providers: [KadaiEngineService, provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(KadaiEngineService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getUserInformation', () => {
    it('should make GET request to /v1/current-user-info and set currentUserInfo', async () => {
      const mockUserInfo = { userId: 'admin', roles: ['ADMIN'], groups: [], permissions: [] };

      const promise = service.getUserInformation();
      const req = httpMock.expectOne((r) => r.url.includes('current-user-info'));
      expect(req.request.method).toBe('GET');
      req.flush(mockUserInfo);

      await promise;
      expect(service.currentUserInfo).toEqual(mockUserInfo);
    });
  });

  describe('hasRole', () => {
    it('should return false when currentUserInfo is null', () => {
      service.currentUserInfo = null;
      expect(service.hasRole(['ADMIN'])).toBe(false);
    });

    it('should return false when currentUserInfo is undefined', () => {
      service.currentUserInfo = undefined;
      expect(service.hasRole(['ADMIN'])).toBe(false);
    });

    it('should return false when currentUserInfo has empty roles', () => {
      service.currentUserInfo = { userId: 'user', roles: [], groups: [], permissions: [] } as any;
      expect(service.hasRole(['ADMIN'])).toBe(false);
    });

    it('should return true when currentUserInfo has a matching role', () => {
      service.currentUserInfo = { userId: 'admin', roles: ['ADMIN', 'USER'], groups: [], permissions: [] } as any;
      expect(service.hasRole(['ADMIN'])).toBe(true);
    });

    it('should return false when currentUserInfo has no matching role', () => {
      service.currentUserInfo = { userId: 'user', roles: ['USER'], groups: [], permissions: [] } as any;
      expect(service.hasRole(['ADMIN'])).toBe(false);
    });

    it('should return true when one of the roles matches', () => {
      service.currentUserInfo = {
        userId: 'monitor',
        roles: ['MONITOR'],
        groups: [],
        permissions: []
      } as any;
      expect(service.hasRole(['ADMIN', 'MONITOR'])).toBe(true);
    });
  });

  describe('getVersion', () => {
    it('should make GET request to /v1/version', () => {
      service.getVersion().subscribe();

      const req = httpMock.expectOne((r) => r.url.includes('/v1/version'));
      expect(req.request.method).toBe('GET');
      req.flush({ version: '1.0.0' });
    });
  });

  describe('logout', () => {
    it('should make POST request to the logout URL', () => {
      service.logout().subscribe();

      const req = httpMock.expectOne((r) => r.url.includes('logout'));
      expect(req.request.method).toBe('POST');
      req.flush('');
    });
  });

  describe('isCustomRoutingRulesEnabled', () => {
    it('should make GET request to /v1/routing-rules/routing-rest-enabled', () => {
      service.isCustomRoutingRulesEnabled().subscribe();

      const req = httpMock.expectOne((r) => r.url.includes('routing-rest-enabled'));
      expect(req.request.method).toBe('GET');
      req.flush(true);
    });
  });

  describe('isHistoryProviderEnabled', () => {
    it('should make GET request to /v1/history-provider-enabled', () => {
      service.isHistoryProviderEnabled().subscribe();

      const req = httpMock.expectOne((r) => r.url.includes('history-provider-enabled'));
      expect(req.request.method).toBe('GET');
      req.flush(true);
    });
  });
});
