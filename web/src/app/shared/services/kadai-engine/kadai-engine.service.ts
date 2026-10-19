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

import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, map, tap, timeout } from 'rxjs/operators';
import { UserInfo } from 'app/shared/models/user-info';
import { Version } from 'app/shared/models/version';
import { RuntimeConfigService } from 'app/shared/services/runtime-config/runtime-config.service';

export const LOGOUT_TIMEOUT_MS = 3000;

@Injectable({
  providedIn: 'root'
})
export class KadaiEngineService {
  private httpClient = inject(HttpClient);
  private runtimeConfigService = inject(RuntimeConfigService);

  private readonly _currentUserInfo = signal<UserInfo | undefined>(undefined);
  readonly currentUserInfo = this._currentUserInfo.asReadonly();

  getUserInformation(): Observable<UserInfo> {
    return this.httpClient.get<UserInfo>(`${this.runtimeConfigService.kadaiRestUrl}/v1/current-user-info`).pipe(
      tap((data) => this._currentUserInfo.set(data)),
      catchError((error) => {
        this._currentUserInfo.set(undefined);
        return throwError(() => error);
      })
    );
  }

  hasRole(rolesToFind: string[]): boolean {
    const user = this._currentUserInfo();
    if (!user || !user.roles || user.roles.length === 0) {
      return false;
    }
    return user.roles.some((role) => rolesToFind.includes(role));
  }

  getVersion(): Observable<Version> {
    return this.httpClient.get<Version>(`${this.runtimeConfigService.kadaiRestUrl}/v1/version`);
  }

  /**
   * Sends the logout request and clears the current user. Emits once and completes after the
   * request succeeded, failed or did not answer within {@link LOGOUT_TIMEOUT_MS}; it never errors,
   * so the caller can always redirect afterwards.
   */
  logout(): Observable<void> {
    return this.httpClient.post(this.runtimeConfigService.kadaiLogoutUrl, {}, { responseType: 'text' }).pipe(
      timeout(LOGOUT_TIMEOUT_MS),
      map(() => undefined),
      catchError((error) => {
        console.warn('Logout request failed or timed out, proceeding with redirect:', error);
        return of(undefined);
      }),
      tap(() => this._currentUserInfo.set(undefined))
    );
  }

  isCustomRoutingRulesEnabled(): Observable<boolean> {
    return this.httpClient
      .get<boolean>(`${this.runtimeConfigService.kadaiRestUrl}/v1/routing-rules/routing-rest-enabled`)
      .pipe(catchError(() => of(false)));
  }

  isHistoryProviderEnabled(): Observable<boolean> {
    return this.httpClient.get<boolean>(`${this.runtimeConfigService.kadaiRestUrl}/v1/history-provider-enabled`);
  }
}
