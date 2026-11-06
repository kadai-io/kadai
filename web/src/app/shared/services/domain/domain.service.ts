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
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, combineLatest, concat, Observable, ReplaySubject, throwError } from 'rxjs';
import { catchError, distinctUntilChanged, filter, ignoreElements, map, shareReplay, tap } from 'rxjs/operators';
import { RequestInProgressService } from 'app/shared/services/request-in-progress/request-in-progress.service';
import { SelectedRouteService } from 'app/shared/services/selected-route/selected-route';
import { RuntimeConfigService } from 'app/shared/services/runtime-config/runtime-config.service';

@Injectable({
  providedIn: 'root'
})
export class DomainService {
  private httpClient = inject(HttpClient);
  private requestInProgressService = inject(RequestInProgressService);
  private selectedRouteService = inject(SelectedRouteService);
  private runtimeConfigService = inject(RuntimeConfigService);

  private domainSelectedValue!: string;
  private domainSelected = new ReplaySubject<string>(1);
  private restDomains$ = new BehaviorSubject<string[] | undefined>(undefined);
  private hasMasterDomain$ = new BehaviorSubject(false);
  // never errors: a failed request only reaches the caller that triggered it
  private domains$: Observable<string[]> = combineLatest([
    this.restDomains$.pipe(filter((domains): domains is string[] => domains !== undefined)),
    this.hasMasterDomain$.pipe(distinctUntilChanged())
  ]).pipe(
    map(([domains, hasMasterDomain]) =>
      hasMasterDomain && domains.some((domain) => domain !== '') ? [...domains, ''] : domains
    )
  );
  private request$?: Observable<string[]>;

  constructor() {
    this.selectedRouteService
      .getSelectedRoute()
      .pipe(takeUntilDestroyed())
      .subscribe((value: string) => {
        if (value.indexOf('workbaskets') === 0) {
          this.removeMasterDomain();
          const firstDomain = this.restDomains$.value?.[0];
          if (this.domainSelectedValue === '' && firstDomain !== undefined) {
            this.selectDomain(firstDomain);
          }
        } else if (value.indexOf('classifications') === 0) {
          this.addMasterDomain();
        }
      });
  }

  get url(): string {
    return this.runtimeConfigService.kadaiRestUrl + '/v1/domains';
  }

  // GET
  getDomains(forceRefresh = false): Observable<string[]> {
    if (forceRefresh || !this.request$) {
      const request$: Observable<string[]> = this.httpClient.get<string[]>(this.url).pipe(
        tap((domains) => {
          this.restDomains$.next(domains);
          if (!this.domainSelectedValue && domains.length > 0) {
            this.selectDomain(domains[0]);
          }
        }),
        catchError((error) => {
          if (this.request$ === request$) {
            this.request$ = undefined; // let the next caller retry
          }
          return throwError(() => error);
        }),
        shareReplay(1)
      );
      this.request$ = request$;
    }
    return concat(this.request$.pipe(ignoreElements()), this.domains$);
  }

  getSelectedDomain(): Observable<string> {
    return this.domainSelected.asObservable();
  }

  switchDomain(value: string) {
    this.selectDomain(value);
  }

  /*
  called when all inner subscriptions of getSelectedDomain have finished
  */
  domainChangedComplete() {
    this.requestInProgressService.setRequestInProgress(false);
  }

  getSelectedDomainValue() {
    return this.domainSelectedValue;
  }

  addMasterDomain() {
    this.hasMasterDomain$.next(true);
  }

  removeMasterDomain() {
    this.hasMasterDomain$.next(false);
  }

  private selectDomain(value: string) {
    this.domainSelectedValue = value;
    this.domainSelected.next(value);
  }
}
