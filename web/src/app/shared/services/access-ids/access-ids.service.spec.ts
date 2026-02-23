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
import { AccessIdsService } from './access-ids.service';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { beforeEach, describe, expect, it, afterEach } from 'vitest';
import { StartupService } from '../startup/startup.service';
import { firstValueFrom } from 'rxjs';

const REST_URL = 'http://localhost:8080/kadai';

describe('AccessIdsService', () => {
  let service: AccessIdsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AccessIdsService,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: StartupService, useValue: { getKadaiRestUrl: () => REST_URL } }
      ]
    });
    service = TestBed.inject(AccessIdsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('url getter', () => {
    it('should return correct URL', () => {
      expect(service.url).toBe(`${REST_URL}/v1/access-ids`);
    });
  });

  describe('searchForAccessId', () => {
    it('should return empty array when accessId is null', async () => {
      const result = await firstValueFrom(service.searchForAccessId(null));
      expect(result).toEqual([]);
    });

    it('should return empty array when accessId is empty string', async () => {
      const result = await firstValueFrom(service.searchForAccessId(''));
      expect(result).toEqual([]);
    });

    it('should return empty array when accessId length < 3', async () => {
      const result = await firstValueFrom(service.searchForAccessId('ab'));
      expect(result).toEqual([]);
    });

    it('should call GET with search param when accessId >= 3 chars', () => {
      const mockResult = [{ accessId: 'user1', name: 'User 1' }];
      service.searchForAccessId('user').subscribe((result) => {
        expect(result).toEqual(mockResult);
      });
      const req = httpMock.expectOne(`${REST_URL}/v1/access-ids?search-for=user`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResult);
    });
  });

  describe('getGroupsByAccessId', () => {
    it('should return empty array when accessId is null', async () => {
      const result = await firstValueFrom(service.getGroupsByAccessId(null));
      expect(result).toEqual([]);
    });

    it('should return empty array when accessId length < 3', async () => {
      const result = await firstValueFrom(service.getGroupsByAccessId('ab'));
      expect(result).toEqual([]);
    });

    it('should call GET groups endpoint when accessId >= 3 chars', () => {
      const mockResult = [{ accessId: 'group1', name: 'Group 1' }];
      service.getGroupsByAccessId('user').subscribe((result) => {
        expect(result).toEqual(mockResult);
      });
      const req = httpMock.expectOne(`${REST_URL}/v1/access-ids/groups?access-id=user`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResult);
    });
  });

  describe('getPermissionsByAccessId', () => {
    it('should return empty array when accessId is null', async () => {
      const result = await firstValueFrom(service.getPermissionsByAccessId(null));
      expect(result).toEqual([]);
    });

    it('should return empty array when accessId length < 3', async () => {
      const result = await firstValueFrom(service.getPermissionsByAccessId('ab'));
      expect(result).toEqual([]);
    });

    it('should call GET permissions endpoint when accessId >= 3 chars', () => {
      const mockResult = [{ accessId: 'perm1', name: 'Permission 1' }];
      service.getPermissionsByAccessId('user').subscribe((result) => {
        expect(result).toEqual(mockResult);
      });
      const req = httpMock.expectOne(`${REST_URL}/v1/access-ids/permissions?access-id=user`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResult);
    });
  });

  describe('getAccessItems', () => {
    it('should call GET workbasket-access-items with no params', () => {
      const mockResult = { accessItems: [], _links: {} };
      service.getAccessItems().subscribe();
      const req = httpMock.expectOne((r) => r.url.includes('/v1/workbasket-access-items'));
      expect(req.request.method).toBe('GET');
      req.flush(mockResult);
    });
  });

  describe('removeAccessItemsPermissions', () => {
    it('should call DELETE with access-id', () => {
      service.removeAccessItemsPermissions('user1').subscribe();
      const req = httpMock.expectOne((r) => r.url.includes('/v1/workbasket-access-items') && r.method === 'DELETE');
      expect(req.request.method).toBe('DELETE');
      expect(req.request.urlWithParams).toContain('access-id=user1');
      req.flush({});
    });
  });
});
