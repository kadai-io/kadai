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

import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { WorkbasketDistributionTargetsComponent } from './workbasket-distribution-targets.component';
import { Observable, of } from 'rxjs';
import { WorkbasketService } from '../../../shared/services/workbasket/workbasket.service';
import { Actions, provideStore, Store } from '@ngxs/store';
import { WorkbasketState } from '../../../shared/store/workbasket-store/workbasket.state';
import { ActivatedRoute } from '@angular/router';
import { engineConfigurationMock, workbasketReadStateMock } from '../../../shared/store/mock-data/mock-store';
import { DomainService } from '../../../shared/services/domain/domain.service';
import { FilterState } from '../../../shared/store/filter-store/filter.state';

jest.mock('angular-svg-icon');

const activatedRouteMock = {
  firstChild: {
    params: of({ id: 'workbasket' })
  }
};

const domainServiceSpy: Partial<DomainService> = {
  getSelectedDomainValue: jest.fn().mockReturnValue(of(null)),
  getSelectedDomain: jest.fn().mockReturnValue(of('A')),
  getDomains: jest.fn().mockReturnValue(of(null))
};

const workbasketServiceSpy: Partial<WorkbasketService> = {
  getWorkBasketsSummary: jest.fn().mockReturnValue(of(null)),
  getWorkBasketsDistributionTargets: jest.fn().mockReturnValue(of(null))
};

describe('WorkbasketDistributionTargetsComponent', () => {
  let fixture: ComponentFixture<WorkbasketDistributionTargetsComponent>;
  let debugElement: DebugElement;
  let component: WorkbasketDistributionTargetsComponent;
  let store: Store;
  let actions$: Observable<any>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [WorkbasketDistributionTargetsComponent],
      providers: [
        provideStore([WorkbasketState, FilterState]),
        { provide: WorkbasketService, useValue: workbasketServiceSpy },
        { provide: ActivatedRoute, useValue: activatedRouteMock },
        { provide: DomainService, useValue: domainServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(WorkbasketDistributionTargetsComponent);
    debugElement = fixture.debugElement;
    component = fixture.componentInstance;
    store = TestBed.inject(Store);
    actions$ = TestBed.inject(Actions);
    store.reset({
      ...store.snapshot(),
      engineConfiguration: engineConfigurationMock,
      workbasket: workbasketReadStateMock
    });
    fixture.detectChanges();
  }));

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should display side-by-side view by default', () => {
    expect(component.sideBySide).toBe(true);
    expect(debugElement.nativeElement.querySelector('.distribution-targets-list__lists--side')).toBeTruthy();
  });

  it('should display single view when toggle view button is clicked', () => {
    const toggleViewButton = debugElement.nativeElement.querySelector('.distribution-targets-list__toggle-view-button');
    expect(toggleViewButton).toBeTruthy();
    toggleViewButton.click();
    fixture.detectChanges();
    expect(component.sideBySide).toBe(false);
    expect(debugElement.nativeElement.querySelector('.distribution-targets-list__lists--side')).toBeFalsy();
  });
});
