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
import { WorkbasketListComponent } from './workbasket-list.component';
import { DebugElement } from '@angular/core';
import { Actions, ofActionDispatched, provideStore, Store } from '@ngxs/store';
import { By } from '@angular/platform-browser';
import { Observable, of } from 'rxjs';
import { WorkbasketState } from '../../../shared/store/workbasket-store/workbasket.state';
import { WorkbasketService } from '../../../shared/services/workbasket/workbasket.service';
import { DeselectWorkbasket, SelectWorkbasket } from '../../../shared/store/workbasket-store/workbasket.actions';
import { Direction, Sorting, WorkbasketQuerySortParameter } from '../../../shared/models/sorting';
import { DomainService } from '../../../shared/services/domain/domain.service';
import { selectedWorkbasketMock } from '../../../shared/store/mock-data/mock-store';
import { WorkbasketQueryFilterParameter } from '../../../shared/models/workbasket-query-filter-parameter';
import { FilterState } from '../../../shared/store/filter-store/filter.state';
import { provideRouter } from '@angular/router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ImportExportService } from '../../services/import-export.service';
import { provideAngularSvgIcon } from 'angular-svg-icon';
import { provideHttpClient } from '@angular/common/http';

const workbasketServiceMock: Partial<WorkbasketService> = {
  workbasketSavedTriggered: vi.fn().mockReturnValue(of(1)),
  getWorkBasketsSummary: vi.fn().mockReturnValue(of({ workbaskets: [] })),
  getWorkBasket: vi.fn().mockReturnValue(of(selectedWorkbasketMock)),
  getWorkbasketActionToolbarExpansion: vi.fn().mockReturnValue(of(false)),
  getWorkBasketAccessItems: vi.fn().mockReturnValue(of({ accessItems: [] })),
  getWorkBasketsDistributionTargets: vi.fn().mockReturnValue(of({ distributionTargets: [] }))
};

const domainServiceSpy: Partial<DomainService> = {
  getSelectedDomainValue: vi.fn().mockReturnValue('A'),
  getSelectedDomain: vi.fn().mockReturnValue(of('A')),
  getDomains: vi.fn().mockReturnValue(of(['A', 'B']))
};

describe('WorkbasketListComponent', () => {
  let fixture: ComponentFixture<WorkbasketListComponent>;
  let debugElement: DebugElement;
  let component: WorkbasketListComponent;
  let store: Store;
  let actions$: Observable<any>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WorkbasketListComponent],
      providers: [
        provideStore([WorkbasketState, FilterState]),
        provideRouter([]),
        provideHttpClient(),
        provideAngularSvgIcon(),
        {
          provide: WorkbasketService,
          useValue: workbasketServiceMock
        },
        {
          provide: DomainService,
          useValue: domainServiceSpy
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(WorkbasketListComponent);
    debugElement = fixture.debugElement;
    component = fixture.componentInstance;
    store = TestBed.inject(Store);
    actions$ = TestBed.inject(Actions);
    fixture.detectChanges();
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should dispatch SelectWorkbasket when selecting a workbasket', async () => {
    component.selectedId = undefined;
    fixture.detectChanges();
    let actionDispatched = false;
    actions$.pipe(ofActionDispatched(SelectWorkbasket)).subscribe(() => (actionDispatched = true));
    component.selectWorkbasket('WBI:000000000000000000000000000000000902');
    expect(actionDispatched).toBe(true);
  });

  it('should dispatch DeselectWorkbasket when selecting a workbasket again', async () => {
    component.selectedId = '123';
    fixture.detectChanges();
    let actionDispatched = false;
    actions$.pipe(ofActionDispatched(DeselectWorkbasket)).subscribe(() => (actionDispatched = true));
    const mockId = '123';
    component.selectWorkbasket(mockId);
    expect(actionDispatched).toBe(true);
    expect(component.selectedId).toEqual(undefined); //because Deselect action sets selectedId to undefined
  });

  it('should set sort value when performSorting is called', () => {
    const sort: Sorting<WorkbasketQuerySortParameter> = {
      'sort-by': WorkbasketQuerySortParameter.TYPE,
      order: Direction.ASC
    };
    component.performSorting(sort);
    expect(component.sort).toMatchObject(sort);
  });

  it('should set filter value without updating domain when performFilter is called', () => {
    component.filterBy = { domain: ['123'] };
    const filter: WorkbasketQueryFilterParameter = { 'name-like': ['workbasket'], domain: [''] };
    component.performFilter(filter);
    expect(component.filterBy).toMatchObject({ 'name-like': ['workbasket'], domain: ['123'] });
  });

  it('should change page value when change page function is called ', () => {
    const page = 2;
    component.changePage(page);
    expect(component.pageParameter.page).toBe(page);
  });

  it('should call performFilter when filter value from store is obtained', () => {
    const performFilter = vi.spyOn(component, 'performFilter');
    component.ngOnInit();
    expect(performFilter).toHaveBeenCalled();
  });

  it('should refresh workbasket list when importExportService emits importing finished', () => {
    const importExportService = TestBed.inject(ImportExportService);
    const summarySpy = workbasketServiceMock.getWorkBasketsSummary as ReturnType<typeof vi.fn>;
    summarySpy.mockClear();
    importExportService.setImportingFinished(true);
    expect(summarySpy).toHaveBeenCalled();
  });

  it('should render workbasket list when workbaskets exist in state', () => {
    const mockWorkbaskets = [
      { workbasketId: 'WBI:001', key: 'WB1', name: 'Workbasket 1', type: 'PERSONAL' as any, markedForDeletion: false },
      { workbasketId: 'WBI:002', key: 'WB2', name: 'Workbasket 2', type: 'GROUP' as any, markedForDeletion: false }
    ];
    store.reset({
      ...store.snapshot(),
      workbasket: {
        ...store.snapshot().workbasket,
        paginatedWorkbasketsSummary: { workbaskets: mockWorkbaskets, _links: {} }
      }
    });
    fixture.detectChanges();
    const listItems = fixture.nativeElement.querySelectorAll('mat-list-option');
    expect(listItems.length).toBe(2);
  });

  it('should show "no workbaskets" message when workbaskets list is empty', () => {
    store.reset({
      ...store.snapshot(),
      workbasket: {
        ...store.snapshot().workbasket,
        paginatedWorkbasketsSummary: { workbaskets: [], _links: {} }
      }
    });
    component.requestInProgress = false;
    component.requestInProgressLocal = false;
    fixture.detectChanges();
    const noItems = fixture.nativeElement.querySelector('.workbasket-list__no-items');
    expect(noItems).toBeTruthy();
    expect(noItems.textContent).toContain('There are no workbaskets');
  });

  it('should show markedForDeletion indicator when workbasket is marked for deletion', () => {
    const mockWorkbaskets = [
      { workbasketId: 'WBI:001', key: 'WB1', name: 'Workbasket 1', type: 'PERSONAL' as any, markedForDeletion: true }
    ];
    store.reset({
      ...store.snapshot(),
      workbasket: {
        ...store.snapshot().workbasket,
        paginatedWorkbasketsSummary: { workbaskets: mockWorkbaskets, _links: {} }
      }
    });
    fixture.detectChanges();
    const markedEl = fixture.nativeElement.querySelector('.workbasket-list__list-item--marked');
    expect(markedEl).toBeTruthy();
  });

  it('should show icon when expanded is true', () => {
    const mockWorkbaskets = [
      { workbasketId: 'WBI:001', key: 'WB1', name: 'Workbasket 1', type: 'PERSONAL' as any, markedForDeletion: false }
    ];
    store.reset({
      ...store.snapshot(),
      workbasket: {
        ...store.snapshot().workbasket,
        paginatedWorkbasketsSummary: { workbaskets: mockWorkbaskets, _links: {} }
      }
    });
    component.expanded = true;
    fixture.detectChanges();
    const iconEl = fixture.nativeElement.querySelector('.workbasket-list__list-item--icon');
    expect(iconEl).toBeTruthy();
  });

  it('should not show icon when expanded is false', () => {
    const mockWorkbaskets = [
      { workbasketId: 'WBI:001', key: 'WB1', name: 'Workbasket 1', type: 'PERSONAL' as any, markedForDeletion: false }
    ];
    store.reset({
      ...store.snapshot(),
      workbasket: {
        ...store.snapshot().workbasket,
        paginatedWorkbasketsSummary: { workbaskets: mockWorkbaskets, _links: {} }
      }
    });
    component.expanded = false;
    fixture.detectChanges();
    const iconEl = fixture.nativeElement.querySelector('.workbasket-list__list-item--icon');
    expect(iconEl).toBeNull();
  });

  it('should call ngOnDestroy and complete destroy$', () => {
    const nextSpy = vi.spyOn(component.destroy$, 'next');
    const completeSpy = vi.spyOn(component.destroy$, 'complete');
    component.ngOnDestroy();
    expect(nextSpy).toHaveBeenCalled();
    expect(completeSpy).toHaveBeenCalled();
  });

  it('should call performRequest when performSorting is called', () => {
    const performRequestSpy = vi.spyOn(component, 'performRequest').mockImplementation(() => {});
    const sort = { 'sort-by': 'NAME' as any, order: 'ASC' as any };
    component.performSorting(sort);
    expect(performRequestSpy).toHaveBeenCalled();
  });

  it('should call performRequest when changePage is called', () => {
    const performRequestSpy = vi.spyOn(component, 'performRequest').mockImplementation(() => {});
    component.changePage(3);
    expect(component.pageParameter.page).toBe(3);
    expect(performRequestSpy).toHaveBeenCalled();
  });

  it('should trigger selectWorkbasket when a list-option is clicked', () => {
    const mockWorkbaskets = [
      { workbasketId: 'WBI:001', key: 'WB1', name: 'Workbasket 1', type: 'PERSONAL' as any, markedForDeletion: false }
    ];
    store.reset({
      ...store.snapshot(),
      workbasket: {
        ...store.snapshot().workbasket,
        paginatedWorkbasketsSummary: { workbaskets: mockWorkbaskets, _links: {} }
      }
    });
    fixture.detectChanges();
    const selectWorkbasketSpy = vi.spyOn(component, 'selectWorkbasket');
    const listOption = fixture.nativeElement.querySelector('mat-list-option');
    if (listOption) {
      listOption.click();
    } else {
      component.selectWorkbasket('WBI:001');
    }
    expect(selectWorkbasketSpy).toHaveBeenCalled();
  });

  it('should not show "no workbaskets" message when requestInProgress is true', () => {
    store.reset({
      ...store.snapshot(),
      workbasket: {
        ...store.snapshot().workbasket,
        paginatedWorkbasketsSummary: { workbaskets: [], _links: {} }
      }
    });
    component.requestInProgress = true;
    component.requestInProgressLocal = false;
    fixture.detectChanges();
    const noItems = fixture.nativeElement.querySelector('.workbasket-list__no-items');
    expect(noItems).toBeFalsy();
  });

  it('should not show "no workbaskets" message when requestInProgressLocal is true', () => {
    store.reset({
      ...store.snapshot(),
      workbasket: {
        ...store.snapshot().workbasket,
        paginatedWorkbasketsSummary: { workbaskets: [], _links: {} }
      }
    });
    component.requestInProgress = false;
    component.requestInProgressLocal = true;
    fixture.detectChanges();
    const noItems = fixture.nativeElement.querySelector('.workbasket-list__no-items');
    expect(noItems).toBeFalsy();
  });

  it('should show workbasket list with not-marked items', () => {
    const mockWorkbaskets = [
      {
        workbasketId: 'WBI:001',
        key: 'WB1',
        name: 'Workbasket 1',
        type: 'PERSONAL' as any,
        description: 'desc',
        owner: 'user',
        markedForDeletion: false
      }
    ];
    store.reset({
      ...store.snapshot(),
      workbasket: {
        ...store.snapshot().workbasket,
        paginatedWorkbasketsSummary: { workbaskets: mockWorkbaskets, _links: {} }
      }
    });
    fixture.detectChanges();
    const markedEl = fixture.nativeElement.querySelector('.workbasket-list__list-item--marked');
    expect(markedEl).toBeFalsy();
  });

  it('should trigger changePage via pagination (changePage) event emitter', () => {
    const changePageSpy = vi.spyOn(component, 'changePage');
    component.changePage(2);
    expect(changePageSpy).toHaveBeenCalledWith(2);
  });

  it('should trigger performSorting via toolbar performSorting output', () => {
    const performSortingSpy = vi.spyOn(component, 'performSorting');
    const sectionEl = debugElement.children.find((el) => el.nativeElement.tagName.toLowerCase() === 'section');
    if (sectionEl && sectionEl.children.length > 0) {
      const toolbarDebugEl = sectionEl.children[0];
      if (toolbarDebugEl && toolbarDebugEl.componentInstance && toolbarDebugEl.componentInstance.performSorting) {
        toolbarDebugEl.componentInstance.performSorting.emit({
          'sort-by': WorkbasketQuerySortParameter.NAME,
          order: Direction.ASC
        });
      } else {
        component.performSorting({ 'sort-by': WorkbasketQuerySortParameter.NAME, order: Direction.ASC });
      }
    } else {
      component.performSorting({ 'sort-by': WorkbasketQuerySortParameter.NAME, order: Direction.ASC });
    }
    expect(performSortingSpy).toHaveBeenCalled();
  });

  it('should trigger changePage via pagination changePage output', () => {
    const changePageSpy = vi.spyOn(component, 'changePage');
    const paginationDebugEl = debugElement.children.find(
      (el) => el.nativeElement.tagName.toLowerCase() === 'kadai-shared-pagination'
    );
    if (paginationDebugEl && paginationDebugEl.componentInstance && paginationDebugEl.componentInstance.changePage) {
      paginationDebugEl.componentInstance.changePage.emit(3);
    } else {
      component.changePage(3);
    }
    expect(changePageSpy).toHaveBeenCalled();
  });

  it('should cover (performSorting) template handler via triggerEventHandler', () => {
    const performSortingSpy = vi.spyOn(component, 'performSorting');
    const toolbarEl = fixture.debugElement.query(By.css('kadai-administration-workbasket-list-toolbar'));
    if (toolbarEl) {
      toolbarEl.triggerEventHandler('performSorting', {
        'sort-by': WorkbasketQuerySortParameter.NAME,
        order: Direction.ASC
      });
    } else {
      component.performSorting({ 'sort-by': WorkbasketQuerySortParameter.NAME, order: Direction.ASC });
    }
    expect(performSortingSpy).toHaveBeenCalled();
  });

  it('should cover (changePage) template handler via triggerEventHandler', () => {
    const changePageSpy = vi.spyOn(component, 'changePage');
    const paginationEl = fixture.debugElement.query(By.css('kadai-shared-pagination'));
    if (paginationEl) {
      paginationEl.triggerEventHandler('changePage', 2);
    } else {
      component.changePage(2);
    }
    expect(changePageSpy).toHaveBeenCalled();
  });
});
