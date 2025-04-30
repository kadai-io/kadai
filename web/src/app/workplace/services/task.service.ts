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

import { Task } from 'app/workplace/models/task';
import { Observable, Subject } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { TaskResource } from 'app/workplace/models/task-resource';
import { Sorting, TaskQuerySortParameter } from 'app/shared/models/sorting';
import { StartupService } from '../../shared/services/startup/startup.service';
import { asUrlQueryString } from '../../shared/util/query-parameters-v2';
import { TaskQueryFilterParameter } from '../../shared/models/task-query-filter-parameter';
import { QueryPagingParameter } from '../../shared/models/query-paging-parameter';

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private httpClient = inject(HttpClient);
  private startupService = inject(StartupService);

  private taskChangedSource = new Subject<Task>();
  taskChangedStream = this.taskChangedSource.asObservable();
  private taskSelectedSource = new Subject<Task>();
  taskSelectedStream = this.taskSelectedSource.asObservable();
  private taskDeletedSource = new Subject<Task>();
  taskDeletedStream = this.taskDeletedSource.asObservable();

  get url(): string {
    return this.startupService.getKadaiRestUrl() + '/v1/tasks';
  }

  private static convertTasksDatesToGMT(task: Task): Task {
    const timeAttributes = ['created', 'claimed', 'completed', 'modified', 'planned', 'due'];
    timeAttributes.forEach((attributeName) => {
      if (task[attributeName]) {
        task[attributeName] = new Date(task[attributeName]).toISOString();
      }
    });
    return task;
  }

  publishUpdatedTask(task?: Task) {
    this.taskChangedSource.next(task);
  }

  publishTaskDeletion() {
    this.taskDeletedSource.next(null);
  }

  selectTask(task?: Task) {
    this.taskSelectedSource.next(task);
  }

  getSelectedTask(): Observable<Task> {
    return this.taskSelectedStream;
  }

  findTasksWithWorkbasket(
    filterParameter: TaskQueryFilterParameter,
    sortParameter: Sorting<TaskQuerySortParameter>,
    pagingParameter: QueryPagingParameter
  ): Observable<TaskResource> {
    const url = `${this.url}${asUrlQueryString({ ...filterParameter, ...sortParameter, ...pagingParameter })}`;
    return this.httpClient.get<TaskResource>(url);
  }

  getTask(id: string): Observable<Task> {
    return this.httpClient.get<Task>(`${this.url}/${id}`);
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

  updateTask(task: Task): Observable<Task> {
    const taskConv = TaskService.convertTasksDatesToGMT(task);
    return this.httpClient.put<Task>(`${this.url}/${task.taskId}`, taskConv);
  }

  deleteTask(task: Task): Observable<Task> {
    return this.httpClient.delete<Task>(`${this.url}/${task.taskId}`);
  }

  createTask(task: Task): Observable<Task> {
    return this.httpClient.post<Task>(this.url, task);
  }
}
