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

import { TestBed } from '@angular/core/testing';
import { NgxsModule, Store } from '@ngxs/store';
import { beforeEach, describe, expect, it } from 'vitest';
import { FilterState } from './filter.state';
import { ClearTaskFilter, ClearWorkbasketFilter, SetTaskFilter, SetWorkbasketFilter } from './filter.actions';

describe('FilterState', () => {
  let store: Store;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NgxsModule.forRoot([FilterState])]
    }).compileComponents();

    store = TestBed.inject(Store);
  });

  it('should initialize state with ngxsOnInit', () => {
    const state = store.snapshot().FilterState;
    expect(state).toBeDefined();
    expect(state.tasks).toBeDefined();
    expect(state.workbasketList).toBeDefined();
    expect(state.availableDistributionTargets).toBeDefined();
    expect(state.selectedDistributionTargets).toBeDefined();
  });

  it('should initialize tasks filter with empty arrays', () => {
    const state = store.snapshot().FilterState;
    expect(state.tasks['name-like']).toEqual([]);
    expect(state.tasks['owner-like']).toEqual([]);
    expect(state.tasks.state).toEqual([]);
  });

  it('should initialize workbasketList filter with empty arrays', () => {
    const state = store.snapshot().FilterState;
    expect(state.workbasketList['name-like']).toEqual([]);
    expect(state.workbasketList['key-like']).toEqual([]);
  });

  describe('SetWorkbasketFilter', () => {
    it('should update workbasket filter for the given component', () => {
      store.dispatch(new SetWorkbasketFilter({ 'name-like': ['test'] }, 'workbasketList'));

      const state = store.snapshot().FilterState;
      expect(state.workbasketList['name-like']).toEqual(['test']);
    });

    it('should keep existing filter values for unspecified keys', () => {
      store.dispatch(new SetWorkbasketFilter({ 'name-like': ['test'] }, 'workbasketList'));

      const state = store.snapshot().FilterState;
      expect(state.workbasketList['key-like']).toEqual([]);
    });
  });

  describe('ClearWorkbasketFilter', () => {
    it('should clear workbasket filter for the given component', () => {
      store.dispatch(new SetWorkbasketFilter({ 'name-like': ['test'] }, 'workbasketList'));
      store.dispatch(new ClearWorkbasketFilter('workbasketList'));

      const state = store.snapshot().FilterState;
      expect(state.workbasketList['name-like']).toEqual([]);
    });
  });

  describe('SetTaskFilter', () => {
    it('should update task filter', () => {
      store.dispatch(new SetTaskFilter({ 'name-like': ['test'] }));

      const state = store.snapshot().FilterState;
      expect(state.tasks['name-like']).toEqual(['test']);
    });

    it('should set wildcard search fields when wildcard-search-value is provided', () => {
      store.dispatch(new SetTaskFilter({ 'wildcard-search-value': ['mySearch'] }));

      const state = store.snapshot().FilterState;
      expect(state.tasks['wildcard-search-fields'].length).toBeGreaterThan(0);
      expect(state.tasks['wildcard-search-fields']).toContain('NAME');
      expect(state.tasks['wildcard-search-fields']).toContain('DESCRIPTION');
      expect(state.tasks['wildcard-search-fields']).toContain('CUSTOM_1');
      expect(state.tasks['wildcard-search-fields']).toContain('CUSTOM_16');
    });

    it('should not set wildcard search fields when wildcard-search-value is empty', () => {
      store.dispatch(new SetTaskFilter({ 'wildcard-search-value': [''] }));

      const state = store.snapshot().FilterState;
      expect(state.tasks['wildcard-search-fields']).toEqual([]);
    });
  });

  describe('ClearTaskFilter', () => {
    it('should clear task filter', () => {
      store.dispatch(new SetTaskFilter({ 'name-like': ['test'] }));
      store.dispatch(new ClearTaskFilter());

      const state = store.snapshot().FilterState;
      expect(state.tasks['name-like']).toEqual([]);
      expect(state.tasks['owner-like']).toEqual([]);
    });
  });

  describe('initWildcardFields', () => {
    it('should return array with NAME, DESCRIPTION, and CUSTOM_1 through CUSTOM_16', () => {
      const filterState = TestBed.inject(FilterState);
      const fields = filterState.initWildcardFields();

      expect(fields).toContain('NAME');
      expect(fields).toContain('DESCRIPTION');
      expect(fields).toContain('CUSTOM_1');
      expect(fields).toContain('CUSTOM_16');
      expect(fields.length).toBe(18); // NAME + DESCRIPTION + 16 CUSTOM fields
    });
  });
});
