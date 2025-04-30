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

import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { WorkbasketListComponent } from './workbasket-list.component';
import { DebugElement } from '@angular/core';
import { Actions, ofActionDispatched, provideStore, Store } from '@ngxs/store';
import { Observable, of } from 'rxjs';
import { WorkbasketState } from '../../../shared/store/workbasket-store/workbasket.state';
import { WorkbasketService } from '../../../shared/services/workbasket/workbasket.service';
import { DeselectWorkbasket, SelectWorkbasket } from '../../../shared/store/workbasket-store/workbasket.actions';
import { Direction, Sorting, WorkbasketQuerySortParameter } from '../../../shared/models/sorting';
import { DomainService } from '../../../shared/services/domain/domain.service';
import { selectedWorkbasketMock } from '../../../shared/store/mock-data/mock-store';
import { WorkbasketQueryFilterParameter } from '../../../shared/models/workbasket-query-filter-parameter';
import { FilterState } from '../../../shared/store/filter-store/filter.state';
import { provideRouter } from '@angular/router';

const workbasketServiceMock: Partial<WorkbasketService> = {
  workbasketSavedTriggered: jest.fn().mockReturnValue(of(1)),
  getWorkBasketsSummary: jest.fn().mockReturnValue(of({})),
  getWorkBasket: jest.fn().mockReturnValue(of(selectedWorkbasketMock)),
  getWorkbasketActionToolbarExpansion: jest.fn().mockReturnValue(of(false)),
  getWorkBasketAccessItems: jest.fn().mockReturnValue(of({})),
  getWorkBasketsDistributionTargets: jest.fn().mockReturnValue(of({}))
};

const domainServiceSpy: Partial<DomainService> = {
  getSelectedDomainValue: jest.fn().mockReturnValue(of()),
  getSelectedDomain: jest.fn().mockReturnValue(of())
};

describe('WorkbasketListComponent', () => {
  let fixture: ComponentFixture<WorkbasketListComponent>;
  let debugElement: DebugElement;
  let component: WorkbasketListComponent;
  let store: Store;
  let actions$: Observable<any>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [WorkbasketListComponent],
      providers: [
        provideStore([WorkbasketState, FilterState]),
        provideRouter([]),
        {
          provide: WorkbasketService,
          useValue: workbasketServiceMock
        },
        {
          provide: DomainService,
          useValue: domainServiceSpy
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(WorkbasketListComponent);
    debugElement = fixture.debugElement;
    component = fixture.componentInstance;
    store = TestBed.inject(Store);
    actions$ = TestBed.inject(Actions);
    fixture.detectChanges();
  }));

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should dispatch SelectWorkbasket when selecting a workbasket', waitForAsync(() => {
    component.selectedId = undefined;
    fixture.detectChanges();
    let actionDispatched = false;
    actions$.pipe(ofActionDispatched(SelectWorkbasket)).subscribe(() => (actionDispatched = true));
    component.selectWorkbasket('WBI:000000000000000000000000000000000902');
    expect(actionDispatched).toBe(true);
  }));

  it('should dispatch DeselectWorkbasket when selecting a workbasket again', waitForAsync(() => {
    component.selectedId = '123';
    fixture.detectChanges();
    let actionDispatched = false;
    actions$.pipe(ofActionDispatched(DeselectWorkbasket)).subscribe(() => (actionDispatched = true));
    const mockId = '123';
    component.selectWorkbasket(mockId);
    expect(actionDispatched).toBe(true);
    expect(component.selectedId).toEqual(undefined); //because Deselect action sets selectedId to undefined
  }));

  it('should set sort value when performSorting is called', () => {
    const sort: Sorting<WorkbasketQuerySortParameter> = {
      'sort-by': WorkbasketQuerySortParameter.TYPE,
      order: Direction.ASC
    };
    component.performSorting(sort);
    expect(component.sort).toMatchObject(sort);
  });

  it('should set filter value without updating domain when performFilter is called', () => {
    component.filterBy = { domain: ['123'] };
    const filter: WorkbasketQueryFilterParameter = { 'name-like': ['workbasket'], domain: [''] };
    component.performFilter(filter);
    expect(component.filterBy).toMatchObject({ 'name-like': ['workbasket'], domain: ['123'] });
  });

  it('should change page value when change page function is called ', () => {
    const page = 2;
    component.changePage(page);
    expect(component.pageParameter.page).toBe(page);
  });

  it('should call performFilter when filter value from store is obtained', () => {
    const performFilter = jest.spyOn(component, 'performFilter');
    component.ngOnInit();
    expect(performFilter).toHaveBeenCalled();
  });
});
