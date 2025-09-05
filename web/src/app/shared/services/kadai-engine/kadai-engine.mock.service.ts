/*
 * Copyright [2025] [envite consulting GmbH]
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

import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { UserInfo } from 'app/shared/models/user-info';
import { Version } from '../../models/version';

@Injectable({
  providedIn: 'root'
})
export class KadaiEngineServiceMock {
  currentUserInfo: UserInfo;

  constructor() {
    this.getUserInformation();
  }

  // GET
  getUserInformation(): Promise<any> {
    this.currentUserInfo = new UserInfo('userid', [''], ['admin']);
    return of(undefined).toPromise();
  }

  hasRole(roles2Find: Array<string>): boolean {
    if (!this.currentUserInfo || this.currentUserInfo.roles.length < 1) {
      return false;
    }
    if (this.findRole(roles2Find)) {
      return true;
    }
    return false;
  }

  getVersion(): Observable<Version> {
    const version = new Version('1.0.0');
    return of(version);
  }

  isHistoryProviderEnabled(): Observable<boolean> {
    return of(true);
  }

  private findRole(roles2Find: Array<string>) {
    return this.currentUserInfo.roles.find((role) => roles2Find.some((roleLookingFor) => role === roleLookingFor));
  }
}
