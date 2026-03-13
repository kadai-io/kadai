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

import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { TaskPriorityReportFilterStateService } from './task-priority-report-filter-state.service';

describe('TaskPriorityReportFilterStateService', () => {
  let service: TaskPriorityReportFilterStateService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TaskPriorityReportFilterStateService]
    });
    service = TestBed.inject(TaskPriorityReportFilterStateService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
    expect(service.currentFilter()).toEqual({});
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

  it('should update currentFilter using .update()', () => {
    service.currentFilter.set({ priority: 'LOW' });
    service.currentFilter.update((prev) => ({ ...prev, domain: 'DOMAIN_A' }));
    expect(service.currentFilter()).toEqual({ priority: 'LOW', domain: 'DOMAIN_A' });
  });

  it('should update activeFilters using .update()', () => {
    service.activeFilters.set(['priority']);
    service.activeFilters.update((prev) => [...prev, 'domain']);
    expect(service.activeFilters()).toEqual(['priority', 'domain']);
  });

  it('should remove a filter from activeFilters using .update()', () => {
    service.activeFilters.set(['priority', 'domain', 'state']);
    service.activeFilters.update((prev) => prev.filter((f) => f !== 'domain'));
    expect(service.activeFilters()).toEqual(['priority', 'state']);
  });

  it('should overwrite all fields in currentFilter using .update()', () => {
    service.currentFilter.set({ priority: 'LOW', domain: 'DOMAIN_A' });
    service.currentFilter.update(() => ({ priority: 'HIGH' }));
    expect(service.currentFilter()).toEqual({ priority: 'HIGH' });
  });

  it('should handle update on empty activeFilters gracefully', () => {
    service.activeFilters.update((prev) => [...prev, 'newFilter']);
    expect(service.activeFilters()).toEqual(['newFilter']);
  });

  it('should handle update on empty currentFilter gracefully', () => {
    service.currentFilter.update((prev) => ({ ...prev, extra: 'value' }));
    expect(service.currentFilter()).toEqual({ extra: 'value' });
  });

  it('should inject the same service instance (singleton) when injected twice', () => {
    const service2 = TestBed.inject(TaskPriorityReportFilterStateService);
    expect(service2).toBe(service);
  });

  it('should keep currentFilter state across multiple reads', () => {
    service.currentFilter.set({ a: 1, b: 2 });
    expect(service.currentFilter()).toEqual({ a: 1, b: 2 });
    expect(service.currentFilter()).toEqual({ a: 1, b: 2 });
  });

  it('should keep activeFilters state across multiple reads', () => {
    service.activeFilters.set(['x', 'y', 'z']);
    expect(service.activeFilters()).toEqual(['x', 'y', 'z']);
    expect(service.activeFilters()).toEqual(['x', 'y', 'z']);
  });

  it('should support setting complex objects in currentFilter', () => {
    const complexFilter = {
      state: ['READY', 'CLAIMED'],
      priority: [1, 2, 3],
      nested: { key: 'value' }
    };
    service.currentFilter.set(complexFilter);
    expect(service.currentFilter()).toEqual(complexFilter);
  });

  it('should allow setting currentFilter to null and back to valid object', () => {
    service.currentFilter.set(null as any);
    expect(service.currentFilter()).toBeNull();
    service.currentFilter.set({ restored: true });
    expect(service.currentFilter()).toEqual({ restored: true });
  });

  it('should allow setting activeFilters to null and back to valid array', () => {
    service.activeFilters.set(null as any);
    expect(service.activeFilters()).toBeNull();
    service.activeFilters.set(['restored']);
    expect(service.activeFilters()).toEqual(['restored']);
  });

  it('should handle update() with function that returns null for currentFilter', () => {
    service.currentFilter.set({ priority: 'LOW' });
    service.currentFilter.update(() => null as any);
    expect(service.currentFilter()).toBeNull();
  });

  it('should handle update() with function that returns empty array for activeFilters', () => {
    service.activeFilters.set(['filter1', 'filter2', 'filter3']);
    service.activeFilters.update(() => []);
    expect(service.activeFilters()).toEqual([]);
  });

  it('should preserve reference equality for same signal value', () => {
    const val = { key: 'same' };
    service.currentFilter.set(val);
    expect(service.currentFilter()).toEqual({ key: 'same' });
  });
});
