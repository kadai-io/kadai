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
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { of } from 'rxjs';
import { ClassificationsService } from './classifications.service';
import { StartupService } from '../startup/startup.service';
import { DomainService } from '../domain/domain.service';
import { Classification } from '../../models/classification';

describe('ClassificationsService', () => {
  let service: ClassificationsService;
  let httpMock: HttpTestingController;

  const mockStartupService = {
    getKadaiRestUrl: vi.fn().mockReturnValue('http://test'),
    getKadaiLogoutUrl: vi.fn().mockReturnValue('')
  };

  const mockDomainService = {
    getSelectedDomain: vi.fn().mockReturnValue(of('DOMAIN_A')),
    domainChangedComplete: vi.fn()
  };

  beforeEach(() => {
    vi.clearAllMocks();
    mockStartupService.getKadaiRestUrl.mockReturnValue('http://test');

    TestBed.configureTestingModule({
      providers: [
        ClassificationsService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: StartupService, useValue: mockStartupService },
        { provide: DomainService, useValue: mockDomainService }
      ]
    });

    service = TestBed.inject(ClassificationsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('url getter', () => {
    it('should return base URL from startupService plus /v1/classifications', () => {
      expect(service.url).toBe('http://test/v1/classifications');
    });
  });

  describe('getClassifications', () => {
    it('should make a GET request to the classifications URL', () => {
      service.getClassifications().subscribe();

      const req = httpMock.expectOne((r) => r.url.includes('/v1/classifications'));
      expect(req.request.method).toBe('GET');
      req.flush({ classifications: [] });
    });

    it('should append filter parameters as query string', () => {
      const filterParam = { key: ['TASK'] };
      service.getClassifications(filterParam as any).subscribe();

      const req = httpMock.expectOne((r) => r.url.includes('/v1/classifications'));
      expect(req.request.urlWithParams).toContain('key=TASK');
      req.flush({ classifications: [] });
    });

    it('should work with no parameters', () => {
      service.getClassifications().subscribe();

      const req = httpMock.expectOne('http://test/v1/classifications');
      expect(req.request.method).toBe('GET');
      req.flush({ classifications: [] });
    });
  });

  describe('getClassification', () => {
    it('should make a GET request to /v1/classifications/{id}', () => {
      service.getClassification('clf-123').subscribe();

      const req = httpMock.expectOne('http://test/v1/classifications/clf-123');
      expect(req.request.method).toBe('GET');
      req.flush({ classificationId: 'clf-123' });
    });
  });

  describe('postClassification', () => {
    it('should make a POST request to /v1/classifications', () => {
      const classification: Classification = {
        classificationId: 'new-clf',
        key: 'KEY1',
        domain: 'DOMAIN_A'
      } as Classification;

      service.postClassification(classification).subscribe();

      const req = httpMock.expectOne('http://test/v1/classifications');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(classification);
      req.flush({ classificationId: 'new-clf' });
    });
  });

  describe('putClassification', () => {
    it('should make a PUT request to /v1/classifications/{classificationId}', () => {
      const classification: Classification = {
        classificationId: 'clf-upd',
        key: 'KEY2',
        domain: 'DOMAIN_A'
      } as Classification;

      service.putClassification(classification).subscribe();

      const req = httpMock.expectOne('http://test/v1/classifications/clf-upd');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(classification);
      req.flush({ classificationId: 'clf-upd' });
    });

    it('should use classificationId from the classification object in URL', () => {
      const classification: Classification = {
        classificationId: 'specific-id',
        key: 'KEY3',
        domain: 'DOMAIN_B'
      } as Classification;

      service.putClassification(classification).subscribe();

      const req = httpMock.expectOne('http://test/v1/classifications/specific-id');
      expect(req.request.url).toContain('specific-id');
      req.flush({});
    });
  });

  describe('deleteClassification', () => {
    it('should make a DELETE request to /v1/classifications/{id}', () => {
      service.deleteClassification('clf-del').subscribe();

      const req = httpMock.expectOne('http://test/v1/classifications/clf-del');
      expect(req.request.method).toBe('DELETE');
      req.flush({});
    });
  });
});
