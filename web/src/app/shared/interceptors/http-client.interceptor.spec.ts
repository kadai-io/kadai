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
import {
  HttpClient,
  HttpErrorResponse,
  HttpXsrfTokenExtractor,
  provideHttpClient,
  withInterceptors
} from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { httpClientInterceptor, tokenInterceptor } from './http-client.interceptor';
import { NotificationService } from '../services/notifications/notification.service';
import { RequestInProgressService } from '../services/request-in-progress/request-in-progress.service';

describe('httpClientInterceptor', () => {
  const url = 'http://host/kadai/api/v1/tasks';

  let http: HttpClient;
  let httpMock: HttpTestingController;
  let xsrfToken: string | null;
  const notificationService = { showError: vi.fn() };
  const requestInProgressService = { setRequestInProgress: vi.fn() };

  beforeEach(() => {
    xsrfToken = null;
    notificationService.showError.mockClear();
    requestInProgressService.setRequestInProgress.mockClear();

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([tokenInterceptor, httpClientInterceptor])),
        provideHttpClientTesting(),
        { provide: HttpXsrfTokenExtractor, useValue: { getToken: () => xsrfToken } },
        { provide: NotificationService, useValue: notificationService },
        { provide: RequestInProgressService, useValue: requestInProgressService }
      ]
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  const failWith = (body: string | object | null, status: number, requestUrl = url): HttpErrorResponse => {
    let received: HttpErrorResponse | undefined;
    http.get(requestUrl).subscribe({ error: (error) => (received = error) });
    httpMock.expectOne(requestUrl).flush(body, { status, statusText: 'Error' });
    return received!;
  };

  describe('request headers', () => {
    it('should add the hal+json content type and the development authorization', () => {
      http.get(url).subscribe();

      const req = httpMock.expectOne(url);
      expect(req.request.headers.get('Content-Type')).toBe('application/hal+json');
      expect(req.request.headers.get('Authorization')).toBe('Basic YWRtaW46YWRtaW4=');
      req.flush({});
    });

    it('should not add the hal+json content type to multipart requests', () => {
      http.post(url, new FormData(), { headers: { 'Content-Type': 'multipart/form-data' } }).subscribe();

      const req = httpMock.expectOne(url);
      expect(req.request.headers.get('Content-Type')).toBeNull();
      req.flush({});
    });

    it('should add the XSRF token when one is available', () => {
      xsrfToken = 'token-123';
      http.get(url).subscribe();

      const req = httpMock.expectOne(url);
      expect(req.request.headers.get('X-XSRF-TOKEN')).toBe('token-123');
      req.flush({});
    });

    it('should not add an XSRF header without a token', () => {
      http.get(url).subscribe();

      const req = httpMock.expectOne(url);
      expect(req.request.headers.has('X-XSRF-TOKEN')).toBe(false);
      req.flush({});
    });
  });

  describe('error handling', () => {
    it('should show the KADAI error key with its message variables', () => {
      failWith({ error: { key: 'TASK_INVALID_STATE', messageVariables: { taskId: 'TKI:1' } } }, 409);

      expect(notificationService.showError).toHaveBeenCalledWith('TASK_INVALID_STATE', { taskId: 'TKI:1' });
    });

    it('should default missing message variables of a KADAI error', () => {
      failWith({ error: { key: 'TASK_INVALID_STATE' } }, 409);

      expect(notificationService.showError).toHaveBeenCalledWith('TASK_INVALID_STATE', {});
    });

    it('should keep the original error and show a fallback notification for an empty 401 body', () => {
      const error = failWith(null, 401);

      expect(error).toBeInstanceOf(HttpErrorResponse);
      expect(error.status).toBe(401);
      expect(notificationService.showError).toHaveBeenCalledWith('FALLBACK', {});
    });

    it.each([
      ['plain text', 'Internal Server Error'],
      ['an object without a KADAI error', { message: 'boom' }],
      ['a KADAI error without a key', { error: { messageVariables: {} } }]
    ])('should show a fallback notification for %s', (_, body) => {
      const error = failWith(body, 500);

      expect(error.status).toBe(500);
      expect(notificationService.showError).toHaveBeenCalledWith('FALLBACK', {});
    });

    it('should not notify about 404 responses', () => {
      failWith(null, 404);

      expect(notificationService.showError).not.toHaveBeenCalled();
    });

    it('should not notify about a failing runtime configuration file', () => {
      failWith(null, 500, 'environments/data-sources/environment-information.json');

      expect(notificationService.showError).not.toHaveBeenCalled();
    });

    it('should reset the loading state on errors', () => {
      failWith(null, 500);

      expect(requestInProgressService.setRequestInProgress).toHaveBeenCalledWith(false);
    });
  });
});
