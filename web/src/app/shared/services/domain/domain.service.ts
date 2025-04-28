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

import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, ReplaySubject } from 'rxjs';
import { Router } from '@angular/router';
import { RequestInProgressService } from '../request-in-progress/request-in-progress.service';
import { SelectedRouteService } from '../selected-route/selected-route';
import { StartupService } from '../startup/startup.service';

@Injectable({
  providedIn: 'root'
})
export class DomainService {
  private httpClient = inject(HttpClient);
  private router = inject(Router);
  private requestInProgressService = inject(RequestInProgressService);
  private selectedRouteService = inject(SelectedRouteService);
  private startupService = inject(StartupService);

  private domainRestValue: Array<string> = new Array<string>();
  private domainValue: Array<string> = new Array<string>();
  private domainSelectedValue: string;
  private domainSelected = new ReplaySubject<string>(1);
  private dataObs$ = new ReplaySubject<Array<string>>(1);
  private hasMasterDomain = false;

  constructor() {
    this.selectedRouteService.getSelectedRoute().subscribe((value: string) => {
      if (value.indexOf('workbaskets') === 0) {
        this.hasMasterDomain = false;
        this.removeMasterDomain();
        if (this.domainSelectedValue === '') {
          this.selectDomain(this.domainValue[0]);
        }
      } else if (value.indexOf('classifications') === 0) {
        this.hasMasterDomain = true;
        this.addMasterDomain();
      }
    });
  }

  // url loads rest url value from startUpService which is a http call to ensure url it is always loaded from environment-information.json
  get url(): string {
    return this.startupService.getKadaiRestUrl() + '/v1/domains';
  }

  // GET
  getDomains(forceRefresh = false): Observable<string[]> {
    if (!this.dataObs$.observers.length || forceRefresh) {
      this.httpClient.get<string[]>(this.url).subscribe(
        (domains) => {
          this.domainRestValue = domains;
          this.domainValue = domains;
          this.dataObs$.next(this.hasMasterDomain ? this.addEmptyDomain(domains) : domains);
          if (!this.domainSelectedValue && this.domainValue.length > 0) {
            this.selectDomain(this.domainValue[0]);
          }
        },
        (error) => {
          this.dataObs$.error(error);
          this.dataObs$ = new ReplaySubject(1);
        }
      );
    }

    return this.dataObs$;
  }

  getSelectedDomain(): Observable<string> {
    return this.domainSelected.asObservable();
  }

  switchDomain(value: string) {
    this.selectDomain(value);
  }

  /*
    This function should be called after getSelectedDomain inner subscriptions have been finished
   */
  domainChangedComplete() {
    this.requestInProgressService.setRequestInProgress(false);
  }

  getSelectedDomainValue() {
    return this.domainSelectedValue;
  }

  addMasterDomain() {
    if (this.domainValue.some((domain) => domain !== '')) {
      this.dataObs$.next(this.addEmptyDomain(this.domainRestValue));
    }
  }

  removeMasterDomain() {
    if (this.domainValue.some((domain) => domain === '')) {
      this.domainValue = this.domainRestValue;
      this.dataObs$.next(this.domainValue);
    }
  }

  private selectDomain(value: string) {
    this.domainSelectedValue = value;
    this.domainSelected.next(value);
  }

  private addEmptyDomain(domains: Array<string>): Array<string> {
    this.domainValue = Object.assign([], domains);
    this.domainValue.push('');
    return this.domainValue;
  }

  private getNavigationUrl(): string {
    if (this.router.url.indexOf('workbaskets') !== -1) {
      return 'kadai/administration/workbaskets';
    }
    if (this.router.url.indexOf('classifications') !== -1) {
      return 'kadai/administration/classifications';
    }
    return '';
  }
}
