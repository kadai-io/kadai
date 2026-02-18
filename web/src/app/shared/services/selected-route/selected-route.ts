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

import { inject, Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { NavigationEnd, Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class SelectedRouteService {
  public selectedRouteTriggered = new Subject<string>();
  private router = inject(Router);
  private detailRoutes: Array<string> = ['workplace', 'administration', 'monitor', 'history', 'settings'];

  selectRoute(value: NavigationEnd): void {
    this.selectedRouteTriggered.next(this.getRoute(value));
  }

  getSelectedRoute(): Observable<string> {
    return this.selectedRouteTriggered.asObservable();
  }

  private getRoute(event: NavigationEnd): string {
    if (!event) {
      return this.checkUrl(this.router.url);
    }
    return this.checkUrl(event.urlAfterRedirects);
  }

  private checkUrl(url: string): string {
    return this.detailRoutes.find((routeDetail) => url.includes(routeDetail)) || '';
  }
}
