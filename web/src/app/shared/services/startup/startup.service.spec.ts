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
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { provideRouter } from '@angular/router';
import { KadaiEngineService } from '../kadai-engine/kadai-engine.service';

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
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
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

  describe('getKadaiRestUrl', () => {
    it('should return the environment kadaiRestUrl', () => {
      environment.kadaiRestUrl = 'http://expected-rest-url';
      expect(service.getKadaiRestUrl()).toBe('http://expected-rest-url');
    });
  });

  describe('getKadaiLogoutUrl', () => {
    it('should return the environment kadaiLogoutUrl', () => {
      environment.kadaiLogoutUrl = 'http://expected-logout-url';
      expect(service.getKadaiLogoutUrl()).toBe('http://expected-logout-url');
    });
  });

  describe('router getter', () => {
    it('should return the injected Router instance', () => {
      const router = service.router;
      expect(router).toBeDefined();
      expect(router).toBeTruthy();
    });

    it('should return the same Router instance on multiple calls', () => {
      const router1 = service.router;
      const router2 = service.router;
      expect(router1).toBe(router2);
    });
  });

  describe('load', () => {
    it('should return a Promise', async () => {
      const kadaiEngineService = TestBed.inject(KadaiEngineService);
      vi.spyOn(kadaiEngineService, 'getUserInformation').mockResolvedValue(undefined);

      const promise = service.load();
      const envReq = httpMock.expectOne(environmentFile);
      envReq.flush(dummyEnvironmentInformation);

      expect(promise).toBeInstanceOf(Promise);
      await promise;
      httpMock.verify();
    });

    it('should call getEnvironmentFilePromise then getUserInformation', async () => {
      const kadaiEngineService = TestBed.inject(KadaiEngineService);
      const getUserInfoSpy = vi.spyOn(kadaiEngineService, 'getUserInformation').mockResolvedValue(undefined);

      const promise = service.load();
      const envReq = httpMock.expectOne(environmentFile);
      envReq.flush(dummyEnvironmentInformation);

      await promise;
      expect(getUserInfoSpy).toHaveBeenCalled();
      httpMock.verify();
    });

    it('should resolve even when environment file request fails', async () => {
      const kadaiEngineService = TestBed.inject(KadaiEngineService);
      vi.spyOn(kadaiEngineService, 'getUserInformation').mockResolvedValue(undefined);

      const promise = service.load();
      const envReq = httpMock.expectOne(environmentFile);
      envReq.flush('not found', { status: 404, statusText: 'Not Found' });

      await expect(promise).resolves.not.toThrow();
      httpMock.verify();
    });
  });
});
