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

import { Injectable, signal } from '@angular/core';
import { Observable, of } from 'rxjs';
import { UserInfo } from 'app/shared/models/user-info';
import { Version } from '../../models/version';

@Injectable({
  providedIn: 'root'
})
export class KadaiEngineServiceMock {
  private readonly _currentUserInfo = signal<UserInfo | undefined>(new UserInfo('userid', [''], ['admin']));
  readonly currentUserInfo = this._currentUserInfo.asReadonly();

  // GET
  getUserInformation(): Observable<UserInfo> {
    const userInfo = new UserInfo('userid', [''], ['admin']);
    this._currentUserInfo.set(userInfo);
    return of(userInfo);
  }

  setCurrentUserInfo(userInfo: UserInfo | undefined): void {
    this._currentUserInfo.set(userInfo);
  }

  hasRole(rolesToFind: Array<string>): boolean {
    const user = this._currentUserInfo();
    if (!user || user.roles.length === 0) {
      return false;
    }
    return user.roles.some((role) => rolesToFind.includes(role));
  }

  getVersion(): Observable<Version> {
    const version = new Version('1.0.0');
    return of(version);
  }

  isHistoryProviderEnabled(): Observable<boolean> {
    return of(true);
  }
}
