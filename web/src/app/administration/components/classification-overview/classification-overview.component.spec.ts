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

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { Actions, ofActionDispatched, provideStore, Store } from '@ngxs/store';
import { ClassificationState } from '../../../shared/store/classification-store/classification.state';
import { ClassificationOverviewComponent } from './classification-overview.component';
import { ActivatedRoute } from '@angular/router';
import { Observable, of, Subject } from 'rxjs';
import {
  CreateClassification,
  SelectClassification
} from '../../../shared/store/classification-store/classification.actions';
import { classificationStateMock, engineConfigurationMock } from '../../../shared/store/mock-data/mock-store';
import { provideHttpClient } from '@angular/common/http';
import { EngineConfigurationState } from '../../../shared/store/engine-configuration-store/engine-configuration.state';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('angular-svg-icon');

const routeParamsMock = { id: 'new-classification' };

describe('ClassificationOverviewComponent', () => {
  let fixture: ComponentFixture<ClassificationOverviewComponent>;
  let debugElement: DebugElement;
  let component: ClassificationOverviewComponent;
  let store: Store;
  let actions$: Observable<any>;
  let mockActivatedRoute: any;

  beforeEach(async () => {
    mockActivatedRoute = {
      firstChild: {
        params: of(routeParamsMock)
      }
    };

    await TestBed.configureTestingModule({
      imports: [ClassificationOverviewComponent],
      providers: [
        provideStore([ClassificationState, EngineConfigurationState]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
        {
          provide: ActivatedRoute,
          useValue: mockActivatedRoute
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ClassificationOverviewComponent);
    debugElement = fixture.debugElement;
    component = fixture.debugElement.componentInstance;
    store = TestBed.inject(Store);
    actions$ = TestBed.inject(Actions);
    store.reset({
      ...store.snapshot(),
      classification: classificationStateMock,
      engineConfiguration: engineConfigurationMock
    });
    fixture.detectChanges();
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should always display classification list', () => {
    expect(debugElement.nativeElement.querySelector('kadai-administration-classification-list')).toBeTruthy();
  });

  it('should display classification details when showDetail is true', () => {
    component.showDetail = true;
    fixture.detectChanges();
    expect(debugElement.nativeElement.querySelector('kadai-administration-classification-details')).toBeTruthy();
  });

  it('should show empty page with icon and text when showDetail is false', () => {
    component.showDetail = false;
    fixture.detectChanges();
    const emptyPage = fixture.debugElement.nativeElement.querySelector('.select-classification');
    expect(emptyPage.textContent).toBe('Select a classification');
    expect(debugElement.nativeElement.querySelector('svg-icon')).toBeTruthy();
    expect(debugElement.nativeElement.querySelector('kadai-administration-classification-details')).toBeFalsy();
  });

  it('should set routerParams property when firstChild of route exists', async () => {
    await fixture.whenStable();
    expect(component.routerParams).toEqual(routeParamsMock);
  });

  it('should dispatch SelectClassification action when routerParams id exists', async () => {
    mockActivatedRoute.firstChild.params = of(routeParamsMock);
    let isActionDispatched = false;
    actions$.pipe(ofActionDispatched(SelectClassification)).subscribe(() => (isActionDispatched = true));
    component.ngOnInit();
    expect(isActionDispatched).toBe(true);
  });

  it('should dispatch CreateClassification action when routerParams id contains new-classification', async () => {
    let isActionDispatched = false;
    actions$.pipe(ofActionDispatched(CreateClassification)).subscribe(() => (isActionDispatched = true));
    component.ngOnInit();
    expect(isActionDispatched).toBe(true);
  });

  it('should dispatch SelectClassification only when routerParams id exists but not new-classification', async () => {
    // arrange: set route firstChild params to id that does not contain 'new-classification'
    (mockActivatedRoute as any).firstChild = { params: of({ id: '101' }) };
    const dispatchSpy = vi.spyOn(store, 'dispatch');

    // act
    component.ngOnInit();

    // assert: a SelectClassification should have been dispatched, but CreateClassification should not
    expect(dispatchSpy).toHaveBeenCalled();
    const calls = (dispatchSpy as any).mock.calls.flat();
    const createdActionDispatched = calls.some((c: any) => c instanceof CreateClassification);
    expect(createdActionDispatched).toBe(false);
  });

  it('should not set routerParams or dispatch when firstChild of route does not exist', () => {
    const dispatchSpy = vi.spyOn(store, 'dispatch');
    // remove firstChild
    (mockActivatedRoute as any).firstChild = undefined;

    component.ngOnInit();

    expect(component.routerParams).toBeUndefined();
    expect(dispatchSpy).not.toHaveBeenCalled();
  });

  it('should react to selectedClassification$ changes and stop after destroy', () => {
    // Tear down existing subscriptions and replace the observable with a controllable Subject
    component.ngOnDestroy();
    const selectedSub = new Subject<any>();
    // override the observable used by the component
    (component as any).selectedClassification$ = selectedSub.asObservable();

    // re-initialize to subscribe to our subject
    component.ngOnInit();

    // initially emit null -> should set showDetail false
    selectedSub.next(null);
    expect(component.showDetail).toBe(false);

    // emit a selected classification -> should set showDetail true
    selectedSub.next({ classificationId: 'ID-1' });
    expect(component.showDetail).toBe(true);

    // destroy and emit again -> should NOT change showDetail
    component.ngOnDestroy();
    selectedSub.next(null);
    expect(component.showDetail).toBe(true);
  });
});
