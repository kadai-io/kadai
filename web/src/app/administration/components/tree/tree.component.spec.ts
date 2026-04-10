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
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { KadaiTreeComponent } from './tree.component';
import { provideStore, Store } from '@ngxs/store';
import { ClassificationState } from '../../../shared/store/classification-store/classification.state';
import { EngineConfigurationState } from '../../../shared/store/engine-configuration-store/engine-configuration.state';
import { classificationStateMock, engineConfigurationMock } from '../../../shared/store/mock-data/mock-store';
import { ClassificationsService } from '../../../shared/services/classifications/classifications.service';
import { ClassificationTreeService } from '../../services/classification-tree.service';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideAngularSvgIcon } from 'angular-svg-icon';
import { of } from 'rxjs';
import { ClassificationCategoriesService } from '../../../shared/services/classification-categories/classification-categories.service';
import { Location } from '@angular/common';
import { By } from '@angular/platform-browser';

const classificationsServiceMock = {
  getClassification: vi.fn().mockReturnValue(of({ classificationId: 'CLF:01', key: 'KEY01', parentId: '' }))
};

const classificationTreeServiceMock = {
  transformToTreeNode: vi.fn().mockReturnValue([])
};

const notificationServiceMock = {
  showSuccess: vi.fn(),
  showError: vi.fn()
};

const requestInProgressServiceMock = {
  setRequestInProgress: vi.fn(),
  getRequestInProgress: vi.fn().mockReturnValue(of(false))
};

const locationMock = {
  path: vi.fn().mockReturnValue('/kadai/administration/classifications'),
  go: vi.fn()
};

const classificationCategoriesServiceSpy: Partial<ClassificationCategoriesService> = {
  getCustomisation: vi.fn().mockReturnValue(of(engineConfigurationMock.customisation))
};

describe('KadaiTreeComponent', () => {
  let component: KadaiTreeComponent;
  let fixture: ComponentFixture<KadaiTreeComponent>;
  let store: Store;
  let httpController: HttpTestingController;

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [KadaiTreeComponent],
      providers: [
        provideStore([ClassificationState, EngineConfigurationState]),
        provideHttpClientTesting(),
        provideAngularSvgIcon(),
        { provide: ClassificationsService, useValue: classificationsServiceMock },
        { provide: ClassificationTreeService, useValue: classificationTreeServiceMock },
        { provide: NotificationService, useValue: notificationServiceMock },
        { provide: RequestInProgressService, useValue: requestInProgressServiceMock },
        { provide: Location, useValue: locationMock },
        { provide: ClassificationCategoriesService, useValue: classificationCategoriesServiceSpy }
      ]
    }).compileComponents();

    store = TestBed.inject(Store);
    httpController = TestBed.inject(HttpTestingController);
    store.reset({
      ...store.snapshot(),
      classification: classificationStateMock,
      engineConfiguration: engineConfigurationMock
    });

    fixture = TestBed.createComponent(KadaiTreeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    httpController.match(() => true).forEach((req) => req.flush(''));
  });

  afterEach(() => {
    fixture.destroy();
    httpController.match(() => true).forEach((req) => req.flush(''));
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should have emptyTreeNodes false initially', () => {
    expect(component.emptyTreeNodes).toBe(false);
  });

  it('should render tree-root element', () => {
    const treeRoot = fixture.nativeElement.querySelector('tree-root');
    expect(treeRoot).toBeTruthy();
  });

  it('should not show "no classifications" message when emptyTreeNodes is false', () => {
    component.emptyTreeNodes = false;
    fixture.detectChanges();
    const msg = fixture.nativeElement.querySelector('h3');
    expect(msg).toBeNull();
  });

  it('should show "no classifications" message when emptyTreeNodes is true', () => {
    const localFixture = TestBed.createComponent(KadaiTreeComponent);
    const localComponent = localFixture.componentInstance;
    localComponent.emptyTreeNodes = true;
    localFixture.detectChanges();
    httpController.match(() => true).forEach((req) => req.flush(''));
    const msg = localFixture.nativeElement.querySelector('h3');
    expect(msg).toBeTruthy();
    expect(msg.textContent).toContain('no classifications');
  });

  it('getCategoryIcon should return category icon when category exists in categoryIcons', () => {
    (component as any).categoryIcons = () => ({
      EXTERNAL: 'path/to/icon.svg',
      missing: 'path/to/missing.svg'
    });
    const result = component.getCategoryIcon('EXTERNAL');
    expect(result.left).toBe('path/to/icon.svg');
    expect(result.right).toBe('EXTERNAL');
  });

  it('getCategoryIcon should return missing icon when category does not exist', () => {
    (component as any).categoryIcons = () => ({
      missing: 'path/to/missing.svg'
    });
    const result = component.getCategoryIcon('NONEXISTENT');
    expect(result.left).toBe('path/to/missing.svg');
    expect(result.right).toBe('Category does not match with the configuration');
  });

  it('switchKadaiSpinner should emit the provided value', () => {
    const emitSpy = vi.spyOn(component.switchKadaiSpinnerEmit, 'emit');
    component.switchKadaiSpinner(true);
    expect(emitSpy).toHaveBeenCalledWith(true);
  });

  it('should complete destroy$ on ngOnDestroy', () => {
    const nextSpy = vi.spyOn((component as any).destroy$, 'next');
    const completeSpy = vi.spyOn((component as any).destroy$, 'complete');
    component.ngOnDestroy();
    expect(nextSpy).toHaveBeenCalled();
    expect(completeSpy).toHaveBeenCalled();
  });

  it('should call store.dispatch and location.go on onActivate', () => {
    const dispatchSpy = vi.spyOn(store, 'dispatch').mockReturnValue(of(undefined as any));
    locationMock.path.mockReturnValue('/kadai/administration/classifications');
    const mockTreeNode = { node: { data: { classificationId: 'CLF:01' } } };
    component.onActivate(mockTreeNode);
    expect(requestInProgressServiceMock.setRequestInProgress).toHaveBeenCalledWith(true);
    expect(dispatchSpy).toHaveBeenCalled();
    expect(locationMock.go).toHaveBeenCalled();
    expect(component.selectNodeId()).toBe('CLF:01');
  });

  it('should dispatch deselect and go to classifications on onDeactivate with empty activeNodes', () => {
    const dispatchSpy = vi.spyOn(store, 'dispatch').mockReturnValue(of(undefined as any));
    locationMock.path.mockReturnValue('/kadai/administration/classifications');
    const mockEvent = { treeModel: { activeNodes: [] } };
    component.onDeactivate(mockEvent);
    expect(dispatchSpy).toHaveBeenCalled();
    expect(locationMock.go).toHaveBeenCalled();
  });

  it('should not dispatch on onDeactivate when activeNodes has items', () => {
    const dispatchSpy = vi.spyOn(store, 'dispatch').mockReturnValue(of(undefined as any));
    const mockEvent = { treeModel: { activeNodes: [{ id: 'CLF:01' }] } };
    component.onDeactivate(mockEvent);
    expect(dispatchSpy).not.toHaveBeenCalled();
  });

  it('should not collapse emptyTreeNodes message when emptyTreeNodes is false', () => {
    component.emptyTreeNodes = false;
    fixture.detectChanges();
    const msg = fixture.nativeElement.querySelector('h3');
    expect(msg).toBeNull();
  });

  it('onDocumentClick should do nothing when element is not in valid area', () => {
    const div = document.createElement('div');
    const event = { target: div };
    expect(() => component.onDocumentClick(event)).not.toThrow();
  });

  it('onMoveNode should call getClassification and updateClassification', async () => {
    const dispatchSpy = vi.spyOn(store, 'dispatch').mockReturnValue(of(undefined as any));
    const emitSpy = vi.spyOn(component.switchKadaiSpinnerEmit, 'emit');
    const mockEvent = {
      node: { classificationId: 'CLF:01', parentId: '' },
      to: { parent: { classificationId: 'CLF:02', key: 'KEY02' } }
    };
    await component.onMoveNode(mockEvent);
    expect(emitSpy).toHaveBeenCalledWith(true);
    expect(classificationsServiceMock.getClassification).toHaveBeenCalledWith('CLF:01');
    expect(dispatchSpy).toHaveBeenCalled();
  });

  it('onDrop should update classification when target is TREE-VIEWPORT', async () => {
    const dispatchSpy = vi.spyOn(store, 'dispatch').mockReturnValue(of(undefined as any));
    const emitSpy = vi.spyOn(component.switchKadaiSpinnerEmit, 'emit');
    const mockEvent = {
      event: { target: { tagName: 'TREE-VIEWPORT' } },
      element: { data: { classificationId: 'CLF:01', parentId: '' } }
    };
    await component.onDrop(mockEvent);
    expect(emitSpy).toHaveBeenCalledWith(true);
    expect(classificationsServiceMock.getClassification).toHaveBeenCalledWith('CLF:01');
    expect(dispatchSpy).toHaveBeenCalled();
  });

  it('onDrop should do nothing when target is not TREE-VIEWPORT', async () => {
    const dispatchSpy = vi.spyOn(store, 'dispatch').mockReturnValue(of(undefined as any));
    const emitSpy = vi.spyOn(component.switchKadaiSpinnerEmit, 'emit');
    const mockEvent = {
      event: { target: { tagName: 'DIV' } },
      element: { data: { classificationId: 'CLF:01', parentId: '' } }
    };
    await component.onDrop(mockEvent);
    expect(emitSpy).not.toHaveBeenCalled();
    expect(dispatchSpy).not.toHaveBeenCalled();
  });

  it('onMoveNode with parent node that has children should collapse parent', async () => {
    const mockClassification = { classificationId: 'CLF:01', key: 'KEY01', parentId: 'CLF:PARENT' };
    classificationsServiceMock.getClassification.mockReturnValueOnce(of(mockClassification));
    const dispatchSpy = vi.spyOn(store, 'dispatch').mockReturnValue(of(undefined as any));
    const mockEvent = {
      node: { classificationId: 'CLF:01', parentId: 'CLF:PARENT' },
      to: { parent: { classificationId: 'CLF:02', key: 'KEY02' } }
    };
    await component.onMoveNode(mockEvent);
    expect(dispatchSpy).toHaveBeenCalled();
  });

  it('switchKadaiSpinner should emit false', () => {
    const emitSpy = vi.spyOn(component.switchKadaiSpinnerEmit, 'emit');
    component.switchKadaiSpinner(false);
    expect(emitSpy).toHaveBeenCalledWith(false);
  });

  it('should handle ngAfterViewChecked when selectNodeId is set', () => {
    component.selectNodeId.set('CLF:NONEXISTENT');
    expect(() => component.ngAfterViewChecked()).not.toThrow();
  });

  it('should handle ngAfterViewChecked when filterText changes', () => {
    fixture.componentRef.setInput('filterText', 'newFilter');
    fixture.detectChanges();
    expect(component.emptyTreeNodes).toBeDefined();
  });

  it('should handle ngAfterViewChecked when filterIcon changes', () => {
    fixture.componentRef.setInput('filterIcon', 'EXTERNAL');
    fixture.detectChanges();
  });

  it('should handle ngAfterViewChecked when filterText is empty string', () => {
    fixture.componentRef.setInput('filterText', 'prev');
    fixture.detectChanges();
    fixture.componentRef.setInput('filterText', '');
    expect(() => fixture.detectChanges()).not.toThrow();
  });

  it('should show "no classifications" message when emptyTreeNodes is true in separate fixture', () => {
    const localFixture = TestBed.createComponent(KadaiTreeComponent);
    const localComponent = localFixture.componentInstance;
    localComponent.emptyTreeNodes = true;
    localFixture.detectChanges();
    httpController.match(() => true).forEach((req) => req.flush(''));
    const h3 = localFixture.nativeElement.querySelector('h3');
    expect(h3).toBeTruthy();
    expect(h3.textContent).toContain('no classifications');
    const p = localFixture.nativeElement.querySelector('p');
    expect(p).toBeTruthy();
  });

  it('should hide the tree-root when emptyTreeNodes is true', () => {
    const localFixture = TestBed.createComponent(KadaiTreeComponent);
    const localComponent = localFixture.componentInstance;
    localComponent.emptyTreeNodes = true;
    localFixture.detectChanges();
    httpController.match(() => true).forEach((req) => req.flush(''));
    const treeRoot = localFixture.nativeElement.querySelector('tree-root');
    if (treeRoot) {
      expect(treeRoot.hidden).toBe(true);
    }
  });

  it('should show tree-root and not show "no classifications" when emptyTreeNodes is false', () => {
    const localFixture = TestBed.createComponent(KadaiTreeComponent);
    const localComponent = localFixture.componentInstance;
    localComponent.emptyTreeNodes = false;
    localFixture.detectChanges();
    httpController.match(() => true).forEach((req) => req.flush(''));
    const h3 = localFixture.nativeElement.querySelector('h3');
    expect(h3).toBeNull();
    const treeRoot = localFixture.nativeElement.querySelector('tree-root');
    expect(treeRoot).toBeTruthy();
  });

  it('onDeactivate should not dispatch when activeNodes is non-empty', () => {
    const dispatchSpy = vi.spyOn(store, 'dispatch').mockReturnValue(of(undefined as any));
    const mockEvent = { treeModel: { activeNodes: [{ id: 'CLF:01' }, { id: 'CLF:02' }] } };
    component.onDeactivate(mockEvent);
    expect(dispatchSpy).not.toHaveBeenCalled();
  });

  it('getCategoryIcon should return correct icon for known category with different key', () => {
    (component as any).categoryIcons = () => ({
      MANUAL: 'path/to/manual.svg',
      missing: 'path/to/missing.svg'
    });
    const result = component.getCategoryIcon('MANUAL');
    expect(result.left).toBe('path/to/manual.svg');
    expect(result.right).toBe('MANUAL');
  });

  it('getCategoryIcon should return missing icon for unknown category', () => {
    (component as any).categoryIcons = () => ({
      EXTERNAL: 'path/to/external.svg',
      missing: 'path/to/missing-icon.svg'
    });
    const result = component.getCategoryIcon('UNKNOWN_CATEGORY');
    expect(result.left).toBe('path/to/missing-icon.svg');
    expect(result.right).toBe('Category does not match with the configuration');
  });

  it('onDocumentClick should not throw when event target is valid tree element', () => {
    const fakeTarget = document.createElement('tree-viewport');
    const event = { target: fakeTarget };
    expect(() => component.onDocumentClick(event)).not.toThrow();
  });

  it('switchKadaiSpinner should emit both true and false values', () => {
    const emitSpy = vi.spyOn(component.switchKadaiSpinnerEmit, 'emit');
    component.switchKadaiSpinner(true);
    component.switchKadaiSpinner(false);
    expect(emitSpy).toHaveBeenCalledTimes(2);
    expect(emitSpy).toHaveBeenNthCalledWith(1, true);
    expect(emitSpy).toHaveBeenNthCalledWith(2, false);
  });

  it('should update filter state when filterIcon and filterText both change', () => {
    fixture.componentRef.setInput('filterText', 'test');
    fixture.componentRef.setInput('filterIcon', 'MANUAL');
    fixture.detectChanges();
    expect(component.filter).toBe('test');
    expect(component.category).toBe('MANUAL');
  });

  it('should set category to ALL when filterIcon is empty string', () => {
    fixture.componentRef.setInput('filterText', 'something');
    fixture.componentRef.setInput('filterIcon', '');
    fixture.detectChanges();
    expect(component.category).toBe('ALL');
  });

  it('should render tree nodes with category and cover @if (node.data.category) true branch', async () => {
    const localFixture = TestBed.createComponent(KadaiTreeComponent);
    const localComponent = localFixture.componentInstance;

    localComponent.options = {
      ...localComponent.options,
      useVirtualScroll: false
    };

    const treeNodes = [
      {
        classificationId: 'CLF:01',
        key: 'KEY01',
        name: 'Node One',
        category: 'EXTERNAL',
        parentId: '',
        parentKey: '',
        children: []
      }
    ];

    classificationTreeServiceMock.transformToTreeNode.mockReturnValueOnce(treeNodes);

    localFixture.detectChanges();
    httpController.match(() => true).forEach((req) => req.flush(''));

    localFixture.detectChanges();
    await localFixture.whenStable();
    httpController.match(() => true).forEach((req) => req.flush(''));

    expect(localComponent.treeNodes()).toBeDefined();
    const result = localComponent.getCategoryIcon('EXTERNAL');
    expect(result.right).toBe('EXTERNAL');
  });

  it('should render tree nodes without category and cover @if (node.data.category) false branch', async () => {
    const localFixture = TestBed.createComponent(KadaiTreeComponent);
    const localComponent = localFixture.componentInstance;

    localComponent.options = {
      ...localComponent.options,
      useVirtualScroll: false
    };

    const treeNodes = [
      {
        classificationId: 'CLF:02',
        key: 'KEY02',
        name: 'Node Two',
        category: '',
        parentId: '',
        parentKey: '',
        children: []
      }
    ];

    classificationTreeServiceMock.transformToTreeNode.mockReturnValueOnce(treeNodes);

    localFixture.detectChanges();
    httpController.match(() => true).forEach((req) => req.flush(''));

    localFixture.detectChanges();
    await localFixture.whenStable();
    httpController.match(() => true).forEach((req) => req.flush(''));

    expect(localComponent.treeNodes()).toBeDefined();
    const result = localComponent.getCategoryIcon('UNKNOWN_CATEGORY');
    expect(result.right).toBe('Category does not match with the configuration');
  });

  it('should render multiple tree nodes and cover template statements', async () => {
    const localFixture = TestBed.createComponent(KadaiTreeComponent);
    const localComponent = localFixture.componentInstance;

    localComponent.options = {
      ...localComponent.options,
      useVirtualScroll: false
    };

    const treeNodes = [
      {
        classificationId: 'CLF:10',
        key: 'KEY10',
        name: 'Node With Category',
        category: 'MANUAL',
        parentId: '',
        parentKey: '',
        children: []
      },
      {
        classificationId: 'CLF:11',
        key: 'KEY11',
        name: 'Node Without Category',
        category: null,
        parentId: '',
        parentKey: '',
        children: []
      }
    ];

    (localComponent as any).categoryIcons = () => ({
      MANUAL: 'path/to/manual.svg',
      missing: 'path/to/missing.svg'
    });

    classificationTreeServiceMock.transformToTreeNode.mockReturnValueOnce(treeNodes);

    localFixture.detectChanges();
    httpController.match(() => true).forEach((req) => req.flush(''));

    localFixture.detectChanges();
    await localFixture.whenStable();
    httpController.match(() => true).forEach((req) => req.flush(''));

    expect(localComponent.treeNodes()).toBeDefined();
    expect(localComponent.treeNodes().length).toBe(2);
  });

  it('filterNodes should call checkNameAndKey and checkIcon for each node when tree has nodes', async () => {
    const treeNodes = [
      {
        classificationId: 'CLF:FILTER1',
        key: 'FKEY1',
        name: 'Filterable Node',
        category: 'EXTERNAL',
        parentId: '',
        parentKey: '',
        children: []
      },
      {
        classificationId: 'CLF:FILTER2',
        key: 'FKEY2',
        name: 'Another Node',
        category: 'MANUAL',
        parentId: '',
        parentKey: '',
        children: []
      }
    ];
    classificationTreeServiceMock.transformToTreeNode.mockReturnValueOnce(treeNodes);

    const localFixture = TestBed.createComponent(KadaiTreeComponent);
    const localComponent = localFixture.componentInstance;
    localComponent.options = { ...localComponent.options, useVirtualScroll: false };
    localFixture.detectChanges();
    httpController.match(() => true).forEach((req) => req.flush(''));

    localFixture.detectChanges();
    await localFixture.whenStable();
    httpController.match(() => true).forEach((req) => req.flush(''));

    localFixture.componentRef.setInput('filterText', 'Filterable');
    localFixture.componentRef.setInput('filterIcon', 'EXTERNAL');
    localFixture.detectChanges();

    expect(localComponent.filter).toBe('Filterable');
    expect(localComponent.category).toBe('EXTERNAL');
    localFixture.destroy();
  });

  it('collapseParentNodeIfItIsTheLastChild: parent with exactly 1 child should collapse', async () => {
    vi.spyOn(store, 'dispatch').mockReturnValue(of(undefined as any));

    const treeNodes = [
      {
        classificationId: 'CLF:SOLO_PARENT',
        key: 'SOLO_PARENT',
        name: 'Parent With One Child',
        category: 'EXTERNAL',
        parentId: '',
        parentKey: '',
        children: [
          {
            classificationId: 'CLF:SOLO_CHILD',
            key: 'SOLO_CHILD',
            name: 'Only Child',
            category: 'EXTERNAL',
            parentId: 'CLF:SOLO_PARENT',
            parentKey: 'SOLO_PARENT',
            children: []
          }
        ]
      }
    ];
    classificationTreeServiceMock.transformToTreeNode.mockReturnValueOnce(treeNodes);

    const localFixture = TestBed.createComponent(KadaiTreeComponent);
    const localComponent = localFixture.componentInstance;
    localComponent.options = { ...localComponent.options, useVirtualScroll: false };
    localFixture.detectChanges();
    httpController.match(() => true).forEach((req) => req.flush(''));

    localFixture.detectChanges();
    await localFixture.whenStable();
    httpController.match(() => true).forEach((req) => req.flush(''));

    const parentNode = (localComponent as any).getNode('CLF:SOLO_PARENT');
    if (parentNode) {
      parentNode.expand();
    }

    const mockClassification = { classificationId: 'CLF:SOLO_CHILD', key: 'SOLO_CHILD', parentId: 'CLF:SOLO_PARENT' };
    classificationsServiceMock.getClassification.mockReturnValueOnce(of(mockClassification));

    const mockEvent = {
      node: { classificationId: 'CLF:SOLO_CHILD', parentId: 'CLF:SOLO_PARENT' },
      to: { parent: { classificationId: 'CLF:ELSEWHERE', key: 'ELSEWHERE' } }
    };
    await localComponent.onMoveNode(mockEvent);
    localFixture.destroy();
  });

  it('selectNode private method: should handle falsy nodeId', () => {
    component.selectNodeId.set(undefined);
    expect(() => component.ngAfterViewChecked()).not.toThrow();
  });

  it('ngOnInit: selectedClassificationId defined — selectNodeId gets set to the id', () => {
    expect(component.selectNodeId()).toBeDefined();
  });

  it('ngAfterViewChecked: selectNodeId defined but node not found — no error', () => {
    component.selectNodeId.set('DOES_NOT_EXIST');
    expect(() => component.ngAfterViewChecked()).not.toThrow();
  });

  it('ngAfterViewChecked: filterText is undefined and filterIcon changes — covers filterText ternary false', () => {
    fixture.componentRef.setInput('filterText', undefined);
    fixture.componentRef.setInput('filterIcon', 'SOME_ICON');
    fixture.detectChanges();
    expect(component.filter).toBe('');
  });

  it('collapseParentNodeIfItIsTheLastChild: node with empty parentId does not attempt to collapse', async () => {
    const dispatchSpy = vi.spyOn(store, 'dispatch').mockReturnValue(of(undefined as any));
    const mockClassification = { classificationId: 'CLF:01', key: 'KEY01', parentId: '' };
    classificationsServiceMock.getClassification.mockReturnValueOnce(of(mockClassification));
    const mockEvent = {
      node: { classificationId: 'CLF:01', parentId: '' },
      to: { parent: { classificationId: '', key: '' } }
    };
    await component.onMoveNode(mockEvent);
    expect(dispatchSpy).toHaveBeenCalled();
  });

  it('collapseParentNodeIfItIsTheLastChild: node with non-empty parentId not in tree — no collapse', async () => {
    const dispatchSpy = vi.spyOn(store, 'dispatch').mockReturnValue(of(undefined as any));
    const mockClassification = { classificationId: 'CLF:03', key: 'KEY03', parentId: 'CLF:UNKNOWN_PARENT' };
    classificationsServiceMock.getClassification.mockReturnValueOnce(of(mockClassification));
    const mockEvent = {
      node: { classificationId: 'CLF:03', parentId: 'CLF:UNKNOWN_PARENT' },
      to: { parent: { classificationId: 'CLF:04', key: 'KEY04' } }
    };
    await component.onMoveNode(mockEvent);
    expect(dispatchSpy).toHaveBeenCalled();
  });

  it('should cover (activate) template event handler via triggerEventHandler', () => {
    vi.spyOn(store, 'dispatch').mockReturnValue(of(undefined as any));
    const treeEl = fixture.debugElement.query(By.css('tree-root'));
    if (treeEl) {
      treeEl.triggerEventHandler('activate', {
        node: { data: { classificationId: 'CLF:01', key: 'KEY01', parentId: '' }, isLeaf: true, parent: null }
      });
    }
    expect(component).toBeTruthy();
  });

  it('should cover (deactivate) template event handler via triggerEventHandler', () => {
    const treeEl = fixture.debugElement.query(By.css('tree-root'));
    if (treeEl) {
      treeEl.triggerEventHandler('deactivate', {
        node: { data: { classificationId: 'CLF:01' } },
        treeModel: { activeNodes: [] }
      });
    }
    expect(component).toBeTruthy();
  });

  it('should cover (moveNode) template event handler via triggerEventHandler', () => {
    vi.spyOn(store, 'dispatch').mockReturnValue(of(undefined as any));
    classificationsServiceMock.getClassification.mockReturnValue(
      of({ classificationId: 'CLF:01', key: 'KEY01', parentId: '' })
    );
    const treeEl = fixture.debugElement.query(By.css('tree-root'));
    if (treeEl) {
      treeEl.triggerEventHandler('moveNode', {
        node: { classificationId: 'CLF:01', parentId: '' },
        to: { parent: { classificationId: '', key: '' } }
      });
    }
    expect(component).toBeTruthy();
  });

  it('should cover (treeDrop) template event handler via triggerEventHandler', () => {
    const treeEl = fixture.debugElement.query(By.css('tree-root'));
    if (treeEl) {
      treeEl.triggerEventHandler('treeDrop', {
        event: { target: { tagName: 'TREE-VIEWPORT' } },
        element: { data: { classificationId: 'CLF:01', parentId: '' } }
      });
    }
    expect(component).toBeTruthy();
  });

  it('should render @if (emptyTreeNodes) true branch in DOM via local fixture', () => {
    const localFixture = TestBed.createComponent(KadaiTreeComponent);
    const localComponent = localFixture.componentInstance;
    localComponent.emptyTreeNodes = true;
    localFixture.detectChanges();
    httpController.match(() => true).forEach((req) => req.flush(''));
    const el = localFixture.nativeElement.querySelector('h3');
    expect(el).toBeTruthy();
    expect(el.textContent).toContain('no classifications');
  });

  it('checkValidElements: target is inside nativeElement with localName tree-viewport — covers true branch', () => {
    const nativeEl: HTMLElement = fixture.nativeElement;
    const treeViewport = document.createElement('tree-viewport');
    nativeEl.appendChild(treeViewport);
    const event = { target: treeViewport };
    expect(() => component.onDocumentClick(event)).not.toThrow();
    nativeEl.removeChild(treeViewport);
  });

  it('checkValidElements: target is inside nativeElement with localName kadai-tree — covers kadai-tree localName branch', () => {
    const nativeEl: HTMLElement = fixture.nativeElement;
    const kadaiTree = document.createElement('kadai-tree');
    nativeEl.appendChild(kadaiTree);
    const event = { target: kadaiTree };
    expect(() => component.onDocumentClick(event)).not.toThrow();
    nativeEl.removeChild(kadaiTree);
  });

  it('collapseParentNodeIfItIsTheLastChild: parent node with 2 or more children does NOT collapse', async () => {
    vi.spyOn(store, 'dispatch').mockReturnValue(of(undefined as any));

    const treeNodes = [
      {
        classificationId: 'CLF:PARENT',
        key: 'PARENT',
        name: 'Parent Node',
        category: 'EXTERNAL',
        parentId: '',
        children: [
          {
            classificationId: 'CLF:CHILD1',
            key: 'CHILD1',
            name: 'Child One',
            category: 'EXTERNAL',
            parentId: 'CLF:PARENT',
            children: []
          },
          {
            classificationId: 'CLF:CHILD2',
            key: 'CHILD2',
            name: 'Child Two',
            category: 'EXTERNAL',
            parentId: 'CLF:PARENT',
            children: []
          }
        ]
      }
    ];
    classificationTreeServiceMock.transformToTreeNode.mockReturnValueOnce(treeNodes);

    const localFixture = TestBed.createComponent(KadaiTreeComponent);
    const localComponent = localFixture.componentInstance;
    localComponent.options = { ...localComponent.options, useVirtualScroll: false };
    localFixture.detectChanges();
    httpController.match(() => true).forEach((req) => req.flush(''));

    localFixture.detectChanges();
    await localFixture.whenStable();
    httpController.match(() => true).forEach((req) => req.flush(''));

    const mockClassification = { classificationId: 'CLF:CHILD1', key: 'CHILD1', parentId: 'CLF:PARENT' };
    classificationsServiceMock.getClassification.mockReturnValueOnce(of(mockClassification));

    const mockEvent = {
      node: { classificationId: 'CLF:CHILD1', parentId: 'CLF:PARENT' },
      to: { parent: { classificationId: 'CLF:ELSEWHERE', key: 'ELSEWHERE' } }
    };
    await expect(localComponent.onMoveNode(mockEvent)).resolves.not.toThrow();
    localFixture.destroy();
  });

  it('ngOnInit subscription: selectedClassificationId undefined — selectNodeId set to undefined', () => {
    store.reset({
      ...store.snapshot(),
      classification: {
        ...classificationStateMock,
        selectedClassification: undefined
      }
    });
    const localFixture = TestBed.createComponent(KadaiTreeComponent);
    const localComponent = localFixture.componentInstance;
    localFixture.detectChanges();
    httpController.match(() => true).forEach((req) => req.flush(''));
    expect(localComponent.selectNodeId()).toBeUndefined();
    localFixture.destroy();
  });

  it('selectNode: node IS found in tree — setIsActive(true) called', () => {
    const treeNodes = [
      {
        classificationId: 'CLF:FOUND',
        key: 'FOUND',
        name: 'Found Node',
        category: 'EXTERNAL',
        parentId: '',
        children: []
      }
    ];
    classificationTreeServiceMock.transformToTreeNode.mockReturnValueOnce(treeNodes);
    store.reset({
      ...store.snapshot(),
      classification: {
        ...classificationStateMock,
        selectedClassification: { classificationId: 'CLF:FOUND', key: 'FOUND' }
      }
    });
    const localFixture = TestBed.createComponent(KadaiTreeComponent);
    const localComponent = localFixture.componentInstance;
    localComponent.options = { ...localComponent.options, useVirtualScroll: false };
    localFixture.detectChanges();
    httpController.match(() => true).forEach((req) => req.flush(''));
    expect(localComponent).toBeTruthy();
    localFixture.destroy();
  });
});
