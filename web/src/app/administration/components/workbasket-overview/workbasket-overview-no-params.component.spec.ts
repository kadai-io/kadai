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
import { WorkbasketOverviewComponent } from './workbasket-overview.component';
import { Actions, ofActionDispatched, provideStore, Store } from '@ngxs/store';
import { Observable, of } from 'rxjs';
import { WorkbasketState } from '../../../shared/store/workbasket-store/workbasket.state';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { SelectWorkbasket } from '../../../shared/store/workbasket-store/workbasket.actions';
import { workbasketReadStateMock } from '../../../shared/store/mock-data/mock-store';
import { provideHttpClient } from '@angular/common/http';
import { FilterState } from '../../../shared/store/filter-store/filter.state';

jest.mock('angular-svg-icon');

const mockActivatedRouteNoParams = {
  url: of([{ path: 'workbaskets' }])
};

describe('WorkbasketOverviewComponent No Params', () => {
  let fixture: ComponentFixture<WorkbasketOverviewComponent>;
  let component: WorkbasketOverviewComponent;
  let store: Store;
  let actions$: Observable<any>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [WorkbasketOverviewComponent],
      providers: [
        provideStore([WorkbasketState, FilterState]),
        {
          provide: ActivatedRoute,
          useValue: mockActivatedRouteNoParams
        },
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(WorkbasketOverviewComponent);
    component = fixture.debugElement.componentInstance;
    fixture.detectChanges();
    store = TestBed.inject(Store);
    actions$ = TestBed.inject(Actions);
    store.reset({
      ...store.snapshot(),
      workbasket: workbasketReadStateMock
    });
  }));

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should dispatch SelectWorkbasket action when route contains workbasket', async () => {
    let actionDispatched = false;
    actions$.pipe(ofActionDispatched(SelectWorkbasket)).subscribe(() => (actionDispatched = true));
    component.ngOnInit();
    expect(actionDispatched).toBe(true);
  });
});
