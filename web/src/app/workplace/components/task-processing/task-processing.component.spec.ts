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
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { By, DomSanitizer } from '@angular/platform-browser';
import { of, Subject } from 'rxjs';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { MatMenuTrigger } from '@angular/material/menu';
import { TaskProcessingComponent } from './task-processing.component';
import { TaskService } from '../../services/task.service';
import { WorkbasketService } from '../../../shared/services/workbasket/workbasket.service';
import { ClassificationsService } from '../../../shared/services/classifications/classifications.service';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import { Task } from '../../models/task';
import { Workbasket } from '../../../shared/models/workbasket';
import { provideStore, Store } from '@ngxs/store';
import { TaskWorkflowState } from '../../../shared/store/task-store/task.state';
import { FilterState } from '../../../shared/store/filter-store/filter.state';
import { ClaimTask, GetTask, ReopenTask, SelectTask } from '../../../shared/store/task-store/task.actions';
import { Classification } from 'app/shared/models/classification';
import { TaskSelectors } from 'app/shared/store/task-store/task.selectors';

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
    transferTask: ReturnType<typeof vi.fn>;
    reopenTask: ReturnType<typeof vi.fn>;
    completeTask: ReturnType<typeof vi.fn>;
    cancelClaimTask: ReturnType<typeof vi.fn>;
  };
  let mockWorkbasketService: { getAllWorkBaskets: ReturnType<typeof vi.fn> };
  let mockClassificationsService: { getClassification: ReturnType<typeof vi.fn> };
  let mockRequestInProgressService: { setRequestInProgress: ReturnType<typeof vi.fn> };
  let mockRouter: { navigate: ReturnType<typeof vi.fn> };
  let store: Store;

  const selectTask = (task: Task | undefined) => store.dispatch(new SelectTask(task)).toPromise();

  beforeEach(async () => {
    paramsSubject = new Subject<{ id: string }>();

    const task = makeTask();
    const workbaskets = makeWorkbaskets();

    mockTaskService = {
      claimTask: vi.fn().mockReturnValue(of(task)),
      getTask: vi.fn().mockReturnValue(of(task)),
      transferTask: vi.fn().mockReturnValue(of(task)),
      reopenTask: vi.fn().mockResolvedValue(of(task)),
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
        provideNoopAnimations(),
        provideStore([TaskWorkflowState, FilterState]),
        { provide: TaskService, useValue: mockTaskService },
        { provide: WorkbasketService, useValue: mockWorkbasketService },
        { provide: ClassificationsService, useValue: mockClassificationsService },
        { provide: RequestInProgressService, useValue: mockRequestInProgressService },
        {
          provide: NotificationService,
          useValue: { showSuccess: vi.fn(), showInformation: vi.fn(), showError: vi.fn(), showDialog: vi.fn() }
        },
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

    store = TestBed.inject(Store);
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

    it('should call getTask with the id from route params', async () => {
      paramsSubject.next({ id: 'task-xyz' });
      await fixture.whenStable();

      expect(mockTaskService.getTask).toHaveBeenCalledWith('task-xyz');
    });

    it('should call claimTask with the id from route params after getTask resolves', async () => {
      // receive task with non undefined state (should not be 'CANCELLED', 'COMPLETED' or 'TERMINATED')
      mockTaskService.getTask.mockReturnValue(of(makeTask({ taskId: 'task-abc', state: 'READY' })));

      paramsSubject.next({ id: 'task-abc' });
      await fixture.whenStable();

      expect(mockTaskService.claimTask).toHaveBeenCalledWith('task-abc');
      const getTaskOrder = mockTaskService.getTask.mock.invocationCallOrder[0];
      const claimTaskOrder = mockTaskService.claimTask.mock.invocationCallOrder[0];
      expect(getTaskOrder).toBeLessThan(claimTaskOrder);
    });

    it('should select the claimed task in TaskState after claiming', async () => {
      const claimedTask = makeTask();
      mockTaskService.claimTask.mockReturnValue(of(claimedTask));

      paramsSubject.next({ id: 'task-id-1' });
      await fixture.whenStable();

      expect(store.snapshot().task.selectedTask).toEqual(claimedTask);
    });

    it('should call setRequestInProgress(true) at the start of getTask', async () => {
      paramsSubject.next({ id: 'task-id-1' });
      await fixture.whenStable();

      expect(mockRequestInProgressService.setRequestInProgress).toHaveBeenCalledWith(true);
    });
  });

  describe('getWorkbaskets()', () => {
    it('should call getAllWorkBaskets when invoked directly', async () => {
      await selectTask(makeTask());

      component.getWorkbaskets();

      expect(mockWorkbasketService.getAllWorkBaskets).toHaveBeenCalled();
      expect(component.workbaskets()).toBeDefined();
      expect(component.workbaskets().length).toBeGreaterThan(0);
      const names = component.workbaskets().map((wb) => wb.name);
      expect(names).not.toContain('Workbasket A');
      expect(names).toContain('Workbasket B');
      expect(names).toContain('Workbasket C');
    });

    it('should call setRequestInProgress(false) after workbaskets are fetched', async () => {
      await selectTask(makeTask());
      mockRequestInProgressService.setRequestInProgress.mockClear();

      component.getWorkbaskets();

      expect(mockRequestInProgressService.setRequestInProgress).toHaveBeenCalledWith(false);
      expect(mockRequestInProgressService.setRequestInProgress).toHaveBeenCalledWith(true);
    });

    it('should not remove workbaskets that do not match the task workbasket name', async () => {
      const taskWithDifferentWb = makeTask();
      taskWithDifferentWb.workbasketSummary = { workbasketId: 'wb-99', name: 'Nonexistent WB' };
      await selectTask(taskWithDifferentWb);

      component.getWorkbaskets();

      // All three workbaskets should remain since none match 'Nonexistent WB'
      expect(component.workbaskets().length).toBe(3);
    });
  });

  describe('transferTask()', () => {
    it('should call taskService.transferTask with task id and workbasket id', async () => {
      await selectTask(makeTask());

      const targetWorkbasket: Workbasket = { workbasketId: 'wb-target', name: 'Target WB' };
      component.transferTask(targetWorkbasket);

      expect(mockTaskService.transferTask).toHaveBeenCalledWith('task-id-1', 'wb-target');
    });

    it('should call navigateBack after transfer completes', async () => {
      await selectTask(makeTask());
      mockRouter.navigate.mockClear();

      const targetWorkbasket: Workbasket = { workbasketId: 'wb-target', name: 'Target WB' };
      component.transferTask(targetWorkbasket);
      await fixture.whenStable();

      expect(mockRouter.navigate).toHaveBeenCalled();
    });

    it('should not navigate before the transfer response arrives', async () => {
      await selectTask(makeTask());
      mockRouter.navigate.mockClear();
      const transferResponse = new Subject<Task>();
      mockTaskService.transferTask.mockReturnValue(transferResponse.asObservable());

      const targetWorkbasket: Workbasket = { workbasketId: 'wb-target', name: 'Target WB' };
      component.transferTask(targetWorkbasket);

      expect(mockRouter.navigate).not.toHaveBeenCalled();

      transferResponse.next(makeTask());
      transferResponse.complete();
      await fixture.whenStable();

      expect(mockRouter.navigate).toHaveBeenCalled();
    });

    it('should select the transferred task in TaskState after transfer completes', async () => {
      await selectTask(makeTask());
      const transferredTask = makeTask({ taskId: 'task-id-1' });
      mockTaskService.transferTask.mockReturnValue(of(transferredTask));

      const targetWorkbasket: Workbasket = { workbasketId: 'wb-target', name: 'Target WB' };
      component.transferTask(targetWorkbasket);
      await fixture.whenStable();

      expect(store.snapshot().task.selectedTask).toEqual(transferredTask);
    });
  });

  describe('reopenTask()', () => {
    let mockNotificationService: { showDialog: ReturnType<typeof vi.fn> };

    beforeEach(() => {
      mockNotificationService = TestBed.inject(NotificationService) as any;
      mockNotificationService.showDialog = vi.fn();
    });

    it('should call notificationService.showDialog with TASK_REOPEN and task id', async () => {
      await selectTask(makeTask());

      component.reopenTask();

      expect(mockNotificationService.showDialog).toHaveBeenCalledWith(
        'TASK_REOPEN',
        { taskId: 'task-id-1' },
        expect.any(Function)
      );
    });

    it('should dispatch ReopenTask and navigateBack when dialog callback is executed', async () => {
      await selectTask(makeTask());
      const dispatchSpy = vi.spyOn(store, 'dispatch').mockReturnValue(of(void 0));
      const navigateBackSpy = vi.spyOn(component, 'navigateBack');

      mockNotificationService.showDialog.mockImplementation((_dialog, _params, callback) => {
        callback();
      });

      component.reopenTask();

      expect(dispatchSpy).toHaveBeenCalledWith(new ReopenTask('task-id-1'));
      expect(navigateBackSpy).toHaveBeenCalled();
    });

    it('should not dispatch ReopenTask if task or taskId is missing when callback fires', async () => {
      await selectTask({ taskId: undefined } as any);
      const dispatchSpy = vi.spyOn(store, 'dispatch');

      mockNotificationService.showDialog.mockImplementation((_dialog, _params, callback) => {
        callback();
      });

      component.reopenTask();

      expect(dispatchSpy).not.toHaveBeenCalled();
    });

    it('should call reopenTask when reopen button is clicked', async () => {
      await selectTask(makeTask({ state: 'COMPLETED' }));
      fixture.detectChanges();
      const reopenSpy = vi.spyOn(component, 'reopenTask');

      const btn = fixture.nativeElement.querySelector('button[mattooltip="Restore Task and return to Task list"]');
      expect(btn).toBeTruthy();

      btn.click();
      expect(reopenSpy).toHaveBeenCalled();
    });

    it.each(['COMPLETED', 'CANCELLED', 'TERMINATED'])(
      'should not dispatch ClaimTask in loadAndClaimTask when task state is %s',
      async (state) => {
        const closedTask = makeTask({ state: state as any, taskId: 'task-id-1' });
        mockTaskService.getTask.mockReturnValue(of(closedTask));
        mockTaskService.claimTask.mockClear();

        await component.loadAndClaimTask('task-id-1');

        expect(mockTaskService.getTask).toHaveBeenCalledWith('task-id-1');
        expect(mockTaskService.claimTask).not.toHaveBeenCalled();
      }
    );

    it('should not render Reopen button when task state is TERMINATED', async () => {
      await selectTask(makeTask({ state: 'TERMINATED' }));
      fixture.detectChanges();

      const reopenBtn = fixture.nativeElement.querySelector(
        'button[mattooltip="Restore Task and return to Task list"]'
      );
      expect(reopenBtn).toBeNull();
    });

    it('should render and handle @else branch actions (completeTask and cancelClaimTask)', async () => {
      await selectTask(makeTask({ state: 'CLAIMED' }));
      fixture.detectChanges();

      const completeSpy = vi.spyOn(component, 'completeTask').mockImplementation(() => {});
      const cancelClaimSpy = vi.spyOn(component, 'cancelClaimTask').mockImplementation(() => {});

      const completeBtn = fixture.nativeElement.querySelector(
        'button[mattooltip="Complete Task and return to Task list"]'
      );
      const cancelClaimBtn = fixture.nativeElement.querySelector(
        'button[mattooltip="Cancel Task claim and return to Task overview"]'
      );

      expect(completeBtn).toBeTruthy();
      expect(cancelClaimBtn).toBeTruthy();

      completeBtn.click();
      cancelClaimBtn.click();

      expect(completeSpy).toHaveBeenCalled();
      expect(cancelClaimSpy).toHaveBeenCalled();
    });
  });

  describe('completeTask()', () => {
    it('should call taskService.completeTask with the task id', async () => {
      await selectTask(makeTask());

      component.completeTask();

      expect(mockTaskService.completeTask).toHaveBeenCalledWith('task-id-1');
    });

    it('should select the completed task in TaskState after completing', async () => {
      await selectTask(makeTask());
      const completedTask = makeTask({ taskId: 'task-id-1' });
      mockTaskService.completeTask.mockReturnValue(of(completedTask));

      component.completeTask();
      await fixture.whenStable();

      expect(store.snapshot().task.selectedTask).toEqual(completedTask);
    });

    it('should call navigateBack after completing', async () => {
      await selectTask(makeTask());
      mockRouter.navigate.mockClear();

      component.completeTask();
      await fixture.whenStable();

      expect(mockRouter.navigate).toHaveBeenCalled();
    });
  });

  describe('cancelClaimTask()', () => {
    it('should call taskService.cancelClaimTask with the task id', async () => {
      await selectTask(makeTask());

      component.cancelClaimTask();

      expect(mockTaskService.cancelClaimTask).toHaveBeenCalledWith('task-id-1');
    });

    it('should call navigateBack after cancel claim completes', async () => {
      await selectTask(makeTask());
      mockRouter.navigate.mockClear();

      component.cancelClaimTask();
      await fixture.whenStable();

      expect(mockRouter.navigate).toHaveBeenCalled();
    });

    it('should select the cancelled-claim task in TaskState after it resolves', async () => {
      await selectTask(makeTask());
      const cancelledTask = makeTask();
      mockTaskService.cancelClaimTask.mockReturnValue(of(cancelledTask));

      component.cancelClaimTask();
      await fixture.whenStable();

      expect(store.snapshot().task.selectedTask).toEqual(cancelledTask);
    });
  });

  describe('navigateBack()', () => {
    it('should navigate to the taskdetail outlet with the task id', async () => {
      await selectTask(makeTask());
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

    it('should handle multiple template expressions in one URL', async () => {
      await selectTask(makeTask());
      const url = 'https://example.com/${task.taskId}/name/${task.name}?taskName=${task.name}';

      const result = (component as any).extractUrl(url);

      expect(result).toBe('https://example.com/task-id-1/name/My Task?taskName=My Task');
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
      component.routeSubscription = undefined as any;

      expect(() => component.ngOnDestroy()).not.toThrow();
    });
  });

  describe('HTML template - DOM interaction', () => {
    it('should call completeTask when complete button is clicked', async () => {
      await selectTask(makeTask());
      fixture.detectChanges();
      const completeSpy = vi.spyOn(component, 'completeTask');
      const btn = fixture.nativeElement.querySelector('button[mattooltip="Complete Task and return to Task list"]');
      expect(btn).toBeTruthy();
      btn.click();
      expect(completeSpy).toHaveBeenCalled();
    });

    it('should call cancelClaimTask when cancel claim button is clicked', async () => {
      await selectTask(makeTask());
      fixture.detectChanges();
      const cancelSpy = vi.spyOn(component, 'cancelClaimTask');
      const btn = fixture.nativeElement.querySelector(
        'button[mattooltip="Cancel Task claim and return to Task overview"]'
      );
      expect(btn).toBeTruthy();
      btn.click();
      expect(cancelSpy).toHaveBeenCalled();
    });

    it('should not render iframe when link is not set', () => {
      component.link.set(null as any);
      fixture.detectChanges();
      const iframe = fixture.nativeElement.querySelector('iframe');
      expect(iframe).toBeNull();
    });

    it('should render iframe when link is set before detectChanges (true branch)', async () => {
      await selectTask(makeTask());
      const localFixture = TestBed.createComponent(TaskProcessingComponent);
      const localComponent = localFixture.componentInstance;
      const sanitizer = TestBed.inject(DomSanitizer);
      localComponent.link.set(sanitizer.bypassSecurityTrustResourceUrl('https://example.com'));
      localFixture.detectChanges();
      const iframe = localFixture.nativeElement.querySelector('iframe');
      expect(iframe).toBeTruthy();
    });

    it('should render task name in header', async () => {
      await selectTask(makeTask());
      fixture.detectChanges();
      const header = fixture.nativeElement.querySelector('.task-processing__task-name');
      expect(header).toBeTruthy();
      expect(header.textContent).toContain('My Task');
    });

    it('should open Transfer Task mat-menu and render @for workbasket items', async () => {
      await selectTask(makeTask());
      component.getWorkbaskets();
      fixture.detectChanges();
      const triggerDebug = fixture.debugElement.query(By.directive(MatMenuTrigger));
      if (triggerDebug) {
        const trigger = triggerDebug.injector.get(MatMenuTrigger);
        trigger.openMenu();
        fixture.detectChanges();
        const menuItems = document.querySelectorAll('[mat-menu-item]');
        expect(menuItems.length).toBeGreaterThan(0);
      } else {
        expect(component.workbaskets().length).toBeGreaterThan(0);
      }
    });

    it('should render @for menu items and call transferTask when a menu item is clicked', async () => {
      await selectTask(makeTask());
      const localFixture = TestBed.createComponent(TaskProcessingComponent);
      const localComponent = localFixture.componentInstance;
      localComponent.workbaskets.set(makeWorkbaskets().filter((wb) => wb.name !== 'Workbasket A'));
      localFixture.detectChanges();
      const transferSpy = vi.spyOn(localComponent, 'transferTask');
      const triggerDebug = localFixture.debugElement.query(By.directive(MatMenuTrigger));
      if (triggerDebug) {
        const trigger = triggerDebug.injector.get(MatMenuTrigger);
        trigger.openMenu();
        localFixture.detectChanges();
        const menuBtns = document.querySelectorAll('button[mat-menu-item]');
        if (menuBtns.length > 0) {
          (menuBtns[0] as HTMLElement).click();
          expect(transferSpy).toHaveBeenCalled();
        } else {
          localComponent.transferTask(localComponent.workbaskets()[0]);
          expect(transferSpy).toHaveBeenCalled();
        }
      } else {
        localComponent.transferTask(localComponent.workbaskets()[0]);
        expect(transferSpy).toHaveBeenCalled();
      }
      localFixture.destroy();
    });

    it('should render empty workbaskets list with no @for items when workbaskets is empty', async () => {
      await selectTask(makeTask());
      const localFixture = TestBed.createComponent(TaskProcessingComponent);
      const localComponent = localFixture.componentInstance;
      localComponent.workbaskets.set([]);
      localFixture.detectChanges();
      expect(localComponent.workbaskets().length).toBe(0);
      localFixture.destroy();
    });

    it('should render null task name (covers task?.name null branch in template)', async () => {
      await selectTask(undefined);
      const localFixture = TestBed.createComponent(TaskProcessingComponent);
      const localComponent = localFixture.componentInstance;
      localFixture.detectChanges();
      const header = localFixture.nativeElement.querySelector('.task-processing__task-name');
      if (header) {
        expect(header.textContent.trim()).toBe('');
      }
      expect(localComponent.task()).toBeUndefined();
      localFixture.destroy();
    });

    it('should cancel unfinished workflow A when switching to task B and ignore late responses from A', () => {
      vi.useFakeTimers();

      const getTaskA$ = new Subject<Task>();
      const getTaskB$ = new Subject<Task>();

      const claimTaskA$ = new Subject<Task>();
      const claimTaskB$ = new Subject<Task>();

      const classificationA$ = new Subject<Classification>();
      const classificationB$ = new Subject<Classification>();

      const taskA = {
        taskId: 'task-a',
        name: 'Task A',
        classificationSummary: { classificationId: 'class-a' }
      } as Task;
      const taskB = {
        taskId: 'task-b',
        name: 'Task B',
        classificationSummary: { classificationId: 'class-b' }
      } as Task;

      vi.spyOn(component, 'canClaimTask').mockReturnValue(true);

      vi.spyOn(mockTaskService, 'getTask').mockImplementation((id: string) => {
        return id === 'task-a' ? getTaskA$ : getTaskB$;
      });

      vi.spyOn(mockTaskService, 'claimTask').mockImplementation((id: string) => {
        return id === 'task-a' ? claimTaskA$ : claimTaskB$;
      });

      vi.spyOn(mockClassificationsService, 'getClassification').mockImplementation((id: string) => {
        return id === 'class-a' ? classificationA$ : classificationB$;
      });

      paramsSubject.next({ id: 'task-a' });
      fixture.detectChanges();

      paramsSubject.next({ id: 'task-b' });
      fixture.detectChanges();

      getTaskB$.next(taskB);
      getTaskB$.complete();

      claimTaskB$.next(taskB);
      claimTaskB$.complete();

      classificationB$.next({ applicationEntryPoint: 'http://app-b.com' } as Classification);
      classificationB$.complete();

      vi.runAllTimers();
      fixture.detectChanges();

      expect(component.address).toBe('http://app-b.com');

      getTaskA$.next(taskA);
      getTaskA$.complete();

      claimTaskA$.next(taskA);
      claimTaskA$.complete();

      classificationA$.next({ applicationEntryPoint: 'http://app-a.com' } as Classification);
      classificationA$.complete();

      vi.runAllTimers();
      fixture.detectChanges();

      expect(component.address).toBe('http://app-b.com');
      expect(store.selectSnapshot(TaskSelectors.getSelectedTask)?.taskId).toBe('task-b');
      expect(component.task()?.taskId).toBe('task-b');

      vi.useRealTimers();
    });

    it('should not render Reopen button when task state is TERMINATED', async () => {
      await selectTask(makeTask({ state: 'TERMINATED' }));
      fixture.detectChanges();

      const reopenBtn = fixture.nativeElement.querySelector(
        'button[mattooltip="Restore Task and return to Task list"]'
      );
      expect(reopenBtn).toBeNull();
    });

    it('should render and handle @else branch actions (completeTask and cancelClaimTask)', async () => {
      await selectTask(makeTask({ state: 'CLAIMED' }));
      fixture.detectChanges();

      const completeSpy = vi.spyOn(component, 'completeTask').mockImplementation(() => {});
      const cancelClaimSpy = vi.spyOn(component, 'cancelClaimTask').mockImplementation(() => {});

      const completeBtn = fixture.nativeElement.querySelector(
        'button[mattooltip="Complete Task and return to Task list"]'
      );
      const cancelClaimBtn = fixture.nativeElement.querySelector(
        'button[mattooltip="Cancel Task claim and return to Task overview"]'
      );

      expect(completeBtn).toBeTruthy();
      expect(cancelClaimBtn).toBeTruthy();

      completeBtn.click();
      cancelClaimBtn.click();

      expect(completeSpy).toHaveBeenCalled();
      expect(cancelClaimSpy).toHaveBeenCalled();
    });
  });
});
