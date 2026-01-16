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

import { getTestBed, TestBed } from '@angular/core/testing';

import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { StartupService } from './startup.service';
import { environment } from '../../../../environments/environment';
import { beforeEach, describe, expect, it } from 'vitest';

describe('StartupService', () => {
  const environmentFile = 'environments/data-sources/environment-information.json';
  const someRestUrl = 'someRestUrl';
  const someLogoutUrl = 'someLogoutUrl';
  const dummyEnvironmentInformation = {
    kadaiRestUrl: someRestUrl,
    kadaiLogoutUrl: someLogoutUrl
  };

  let httpMock: HttpTestingController;
  let service: StartupService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
  });

  beforeEach(() => {
    const injector = getTestBed();
    httpMock = injector.inject(HttpTestingController);
    service = injector.inject(StartupService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should initialize rest and logout url from external file', async () => {
    environment.kadaiRestUrl = '';
    environment.kadaiLogoutUrl = '';

    const promise = service.getEnvironmentFilePromise();

    const req = httpMock.expectOne(environmentFile);
    expect(req.request.method).toBe('GET');
    req.flush(dummyEnvironmentInformation);

    await promise;

    expect(environment.kadaiRestUrl).toBe(someRestUrl);
    expect(environment.kadaiLogoutUrl).toBe(someLogoutUrl);

    httpMock.verify();
  });

  it('should initialize rest and logout url from external file and override previous config', async () => {
    environment.kadaiRestUrl = 'oldRestUrl';
    environment.kadaiLogoutUrl = 'oldLogoutUrl';

    const promise = service.getEnvironmentFilePromise();

    const req = httpMock.expectOne(environmentFile);
    expect(req.request.method).toBe('GET');
    req.flush(dummyEnvironmentInformation);

    await promise;

    expect(environment.kadaiRestUrl).toBe(someRestUrl);
    expect(environment.kadaiLogoutUrl).toBe(someLogoutUrl);

    httpMock.verify();
  });
});
