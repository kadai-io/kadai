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
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';

describe('WorkbasketAccessItemsComponent', () => {
  let fixture: ComponentFixture<WorkbasketAccessItemsComponent>;
  let debugElement: DebugElement;
  let component: WorkbasketAccessItemsComponent;
  let store: Store;
  let actions$: Observable<any>;
  let httpMock: HttpTestingController;

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
    httpMock = TestBed.inject(HttpTestingController);

    const reqs = httpMock.match('environments/data-sources/kadai-customization.json');
    reqs.forEach((req) => req.flush({ EN: {}, DE: {} }));
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

  it('should create default workbasket access item with all flags false', () => {
    const item = component.createWorkbasketAccessItems();
    expect(item.accessItemId).toBe('');
    expect(item.accessId).toBe('');
    expect(item.permRead).toBe(false);
    expect(item.permCustom12).toBe(false);
  });

  it('should add a new access item with permRead true and set added flag', () => {
    component.ngOnInit();
    const initialLength = component.accessItemsGroups.length;
    component.addAccessItem();
    expect(component.accessItemsGroups.length).toBe(initialLength + 1);
    expect(component.AccessItemsForm.get('accessItemsGroups')?.get('0.permRead')?.value).toBe(true);
    expect(component.added).toBe(true);
  });

  it('onSubmit should call onSave (validation promise handled truthy)', () => {
    const saveSpy = jest.spyOn(component, 'onSave');
    component.onSubmit();
    expect(saveSpy).toHaveBeenCalled();
  });

  it('should set accessId and accessName when accessItemSelected is triggered', () => {
    component.ngOnInit();
    const access = { accessId: 'A123', name: 'Alice' } as any;
    component.accessItemSelected(access, 0);
    expect(component.AccessItemsForm.get('accessItemsGroups')?.get('0.accessId')?.value).toBe('A123');
    expect(component.AccessItemsForm.get('accessItemsGroups')?.get('0.accessName')?.value).toBe('Alice');
  });

  it('checkAll should toggle all visible permission fields for a row', () => {
    component.keysOfVisibleFields = ['permRead', 'permOpen', 'permAppend'];
    const group = (component as any).formBuilder.group({
      permRead: false,
      permOpen: false,
      permAppend: false
    });
    component.AccessItemsForm.setControl('accessItemsGroups', (component as any).formBuilder.array([group]));

    component.checkAll(0, { target: { checked: true } });
    expect(group.get('permRead')?.value).toBe(true);
    expect(group.get('permOpen')?.value).toBe(true);
    expect(group.get('permAppend')?.value).toBe(true);

    component.checkAll(0, { target: { checked: false } });
    expect(group.get('permRead')?.value).toBe(false);
    expect(group.get('permOpen')?.value).toBe(false);
    expect(group.get('permAppend')?.value).toBe(false);
  });

  it('setSelectAllCheckbox should reflect row state to the master checkbox element', () => {
    component.keysOfVisibleFields = ['permRead', 'permOpen'];
    const group = (component as any).formBuilder.group({
      permRead: true,
      permOpen: true
    });
    component.AccessItemsForm.setControl('accessItemsGroups', (component as any).formBuilder.array([group]));
    const checkbox = document.createElement('input');
    checkbox.type = 'checkbox';
    checkbox.id = 'checkbox-0-00';
    document.body.appendChild(checkbox);

    component.setSelectAllCheckbox(0, { currentTarget: { checked: true } });
    expect((document.getElementById('checkbox-0-00') as HTMLInputElement).checked).toBe(true);

    group.get('permOpen')?.setValue(false);
    component.setSelectAllCheckbox(0, { currentTarget: { checked: true } });
    expect((document.getElementById('checkbox-0-00') as HTMLInputElement).checked).toBe(false);
  });

  it('cloneAccessItems should return copies independent from current form values', () => {
    component.ngOnInit();
    const clones = component.cloneAccessItems();
    component.AccessItemsForm.get('accessItemsGroups')?.get('0.permRead')?.setValue(true);
    expect(clones[0].permRead).toBe(true);
  });

  it('setWorkbasketIdForCopy should set workbasketId and remove accessItemId in values', () => {
    const group = (component as any).formBuilder.group({
      accessItemId: 'oldId',
      workbasketId: '',
      permRead: false
    });
    component.AccessItemsForm.setControl('accessItemsGroups', (component as any).formBuilder.array([group]));
    component.setWorkbasketIdForCopy('WB123');
    const val = component.AccessItemsForm.value.accessItemsGroups[0];
    expect(val.workbasketId).toBe('WB123');
    expect(val.accessItemId).toBeUndefined();
  });

  it('getAccessItemCustomProperty should build correct property name', () => {
    expect(component.getAccessItemCustomProperty(5)).toBe('permCustom5');
  });

  it('selectRow should add and remove row indices based on checkbox state', () => {
    component.selectedRows = [];
    component.selectRow({ target: { checked: true } } as any, 2);
    expect(component.selectedRows).toEqual([2]);
    component.selectRow({ target: { checked: false } } as any, 2);
    expect(component.selectedRows).toEqual([]);
  });

  it('deleteAccessItems should remove selected rows from form and clone', () => {
    const g1 = (component as any).formBuilder.group({ permRead: false });
    const g2 = (component as any).formBuilder.group({ permRead: false });
    component.AccessItemsForm.setControl('accessItemsGroups', (component as any).formBuilder.array([g1, g2]));
    component.accessItemsClone = [{ permRead: false } as any, { permRead: false } as any];
    component.selectedRows = [1];
    component.deleteAccessItems();
    expect((component.AccessItemsForm.get('accessItemsGroups') as any).length).toBe(1);
    expect(component.accessItemsClone.length).toBe(1);
  });

  it('ngOnChanges should re-initialize when workbasketId changes', () => {
    const initSpy = jest.spyOn(component, 'init');
    (component as any).workbasketClone = { ...component.workbasket, workbasketId: 'DIFFERENT' };
    component.workbasket = { ...component.workbasket, workbasketId: 'WB-NEW' } as any;
    component.ngOnChanges();
    expect(initSpy).toHaveBeenCalled();
  });

  it('ngOnChanges should not re-initialize when workbasketId stays the same', () => {
    const initSpy = jest.spyOn(component, 'init');
    (component as any).workbasketClone = { ...component.workbasket };
    component.workbasket = { ...component.workbasket } as any;
    component.ngOnChanges();
    expect(initSpy).not.toHaveBeenCalled();
  });

  it('ngAfterViewChecked should set select-all per row and reset flags', () => {
    const g1 = (component as any).formBuilder.group({ permRead: true });
    const g2 = (component as any).formBuilder.group({ permRead: true });
    component.AccessItemsForm.setControl('accessItemsGroups', (component as any).formBuilder.array([g1, g2]));
    component.keysOfVisibleFields = ['permRead'];
    const checkbox0 = document.createElement('input');
    checkbox0.type = 'checkbox';
    checkbox0.id = 'checkbox-0-00';
    document.body.appendChild(checkbox0);
    const checkbox1 = document.createElement('input');
    checkbox1.type = 'checkbox';
    checkbox1.id = 'checkbox-1-00';
    document.body.appendChild(checkbox1);
    component.isNewAccessItemsFromStore = true;
    const spy = jest.spyOn(component, 'setSelectAllCheckbox');

    component.ngAfterViewChecked();

    expect(spy).toHaveBeenCalledTimes(2);
    expect(component.isAccessItemsTabSelected).toBe(false);
    expect(component.isNewAccessItemsFromStore).toBe(false);
  });
});
