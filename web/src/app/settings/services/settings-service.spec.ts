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

import { describe, expect, it, beforeEach, afterEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { SettingsService } from './settings-service';
import { environment } from '../../../environments/environment';
import { Settings } from '../models/settings';

describe('SettingsService', () => {
  let service: SettingsService;
  let httpTesting: HttpTestingController;

  const baseUrl = `${environment.kadaiRestUrl}/v1/config/custom-attributes`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()]
    });
    service = TestBed.inject(SettingsService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getSettings() should make a GET request to the correct URL', () => {
    service.getSettings().subscribe();
    const req = httpTesting.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush({ customAttributes: {} });
  });

  it('getSettings() should map the response to customAttributes', () => {
    const mockCustomAttributes: Settings = {
      schema: [],
      someKey: 'someValue'
    };

    let result: Settings | undefined;
    service.getSettings().subscribe((settings) => {
      result = settings;
    });

    const req = httpTesting.expectOne(baseUrl);
    req.flush({ customAttributes: mockCustomAttributes });

    expect(result).toEqual(mockCustomAttributes);
  });

  it('updateSettings() should make a PUT request to the correct URL', () => {
    const settings: Settings = { schema: [], theme: 'dark' };
    service.updateSettings(settings).subscribe();

    const req = httpTesting.expectOne(baseUrl);
    expect(req.request.method).toBe('PUT');
    req.flush(settings);
  });

  it('updateSettings() should send the settings wrapped in customAttributes body', () => {
    const settings: Settings = { schema: [], language: 'de' };
    service.updateSettings(settings).subscribe();

    const req = httpTesting.expectOne(baseUrl);
    expect(req.request.body).toEqual({ customAttributes: settings });
    req.flush(settings);
  });
});
