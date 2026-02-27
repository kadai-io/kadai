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

import { describe, expect, it, beforeEach, afterEach, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { WorkbasketDefinitionService } from './workbasket-definition.service';
import { StartupService } from '../../shared/services/startup/startup.service';
import { BlobGenerator } from '../../shared/util/blob-generator';

const REST_URL = 'http://localhost:8080/kadai';
const BASE_URL = `${REST_URL}/v1/workbasket-definitions`;

describe('WorkbasketDefinitionService', () => {
  let service: WorkbasketDefinitionService;
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
    service = TestBed.inject(WorkbasketDefinitionService);
    httpTesting = TestBed.inject(HttpTestingController);
    saveFileSpy = vi.spyOn(BlobGenerator, 'saveFile').mockImplementation(() => {});
  });

  afterEach(() => {
    httpTesting.verify();
    vi.restoreAllMocks();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('url getter should return the correct URL', () => {
    expect(service.url).toBe(BASE_URL);
  });

  it('exportWorkbaskets("") should make a GET request without query parameters', () => {
    service.exportWorkbaskets('');
    const reqs = httpTesting.match(BASE_URL);
    expect(reqs.length).toBe(1);
    expect(reqs[0].request.method).toBe('GET');
    reqs[0].flush([]);
  });

  it('exportWorkbaskets("") should call BlobGenerator.saveFile with the response data', () => {
    const mockData = [{ workbasketId: 'WB1' }];
    service.exportWorkbaskets('');
    const reqs = httpTesting.match(BASE_URL);
    reqs[0].flush(mockData);
    expect(saveFileSpy).toHaveBeenCalledOnce();
    const [dataArg, fileNameArg] = saveFileSpy.mock.calls[0];
    expect(dataArg).toEqual(mockData);
    expect(fileNameArg).toMatch(/^Workbaskets_.*\.json$/);
  });

  it('exportWorkbaskets("DOMAIN_X") should make a GET request with ?domain=DOMAIN_X', () => {
    service.exportWorkbaskets('DOMAIN_X');
    const reqs = httpTesting.match(`${BASE_URL}?domain=DOMAIN_X`);
    expect(reqs.length).toBe(1);
    expect(reqs[0].request.method).toBe('GET');
    reqs[0].flush([]);
  });

  it('exportWorkbaskets("DOMAIN_X") should call BlobGenerator.saveFile with domain-filtered data', () => {
    const mockData = [{ workbasketId: 'WB2', domain: 'DOMAIN_X' }];
    service.exportWorkbaskets('DOMAIN_X');
    const reqs = httpTesting.match(`${BASE_URL}?domain=DOMAIN_X`);
    reqs[0].flush(mockData);
    expect(saveFileSpy).toHaveBeenCalledOnce();
    const [dataArg] = saveFileSpy.mock.calls[0];
    expect(dataArg).toEqual(mockData);
  });

  it('exportWorkbaskets should return an Observable that emits the workbasket data', () => {
    const mockData = [{ workbasketId: 'WB3' }];
    let received: any;
    service.exportWorkbaskets('').subscribe((data) => (received = data));
    const reqs = httpTesting.match(BASE_URL);
    expect(reqs.length).toBe(2);
    reqs.forEach((req) => req.flush(mockData));
    expect(received).toEqual(mockData);
  });

  it('importWorkbasket should make a POST request to the correct URL', () => {
    const file = new File(['{}'], 'workbaskets.json', { type: 'application/json' });
    service.importWorkbasket(file).subscribe();
    const req = httpTesting.expectOne(BASE_URL);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('importWorkbasket should send a FormData body containing the file', () => {
    const file = new File(['{}'], 'workbaskets.json', { type: 'application/json' });
    service.importWorkbasket(file).subscribe();
    const req = httpTesting.expectOne(BASE_URL);
    expect(req.request.body).toBeInstanceOf(FormData);
    req.flush({});
  });
});
