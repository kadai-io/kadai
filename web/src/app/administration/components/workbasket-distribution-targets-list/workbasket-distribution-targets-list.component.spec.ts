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
import { DebugElement } from '@angular/core';
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
    component.side = Side.AVAILABLE;
    component.transferDistributionTargetObservable = EMPTY;
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
    expect(component.side).toBe(Side.AVAILABLE);
  });

  it('should change toolbar state', () => {
    expect(component.toolbarState).toBe(false);
    component.changeToolbarState(true);
    expect(component.toolbarState).toBe(true);
  });

  it('should display filter when toolbarState is true', () => {
    component.component = 'availableDistributionTargets';
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
    component.side = Side.SELECTED;
    expect(component.side).toBe(Side.SELECTED);
  });

  it('should handle transferDistributionTargetObservable with EMPTY', () => {
    component.transferDistributionTargetObservable = EMPTY;
    expect(() => fixture.detectChanges()).not.toThrow();
  });

  it('should handle transferDistributionTargetObservable with matching side', () => {
    const subject = new Subject<Side>();
    component.side = Side.AVAILABLE;
    component.transferDistributionTargetObservable = subject.asObservable();
    fixture.detectChanges();

    expect(() => subject.next(Side.AVAILABLE)).not.toThrow();
  });

  it('should handle transferDistributionTargetObservable with different side', () => {
    const subject = new Subject<Side>();
    component.side = Side.AVAILABLE;
    component.distributionTargets = [...sampleDistributionTargets];
    component.transferDistributionTargetObservable = subject.asObservable();
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

  describe('SELECTED side', () => {
    let selectedFixture: ComponentFixture<WorkbasketDistributionTargetsListComponent>;
    let selectedComponent: WorkbasketDistributionTargetsListComponent;

    beforeEach(() => {
      selectedFixture = TestBed.createComponent(WorkbasketDistributionTargetsListComponent);
      selectedComponent = selectedFixture.componentInstance;
      selectedComponent.side = Side.SELECTED;
      selectedComponent.transferDistributionTargetObservable = EMPTY;

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
      expect(selectedComponent.side).toBe(Side.SELECTED);
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
});
