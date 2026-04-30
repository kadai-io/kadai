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
import { TaskHistoryEventResourceData } from 'app/shared/models/task-history-event-resource';
import { QueryParameters } from 'app/shared/models/query-parameters';
import { KadaiQueryParameters } from 'app/shared/util/query-parameters';
import { Sorting, TaskHistoryQuerySortParameter } from 'app/shared/models/sorting';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { StartupService } from '../../../shared/services/startup/startup.service';
import { TaskHistoryQueryFilterParameter } from '../../../shared/models/task-history-query-filter-parameter';
import { QueryPagingParameter } from '../../../shared/models/query-paging-parameter';
import { asUrlQueryString } from '../../../shared/util/query-parameters-v2';

@Injectable({
  providedIn: 'root'
})
export class TaskHistoryQueryService {
  private httpClient = inject(HttpClient);
  private startupService = inject(StartupService);

  get url(): string {
    return this.startupService.getKadaiRestUrl();
  }

  getTaskHistoryEvents(
    filterParameter?: TaskHistoryQueryFilterParameter,
    sortParameter?: Sorting<TaskHistoryQuerySortParameter>,
    pagingParameter?: QueryPagingParameter
  ): Observable<TaskHistoryEventResourceData> {
    return this.httpClient.get<TaskHistoryEventResourceData>(
      `${this.url}/v1/task-history-event${asUrlQueryString({
        ...filterParameter,
        ...sortParameter,
        ...pagingParameter
      })}`
    );
  }
}
