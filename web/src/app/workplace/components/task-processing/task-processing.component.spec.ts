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
import { provideRouter, ActivatedRoute, Router } from '@angular/router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { of, Subject } from 'rxjs';
import { TaskProcessingComponent } from './task-processing.component';
import { TaskService } from '../../services/task.service';
import { WorkbasketService } from '../../../shared/services/workbasket/workbasket.service';
import { ClassificationsService } from '../../../shared/services/classifications/classifications.service';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';
import { Task } from '../../models/task';
import { Workbasket } from '../../../shared/models/workbasket';

const makeTask = (overrides: Partial<Task> = {}): Task => {
  const task = new Task(
    'task-id-1',
    undefined,
    { workbasketId: 'wb-1', name: 'Workbasket A' },
    { classificationId: 'class-1', applicationEntryPoint: 'https://example.com' },
    undefined,
    undefined,
    undefined,
    undefined,
    undefined,
    undefined,
    undefined,
    undefined,
    undefined,
    undefined,
    'My Task',
    undefined,
    undefined,
    undefined,
    undefined,
    false,
    false,
    1,
    [],
    []
  );
  return Object.assign(task, overrides);
};

const makeWorkbaskets = (): Workbasket[] => [
  { workbasketId: 'wb-1', name: 'Workbasket A' },
  { workbasketId: 'wb-2', name: 'Workbasket B' },
  { workbasketId: 'wb-3', name: 'Workbasket C' }
];

describe('TaskProcessingComponent', () => {
  let component: TaskProcessingComponent;
  let fixture: ComponentFixture<TaskProcessingComponent>;
  let paramsSubject: Subject<{ id: string }>;

  let mockTaskService: {
    claimTask: ReturnType<typeof vi.fn>;
    getTask: ReturnType<typeof vi.fn>;
    selectTask: ReturnType<typeof vi.fn>;
    publishUpdatedTask: ReturnType<typeof vi.fn>;
    transferTask: ReturnType<typeof vi.fn>;
    completeTask: ReturnType<typeof vi.fn>;
    cancelClaimTask: ReturnType<typeof vi.fn>;
  };
  let mockWorkbasketService: { getAllWorkBaskets: ReturnType<typeof vi.fn> };
  let mockClassificationsService: { getClassification: ReturnType<typeof vi.fn> };
  let mockRequestInProgressService: { setRequestInProgress: ReturnType<typeof vi.fn> };
  let mockRouter: { navigate: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    paramsSubject = new Subject<{ id: string }>();

    const task = makeTask();
    const workbaskets = makeWorkbaskets();

    mockTaskService = {
      claimTask: vi.fn().mockReturnValue(of(task)),
      getTask: vi.fn().mockReturnValue(of(task)),
      selectTask: vi.fn(),
      publishUpdatedTask: vi.fn(),
      transferTask: vi.fn().mockReturnValue(of(task)),
      completeTask: vi.fn().mockReturnValue(of(task)),
      cancelClaimTask: vi.fn().mockReturnValue(of(task))
    };

    mockWorkbasketService = {
      getAllWorkBaskets: vi.fn().mockReturnValue(of({ workbaskets }))
    };

    mockClassificationsService = {
      getClassification: vi
        .fn()
        .mockReturnValue(of({ classificationId: 'class-1', applicationEntryPoint: 'https://example.com' }))
    };

    mockRequestInProgressService = {
      setRequestInProgress: vi.fn()
    };

    mockRouter = {
      navigate: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [TaskProcessingComponent],
      providers: [
        provideRouter([]),
        { provide: TaskService, useValue: mockTaskService },
        { provide: WorkbasketService, useValue: mockWorkbasketService },
        { provide: ClassificationsService, useValue: mockClassificationsService },
        { provide: RequestInProgressService, useValue: mockRequestInProgressService },
        {
          provide: ActivatedRoute,
          useValue: {
            params: paramsSubject.asObservable(),
            parent: null
          }
        },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TaskProcessingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit()', () => {
    it('should subscribe to route params', () => {
      expect(component.routeSubscription).toBeDefined();
    });

    it('should call claimTask with the id from route params', async () => {
      paramsSubject.next({ id: 'task-abc' });
      await fixture.whenStable();

      expect(mockTaskService.claimTask).toHaveBeenCalledWith('task-abc');
    });

    it('should call publishUpdatedTask after claiming', async () => {
      const claimedTask = makeTask();
      mockTaskService.claimTask.mockReturnValue(of(claimedTask));

      paramsSubject.next({ id: 'task-id-1' });
      await fixture.whenStable();

      expect(mockTaskService.publishUpdatedTask).toHaveBeenCalledWith(claimedTask);
    });

    it('should call getTask with the id from route params', async () => {
      paramsSubject.next({ id: 'task-xyz' });
      await fixture.whenStable();

      expect(mockTaskService.getTask).toHaveBeenCalledWith('task-xyz');
    });

    it('should call setRequestInProgress(true) at the start of getTask', async () => {
      paramsSubject.next({ id: 'task-id-1' });
      await fixture.whenStable();

      expect(mockRequestInProgressService.setRequestInProgress).toHaveBeenCalledWith(true);
    });
  });

  describe('getWorkbaskets()', () => {
    it('should call getAllWorkBaskets when invoked directly', () => {
      component.task = makeTask();

      component.getWorkbaskets();

      expect(mockWorkbasketService.getAllWorkBaskets).toHaveBeenCalled();
    });

    it('should set workbaskets from the service response when invoked directly', () => {
      component.task = makeTask();

      component.getWorkbaskets();

      expect(component.workbaskets).toBeDefined();
      expect(component.workbaskets.length).toBeGreaterThan(0);
    });

    it('should remove the current task workbasket from the list', () => {
      component.task = makeTask();

      component.getWorkbaskets();

      const names = component.workbaskets.map((wb) => wb.name);
      expect(names).not.toContain('Workbasket A');
    });

    it('should keep other workbaskets in the list', () => {
      component.task = makeTask();

      component.getWorkbaskets();

      const names = component.workbaskets.map((wb) => wb.name);
      expect(names).toContain('Workbasket B');
      expect(names).toContain('Workbasket C');
    });

    it('should call setRequestInProgress(false) after workbaskets are fetched', () => {
      component.task = makeTask();
      mockRequestInProgressService.setRequestInProgress.mockClear();

      component.getWorkbaskets();

      expect(mockRequestInProgressService.setRequestInProgress).toHaveBeenCalledWith(false);
    });

    it('should call setRequestInProgress(true) before fetching workbaskets', () => {
      component.task = makeTask();
      mockRequestInProgressService.setRequestInProgress.mockClear();

      component.getWorkbaskets();

      expect(mockRequestInProgressService.setRequestInProgress).toHaveBeenCalledWith(true);
    });

    it('should not remove workbaskets that do not match the task workbasket name', () => {
      const taskWithDifferentWb = makeTask();
      taskWithDifferentWb.workbasketSummary = { workbasketId: 'wb-99', name: 'Nonexistent WB' };
      component.task = taskWithDifferentWb;

      component.getWorkbaskets();

      // All three workbaskets should remain since none match 'Nonexistent WB'
      expect(component.workbaskets.length).toBe(3);
    });
  });

  describe('transferTask()', () => {
    it('should call taskService.transferTask with task id and workbasket id', () => {
      component.task = makeTask();

      const targetWorkbasket: Workbasket = { workbasketId: 'wb-target', name: 'Target WB' };
      component.transferTask(targetWorkbasket);

      expect(mockTaskService.transferTask).toHaveBeenCalledWith('task-id-1', 'wb-target');
    });

    it('should call setRequestInProgress(true) before transfer', () => {
      component.task = makeTask();
      mockRequestInProgressService.setRequestInProgress.mockClear();

      const targetWorkbasket: Workbasket = { workbasketId: 'wb-target', name: 'Target WB' };
      component.transferTask(targetWorkbasket);

      expect(mockRequestInProgressService.setRequestInProgress).toHaveBeenCalledWith(true);
    });

    it('should call navigateBack after initiating transfer', () => {
      component.task = makeTask();
      mockRouter.navigate.mockClear();

      const targetWorkbasket: Workbasket = { workbasketId: 'wb-target', name: 'Target WB' };
      component.transferTask(targetWorkbasket);

      expect(mockRouter.navigate).toHaveBeenCalled();
    });

    it('should call setRequestInProgress(false) after transfer completes', () => {
      component.task = makeTask();
      const transferredTask = makeTask();
      mockTaskService.transferTask.mockReturnValue(of(transferredTask));
      mockRequestInProgressService.setRequestInProgress.mockClear();

      const targetWorkbasket: Workbasket = { workbasketId: 'wb-target', name: 'Target WB' };
      component.transferTask(targetWorkbasket);

      expect(mockRequestInProgressService.setRequestInProgress).toHaveBeenCalledWith(false);
    });
  });

  describe('completeTask()', () => {
    it('should call taskService.completeTask with the task id', () => {
      component.task = makeTask();

      component.completeTask();

      expect(mockTaskService.completeTask).toHaveBeenCalledWith('task-id-1');
    });

    it('should call setRequestInProgress(true) before completing', () => {
      component.task = makeTask();
      mockRequestInProgressService.setRequestInProgress.mockClear();

      component.completeTask();

      expect(mockRequestInProgressService.setRequestInProgress).toHaveBeenCalledWith(true);
    });

    it('should call publishUpdatedTask after completing', () => {
      const completedTask = makeTask({ taskId: 'task-id-1' });
      mockTaskService.completeTask.mockReturnValue(of(completedTask));
      component.task = makeTask();

      component.completeTask();

      expect(mockTaskService.publishUpdatedTask).toHaveBeenCalledWith(completedTask);
    });

    it('should call navigateBack after completing', () => {
      component.task = makeTask();
      mockRouter.navigate.mockClear();

      component.completeTask();

      expect(mockRouter.navigate).toHaveBeenCalled();
    });

    it('should call setRequestInProgress(false) after completing', () => {
      component.task = makeTask();
      mockTaskService.completeTask.mockReturnValue(of(makeTask()));
      mockRequestInProgressService.setRequestInProgress.mockClear();

      component.completeTask();

      expect(mockRequestInProgressService.setRequestInProgress).toHaveBeenCalledWith(false);
    });
  });

  describe('cancelClaimTask()', () => {
    it('should call taskService.cancelClaimTask with the task id', () => {
      component.task = makeTask();

      component.cancelClaimTask();

      expect(mockTaskService.cancelClaimTask).toHaveBeenCalledWith('task-id-1');
    });

    it('should call setRequestInProgress(true) before cancelling', () => {
      component.task = makeTask();
      mockRequestInProgressService.setRequestInProgress.mockClear();

      component.cancelClaimTask();

      expect(mockRequestInProgressService.setRequestInProgress).toHaveBeenCalledWith(true);
    });

    it('should call navigateBack after initiating cancel claim', () => {
      component.task = makeTask();
      mockRouter.navigate.mockClear();

      component.cancelClaimTask();

      expect(mockRouter.navigate).toHaveBeenCalled();
    });

    it('should call publishUpdatedTask after cancel claim resolves', () => {
      const cancelledTask = makeTask();
      mockTaskService.cancelClaimTask.mockReturnValue(of(cancelledTask));
      component.task = makeTask();

      component.cancelClaimTask();

      expect(mockTaskService.publishUpdatedTask).toHaveBeenCalledWith(cancelledTask);
    });

    it('should call setRequestInProgress(false) after cancel claim resolves', () => {
      component.task = makeTask();
      mockTaskService.cancelClaimTask.mockReturnValue(of(makeTask()));
      mockRequestInProgressService.setRequestInProgress.mockClear();

      component.cancelClaimTask();

      expect(mockRequestInProgressService.setRequestInProgress).toHaveBeenCalledWith(false);
    });
  });

  describe('navigateBack()', () => {
    it('should navigate to the taskdetail outlet with the task id', () => {
      component.task = makeTask();
      mockRouter.navigate.mockClear();

      component.navigateBack();

      expect(mockRouter.navigate).toHaveBeenCalledWith(
        [{ outlets: { detail: 'taskdetail/task-id-1' } }],
        expect.objectContaining({ queryParamsHandling: 'merge' })
      );
    });
  });

  describe('extractUrl() - private method', () => {
    it('should return the URL as-is when it has no template expressions', () => {
      const url = 'https://example.com/task/path';
      const result = (component as any).extractUrl(url);

      expect(result).toBe(url);
    });

    it('should replace ${task.taskId} with the actual task id', () => {
      component.task = makeTask();
      const url = 'https://example.com/${task.taskId}';

      const result = (component as any).extractUrl(url);

      expect(result).toBe('https://example.com/task-id-1');
    });

    it('should replace ${task.name} with the task name', () => {
      component.task = makeTask();
      const url = 'https://example.com?taskName=${task.name}';

      const result = (component as any).extractUrl(url);

      expect(result).toBe('https://example.com?taskName=My Task');
    });

    it('should handle multiple template expressions in one URL', () => {
      component.task = makeTask();
      const url = 'https://example.com/${task.taskId}/name/${task.name}';

      const result = (component as any).extractUrl(url);

      expect(result).toBe('https://example.com/task-id-1/name/My Task');
    });

    it('should return the URL unchanged when it is an empty string', () => {
      const result = (component as any).extractUrl('');

      expect(result).toBe('');
    });
  });

  describe('getReflectiveProperty() - private method', () => {
    it('should return the property value from the object', () => {
      const obj = { foo: 'bar', count: 42 };

      expect((component as any).getReflectiveProperty(obj, 'foo')).toBe('bar');
      expect((component as any).getReflectiveProperty(obj, 'count')).toBe(42);
    });

    it('should return undefined for a property that does not exist', () => {
      const obj = { foo: 'bar' };

      expect((component as any).getReflectiveProperty(obj, 'nonexistent')).toBeUndefined();
    });

    it('should work with nested objects via Reflect.get', () => {
      const nested = { inner: 'value' };
      const obj = { nested };

      const result = (component as any).getReflectiveProperty(obj, 'nested');
      expect(result).toBe(nested);
    });
  });

  describe('ngOnDestroy()', () => {
    it('should unsubscribe from routeSubscription', () => {
      paramsSubject.next({ id: 'task-id-1' });

      const unsubscribeSpy = vi.spyOn(component.routeSubscription, 'unsubscribe');

      component.ngOnDestroy();

      expect(unsubscribeSpy).toHaveBeenCalled();
    });

    it('should not throw if routeSubscription is undefined', () => {
      component.routeSubscription = undefined;

      expect(() => component.ngOnDestroy()).not.toThrow();
    });
  });
});
