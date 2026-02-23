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
import { WorkplaceState } from './workplace.state';
import { CalculateNumberOfCards, SetFilterExpansion } from './workplace.actions';

describe('WorkplaceState', () => {
  let store: Store;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NgxsModule.forRoot([WorkplaceState])]
    }).compileComponents();

    store = TestBed.inject(Store);
  });

  it('should have state name WorkplaceState', () => {
    const state = store.snapshot().WorkplaceState;
    expect(state).toBeDefined();
  });

  it('should initialize isFilterExpanded to false via ngxsOnInit', () => {
    const state = store.snapshot().WorkplaceState;
    expect(state.isFilterExpanded).toBe(false);
  });

  it('should initialize cards to a value >= 1 via ngxsOnInit', () => {
    const state = store.snapshot().WorkplaceState;
    expect(state.cards).toBeGreaterThanOrEqual(1);
  });

  describe('SetFilterExpansion', () => {
    it('should set isFilterExpanded to true when explicit true is dispatched', async () => {
      await store.dispatch(new SetFilterExpansion(true)).toPromise();
      const state = store.snapshot().WorkplaceState;
      expect(state.isFilterExpanded).toBe(true);
    });

    it('should set isFilterExpanded to false when explicit false is dispatched', async () => {
      await store.dispatch(new SetFilterExpansion(true)).toPromise();
      await store.dispatch(new SetFilterExpansion(false)).toPromise();
      const state = store.snapshot().WorkplaceState;
      expect(state.isFilterExpanded).toBe(false);
    });

    it('should toggle isFilterExpanded when no argument is passed', async () => {
      const initialState = store.snapshot().WorkplaceState;
      const initialExpanded = initialState.isFilterExpanded;

      await store.dispatch(new SetFilterExpansion()).toPromise();
      const afterFirstToggle = store.snapshot().WorkplaceState;
      expect(afterFirstToggle.isFilterExpanded).toBe(!initialExpanded);

      await store.dispatch(new SetFilterExpansion()).toPromise();
      const afterSecondToggle = store.snapshot().WorkplaceState;
      expect(afterSecondToggle.isFilterExpanded).toBe(initialExpanded);
    });

    it('should trigger CalculateNumberOfCards after SetFilterExpansion', async () => {
      await store.dispatch(new SetFilterExpansion(true)).toPromise();
      const state = store.snapshot().WorkplaceState;
      expect(state.cards).toBeGreaterThanOrEqual(1);
    });
  });

  describe('CalculateNumberOfCards', () => {
    it('should set cards to at least 1', async () => {
      await store.dispatch(new CalculateNumberOfCards()).toPromise();
      const state = store.snapshot().WorkplaceState;
      expect(state.cards).toBeGreaterThanOrEqual(1);
    });

    it('should use different toolbarHeight based on isFilterExpanded', async () => {
      await store.dispatch(new SetFilterExpansion(false)).toPromise();
      const stateCollapsed = store.snapshot().WorkplaceState;
      const cardsCollapsed = stateCollapsed.cards;

      await store.dispatch(new SetFilterExpansion(true)).toPromise();
      const stateExpanded = store.snapshot().WorkplaceState;
      const cardsExpanded = stateExpanded.cards;

      // When expanded, toolbarHeight is 308 vs 192 when collapsed, so more space is occupied
      // meaning fewer cards when expanded (or equal if window is very small)
      expect(cardsExpanded).toBeLessThanOrEqual(cardsCollapsed);
    });
  });
});
