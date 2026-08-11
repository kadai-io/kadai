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
import { provideStore, Store } from '@ngxs/store';
import { beforeEach, describe, expect, it } from 'vitest';

import { TaskWorkflowState } from './task.state';
import { TaskSelectors } from './task.selectors';
import { TaskService } from '../../../workplace/services/task.service';
import { NotificationService } from '../../services/notifications/notification.service';
import { RequestInProgressService } from '../../services/request-in-progress/request-in-progress.service';
import { Task } from '../../../workplace/models/task';
import { ObjectReference } from '../../../workplace/models/object-reference';
import { selectedWorkbasketMock } from '../mock-data/mock-store';

describe('TaskSelectors', () => {
  let store: Store;
  const mockWorkbasket = selectedWorkbasketMock;
  const mockTask = new Task('TKI:001', new ObjectReference(), mockWorkbasket);

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [],
      providers: [
        provideStore([TaskWorkflowState]),
        { provide: TaskService, useValue: {} },
        { provide: NotificationService, useValue: {} },
        { provide: RequestInProgressService, useValue: { setRequestInProgress: () => {} } }
      ]
    }).compileComponents();

    store = TestBed.inject(Store);
    store.reset({
      ...store.snapshot(),
      task: {
        ...store.snapshot().task,
        tasks: [mockTask],
        selectedTask: mockTask,
        selectedWorkbasket: mockWorkbasket
      }
    });
  });

  it('getTasks returns the task list', () => {
    expect(store.selectSnapshot(TaskSelectors.getTasks)).toEqual([mockTask]);
  });

  it('getPage returns the page info', () => {
    expect(store.selectSnapshot(TaskSelectors.getPage)).toEqual(store.snapshot().task.page);
  });

  it('getSelectedTask returns the selected task', () => {
    expect(store.selectSnapshot(TaskSelectors.getSelectedTask)).toEqual(mockTask);
  });

  it('getSelectedWorkbasket returns the selected workbasket', () => {
    expect(store.selectSnapshot(TaskSelectors.getSelectedWorkbasket)).toEqual(mockWorkbasket);
  });

  it('getSearchType returns the search type', () => {
    expect(store.selectSnapshot(TaskSelectors.getSearchType)).toEqual(store.snapshot().task.selectedSearchType);
  });

  it('getSort returns the sort order', () => {
    expect(store.selectSnapshot(TaskSelectors.getSort)).toEqual(store.snapshot().task.sort);
  });

  it('getPaging returns the paging parameters', () => {
    expect(store.selectSnapshot(TaskSelectors.getPaging)).toEqual(store.snapshot().task.paging);
  });
});
