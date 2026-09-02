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

import { Injectable } from '@angular/core';
import { BehaviorSubject, distinctUntilChanged, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RequestInProgressService {
  private activeRequestsCount = 0;
  private readonly requestInProgressSubject = new BehaviorSubject<boolean>(false);

  setRequestInProgress(value: boolean) {
    if(value) {
      this.activeRequestsCount++;
      if(this.activeRequestsCount === 1) {
        this.requestInProgressSubject.next(true);
      } 
    } else {
      if (this.activeRequestsCount > 0) {
        this.activeRequestsCount--;
      }
      if (this.activeRequestsCount === 0) {
        this.requestInProgressSubject.next(false);
      }
    }

    this.requestInProgressSubject.next(value);
  }

  getRequestInProgress(): Observable<boolean> {
    return this.requestInProgressSubject.asObservable().pipe(
      distinctUntilChanged()
    );
  }
}
