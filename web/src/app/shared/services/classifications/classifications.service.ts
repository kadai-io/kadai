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
import { Observable } from 'rxjs';

import { Classification } from 'app/shared/models/classification';

import { ClassificationPagingList } from 'app/shared/models/classification-paging-list';
import { DomainService } from 'app/shared/services/domain/domain.service';
import { ClassificationQuerySortParameter, Sorting } from 'app/shared/models/sorting';
import { StartupService } from '../startup/startup.service';
import { asUrlQueryString } from '../../util/query-parameters-v2';
import { ClassificationQueryFilterParameter } from '../../models/classification-query-filter-parameter';
import { QueryPagingParameter } from '../../models/query-paging-parameter';

@Injectable({
  providedIn: 'root'
})
export class ClassificationsService {
  private httpClient = inject(HttpClient);
  private domainService = inject(DomainService);
  private startupService = inject(StartupService);

  get url(): string {
    return this.startupService.getKadaiRestUrl() + '/v1/classifications';
  }

  // GET
  getClassifications(
    filterParameter?: ClassificationQueryFilterParameter,
    sortParameter?: Sorting<ClassificationQuerySortParameter>,
    pagingParameter?: QueryPagingParameter
  ): Observable<ClassificationPagingList> {
    return this.httpClient.get<ClassificationPagingList>(
      `${this.url}${asUrlQueryString({ ...filterParameter, ...sortParameter, ...pagingParameter })}`
    );
  }

  // GET
  getClassification(id: string): Observable<Classification> {
    return this.httpClient.get<Classification>(`${this.url}/${id}`);
  }

  // POST
  postClassification(classification: Classification): Observable<Classification> {
    return this.httpClient.post<Classification>(`${this.url}`, classification);
  }

  // PUT
  putClassification(classification: Classification): Observable<Classification> {
    return this.httpClient.put<Classification>(`${this.url}/${classification.classificationId}`, classification);
  }

  // DELETE
  deleteClassification(id: string): Observable<string> {
    return this.httpClient.delete<string>(`${this.url}/${id}`);
  }
}
