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
import { AccessItemsManagementComponent } from './access-items-management.component';
import { Actions, ofActionDispatched, provideStore, Store } from '@ngxs/store';
import { DebugElement } from '@angular/core';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import { EngineConfigurationState } from '../../../shared/store/engine-configuration-store/engine-configuration.state';
import { AccessItemsManagementState } from '../../../shared/store/access-items-management-store/access-items-management.state';
import { Observable } from 'rxjs';
import { GetAccessItems } from '../../../shared/store/access-items-management-store/access-items-management.actions';
import { Direction, Sorting, WorkbasketAccessItemQuerySortParameter } from '../../../shared/models/sorting';
import { engineConfigurationMock } from '../../../shared/store/mock-data/mock-store';
import { provideHttpClient } from '@angular/common/http';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { provideAngularSvgIcon } from 'angular-svg-icon';

describe('AccessItemsManagementComponent', () => {
  let fixture: ComponentFixture<AccessItemsManagementComponent>;
  let debugElement: DebugElement;
  let app: AccessItemsManagementComponent;
  let store: Store;
  let actions$: Observable<any>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccessItemsManagementComponent],
      providers: [
        provideStore([EngineConfigurationState, AccessItemsManagementState]),
        provideHttpClient(),
        provideAngularSvgIcon()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AccessItemsManagementComponent);
    debugElement = fixture.debugElement;
    app = fixture.debugElement.componentInstance;
    store = TestBed.inject(Store);
    actions$ = TestBed.inject(Actions);
    store.reset({
      ...store.snapshot(),
      engineConfiguration: engineConfigurationMock
    });
    fixture.detectChanges();
  });

  it('should create the app', () => {
    expect(app).toBeTruthy();
  });

  it('should render search type ahead', () => {
    const typeAhead = () => debugElement.nativeElement.querySelector('kadai-shared-type-ahead');
    expect(typeAhead()).toBeTruthy();
  });

  it('should not display result table when search bar is empty', () => {
    const form = () => debugElement.nativeElement.querySelector('ng-form');
    expect(form()).toBeFalsy();
  });

  it('should initialize app with ngxs store', () => {
    const engineConfigs = store.selectSnapshot((state) => {
      return state.engineConfiguration.customisation.EN.workbaskets['access-items'];
    });
    expect(engineConfigs).toBeDefined();
    expect(engineConfigs).not.toEqual([]);

    const groups = store.selectSnapshot((state) => state.accessItemsManagement);
    expect(groups).toBeDefined();

    const permissions = store.selectSnapshot((state) => state.accessItemsManagement);
    expect(permissions).toBeDefined();
  });

  it('should be able to get groups if selected access ID is not null in onSelectAccessId', () => {
    const selectedAccessId = { accessId: '1', name: '' };
    app.onSelectAccessId(selectedAccessId);
    const groups = store.selectSnapshot((state) => state.accessItemsManagement);
    expect(selectedAccessId).not.toBeNull();
    expect(groups).not.toBeNull();
    app.onSelectAccessId(null);
    expect(groups).toMatchObject({});
  });

  it('should be able to get permissions if selected access ID is not null in onSelectAccessId', () => {
    const selectedAccessId = { accessId: '1', name: '' };
    app.permissions = [
      { accessId: '1', name: 'perm' },
      { accessId: '2', name: 'perm' }
    ];
    app.onSelectAccessId(selectedAccessId);
    const permissions = store.selectSnapshot((state) => state.accessItemsManagement);
    expect(selectedAccessId).not.toBeNull();
    expect(permissions).not.toBeNull();
    app.onSelectAccessId(null);
    expect(permissions).toMatchObject({});
  });

  it('should dispatch GetAccessItems action in searchForAccessItemsWorkbaskets', async () => {
    app.accessId = { accessId: '1', name: 'max' };
    app.groups = [
      { accessId: '1', name: 'users' },
      { accessId: '2', name: 'users' }
    ];
    app.permissions = [
      { accessId: '1', name: 'perm' },
      { accessId: '2', name: 'perm' }
    ];
    app.sortModel = {
      'sort-by': WorkbasketAccessItemQuerySortParameter.ACCESS_ID,
      order: Direction.DESC
    };
    app.searchForAccessItemsWorkbaskets();
    fixture.detectChanges();
    let actionDispatched = false;
    actions$.pipe(ofActionDispatched(GetAccessItems)).subscribe(() => {
      actionDispatched = true;
      expect(actionDispatched).toBe(true);
      expect(app.setAccessItemsGroups).toHaveBeenCalled();
      expect(app.setAccessItemsPermissions).toHaveBeenCalled();
    });
  });

  it('should display a dialog when access is revoked', async () => {
    app.accessId = { accessId: 'xyz', name: 'xyz' };
    const notificationService = TestBed.inject(NotificationService);
    const showDialogSpy = vi.spyOn(notificationService, 'showDialog').mockImplementation(() => undefined);
    app.revokeAccess();
    fixture.detectChanges();
    expect(showDialogSpy).toHaveBeenCalled();
  });

  it('should create accessItemsForm in setAccessItemsGroups', () => {
    app.setAccessItemsGroups([]);
    expect(app.accessItemsForm).toBeDefined();
    expect(app.accessItemsForm).not.toBeNull();
  });

  it('should create accessItemsForm in setAccessItemsPermissions', () => {
    app.setAccessItemsPermissions([]);
    expect(app.accessItemsForm).toBeDefined();
    expect(app.accessItemsForm).not.toBeNull();
  });

  it('should invoke sorting function correctly', () => {
    const newSort: Sorting<WorkbasketAccessItemQuerySortParameter> = {
      'sort-by': WorkbasketAccessItemQuerySortParameter.ACCESS_ID,
      order: Direction.DESC
    };
    app.accessId = { accessId: '1', name: 'max' };
    app.groups = [{ accessId: '1', name: 'users' }];
    app.permissions = [{ accessId: '1', name: 'perm' }];
    app.sorting(newSort);
    expect(app.sortModel).toMatchObject(newSort);
  });

  it('should not return accessItemsGroups when accessItemsForm is null', () => {
    app.accessItemsForm = null;
    expect(app.accessItemsGroups).toBeNull();
  });

  it('should not return accessItemsPermissions when accessItemsForm is null', () => {
    app.accessItemsForm = null;
    expect(app.accessItemsPermissions).toBeNull();
  });

  it('should filter access items by accessIdFilter', () => {
    app.setAccessItemsGroups([
      {
        accessId: 'user1',
        workbasketId: 'wb1',
        accessItemId: 'ai1',
        workbasketKey: 'key1',
        accessName: 'User Alpha',
        permRead: false,
        permReadTasks: false,
        permEditTasks: false,
        permOpen: false,
        permAppend: false,
        permTransfer: false,
        permDistribute: false,
        permCustom1: false,
        permCustom2: false,
        permCustom3: false,
        permCustom4: false,
        permCustom5: false,
        permCustom6: false,
        permCustom7: false,
        permCustom8: false,
        permCustom9: false,
        permCustom10: false,
        permCustom11: false,
        permCustom12: false
      },
      {
        accessId: 'user2',
        workbasketId: 'wb2',
        accessItemId: 'ai2',
        workbasketKey: 'key2',
        accessName: 'User Beta',
        permRead: false,
        permReadTasks: false,
        permEditTasks: false,
        permOpen: false,
        permAppend: false,
        permTransfer: false,
        permDistribute: false,
        permCustom1: false,
        permCustom2: false,
        permCustom3: false,
        permCustom4: false,
        permCustom5: false,
        permCustom6: false,
        permCustom7: false,
        permCustom8: false,
        permCustom9: false,
        permCustom10: false,
        permCustom11: false,
        permCustom12: false
      }
    ]);
    app.accessItemsForm.patchValue({ accessIdFilter: 'alpha' });
    app.filterAccessItems();
    expect(app.accessItems.length).toBe(1);
    expect(app.accessItems[0].accessName).toBe('User Alpha');
  });

  it('should filter access items by workbasketKeyFilter', () => {
    app.setAccessItemsGroups([
      {
        accessId: 'user1',
        workbasketId: 'wb1',
        accessItemId: 'ai1',
        workbasketKey: 'MYKEY1',
        accessName: 'User Alpha',
        permRead: false,
        permReadTasks: false,
        permEditTasks: false,
        permOpen: false,
        permAppend: false,
        permTransfer: false,
        permDistribute: false,
        permCustom1: false,
        permCustom2: false,
        permCustom3: false,
        permCustom4: false,
        permCustom5: false,
        permCustom6: false,
        permCustom7: false,
        permCustom8: false,
        permCustom9: false,
        permCustom10: false,
        permCustom11: false,
        permCustom12: false
      },
      {
        accessId: 'user2',
        workbasketId: 'wb2',
        accessItemId: 'ai2',
        workbasketKey: 'OTHERKEY',
        accessName: 'User Beta',
        permRead: false,
        permReadTasks: false,
        permEditTasks: false,
        permOpen: false,
        permAppend: false,
        permTransfer: false,
        permDistribute: false,
        permCustom1: false,
        permCustom2: false,
        permCustom3: false,
        permCustom4: false,
        permCustom5: false,
        permCustom6: false,
        permCustom7: false,
        permCustom8: false,
        permCustom9: false,
        permCustom10: false,
        permCustom11: false,
        permCustom12: false
      }
    ]);
    app.accessItemsForm.patchValue({ workbasketKeyFilter: 'mykey' });
    app.filterAccessItems();
    expect(app.accessItems.length).toBe(1);
    expect(app.accessItems[0].workbasketKey).toBe('MYKEY1');
  });

  it('should clear filters and call searchForAccessItemsWorkbaskets when clearFilter is called', () => {
    app.setAccessItemsGroups([]);
    app.accessItemsForm.patchValue({ workbasketKeyFilter: 'something', accessIdFilter: 'another' });
    const searchSpy = vi.spyOn(app, 'searchForAccessItemsWorkbaskets').mockImplementation(() => {});
    app.clearFilter();
    expect(app.accessItemsForm.value.workbasketKeyFilter).toBe('');
    expect(app.accessItemsForm.value.accessIdFilter).toBe('');
    expect(searchSpy).toHaveBeenCalled();
  });

  it('should not throw when clearFilter is called with no accessItemsForm', () => {
    app.accessItemsForm = null;
    expect(() => app.clearFilter()).not.toThrow();
  });

  it('should complete destroy$ on ngOnDestroy', () => {
    const nextSpy = vi.spyOn(app.destroy$, 'next');
    const completeSpy = vi.spyOn(app.destroy$, 'complete');
    app.ngOnDestroy();
    expect(nextSpy).toHaveBeenCalled();
    expect(completeSpy).toHaveBeenCalled();
  });

  it('should call removeFocus without throwing', () => {
    expect(() => app.removeFocus()).not.toThrow();
  });

  it('should set null accessItemsForm when onSelectAccessId is called with null', () => {
    app.setAccessItemsGroups([]);
    expect(app.accessItemsForm).not.toBeNull();
    app.onSelectAccessId(null);
    expect(app.accessItemsForm).toBeNull();
  });
});
