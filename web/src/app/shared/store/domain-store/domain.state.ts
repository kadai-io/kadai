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
import { Action, State, StateContext } from '@ngxs/store';
import { Observable, of } from 'rxjs';
import { take, tap } from 'rxjs/operators';
import { RuntimeConfigService } from '../../services/runtime-config/runtime-config.service';
import { FetchDomains, SelectDomain } from './domain.actions';

export const DOMAIN_QUERY_PARAM = 'domain';

export interface DomainStateModel {
  domains: Array<string>;
  selectedDomain: string | undefined;
}

@Injectable({
  providedIn: 'root'
})
@State<DomainStateModel>({
  name: 'domain',
  defaults: {
    domains: [],
    selectedDomain: undefined
  }
})
export class DomainState {
  private httpClient = inject(HttpClient);
  private runtimeConfigService = inject(RuntimeConfigService);

  get url(): string {
    return this.runtimeConfigService.kadaiRestUrl + '/v1/domains';
  }

  @Action(FetchDomains)
  fetchDomains(ctx: StateContext<DomainStateModel>, action: FetchDomains): Observable<Array<string>> {
    if (!action.forceRefresh && ctx.getState().domains.length > 0) {
      return of(ctx.getState().domains);
    }

    return this.httpClient.get<Array<string>>(this.url).pipe(
      take(1),
      tap((domains) => {
        ctx.patchState({ domains });
        this.selectOfferedDomain(ctx, domains, action.preferredDomain);
      })
    );
  }

  @Action(SelectDomain)
  selectDomain(ctx: StateContext<DomainStateModel>, action: SelectDomain): Observable<null> {
    ctx.patchState({ selectedDomain: action.domain });
    return of(null);
  }

  // The master domain is the empty string, so an absent selection has to be undefined rather than falsy:
  // treating '' as "nothing selected" made every reload of the domain list drop it for the first domain.
  private selectOfferedDomain(
    ctx: StateContext<DomainStateModel>,
    domains: Array<string>,
    preferredDomain?: string
  ): void {
    const { selectedDomain } = ctx.getState();
    const current = selectedDomain ?? preferredDomain;
    const offered = current !== undefined && domains.includes(current) ? current : domains[0];
    if (offered !== undefined && offered !== selectedDomain) {
      ctx.dispatch(new SelectDomain(offered));
    }
  }
}
