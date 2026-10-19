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

import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable, InjectionToken, signal } from '@angular/core';
import { environment } from 'environments/environment';
import { firstValueFrom, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

export interface RuntimeConfig {
  kadaiRestUrl: string;
  kadaiLogoutUrl: string;
  // Empty string means "not configured": unauthenticated users are not redirected to a login page.
  kadaiLoginUrl: string;
}

const RUNTIME_CONFIG_KEYS: ReadonlyArray<keyof RuntimeConfig> = ['kadaiRestUrl', 'kadaiLogoutUrl', 'kadaiLoginUrl'];
// For these keys an empty string is a meaningful override instead of "not set".
const EMPTY_VALUE_KEYS: ReadonlyArray<keyof RuntimeConfig> = ['kadaiLoginUrl'];

export const RUNTIME_CONFIG_URL = 'environments/data-sources/environment-information.json';

export const RUNTIME_CONFIG_DEFAULTS = new InjectionToken<Readonly<RuntimeConfig>>('RUNTIME_CONFIG_DEFAULTS', {
  providedIn: 'root',
  factory: () => ({
    kadaiRestUrl: environment.kadaiRestUrl,
    kadaiLogoutUrl: environment.kadaiLogoutUrl,
    kadaiLoginUrl: environment.kadaiLoginUrl
  })
});

@Injectable({
  providedIn: 'root'
})
export class RuntimeConfigService {
  private readonly httpClient = inject(HttpClient);
  private readonly defaults = inject(RUNTIME_CONFIG_DEFAULTS);
  private readonly _config = signal<Readonly<RuntimeConfig>>({ ...this.defaults });

  readonly config = this._config.asReadonly();

  get kadaiRestUrl(): string {
    return this._config().kadaiRestUrl;
  }

  get kadaiLogoutUrl(): string {
    return this._config().kadaiLogoutUrl;
  }

  get kadaiLoginUrl(): string {
    return this._config().kadaiLoginUrl;
  }

  async load(): Promise<void> {
    const overrides = await firstValueFrom(
      this.httpClient.get<unknown>(RUNTIME_CONFIG_URL).pipe(
        map((raw) => parseRuntimeConfig(raw)),
        catchError((error) => {
          logLoadError(error);
          return of({});
        })
      )
    );
    this._config.set({ ...this.defaults, ...overrides });
  }
}

export function parseRuntimeConfig(raw: unknown): Partial<RuntimeConfig> {
  if (raw === null || raw === undefined) {
    console.info('Runtime configuration is empty, using build-time defaults.');
    return {};
  }
  if (typeof raw !== 'object' || Array.isArray(raw)) {
    console.error('Runtime configuration is not a JSON object, using build-time defaults:', raw);
    return {};
  }

  const config: Partial<RuntimeConfig> = {};
  for (const key of RUNTIME_CONFIG_KEYS) {
    const value = (raw as Record<string, unknown>)[key];
    if (value === undefined || value === null || (value === '' && !EMPTY_VALUE_KEYS.includes(key))) {
      continue;
    }
    if (typeof value === 'string') {
      config[key] = value;
    } else {
      console.error(`Runtime configuration property "${key}" is not a string, using build-time default:`, value);
    }
  }
  return config;
}

function logLoadError(error: unknown): void {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 404) {
      console.info('Runtime configuration file not found, using build-time defaults.');
      return;
    }
    if (error.status === 0) {
      console.error('Runtime configuration file is unreachable, using build-time defaults:', error);
      return;
    }
    if (error.status >= 200 && error.status < 300) {
      console.error('Runtime configuration file is not valid JSON, using build-time defaults:', error);
      return;
    }
  }
  console.error('Failed to load runtime configuration, using build-time defaults:', error);
}
