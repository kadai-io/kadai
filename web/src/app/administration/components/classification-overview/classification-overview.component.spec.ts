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
import { DebugElement, Component } from '@angular/core';
import { Actions, ofActionDispatched, provideStore, Store } from '@ngxs/store';
import { DomainState } from 'app/shared/store/domain-store/domain.state';
import { ClassificationState } from 'app/shared/store/classification-store/classification.state';
import { ClassificationOverviewComponent } from './classification-overview.component';
import { ActivatedRoute, Router, provideRouter } from '@angular/router';
import { Observable } from 'rxjs';
import {
  CreateClassification,
  DeselectClassification,
  SelectClassification
} from 'app/shared/store/classification-store/classification.actions';
import { classificationStateMock, engineConfigurationMock } from 'app/shared/store/mock-data/mock-store';
import { EngineConfigurationState } from 'app/shared/store/engine-configuration-store/engine-configuration.state';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { RuntimeConfigService } from 'app/shared/services/runtime-config/runtime-config.service';
import { SvgIconRegistryService } from 'app/shared/components/svg-icon/svg-icon-registry.service';

const routeParamsMock = { id: 'new-classification' };

@Component({ template: '' })
class BlankRouteComponent {}

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
        snapshot: { params: routeParamsMock }
      }
    };

    await TestBed.configureTestingModule({
      imports: [ClassificationOverviewComponent],
      providers: [
        provideStore([ClassificationState, EngineConfigurationState, DomainState]),
        provideRouter([{ path: '**', component: BlankRouteComponent }]),
        { provide: RuntimeConfigService, useValue: { kadaiRestUrl: 'http://localhost:8080/kadai' } },
        { provide: SvgIconRegistryService, useValue: { loadSvg: () => of(null) } },

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
    store.reset({ ...store.snapshot(), domain: { domains: ['DOMAIN_A', 'DOMAIN_B'], selectedDomain: 'DOMAIN_A' } });
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
    component.showDetail.set(true);
    fixture.detectChanges();
    expect(debugElement.nativeElement.querySelector('kadai-administration-classification-details')).toBeTruthy();
  });

  it('should show empty page with icon and text when showDetail is false', () => {
    component.showDetail.set(false);
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

  it('should dispatch SelectClassification when the route carries a classification id', () => {
    mockActivatedRoute.firstChild.snapshot = { params: { id: 'CLI:01' } };
    store.reset({ ...store.snapshot(), classification: classificationStateMock });
    let isActionDispatched = false;
    actions$.pipe(ofActionDispatched(SelectClassification)).subscribe(() => (isActionDispatched = true));

    component.ngOnInit();

    expect(isActionDispatched).toBe(true);
  });

  it('should dispatch CreateClassification when the route carries new-classification', () => {
    mockActivatedRoute.firstChild.snapshot = { params: { id: 'new-classification' } };
    store.reset({ ...store.snapshot(), classification: classificationStateMock });
    let isActionDispatched = false;
    actions$.pipe(ofActionDispatched(CreateClassification)).subscribe(() => (isActionDispatched = true));

    component.ngOnInit();

    expect(isActionDispatched).toBe(true);
  });

  it('should not select a classification for the new-classification url', () => {
    mockActivatedRoute.firstChild.snapshot = { params: { id: 'new-classification' } };
    store.reset({ ...store.snapshot(), classification: classificationStateMock });
    let isActionDispatched = false;
    actions$.pipe(ofActionDispatched(SelectClassification)).subscribe(() => (isActionDispatched = true));

    component.ngOnInit();

    expect(isActionDispatched).toBe(false);
  });

  it('should read the route again after each navigation', async () => {
    mockActivatedRoute.firstChild.snapshot = { params: { id: 'CLI:99' } };
    store.reset({ ...store.snapshot(), classification: classificationStateMock });
    let isActionDispatched = false;
    actions$.pipe(ofActionDispatched(SelectClassification)).subscribe(() => (isActionDispatched = true));

    await TestBed.inject(Router).navigateByUrl('/kadai/administration/classifications');

    expect(isActionDispatched).toBe(true);
  });

  it('should dispatch DeselectClassification when the route carries no id', () => {
    mockActivatedRoute.firstChild.snapshot = { params: {} };
    let isActionDispatched = false;
    actions$.pipe(ofActionDispatched(DeselectClassification)).subscribe(() => (isActionDispatched = true));

    component.ngOnInit();

    expect(isActionDispatched).toBe(true);
  });

  it('should derive showDetail from store state only, not directly from the route id', () => {
    store.reset({
      ...store.snapshot(),
      classification: { ...classificationStateMock, selectedClassification: undefined }
    });
    mockActivatedRoute.firstChild.snapshot = { params: { id: 'ID01' } };
    const setSpy = vi.spyOn(component.showDetail, 'set');

    component.ngOnInit();

    expect(setSpy).toHaveBeenNthCalledWith(1, false);
  });
});
