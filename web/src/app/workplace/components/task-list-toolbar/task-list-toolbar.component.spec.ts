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
import { ActivatedRoute, Router } from '@angular/router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { of, Subject } from 'rxjs';
import { provideStore, Store } from '@ngxs/store';
import { By } from '@angular/platform-browser';

import { TaskListToolbarComponent } from './task-list-toolbar.component';
import { SearchType } from '../../models/search';
import { SelectTask, SetPage } from '../../../shared/store/task-store/task.actions';
import { TaskWorkflowState } from '../../../shared/store/task-store/task.state';
import { WorkplaceState } from '../../../shared/store/workplace-store/workplace.state';
import { FilterState } from '../../../shared/store/filter-store/filter.state';
import { SetTaskFilter } from '../../../shared/store/filter-store/filter.actions';
import { WorkbasketService } from '../../../shared/services/workbasket/workbasket.service';
import { TaskService } from '../../services/task.service';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';
import { KadaiEngineService } from '../../../shared/services/kadai-engine/kadai-engine.service';
import { UserInfo } from '../../../shared/models/user-info';
import { selectedWorkbasketMock } from '../../../shared/store/mock-data/mock-store';
import { Task } from '../../models/task';
import { ObjectReference } from '../../models/object-reference';
import { Direction, TaskQuerySortParameter } from '../../../shared/models/sorting';

const mockWorkbasket = selectedWorkbasketMock;
const mockTask = new Task('TKI:001', new ObjectReference(), mockWorkbasket);

describe('TaskListToolbarComponent', () => {
  let component: TaskListToolbarComponent;
  let fixture: ComponentFixture<TaskListToolbarComponent>;
  let store: Store;
  let workbasketServiceMock: Partial<WorkbasketService>;
  let routerMock: Partial<Router>;
  let queryParams$: Subject<any>;
  let kadaiEngineServiceMock: Partial<KadaiEngineService>;

  beforeEach(async () => {
    workbasketServiceMock = {
      getAllWorkBaskets: vi.fn().mockReturnValue(of({ workbaskets: [mockWorkbasket] }))
    };

    routerMock = {
      url: '',
      navigate: vi.fn()
    };

    queryParams$ = new Subject();

    kadaiEngineServiceMock = { currentUserInfo: new UserInfo('no-match-user', [], []) };

    await TestBed.configureTestingModule({
      imports: [TaskListToolbarComponent],
      providers: [
        provideStore([TaskWorkflowState, WorkplaceState, FilterState]),
        { provide: WorkbasketService, useValue: workbasketServiceMock },
        { provide: TaskService, useValue: {} },
        { provide: NotificationService, useValue: { showSuccess: vi.fn(), showInformation: vi.fn() } },
        { provide: RequestInProgressService, useValue: { setRequestInProgress: vi.fn() } },
        { provide: KadaiEngineService, useValue: kadaiEngineServiceMock },
        { provide: Router, useValue: routerMock },
        { provide: ActivatedRoute, useValue: { queryParams: queryParams$.asObservable(), parent: {} } }
      ]
    }).compileComponents();

    store = TestBed.inject(Store);
    fixture = TestBed.createComponent(TaskListToolbarComponent);
    component = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('selects the workbasket matching the current user and dispatches SelectWorkbasket', () => {
      TestBed.inject(KadaiEngineService).currentUserInfo = new UserInfo(mockWorkbasket.key ?? '', [], []);

      component.ngOnInit();
      queryParams$.next({});

      expect(store.snapshot().task.selectedWorkbasket).toEqual(mockWorkbasket);
      expect(component.workbasketSelected()).toBe(true);
    });

    it('does not auto-select a workbasket when none matches the current user', () => {
      component.ngOnInit();
      queryParams$.next({});

      expect(store.snapshot().task.selectedWorkbasket).toBeUndefined();
    });

    it('switches to the workbaskets tab and selects byWorkbasket search when component=workbaskets', () => {
      component.ngOnInit();
      queryParams$.next({ component: 'workbaskets' });

      expect(component.activeTab()).toBe(0);
      expect(store.snapshot().task.selectedSearchType).toBe(SearchType.byWorkbasket);
    });

    it('switches to the task-search tab and selects byTypeAndValue search when component=task-search', () => {
      component.ngOnInit();
      queryParams$.next({ component: 'task-search' });

      expect(component.activeTab()).toBe(1);
      expect(component.searched()).toBe(true);
      expect(store.snapshot().task.selectedSearchType).toBe(SearchType.byTypeAndValue);
    });

    it('syncs resultName/currentBasket and dispatches SelectWorkbasket when the selected task changes', () => {
      component.ngOnInit();
      queryParams$.next({});

      store.dispatch(new SelectTask(mockTask));

      expect(component.resultName()).toBe(mockWorkbasket.name);
      expect(store.snapshot().task.selectedWorkbasket).toEqual(mockWorkbasket);
    });

    it('skips a nameless workbasket and falls back to empty name/id for the matched user workbasket', () => {
      const namelessWorkbasket = { ...mockWorkbasket, name: undefined, workbasketId: undefined, key: 'no-name' };
      (workbasketServiceMock.getAllWorkBaskets as any).mockReturnValue(of({ workbaskets: [namelessWorkbasket] }));
      TestBed.inject(KadaiEngineService).currentUserInfo = new UserInfo('no-name', [], []);

      component.ngOnInit();
      queryParams$.next({});

      expect(component.workbasketNames).toEqual([]);
      expect(component.resultName()).toBe('');
      expect(component.resultId()).toBe('');
    });

    it('does not re-sync resultName when the selected task has no workbasketSummary', () => {
      component.ngOnInit();
      queryParams$.next({});

      store.dispatch(new SelectTask({ ...mockTask, workbasketSummary: undefined } as any));

      expect(component.resultName()).toBe('');
    });

    it('falls back to empty name/id when the selected task workbasket has none', () => {
      component.ngOnInit();
      queryParams$.next({});

      store.dispatch(
        new SelectTask({
          ...mockTask,
          workbasketSummary: { ...mockWorkbasket, name: undefined, workbasketId: undefined }
        } as any)
      );

      expect(component.resultName()).toBe('');
      expect(component.resultId()).toBe('');
    });

    it('applies the previously selected workbasket name/id when returning to the workbaskets tab', () => {
      TestBed.inject(KadaiEngineService).currentUserInfo = new UserInfo(mockWorkbasket.key ?? '', [], []);
      component.ngOnInit();
      queryParams$.next({});

      queryParams$.next({ component: 'workbaskets' });

      expect(component.resultName()).toBe(mockWorkbasket.name);
    });
  });

  describe('setFilterExpansion', () => {
    it('dispatches SetFilterExpansion', () => {
      const before = store.snapshot().WorkplaceState.isFilterExpanded;
      component.setFilterExpansion();
      expect(store.snapshot().WorkplaceState.isFilterExpanded).toBe(!before);
    });
  });

  describe('updateState', () => {
    it('dispatches SetTaskFilter with the current wildcard filter input', () => {
      component.filterInput.set('abc');
      component.updateState();

      expect(store.snapshot().FilterState.tasks['wildcard-search-value']).toEqual(['abc']);
    });
  });

  describe('filterWorkbasketNames', () => {
    it('filters workbasketNames by the current resultName', () => {
      component.workbasketNames = ['Foo', 'Bar', 'Foobar'];
      component.resultName.set('foo');

      component.filterWorkbasketNames();

      expect(component.filteredWorkbasketNames()).toEqual(['Foo', 'Foobar']);
    });
  });

  describe('searchBasket', () => {
    it('dispatches SelectWorkbasket when resultName matches a known workbasket', () => {
      component.workbaskets.set([mockWorkbasket]);
      component.resultName.set(mockWorkbasket.name!);

      component.searchBasket();

      expect(store.snapshot().task.selectedWorkbasket).toEqual(mockWorkbasket);
      expect(component.searched()).toBe(true);
    });

    it('clears the workbasket selection when resultId is empty', () => {
      component.workbaskets.set([mockWorkbasket]);
      component.resultName.set('no such workbasket');

      component.searchBasket();

      expect(store.snapshot().task.selectedWorkbasket).toBeUndefined();
    });
  });

  describe('sorting', () => {
    it('dispatches SetSort', () => {
      const sort = { 'sort-by': TaskQuerySortParameter.NAME, order: Direction.DESC };
      component.sorting(sort);

      expect(store.snapshot().task.sort).toEqual(sort);
    });
  });

  describe('onFilter', () => {
    it('resets to page 1', () => {
      component.onFilter();

      expect(store.snapshot().task.paging.page).toBe(1);
    });
  });

  describe('onClearFilter', () => {
    it('clears the task filter and resets to page 1', () => {
      store.dispatch(new SetTaskFilter({ 'name-like': ['x'] }));

      component.onClearFilter();

      expect(store.snapshot().task.paging.page).toBe(1);
      expect(store.snapshot().FilterState.tasks['name-like']).toEqual([]);
    });
  });

  describe('createTask', () => {
    it('clears the selected task and navigates to the new-task detail route', () => {
      store.dispatch(new SelectTask(mockTask));

      component.createTask();

      expect(store.snapshot().task.selectedTask).toBeUndefined();
      expect(routerMock.navigate).toHaveBeenCalledWith(
        [{ outlets: { detail: 'taskdetail/new-task' } }],
        expect.objectContaining({ queryParamsHandling: 'merge' })
      );
    });
  });

  describe('selectSearch', () => {
    it('sets searchSelected and dispatches SetSearchType', () => {
      component.selectSearch(SearchType.byTypeAndValue);

      expect(component.searchSelected).toBe(SearchType.byTypeAndValue);
      expect(store.snapshot().task.selectedSearchType).toBe(SearchType.byTypeAndValue);
    });
  });

  describe('rendering', () => {
    it('renders the workbaskets tab collapsed by default', () => {
      fixture.detectChanges();

      const expandButton = fixture.nativeElement.querySelector('button[mattooltip="Display more filter options"]');
      expect(expandButton).toBeTruthy();
      expect(fixture.nativeElement.querySelector('.task-list-toolbar__additional-filter')).toBeFalsy();
      expect(fixture.nativeElement.querySelector('.task-list-toolbar__additional-toolbar')).toBeFalsy();
    });

    it('shows the additional filter panel once expanded (covers both async-pipe branches)', () => {
      fixture.detectChanges();

      component.setFilterExpansion();
      fixture.detectChanges();
      expect(fixture.nativeElement.querySelector('.task-list-toolbar__additional-filter')).toBeTruthy();
      expect(fixture.nativeElement.querySelector('kadai-shared-task-filter')).toBeTruthy();

      component.setFilterExpansion();
      fixture.detectChanges();
      expect(fixture.nativeElement.querySelector('.task-list-toolbar__additional-filter')).toBeFalsy();
    });

    it('shows the additional toolbar with add/sort once searched is set', () => {
      fixture.detectChanges();

      component.searched.set(true);
      fixture.detectChanges();

      const addButton = fixture.nativeElement.querySelector('button[mattooltip="Add Task"]');
      expect(addButton).toBeTruthy();
      expect(fixture.nativeElement.querySelector('kadai-shared-sort')).toBeTruthy();
    });

    it('calls setFilterExpansion when the expand button is clicked', () => {
      fixture.detectChanges();
      const before = store.snapshot().WorkplaceState.isFilterExpanded;

      const expandButton = fixture.nativeElement.querySelector('button[mattooltip="Display more filter options"]');
      expandButton.click();
      fixture.detectChanges();

      expect(store.snapshot().WorkplaceState.isFilterExpanded).toBe(!before);
    });

    it('calls onFilter when the workbaskets-tab search button is clicked', () => {
      fixture.detectChanges();
      store.dispatch(new SetPage(4));

      const searchButton = fixture.nativeElement.querySelector('button[matTooltip="Filter Tasks"]');
      searchButton.click();
      fixture.detectChanges();

      expect(store.snapshot().task.paging.page).toBe(1);
    });

    it('calls onClearFilter when the workbaskets-tab clear button is clicked', () => {
      fixture.detectChanges();
      store.dispatch(new SetTaskFilter({ 'name-like': ['x'] }));

      const clearButton = fixture.nativeElement.querySelector('button[matTooltip="Clear Filter"]');
      clearButton.click();
      fixture.detectChanges();

      expect(store.snapshot().FilterState.tasks['name-like']).toEqual([]);
    });

    it('calls createTask when the Add Task button is clicked', () => {
      fixture.detectChanges();
      component.searched.set(true);
      fixture.detectChanges();

      const addButton = fixture.nativeElement.querySelector('button[mattooltip="Add Task"]');
      addButton.click();

      expect(routerMock.navigate).toHaveBeenCalledWith(
        [{ outlets: { detail: 'taskdetail/new-task' } }],
        expect.objectContaining({ queryParamsHandling: 'merge' })
      );
    });

    it('updates resultName and filteredWorkbasketNames when typing in the workbasket input', () => {
      fixture.detectChanges();
      component.workbasketNames = ['Foo', 'Bar'];

      const input = fixture.nativeElement.querySelector('.task-list-toolbar__filter--workbasket input');
      input.value = 'Fo';
      input.dispatchEvent(new Event('input'));
      fixture.detectChanges();

      expect(component.resultName()).toBe('Fo');
      expect(component.filteredWorkbasketNames()).toEqual(['Foo']);
    });

    it('updates filterInput when typing in the task-search filter and dispatches SetTaskFilter', () => {
      component.activeTab.set(1);
      fixture.detectChanges();

      const input = fixture.nativeElement.querySelector('.task-list-toolbar__filter-input input');
      input.value = 'abc';
      input.dispatchEvent(new Event('input'));
      fixture.detectChanges();

      expect(component.filterInput()).toBe('abc');
      expect(store.snapshot().FilterState.tasks['wildcard-search-value']).toEqual(['abc']);
    });

    it('sets searched and calls onFilter when pressing enter in the task-search filter', () => {
      component.activeTab.set(1);
      fixture.detectChanges();
      store.dispatch(new SetPage(4));

      const input = fixture.nativeElement.querySelector('.task-list-toolbar__filter-input input');
      input.dispatchEvent(new KeyboardEvent('keyup', { key: 'Enter' }));
      fixture.detectChanges();

      expect(component.searched()).toBe(true);
      expect(store.snapshot().task.paging.page).toBe(1);
    });

    it('calls onFilter and sets searched when the task-search search button is clicked', () => {
      component.activeTab.set(1);
      fixture.detectChanges();
      store.dispatch(new SetPage(4));

      const searchButton = fixture.nativeElement.querySelector('button[matTooltip="Filter Tasks"]');
      searchButton.click();
      fixture.detectChanges();

      expect(component.searched()).toBe(true);
      expect(store.snapshot().task.paging.page).toBe(1);
    });

    it('calls onClearFilter and sets searched when the task-search clear button is clicked', () => {
      component.activeTab.set(1);
      fixture.detectChanges();
      store.dispatch(new SetTaskFilter({ 'name-like': ['x'] }));

      const clearButton = fixture.nativeElement.querySelector('button[matTooltip="Clear Filter"]');
      clearButton.click();
      fixture.detectChanges();

      expect(component.searched()).toBe(true);
      expect(store.snapshot().FilterState.tasks['name-like']).toEqual([]);
    });

    it('calls sorting when the sort component emits performSorting', () => {
      fixture.detectChanges();
      component.searched.set(true);
      fixture.detectChanges();

      const sortDebug = fixture.debugElement.query(By.css('kadai-shared-sort'));
      const sort = { 'sort-by': TaskQuerySortParameter.NAME, order: Direction.DESC };
      sortDebug.triggerEventHandler('performSorting', sort);

      expect(store.snapshot().task.sort).toEqual(sort);
    });

    it('calls onTabChange when a tab is clicked', () => {
      fixture.detectChanges();

      const tabLabel = fixture.nativeElement.querySelector('div[role="tab"]');
      Object.defineProperty(tabLabel, 'innerText', { value: 'Workbaskets', configurable: true });
      tabLabel.dispatchEvent(new MouseEvent('click', { bubbles: true }));
      fixture.detectChanges();

      expect(routerMock.navigate).toHaveBeenCalled();
    });
  });
});
