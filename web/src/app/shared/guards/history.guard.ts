/*
 * Copyright [2024] [envite consulting GmbH]
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
import { CanActivate, Router, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { KadaiEngineService } from 'app/shared/services/kadai-engine/kadai-engine.service';
import { catchError, map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class HistoryGuard implements CanActivate {
  constructor(private kadaiEngineService: KadaiEngineService, public router: Router) {}

  canActivate(): Observable<boolean | UrlTree> {
    return this.kadaiEngineService.isHistoryProviderEnabled().pipe(
      map((value) => {
        if (value) {
          return value;
        }
        return this.router.parseUrl('/kadai/workplace');
      }),
      catchError(() => {
        return of(this.router.parseUrl('/kadai/workplace'));
      })
    );
  }
}
