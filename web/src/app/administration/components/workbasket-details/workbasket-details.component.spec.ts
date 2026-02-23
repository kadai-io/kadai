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
import { WorkbasketDetailsComponent } from './workbasket-details.component';
import { DebugElement } from '@angular/core';
import { Actions, ofActionDispatched, provideStore, Store } from '@ngxs/store';
import { firstValueFrom, Observable, of } from 'rxjs';
import { ACTION } from '../../../shared/models/action';
import { WorkbasketState } from '../../../shared/store/workbasket-store/workbasket.state';
import { DomainService } from '../../../shared/services/domain/domain.service';
import { WorkbasketService } from '../../../shared/services/workbasket/workbasket.service';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import {
  engineConfigurationMock,
  selectedWorkbasketMock,
  workbasketReadStateMock
} from '../../../shared/store/mock-data/mock-store';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import {
  CopyWorkbasket,
  CreateWorkbasket,
  DeselectWorkbasket,
  OnButtonPressed,
  SaveNewWorkbasket,
  SelectComponent,
  UpdateWorkbasket
} from '../../../shared/store/workbasket-store/workbasket.actions';
import { take } from 'rxjs/operators';
import { EngineConfigurationState } from '../../../shared/store/engine-configuration-store/engine-configuration.state';
import { provideRouter } from '@angular/router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { provideAngularSvgIcon } from 'angular-svg-icon';
import { ButtonAction } from '../../models/button-action';

const domainServiceSpy: Partial<DomainService> = {
  getSelectedDomain: vi.fn().mockReturnValue(of('A')),
  getSelectedDomainValue: vi.fn().mockReturnValue('A'),
  getDomains: vi.fn().mockReturnValue(of(['A']))
};

const workbasketServiceMock: Partial<WorkbasketService> = {
  getWorkBasket: vi.fn().mockReturnValue(of(selectedWorkbasketMock)),
  updateWorkbasket: vi.fn().mockReturnValue(of(selectedWorkbasketMock)),
  createWorkbasket: vi.fn().mockReturnValue(of(selectedWorkbasketMock)),
  getWorkBasketAccessItems: vi
    .fn()
    .mockReturnValue(of({ accessItems: [], _links: { self: { href: 'http://test/workbasketAccessItems' } } })),
  updateWorkBasketAccessItem: vi.fn().mockReturnValue(of({ accessItems: [] })),
  getWorkBasketsDistributionTargets: vi.fn().mockReturnValue(of({ distributionTargets: [], _links: {} })),
  getWorkBasketsSummary: vi.fn().mockReturnValue(of({ workbaskets: [], page: {}, _links: {} })),
  updateWorkBasketsDistributionTargets: vi.fn().mockReturnValue(of({ distributionTargets: [], _links: {} })),
  removeDistributionTarget: vi.fn().mockReturnValue(of({}))
};

export const workbasketReadState = {
  selectedWorkbasket: selectedWorkbasketMock,
  action: ACTION.READ
};

describe('WorkbasketDetailsComponent', () => {
  let fixture: ComponentFixture<WorkbasketDetailsComponent>;
  let debugElement: DebugElement;
  let component: WorkbasketDetailsComponent;
  let store: Store;
  let actions$: Observable<any>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WorkbasketDetailsComponent],
      providers: [
        provideStore([WorkbasketState, EngineConfigurationState]),
        provideRouter([]),
        provideNoopAnimations(),
        {
          provide: DomainService,
          useValue: domainServiceSpy
        },
        { provide: WorkbasketService, useValue: workbasketServiceMock },
        provideHttpClientTesting(),
        provideAngularSvgIcon()
      ]
    }).compileComponents();

    store = TestBed.inject(Store);
    store.reset({
      ...store.snapshot(),
      workbasket: workbasketReadStateMock,
      engineConfiguration: engineConfigurationMock
    });

    fixture = TestBed.createComponent(WorkbasketDetailsComponent);

    debugElement = fixture.debugElement;
    component = fixture.debugElement.componentInstance;
    actions$ = TestBed.inject(Actions);
    fixture.detectChanges();
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should render information component when workbasket details is opened', () => {
    fixture.detectChanges();
    const information = debugElement.nativeElement.querySelector('kadai-administration-workbasket-information');
    expect(information).toBeTruthy();
  });

  it('should render new workbasket when action is CREATE', async () => {
    await firstValueFrom(store.dispatch(new CreateWorkbasket()).pipe(take(1)));
    const state = await firstValueFrom(component.selectedWorkbasketAndComponentAndAction$.pipe(take(1)));

    expect(state.selectedWorkbasket.workbasketId).toBeUndefined();
  });

  it('should render copied workbasket when action is COPY', async () => {
    const workbasket = component.workbasket;
    await firstValueFrom(store.dispatch(new CopyWorkbasket(component.workbasket)).pipe(take(1)));
    const state = await firstValueFrom(component.selectedWorkbasketAndComponentAndAction$.pipe(take(1)));
    const workbasketCopy = state.selectedWorkbasket;

    expect(workbasketCopy.workbasketId).toBeUndefined();
    expect(workbasketCopy.key).toEqual(workbasket.key);
    expect(workbasketCopy.owner).toEqual(workbasket.owner);
  });

  it('should render workbasket when action is READ', () => {
    store.reset({
      ...store.snapshot(),
      workbasket: workbasketReadState
    });
    fixture.detectChanges();
    expect(component.workbasket).not.toBeUndefined();
    expect(component.workbasket).not.toBeNull();
    expect(component.workbasket).toEqual(selectedWorkbasketMock);
  });

  it('should select information tab when action is CREATE', async () => {
    component.selectComponent(1);
    await firstValueFrom(store.dispatch(new CreateWorkbasket()).pipe(take(1)));
    const tab = await firstValueFrom(component.selectedTab$.pipe(take(1)));

    expect(tab).toEqual(0);
  });

  it('should set areAllAccessItemsValid to false when isValid is false', () => {
    component.handleAccessItemsValidityChanged(false);
    expect(component.areAllAccessItemsValid).toBeFalsy();
  });

  it('should set areAllAccessItemsValid to true when isValid is true', () => {
    component.handleAccessItemsValidityChanged(true);
    expect(component.areAllAccessItemsValid).toBeTruthy();
  });

  it('should dispatch OnButtonPressed SAVE when onSubmit is called', () => {
    let dispatched = false;
    actions$.pipe(ofActionDispatched(OnButtonPressed)).subscribe((action) => {
      if (action.button === ButtonAction.SAVE) dispatched = true;
    });
    component.onSubmit();
    expect(dispatched).toBe(true);
  });

  it('should dispatch OnButtonPressed UNDO when onRestore is called', () => {
    let dispatched = false;
    actions$.pipe(ofActionDispatched(OnButtonPressed)).subscribe((action) => {
      if (action.button === ButtonAction.UNDO) dispatched = true;
    });
    component.onRestore();
    expect(dispatched).toBe(true);
  });

  it('should dispatch OnButtonPressed COPY when onCopy is called', () => {
    let copyDispatched = false;
    let copyWorkbasketDispatched = false;
    actions$.pipe(ofActionDispatched(OnButtonPressed)).subscribe((action) => {
      if (action.button === ButtonAction.COPY) copyDispatched = true;
    });
    actions$.pipe(ofActionDispatched(CopyWorkbasket)).subscribe(() => {
      copyWorkbasketDispatched = true;
    });
    component.onCopy();
    expect(copyDispatched).toBe(true);
    expect(copyWorkbasketDispatched).toBe(true);
  });

  it('should dispatch OnButtonPressed REMOVE_AS_DISTRIBUTION_TARGETS when onRemoveAsDistributionTarget is called', () => {
    let dispatched = false;
    actions$.pipe(ofActionDispatched(OnButtonPressed)).subscribe((action) => {
      if (action.button === ButtonAction.REMOVE_AS_DISTRIBUTION_TARGETS) dispatched = true;
    });
    component.onRemoveAsDistributionTarget();
    expect(dispatched).toBe(true);
  });

  it('should dispatch OnButtonPressed DELETE when onRemoveWorkbasket is called', () => {
    let dispatched = false;
    actions$.pipe(ofActionDispatched(OnButtonPressed)).subscribe((action) => {
      if (action.button === ButtonAction.DELETE) dispatched = true;
    });
    component.onRemoveWorkbasket();
    expect(dispatched).toBe(true);
  });

  it('should dispatch OnButtonPressed CLOSE and DeselectWorkbasket when onClose is called', () => {
    let closeDispatched = false;
    let deselectDispatched = false;
    actions$.pipe(ofActionDispatched(OnButtonPressed)).subscribe((action) => {
      if (action.button === ButtonAction.CLOSE) closeDispatched = true;
    });
    actions$.pipe(ofActionDispatched(DeselectWorkbasket)).subscribe(() => {
      deselectDispatched = true;
    });
    component.onClose();
    expect(closeDispatched).toBe(true);
    expect(deselectDispatched).toBe(true);
  });

  it('should dispatch SelectComponent when selectComponent is called', () => {
    let dispatched = false;
    actions$.pipe(ofActionDispatched(SelectComponent)).subscribe((index) => {
      dispatched = true;
    });
    component.selectComponent(2);
    expect(dispatched).toBe(true);
  });

  it('should complete destroy$ on ngOnDestroy', () => {
    const nextSpy = vi.spyOn(component.destroy$, 'next');
    const completeSpy = vi.spyOn(component.destroy$, 'complete');
    component.ngOnDestroy();
    expect(nextSpy).toHaveBeenCalled();
    expect(completeSpy).toHaveBeenCalled();
  });

  it('should update workbasket after UpdateWorkbasket action succeeds', async () => {
    await store
      .dispatch(new UpdateWorkbasket(selectedWorkbasketMock._links.self.href, selectedWorkbasketMock))
      .toPromise();
    expect(workbasketServiceMock.updateWorkbasket).toHaveBeenCalled();
  });

  it('should update workbasket after SaveNewWorkbasket action succeeds', async () => {
    await store.dispatch(new SaveNewWorkbasket(selectedWorkbasketMock)).toPromise();
    expect(workbasketServiceMock.createWorkbasket).toHaveBeenCalled();
  });
});
