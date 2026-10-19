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
import { HttpErrorResponse, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { of, throwError } from 'rxjs';
import { StartupService } from './startup.service';
import { KadaiEngineService } from '../kadai-engine/kadai-engine.service';
import {
  RUNTIME_CONFIG_DEFAULTS,
  RUNTIME_CONFIG_URL,
  RuntimeConfigService
} from '../runtime-config/runtime-config.service';
import { WindowRefService } from '../window/window.service';
import { UserInfo } from '../../models/user-info';
import { httpClientInterceptor, tokenInterceptor } from '../../interceptors/http-client.interceptor';
import { NotificationService } from '../notifications/notification.service';

describe('StartupService', () => {
  const loginUrl = 'http://host/kadai/login';

  let service: StartupService;
  let runtimeConfigServiceMock: { load: ReturnType<typeof vi.fn>; kadaiLoginUrl: string };
  let kadaiEngineServiceMock: { getUserInformation: ReturnType<typeof vi.fn> };
  let windowMock: { nativeWindow: { location: { href: string } } };
  let consoleErrorSpy: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    runtimeConfigServiceMock = { load: vi.fn().mockResolvedValue(undefined), kadaiLoginUrl: loginUrl };
    kadaiEngineServiceMock = { getUserInformation: vi.fn().mockReturnValue(of(new UserInfo('user'))) };
    windowMock = { nativeWindow: { location: { href: '' } } };
    consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

    TestBed.configureTestingModule({
      providers: [
        { provide: RuntimeConfigService, useValue: runtimeConfigServiceMock },
        { provide: KadaiEngineService, useValue: kadaiEngineServiceMock },
        { provide: WindowRefService, useValue: windowMock }
      ]
    });
    service = TestBed.inject(StartupService);
  });

  afterEach(() => {
    consoleErrorSpy.mockRestore();
  });

  const failUserInformation = (error: unknown) =>
    kadaiEngineServiceMock.getUserInformation.mockReturnValue(throwError(() => error));

  const startLoad = () => {
    let settled = false;
    const promise = service.load().then(() => (settled = true));
    return { promise, isSettled: () => settled };
  };

  it('should load the runtime configuration before the current user', async () => {
    const order: string[] = [];
    runtimeConfigServiceMock.load.mockImplementation(async () => {
      order.push('config');
    });
    kadaiEngineServiceMock.getUserInformation.mockImplementation(() => {
      order.push('user');
      return of(new UserInfo('user'));
    });

    await service.load();

    expect(order).toEqual(['config', 'user']);
    expect(windowMock.nativeWindow.location.href).toBe('');
  });

  describe('when loading the current user fails', () => {
    it('should start without a user session on a non-authentication error', async () => {
      const error = new HttpErrorResponse({ status: 500, url: 'http://host/kadai/api/v1/current-user-info' });
      failUserInformation(error);

      await startLoad().promise;

      expect(windowMock.nativeWindow.location.href).toBe('');
      expect(consoleErrorSpy).toHaveBeenCalledWith(
        'Failed to load current user, starting without a user session:',
        error
      );
    });

    it('should start without a user session on a non-HTTP error', async () => {
      failUserInformation(new Error('User info failed'));

      await expect(startLoad().promise).resolves.toBe(true);
      expect(windowMock.nativeWindow.location.href).toBe('');
    });

    it('should redirect to the login page and keep bootstrap pending on 401', async () => {
      failUserInformation(new HttpErrorResponse({ status: 401, url: 'http://host/kadai/api/v1/current-user-info' }));

      const { isSettled } = startLoad();

      await vi.waitFor(() => expect(windowMock.nativeWindow.location.href).toBe(loginUrl));
      expect(isSettled()).toBe(false);
    });

    it('should redirect to the login page when the request was redirected to the login page', async () => {
      failUserInformation(new HttpErrorResponse({ status: 200, url: 'http://host/kadai/login' }));

      const { isSettled } = startLoad();

      await vi.waitFor(() => expect(windowMock.nativeWindow.location.href).toBe(loginUrl));
      expect(isSettled()).toBe(false);
    });

    it('should start without a user session on 401 when no login url is configured', async () => {
      runtimeConfigServiceMock.kadaiLoginUrl = '';
      failUserInformation(new HttpErrorResponse({ status: 401 }));

      await expect(startLoad().promise).resolves.toBe(true);
      expect(windowMock.nativeWindow.location.href).toBe('');
    });
  });
});

describe('StartupService with the real HTTP interceptor chain', () => {
  const restUrl = 'http://host/kadai/api';
  const loginUrl = 'http://host/kadai/login';

  let httpMock: HttpTestingController;
  let windowMock: { nativeWindow: { location: { href: string } } };
  let consoleErrorSpy: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    windowMock = { nativeWindow: { location: { href: '' } } };
    consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([tokenInterceptor, httpClientInterceptor])),
        provideHttpClientTesting(),
        {
          provide: RUNTIME_CONFIG_DEFAULTS,
          useValue: { kadaiRestUrl: restUrl, kadaiLogoutUrl: '', kadaiLoginUrl: loginUrl }
        },
        { provide: NotificationService, useValue: { showError: vi.fn() } },
        { provide: WindowRefService, useValue: windowMock }
      ]
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    consoleErrorSpy.mockRestore();
  });

  const startWithUserResponse = (body: string | object | null, status: number) => {
    let settled = false;
    TestBed.inject(StartupService)
      .load()
      .then(() => (settled = true));
    return {
      respond: async () => {
        httpMock.expectOne(RUNTIME_CONFIG_URL).flush({});
        await vi.waitFor(() =>
          httpMock.expectOne(`${restUrl}/v1/current-user-info`).flush(body, { status, statusText: 'Error' })
        );
      },
      isSettled: () => settled
    };
  };

  it('should redirect to the login page on a 401 without response body', async () => {
    const startup = startWithUserResponse(null, 401);
    await startup.respond();

    await vi.waitFor(() => expect(windowMock.nativeWindow.location.href).toBe(loginUrl));
    expect(startup.isSettled()).toBe(false);
  });

  it('should start without a user session on a 500 without response body', async () => {
    const startup = startWithUserResponse(null, 500);
    await startup.respond();

    await vi.waitFor(() => expect(startup.isSettled()).toBe(true));
    expect(windowMock.nativeWindow.location.href).toBe('');
  });
});
