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

import { describe, expect, it, beforeEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { TaskPriorityReportFilterStateService } from './task-priority-report-filter-state.service';

describe('TaskPriorityReportFilterStateService', () => {
  let service: TaskPriorityReportFilterStateService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TaskPriorityReportFilterStateService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('currentFilter signal should start as an empty object', () => {
    expect(service.currentFilter()).toEqual({});
  });

  it('activeFilters signal should start as an empty array', () => {
    expect(service.activeFilters()).toEqual([]);
  });

  it('should update currentFilter when .set() is called', () => {
    const newFilter = { priority: 'HIGH', domain: 'TEST' };
    service.currentFilter.set(newFilter);
    expect(service.currentFilter()).toEqual(newFilter);
  });

  it('should update activeFilters when .set() is called with a list of filter names', () => {
    const filters = ['priority', 'domain', 'state'];
    service.activeFilters.set(filters);
    expect(service.activeFilters()).toEqual(filters);
  });

  it('should reflect an empty object after resetting currentFilter', () => {
    service.currentFilter.set({ someFilter: 'value' });
    service.currentFilter.set({});
    expect(service.currentFilter()).toEqual({});
  });

  it('should reflect an empty array after resetting activeFilters', () => {
    service.activeFilters.set(['filter1', 'filter2']);
    service.activeFilters.set([]);
    expect(service.activeFilters()).toEqual([]);
  });

  it('currentFilter and activeFilters should be independent signals', () => {
    service.currentFilter.set({ x: 1 });
    expect(service.activeFilters()).toEqual([]);

    service.activeFilters.set(['x']);
    expect(service.currentFilter()).toEqual({ x: 1 });
  });
});
