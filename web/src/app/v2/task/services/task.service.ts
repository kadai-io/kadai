/*
 * Copyright [2024] [envite consulting GmbH]
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

import { Task } from '@task/models/task';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Sorting, TaskQuerySortParameter } from 'app/shared/models/sorting';
import { StartupService } from 'app/shared/services/startup/startup.service'; /* @TODO Legacy */
import { asUrlQueryString } from 'app/shared/util/query-parameters-v2'; /* @TODO Legacy */
import { TaskQueryFilterParameter } from 'app/shared/models/task-query-filter-parameter'; /* @TODO Legacy */
import { QueryPagingParameter } from 'app/shared/models/query-paging-parameter'; /* @TODO Legacy */
import { PagedTaskSummary } from '@task/models/paged-task';

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  constructor(private httpClient: HttpClient, private startupService: StartupService) {}

  get url(): string {
    return this.startupService.getKadaiRestUrl() + '/v1/tasks';
  }

  createTask(task: Task): Observable<Task> {
    return this.httpClient.post<Task>(this.url, task);
  }

  getTasks(
    filterParameter?: TaskQueryFilterParameter,
    sortParameter?: Sorting<TaskQuerySortParameter>,
    pagingParameter?: QueryPagingParameter
  ): Observable<PagedTaskSummary> {
    const url: string = `${this.url}${asUrlQueryString({ ...filterParameter, ...sortParameter, ...pagingParameter })}`;

    return this.httpClient.get<PagedTaskSummary>(url);
  }

  getTask(id: string): Observable<Task> {
    return this.httpClient.get<Task>(`${this.url}/${id}`);
  }

  updateTask(task: Task): Observable<Task> {
    return this.httpClient.put<Task>(`${this.url}/${task.taskId}`, task);
  }

  completeTask(id: string): Observable<Task> {
    return this.httpClient.post<Task>(`${this.url}/${id}/complete`, '');
  }

  claimTask(id: string): Observable<Task> {
    return this.httpClient.post<Task>(`${this.url}/${id}/claim`, 'test');
  }

  cancelClaimTask(id: string): Observable<Task> {
    return this.httpClient.delete<Task>(`${this.url}/${id}/claim`);
  }

  transferTask(taskId: string, workbasketId: string): Observable<Task> {
    return this.httpClient.post<Task>(`${this.url}/${taskId}/transfer/${workbasketId}`, '');
  }

  deleteTask(task: Task): Observable<Task> {
    return this.httpClient.delete<Task>(`${this.url}/${task.taskId}`);
  }
}
