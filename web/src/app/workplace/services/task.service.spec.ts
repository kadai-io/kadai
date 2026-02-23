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
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { TaskService } from './task.service';
import { StartupService } from '../../shared/services/startup/startup.service';
import { Task } from '../models/task';

describe('TaskService', () => {
  let service: TaskService;
  let httpMock: HttpTestingController;

  const mockStartupService = {
    getKadaiRestUrl: vi.fn().mockReturnValue('http://test'),
    getKadaiLogoutUrl: vi.fn().mockReturnValue(''),
    router: {}
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        TaskService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: StartupService, useValue: mockStartupService }
      ]
    });

    service = TestBed.inject(TaskService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('url getter', () => {
    it('should return base url from startup service plus /v1/tasks', () => {
      expect(service.url).toBe('http://test/v1/tasks');
    });
  });

  describe('publishUpdatedTask', () => {
    it('should emit the task via taskChangedStream', () => {
      const task = new Task('task-1');
      let emitted: Task;
      service.taskChangedStream.subscribe((t) => (emitted = t));
      service.publishUpdatedTask(task);
      expect(emitted).toBe(task);
    });

    it('should emit undefined when called without argument', () => {
      let emitted: Task = null;
      service.taskChangedStream.subscribe((t) => (emitted = t));
      service.publishUpdatedTask();
      expect(emitted).toBeUndefined();
    });
  });

  describe('publishTaskDeletion', () => {
    it('should emit null via taskDeletedStream', () => {
      let emitted: any = 'not null';
      service.taskDeletedStream.subscribe((t) => (emitted = t));
      service.publishTaskDeletion();
      expect(emitted).toBeNull();
    });
  });

  describe('selectTask', () => {
    it('should emit the task via taskSelectedStream', () => {
      const task = new Task('task-2');
      let emitted: Task;
      service.taskSelectedStream.subscribe((t) => (emitted = t));
      service.selectTask(task);
      expect(emitted).toBe(task);
    });

    it('should emit undefined when called without argument', () => {
      let emitted: Task = null;
      service.taskSelectedStream.subscribe((t) => (emitted = t));
      service.selectTask();
      expect(emitted).toBeUndefined();
    });
  });

  describe('getSelectedTask', () => {
    it('should return an observable from taskSelectedStream', () => {
      const obs = service.getSelectedTask();
      expect(obs).toBeDefined();
      expect(typeof obs.subscribe).toBe('function');
    });
  });

  describe('findTasksWithWorkbasket', () => {
    it('should make a GET request to tasks url with query params', () => {
      const filterParam = { workbasketId: ['wb-1'] };
      const sortParam = {};
      const pagingParam = { 'page-size': 10 };

      service.findTasksWithWorkbasket(filterParam as any, sortParam as any, pagingParam as any).subscribe();

      const req = httpMock.expectOne((r) => r.url.includes('/v1/tasks'));
      expect(req.request.method).toBe('GET');
      req.flush({ tasks: [] });
    });
  });

  describe('getTask', () => {
    it('should make a GET request to /tasks/{id}', () => {
      service.getTask('task-123').subscribe();

      const req = httpMock.expectOne('http://test/v1/tasks/task-123');
      expect(req.request.method).toBe('GET');
      req.flush({ taskId: 'task-123' });
    });
  });

  describe('completeTask', () => {
    it('should make a POST request to /tasks/{id}/complete', () => {
      service.completeTask('task-456').subscribe();

      const req = httpMock.expectOne('http://test/v1/tasks/task-456/complete');
      expect(req.request.method).toBe('POST');
      req.flush({ taskId: 'task-456' });
    });
  });

  describe('claimTask', () => {
    it('should make a POST request to /tasks/{id}/claim', () => {
      service.claimTask('task-789').subscribe();

      const req = httpMock.expectOne('http://test/v1/tasks/task-789/claim');
      expect(req.request.method).toBe('POST');
      req.flush({ taskId: 'task-789' });
    });
  });

  describe('cancelClaimTask', () => {
    it('should make a DELETE request to /tasks/{id}/claim', () => {
      service.cancelClaimTask('task-abc').subscribe();

      const req = httpMock.expectOne('http://test/v1/tasks/task-abc/claim');
      expect(req.request.method).toBe('DELETE');
      req.flush({});
    });
  });

  describe('transferTask', () => {
    it('should make a POST request to /tasks/{taskId}/transfer/{workbasketId}', () => {
      service.transferTask('task-1', 'wb-2').subscribe();

      const req = httpMock.expectOne('http://test/v1/tasks/task-1/transfer/wb-2');
      expect(req.request.method).toBe('POST');
      req.flush({});
    });
  });

  describe('updateTask', () => {
    it('should make a PUT request to /tasks/{taskId}', () => {
      const task = new Task('task-update');
      service.updateTask(task).subscribe();

      const req = httpMock.expectOne('http://test/v1/tasks/task-update');
      expect(req.request.method).toBe('PUT');
      req.flush({ taskId: 'task-update' });
    });

    it('should convert date attributes to ISO strings via convertTasksDatesToGMT', () => {
      const task = new Task('task-dates');
      task.created = '2024-01-15T10:00:00.000Z';
      task.planned = '2024-01-16T08:00:00.000Z';
      task.due = '2024-01-17T12:00:00.000Z';

      service.updateTask(task).subscribe();

      const req = httpMock.expectOne('http://test/v1/tasks/task-dates');
      expect(req.request.method).toBe('PUT');
      const body = req.request.body;
      expect(body.created).toBe(new Date('2024-01-15T10:00:00.000Z').toISOString());
      expect(body.planned).toBe(new Date('2024-01-16T08:00:00.000Z').toISOString());
      expect(body.due).toBe(new Date('2024-01-17T12:00:00.000Z').toISOString());
      req.flush({});
    });

    it('should leave undefined date attributes unchanged', () => {
      const task = new Task('task-no-dates');

      service.updateTask(task).subscribe();

      const req = httpMock.expectOne('http://test/v1/tasks/task-no-dates');
      const body = req.request.body;
      expect(body.created).toBeUndefined();
      expect(body.planned).toBeUndefined();
      req.flush({});
    });
  });

  describe('deleteTask', () => {
    it('should make a DELETE request to /tasks/{taskId}', () => {
      const task = new Task('task-del');
      service.deleteTask(task).subscribe();

      const req = httpMock.expectOne('http://test/v1/tasks/task-del');
      expect(req.request.method).toBe('DELETE');
      req.flush({});
    });
  });

  describe('createTask', () => {
    it('should make a POST request to /tasks', () => {
      const task = new Task('task-new');
      service.createTask(task).subscribe();

      const req = httpMock.expectOne('http://test/v1/tasks');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toBe(task);
      req.flush({ taskId: 'task-new' });
    });
  });
});
