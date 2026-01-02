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

import { getTestBed, inject, TestBed } from '@angular/core/testing';

import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { StartupService } from './startup.service';
import { environment } from '../../../../environments/environment';

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
    // UserService provided to the TestBed
    service = injector.inject(StartupService);
  });

  it('should be created', inject([StartupService], () => {
    expect(service).toBeTruthy();
  }));

  it('should initialize rest and logout url from external file', (done) => {
    environment.kadaiRestUrl = '';
    environment.kadaiLogoutUrl = '';
    service.getEnvironmentFilePromise().then((res) => {
      expect(environment.kadaiRestUrl).toBe(someRestUrl);
      expect(environment.kadaiLogoutUrl).toBe(someLogoutUrl);
      done();
    });
    const req = httpMock.expectOne(environmentFile);
    expect(req.request.method).toBe('GET');
    req.flush(dummyEnvironmentInformation);
    httpMock.verify();
  });

  it('should initialize rest and logout url from external file and override previous config', (done) => {
    environment.kadaiRestUrl = 'oldRestUrl';
    environment.kadaiLogoutUrl = 'oldLogoutUrl';
    service.getEnvironmentFilePromise().then(() => {
      expect(environment.kadaiRestUrl).toBe(someRestUrl);
      expect(environment.kadaiLogoutUrl).toBe(someLogoutUrl);
      done();
    });
    const req = httpMock.expectOne(environmentFile);
    expect(req.request.method).toBe('GET');
    req.flush(dummyEnvironmentInformation);
    httpMock.verify();
  });
});
