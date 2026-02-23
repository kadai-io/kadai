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
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { provideAngularSvgIcon } from 'angular-svg-icon';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import { Side } from '../../models/workbasket-distribution-enums';
import {
  FetchAvailableDistributionTargets,
  FetchWorkbasketDistributionTargets,
  UpdateWorkbasketDistributionTargets
} from '../../../shared/store/workbasket-store/workbasket.actions';
import { ClearWorkbasketFilter } from '../../../shared/store/filter-store/filter.actions';
import { ButtonAction } from '../../models/button-action';

const activatedRouteMock = {
  firstChild: {
    params: of({ id: 'workbasket' })
  }
};

const domainServiceSpy: Partial<DomainService> = {
  getSelectedDomainValue: vi.fn().mockReturnValue('A'),
  getSelectedDomain: vi.fn().mockReturnValue(of('A')),
  getDomains: vi.fn().mockReturnValue(of('A'))
};

const workbasketServiceSpy: Partial<WorkbasketService> = {
  getWorkBasketsSummary: vi.fn().mockReturnValue(of({ workbaskets: [] })),
  getWorkBasketsDistributionTargets: vi.fn().mockReturnValue(of({ distributionTargets: [] })),
  getWorkBasket: vi.fn().mockReturnValue(of(undefined))
};

const notificationServiceSpy: Partial<NotificationService> = {
  showSuccess: vi.fn(),
  showError: vi.fn(),
  showDialog: vi.fn()
};

describe('WorkbasketDistributionTargetsComponent', () => {
  let fixture: ComponentFixture<WorkbasketDistributionTargetsComponent>;
  let debugElement: DebugElement;
  let component: WorkbasketDistributionTargetsComponent;
  let store: Store;
  let actions$: Observable<any>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WorkbasketDistributionTargetsComponent],
      providers: [
        provideStore([WorkbasketState, FilterState]),
        provideAngularSvgIcon(),
        { provide: WorkbasketService, useValue: workbasketServiceSpy },
        { provide: ActivatedRoute, useValue: activatedRouteMock },
        { provide: DomainService, useValue: domainServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy }
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
  });

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

  it('should toggle sideBySide when toggleSideBySideView is called', () => {
    expect(component.sideBySide).toBe(true);
    component.toggleSideBySideView();
    expect(component.sideBySide).toBe(false);
    component.toggleSideBySideView();
    expect(component.sideBySide).toBe(true);
  });

  it('should set displayingDistributionTargetsPicker to true when toggleSideBySideView is called', () => {
    component.displayingDistributionTargetsPicker = false;
    component.toggleSideBySideView();
    expect(component.displayingDistributionTargetsPicker).toBe(true);
  });

  it('should toggle displayingDistributionTargetsPicker when toggleDistributionTargetsPicker is called', () => {
    expect(component.displayingDistributionTargetsPicker).toBe(true);
    component.toggleDistributionTargetsPicker();
    expect(component.displayingDistributionTargetsPicker).toBe(false);
    component.toggleDistributionTargetsPicker();
    expect(component.displayingDistributionTargetsPicker).toBe(true);
  });

  it('should dispatch UpdateWorkbasketDistributionTargets when onSave is called', () => {
    const dispatchSpy = vi.spyOn(store, 'dispatch').mockReturnValue(of(undefined));
    component.onSave();
    expect(dispatchSpy).toHaveBeenCalledWith(new UpdateWorkbasketDistributionTargets());
  });

  it('should emit to transferDistributionTargetObservable when moveDistributionTargets is called', () => {
    const nextSpy = vi.spyOn(component.transferDistributionTargetObservable, 'next');
    component.moveDistributionTargets(Side.SELECTED);
    expect(nextSpy).toHaveBeenCalledWith(Side.SELECTED);
  });

  it('should emit AVAILABLE side when moveDistributionTargets is called with AVAILABLE', () => {
    const nextSpy = vi.spyOn(component.transferDistributionTargetObservable, 'next');
    component.moveDistributionTargets(Side.AVAILABLE);
    expect(nextSpy).toHaveBeenCalledWith(Side.AVAILABLE);
  });

  it('should dispatch multiple actions when onClear is called', () => {
    const dispatchSpy = vi.spyOn(store, 'dispatch').mockReturnValue(of(undefined));
    component.onClear();
    expect(dispatchSpy).toHaveBeenCalledWith(new FetchWorkbasketDistributionTargets(true));
    expect(dispatchSpy).toHaveBeenCalledWith(new FetchAvailableDistributionTargets(true));
    expect(dispatchSpy).toHaveBeenCalledWith(new ClearWorkbasketFilter('selectedDistributionTargets'));
    expect(dispatchSpy).toHaveBeenCalledWith(new ClearWorkbasketFilter('availableDistributionTargets'));
    expect(notificationServiceSpy.showSuccess).toHaveBeenCalledWith('WORKBASKET_DISTRIBUTION_TARGET_RESTORE');
  });

  it('should complete destroy$ and transferDistributionTargetObservable on ngOnDestroy', () => {
    const destroySpy = vi.spyOn(component.destroy$, 'next');
    const destroyCompleteSpy = vi.spyOn(component.destroy$, 'complete');
    const transferCompleteSpy = vi.spyOn(component.transferDistributionTargetObservable, 'complete');
    component.ngOnDestroy();
    expect(transferCompleteSpy).toHaveBeenCalled();
    expect(destroySpy).toHaveBeenCalled();
    expect(destroyCompleteSpy).toHaveBeenCalled();
  });

  it('should have sideEnum property set to Side enum', () => {
    expect(component.sideEnum).toBe(Side);
  });

  it('should call onClear when buttonAction$ emits UNDO action', () => {
    const clearSpy = vi.spyOn(component, 'onClear').mockImplementation(() => {});
    store.reset({
      ...store.snapshot(),
      workbasket: { ...workbasketReadStateMock, button: ButtonAction.UNDO }
    });
    expect(clearSpy).toHaveBeenCalled();
  });
});
