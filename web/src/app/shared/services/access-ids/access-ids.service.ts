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

import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { AccessId } from 'app/shared/models/access-id';
import { Observable, of } from 'rxjs';
import { WorkbasketAccessItemsRepresentation } from 'app/shared/models/workbasket-access-items-representation';
import { Sorting, WorkbasketAccessItemQuerySortParameter } from 'app/shared/models/sorting';
import { WorkbasketAccessItemQueryFilterParameter } from '../../models/workbasket-access-item-query-filter-parameter';
import { QueryPagingParameter } from '../../models/query-paging-parameter';
import { asUrlQueryString } from '../../util/query-parameters-v2';
import { RuntimeConfigService } from '../runtime-config/runtime-config.service';

@Injectable({
  providedIn: 'root'
})
export class AccessIdsService {
  private httpClient = inject(HttpClient);
  private runtimeConfigService = inject(RuntimeConfigService);

  get url(): string {
    return this.runtimeConfigService.kadaiRestUrl + '/v1/access-ids';
  }

  searchForAccessId(accessId: string): Observable<AccessId[]> {
    if (!accessId || accessId.length < 3) {
      return of([]);
    }
    return this.httpClient.get<AccessId[]>(`${this.url}?search-for=${accessId}`);
  }

  validateAccessId(accessId: string): Observable<boolean> {
    if (!accessId) {
      return of(false);
    }
    return this.httpClient.get<boolean>(`${this.url}/validation`, {
      params: { 'access-id': accessId }
    });
  }

  getGroupsByAccessId(accessId: string): Observable<AccessId[]> {
    if (!accessId || accessId.length < 3) {
      return of([]);
    }
    return this.httpClient.get<AccessId[]>(`${this.url}/groups?access-id=${accessId}`);
  }

  getPermissionsByAccessId(accessId: string): Observable<AccessId[]> {
    if (!accessId || accessId.length < 3) {
      return of([]);
    }
    return this.httpClient.get<AccessId[]>(`${this.url}/permissions?access-id=${accessId}`);
  }

  getAccessItems(
    filterParameter?: WorkbasketAccessItemQueryFilterParameter,
    sortParameter?: Sorting<WorkbasketAccessItemQuerySortParameter>,
    pagingParameter?: QueryPagingParameter
  ): Observable<WorkbasketAccessItemsRepresentation> {
    return this.httpClient.get<WorkbasketAccessItemsRepresentation>(
      encodeURI(
        `${this.runtimeConfigService.kadaiRestUrl}/v1/workbasket-access-items${asUrlQueryString({
          ...filterParameter,
          ...sortParameter,
          ...pagingParameter
        })}`
      )
    );
  }

  removeAccessItemsPermissions(accessId: string) {
    return this.httpClient.delete<WorkbasketAccessItemsRepresentation>(
      `${this.runtimeConfigService.kadaiRestUrl}/v1/workbasket-access-items?access-id=${accessId}`
    );
  }
}
