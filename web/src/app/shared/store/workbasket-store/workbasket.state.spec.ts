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
import { of, throwError } from 'rxjs';
import { Location } from '@angular/common';

import { WorkbasketState } from './workbasket.state';
import { FilterState } from '../filter-store/filter.state';
import {
  CopyWorkbasket,
  CreateWorkbasket,
  DeselectWorkbasket,
  FetchAvailableDistributionTargets,
  FetchWorkbasketDistributionTargets,
  GetWorkbasketAccessItems,
  GetWorkbasketsSummary,
  MarkWorkbasketForDeletion,
  OnButtonPressed,
  RemoveDistributionTarget,
  SaveNewWorkbasket,
  SelectComponent,
  SelectWorkbasket,
  SetActiveAction,
  TransferDistributionTargets,
  UpdateWorkbasket,
  UpdateWorkbasketAccessItems,
  UpdateWorkbasketDistributionTargets
} from './workbasket.actions';

import { WorkbasketService } from '../../services/workbasket/workbasket.service';
import { NotificationService } from '../../services/notifications/notification.service';
import { RequestInProgressService } from '../../services/request-in-progress/request-in-progress.service';
import { DomainService } from '../../services/domain/domain.service';
import { ACTION } from '../../models/action';
import { WorkbasketComponent } from '../../../administration/models/workbasket-component';
import { Side } from '../../../administration/models/workbasket-distribution-enums';
import { ButtonAction } from '../../../administration/models/button-action';
import { WorkbasketQueryFilterParameter } from '../../models/workbasket-query-filter-parameter';
import { Direction, WorkbasketQuerySortParameter } from '../../models/sorting';
import { QueryPagingParameter } from '../../models/query-paging-parameter';
import { selectedWorkbasketMock, workbasketAccessItemsMock } from '../mock-data/mock-store';
import { ActivatedRoute } from '@angular/router';

const mockWorkbasket = selectedWorkbasketMock;

const mockAccessItems = workbasketAccessItemsMock;

const mockDistributionTargets = {
  distributionTargets: [
    { workbasketId: 'WBI:DT1', key: 'DT-1', name: 'DT Workbasket 1' },
    { workbasketId: 'WBI:DT2', key: 'DT-2', name: 'DT Workbasket 2' }
  ],
  _links: {}
};

const mockAvailableDistributionTargets = {
  workbaskets: [
    { workbasketId: 'WBI:AV1', key: 'AV-1', name: 'Available WB 1' },
    { workbasketId: 'WBI:AV2', key: 'AV-2', name: 'Available WB 2' }
  ],
  page: { totalPages: 1, size: 10, totalElements: 2, number: 1 },
  _links: {}
};

const initialWorkbasketState = {
  selectedWorkbasket: mockWorkbasket,
  action: ACTION.READ,
  paginatedWorkbasketsSummary: { workbaskets: [], page: {}, _links: {} },
  workbasketAccessItems: null,
  workbasketDistributionTargets: mockDistributionTargets,
  availableDistributionTargets: mockAvailableDistributionTargets,
  distributionTargetsPage: 0,
  availableDistributionTargetsPage: 0,
  selectedComponent: WorkbasketComponent.INFORMATION,
  badgeMessage: '',
  button: undefined
};

describe('WorkbasketState', () => {
  let store: Store;
  let workbasketServiceMock: Partial<WorkbasketService>;
  let notificationServiceMock: Partial<NotificationService>;
  let requestInProgressServiceMock: Partial<RequestInProgressService>;
  let domainServiceMock: Partial<DomainService>;
  let locationMock: Partial<Location>;

  beforeEach(async () => {
    workbasketServiceMock = {
      getWorkBasket: vi.fn().mockReturnValue(of(mockWorkbasket)),
      getWorkBasketAccessItems: vi.fn().mockReturnValue(of(mockAccessItems)),
      removeDistributionTarget: vi.fn().mockReturnValue(of({})),
      getWorkBasketsDistributionTargets: vi.fn().mockReturnValue(of(mockDistributionTargets)),
      getWorkBasketsSummary: vi.fn().mockReturnValue(of(mockAvailableDistributionTargets)),
      updateWorkBasketsDistributionTargets: vi.fn().mockReturnValue(of(mockDistributionTargets)),
      updateWorkBasketAccessItem: vi.fn().mockReturnValue(of(mockAccessItems)),
      createWorkbasket: vi.fn().mockReturnValue(of(mockWorkbasket)),
      updateWorkbasket: vi.fn().mockReturnValue(of(mockWorkbasket)),
      markWorkbasketForDeletion: vi.fn().mockReturnValue(of({ status: 200 }))
    };

    notificationServiceMock = {
      showSuccess: vi.fn(),
      showError: vi.fn()
    };

    requestInProgressServiceMock = {
      setRequestInProgress: vi.fn()
    };

    domainServiceMock = {
      getSelectedDomain: vi.fn().mockReturnValue(of('DOMAIN_A')),
      getSelectedDomainValue: vi.fn().mockReturnValue('DOMAIN_A'),
      domainChangedComplete: vi.fn()
    };

    locationMock = {
      path: vi.fn().mockReturnValue('/workbaskets/(detail:WBI:001)?tab=information'),
      go: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [NgxsModule.forRoot([WorkbasketState, FilterState])],
      providers: [
        { provide: WorkbasketService, useValue: workbasketServiceMock },
        { provide: NotificationService, useValue: notificationServiceMock },
        { provide: RequestInProgressService, useValue: requestInProgressServiceMock },
        { provide: DomainService, useValue: domainServiceMock },
        { provide: Location, useValue: locationMock },
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: of({})
          }
        }
      ]
    }).compileComponents();

    store = TestBed.inject(Store);

    store.reset({
      ...store.snapshot(),
      workbasket: { ...initialWorkbasketState }
    });
  });

  describe('SelectWorkbasket', () => {
    it('should set selectedWorkbasket and action to READ when workbasketId is provided', async () => {
      const newWorkbasket = { ...mockWorkbasket, workbasketId: 'WBI:NEW', key: 'NEW-WB' };
      (workbasketServiceMock.getWorkBasket as ReturnType<typeof vi.fn>).mockReturnValue(of(newWorkbasket));
      (workbasketServiceMock.getWorkBasketAccessItems as ReturnType<typeof vi.fn>).mockReturnValue(of(mockAccessItems));
      (workbasketServiceMock.getWorkBasketsDistributionTargets as ReturnType<typeof vi.fn>).mockReturnValue(
        of(mockDistributionTargets)
      );
      (workbasketServiceMock.getWorkBasketsSummary as ReturnType<typeof vi.fn>).mockReturnValue(
        of(mockAvailableDistributionTargets)
      );

      await store.dispatch(new SelectWorkbasket('WBI:NEW')).toPromise();

      const state = store.snapshot().workbasket;
      expect(state.selectedWorkbasket).toEqual(newWorkbasket);
      expect(state.action).toBe(ACTION.READ);
    });

    it('should return of(null) when workbasketId is undefined', async () => {
      await store.dispatch(new SelectWorkbasket(undefined)).toPromise();

      const state = store.snapshot().workbasket;
      // State should remain unchanged - selectedWorkbasket stays as initial
      expect(state.selectedWorkbasket).toEqual(mockWorkbasket);
    });

    it('should call workbasketService.getWorkBasket with correct id', async () => {
      (workbasketServiceMock.getWorkBasketAccessItems as ReturnType<typeof vi.fn>).mockReturnValue(of(mockAccessItems));
      (workbasketServiceMock.getWorkBasketsDistributionTargets as ReturnType<typeof vi.fn>).mockReturnValue(
        of(mockDistributionTargets)
      );
      (workbasketServiceMock.getWorkBasketsSummary as ReturnType<typeof vi.fn>).mockReturnValue(
        of(mockAvailableDistributionTargets)
      );

      await store.dispatch(new SelectWorkbasket('WBI:001')).toPromise();

      expect(workbasketServiceMock.getWorkBasket).toHaveBeenCalledWith('WBI:001');
    });
  });

  describe('DeselectWorkbasket', () => {
    it('should clear selectedWorkbasket and set action to READ', async () => {
      await store.dispatch(new DeselectWorkbasket()).toPromise();

      const state = store.snapshot().workbasket;
      expect(state.selectedWorkbasket).toBeUndefined();
      expect(state.action).toBe(ACTION.READ);
    });

    it('should call location.go to clear workbasket from URL', async () => {
      await store.dispatch(new DeselectWorkbasket()).toPromise();

      expect(locationMock.go).toHaveBeenCalled();
    });
  });

  describe('SetActiveAction', () => {
    it('should set action to CREATE', async () => {
      await store.dispatch(new SetActiveAction(ACTION.CREATE)).toPromise();

      const state = store.snapshot().workbasket;
      expect(state.action).toBe(ACTION.CREATE);
    });

    it('should set action to COPY', async () => {
      await store.dispatch(new SetActiveAction(ACTION.COPY)).toPromise();

      const state = store.snapshot().workbasket;
      expect(state.action).toBe(ACTION.COPY);
    });

    it('should set action to READ', async () => {
      store.reset({ ...store.snapshot(), workbasket: { ...initialWorkbasketState, action: ACTION.CREATE } });

      await store.dispatch(new SetActiveAction(ACTION.READ)).toPromise();

      const state = store.snapshot().workbasket;
      expect(state.action).toBe(ACTION.READ);
    });
  });

  describe('SelectComponent', () => {
    it('should set selectedComponent to INFORMATION', async () => {
      store.reset({
        ...store.snapshot(),
        workbasket: { ...initialWorkbasketState, selectedComponent: WorkbasketComponent.ACCESS_ITEMS }
      });

      await store.dispatch(new SelectComponent(WorkbasketComponent.INFORMATION)).toPromise();

      const state = store.snapshot().workbasket;
      expect(state.selectedComponent).toBe(WorkbasketComponent.INFORMATION);
    });

    it('should set selectedComponent to ACCESS_ITEMS', async () => {
      await store.dispatch(new SelectComponent(WorkbasketComponent.ACCESS_ITEMS)).toPromise();

      const state = store.snapshot().workbasket;
      expect(state.selectedComponent).toBe(WorkbasketComponent.ACCESS_ITEMS);
    });

    it('should set selectedComponent to DISTRIBUTION_TARGETS', async () => {
      await store.dispatch(new SelectComponent(WorkbasketComponent.DISTRIBUTION_TARGETS)).toPromise();

      const state = store.snapshot().workbasket;
      expect(state.selectedComponent).toBe(WorkbasketComponent.DISTRIBUTION_TARGETS);
    });

    it('should update location when selecting INFORMATION component', async () => {
      await store.dispatch(new SelectComponent(WorkbasketComponent.INFORMATION)).toPromise();

      expect(locationMock.go).toHaveBeenCalled();
    });
  });

  describe('CopyWorkbasket', () => {
    it('should set action to COPY', async () => {
      await store.dispatch(new CopyWorkbasket(mockWorkbasket)).toPromise();

      const state = store.snapshot().workbasket;
      expect(state.action).toBe(ACTION.COPY);
    });

    it('should remove workbasketId from the copy', async () => {
      await store.dispatch(new CopyWorkbasket(mockWorkbasket)).toPromise();

      const state = store.snapshot().workbasket;
      expect(state.selectedWorkbasket.workbasketId).toBeUndefined();
    });

    it('should preserve key on copied workbasket', async () => {
      await store.dispatch(new CopyWorkbasket(mockWorkbasket)).toPromise();

      const state = store.snapshot().workbasket;
      expect(state.selectedWorkbasket.key).toBe(mockWorkbasket.key);
    });

    it('should set badgeMessage with the workbasket key', async () => {
      await store.dispatch(new CopyWorkbasket(mockWorkbasket)).toPromise();

      const state = store.snapshot().workbasket;
      expect(state.badgeMessage).toContain(mockWorkbasket.key);
    });
  });

  describe('CreateWorkbasket', () => {
    it('should set action to CREATE', async () => {
      await store.dispatch(new CreateWorkbasket()).toPromise();

      const state = store.snapshot().workbasket;
      expect(state.action).toBe(ACTION.CREATE);
    });

    it('should set selectedComponent to INFORMATION', async () => {
      store.reset({
        ...store.snapshot(),
        workbasket: { ...initialWorkbasketState, selectedComponent: WorkbasketComponent.ACCESS_ITEMS }
      });

      await store.dispatch(new CreateWorkbasket()).toPromise();

      const state = store.snapshot().workbasket;
      expect(state.selectedComponent).toBe(WorkbasketComponent.INFORMATION);
    });

    it('should create an empty workbasket with domain from DomainService', async () => {
      await store.dispatch(new CreateWorkbasket()).toPromise();

      const state = store.snapshot().workbasket;
      expect(state.selectedWorkbasket).toBeDefined();
      expect(state.selectedWorkbasket.domain).toBe('DOMAIN_A');
    });

    it('should set badgeMessage to creating new workbasket message', async () => {
      await store.dispatch(new CreateWorkbasket()).toPromise();

      const state = store.snapshot().workbasket;
      expect(state.badgeMessage).toContain('Creating new workbasket');
    });

    it('should initialize empty workbasketAccessItems', async () => {
      await store.dispatch(new CreateWorkbasket()).toPromise();

      const state = store.snapshot().workbasket;
      expect(state.workbasketAccessItems).toBeDefined();
      expect(state.workbasketAccessItems.accessItems).toEqual([]);
    });
  });

  describe('RemoveDistributionTarget', () => {
    it('should call workbasketService.removeDistributionTarget with correct url', async () => {
      const url = 'http://localhost:8080/kadai/api/v1/workbaskets/WBI:001/distribution-targets';

      await store.dispatch(new RemoveDistributionTarget(url)).toPromise();

      expect(workbasketServiceMock.removeDistributionTarget).toHaveBeenCalledWith(url);
    });

    it('should call notificationService.showSuccess after successful removal', async () => {
      const url = 'http://localhost:8080/kadai/api/v1/workbaskets/WBI:001/distribution-targets';

      await store.dispatch(new RemoveDistributionTarget(url)).toPromise();

      expect(notificationServiceMock.showSuccess).toHaveBeenCalledWith(
        'WORKBASKET_DISTRIBUTION_TARGET_REMOVE',
        expect.any(Object)
      );
    });
  });

  describe('GetWorkbasketAccessItems', () => {
    it('should call workbasketService.getWorkBasketAccessItems with correct url', async () => {
      const url = 'http://localhost:8080/kadai/api/v1/workbaskets/WBI:001/workbasketAccessItems';

      await store.dispatch(new GetWorkbasketAccessItems(url)).toPromise();

      expect(workbasketServiceMock.getWorkBasketAccessItems).toHaveBeenCalledWith(url);
    });

    it('should update workbasketAccessItems in state', async () => {
      const url = mockWorkbasket._links.accessItems.href;
      (workbasketServiceMock.getWorkBasketAccessItems as ReturnType<typeof vi.fn>).mockReturnValue(of(mockAccessItems));

      await store.dispatch(new GetWorkbasketAccessItems(url)).toPromise();

      const state = store.snapshot().workbasket;
      expect(state.workbasketAccessItems).toEqual(mockAccessItems);
    });
  });

  describe('TransferDistributionTargets', () => {
    it('should move workbaskets from distribution targets to available when targetSide is AVAILABLE', async () => {
      const workbasketToTransfer = mockDistributionTargets.distributionTargets[0];

      await store.dispatch(new TransferDistributionTargets(Side.AVAILABLE, [workbasketToTransfer])).toPromise();

      const state = store.snapshot().workbasket;
      const dtIds = state.workbasketDistributionTargets.distributionTargets.map((wb) => wb.workbasketId);
      expect(dtIds).not.toContain(workbasketToTransfer.workbasketId);

      const availableIds = state.availableDistributionTargets.workbaskets.map((wb) => wb.workbasketId);
      expect(availableIds).toContain(workbasketToTransfer.workbasketId);
    });

    it('should move workbaskets from available to distribution targets when targetSide is SELECTED', async () => {
      const workbasketToTransfer = mockAvailableDistributionTargets.workbaskets[0];

      await store.dispatch(new TransferDistributionTargets(Side.SELECTED, [workbasketToTransfer])).toPromise();

      const state = store.snapshot().workbasket;
      const availableIds = state.availableDistributionTargets.workbaskets.map((wb) => wb.workbasketId);
      expect(availableIds).not.toContain(workbasketToTransfer.workbasketId);

      const dtIds = state.workbasketDistributionTargets.distributionTargets.map((wb) => wb.workbasketId);
      expect(dtIds).toContain(workbasketToTransfer.workbasketId);
    });

    it('should handle transferring multiple workbaskets', async () => {
      const workbasketsToTransfer = mockDistributionTargets.distributionTargets;

      await store.dispatch(new TransferDistributionTargets(Side.AVAILABLE, workbasketsToTransfer)).toPromise();

      const state = store.snapshot().workbasket;
      expect(state.workbasketDistributionTargets.distributionTargets).toHaveLength(0);
    });
  });

  describe('OnButtonPressed', () => {
    it('should set button in state', async () => {
      await store.dispatch(new OnButtonPressed(ButtonAction.SAVE)).toPromise();

      const state = store.snapshot().workbasket;
      expect(state.button).toBe(ButtonAction.SAVE);
    });

    it('should set button to undefined when called with undefined', async () => {
      store.reset({ ...store.snapshot(), workbasket: { ...initialWorkbasketState, button: ButtonAction.SAVE } });

      await store.dispatch(new OnButtonPressed(undefined)).toPromise();

      const state = store.snapshot().workbasket;
      expect(state.button).toBeUndefined();
    });
  });

  describe('GetWorkbasketsSummary', () => {
    it('should call getWorkBasketsSummary and update paginatedWorkbasketsSummary', async () => {
      const filterParam: WorkbasketQueryFilterParameter = {};
      const sortParam = { 'sort-by': WorkbasketQuerySortParameter.NAME, order: Direction.ASC };
      const pageParam: QueryPagingParameter = { page: 1, 'page-size': 10 };

      await store.dispatch(new GetWorkbasketsSummary(true, filterParam, sortParam, pageParam)).toPromise();

      expect(workbasketServiceMock.getWorkBasketsSummary).toHaveBeenCalled();
      const state = store.snapshot().workbasket;
      expect(state.paginatedWorkbasketsSummary).toEqual(mockAvailableDistributionTargets);
    });

    it('should set paginatedWorkbasketsSummary to undefined before fetching', async () => {
      let capturedState: any;
      const originalGetSummary = workbasketServiceMock.getWorkBasketsSummary as ReturnType<typeof vi.fn>;
      originalGetSummary.mockImplementationOnce(() => {
        capturedState = store.snapshot().workbasket;
        return of(mockAvailableDistributionTargets);
      });

      const filterParam: WorkbasketQueryFilterParameter = {};
      const sortParam = { 'sort-by': WorkbasketQuerySortParameter.NAME, order: Direction.ASC };
      const pageParam: QueryPagingParameter = { page: 1, 'page-size': 10 };

      await store.dispatch(new GetWorkbasketsSummary(true, filterParam, sortParam, pageParam)).toPromise();

      expect(capturedState.paginatedWorkbasketsSummary).toBeUndefined();
    });
  });

  describe('SaveNewWorkbasket', () => {
    it('should call createWorkbasket with the provided workbasket', async () => {
      (workbasketServiceMock.getWorkBasketAccessItems as ReturnType<typeof vi.fn>).mockReturnValue(of(mockAccessItems));
      (workbasketServiceMock.getWorkBasketsDistributionTargets as ReturnType<typeof vi.fn>).mockReturnValue(
        of(mockDistributionTargets)
      );
      (workbasketServiceMock.getWorkBasketsSummary as ReturnType<typeof vi.fn>).mockReturnValue(
        of(mockAvailableDistributionTargets)
      );

      await store.dispatch(new SaveNewWorkbasket(mockWorkbasket)).toPromise();

      expect(workbasketServiceMock.createWorkbasket).toHaveBeenCalledWith(mockWorkbasket);
    });

    it('should show success notification after creating workbasket', async () => {
      (workbasketServiceMock.getWorkBasketAccessItems as ReturnType<typeof vi.fn>).mockReturnValue(of(mockAccessItems));
      (workbasketServiceMock.getWorkBasketsDistributionTargets as ReturnType<typeof vi.fn>).mockReturnValue(
        of(mockDistributionTargets)
      );
      (workbasketServiceMock.getWorkBasketsSummary as ReturnType<typeof vi.fn>).mockReturnValue(
        of(mockAvailableDistributionTargets)
      );

      await store.dispatch(new SaveNewWorkbasket(mockWorkbasket)).toPromise();

      expect(notificationServiceMock.showSuccess).toHaveBeenCalledWith('WORKBASKET_CREATE', {
        workbasketKey: mockWorkbasket.key
      });
    });
  });

  describe('UpdateWorkbasket', () => {
    it('should call updateWorkbasket service with url and workbasket', async () => {
      const url = mockWorkbasket._links.self.href;
      const updatedWorkbasket = { ...mockWorkbasket, name: 'Updated Name' };
      (workbasketServiceMock.updateWorkbasket as ReturnType<typeof vi.fn>).mockReturnValue(of(updatedWorkbasket));

      await store.dispatch(new UpdateWorkbasket(url, updatedWorkbasket)).toPromise();

      expect(workbasketServiceMock.updateWorkbasket).toHaveBeenCalledWith(url, updatedWorkbasket);
    });

    it('should show success notification after updating', async () => {
      const url = mockWorkbasket._links.self.href;
      (workbasketServiceMock.updateWorkbasket as ReturnType<typeof vi.fn>).mockReturnValue(of(mockWorkbasket));

      await store.dispatch(new UpdateWorkbasket(url, mockWorkbasket)).toPromise();

      expect(notificationServiceMock.showSuccess).toHaveBeenCalledWith('WORKBASKET_UPDATE', {
        workbasketKey: mockWorkbasket.key
      });
    });

    it('should update selectedWorkbasket in state after success', async () => {
      const url = mockWorkbasket._links.self.href;
      const updatedWorkbasket = { ...mockWorkbasket, name: 'Updated' };
      (workbasketServiceMock.updateWorkbasket as ReturnType<typeof vi.fn>).mockReturnValue(of(updatedWorkbasket));

      await store.dispatch(new UpdateWorkbasket(url, mockWorkbasket)).toPromise();

      const state = store.snapshot().workbasket;
      expect(state.selectedWorkbasket.name).toBe('Updated');
    });

    it('should call updateWorkbasketSummaryRepresentation to update workbaskets list', async () => {
      const url = mockWorkbasket._links.self.href;
      store.reset({
        ...store.snapshot(),
        workbasket: {
          ...initialWorkbasketState,
          paginatedWorkbasketsSummary: {
            workbaskets: [mockWorkbasket],
            page: {},
            _links: {}
          }
        }
      });
      (workbasketServiceMock.updateWorkbasket as ReturnType<typeof vi.fn>).mockReturnValue(of(mockWorkbasket));

      await store.dispatch(new UpdateWorkbasket(url, mockWorkbasket)).toPromise();

      const state = store.snapshot().workbasket;
      expect(state.paginatedWorkbasketsSummary.workbaskets.length).toBe(1);
      expect(state.paginatedWorkbasketsSummary.workbaskets[0].workbasketId).toBe(mockWorkbasket.workbasketId);
    });
  });

  describe('UpdateWorkbasketAccessItems', () => {
    it('should call updateWorkBasketAccessItem with url and access items', async () => {
      const url = mockWorkbasket._links.accessItems.href;
      const accessItems = mockAccessItems.accessItems;

      await store.dispatch(new UpdateWorkbasketAccessItems(url, accessItems)).toPromise();

      expect(workbasketServiceMock.updateWorkBasketAccessItem).toHaveBeenCalledWith(url, { accessItems });
    });

    it('should show success notification after updating access items', async () => {
      const url = mockWorkbasket._links.accessItems.href;

      await store.dispatch(new UpdateWorkbasketAccessItems(url, mockAccessItems.accessItems)).toPromise();

      expect(notificationServiceMock.showSuccess).toHaveBeenCalledWith(
        'WORKBASKET_ACCESS_ITEM_SAVE',
        expect.any(Object)
      );
    });

    it('should update workbasketAccessItems in state', async () => {
      const url = mockWorkbasket._links.accessItems.href;

      await store.dispatch(new UpdateWorkbasketAccessItems(url, mockAccessItems.accessItems)).toPromise();

      const state = store.snapshot().workbasket;
      expect(state.workbasketAccessItems).toEqual(mockAccessItems);
    });
  });

  describe('MarkWorkbasketForDeletion', () => {
    it('should call markWorkbasketForDeletion with the given url', async () => {
      const url = mockWorkbasket._links.self.href;
      (workbasketServiceMock.markWorkbasketForDeletion as ReturnType<typeof vi.fn>).mockReturnValue(
        of({ status: 200 })
      );

      await store.dispatch(new MarkWorkbasketForDeletion(url)).toPromise();

      expect(workbasketServiceMock.markWorkbasketForDeletion).toHaveBeenCalledWith(url);
    });

    it('should show success and deselect workbasket when status is not 202', async () => {
      const url = mockWorkbasket._links.self.href;
      (workbasketServiceMock.markWorkbasketForDeletion as ReturnType<typeof vi.fn>).mockReturnValue(
        of({ status: 200 })
      );

      await store.dispatch(new MarkWorkbasketForDeletion(url)).toPromise();

      expect(notificationServiceMock.showSuccess).toHaveBeenCalledWith('WORKBASKET_REMOVE', expect.any(Object));
    });

    it('should not show success when status is 202', async () => {
      const url = mockWorkbasket._links.self.href;
      (workbasketServiceMock.markWorkbasketForDeletion as ReturnType<typeof vi.fn>).mockReturnValue(
        of({ status: 202 })
      );

      await store.dispatch(new MarkWorkbasketForDeletion(url)).toPromise();

      expect(notificationServiceMock.showSuccess).not.toHaveBeenCalled();
    });
  });

  describe('FetchWorkbasketDistributionTargets', () => {
    it('should call getWorkBasketsDistributionTargets and update state', async () => {
      await store.dispatch(new FetchWorkbasketDistributionTargets(true)).toPromise();

      expect(workbasketServiceMock.getWorkBasketsDistributionTargets).toHaveBeenCalled();
      const state = store.snapshot().workbasket;
      expect(state.workbasketDistributionTargets).toBeDefined();
    });

    it('should set distributionTargetsPage to 1 when refetchAll is true', async () => {
      store.reset({ ...store.snapshot(), workbasket: { ...initialWorkbasketState, distributionTargetsPage: 0 } });

      await store.dispatch(new FetchWorkbasketDistributionTargets(true)).toPromise();

      const state = store.snapshot().workbasket;
      expect(state.distributionTargetsPage).toBe(1);
    });

    it('should concatenate existing targets when refetchAll is false', async () => {
      const extraTarget = { workbasketId: 'WBI:EXTRA', key: 'EXTRA', name: 'Extra WB' };
      (workbasketServiceMock.getWorkBasketsDistributionTargets as ReturnType<typeof vi.fn>).mockReturnValue(
        of({ distributionTargets: [extraTarget], _links: {} })
      );

      await store.dispatch(new FetchWorkbasketDistributionTargets(false)).toPromise();

      const state = store.snapshot().workbasket;
      expect(state.workbasketDistributionTargets.distributionTargets.length).toBeGreaterThan(0);
    });
  });

  describe('FetchAvailableDistributionTargets', () => {
    it('should call getWorkBasketsSummary and update availableDistributionTargets', async () => {
      store.reset({
        ...store.snapshot(),
        workbasket: {
          ...initialWorkbasketState,
          availableDistributionTargets: { workbaskets: [], page: { totalPages: 5 }, _links: {} },
          availableDistributionTargetsPage: 0
        }
      });

      await store.dispatch(new FetchAvailableDistributionTargets(true)).toPromise();

      expect(workbasketServiceMock.getWorkBasketsSummary).toHaveBeenCalled();
      const state = store.snapshot().workbasket;
      expect(state.availableDistributionTargets).toBeDefined();
    });

    it('should return of(null) when no more pages available', async () => {
      store.reset({
        ...store.snapshot(),
        workbasket: {
          ...initialWorkbasketState,
          availableDistributionTargets: { workbaskets: [], page: { totalPages: 1 }, _links: {} },
          availableDistributionTargetsPage: 2
        }
      });

      await store.dispatch(new FetchAvailableDistributionTargets(false)).toPromise();

      expect(workbasketServiceMock.getWorkBasketsSummary).not.toHaveBeenCalled();
    });

    it('should concatenate workbaskets when refetchAll is false', async () => {
      const extraWb = { workbasketId: 'WBI:NEWEXTRA', key: 'NEWEXTRA', name: 'New Extra WB' };
      (workbasketServiceMock.getWorkBasketsSummary as ReturnType<typeof vi.fn>).mockReturnValue(
        of({ workbaskets: [extraWb], page: { totalPages: 5 }, _links: {} })
      );
      store.reset({
        ...store.snapshot(),
        workbasket: {
          ...initialWorkbasketState,
          availableDistributionTargets: { workbaskets: [], page: { totalPages: 5 }, _links: {} },
          availableDistributionTargetsPage: 0,
          workbasketDistributionTargets: { distributionTargets: [], _links: {} }
        }
      });

      await store.dispatch(new FetchAvailableDistributionTargets(false)).toPromise();

      const state = store.snapshot().workbasket;
      expect(state.availableDistributionTargets.workbaskets.length).toBeGreaterThan(0);
    });
  });

  describe('InitializeStore', () => {
    it('should reset distribution target state when InitializeStore type is dispatched', async () => {
      await store.dispatch({ type: '[Workbasket] Initializing state' }).toPromise();
      const state = store.snapshot().workbasket;
      expect(state.workbasketDistributionTargets.distributionTargets).toEqual([]);
      expect(state.availableDistributionTargets.workbaskets).toEqual([]);
    });

    it('should select a component tab from queryParams when InitializeStore is dispatched', async () => {
      await store.dispatch({ type: '[Workbasket] Initializing state' }).toPromise();
      const state = store.snapshot().workbasket;
      expect(state.selectedComponent).toBeDefined();
    });
  });

  describe('ngxsAfterBootstrap', () => {
    it('should dispatch InitializeStore when ngxsAfterBootstrap is called', () => {
      const workbasketState = TestBed.inject(WorkbasketState);
      const mockCtx = { dispatch: vi.fn() };
      workbasketState.ngxsAfterBootstrap(mockCtx as any);
      expect(mockCtx.dispatch).toHaveBeenCalled();
    });
  });

  describe('UpdateWorkbasketDistributionTargets', () => {
    it('should call updateWorkBasketsDistributionTargets', async () => {
      await store.dispatch(new UpdateWorkbasketDistributionTargets()).toPromise();

      expect(workbasketServiceMock.updateWorkBasketsDistributionTargets).toHaveBeenCalled();
    });

    it('should show success notification after updating distribution targets', async () => {
      await store.dispatch(new UpdateWorkbasketDistributionTargets()).toPromise();

      expect(notificationServiceMock.showSuccess).toHaveBeenCalledWith(
        'WORKBASKET_DISTRIBUTION_TARGET_SAVE',
        expect.any(Object)
      );
    });

    it('should set requestInProgress to true then false', async () => {
      await store.dispatch(new UpdateWorkbasketDistributionTargets()).toPromise();

      expect(requestInProgressServiceMock.setRequestInProgress).toHaveBeenCalledWith(true);
      expect(requestInProgressServiceMock.setRequestInProgress).toHaveBeenCalledWith(false);
    });

    it('should call setRequestInProgress(false) when updateWorkBasketsDistributionTargets throws', async () => {
      (workbasketServiceMock.updateWorkBasketsDistributionTargets as ReturnType<typeof vi.fn>).mockReturnValue(
        throwError(() => new Error('update failed'))
      );
      try {
        await store.dispatch(new UpdateWorkbasketDistributionTargets()).toPromise();
      } catch {}
      expect(requestInProgressServiceMock.setRequestInProgress).toHaveBeenCalledWith(false);
    });
  });
});
