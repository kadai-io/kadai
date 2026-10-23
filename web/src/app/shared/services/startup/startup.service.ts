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
    try {
      await firstValueFrom(this.kadaiEngineService.getUserInformation());
    } catch (error) {
      const loginUrl = this.runtimeConfigService.kadaiLoginUrl;
      if (loginUrl && isAuthenticationError(error, loginUrl, this.document.baseURI)) {
        this.window.nativeWindow.location.href = loginUrl;
        return new Promise<never>(() => {});
      }
      console.error('Failed to load current user, starting without a user session:', error);
    }
  }
}

// Spring Security answers unauthenticated XHR requests either with 401 or with a redirect to the
// login page, which the browser follows transparently. In the latter case the response URL
// points to the configured login page and parsing its HTML as JSON fails.
function isAuthenticationError(error: unknown, loginUrl: string, baseUrl: string): boolean {
  if (!(error instanceof HttpErrorResponse)) {
    return false;
  }
  return error.status === 401 || isSameEndpoint(error.url, loginUrl, baseUrl);
}

function isSameEndpoint(actualUrl: string | null, expectedUrl: string, baseUrl: string): boolean {
  if (!actualUrl) {
    return false;
  }
  try {
    const actual = new URL(actualUrl, baseUrl);
    const expected = new URL(expectedUrl, baseUrl);
    return (
      actual.origin === expected.origin &&
      withoutTrailingSlash(actual.pathname) === withoutTrailingSlash(expected.pathname)
    );
  } catch {
    return false;
  }
}

function withoutTrailingSlash(path: string): string {
  return path.length > 1 ? path.replace(/\/+$/, '') : path;
}
