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

import { TestBed } from '@angular/core/testing';
import { NgxsModule, Store } from '@ngxs/store';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { Observable, of } from 'rxjs';
import { AccessItemsManagementState } from './access-items-management.state';
import {
  GetAccessItems,
  GetGroupsByAccessId,
  GetPermissionsByAccessId,
  RemoveAccessItemsPermissions,
  SelectAccessId
} from './access-items-management.actions';
import { AccessIdsService } from '../../services/access-ids/access-ids.service';
import { NotificationService } from '../../services/notifications/notification.service';
import { RequestInProgressService } from '../../services/request-in-progress/request-in-progress.service';
import { AccessId } from '../../models/access-id';
import { WorkbasketAccessItemsRepresentation } from '../../models/workbasket-access-items-representation';

describe('AccessItemsManagementState', () => {
  let store: Store;
  let accessIdsServiceMock: {
    getGroupsByAccessId: ReturnType<typeof vi.fn>;
    getPermissionsByAccessId: ReturnType<typeof vi.fn>;
    getAccessItems: ReturnType<typeof vi.fn>;
    removeAccessItemsPermissions: ReturnType<typeof vi.fn>;
  };
  let notificationServiceMock: {
    showError: ReturnType<typeof vi.fn>;
    showSuccess: ReturnType<typeof vi.fn>;
  };
  let requestInProgressServiceMock: {
    setRequestInProgress: ReturnType<typeof vi.fn>;
  };

  const mockGroups: AccessId[] = [
    { accessId: 'group1', name: 'Group One' },
    { accessId: 'group2', name: 'Group Two' }
  ];

  const mockPermissions: AccessId[] = [{ accessId: 'perm1', name: 'Permission One' }];

  const mockAccessItemsResource: WorkbasketAccessItemsRepresentation = {
    accessItems: [
      {
        accessId: 'user1',
        workbasketId: 'wb1',
        accessItemId: 'ai1',
        workbasketKey: 'key1',
        accessName: 'User One',
        permRead: true,
        permReadTasks: false,
        permEditTasks: false,
        permOpen: true,
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
    ]
  };

  beforeEach(async () => {
    accessIdsServiceMock = {
      getGroupsByAccessId: vi.fn().mockReturnValue(of(mockGroups)),
      getPermissionsByAccessId: vi.fn().mockReturnValue(of(mockPermissions)),
      getAccessItems: vi.fn().mockReturnValue(of(mockAccessItemsResource)),
      removeAccessItemsPermissions: vi.fn().mockReturnValue(of(null))
    };

    notificationServiceMock = {
      showError: vi.fn(),
      showSuccess: vi.fn()
    };

    requestInProgressServiceMock = {
      setRequestInProgress: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [NgxsModule.forRoot([AccessItemsManagementState])],
      providers: [
        { provide: AccessIdsService, useValue: accessIdsServiceMock },
        { provide: NotificationService, useValue: notificationServiceMock },
        { provide: RequestInProgressService, useValue: requestInProgressServiceMock }
      ]
    }).compileComponents();

    store = TestBed.inject(Store);
  });

  it('should initialize the state', () => {
    const state = store.snapshot().accessItemsManagement;
    expect(state).toBeDefined();
  });

  describe('SelectAccessId', () => {
    it('should set selectedAccessId in state', async () => {
      const accessId: AccessId = { accessId: 'user123', name: 'User 123' };

      await store.dispatch(new SelectAccessId(accessId)).toPromise();

      const state = store.snapshot().accessItemsManagement;
      expect(state.selectedAccessId).toEqual(accessId);
    });

    it('should replace existing selectedAccessId', async () => {
      const firstAccessId: AccessId = { accessId: 'user1', name: 'User 1' };
      const secondAccessId: AccessId = { accessId: 'user2', name: 'User 2' };

      await store.dispatch(new SelectAccessId(firstAccessId)).toPromise();
      await store.dispatch(new SelectAccessId(secondAccessId)).toPromise();

      const state = store.snapshot().accessItemsManagement;
      expect(state.selectedAccessId).toEqual(secondAccessId);
    });
  });

  describe('GetGroupsByAccessId', () => {
    it('should call accessIdsService.getGroupsByAccessId with the given accessId', async () => {
      await store.dispatch(new GetGroupsByAccessId('user123')).toPromise();

      expect(accessIdsServiceMock.getGroupsByAccessId).toHaveBeenCalledWith('user123');
    });

    it('should update groups in state with returned groups', async () => {
      await store.dispatch(new GetGroupsByAccessId('user123')).toPromise();

      const state = store.snapshot().accessItemsManagement;
      expect(state.groups).toEqual(mockGroups);
    });
  });

  describe('GetPermissionsByAccessId', () => {
    it('should call accessIdsService.getPermissionsByAccessId with the given accessId', async () => {
      await store.dispatch(new GetPermissionsByAccessId('user123')).toPromise();

      expect(accessIdsServiceMock.getPermissionsByAccessId).toHaveBeenCalledWith('user123');
    });

    it('should update permissions in state with returned permissions', async () => {
      await store.dispatch(new GetPermissionsByAccessId('user123')).toPromise();

      const state = store.snapshot().accessItemsManagement;
      expect(state.permissions).toEqual(mockPermissions);
    });
  });

  describe('GetAccessItems', () => {
    it('should call accessIdsService.getAccessItems', async () => {
      await store.dispatch(new GetAccessItems()).toPromise();

      expect(accessIdsServiceMock.getAccessItems).toHaveBeenCalled();
    });

    it('should update accessItemsResource in state', async () => {
      await store.dispatch(new GetAccessItems()).toPromise();

      const state = store.snapshot().accessItemsManagement;
      expect(state.accessItemsResource).toEqual(mockAccessItemsResource);
    });

    it('should set requestInProgress to true before fetching and false after', async () => {
      await store.dispatch(new GetAccessItems()).toPromise();

      expect(requestInProgressServiceMock.setRequestInProgress).toHaveBeenCalledWith(true);
      expect(requestInProgressServiceMock.setRequestInProgress).toHaveBeenCalledWith(false);
    });
  });

  describe('RemoveAccessItemsPermissions', () => {
    it('should call accessIdsService.removeAccessItemsPermissions with the given accessId', async () => {
      await store.dispatch(new RemoveAccessItemsPermissions('user123')).toPromise();

      expect(accessIdsServiceMock.removeAccessItemsPermissions).toHaveBeenCalledWith('user123');
    });

    it('should call notificationService.showSuccess after removing permissions', async () => {
      await store.dispatch(new RemoveAccessItemsPermissions('user123')).toPromise();

      expect(notificationServiceMock.showSuccess).toHaveBeenCalledWith('WORKBASKET_ACCESS_ITEM_REMOVE_PERMISSION', {
        accessId: 'user123'
      });
    });

    it('should set requestInProgress to true before and false after removing permissions', async () => {
      await store.dispatch(new RemoveAccessItemsPermissions('user123')).toPromise();

      expect(requestInProgressServiceMock.setRequestInProgress).toHaveBeenCalledWith(true);
      expect(requestInProgressServiceMock.setRequestInProgress).toHaveBeenCalledWith(false);
    });
  });

  describe('GetGroupsByAccessId - error path', () => {
    it('should call setRequestInProgress(false) on error', async () => {
      accessIdsServiceMock.getGroupsByAccessId.mockReturnValue(
        new Observable((subscriber) => {
          subscriber.error(new Error('test error'));
        })
      );

      try {
        await store.dispatch(new GetGroupsByAccessId('user123')).toPromise();
      } catch (e) {
        // error expected
      }

      expect(requestInProgressServiceMock.setRequestInProgress).toHaveBeenCalledWith(false);
    });
  });

  describe('GetPermissionsByAccessId - error path', () => {
    it('should call setRequestInProgress(false) on error', async () => {
      accessIdsServiceMock.getPermissionsByAccessId.mockReturnValue(
        new Observable((subscriber) => {
          subscriber.error(new Error('test error'));
        })
      );

      try {
        await store.dispatch(new GetPermissionsByAccessId('user123')).toPromise();
      } catch (e) {
        // error expected
      }

      expect(requestInProgressServiceMock.setRequestInProgress).toHaveBeenCalledWith(false);
    });
  });

  describe('GetAccessItems - error path', () => {
    it('should call setRequestInProgress(false) on error', async () => {
      accessIdsServiceMock.getAccessItems.mockReturnValue(
        new Observable((subscriber) => {
          subscriber.error(new Error('test error'));
        })
      );

      try {
        await store.dispatch(new GetAccessItems()).toPromise();
      } catch (e) {
        // error expected
      }

      expect(requestInProgressServiceMock.setRequestInProgress).toHaveBeenCalledWith(false);
    });
  });

  describe('RemoveAccessItemsPermissions - error path', () => {
    it('should call setRequestInProgress(false) on error', async () => {
      accessIdsServiceMock.removeAccessItemsPermissions.mockReturnValue(
        new Observable((subscriber) => {
          subscriber.error(new Error('test error'));
        })
      );

      try {
        await store.dispatch(new RemoveAccessItemsPermissions('user123')).toPromise();
      } catch (e) {
        // error expected
      }

      expect(requestInProgressServiceMock.setRequestInProgress).toHaveBeenCalledWith(false);
    });
  });
});
