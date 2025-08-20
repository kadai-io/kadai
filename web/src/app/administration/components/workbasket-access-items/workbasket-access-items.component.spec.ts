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

import { ComponentFixture, fakeAsync, TestBed, tick, waitForAsync } from '@angular/core/testing';
import { WorkbasketAccessItemsComponent } from './workbasket-access-items.component';
import { DebugElement } from '@angular/core';
import { Actions, ofActionDispatched, provideStore, Store } from '@ngxs/store';
import { Observable } from 'rxjs';
import { WorkbasketState } from '../../../shared/store/workbasket-store/workbasket.state';
import { EngineConfigurationState } from '../../../shared/store/engine-configuration-store/engine-configuration.state';
import {
  engineConfigurationMock,
  selectedWorkbasketMock,
  workbasketAccessItemsMock
} from '../../../shared/store/mock-data/mock-store';
import {
  GetWorkbasketAccessItems,
  UpdateWorkbasketAccessItems
} from '../../../shared/store/workbasket-store/workbasket.actions';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { provideHttpClientTesting } from '@angular/common/http/testing';

describe('WorkbasketAccessItemsComponent', () => {
  let fixture: ComponentFixture<WorkbasketAccessItemsComponent>;
  let debugElement: DebugElement;
  let component: WorkbasketAccessItemsComponent;
  let store: Store;
  let actions$: Observable<any>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [WorkbasketAccessItemsComponent],
      providers: [
        provideStore([WorkbasketState, EngineConfigurationState]),
        provideRouter([]),
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(WorkbasketAccessItemsComponent);
    debugElement = fixture.debugElement;
    component = fixture.componentInstance;
    store = TestBed.inject(Store);
    actions$ = TestBed.inject(Actions);
    component.workbasket = { ...selectedWorkbasketMock };
    component.accessItemsRepresentation = workbasketAccessItemsMock;
    store.reset({
      ...store.snapshot(),
      engineConfiguration: engineConfigurationMock,
      workbasket: {
        workbasketAccessItems: workbasketAccessItemsMock
      }
    });
  }));

  afterEach(waitForAsync(() => {
    component.workbasket = { ...selectedWorkbasketMock };
  }));

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize when accessItems exist', waitForAsync(() => {
    let actionDispatched = false;
    actions$.pipe(ofActionDispatched(GetWorkbasketAccessItems)).subscribe(() => (actionDispatched = true));
    component.init();
    expect(actionDispatched).toBe(true);
  }));

  it("should discard initializing when accessItems don't exist", () => {
    component.workbasket._links.accessItems = null;
    let actionDispatched = false;
    actions$.pipe(ofActionDispatched(GetWorkbasketAccessItems)).subscribe(() => (actionDispatched = true));
    component.init();
    expect(actionDispatched).toBe(false);
  });

  it('should call access items sorting when access items are obtained from store', () => {
    const sortSpy = jest.spyOn(component, 'sortAccessItems');
    component.ngOnInit();
    expect(sortSpy).toHaveBeenCalled();
  });

  it('should sort access items by given value', () => {
    const accessItems = component.accessItemsRepresentation.accessItems;
    expect(accessItems[0].accessId).toBe('user-b-1');
    expect(accessItems[1].accessId).toBe('user-b-0');
    component.sortAccessItems(accessItems, 'accessId');
    expect(accessItems[0].accessId).toBe('user-b-0');
    expect(accessItems[1].accessId).toBe('user-b-1');
  });

  it('should add accessItems when add access item button is clicked', () => {
    fixture.detectChanges();
    const addAccessItemButton = debugElement.nativeElement.querySelector(
      'button.workbasket-access-items__buttons-add-access'
    );
    const clearSpy = jest.spyOn(component, 'addAccessItem');

    addAccessItemButton.click();
    expect(clearSpy).toHaveBeenCalled();
  });

  it('should undo changes when undo button is clicked', () => {
    fixture.detectChanges();
    const clearSpy = jest.spyOn(component, 'clear');
    component.clear();
    expect(clearSpy).toHaveBeenCalled();
  });

  it('should check all permissions when check all box is checked', () => {
    fixture.detectChanges();
    const checkAllSpy = jest.spyOn(component, 'checkAll');
    const checkAllButton = debugElement.nativeElement.querySelector('#checkbox-0-00');
    expect(checkAllButton).toBeTruthy();
    checkAllButton.click();
    expect(checkAllSpy).toHaveBeenCalled();
  });

  it('should dispatch UpdateWorkbasketAccessItems action when save button is triggered', () => {
    component.accessItemsRepresentation._links.self.href = 'https://link.mock';
    const onSaveSpy = jest.spyOn(component, 'onSave');
    let actionDispatched = false;
    actions$.pipe(ofActionDispatched(UpdateWorkbasketAccessItems)).subscribe(() => (actionDispatched = true));
    component.onSave();
    expect(onSaveSpy).toHaveBeenCalled();
    expect(actionDispatched).toBe(true);
  });

  it('should emit accessItemsValidityChanged when accessItemsGroups status changes', fakeAsync(() => {
    const emitSpy = jest.spyOn(component.accessItemsValidityChanged, 'emit');

    component.ngOnInit();
    fixture.detectChanges();
    tick();

    const control = component.AccessItemsForm.get('accessItemsGroups')?.get('0.accessId');
    expect(control).toBeTruthy();

    control?.setValue('');
    control?.markAsTouched();
    control?.updateValueAndValidity();

    tick();

    expect(emitSpy).toHaveBeenCalledWith(false);
  }));
});
