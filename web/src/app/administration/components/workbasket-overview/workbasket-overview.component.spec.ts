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
import { WorkbasketOverviewComponent } from './workbasket-overview.component';
import { DebugElement } from '@angular/core';
import { Actions, ofActionCompleted, ofActionDispatched, provideStore, Store } from '@ngxs/store';
import { Observable, of } from 'rxjs';
import { WorkbasketState } from '../../../shared/store/workbasket-store/workbasket.state';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { CreateWorkbasket, SelectWorkbasket } from '../../../shared/store/workbasket-store/workbasket.actions';
import { take } from 'rxjs/operators';
import { provideHttpClient } from '@angular/common/http';
import { FilterState } from '../../../shared/store/filter-store/filter.state';
import { EngineConfigurationState } from '../../../shared/store/engine-configuration-store/engine-configuration.state';
import { engineConfigurationMock } from '../../../shared/store/mock-data/mock-store';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { beforeEach, describe, expect, it } from 'vitest';
import { provideAngularSvgIcon } from 'angular-svg-icon';

const mockActivatedRoute = {
  url: of([{ path: 'foobar' }]),
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

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WorkbasketOverviewComponent],
      providers: [
        provideStore([WorkbasketState, FilterState, EngineConfigurationState]),
        provideNoopAnimations(),
        {
          provide: ActivatedRoute,
          useValue: mockActivatedRoute
        },
        provideHttpClient(),
        provideHttpClientTesting(),
        provideAngularSvgIcon()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(WorkbasketOverviewComponent);
    debugElement = fixture.debugElement;
    component = fixture.debugElement.componentInstance;
    store = TestBed.inject(Store);
    store.reset({
      ...store.snapshot(),
      engineConfiguration: engineConfigurationMock
    });
    actions$ = TestBed.inject(Actions);
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should always displays workbasket-list', () => {
    expect(debugElement.nativeElement.querySelector('kadai-administration-workbasket-list')).toBeTruthy();
  });

  it('should display details when params id exists', async () => {
    actions$.pipe(ofActionCompleted(CreateWorkbasket), take(1)).subscribe(() => {
      expect(component.routerParams.id).toMatch('new-workbasket');
      expect(component.showDetail).toBeTruthy();
      expect(debugElement.nativeElement.querySelector('kadai-administration-workbasket-details')).toBeTruthy();
    });
    component.ngOnInit();
  });

  it('should not display workbasket-details', () => {
    component.showDetail = false;
    fixture.detectChanges();
    expect(debugElement.nativeElement.querySelector('kadai-administration-workbasket-details')).toBeNull();
  });

  it('should display workbasket-details', () => {
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

  it('should set expanded to false when toggleWidth is called and offsetWidth is not 250', () => {
    component.toggleWidth();
    expect(component.expanded).toBe(false);
  });

  it('should set expanded to true when toggleWidth is called and offsetWidth is 250', () => {
    Object.defineProperty(component.workbasketList.nativeElement, 'offsetWidth', { value: 250, configurable: true });
    component.toggleWidth();
    expect(component.expanded).toBe(true);
  });

  it('should trigger toggleWidth when toggle button is clicked', () => {
    const toggleButton: HTMLElement = debugElement.nativeElement.querySelector(
      '.workbasket-overview__toggle-view-button'
    );
    expect(toggleButton).toBeTruthy();
    toggleButton.click();
    fixture.detectChanges();
    expect(component.expanded).toBe(false);
  });

  it('should trigger toggleWidth a second time and restore expanded to true when offsetWidth is 250', () => {
    Object.defineProperty(component.workbasketList.nativeElement, 'offsetWidth', { value: 250, configurable: true });
    const toggleButton: HTMLElement = debugElement.nativeElement.querySelector(
      '.workbasket-overview__toggle-view-button'
    );
    toggleButton.click();
    fixture.detectChanges();
    expect(component.expanded).toBe(true);
  });

  it('should render chevron_left icon when expanded is true', () => {
    component.expanded = true;
    fixture.detectChanges();
    const icons = debugElement.nativeElement.querySelectorAll('mat-icon');
    const iconTexts = Array.from(icons).map((el: any) => el.textContent.trim());
    expect(iconTexts).toContain('chevron_left');
  });

  it('should render chevron_right icon when expanded is false', () => {
    component.expanded = false;
    fixture.detectChanges();
    const icons = debugElement.nativeElement.querySelectorAll('mat-icon');
    const iconTexts = Array.from(icons).map((el: any) => el.textContent.trim());
    expect(iconTexts).toContain('chevron_right');
  });

  it('should display empty page with "Select a workbasket" when showDetail is false', () => {
    component.showDetail = false;
    fixture.detectChanges();
    const emptyPage = debugElement.nativeElement.querySelector('.workbasket-overview__empty-page');
    expect(emptyPage).toBeTruthy();
    expect(emptyPage.textContent).toContain('Select a workbasket');
  });

  it('should not display empty page when showDetail is true', () => {
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
    const emptyPage = debugElement.nativeElement.querySelector('.workbasket-overview__empty-page');
    expect(emptyPage).toBeNull();
  });

  it('should subscribe to selectedWorkbasket$ when url path is workbaskets', () => {
    const activatedRoute = TestBed.inject(ActivatedRoute) as any;
    activatedRoute.url = of([{ path: 'workbaskets' }]);
    store.reset({
      ...store.snapshot(),
      workbasket: { selectedWorkbasket: { workbasketId: 'WBI:test-123' } }
    });
    component.ngOnInit();
    expect(component).toBeTruthy();
  });
});

const mockActivatedRouteAlternativeId = {
  url: of([{ path: 'foobar' }]),
  firstChild: {
    params: of({ id: '101' })
  }
};

describe('WorkbasketOverviewComponent Alternative Params ID', () => {
  let fixture: ComponentFixture<WorkbasketOverviewComponent>;
  let component: WorkbasketOverviewComponent;
  let store: Store;
  let actions$: Observable<any>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WorkbasketOverviewComponent],
      providers: [
        provideStore([WorkbasketState, FilterState]),
        { provide: ActivatedRoute, useValue: mockActivatedRouteAlternativeId },
        provideHttpClient(),
        provideHttpClientTesting(),
        provideAngularSvgIcon()
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(WorkbasketOverviewComponent);
    component = fixture.debugElement.componentInstance;
    store = TestBed.inject(Store);
    actions$ = TestBed.inject(Actions);
    fixture.detectChanges();
  });

  it('should dispatch SelectWorkbasket action when params contain an existing workbasket ID', async () => {
    expect(component.routerParams.id).toBeTruthy();
    let actionDispatched = false;
    actions$.pipe(ofActionDispatched(SelectWorkbasket)).subscribe(() => (actionDispatched = true));
    component.ngOnInit();
    expect(actionDispatched).toBe(true);
  });
});
