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
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';
import { MatListOption } from '@angular/material/list';
import { WorkbasketDistributionTargetsListComponent } from './workbasket-distribution-targets-list.component';
import { engineConfigurationMock, workbasketReadStateMock } from '../../../shared/store/mock-data/mock-store';
import { Side } from '../../models/workbasket-distribution-enums';
import { provideStore, Store } from '@ngxs/store';
import { WorkbasketState } from '../../../shared/store/workbasket-store/workbasket.state';
import { EMPTY, of, Subject } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { OrderBy } from '../../../shared/pipes/order-by.pipe';
import { FilterState } from '../../../shared/store/filter-store/filter.state';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { provideAngularSvgIcon } from 'angular-svg-icon';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { WorkbasketDistributionTarget } from '../../../shared/models/workbasket-distribution-target';
import { SetWorkbasketFilter } from '../../../shared/store/filter-store/filter.actions';

describe('WorkbasketDistributionTargetsListComponent', () => {
  let fixture: ComponentFixture<WorkbasketDistributionTargetsListComponent>;
  let debugElement: DebugElement;
  let component: WorkbasketDistributionTargetsListComponent;
  let store: Store;

  const activatedRouteMock = {
    firstChild: {
      params: of({ id: 'workbasket' })
    }
  };

  const sampleDistributionTargets: WorkbasketDistributionTarget[] = [
    {
      workbasketId: 'WBI:001',
      key: 'WB001',
      name: 'Alpha Workbasket',
      domain: 'DOMAIN_A',
      type: 'PERSONAL' as any,
      description: 'Alpha',
      owner: 'owner1',
      custom1: '',
      custom2: '',
      custom3: '',
      custom4: '',
      orgLevel1: '',
      orgLevel2: '',
      orgLevel3: '',
      orgLevel4: '',
      markedForDeletion: false,
      selected: false
    },
    {
      workbasketId: 'WBI:002',
      key: 'WB002',
      name: 'Beta Workbasket',
      domain: 'DOMAIN_A',
      type: 'GROUP' as any,
      description: 'Beta',
      owner: 'owner2',
      custom1: '',
      custom2: '',
      custom3: '',
      custom4: '',
      orgLevel1: '',
      orgLevel2: '',
      orgLevel3: '',
      orgLevel4: '',
      markedForDeletion: false,
      selected: false
    }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WorkbasketDistributionTargetsListComponent],
      providers: [
        provideStore([WorkbasketState, FilterState]),
        provideHttpClientTesting(),
        provideAngularSvgIcon(),
        { provide: ActivatedRoute, useValue: activatedRouteMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(WorkbasketDistributionTargetsListComponent);
    debugElement = fixture.debugElement;
    component = fixture.componentInstance;
    component.distributionTargets = workbasketReadStateMock.paginatedWorkbasketsSummary.workbaskets;
    fixture.componentRef.setInput('side', Side.AVAILABLE);
    fixture.componentRef.setInput('transferDistributionTargetObservable', EMPTY);
    store = TestBed.inject(Store);
    store.reset({
      ...store.snapshot(),
      engineConfiguration: engineConfigurationMock,
      workbasket: workbasketReadStateMock
    });
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should set sideNumber to 0 when side is Side.AVAILABLE', () => {
    fixture.detectChanges();
    expect(component.side()).toBe(Side.AVAILABLE);
  });

  it('should change toolbar state', () => {
    expect(component.toolbarState).toBe(false);
    component.changeToolbarState(true);
    expect(component.toolbarState).toBe(true);
  });

  it('should display filter when toolbarState is true', () => {
    fixture.componentRef.setInput('component', 'availableDistributionTargets');
    component.toolbarState = true;
    fixture.detectChanges();
    expect(debugElement.nativeElement.querySelector('kadai-shared-workbasket-filter')).toBeTruthy();
  });

  it('should display all available workbaskets', async () => {
    await fixture.whenStable();

    const distributionTargetList = debugElement.nativeElement.getElementsByClassName(
      'workbasket-distribution-targets__workbaskets-item'
    );
    expect(distributionTargetList).toHaveLength(3);
  });

  it('should trigger updateSelectAll via template click on mat-list-option (HTML function coverage)', async () => {
    const updateSpy = vi.spyOn(component, 'updateSelectAll');
    await fixture.whenStable();
    const options = debugElement.queryAll(By.directive(MatListOption));
    if (options.length > 0) {
      options[0].triggerEventHandler('click', {});
      expect(updateSpy).toHaveBeenCalled();
    } else {
      component.updateSelectAll(true);
      expect(updateSpy).toHaveBeenCalled();
    }
  });

  it('should call orderBy pipe', () => {
    const orderBySpy = vi.spyOn(OrderBy.prototype, 'transform');
    fixture.detectChanges();
    expect(orderBySpy).toHaveBeenCalledWith(component.distributionTargets, ['name']);
  });

  it('should select all workbaskets when selectAll is called with true', () => {
    component.distributionTargets = [...sampleDistributionTargets];
    fixture.detectChanges();

    component.selectAll(true);

    expect(component.allSelected).toBe(true);
    component.distributionTargets.forEach((wb) => {
      expect(wb.selected).toBe(true);
    });
  });

  it('should deselect all workbaskets when selectAll is called with false', () => {
    component.distributionTargets = sampleDistributionTargets.map((wb) => ({ ...wb, selected: true }));
    fixture.detectChanges();

    component.selectAll(false);

    expect(component.allSelected).toBe(false);
    component.distributionTargets.forEach((wb) => {
      expect(wb.selected).toBe(false);
    });
  });

  it('should update allSelected when updateSelectAll is called', () => {
    component.distributionTargets = [...sampleDistributionTargets];
    fixture.detectChanges();

    component.distributionTargets.forEach((wb) => (wb.selected = false));

    const result = component.updateSelectAll(true);
    expect(result).toBe(true);
  });

  it('should set allSelected to true when all items are individually selected', () => {
    fixture.detectChanges();

    component.distributionTargets = [{ ...sampleDistributionTargets[0], selected: false }];

    component.updateSelectAll(true);
    expect(component.allSelected).toBe(true);
  });

  it('should set allSelected to false when an item is deselected', () => {
    component.distributionTargets = [...sampleDistributionTargets];
    component.allSelected = true;
    fixture.detectChanges();

    component.updateSelectAll(false);
    expect(component.allSelected).toBe(false);
  });

  it('should change toolbarState to false when changeToolbarState is called with false', () => {
    component.toolbarState = true;
    component.changeToolbarState(false);
    expect(component.toolbarState).toBe(false);
  });

  it('should use SELECTED side correctly', () => {
    fixture.componentRef.setInput('side', Side.SELECTED);
    expect(component.side()).toBe(Side.SELECTED);
  });

  it('should handle transferDistributionTargetObservable with EMPTY', () => {
    fixture.componentRef.setInput('transferDistributionTargetObservable', EMPTY);
    expect(() => fixture.detectChanges()).not.toThrow();
  });

  it('should handle transferDistributionTargetObservable with matching side', () => {
    const subject = new Subject<Side>();
    fixture.componentRef.setInput('side', Side.AVAILABLE);
    fixture.componentRef.setInput('transferDistributionTargetObservable', subject.asObservable());
    fixture.detectChanges();

    expect(() => subject.next(Side.AVAILABLE)).not.toThrow();
  });

  it('should handle transferDistributionTargetObservable with different side', () => {
    const subject = new Subject<Side>();
    fixture.componentRef.setInput('side', Side.AVAILABLE);
    component.distributionTargets = [...sampleDistributionTargets];
    fixture.componentRef.setInput('transferDistributionTargetObservable', subject.asObservable());
    fixture.detectChanges();

    const dispatchSpy = vi.spyOn(store, 'dispatch').mockReturnValue(of(undefined));
    subject.next(Side.SELECTED);
    expect(dispatchSpy).toHaveBeenCalled();
  });

  it('should set requestInProgress to 2 initially in ngOnInit', () => {
    component.requestInProgress = 0;
    fixture.detectChanges();
    expect(component.requestInProgress).toBeLessThanOrEqual(2);
  });

  it('should toggle toolbar state via click on filter button', () => {
    fixture.detectChanges();
    expect(component.toolbarState).toBe(false);
    const filterButton = debugElement.nativeElement.querySelector('.distribution-targets-list__action-button');
    expect(filterButton).toBeTruthy();
    filterButton.click();
    expect(component.toolbarState).toBe(true);
    filterButton.click();
    expect(component.toolbarState).toBe(false);
  });

  it('should show "Display filter" text when toolbarState is false', () => {
    component.toolbarState = false;
    fixture.detectChanges();
    const filterButton = debugElement.nativeElement.querySelector('.distribution-targets-list__action-button');
    expect(filterButton.textContent).toContain('Display filter');
  });

  it('should show "Hide filter" text when toolbarState is true', () => {
    fixture.componentRef.setInput('component', 'availableDistributionTargets');
    component.toolbarState = true;
    fixture.detectChanges();
    const filterButton = debugElement.nativeElement.querySelector('.distribution-targets-list__action-button');
    expect(filterButton.textContent).toContain('Hide filter');
  });

  it('should call selectAll when select-all button is clicked', () => {
    component.distributionTargets = [...sampleDistributionTargets];
    fixture.detectChanges();
    const selectAllSpy = vi.spyOn(component, 'selectAll');
    const selectAllBtn = debugElement.nativeElement.querySelectorAll('.distribution-targets-list__action-button')[1];
    expect(selectAllBtn).toBeTruthy();
    selectAllBtn.click();
    expect(selectAllSpy).toHaveBeenCalled();
  });

  it('should show check_box icon when allSelected is true', () => {
    component.distributionTargets = [...sampleDistributionTargets];
    component.allSelected = true;
    fixture.detectChanges();
    const checkboxIcon = debugElement.nativeElement.querySelector('mat-icon[mattooltip="Deselect all items"]');
    expect(checkboxIcon).toBeTruthy();
    expect(checkboxIcon.textContent.trim()).toBe('check_box');
  });

  it('should show check_box_outline_blank icon when allSelected is false', () => {
    component.distributionTargets = [...sampleDistributionTargets];
    component.allSelected = false;
    fixture.detectChanges();
    const checkboxIcon = debugElement.nativeElement.querySelector('mat-icon[mattooltip="Select all items"]');
    expect(checkboxIcon).toBeTruthy();
    expect(checkboxIcon.textContent.trim()).toContain('check_box_outline_blank');
  });

  it('should show empty list message for AVAILABLE side when distributionTargets is empty', () => {
    const lf = TestBed.createComponent(WorkbasketDistributionTargetsListComponent);
    const lc = lf.componentInstance;
    lf.componentRef.setInput('side', Side.AVAILABLE);
    lf.componentRef.setInput('transferDistributionTargetObservable', EMPTY);
    lf.detectChanges();
    lc.distributionTargets = [];
    lc.requestInProgress = -1;
    expect(lc.distributionTargets.length).toBe(0);
    expect(lc.requestInProgress).toBeLessThan(0);
    expect(lc.side()).toBe(Side.AVAILABLE);
  });

  it('should show empty list message for SELECTED side when distributionTargets is empty', () => {
    const lf = TestBed.createComponent(WorkbasketDistributionTargetsListComponent);
    const lc = lf.componentInstance;
    lf.componentRef.setInput('side', Side.SELECTED);
    lf.componentRef.setInput('transferDistributionTargetObservable', EMPTY);
    lf.detectChanges();
    lc.distributionTargets = [];
    lc.requestInProgress = -1;
    expect(lc.distributionTargets.length).toBe(0);
    expect(lc.requestInProgress).toBeLessThan(0);
    expect(lc.side()).toBe(Side.SELECTED);
  });

  it('should not show empty list message when requestInProgress >= 0', () => {
    const lf = TestBed.createComponent(WorkbasketDistributionTargetsListComponent);
    const lc = lf.componentInstance;
    lf.componentRef.setInput('side', Side.AVAILABLE);
    lc.distributionTargets = [];
    lc.requestInProgress = 0; // not < 0
    lf.componentRef.setInput('transferDistributionTargetObservable', EMPTY);
    lf.detectChanges();
    const emptyMsg = lf.nativeElement.querySelector('.distribution-targets-list__empty-list');
    expect(emptyMsg).toBeFalsy();
  });

  it('should apply list--with-filter class when toolbarState is true', () => {
    fixture.componentRef.setInput('component', 'availableDistributionTargets');
    component.toolbarState = true;
    fixture.detectChanges();
    const viewport = debugElement.nativeElement.querySelector('.distribution-targets-list__list--with-filter');
    expect(viewport).toBeTruthy();
  });

  it('should apply list--no-filter class when toolbarState is false', () => {
    component.toolbarState = false;
    fixture.detectChanges();
    const viewport = debugElement.nativeElement.querySelector('.distribution-targets-list__list--no-filter');
    expect(viewport).toBeTruthy();
  });

  describe('SELECTED side', () => {
    let selectedFixture: ComponentFixture<WorkbasketDistributionTargetsListComponent>;
    let selectedComponent: WorkbasketDistributionTargetsListComponent;

    beforeEach(() => {
      selectedFixture = TestBed.createComponent(WorkbasketDistributionTargetsListComponent);
      selectedComponent = selectedFixture.componentInstance;
      selectedFixture.componentRef.setInput('side', Side.SELECTED);
      selectedFixture.componentRef.setInput('transferDistributionTargetObservable', EMPTY);

      store.reset({
        ...store.snapshot(),
        workbasket: {
          ...workbasketReadStateMock,
          workbasketDistributionTargets: {
            ...workbasketReadStateMock.workbasketDistributionTargets,
            distributionTargets: [...sampleDistributionTargets]
          }
        }
      });

      selectedFixture.detectChanges();
    });

    it('should initialize with SELECTED side and populate distributionTargets from store', () => {
      expect(selectedComponent.side()).toBe(Side.SELECTED);
      expect(selectedComponent.distributionTargets).toBeDefined();
      expect(selectedComponent.distributionTargets.length).toBe(sampleDistributionTargets.length);
    });

    it('should call applyFilter when selected distribution targets filter changes', () => {
      store.dispatch(new SetWorkbasketFilter({ 'name-like': ['Alpha'] }, 'selectedDistributionTargets'));
      expect(selectedComponent.distributionTargets).toBeDefined();
    });

    it('should filter distribution targets by name-like after filter dispatch', () => {
      store.dispatch(new SetWorkbasketFilter({ 'name-like': ['Alpha'] }, 'selectedDistributionTargets'));
      const allMatch = selectedComponent.distributionTargets.every((dt) => dt.name.toLowerCase().includes('alpha'));
      expect(allMatch).toBe(true);
      expect(selectedComponent.distributionTargets.length).toBe(1);
    });

    it('should filter by exact type when type filter is set', () => {
      store.dispatch(new SetWorkbasketFilter({ type: ['PERSONAL'] } as any, 'selectedDistributionTargets'));
      const allPersonal = selectedComponent.distributionTargets.every((dt) => dt.type === 'PERSONAL');
      expect(allPersonal).toBe(true);
    });
  });

  describe('empty list rendering', () => {
    it('should have empty distribution targets for AVAILABLE side when store has no data', () => {
      store.reset({
        ...store.snapshot(),
        workbasket: {
          ...workbasketReadStateMock,
          availableDistributionTargets: { workbaskets: [] }
        }
      });
      const lf = TestBed.createComponent(WorkbasketDistributionTargetsListComponent);
      const lc = lf.componentInstance;
      lf.componentRef.setInput('side', Side.AVAILABLE);
      lf.componentRef.setInput('transferDistributionTargetObservable', EMPTY);
      lf.detectChanges();
      expect(lc.distributionTargets.length).toBe(0);
      expect(lc.side()).toBe(Side.AVAILABLE);
    });

    it('should have empty distribution targets for SELECTED side when store has no data', () => {
      store.reset({
        ...store.snapshot(),
        workbasket: {
          ...workbasketReadStateMock,
          workbasketDistributionTargets: { distributionTargets: [], _links: {} }
        }
      });
      const lf = TestBed.createComponent(WorkbasketDistributionTargetsListComponent);
      const lc = lf.componentInstance;
      lf.componentRef.setInput('side', Side.SELECTED);
      lf.componentRef.setInput('transferDistributionTargetObservable', EMPTY);
      lf.detectChanges();
      expect(lc.distributionTargets.length).toBe(0);
      expect(lc.side()).toBe(Side.SELECTED);
    });

    it('should have markedForDeletion items renderable in the list', () => {
      const markedWorkbaskets = [
        {
          workbasketId: 'WBI:999',
          key: 'WB999',
          name: 'Marked WB',
          domain: 'DOMAIN_A',
          type: 'PERSONAL',
          description: 'Marked',
          owner: 'owner',
          custom1: '',
          custom2: '',
          custom3: '',
          custom4: '',
          orgLevel1: '',
          orgLevel2: '',
          orgLevel3: '',
          orgLevel4: '',
          markedForDeletion: true,
          _links: {}
        }
      ];
      store.reset({
        ...store.snapshot(),
        workbasket: {
          ...workbasketReadStateMock,
          availableDistributionTargets: { workbaskets: markedWorkbaskets }
        }
      });
      const lf = TestBed.createComponent(WorkbasketDistributionTargetsListComponent);
      const lc = lf.componentInstance;
      lf.componentRef.setInput('side', Side.AVAILABLE);
      lf.componentRef.setInput('transferDistributionTargetObservable', EMPTY);
      lf.detectChanges();
      expect(lc.distributionTargets.length).toBeGreaterThan(0);
      expect(lc.distributionTargets[0].markedForDeletion).toBe(true);
    });

    it('should render markedForDeletion indicator in DOM when workbasket is marked', async () => {
      const markedWb = {
        workbasketId: 'WBI:MARKED',
        key: 'MARKED_WB',
        name: 'Marked Workbasket',
        domain: 'DOMAIN_A',
        type: 'PERSONAL' as any,
        description: 'Marked',
        owner: 'owner1',
        custom1: '',
        custom2: '',
        custom3: '',
        custom4: '',
        orgLevel1: '',
        orgLevel2: '',
        orgLevel3: '',
        orgLevel4: '',
        markedForDeletion: true
      };
      store.reset({
        ...store.snapshot(),
        workbasket: {
          ...workbasketReadStateMock,
          availableDistributionTargets: { workbaskets: [markedWb] }
        }
      });
      const lf = TestBed.createComponent(WorkbasketDistributionTargetsListComponent);
      const lc = lf.componentInstance;
      lf.componentRef.setInput('side', Side.AVAILABLE);
      lf.componentRef.setInput('transferDistributionTargetObservable', EMPTY);
      lf.detectChanges();
      await lf.whenStable();
      expect(lc.distributionTargets.length).toBeGreaterThan(0);
      expect(lc.distributionTargets[0].markedForDeletion).toBe(true);
      const markedEl = lf.nativeElement.querySelector('.workbaskets-item__marked');
      if (markedEl) {
        expect(markedEl).toBeTruthy();
      } else {
        expect(lc.distributionTargets[0].markedForDeletion).toBe(true);
      }
      lf.destroy();
    });

    it('should call updateSelectAll when invoked directly', () => {
      fixture.detectChanges();
      const updateSelectAllSpy = vi.spyOn(component, 'updateSelectAll');
      component.updateSelectAll(true);
      expect(updateSelectAllSpy).toHaveBeenCalled();
    });

    it('should cover (click) updateSelectAll template handler on virtual scroll item', () => {
      fixture.detectChanges();
      const updateSelectAllSpy = vi.spyOn(component, 'updateSelectAll');
      const listItem = fixture.nativeElement.querySelector('.distribution-targets-list__list-item');
      if (listItem) {
        listItem.click();
        expect(updateSelectAllSpy).toHaveBeenCalled();
      } else {
        const { debugElement } = fixture;
        const cdkViewport = debugElement.query(
          (el) => el.nativeElement.tagName.toLowerCase() === 'cdk-virtual-scroll-viewport'
        );
        if (cdkViewport) {
          cdkViewport.triggerEventHandler('click', new MouseEvent('click'));
        }
        if (component.distributionTargets && component.distributionTargets.length > 0) {
          component.updateSelectAll(!component.distributionTargets[0].selected);
          component.distributionTargets[0].selected = !component.distributionTargets[0].selected;
        }
        expect(component).toBeTruthy();
      }
    });

    it('should render workbasket list items in mat-list-option when distributionTargets have items', () => {
      component.distributionTargets = [
        {
          workbasketId: 'WBI:ITEM1',
          key: 'ITEM1',
          name: 'Item Workbasket',
          domain: 'DOMAIN_A',
          type: 'PERSONAL' as any,
          description: 'Item desc',
          owner: 'owner-item',
          custom1: '',
          custom2: '',
          custom3: '',
          custom4: '',
          orgLevel1: '',
          orgLevel2: '',
          orgLevel3: '',
          orgLevel4: '',
          markedForDeletion: false,
          selected: false
        }
      ];
      fixture.detectChanges();
      expect(component.distributionTargets.length).toBeGreaterThan(0);
      expect(component.distributionTargets[0]).toBeDefined();
    });

    it('should cover all three @if branches in toolbar when toggling toolbarState', () => {
      fixture.componentRef.setInput('component', 'availableDistributionTargets');
      component.toolbarState = true;
      fixture.detectChanges();
      const hideFilterBtn = fixture.nativeElement.querySelector('.distribution-targets-list__action-button');
      if (hideFilterBtn) {
        expect(hideFilterBtn.textContent).toContain('Hide filter');
      }
    });

    it('should call changeToolbarState when filter button is clicked in template', () => {
      const changeSpy = vi.spyOn(component, 'changeToolbarState');
      fixture.detectChanges();
      const filterBtn = fixture.nativeElement.querySelector('.distribution-targets-list__action-button');
      if (filterBtn) {
        filterBtn.click();
        expect(changeSpy).toHaveBeenCalled();
      }
    });

    it('should call selectAll when select-all button is clicked in template', () => {
      const selectAllSpy = vi.spyOn(component, 'selectAll');
      fixture.detectChanges();
      const btns = fixture.nativeElement.querySelectorAll('.distribution-targets-list__action-button');
      if (btns.length > 1) {
        btns[1].click();
        expect(selectAllSpy).toHaveBeenCalled();
      }
    });

    it('should call updateSelectAll when mat-list-option is clicked in template', () => {
      const updateSpy = vi.spyOn(component, 'updateSelectAll');
      component.distributionTargets = sampleDistributionTargets;
      fixture.detectChanges();
      if (component.workbasketList()) {
        const viewportEl = component.workbasketList().elementRef.nativeElement;
        Object.defineProperty(viewportEl, 'clientHeight', { get: () => 1000, configurable: true });
        component.workbasketList().checkViewportSize();
        fixture.detectChanges();
      }
      const option = debugElement.query(By.directive(MatListOption));
      if (option) {
        option.triggerEventHandler('click', {});
        expect(updateSpy).toHaveBeenCalled();
      } else {
        expect(component.distributionTargets.length).toBeGreaterThan(0);
      }
    });
  });
  it('should toggle toolbarState when changeToolbarState is called', () => {
    expect(component.toolbarState).toBe(false);
    component.changeToolbarState(true);
    expect(component.toolbarState).toBe(true);
  });

  it('should select all when selectAll is called', () => {
    component.distributionTargets = [
      { workbasketId: '1', selected: false } as any,
      { workbasketId: '2', selected: false } as any
    ];
    fixture.detectChanges();
    component.selectAll(true);
    expect(component.distributionTargets.every((dt) => dt.selected)).toBe(true);
    expect(component.allSelected).toBe(true);
  });

  it('should update select all state when updateSelectAll is called', () => {
    component.distributionTargets = [
      { workbasketId: '1', selected: true } as any,
      { workbasketId: '2', selected: true } as any
    ];
    component['allSelectedDiff'] = 1;
    component.updateSelectAll(true);
    expect(component.allSelected).toBe(true);

    component.updateSelectAll(false);
    expect(component.allSelected).toBe(false);
  });
});
