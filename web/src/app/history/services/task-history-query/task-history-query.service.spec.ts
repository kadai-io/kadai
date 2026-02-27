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

import { describe, expect, it, beforeEach, afterEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TaskHistoryQueryService } from './task-history-query.service';
import { StartupService } from '../../../shared/services/startup/startup.service';
import { Direction, Sorting, TaskHistoryQuerySortParameter } from '../../../shared/models/sorting';
import { QueryPagingParameter } from '../../../shared/models/query-paging-parameter';
import { TaskHistoryEventResourceData } from '../../../shared/models/task-history-event-resource';

const REST_URL = 'http://localhost:8080/kadai';
const HISTORY_URL = `${REST_URL}/v1/task-history-event`;

describe('TaskHistoryQueryService', () => {
  let service: TaskHistoryQueryService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: StartupService, useValue: { getKadaiRestUrl: () => REST_URL } }
      ]
    });
    service = TestBed.inject(TaskHistoryQueryService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('url getter should return the base REST URL', () => {
    expect(service.url).toBe(REST_URL);
  });

  it('getTaskHistoryEvents() with no parameters should make a GET request to the history event endpoint', () => {
    service.getTaskHistoryEvents().subscribe();
    const req = httpTesting.expectOne(HISTORY_URL);
    expect(req.request.method).toBe('GET');
    req.flush({ taskHistoryEvents: [], _links: {} });
  });

  it('getTaskHistoryEvents() should return the flushed data', () => {
    const mockResponse: TaskHistoryEventResourceData = {
      taskHistoryEvents: [],
      _links: {}
    };

    let result: TaskHistoryEventResourceData | undefined;
    service.getTaskHistoryEvents().subscribe((data) => (result = data));

    const req = httpTesting.expectOne(HISTORY_URL);
    req.flush(mockResponse);

    expect(result).toEqual(mockResponse);
  });

  it('getTaskHistoryEvents() with sort parameter should include sort-by and order in the URL', () => {
    const sortParam: Sorting<TaskHistoryQuerySortParameter> = {
      'sort-by': TaskHistoryQuerySortParameter.CREATED,
      order: Direction.ASC
    };

    service.getTaskHistoryEvents(undefined, sortParam).subscribe();

    const req = httpTesting.expectOne(
      `${HISTORY_URL}?sort-by=${TaskHistoryQuerySortParameter.CREATED}&order=${Direction.ASC}`
    );
    expect(req.request.method).toBe('GET');
    req.flush({ taskHistoryEvents: [], _links: {} });
  });

  it('getTaskHistoryEvents() with paging parameter should include page and page-size in the URL', () => {
    const pagingParam: QueryPagingParameter = { page: 2, 'page-size': 25 };

    service.getTaskHistoryEvents(undefined, undefined, pagingParam).subscribe();

    const req = httpTesting.expectOne(`${HISTORY_URL}?page=2&page-size=25`);
    expect(req.request.method).toBe('GET');
    req.flush({ taskHistoryEvents: [], _links: {} });
  });

  it('getTaskHistoryEvents() with combined sort and paging parameters builds correct URL', () => {
    const sortParam: Sorting<TaskHistoryQuerySortParameter> = {
      'sort-by': TaskHistoryQuerySortParameter.USER_ID,
      order: Direction.DESC
    };
    const pagingParam: QueryPagingParameter = { page: 1, 'page-size': 10 };

    service.getTaskHistoryEvents(undefined, sortParam, pagingParam).subscribe();

    const req = httpTesting.expectOne(
      `${HISTORY_URL}?sort-by=${TaskHistoryQuerySortParameter.USER_ID}&order=${Direction.DESC}&page=1&page-size=10`
    );
    expect(req.request.method).toBe('GET');
    req.flush({ taskHistoryEvents: [], _links: {} });
  });

  it('getTaskHistoryEvents() with filter parameter should include filter values in the URL', () => {
    const filterParam = { 'task-id': 'TASK-001' };

    service.getTaskHistoryEvents(filterParam as any).subscribe();

    const req = httpTesting.expectOne(`${HISTORY_URL}?task-id=TASK-001`);
    expect(req.request.method).toBe('GET');
    req.flush({ taskHistoryEvents: [], _links: {} });
  });
});
