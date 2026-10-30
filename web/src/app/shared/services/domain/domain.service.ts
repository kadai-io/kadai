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
import { HttpClient } from '@angular/common/http';
import { Observable, ReplaySubject } from 'rxjs';
import { RequestInProgressService } from '../request-in-progress/request-in-progress.service';
import { SelectedRouteService } from '../selected-route/selected-route';
import { RuntimeConfigService } from '../runtime-config/runtime-config.service';

export const SELECTED_DOMAIN_STORAGE_KEY = 'kadai.selectedDomain';

@Injectable({
  providedIn: 'root'
})
export class DomainService {
  private httpClient = inject(HttpClient);
  private requestInProgressService = inject(RequestInProgressService);
  private selectedRouteService = inject(SelectedRouteService);
  private runtimeConfigService = inject(RuntimeConfigService);

  private domainRestValue: Array<string> = new Array<string>();
  private domainValue: Array<string> = new Array<string>();
  private domainSelectedValue: string | undefined;
  private readonly storedDomain = readStoredDomain();
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
  get url(): string {
    return this.runtimeConfigService.kadaiRestUrl + '/v1/domains';
  }

  // GET
  getDomains(forceRefresh = false): Observable<string[]> {
    if (!this.dataObs$.observers.length || forceRefresh) {
      this.httpClient.get<string[]>(this.url).subscribe(
        (domains) => {
          this.domainRestValue = domains;
          this.domainValue = domains;
          this.dataObs$.next(this.hasMasterDomain ? this.addEmptyDomain(domains) : domains);
          this.selectOfferedDomain(domains);
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

  getSelectedDomainValue(): string | undefined {
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

  // The master domain is the empty string, so an absent selection has to be undefined rather than falsy:
  // treating '' as "nothing selected" made every reload of the domain list drop it for the first domain.
  private selectOfferedDomain(domains: Array<string>): void {
    if (domains.length === 0) {
      return;
    }
    const current = this.domainSelectedValue ?? this.storedDomain;
    const offered = current !== undefined && domains.includes(current) ? current : domains[0];
    if (offered !== this.domainSelectedValue) {
      this.selectDomain(offered);
    }
  }

  private selectDomain(value: string) {
    this.domainSelectedValue = value;
    storeDomain(value);
    this.domainSelected.next(value);
  }

  private addEmptyDomain(domains: Array<string>): Array<string> {
    this.domainValue = Object.assign([], domains);
    this.domainValue.push('');
    return this.domainValue;
  }
}

function readStoredDomain(): string | undefined {
  try {
    return sessionStorage.getItem(SELECTED_DOMAIN_STORAGE_KEY) ?? undefined;
  } catch {
    return undefined;
  }
}

function storeDomain(value: string): void {
  try {
    sessionStorage.setItem(SELECTED_DOMAIN_STORAGE_KEY, value);
  } catch {
    // Storage can be unavailable; the selection then only lives as long as the page.
  }
}
