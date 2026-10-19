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
import {
  parseRuntimeConfig,
  RUNTIME_CONFIG_DEFAULTS,
  RUNTIME_CONFIG_URL,
  RuntimeConfig,
  RuntimeConfigService
} from './runtime-config.service';

describe('RuntimeConfigService', () => {
  const defaults: RuntimeConfig = {
    kadaiRestUrl: '/kadai/api',
    kadaiLogoutUrl: '/kadai/logout',
    kadaiLoginUrl: '/kadai/login'
  };

  let service: RuntimeConfigService;
  let httpMock: HttpTestingController;
  let consoleSpies: ReturnType<typeof vi.spyOn>[];

  beforeEach(() => {
    consoleSpies = [
      vi.spyOn(console, 'info').mockImplementation(() => {}),
      vi.spyOn(console, 'error').mockImplementation(() => {})
    ];

    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting(), { provide: RUNTIME_CONFIG_DEFAULTS, useValue: defaults }]
    });
    service = TestBed.inject(RuntimeConfigService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    consoleSpies.forEach((spy) => spy.mockRestore());
  });

  const loadWith = async (respond: (req: ReturnType<HttpTestingController['expectOne']>) => void) => {
    const promise = service.load();
    respond(httpMock.expectOne(RUNTIME_CONFIG_URL));
    await promise;
  };

  it('should provide the build-time defaults before loading', () => {
    expect(service.config()).toEqual(defaults);
    expect(service.kadaiRestUrl).toBe('/kadai/api');
    expect(service.kadaiLogoutUrl).toBe('/kadai/logout');
    expect(service.kadaiLoginUrl).toBe('/kadai/login');
  });

  it('should override the defaults with the runtime file', async () => {
    const runtime: RuntimeConfig = {
      kadaiRestUrl: 'http://host/kadai/api',
      kadaiLogoutUrl: 'http://host/kadai/logout',
      kadaiLoginUrl: 'http://host/kadai/login'
    };

    await loadWith((req) => req.flush(runtime));

    expect(service.config()).toEqual(runtime);
  });

  it('should keep the defaults for properties missing or empty in the runtime file', async () => {
    await loadWith((req) => req.flush({ kadaiRestUrl: 'http://host/kadai/api', kadaiLogoutUrl: '' }));

    expect(service.config()).toEqual({ ...defaults, kadaiRestUrl: 'http://host/kadai/api' });
  });

  it('should allow the runtime file to disable the login redirect with an empty kadaiLoginUrl', async () => {
    await loadWith((req) => req.flush({ kadaiLoginUrl: '' }));

    expect(service.kadaiLoginUrl).toBe('');
    expect(service.config()).toEqual({ ...defaults, kadaiLoginUrl: '' });
  });

  it('should not mutate the defaults', async () => {
    await loadWith((req) => req.flush({ kadaiRestUrl: 'http://host/kadai/api' }));

    expect(defaults.kadaiRestUrl).toBe('/kadai/api');
  });

  it('should use the defaults when the runtime file is missing', async () => {
    await loadWith((req) => req.flush('Not Found', { status: 404, statusText: 'Not Found' }));

    expect(service.config()).toEqual(defaults);
    expect(console.info).toHaveBeenCalledWith('Runtime configuration file not found, using build-time defaults.');
    expect(console.error).not.toHaveBeenCalled();
  });

  it('should use the defaults when the runtime file is empty', async () => {
    await loadWith((req) => req.flush(null));

    expect(service.config()).toEqual(defaults);
    expect(console.error).not.toHaveBeenCalled();
  });

  it('should use the defaults and log an error when the runtime file is unreachable', async () => {
    await loadWith((req) => req.error(new ProgressEvent('error'), { status: 0, statusText: 'Unknown Error' }));

    expect(service.config()).toEqual(defaults);
    expect(console.error).toHaveBeenCalledWith(
      'Runtime configuration file is unreachable, using build-time defaults:',
      expect.anything()
    );
  });

  it('should use the defaults and log an error when the server responds with an error', async () => {
    await loadWith((req) => req.flush('Error', { status: 500, statusText: 'Server Error' }));

    expect(service.config()).toEqual(defaults);
    expect(console.error).toHaveBeenCalledWith(
      'Failed to load runtime configuration, using build-time defaults:',
      expect.anything()
    );
  });

  it('should use the defaults and log an error when the runtime file is not valid JSON', async () => {
    await loadWith((req) => req.error(new ProgressEvent('error'), { status: 200, statusText: 'OK' }));

    expect(service.config()).toEqual(defaults);
    expect(console.error).toHaveBeenCalledWith(
      'Runtime configuration file is not valid JSON, using build-time defaults:',
      expect.anything()
    );
  });

  describe('parseRuntimeConfig', () => {
    it.each([['a string'], [42], [['kadaiRestUrl']]])('should reject a non-object value %j', (raw) => {
      expect(parseRuntimeConfig(raw)).toEqual({});
      expect(console.error).toHaveBeenCalledWith(
        'Runtime configuration is not a JSON object, using build-time defaults:',
        raw
      );
    });

    it('should drop properties with a wrong type and keep the valid ones', () => {
      expect(parseRuntimeConfig({ kadaiRestUrl: 42, kadaiLogoutUrl: 'http://host/kadai/logout' })).toEqual({
        kadaiLogoutUrl: 'http://host/kadai/logout'
      });
      expect(console.error).toHaveBeenCalledWith(
        'Runtime configuration property "kadaiRestUrl" is not a string, using build-time default:',
        42
      );
    });

    it('should ignore unknown properties', () => {
      expect(parseRuntimeConfig({ kadaiRestUrl: 'http://host/kadai/api', unknown: 'value' })).toEqual({
        kadaiRestUrl: 'http://host/kadai/api'
      });
    });
  });
});
