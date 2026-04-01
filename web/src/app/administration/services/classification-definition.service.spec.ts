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

import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ClassificationDefinitionService } from './classification-definition.service';
import { StartupService } from '../../shared/services/startup/startup.service';
import { BlobGenerator } from '../../shared/util/blob-generator';

const REST_URL = 'http://localhost:8080/kadai';
const BASE_URL = `${REST_URL}/v1/classification-definitions`;

describe('ClassificationDefinitionService', () => {
  let service: ClassificationDefinitionService;
  let httpTesting: HttpTestingController;
  let saveFileSpy: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: StartupService, useValue: { getKadaiRestUrl: () => REST_URL } }
      ]
    });
    service = TestBed.inject(ClassificationDefinitionService);
    httpTesting = TestBed.inject(HttpTestingController);
    saveFileSpy = vi.spyOn(BlobGenerator, 'saveFile').mockImplementation(() => {});
  });

  afterEach(() => {
    httpTesting.verify();
    vi.restoreAllMocks();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
    expect(service.url).toBe(BASE_URL);
  });

  it('exportClassifications("") should make a GET request and save the file', () => {
    const mockData = [{ classificationId: 'CL1' }];
    service.exportClassifications('');
    const reqs = httpTesting.match(BASE_URL);
    expect(reqs.length).toBe(1);
    expect(reqs[0].request.method).toBe('GET');
    reqs[0].flush(mockData);
    expect(saveFileSpy).toHaveBeenCalledOnce();
    const [dataArg, fileNameArg] = saveFileSpy.mock.calls[0];
    expect(dataArg).toEqual(mockData);
    expect(fileNameArg).toMatch(/^Classifications_.*\.json$/);
  });

  it('exportClassifications("DOMAIN_A") should make a GET request with ?domain=DOMAIN_A and save domain-filtered data', () => {
    const mockData = [{ classificationId: 'CL2', domain: 'DOMAIN_A' }];
    service.exportClassifications('DOMAIN_A');
    const reqs = httpTesting.match(`${BASE_URL}?domain=DOMAIN_A`);
    expect(reqs.length).toBe(1);
    expect(reqs[0].request.method).toBe('GET');
    reqs[0].flush(mockData);
    expect(saveFileSpy).toHaveBeenCalledOnce();
    const [dataArg] = saveFileSpy.mock.calls[0];
    expect(dataArg).toEqual(mockData);
  });

  it('exportClassifications should return an Observable that emits the classification data', () => {
    const mockData = [{ classificationId: 'CL3' }];
    let received: any;
    service.exportClassifications('').subscribe((data) => (received = data));
    const reqs = httpTesting.match(BASE_URL);
    expect(reqs.length).toBe(2);
    reqs.forEach((req) => req.flush(mockData));
    expect(received).toEqual(mockData);
  });

  it('importClassification should make a POST request to the correct URL', () => {
    const file = new File(['{}'], 'classifications.json', { type: 'application/json' });
    service.importClassification(file).subscribe();
    const req = httpTesting.expectOne(BASE_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toBeInstanceOf(FormData);
    req.flush({});
  });
});
