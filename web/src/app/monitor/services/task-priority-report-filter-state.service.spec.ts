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
 */

import { TestBed } from '@angular/core/testing';
import { Store } from '@ngxs/store';
import { BehaviorSubject } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import {
  SETTING_MEMBER_FILTER,
  TaskPriorityReportFilterStateService
} from './task-priority-report-filter-state.service';
import { SettingsSelectors } from '../../shared/store/settings-store/settings.selectors';

describe('TaskPriorityReportFilterStateService', () => {
  let service: TaskPriorityReportFilterStateService;
  let settingsSubject$: BehaviorSubject<Record<string, unknown> | undefined>;

  const validFilterConfig = {
    'State READY': { state: ['READY'] },
    'State CLAIMED': { state: ['CLAIMED'], priority: ['HIGH'] }
  };

  beforeEach(() => {
    settingsSubject$ = new BehaviorSubject<Record<string, unknown> | undefined>(undefined);

    const mockStore = {
      select: vi.fn().mockImplementation((selector) => {
        if (selector === SettingsSelectors.getSettings) {
          return settingsSubject$.asObservable();
        }
        return new BehaviorSubject(undefined).asObservable();
      })
    };

    TestBed.configureTestingModule({
      providers: [TaskPriorityReportFilterStateService, { provide: Store, useValue: mockStore }]
    });

    service = TestBed.inject(TaskPriorityReportFilterStateService);
  });

  it('should be created with initial default state', () => {
    expect(service).toBeTruthy();
    expect(service.activeFilters()).toEqual([]);
    expect(service.workbasketKey()).toBeUndefined();
    expect(service.filterKeys()).toEqual([]);
    expect(service.filtersAreSpecified()).toBe(false);
    expect(service.activeQuery()).toEqual({});
  });

  describe('parsedFilterConfig & Filter Specs Validation', () => {
    it('should return isValid: false when settings are undefined or filter setting is missing', () => {
      settingsSubject$.next(undefined);
      expect(service.parsedFilterConfig()).toEqual({ config: {}, isValid: false });
      expect(service.filtersAreSpecified()).toBe(false);

      settingsSubject$.next({});
      expect(service.parsedFilterConfig()).toEqual({ config: {}, isValid: false });
      expect(service.filtersAreSpecified()).toBe(false);
    });

    it('should return isValid: false when filter setting is empty or whitespace-only string', () => {
      settingsSubject$.next({ [SETTING_MEMBER_FILTER]: '   ' });
      expect(service.parsedFilterConfig()).toEqual({ config: {}, isValid: false });
      expect(service.filtersAreSpecified()).toBe(false);
    });

    it('should handle invalid JSON in filter setting gracefully without crashing', () => {
      settingsSubject$.next({ [SETTING_MEMBER_FILTER]: '{ invalid json }' });
      expect(service.parsedFilterConfig()).toEqual({ config: {}, isValid: false });
      expect(service.filtersAreSpecified()).toBe(false);
    });

    it('should parse valid JSON filter config and update filterKeys and filtersAreSpecified', () => {
      settingsSubject$.next({
        [SETTING_MEMBER_FILTER]: JSON.stringify(validFilterConfig)
      });

      expect(service.parsedFilterConfig()).toEqual({
        config: validFilterConfig,
        isValid: true
      });
      expect(service.filterKeys()).toEqual(['State READY', 'State CLAIMED']);
      expect(service.filtersAreSpecified()).toBe(true);
    });

    it('should return filtersAreSpecified as false when parsed JSON is an empty object', () => {
      settingsSubject$.next({
        [SETTING_MEMBER_FILTER]: JSON.stringify({})
      });

      expect(service.parsedFilterConfig()).toEqual({ config: {}, isValid: true });
      expect(service.filterKeys()).toEqual([]);
      expect(service.filtersAreSpecified()).toBe(false);
    });
  });

  describe('toggleFilter() & activeFilters', () => {
    it('should add a filter key when enabled', () => {
      service.toggleFilter('State READY', true);
      expect(service.activeFilters()).toEqual(['State READY']);
    });

    it('should not duplicate filter keys when enabling an already active filter', () => {
      service.toggleFilter('State READY', true);
      service.toggleFilter('State READY', true);
      expect(service.activeFilters()).toEqual(['State READY']);
    });

    it('should remove a filter key when disabled', () => {
      service.activeFilters.set(['State READY', 'State CLAIMED']);

      service.toggleFilter('State READY', false);
      expect(service.activeFilters()).toEqual(['State CLAIMED']);
    });

    it('should handle disabling a filter that is not currently active gracefully', () => {
      service.activeFilters.set(['State READY']);

      service.toggleFilter('NonExistingFilter', false);
      expect(service.activeFilters()).toEqual(['State READY']);
    });
  });

  describe('activeQuery Computation', () => {
    beforeEach(() => {
      settingsSubject$.next({
        [SETTING_MEMBER_FILTER]: JSON.stringify(validFilterConfig)
      });
    });

    it('should return empty activeQuery when no filters are active', () => {
      expect(service.activeQuery()).toEqual({});
    });

    it('should compute query for a single active filter', () => {
      service.toggleFilter('State READY', true);

      expect(service.activeQuery()).toEqual({
        state: ['READY']
      });
    });

    it('should merge multiple active filters into combined query arrays', () => {
      service.toggleFilter('State READY', true);
      service.toggleFilter('State CLAIMED', true);

      expect(service.activeQuery()).toEqual({
        state: ['READY', 'CLAIMED'],
        priority: ['HIGH']
      });
    });

    it('should ignore active filter keys that do not exist in filter config', () => {
      service.toggleFilter('UnknownFilter', true);

      expect(service.activeQuery()).toEqual({});
    });

    it('should deduplicate values when multiple active filters contain overlapping query values', () => {
      const overlappingConfig = {
        'Filter A': { state: ['READY', 'CLAIMED'] },
        'Filter B': { state: ['READY', 'COMPLETED'] }
      };

      TestBed.tick();

      settingsSubject$.next({
        [SETTING_MEMBER_FILTER]: JSON.stringify(overlappingConfig)
      });

      service.toggleFilter('Filter A', true);
      service.toggleFilter('Filter B', true);

      expect(service.activeQuery()).toEqual({
        state: ['READY', 'CLAIMED', 'COMPLETED']
      });
    });
  });

  describe('workbasketKey WritableSignal', () => {
    it('should allow setting and updating workbasketKey', () => {
      expect(service.workbasketKey()).toBeUndefined();

      service.workbasketKey.set('WBK_123');
      expect(service.workbasketKey()).toBe('WBK_123');

      service.workbasketKey.set(undefined);
      expect(service.workbasketKey()).toBeUndefined();
    });
  });
});
