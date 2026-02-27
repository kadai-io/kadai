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
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

describe('WorkbasketAccessItemsComponent', () => {
  let fixture: ComponentFixture<WorkbasketAccessItemsComponent>;
  let debugElement: DebugElement;
  let component: WorkbasketAccessItemsComponent;
  let store: Store;
  let actions$: Observable<any>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
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
  });

  afterEach(async () => {
    component.workbasket = { ...selectedWorkbasketMock };
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize when accessItems exist', async () => {
    let actionDispatched = false;
    actions$.pipe(ofActionDispatched(GetWorkbasketAccessItems)).subscribe(() => (actionDispatched = true));
    component.init();
    expect(actionDispatched).toBe(true);
  });

  it("should discard initializing when accessItems don't exist", () => {
    component.workbasket = {
      ...selectedWorkbasketMock,
      _links: { ...selectedWorkbasketMock._links, accessItems: null }
    };
    let actionDispatched = false;
    actions$.pipe(ofActionDispatched(GetWorkbasketAccessItems)).subscribe(() => (actionDispatched = true));
    component.init();
    expect(actionDispatched).toBe(false);
  });

  it('should call access items sorting when access items are obtained from store', () => {
    const sortSpy = vi.spyOn(component, 'sortAccessItems');
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
    const clearSpy = vi.spyOn(component, 'addAccessItem');

    addAccessItemButton.click();
    expect(clearSpy).toHaveBeenCalled();
  });

  it('should undo changes when undo button is clicked', () => {
    fixture.detectChanges();
    const clearSpy = vi.spyOn(component, 'clear');
    component.clear();
    expect(clearSpy).toHaveBeenCalled();
  });

  it('should check all permissions when check all box is checked', () => {
    fixture.detectChanges();
    const checkAllSpy = vi.spyOn(component, 'checkAll');
    const checkAllButton = debugElement.nativeElement.querySelector('#checkbox-0-00');
    expect(checkAllButton).toBeTruthy();
    checkAllButton.click();
    expect(checkAllSpy).toHaveBeenCalled();
  });

  it('should dispatch UpdateWorkbasketAccessItems action when save button is triggered', () => {
    component.accessItemsRepresentation._links.self.href = 'https://link.mock';
    const onSaveSpy = vi.spyOn(component, 'onSave');
    let actionDispatched = false;
    actions$.pipe(ofActionDispatched(UpdateWorkbasketAccessItems)).subscribe(() => (actionDispatched = true));
    component.onSave();
    expect(onSaveSpy).toHaveBeenCalled();
    expect(actionDispatched).toBe(true);
  });

  it('should emit accessItemsValidityChanged when accessItemsGroups status changes', async () => {
    const emitSpy = vi.spyOn(component.accessItemsValidityChanged, 'emit');

    component.ngOnInit();
    await fixture.whenStable();

    const control = component.AccessItemsForm.get('accessItemsGroups')?.get('0.accessId');
    expect(control).toBeTruthy();

    control?.setValue('');
    control?.markAsTouched();
    control?.updateValueAndValidity();

    expect(emitSpy).toHaveBeenCalledWith(false);
  });

  it('should set accessId and accessName on the form control for the given row when accessItemSelected is called', () => {
    fixture.detectChanges();
    component.accessItemSelected({ accessId: 'user-1', name: 'User One' }, 0);
    expect(component.accessItemsGroups.controls[0].get('accessId').value).toBe('user-1');
    expect(component.accessItemsGroups.controls[0].get('accessName').value).toBe('User One');
  });

  it('should handle null accessItem gracefully in accessItemSelected', () => {
    fixture.detectChanges();
    component.accessItemSelected(null, 0);
    expect(component.accessItemsGroups.controls[0].get('accessId').value).toBeUndefined();
    expect(component.accessItemsGroups.controls[0].get('accessName').value).toBeUndefined();
  });

  it('should return an array copy of access items when cloneAccessItems is called', () => {
    fixture.detectChanges();
    const clone = component.cloneAccessItems();
    expect(Array.isArray(clone)).toBe(true);
    expect(clone.length).toBe(component.accessItemsGroups.length);
  });

  it('should return a deep copy and not the same references when cloneAccessItems is called', () => {
    fixture.detectChanges();
    const clone = component.cloneAccessItems();
    const original = component.AccessItemsForm.value.accessItemsGroups;
    expect(clone[0]).not.toBe(original[0]);
    expect(clone[0].accessId).toBe(original[0].accessId);
  });

  it('should return the correct permCustomN string for getAccessItemCustomProperty', () => {
    expect(component.getAccessItemCustomProperty(1)).toBe('permCustom1');
    expect(component.getAccessItemCustomProperty(5)).toBe('permCustom5');
    expect(component.getAccessItemCustomProperty(12)).toBe('permCustom12');
  });

  it('should update workbasketId for all access items when setWorkbasketIdForCopy is called', () => {
    fixture.detectChanges();
    const newId = 'WBI:NEW-WORKBASKET-ID';
    component.setWorkbasketIdForCopy(newId);
    component.accessItemsGroups.value.forEach((item) => {
      expect(item.workbasketId).toBe(newId);
      expect('accessItemId' in item).toBe(false);
    });
  });

  it('should add index to selectedRows when selectRow is called with checked true', () => {
    component.selectedRows = [];
    component.selectRow({ target: { checked: true } }, 0);
    expect(component.selectedRows).toContain(0);
  });

  it('should remove index from selectedRows when selectRow is called with checked false', () => {
    component.selectedRows = [0, 1];
    component.selectRow({ target: { checked: false } }, 0);
    expect(component.selectedRows).not.toContain(0);
    expect(component.selectedRows).toContain(1);
  });

  it('should remove multiple rows in descending order when deleteAccessItems is called with multiple selectedRows', () => {
    fixture.detectChanges();
    const initialLength = component.accessItemsGroups.length;
    component.selectedRows = [0, 1];
    component.deleteAccessItems();
    expect(component.accessItemsGroups.length).toBe(initialLength - 2);
    expect(component.selectedRows).toEqual([]);
  });

  it('should set formSubmitAttempt to true and call validateFormAccess, onSave when onSubmit is called', async () => {
    fixture.detectChanges();
    const validateSpy = vi.spyOn(component.formsValidatorService, 'validateFormAccess').mockResolvedValue(true);
    const onSaveSpy = vi.spyOn(component, 'onSave');
    component.onSubmit();
    expect(component.formsValidatorService.formSubmitAttempt).toBe(true);
    expect(validateSpy).toHaveBeenCalledWith(component.accessItemsGroups, component.toggleValidationAccessIdMap);
    expect(onSaveSpy).toHaveBeenCalled();
  });

  it('should set isNewAccessItemsFromStore and isAccessItemsTabSelected to false in ngAfterViewChecked when element exists', () => {
    fixture.detectChanges();
    component.isNewAccessItemsFromStore = true;
    component.isAccessItemsTabSelected = true;
    component.ngAfterViewChecked();
    if (document.getElementById('checkbox-0-00')) {
      expect(component.isNewAccessItemsFromStore).toBe(false);
      expect(component.isAccessItemsTabSelected).toBe(false);
    } else {
      expect(component.isNewAccessItemsFromStore).toBe(true);
    }
  });

  it('should not change flags in ngAfterViewChecked when neither isNewAccessItemsFromStore nor isAccessItemsTabSelected is true', () => {
    fixture.detectChanges();
    component.isNewAccessItemsFromStore = false;
    component.isAccessItemsTabSelected = false;
    component.ngAfterViewChecked();
    expect(component.isNewAccessItemsFromStore).toBe(false);
    expect(component.isAccessItemsTabSelected).toBe(false);
  });
});
