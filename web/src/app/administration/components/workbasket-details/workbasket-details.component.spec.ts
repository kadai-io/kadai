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
import { Actions, provideStore, Store } from '@ngxs/store';
import { firstValueFrom, Observable, of } from 'rxjs';
import { ACTION } from '../../../shared/models/action';
import { WorkbasketState } from '../../../shared/store/workbasket-store/workbasket.state';
import { DomainService } from '../../../shared/services/domain/domain.service';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import {
  engineConfigurationMock,
  selectedWorkbasketMock,
  workbasketReadStateMock
} from '../../../shared/store/mock-data/mock-store';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { CopyWorkbasket, CreateWorkbasket } from '../../../shared/store/workbasket-store/workbasket.actions';
import { take } from 'rxjs/operators';
import { EngineConfigurationState } from '../../../shared/store/engine-configuration-store/engine-configuration.state';
import { provideRouter } from '@angular/router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { provideAngularSvgIcon } from 'angular-svg-icon';

const domainServiceSpy: Partial<DomainService> = {
  getSelectedDomain: vi.fn().mockReturnValue(of('A')),
  getSelectedDomainValue: vi.fn().mockReturnValue('A'),
  getDomains: vi.fn().mockReturnValue(of(['A']))
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
});
