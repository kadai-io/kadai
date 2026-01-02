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

import { ComponentFixture, fakeAsync, flush, TestBed, waitForAsync } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { WorkbasketDistributionTargetsListComponent } from './workbasket-distribution-targets-list.component';
import { engineConfigurationMock, workbasketReadStateMock } from '../../../shared/store/mock-data/mock-store';
import { Side } from '../../models/workbasket-distribution-enums';
import { provideStore, Store } from '@ngxs/store';
import { WorkbasketState } from '../../../shared/store/workbasket-store/workbasket.state';
import { EMPTY, of } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { OrderBy } from '../../../shared/pipes/order-by.pipe';
import { FilterState } from '../../../shared/store/filter-store/filter.state';

jest.mock('angular-svg-icon');

describe('WorkbasketDistributionTargetsListComponent', () => {
  let fixture: ComponentFixture<WorkbasketDistributionTargetsListComponent>;
  let debugElement: DebugElement;
  let component: WorkbasketDistributionTargetsListComponent;
  let store: Store;

  const activatedRouteMock = {
    firstChild: {
      params: of({ id: 'workbasket' })
    }
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [WorkbasketDistributionTargetsListComponent],
      providers: [
        provideStore([WorkbasketState, FilterState]),
        provideHttpClient(),
        { provide: ActivatedRoute, useValue: activatedRouteMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(WorkbasketDistributionTargetsListComponent);
    debugElement = fixture.debugElement;
    component = fixture.componentInstance;
    component.distributionTargets = workbasketReadStateMock.paginatedWorkbasketsSummary.workbaskets;
    component.side = Side.AVAILABLE;
    component.transferDistributionTargetObservable = EMPTY;
    store = TestBed.inject(Store);
    store.reset({
      ...store.snapshot(),
      engineConfiguration: engineConfigurationMock,
      workbasket: workbasketReadStateMock
    });
  }));

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should set sideNumber to 0 when side is Side.AVAILABLE', () => {
    fixture.detectChanges();
    expect(component.side).toBe(Side.AVAILABLE);
  });

  it('should change toolbar state', () => {
    expect(component.toolbarState).toBe(false);
    component.changeToolbarState(true);
    expect(component.toolbarState).toBe(true);
  });

  it('should display filter when toolbarState is true', () => {
    component.component = 'availableDistributionTargets';
    component.toolbarState = true;
    fixture.detectChanges();
    expect(debugElement.nativeElement.querySelector('kadai-shared-workbasket-filter')).toBeTruthy();
  });

  it('should display all available workbaskets', fakeAsync(() => {
    // On the first cycle we render the items.
    fixture.detectChanges();
    flush();

    fixture.detectChanges();
    flush();

    const distributionTargetList = debugElement.nativeElement.getElementsByClassName(
      'workbasket-distribution-targets__workbaskets-item'
    );
    expect(distributionTargetList).toHaveLength(3);
  }));

  it('should call orderBy pipe', () => {
    const orderBySpy = jest.spyOn(OrderBy.prototype, 'transform');
    fixture.detectChanges();
    expect(orderBySpy).toHaveBeenCalledWith(component.distributionTargets, ['name']);
  });
});
