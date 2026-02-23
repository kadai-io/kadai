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

import { RoutingUploadService } from './routing-upload.service';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { StartupService } from 'app/shared/services/startup/startup.service';

const REST_URL = 'http://localhost:8080/kadai';

describe('RoutingUploadService', () => {
  let service: RoutingUploadService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: StartupService,
          useValue: { getKadaiRestUrl: () => REST_URL }
        }
      ]
    });
    service = TestBed.inject(RoutingUploadService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('url getter', () => {
    it('should return the routing rules URL based on the REST URL', () => {
      expect(service.url).toBe(`${REST_URL}/v1/routing-rules/default`);
    });
  });

  describe('uploadRoutingRules()', () => {
    it('should make a PUT request to the routing rules URL', () => {
      const file = new File(['content'], 'routing.xlsx', { type: 'application/vnd.ms-excel' });
      service.uploadRoutingRules(file).subscribe();

      const req = httpMock.expectOne(`${REST_URL}/v1/routing-rules/default`);
      expect(req.request.method).toBe('PUT');
      req.flush({});
    });

    it('should include the file in the form data', () => {
      const file = new File(['content'], 'routing.xlsx', { type: 'application/vnd.ms-excel' });
      service.uploadRoutingRules(file).subscribe();

      const req = httpMock.expectOne(`${REST_URL}/v1/routing-rules/default`);
      expect(req.request.body).toBeInstanceOf(FormData);
      req.flush({});
    });
  });
});
