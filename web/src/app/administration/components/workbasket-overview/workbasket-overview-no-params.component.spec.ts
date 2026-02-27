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
import { provideStore, Store } from '@ngxs/store';
import { of } from 'rxjs';
import { WorkbasketState } from '../../../shared/store/workbasket-store/workbasket.state';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { SelectWorkbasket } from '../../../shared/store/workbasket-store/workbasket.actions';
import {
  engineConfigurationMock,
  selectedWorkbasketMock,
  workbasketReadStateMock
} from '../../../shared/store/mock-data/mock-store';
import { provideHttpClient } from '@angular/common/http';
import { FilterState } from '../../../shared/store/filter-store/filter.state';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { EngineConfigurationState } from '../../../shared/store/engine-configuration-store/engine-configuration.state';
import { provideAngularSvgIcon } from 'angular-svg-icon';

const mockActivatedRouteNoParams = {
  url: of([{ path: 'workbaskets' }]),
  queryParams: of({})
};

describe('WorkbasketOverviewComponent No Params', () => {
  let fixture: ComponentFixture<WorkbasketOverviewComponent>;
  let component: WorkbasketOverviewComponent;
  let store: Store;
  let keepAliveSubscription: { unsubscribe(): void };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WorkbasketOverviewComponent],
      providers: [
        provideStore([WorkbasketState, FilterState, EngineConfigurationState]),
        {
          provide: ActivatedRoute,
          useValue: mockActivatedRouteNoParams
        },
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
        provideAngularSvgIcon()
      ]
    }).compileComponents();
    store = TestBed.inject(Store);

    keepAliveSubscription = store.subscribe(() => {});

    store.reset({
      ...store.snapshot(),
      workbasket: workbasketReadStateMock,
      engineConfiguration: engineConfigurationMock
    });

    fixture = TestBed.createComponent(WorkbasketOverviewComponent);
    component = fixture.debugElement.componentInstance;
  });

  afterEach(() => {
    keepAliveSubscription?.unsubscribe();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should dispatch SelectWorkbasket action when route contains workbasket', async () => {
    const dispatchSpy = vi.spyOn(store, 'dispatch');
    fixture.detectChanges();
    await vi.waitFor(() =>
      expect(dispatchSpy).toHaveBeenCalledWith(new SelectWorkbasket(selectedWorkbasketMock.workbasketId))
    );
  });
});
