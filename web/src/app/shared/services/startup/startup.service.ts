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

import { firstValueFrom } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { DOCUMENT, inject, Injectable } from '@angular/core';
import { KadaiEngineService } from 'app/shared/services/kadai-engine/kadai-engine.service';
import { WindowRefService } from 'app/shared/services/window/window.service';
import { RuntimeConfigService } from '../runtime-config/runtime-config.service';

@Injectable({
  providedIn: 'root'
})
export class StartupService {
  private kadaiEngineService = inject(KadaiEngineService);
  private runtimeConfigService = inject(RuntimeConfigService);
  private window = inject(WindowRefService);
  private document = inject(DOCUMENT);

  async load(): Promise<void> {
    await this.runtimeConfigService.load();
    const userInfoUrl = this.kadaiEngineService.currentUserInfoUrl;
    try {
      await firstValueFrom(this.kadaiEngineService.getUserInformation());
    } catch (error) {
      const loginUrl = this.runtimeConfigService.kadaiLoginUrl;
      if (loginUrl && isAuthenticationError(error, userInfoUrl, this.document.baseURI)) {
        this.window.nativeWindow.location.href = loginUrl;
        return new Promise<never>(() => {});
      }
      console.error('Failed to load current user, starting without a user session:', error);
    }
  }
}

// Unauthenticated requests come back either as 401 or as a login redirect the browser followed, so
// a successful status from another URL means no session; from the endpoint itself it is a server bug.
function isAuthenticationError(error: unknown, requestUrl: string, baseUrl: string): boolean {
  if (!(error instanceof HttpErrorResponse)) {
    return false;
  }
  if (error.status === 401) {
    return true;
  }
  return isSuccessful(error.status) && wasRedirected(error.url, requestUrl, baseUrl);
}

function isSuccessful(status: number): boolean {
  return status >= 200 && status < 300;
}

// A response URL that is unknown or unparsable gives no evidence of a redirect.
function wasRedirected(responseUrl: string | null, requestUrl: string, baseUrl: string): boolean {
  if (!responseUrl) {
    return false;
  }
  try {
    const response = new URL(responseUrl, baseUrl);
    const request = new URL(requestUrl, baseUrl);
    return (
      response.origin !== request.origin ||
      withoutTrailingSlash(response.pathname) !== withoutTrailingSlash(request.pathname)
    );
  } catch {
    return false;
  }
}

function withoutTrailingSlash(path: string): string {
  return path.length > 1 ? path.replace(/\/+$/, '') : path;
}
