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

import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { of, BehaviorSubject, Subject } from 'rxjs';
import { Store } from '@ngxs/store';
import { Location } from '@angular/common';
import { KadaiTreeComponent } from './tree.component';
import { Classification } from '../../../shared/models/classification';
import { ClassificationTreeService } from '../../services/classification-tree.service';
import { ClassificationsService } from '../../../shared/services/classifications/classifications.service';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';

jest.mock('angular-svg-icon');

describe('KadaiTreeComponent', () => {
  let fixture: ComponentFixture<KadaiTreeComponent>;
  let debugElement: DebugElement;
  let component: KadaiTreeComponent;

  let mockStore: any;
  let mockLocation: any;
  let mockClassificationTreeService: any;
  let mockClassificationsService: any;
  let mockNotificationService: any;
  let mockRequestInProgressService: any;

  let categoryIcons$ = new BehaviorSubject<any>({
    EXTERNAL: 'external.svg',
    missing: 'missing.svg'
  });
  let selectedClassificationId$ = new BehaviorSubject<string | undefined>(undefined);
  let classifications$ = new BehaviorSubject<Classification[] | undefined>([]);
  let classificationTypeSelected$ = new Subject<string>();

  const makeNode = (id: string, dataExtras: any = {}) => {
    const node: any = {
      id,
      data: { classificationId: id, name: 'Node ' + id, key: 'KEY-' + id, category: 'EXTERNAL', ...dataExtras },
      children: [],
      setIsActive: jest.fn(),
      blur: jest.fn(),
      ensureVisible: jest.fn(),
      collapse: jest.fn()
    };
    return node;
  };

  let nodesMap: Record<string, any>;
  let activeNode: any | undefined;
  let visibleRoots: any[] = [];
  let filterPredicate: any;

  const mockTreeModel = {
    getActiveNode: jest.fn(() => activeNode),
    getNodeById: jest.fn((id: string) => nodesMap[id]),
    filterNodes: jest.fn((predicate: any) => {
      filterPredicate = predicate;
    }),
    getVisibleRoots: jest.fn(() => visibleRoots),
    collapseAll: jest.fn(),
    update: jest.fn()
  };

  const mockTree = { treeModel: mockTreeModel } as any;

  beforeEach(waitForAsync(() => {
    mockStore = {
      select: jest
        .fn()
        .mockImplementationOnce(() => categoryIcons$)
        .mockImplementationOnce(() => selectedClassificationId$)
        .mockImplementationOnce(() => classifications$)
        .mockImplementationOnce(() => classificationTypeSelected$),
      dispatch: jest.fn(() => of(undefined)),
      snapshot: jest.fn(() => ({}))
    };

    mockLocation = {
      path: jest.fn(() => '/administration/classifications'),
      go: jest.fn()
    };

    mockClassificationTreeService = {
      transformToTreeNode: jest.fn((cls: any[]) => cls as any)
    };

    mockClassificationsService = {
      getClassification: jest.fn((id: string) => of({ classificationId: id, key: 'KEY-' + id } as any))
    };

    mockNotificationService = {
      showSuccess: jest.fn()
    };

    mockRequestInProgressService = {
      setRequestInProgress: jest.fn()
    };

    TestBed.configureTestingModule({
      imports: [KadaiTreeComponent],
      providers: [
        { provide: Store, useValue: mockStore },
        { provide: Location, useValue: mockLocation },
        { provide: ClassificationTreeService, useValue: mockClassificationTreeService },
        { provide: ClassificationsService, useValue: mockClassificationsService },
        { provide: NotificationService, useValue: mockNotificationService },
        { provide: RequestInProgressService, useValue: mockRequestInProgressService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(KadaiTreeComponent);
    debugElement = fixture.debugElement;
    component = fixture.debugElement.componentInstance;

    (component as any).tree = mockTree;

    const parent = makeNode('P1');
    const child = makeNode('C1', { parentId: 'P1' });
    parent.children = [child];
    child.parentId = 'P1';
    nodesMap = { P1: parent, C1: child };
    activeNode = undefined;
    visibleRoots = [parent];

    fixture.detectChanges();
  }));

  afterEach(() => {});

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('ngOnInit should set treeNodes and selectNodeId and stop requestInProgress; schedule select when active differs', () => {
    jest.useFakeTimers();
    activeNode = { data: { classificationId: 'DIFF' } } as any;
    const selectSpy = jest.spyOn<any, any>(component as any, 'selectNode').mockImplementation(() => {});
    classifications$.next([{ classificationId: 'P1' } as any]);
    selectedClassificationId$.next('P1');

    jest.runAllTimers();

    expect(component.treeNodes).toEqual([{ classificationId: 'P1' }]);
    expect(component.selectNodeId).toBe('P1');
    expect(mockRequestInProgressService.setRequestInProgress).toHaveBeenCalledWith(false);
    expect(selectSpy).toHaveBeenCalledWith('P1');
    selectSpy.mockRestore();
    jest.useRealTimers();
  });

  it('should deselect active node on classification type change', () => {
    const active = makeNode('P1');
    activeNode = active;
    classificationTypeSelected$.next('DOC');
    expect(active.setIsActive).toHaveBeenCalledWith(false);
    expect(active.blur).toHaveBeenCalled();
  });

  it('should update categoryIcons and getCategoryIcon should resolve existing and missing', () => {
    categoryIcons$.next({ EXTERNAL: 'ext.svg', missing: 'miss.svg' });
    expect((component as any).categoryIcons).toEqual({ EXTERNAL: 'ext.svg', missing: 'miss.svg' });

    expect(component.getCategoryIcon('EXTERNAL')).toEqual({ left: 'ext.svg', right: 'EXTERNAL' });
    expect(component.getCategoryIcon('UNKNOWN')).toEqual({
      left: 'miss.svg',
      right: 'Category does not match with the configuration'
    });
  });

  it('ngAfterViewChecked should select node when selectNodeId present and no active node; ensure visibility and filter/manage tree', () => {
    const selectSpy = jest.spyOn<any, any>(component as any, 'selectNode').mockImplementation(() => {});
    component.selectNodeId = 'P1';
    activeNode = undefined;

    component.ngAfterViewChecked();
    expect(selectSpy).toHaveBeenCalledWith('P1');

    selectSpy.mockClear();
    (component as any).getNode = jest.fn(() => nodesMap['P1']);
    component.ngAfterViewChecked();
    expect(nodesMap['P1'].ensureVisible).toHaveBeenCalled();

    const filterNodesSpy = jest.spyOn<any, any>(component as any, 'filterNodes');
    const manageTreeStateSpy = jest.spyOn<any, any>(component as any, 'manageTreeState');
    component['filterTextOld'] = 'old';
    component['filterIconOld'] = 'OLD';
    component.filterText = 'new';
    component.filterIcon = 'NEW';
    component.ngAfterViewChecked();
    expect(filterNodesSpy).toHaveBeenCalledWith('new', 'NEW');
    expect(manageTreeStateSpy).toHaveBeenCalled();
    filterNodesSpy.mockRestore();
    manageTreeStateSpy.mockRestore();
    selectSpy.mockRestore();
  });

  it('onActivate should set selectNodeId, set request in progress, dispatch SelectClassification and update URL', () => {
    mockLocation.path.mockReturnValue('/administration/classifications/(detail:OLD)');
    const nodeRef = { node: { data: { classificationId: 'C1' } } } as any;
    component.onActivate(nodeRef);
    expect(component.selectNodeId).toBe('C1');
    expect(mockRequestInProgressService.setRequestInProgress).toHaveBeenCalledWith(true);
    expect(mockStore.dispatch).toHaveBeenCalled();
    expect(mockLocation.go).toHaveBeenCalled();
  });

  it('onDeactivate should dispatch DeselectClassification and reset URL when no active nodes', () => {
    const event = { treeModel: { activeNodes: [] } } as any;
    mockLocation.path.mockReturnValue('/administration/classifications/(detail:C1)');
    component.onDeactivate(event);
    expect(mockStore.dispatch).toHaveBeenCalled();
    expect(mockLocation.go).toHaveBeenCalledWith('/administration/classifications');
  });

  it('onMoveNode should fetch classification, update parent and collapse when parent has less than 2 children', async () => {
    const parentNode = makeNode('P1');
    parentNode.children = [makeNode('ONLY')];
    nodesMap['P1'] = parentNode;

    const evt: any = {
      node: { classificationId: 'C1', parentId: 'P1' },
      to: { parent: { classificationId: 'P1', key: 'PKEY' } }
    };

    const updateClassificationSpy = jest.spyOn<any, any>(component as any, 'updateClassification');

    const callsBefore = mockRequestInProgressService.setRequestInProgress.mock.calls.length;
    await component.onMoveNode(evt);
    const callsAfter = mockRequestInProgressService.setRequestInProgress.mock.calls.length;

    expect(callsAfter).toBe(callsBefore);
    expect((component as any).switchKadaiSpinnerEmit.emit).toBeDefined();
    expect(updateClassificationSpy).toHaveBeenCalledWith({
      classificationId: 'C1',
      key: 'KEY-C1',
      parentId: 'P1',
      parentKey: 'PKEY'
    });
    expect(mockTreeModel.update).toHaveBeenCalled();
    expect(nodesMap['P1'].collapse).toHaveBeenCalled();

    updateClassificationSpy.mockRestore();
  });

  it('onDrop should set parent to root and update when dropped on viewport', async () => {
    const evt: any = {
      event: { target: { tagName: 'TREE-VIEWPORT' } },
      element: { data: { classificationId: 'C1', parentId: 'P1' } }
    };

    const updateClassificationSpy = jest.spyOn<any, any>(component as any, 'updateClassification');

    await component.onDrop(evt);

    expect(updateClassificationSpy).toHaveBeenCalledWith({
      classificationId: 'C1',
      key: 'KEY-C1',
      parentId: '',
      parentKey: ''
    });
  });

  it('switchKadaiSpinner should emit value', () => {
    const spy = jest.spyOn(component.switchKadaiSpinnerEmit, 'emit');
    component.switchKadaiSpinner(true);
    expect(spy).toHaveBeenCalledWith(true);
    spy.mockRestore();
  });

  it('ngOnDestroy should complete destroy$', () => {
    const destroy$ = (component as any).destroy$;
    const nextSpy = jest.spyOn(destroy$, 'next');
    const completeSpy = jest.spyOn(destroy$, 'complete');
    component.ngOnDestroy();
    expect(nextSpy).toHaveBeenCalled();
    expect(completeSpy).toHaveBeenCalled();
    nextSpy.mockRestore();
    completeSpy.mockRestore();
  });

  it('checkValidElements should validate allowed click targets inside component', () => {
    (component as any).elementRef = { nativeElement: { contains: () => true } };
    const isValid = (component as any).checkValidElements({ target: { localName: 'tree-viewport' } });
    expect(isValid).toBe(true);
    const isInvalid = (component as any).checkValidElements({ target: { localName: 'div' } });
    expect(isInvalid).toBe(false);
  });

  it('filterNodes should combine name/key and icon filtering and set flags', () => {
    const spyCheckNameAndKey = jest.spyOn<any, any>(component as any, 'checkNameAndKey');
    const spyCheckIcon = jest.spyOn<any, any>(component as any, 'checkIcon');
    visibleRoots = [];

    (component as any).filterNodes('Node', 'EXTERNAL');

    expect(mockTreeModel.filterNodes).toHaveBeenCalled();
    const aNode = makeNode('X1');
    filterPredicate(aNode);
    expect(spyCheckNameAndKey).toHaveBeenCalledWith(aNode, 'Node');
    expect(spyCheckIcon).toHaveBeenCalledWith(aNode, 'EXTERNAL');

    expect(component['filter']).toBe('Node');
    expect(component['category']).toBe('EXTERNAL');
    expect(component.emptyTreeNodes).toBe(true);

    spyCheckNameAndKey.mockRestore();
    spyCheckIcon.mockRestore();
  });

  it('manageTreeState should collapse all and check filterText empty branch', () => {
    mockTreeModel.collapseAll.mockClear();
    component.filterText = '' as any;
    (component as any).manageTreeState();
    expect(mockTreeModel.collapseAll).toHaveBeenCalledTimes(2);
  });

  it('selectNode should activate node when it exists', () => {
    const sel = makeNode('P1');
    nodesMap['P1'] = sel;
    (component as any).selectNode('P1');
    expect(sel.setIsActive).toHaveBeenCalledWith(true);
  });

  it('deselectActiveNode should clear selectNodeId and deactivate/blur active node', () => {
    const active = makeNode('P1');
    activeNode = active;
    component['selectNodeId'] = 'P1' as any;
    (component as any).deselectActiveNode();
    expect(component['selectNodeId']).toBeUndefined();
    expect(active.setIsActive).toHaveBeenCalledWith(false);
    expect(active.blur).toHaveBeenCalled();
  });

  it('getClassification should return a classification via service (toPromise path)', async () => {
    const result = await (component as any)['getClassification']('X1');
    expect(result).toEqual({ classificationId: 'X1', key: 'KEY-X1' });
  });

  it('updateClassification should dispatch, show notification and stop spinner', (done) => {
    const classification: any = { classificationId: 'ID', key: 'KEY' };
    const emitSpy = jest.spyOn(component.switchKadaiSpinnerEmit, 'emit');
    (component as any)['updateClassification'](classification);

    setTimeout(() => {
      expect(mockStore.dispatch).toHaveBeenCalled();
      expect(mockNotificationService.showSuccess).toHaveBeenCalledWith('CLASSIFICATION_MOVE', {
        classificationKey: 'KEY'
      });
      expect(emitSpy).toHaveBeenCalledWith(false);
      emitSpy.mockRestore();
      done();
    }, 0);
  });

  it('onDocumentClick should deselect active node when clicking on valid elements', () => {
    (component as any).elementRef = { nativeElement: { contains: () => true } };
    const active = makeNode('P1');
    activeNode = active;
    component.selectNodeId = 'P1';
    component.onDocumentClick({ target: { localName: 'tree-viewport' } } as any);
    expect(component.selectNodeId).toBeUndefined();
    expect(active.setIsActive).toHaveBeenCalledWith(false);
  });

  it('collapseParentNodeIfItIsTheLastChild should update and collapse when only one child; not when more', () => {
    const parent1 = makeNode('P2');
    parent1.children = [makeNode('K1')];
    nodesMap['P2'] = parent1;
    (component as any)['collapseParentNodeIfItIsTheLastChild']({ parentId: 'P2' });
    expect(mockTreeModel.update).toHaveBeenCalled();
    expect(parent1.collapse).toHaveBeenCalled();

    const parent2 = makeNode('P3');
    parent2.children = [makeNode('K1'), makeNode('K2')];
    nodesMap['P3'] = parent2;
    (component as any)['collapseParentNodeIfItIsTheLastChild']({ parentId: 'P3' });
    expect(parent2.collapse).not.toHaveBeenCalled();
  });
});
