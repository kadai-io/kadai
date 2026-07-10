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

import { TaskWorkflowState } from './task.state';
import { FilterState } from '../filter-store/filter.state';
import {
  CreateTask,
  DeleteTask,
  GetTask,
  LoadTasks,
  SelectTask,
  SelectWorkbasket,
  SetPage,
  SetPageSize,
  SetSearchType,
  SetSort,
  UpdateTask
} from './task.actions';

import { TaskService } from '../../../workplace/services/task.service';
import { NotificationService } from '../../services/notifications/notification.service';
import { RequestInProgressService } from '../../services/request-in-progress/request-in-progress.service';
import { Task } from '../../../workplace/models/task';
import { ObjectReference } from '../../../workplace/models/object-reference';
import { SearchType } from '../../../workplace/models/search';
import { Direction, TaskQuerySortParameter } from '../../models/sorting';
import { selectedWorkbasketMock } from '../mock-data/mock-store';

const mockWorkbasket = selectedWorkbasketMock;

const mockTask = new Task('TKI:001', new ObjectReference(), mockWorkbasket);
const mockTask2 = new Task('TKI:002', new ObjectReference(), mockWorkbasket);

const initialTaskState = {
  tasks: [],
  page: {},
  selectedTask: undefined,
  selectedWorkbasket: undefined,
  selectedSearchType: SearchType.byWorkbasket,
  sort: {
    'sort-by': TaskQuerySortParameter.PRIORITY,
    order: Direction.ASC
  },
  paging: {
    page: 1,
    'page-size': 9
  }
};

describe('TaskWorkflowState', () => {
  let store: Store;
  let taskServiceMock: Partial<TaskService>;
  let notificationServiceMock: Partial<NotificationService>;
  let requestInProgressServiceMock: Partial<RequestInProgressService>;

  beforeEach(async () => {
    taskServiceMock = {
      findTasksWithWorkbasket: vi
        .fn()
        .mockReturnValue(of({ tasks: [mockTask, mockTask2], page: { totalElements: 2 } })),
      getTask: vi.fn().mockReturnValue(of(mockTask)),
      createTask: vi.fn().mockReturnValue(of(mockTask)),
      updateTask: vi.fn().mockReturnValue(of(mockTask)),
      deleteTask: vi.fn().mockReturnValue(of(mockTask))
    };

    notificationServiceMock = {
      showSuccess: vi.fn(),
      showInformation: vi.fn(),
      showError: vi.fn()
    };

    requestInProgressServiceMock = {
      setRequestInProgress: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [NgxsModule.forRoot([TaskWorkflowState, FilterState])],
      providers: [
        { provide: TaskService, useValue: taskServiceMock },
        { provide: NotificationService, useValue: notificationServiceMock },
        { provide: RequestInProgressService, useValue: requestInProgressServiceMock }
      ]
    }).compileComponents();

    store = TestBed.inject(Store);

    store.reset({
      ...store.snapshot(),
      task: { ...initialTaskState }
    });
  });

  describe('LoadTasks', () => {
    it('does not call the API and clears the list when searching by workbasket with none selected', async () => {
      await store.dispatch(new LoadTasks()).toPromise();

      expect(taskServiceMock.findTasksWithWorkbasket).not.toHaveBeenCalled();
      expect(store.snapshot().task.tasks).toEqual([]);
    });

    it('fetches tasks for the selected workbasket and stores the result', async () => {
      store.reset({
        ...store.snapshot(),
        task: { ...initialTaskState, selectedWorkbasket: mockWorkbasket }
      });

      await store.dispatch(new LoadTasks()).toPromise();

      expect(taskServiceMock.findTasksWithWorkbasket).toHaveBeenCalledWith(
        expect.objectContaining({ 'workbasket-id': [mockWorkbasket.workbasketId] }),
        initialTaskState.sort,
        initialTaskState.paging
      );
      expect(store.snapshot().task.tasks).toEqual([mockTask, mockTask2]);
      expect(requestInProgressServiceMock.setRequestInProgress).toHaveBeenCalledWith(true);
      expect(requestInProgressServiceMock.setRequestInProgress).toHaveBeenCalledWith(false);
    });

    it('shows an information toast when the selected workbasket has no tasks', async () => {
      (taskServiceMock.findTasksWithWorkbasket as ReturnType<typeof vi.fn>).mockReturnValue(
        of({ tasks: [], page: {} })
      );
      store.reset({
        ...store.snapshot(),
        task: { ...initialTaskState, selectedWorkbasket: mockWorkbasket }
      });

      await store.dispatch(new LoadTasks()).toPromise();

      expect(notificationServiceMock.showInformation).toHaveBeenCalledWith('EMPTY_WORKBASKET');
    });

    it('does not filter by workbasket-id when searching by type and value', async () => {
      store.reset({
        ...store.snapshot(),
        task: { ...initialTaskState, selectedSearchType: SearchType.byTypeAndValue, selectedWorkbasket: mockWorkbasket }
      });

      await store.dispatch(new LoadTasks()).toPromise();

      const filterArgument = (taskServiceMock.findTasksWithWorkbasket as ReturnType<typeof vi.fn>).mock.calls[0][0];
      expect(filterArgument['workbasket-id']).toBeUndefined();
    });

    it('clears requestInProgress and rethrows on error', async () => {
      (taskServiceMock.findTasksWithWorkbasket as ReturnType<typeof vi.fn>).mockReturnValue(
        throwError(() => new Error('boom'))
      );
      store.reset({
        ...store.snapshot(),
        task: { ...initialTaskState, selectedWorkbasket: mockWorkbasket }
      });

      await expect(store.dispatch(new LoadTasks()).toPromise()).rejects.toThrow('boom');
      expect(requestInProgressServiceMock.setRequestInProgress).toHaveBeenCalledWith(false);
    });
  });

  describe('SelectWorkbasket', () => {
    it('sets selectedWorkbasket and reloads when searching by workbasket', async () => {
      await store.dispatch(new SelectWorkbasket(mockWorkbasket)).toPromise();

      expect(store.snapshot().task.selectedWorkbasket).toEqual(mockWorkbasket);
      expect(taskServiceMock.findTasksWithWorkbasket).toHaveBeenCalled();
    });

    it('does not reload when searching by type and value', async () => {
      store.reset({
        ...store.snapshot(),
        task: { ...initialTaskState, selectedSearchType: SearchType.byTypeAndValue }
      });

      await store.dispatch(new SelectWorkbasket(mockWorkbasket)).toPromise();

      expect(store.snapshot().task.selectedWorkbasket).toEqual(mockWorkbasket);
      expect(taskServiceMock.findTasksWithWorkbasket).not.toHaveBeenCalled();
    });
  });

  describe('SetSearchType', () => {
    it('sets the search type and clears the task list', async () => {
      store.reset({
        ...store.snapshot(),
        task: { ...initialTaskState, tasks: [mockTask] }
      });

      await store.dispatch(new SetSearchType(SearchType.byTypeAndValue)).toPromise();

      const state = store.snapshot().task;
      expect(state.selectedSearchType).toBe(SearchType.byTypeAndValue);
      expect(state.tasks).toEqual([]);
    });
  });

  describe('SetSort / SetPage / SetPageSize', () => {
    it('SetSort patches sort and reloads', async () => {
      store.reset({
        ...store.snapshot(),
        task: { ...initialTaskState, selectedWorkbasket: mockWorkbasket }
      });
      const newSort = { 'sort-by': TaskQuerySortParameter.NAME, order: Direction.DESC };
      await store.dispatch(new SetSort(newSort)).toPromise();

      expect(store.snapshot().task.sort).toEqual(newSort);
      expect(taskServiceMock.findTasksWithWorkbasket).toHaveBeenCalled();
    });

    it('SetPage patches paging.page and reloads', async () => {
      store.reset({
        ...store.snapshot(),
        task: { ...initialTaskState, selectedWorkbasket: mockWorkbasket }
      });
      await store.dispatch(new SetPage(3)).toPromise();

      expect(store.snapshot().task.paging.page).toBe(3);
      expect(taskServiceMock.findTasksWithWorkbasket).toHaveBeenCalled();
    });

    it('SetPageSize patches paging[page-size] and reloads', async () => {
      store.reset({
        ...store.snapshot(),
        task: { ...initialTaskState, selectedWorkbasket: mockWorkbasket }
      });
      await store.dispatch(new SetPageSize(12)).toPromise();

      expect(store.snapshot().task.paging['page-size']).toBe(12);
      expect(taskServiceMock.findTasksWithWorkbasket).toHaveBeenCalled();
    });
  });

  describe('SelectTask', () => {
    it('sets selectedTask', async () => {
      await store.dispatch(new SelectTask(mockTask)).toPromise();

      expect(store.snapshot().task.selectedTask).toEqual(mockTask);
    });

    it('clears selectedTask when given undefined', async () => {
      store.reset({
        ...store.snapshot(),
        task: { ...initialTaskState, selectedTask: mockTask }
      });

      await store.dispatch(new SelectTask(undefined)).toPromise();

      expect(store.snapshot().task.selectedTask).toBeUndefined();
    });
  });

  describe('GetTask', () => {
    it('fetches a task by id and selects it', async () => {
      await store.dispatch(new GetTask('TKI:001')).toPromise();

      expect(taskServiceMock.getTask).toHaveBeenCalledWith('TKI:001');
      expect(store.snapshot().task.selectedTask).toEqual(mockTask);
      expect(requestInProgressServiceMock.setRequestInProgress).toHaveBeenCalledWith(true);
      expect(requestInProgressServiceMock.setRequestInProgress).toHaveBeenCalledWith(false);
    });

    it('builds a blank task locally for id "new-task" without calling the API', async () => {
      store.reset({
        ...store.snapshot(),
        task: { ...initialTaskState, selectedWorkbasket: mockWorkbasket }
      });

      await store.dispatch(new GetTask('new-task')).toPromise();

      expect(taskServiceMock.getTask).not.toHaveBeenCalled();
      const selectedTask = store.snapshot().task.selectedTask;
      expect(selectedTask.taskId).toBe('');
      expect(selectedTask.workbasketSummary).toEqual(mockWorkbasket);
      expect(requestInProgressServiceMock.setRequestInProgress).toHaveBeenCalledWith(true);
      expect(requestInProgressServiceMock.setRequestInProgress).toHaveBeenCalledWith(false);
    });

    it('clears requestInProgress and rethrows on error', async () => {
      (taskServiceMock.getTask as ReturnType<typeof vi.fn>).mockReturnValue(throwError(() => new Error('boom')));

      await expect(store.dispatch(new GetTask('TKI:001')).toPromise()).rejects.toThrow('boom');
      expect(requestInProgressServiceMock.setRequestInProgress).toHaveBeenCalledWith(false);
    });
  });

  describe('CreateTask', () => {
    it('creates the task, selects it, and reloads the list', async () => {
      store.reset({
        ...store.snapshot(),
        task: { ...initialTaskState, selectedWorkbasket: mockWorkbasket }
      });
      await store.dispatch(new CreateTask(mockTask)).toPromise();

      expect(taskServiceMock.createTask).toHaveBeenCalledWith(mockTask);
      expect(store.snapshot().task.selectedTask).toEqual(mockTask);
      expect(notificationServiceMock.showSuccess).toHaveBeenCalledWith('TASK_CREATE', { taskName: mockTask.name });
      expect(taskServiceMock.findTasksWithWorkbasket).toHaveBeenCalled();
      expect(requestInProgressServiceMock.setRequestInProgress).toHaveBeenCalledWith(true);
      expect(requestInProgressServiceMock.setRequestInProgress).toHaveBeenCalledWith(false);
    });

    it('clears requestInProgress and rethrows on error', async () => {
      (taskServiceMock.createTask as ReturnType<typeof vi.fn>).mockReturnValue(throwError(() => new Error('boom')));

      await expect(store.dispatch(new CreateTask(mockTask)).toPromise()).rejects.toThrow('boom');
      expect(requestInProgressServiceMock.setRequestInProgress).toHaveBeenCalledWith(false);
    });
  });

  describe('UpdateTask', () => {
    it('updates the task, refreshes the selection, and reloads the list', async () => {
      store.reset({
        ...store.snapshot(),
        task: { ...initialTaskState, selectedWorkbasket: mockWorkbasket }
      });
      await store.dispatch(new UpdateTask(mockTask)).toPromise();

      expect(taskServiceMock.updateTask).toHaveBeenCalledWith(mockTask);
      expect(store.snapshot().task.selectedTask).toEqual(mockTask);
      expect(notificationServiceMock.showSuccess).toHaveBeenCalledWith('TASK_UPDATE', { taskName: mockTask.name });
      expect(taskServiceMock.findTasksWithWorkbasket).toHaveBeenCalled();
      expect(requestInProgressServiceMock.setRequestInProgress).toHaveBeenCalledWith(true);
      expect(requestInProgressServiceMock.setRequestInProgress).toHaveBeenCalledWith(false);
    });

    it('clears requestInProgress and rethrows on error', async () => {
      (taskServiceMock.updateTask as ReturnType<typeof vi.fn>).mockReturnValue(throwError(() => new Error('boom')));

      await expect(store.dispatch(new UpdateTask(mockTask)).toPromise()).rejects.toThrow('boom');
      expect(requestInProgressServiceMock.setRequestInProgress).toHaveBeenCalledWith(false);
    });
  });

  describe('DeleteTask', () => {
    it('deletes the task, clears the selection, and reloads the list', async () => {
      store.reset({
        ...store.snapshot(),
        task: { ...initialTaskState, selectedTask: mockTask, selectedWorkbasket: mockWorkbasket }
      });

      await store.dispatch(new DeleteTask(mockTask)).toPromise();

      expect(taskServiceMock.deleteTask).toHaveBeenCalledWith(mockTask);
      expect(store.snapshot().task.selectedTask).toBeUndefined();
      expect(notificationServiceMock.showSuccess).toHaveBeenCalledWith('TASK_DELETE', { taskName: mockTask.name });
      expect(taskServiceMock.findTasksWithWorkbasket).toHaveBeenCalled();
    });
  });
});
