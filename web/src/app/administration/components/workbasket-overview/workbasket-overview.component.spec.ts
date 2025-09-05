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
import { DebugElement } from '@angular/core';
import { Actions, ofActionCompleted, provideStore, Store } from '@ngxs/store';
import { Observable, of } from 'rxjs';
import { WorkbasketState } from '../../../shared/store/workbasket-store/workbasket.state';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { CreateWorkbasket } from '../../../shared/store/workbasket-store/workbasket.actions';
import { take } from 'rxjs/operators';
import { provideHttpClient } from '@angular/common/http';
import { FilterState } from '../../../shared/store/filter-store/filter.state';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

jest.mock('angular-svg-icon');

const mockActivatedRoute = {
  firstChild: {
    params: of({
      id: 'new-workbasket'
    })
  }
};

describe('WorkbasketOverviewComponent', () => {
  let fixture: ComponentFixture<WorkbasketOverviewComponent>;
  let debugElement: DebugElement;
  let component: WorkbasketOverviewComponent;
  let store: Store;
  let actions$: Observable<any>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [WorkbasketOverviewComponent],
      providers: [
        provideStore([WorkbasketState, FilterState]),
        provideNoopAnimations(),
        {
          provide: ActivatedRoute,
          useValue: mockActivatedRoute
        },
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(WorkbasketOverviewComponent);
    debugElement = fixture.debugElement;
    component = fixture.debugElement.componentInstance;
    store = TestBed.inject(Store);
    actions$ = TestBed.inject(Actions);
    fixture.detectChanges();
  }));

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should always displays workbasket-list', () => {
    expect(debugElement.nativeElement.querySelector('kadai-administration-workbasket-list')).toBeTruthy();
  });

  it('should display details when params id exists', waitForAsync((done) => {
    actions$.pipe(ofActionCompleted(CreateWorkbasket), take(1)).subscribe(() => {
      expect(component.routerParams.id).toMatch('new-workbasket');
      expect(component.showDetail).toBeTruthy();
      expect(debugElement.nativeElement.querySelector('kadai-administration-workbasket-details')).toBeTruthy();
      done();
    });
    component.ngOnInit();
  }));

  it('should display workbasket-details correctly', () => {
    component.showDetail = false;
    fixture.detectChanges();
    expect(debugElement.nativeElement.querySelector('kadai-administration-workbasket-details')).toBeNull();

    store.reset({
      ...store.snapshot(),
      workbasket: {
        selectedWorkbasket: {
          workbasketId: 'test-id',
          name: 'Test Workbasket'
        }
      }
    });

    component.showDetail = true;
    fixture.detectChanges();
    expect(debugElement.nativeElement.querySelector('kadai-administration-workbasket-details')).toBeTruthy();
  });
});
