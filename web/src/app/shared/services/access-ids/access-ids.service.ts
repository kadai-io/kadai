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
import { environment } from 'environments/environment';
import { AccessId } from 'app/shared/models/access-id';
import { Observable, of } from 'rxjs';
import { WorkbasketAccessItemsRepresentation } from 'app/shared/models/workbasket-access-items-representation';
import { Sorting, WorkbasketAccessItemQuerySortParameter } from 'app/shared/models/sorting';
import { StartupService } from '../startup/startup.service';
import { WorkbasketAccessItemQueryFilterParameter } from '../../models/workbasket-access-item-query-filter-parameter';
import { QueryPagingParameter } from '../../models/query-paging-parameter';
import { asUrlQueryString } from '../../util/query-parameters-v2';

@Injectable({
  providedIn: 'root'
})
export class AccessIdsService {
  private httpClient = inject(HttpClient);
  private startupService = inject(StartupService);

  get url(): string {
    return this.startupService.getKadaiRestUrl() + '/v1/access-ids';
  }

  searchForAccessId(accessId: string): Observable<AccessId[]> {
    if (!accessId || accessId.length < 3) {
      return of([]);
    }
    return this.httpClient.get<AccessId[]>(`${this.url}?search-for=${accessId}`);
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
        `${environment.kadaiRestUrl}/v1/workbasket-access-items${asUrlQueryString({
          ...filterParameter,
          ...sortParameter,
          ...pagingParameter
        })}`
      )
    );
  }

  removeAccessItemsPermissions(accessId: string) {
    return this.httpClient.delete<WorkbasketAccessItemsRepresentation>(
      `${environment.kadaiRestUrl}/v1/workbasket-access-items?access-id=${accessId}`
    );
  }
}
