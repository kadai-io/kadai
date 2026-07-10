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
import { Component, input, model } from '@angular/core';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideAngularSvgIcon } from 'angular-svg-icon';
import { By } from '@angular/platform-browser';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { TaskMasterComponent } from './task-master.component';
import { TaskListToolbarComponent } from '../task-list-toolbar/task-list-toolbar.component';
import { TaskListComponent } from '../task-list/task-list.component';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { provideStore, Store } from '@ngxs/store';
import { TaskWorkflowState } from '../../../shared/store/task-store/task.state';
import { WorkplaceState } from '../../../shared/store/workplace-store/workplace.state';
import { FilterState } from '../../../shared/store/filter-store/filter.state';
import { TaskService } from '../../services/task.service';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';
import { OrientationService } from '../../../shared/services/orientation/orientation.service';
import { Orientation } from '../../../shared/models/orientation';
import { Task } from '../../models/task';
import { ObjectReference } from '../../models/object-reference';
import { SelectTask, SelectWorkbasket, SetPageSize } from '../../../shared/store/task-store/task.actions';
import { selectedWorkbasketMock } from '../../../shared/store/mock-data/mock-store';
import { WorkbasketService } from '../../../shared/services/workbasket/workbasket.service';
import { KadaiEngineService } from '../../../shared/services/kadai-engine/kadai-engine.service';
import { UserInfo } from '../../../shared/models/user-info';

@Component({ selector: 'kadai-task-list-toolbar', template: '', standalone: true })
class StubTaskListToolbarComponent {
  taskDefaultSortBy = input<any>();
}

@Component({ selector: 'kadai-task-list', template: '', standalone: true })
class StubTaskListComponent {
  tasks = input<any>();
  selectedId = model<string>();
}

@Component({ selector: 'kadai-shared-pagination', template: '', standalone: true })
class StubPaginationComponent {
  numberOfItems = input<any>();
  page = input<any>();
  type = input<any>();
}

describe('TaskMasterComponent', () => {
  let component: TaskMasterComponent;
  let fixture: ComponentFixture<TaskMasterComponent>;
  let store: Store;
  let taskServiceMock: Partial<TaskService>;
  let orientationServiceMock: Partial<OrientationService>;
  let requestInProgressSubject: BehaviorSubject<boolean>;

  const mockTask = new Task('TKI:001', new ObjectReference());

  beforeEach(async () => {
    taskServiceMock = {
      findTasksWithWorkbasket: vi.fn().mockReturnValue(of({ tasks: [mockTask], page: { totalElements: 1 } }))
    };

    orientationServiceMock = {
      getOrientation: vi.fn().mockReturnValue(of(Orientation.landscape))
    };

    requestInProgressSubject = new BehaviorSubject<boolean>(false);

    await TestBed.configureTestingModule({
      imports: [TaskMasterComponent],
      providers: [
        provideStore([TaskWorkflowState, WorkplaceState, FilterState]),
        { provide: TaskService, useValue: taskServiceMock },
        { provide: NotificationService, useValue: { showSuccess: vi.fn(), showInformation: vi.fn() } },
        {
          provide: RequestInProgressService,
          useValue: {
            setRequestInProgress: vi.fn(),
            getRequestInProgress: vi.fn().mockReturnValue(requestInProgressSubject.asObservable())
          }
        },
        { provide: OrientationService, useValue: orientationServiceMock }
      ]
    })
      .overrideComponent(TaskMasterComponent, {
        remove: { imports: [TaskListToolbarComponent, TaskListComponent, PaginationComponent] },
        add: { imports: [StubTaskListToolbarComponent, StubTaskListComponent, StubPaginationComponent] }
      })
      .compileComponents();

    store = TestBed.inject(Store);
    fixture = TestBed.createComponent(TaskMasterComponent);
    component = fixture.componentInstance;
  });

  it('creates and loads tasks for the initial card count on init', () => {
    store.dispatch(new SelectWorkbasket(selectedWorkbasketMock));
    fixture.detectChanges();

    expect(taskServiceMock.findTasksWithWorkbasket).toHaveBeenCalled();
  });

  it('exposes tasks from TaskState via a signal', () => {
    store.dispatch(new SelectWorkbasket(selectedWorkbasketMock));
    store.dispatch(new SetPageSize(9));
    fixture.detectChanges();

    expect(component.tasks()).toEqual([mockTask]);
  });

  it('dispatches SetPageSize when the number of cards changes', () => {
    fixture.detectChanges();

    const state = store.snapshot().task;
    expect(state.paging['page-size']).toBe(store.snapshot().WorkplaceState.cards);
  });

  it('changePage dispatches SetPage', () => {
    fixture.detectChanges();

    component.changePage(4);

    expect(store.snapshot().task.paging.page).toBe(4);
  });

  it('reflects requestInProgress from RequestInProgressService', () => {
    fixture.detectChanges();
    expect(component.requestInProgress()).toBe(false);

    requestInProgressSubject.next(true);
    fixture.detectChanges();
    expect(component.requestInProgress()).toBe(true);
  });

  it('resolves selectedId from the selected task once one is chosen', () => {
    fixture.detectChanges();
    expect(component.selectedId()).toBe('');

    store.dispatch(new SelectTask(mockTask));
    fixture.detectChanges();

    expect(component.selectedId()).toBe(mockTask.taskId);
  });

  it('renders the task list once loaded and hides it while a request is in progress', () => {
    store.dispatch(new SelectWorkbasket(selectedWorkbasketMock));
    store.dispatch(new SetPageSize(9));
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('kadai-task-list')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('kadai-shared-pagination')).toBeTruthy();

    requestInProgressSubject.next(true);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('kadai-task-list')).toBeFalsy();
  });

  it('does not render pagination when there are no tasks', () => {
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('kadai-shared-pagination')).toBeFalsy();
  });
});

describe('TaskMasterComponent - HTML template without overrideComponent', () => {
  let component: TaskMasterComponent;
  let fixture: ComponentFixture<TaskMasterComponent>;
  let store: Store;
  let routerMock: Partial<Router>;
  let queryParams$: Subject<any>;

  const mockTask = new Task('TKI:001', new ObjectReference());

  beforeEach(async () => {
    queryParams$ = new Subject();
    routerMock = { url: '', navigate: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [TaskMasterComponent],
      providers: [
        provideStore([TaskWorkflowState, WorkplaceState, FilterState]),
        {
          provide: TaskService,
          useValue: {
            findTasksWithWorkbasket: vi.fn().mockReturnValue(of({ tasks: [mockTask], page: { totalElements: 1 } }))
          }
        },
        { provide: NotificationService, useValue: { showSuccess: vi.fn(), showInformation: vi.fn() } },
        {
          provide: RequestInProgressService,
          useValue: { setRequestInProgress: vi.fn(), getRequestInProgress: vi.fn().mockReturnValue(of(false)) }
        },
        {
          provide: OrientationService,
          useValue: { getOrientation: vi.fn().mockReturnValue(of(Orientation.landscape)) }
        },
        {
          provide: WorkbasketService,
          useValue: { getAllWorkBaskets: vi.fn().mockReturnValue(of({ workbaskets: [selectedWorkbasketMock] })) }
        },
        { provide: KadaiEngineService, useValue: { currentUserInfo: new UserInfo('no-match-user', [], []) } },
        { provide: Router, useValue: routerMock },
        { provide: ActivatedRoute, useValue: { queryParams: queryParams$.asObservable(), parent: {} } },
        provideNoopAnimations(),
        provideAngularSvgIcon(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    store = TestBed.inject(Store);
    fixture = TestBed.createComponent(TaskMasterComponent);
    component = fixture.componentInstance;
  });

  it('renders the toolbar, task list and pagination with real child components', () => {
    store.dispatch(new SelectWorkbasket(selectedWorkbasketMock));
    store.dispatch(new SetPageSize(9));
    fixture.detectChanges();
    queryParams$.next({});

    expect(fixture.nativeElement.querySelector('kadai-task-list-toolbar')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('kadai-task-list')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('kadai-shared-pagination')).toBeTruthy();
  });

  it('hides the task list while a request is in progress', () => {
    const requestInProgressService = TestBed.inject(RequestInProgressService) as any;
    requestInProgressService.getRequestInProgress = vi.fn().mockReturnValue(of(true));

    fixture = TestBed.createComponent(TaskMasterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    queryParams$.next({});

    expect(fixture.nativeElement.querySelector('kadai-task-list')).toBeFalsy();
  });

  it('propagates the selectedId from the task list up through the master component', () => {
    fixture.detectChanges();
    queryParams$.next({});

    store.dispatch(new SelectTask(mockTask));
    fixture.detectChanges();

    expect(component.selectedId()).toBe(mockTask.taskId);
  });

  it('forwards the changePage event from the pagination component', () => {
    store.dispatch(new SelectWorkbasket(selectedWorkbasketMock));
    store.dispatch(new SetPageSize(9));
    fixture.detectChanges();
    queryParams$.next({});

    const paginationDebug = fixture.debugElement.query(By.css('kadai-shared-pagination'));
    paginationDebug.triggerEventHandler('changePage', 3);

    expect(store.snapshot().task.paging.page).toBe(3);
  });
});
