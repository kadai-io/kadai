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

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { NgxsModule, Store } from '@ngxs/store';
import { WorkbasketFilterComponent } from './workbasket-filter.component';
import { FilterState } from '../../store/filter-store/filter.state';
import { SetWorkbasketFilter } from '../../store/filter-store/filter.actions';
import { WorkbasketType } from '../../models/workbasket-type';
import { WorkbasketQueryFilterParameter } from '../../models/workbasket-query-filter-parameter';
import { provideAnimations } from '@angular/platform-browser/animations';

describe('WorkbasketFilterComponent', () => {
  let component: WorkbasketFilterComponent;
  let fixture: ComponentFixture<WorkbasketFilterComponent>;
  let store: Store;

  const emptyFilter: WorkbasketQueryFilterParameter = {
    'description-like': [],
    'key-like': [],
    'name-like': [],
    'owner-like': [],
    type: []
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WorkbasketFilterComponent, NgxsModule.forRoot([FilterState])],
      providers: [provideAnimations()]
    }).compileComponents();

    store = TestBed.inject(Store);
    fixture = TestBed.createComponent(WorkbasketFilterComponent);
    component = fixture.componentInstance;
    component.component = 'workbasketList';
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should have allTypes map defined', () => {
    expect(component.allTypes).toBeDefined();
    expect(component.allTypes.size).toBeGreaterThan(0);
  });

  it('setFilter() should set filter properties by spreading arrays', () => {
    const inputFilter: WorkbasketQueryFilterParameter = {
      'description-like': ['desc1'],
      'key-like': ['key1'],
      'name-like': ['name1'],
      'owner-like': ['owner1'],
      type: [WorkbasketType.PERSONAL]
    };

    component.setFilter(inputFilter);

    expect(component.filter['description-like']).toEqual(['desc1']);
    expect(component.filter['key-like']).toEqual(['key1']);
    expect(component.filter['name-like']).toEqual(['name1']);
    expect(component.filter['owner-like']).toEqual(['owner1']);
    expect(component.filter.type).toEqual([WorkbasketType.PERSONAL]);
  });

  it('setFilter() should create independent copies of arrays (spreading)', () => {
    const inputFilter: WorkbasketQueryFilterParameter = {
      'description-like': ['desc1'],
      'key-like': [],
      'name-like': [],
      'owner-like': [],
      type: []
    };

    component.setFilter(inputFilter);

    inputFilter['description-like'].push('desc2');
    expect(component.filter['description-like']).toEqual(['desc1']);
  });

  it('selectType() should set filter.type to [type] for non-ALL type', () => {
    component.setFilter(emptyFilter);

    component.selectType(WorkbasketType.PERSONAL);
    expect(component.filter.type).toEqual([WorkbasketType.PERSONAL]);
  });

  it('selectType() should set filter.type to [] when WorkbasketType.ALL is passed', () => {
    component.setFilter({ ...emptyFilter, type: [WorkbasketType.PERSONAL] });

    component.selectType(WorkbasketType.ALL);
    expect(component.filter.type).toEqual([]);
  });

  it('selectType() should set filter.type to [GROUP] for WorkbasketType.GROUP', () => {
    component.setFilter(emptyFilter);

    component.selectType(WorkbasketType.GROUP);
    expect(component.filter.type).toEqual([WorkbasketType.GROUP]);
  });

  it('search() should dispatch SetWorkbasketFilter with current filter and component', () => {
    const dispatchSpy = vi.spyOn(store, 'dispatch');
    component.setFilter(emptyFilter);

    component.search();

    expect(dispatchSpy).toHaveBeenCalledWith(new SetWorkbasketFilter(component.filter, component.component));
  });

  it('clear() should dispatch ClearWorkbasketFilter for the current component', () => {
    const dispatchSpy = vi.spyOn(store, 'dispatch');

    component.clear();

    expect(dispatchSpy).toHaveBeenCalled();
  });

  it('ngOnDestroy() should complete destroy$', () => {
    const completeSpy = vi.spyOn(component.destroy$, 'complete');
    const nextSpy = vi.spyOn(component.destroy$, 'next');

    component.ngOnDestroy();

    expect(nextSpy).toHaveBeenCalled();
    expect(completeSpy).toHaveBeenCalled();
  });
});
