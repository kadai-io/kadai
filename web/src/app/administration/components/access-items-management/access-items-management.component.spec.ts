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

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AccessItemsManagementComponent } from './access-items-management.component';
import { provideStore, Store } from '@ngxs/store';
import { DebugElement } from '@angular/core';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import { EngineConfigurationState } from '../../../shared/store/engine-configuration-store/engine-configuration.state';
import { AccessItemsManagementState } from '../../../shared/store/access-items-management-store/access-items-management.state';
import { of } from 'rxjs';
import { Direction, Sorting, WorkbasketAccessItemQuerySortParameter } from '../../../shared/models/sorting';
import { engineConfigurationMock, workbasketAccessItemsMock } from '../../../shared/store/mock-data/mock-store';
import { provideHttpClient } from '@angular/common/http';
import { AccessIdsService } from '../../../shared/services/access-ids/access-ids.service';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';
import { FormsValidatorService } from '../../../shared/services/forms-validator/forms-validator.service';
import { SvgIconRegistryService } from 'angular-svg-icon';
import { ClassificationCategoriesService } from '../../../shared/services/classification-categories/classification-categories.service';

describe('AccessItemsManagementComponent', () => {
  let fixture: ComponentFixture<AccessItemsManagementComponent>;
  let debugElement: DebugElement;
  let app: AccessItemsManagementComponent;
  let store: Store;

  beforeEach(async () => {
    const accessIdsServiceMock: Partial<AccessIdsService> = {
      getGroupsByAccessId: jest.fn().mockReturnValue(of([{ accessId: 'g1', name: 'Group 1' }])),
      getPermissionsByAccessId: jest.fn().mockReturnValue(of([{ accessId: 'p1', name: 'Perm 1' }])),
      getAccessItems: jest.fn().mockReturnValue(of(workbasketAccessItemsMock)),
      removeAccessItemsPermissions: jest.fn().mockReturnValue(of({}))
    };

    const notificationServiceSpy: Partial<NotificationService> = {
      showDialog: jest.fn()
    };

    const requestInProgressServiceSpy: Partial<RequestInProgressService> = {
      setRequestInProgress: jest.fn()
    } as any;

    const formsValidatorServiceSpy: Partial<FormsValidatorService> = {
      isFieldValid: jest.fn().mockReturnValue(false)
    };

    await TestBed.configureTestingModule({
      imports: [AccessItemsManagementComponent],
      providers: [
        provideStore([EngineConfigurationState, AccessItemsManagementState]),
        provideHttpClient(),
        { provide: AccessIdsService, useValue: accessIdsServiceMock },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: RequestInProgressService, useValue: requestInProgressServiceSpy },
        { provide: FormsValidatorService, useValue: formsValidatorServiceSpy },
        {
          provide: SvgIconRegistryService,
          useValue: { addSvgIcon: jest.fn(), loadSvg: jest.fn(), getSvgByName: jest.fn() }
        },
        {
          provide: ClassificationCategoriesService,
          useValue: { getCustomisation: jest.fn().mockReturnValue(of(engineConfigurationMock.customisation)) }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AccessItemsManagementComponent);
    debugElement = fixture.debugElement;
    app = fixture.debugElement.componentInstance;
    store = TestBed.inject(Store);
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

  it(' (with permissions)', (done) => {
    jest.spyOn(app, 'setAccessItemsGroups');
    jest.spyOn(app, 'setAccessItemsPermissions');
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
    setTimeout(() => {
      expect(app.setAccessItemsGroups).toHaveBeenCalled();
      expect(app.setAccessItemsPermissions).toHaveBeenCalled();
      done();
    }, 0);
  });

  it('should display a dialog when access is revoked', () => {
    app.accessId = { accessId: 'xyz', name: 'xyz' };
    const notificationService = TestBed.inject(NotificationService);
    const showDialogSpy = jest.spyOn(notificationService, 'showDialog').mockImplementation();
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

  it('should dispatch GetAccessItems when permissions is null (groups only branch)', (done) => {
    jest.spyOn(app, 'setAccessItemsGroups');
    app.accessId = { accessId: '1', name: 'max' };
    app.groups = [
      { accessId: '1', name: 'users' },
      { accessId: '2', name: 'users' }
    ];
    app.permissions = null as any;
    app.searchForAccessItemsWorkbaskets();
    setTimeout(() => {
      expect(app.setAccessItemsGroups).toHaveBeenCalled();
      done();
    }, 0);
  });

  it('should filter access items by accessId and workbasketKey', () => {
    const items = [
      { accessId: 'a1', accessName: 'Alice', workbasketKey: 'WB_A' },
      { accessId: 'b1', accessName: 'Bob', workbasketKey: 'WB_B' }
    ] as any;
    app.setAccessItemsGroups(items);
    app.accessItemsForm.patchValue({ accessIdFilter: 'ali', workbasketKeyFilter: 'wb_a' });
    app.filterAccessItems();
    expect(app.accessItems.length).toBe(1);
    expect(app.accessItems[0].accessName).toBe('Alice');
  });

  it('should remove focus by calling activeElement.focus()', () => {
    const active = document.activeElement as HTMLElement;
    const focusSpy = jest.spyOn(active, 'focus');
    app.removeFocus();
    expect(focusSpy).toHaveBeenCalled();
  });

  it('ngOnInit should set requestInProgress to false', () => {
    const req = TestBed.inject(RequestInProgressService) as any;
    const spy = jest.spyOn(req, 'setRequestInProgress');
    app.ngOnInit();
    expect(spy).toHaveBeenCalledWith(false);
  });
});
