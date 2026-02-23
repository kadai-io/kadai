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
import { provideRouter, Routes } from '@angular/router';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { TaskDetailsComponent } from './task-details.component';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { TaskService } from '../../services/task.service';
import { WorkplaceService } from '../../services/workplace.service';
import { MasterAndDetailService } from '../../../shared/services/master-and-detail/master-and-detail.service';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';
import { of, Subject, throwError } from 'rxjs';
import { Task } from '../../models/task';
import { ObjectReference } from '../../models/object-reference';
import { provideStore, Store } from '@ngxs/store';
import { EngineConfigurationState } from '../../../shared/store/engine-configuration-store/engine-configuration.state';
import { engineConfigurationMock } from '../../../shared/store/mock-data/mock-store';
import { TaskInformationComponent } from '../task-information/task-information.component';
import { TaskStatusDetailsComponent } from '../task-status-details/task-status-details.component';
import { TaskCustomFieldsComponent } from '../task-custom-fields/task-custom-fields.component';
import { TaskAttributeValueComponent } from '../task-attribute-value/task-attribute-value.component';

@Component({ selector: 'kadai-task-information', template: '', standalone: true })
class StubTaskInformationComponent {
  @Input() task: any;
  @Input() saveToggleTriggered: any;
  @Output() formValid = new EventEmitter<boolean>();
  @Output() taskChange = new EventEmitter<any>();
}

@Component({ selector: 'kadai-task-status-details', template: '', standalone: true })
class StubTaskStatusDetailsComponent {
  @Input() task: any;
}

@Component({ selector: 'kadai-task-custom-fields', template: '', standalone: true })
class StubTaskCustomFieldsComponent {
  @Input() task: any;
}

@Component({ selector: 'kadai-task-attribute-value', template: '', standalone: true })
class StubTaskAttributeValueComponent {
  @Input() attributes: any;
  @Input() callbackInfo: any;
}

@Component({
  selector: 'kadai-dummy-detail',
  template: 'dummydetail'
})
class DummyDetailComponent {}

const routes: Routes = [{ path: 'workplace/taskdetail/:id', component: DummyDetailComponent }];

describe('TaskDetailsComponent', () => {
  let component: TaskDetailsComponent;
  let fixture: ComponentFixture<TaskDetailsComponent>;
  let taskServiceSpy: Partial<TaskService>;
  let workplaceServiceSpy: Partial<WorkplaceService>;
  let masterAndDetailServiceSpy: Partial<MasterAndDetailService>;
  let notificationServiceSpy: Partial<NotificationService>;
  let requestInProgressServiceSpy: Partial<RequestInProgressService>;

  const mockWorkbasket = {
    workbasketId: 'WBI:001',
    key: 'WB001',
    name: 'Test Workbasket',
    domain: 'DOMAIN_A',
    type: 'PERSONAL' as any,
    description: '',
    owner: '',
    custom1: '',
    custom2: '',
    custom3: '',
    custom4: '',
    orgLevel1: '',
    orgLevel2: '',
    orgLevel3: '',
    orgLevel4: '',
    markedForDeletion: false,
    created: '',
    modified: '',
    _links: {}
  };

  const mockTask: Task = new Task(
    'task-id-1',
    new ObjectReference(),
    mockWorkbasket as any,
    undefined,
    undefined,
    undefined,
    'owner1',
    '2026-01-01T00:00:00Z',
    undefined,
    undefined,
    '2026-01-01T00:00:00Z',
    undefined,
    undefined,
    undefined,
    'Test Task',
    undefined,
    undefined,
    undefined,
    'READY',
    false,
    false,
    1
  );

  beforeEach(async () => {
    taskServiceSpy = {
      getTask: vi.fn().mockReturnValue(of(mockTask)),
      selectTask: vi.fn(),
      updateTask: vi.fn().mockReturnValue(of(mockTask)),
      createTask: vi.fn().mockReturnValue(of({ ...mockTask, taskId: 'new-task-id' })),
      deleteTask: vi.fn().mockReturnValue(of({})),
      publishTaskDeletion: vi.fn(),
      publishUpdatedTask: vi.fn(),
      getSelectedTask: vi.fn().mockReturnValue(new Subject<Task>().asObservable())
    };

    workplaceServiceSpy = {
      getSelectedWorkbasket: vi.fn().mockReturnValue(of(mockWorkbasket))
    };

    masterAndDetailServiceSpy = {
      getShowDetail: vi.fn().mockReturnValue(of(false))
    };

    notificationServiceSpy = {
      showSuccess: vi.fn(),
      showError: vi.fn(),
      showDialog: vi.fn()
    };

    requestInProgressServiceSpy = {
      setRequestInProgress: vi.fn(),
      getRequestInProgress: vi.fn().mockReturnValue(of(false))
    };

    await TestBed.configureTestingModule({
      imports: [TaskDetailsComponent],
      providers: [
        provideRouter(routes),
        provideHttpClient(),
        provideStore([EngineConfigurationState]),
        { provide: TaskService, useValue: taskServiceSpy },
        { provide: WorkplaceService, useValue: workplaceServiceSpy },
        { provide: MasterAndDetailService, useValue: masterAndDetailServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: RequestInProgressService, useValue: requestInProgressServiceSpy }
      ]
    })
      .overrideComponent(TaskDetailsComponent, {
        remove: {
          imports: [
            TaskInformationComponent,
            TaskStatusDetailsComponent,
            TaskCustomFieldsComponent,
            TaskAttributeValueComponent
          ]
        },
        add: {
          imports: [
            StubTaskInformationComponent,
            StubTaskStatusDetailsComponent,
            StubTaskCustomFieldsComponent,
            StubTaskAttributeValueComponent
          ]
        }
      })
      .compileComponents();

    const store = TestBed.inject(Store);
    store.reset({ ...store.snapshot(), engineConfiguration: engineConfigurationMock });
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TaskDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should set currentWorkbasket from workplaceService on init', () => {
    expect(workplaceServiceSpy.getSelectedWorkbasket).toHaveBeenCalled();
    expect(component.currentWorkbasket).toEqual(mockWorkbasket);
  });

  it('should set showDetail from masterAndDetailService on init', () => {
    expect(masterAndDetailServiceSpy.getShowDetail).toHaveBeenCalled();
    expect(component.showDetail).toBe(false);
  });

  it('should subscribe to requestInProgress on init', () => {
    expect(requestInProgressServiceSpy.getRequestInProgress).toHaveBeenCalled();
    expect(component.requestInProgress).toBe(false);
  });

  it('should call getTask when route params change', () => {
    expect(requestInProgressServiceSpy.setRequestInProgress).toHaveBeenCalled();
  });

  it('should reset task when resetTask is called', () => {
    const originalTask: Task = new Task('original-id', new ObjectReference());
    originalTask.customAttributes = [{ key: 'key1', value: 'val1' }];
    originalTask.callbackInfo = [{ key: 'cb1', value: 'cbval1' }];
    originalTask.primaryObjRef = new ObjectReference();

    component.task = originalTask;
    component.taskClone = {
      ...originalTask,
      customAttributes: [{ key: 'key1', value: 'val1' }],
      callbackInfo: [{ key: 'cb1', value: 'cbval1' }],
      primaryObjRef: new ObjectReference()
    };

    component.resetTask();

    expect(notificationServiceSpy.showSuccess).toHaveBeenCalledWith('TASK_RESTORE');
  });

  it('should call workOnTaskDisabled returns false when task is null', () => {
    component.task = null;
    expect(component.workOnTaskDisabled()).toBe(false);
  });

  it('should return true for workOnTaskDisabled when task state is COMPLETED', () => {
    component.task = { ...mockTask, state: 'COMPLETED' } as Task;
    expect(component.workOnTaskDisabled()).toBe(true);
  });

  it('should return false for workOnTaskDisabled when task state is READY', () => {
    component.task = { ...mockTask, state: 'READY' } as Task;
    expect(component.workOnTaskDisabled()).toBe(false);
  });

  it('should set tabSelected when selectTab is called', () => {
    expect(component.tabSelected).toBe('general');
    component.selectTab('details');
    expect(component.tabSelected).toBe('details');
  });

  it('should call onSave with createTask when currentId is new-task', () => {
    component.currentId = 'new-task';
    component.task = new Task('', new ObjectReference(), mockWorkbasket as any);
    component.onSave();
    expect(taskServiceSpy.createTask).toHaveBeenCalled();
  });

  it('should call onSave with updateTask when currentId is not new-task', () => {
    component.currentId = 'task-id-1';
    component.task = mockTask;
    component.onSave();
    expect(taskServiceSpy.updateTask).toHaveBeenCalled();
  });

  it('should show notification when updateTask succeeds', () => {
    component.currentId = 'task-id-1';
    component.task = mockTask;
    component.onSave();
    expect(notificationServiceSpy.showSuccess).toHaveBeenCalledWith('TASK_UPDATE', { taskName: mockTask.name });
  });

  it('should call notificationService.showDialog when deleteTask is called', () => {
    component.currentId = 'task-id-1';
    component.deleteTask();
    expect(notificationServiceSpy.showDialog).toHaveBeenCalledWith(
      'TASK_DELETE',
      { taskId: 'task-id-1' },
      expect.any(Function)
    );
  });

  it('should call backClicked and call taskService.selectTask with undefined', () => {
    component.task = mockTask;
    component.backClicked();
    expect(taskServiceSpy.selectTask).toHaveBeenCalled();
  });

  it('should complete destroy$ on ngOnDestroy', () => {
    const destroySpy = vi.spyOn(component.destroy$, 'next');
    const completeSpy = vi.spyOn(component.destroy$, 'complete');
    component.ngOnDestroy();
    expect(destroySpy).toHaveBeenCalled();
    expect(completeSpy).toHaveBeenCalled();
  });

  it('should unsubscribe subscriptions on ngOnDestroy', () => {
    component.ngOnDestroy();
    expect(component).toBeTruthy();
  });

  it('should call deleteTaskConfirmation: delete task then navigate', () => {
    component.task = mockTask;
    component.currentId = 'task-id-1';
    component.deleteTaskConfirmation();
    expect(taskServiceSpy.deleteTask).toHaveBeenCalledWith(mockTask);
    expect(taskServiceSpy.publishTaskDeletion).toHaveBeenCalled();
    expect(notificationServiceSpy.showSuccess).toHaveBeenCalledWith('TASK_DELETE', { taskName: mockTask.name });
  });

  it('should set task to null after deleteTaskConfirmation', () => {
    component.task = mockTask;
    component.deleteTaskConfirmation();
    expect(component.task).toBeNull();
  });

  it('should handle getTask for new-task correctly', () => {
    component.currentId = 'new-task';
    component.currentWorkbasket = mockWorkbasket as any;
    component.getTask();
    expect(requestInProgressServiceSpy.setRequestInProgress).toHaveBeenCalledWith(true);
    expect(requestInProgressServiceSpy.setRequestInProgress).toHaveBeenCalledWith(false);
    expect(component.task).toBeDefined();
  });

  it('should call taskService.getTask for existing task ID', () => {
    component.currentId = 'task-id-1';
    component.getTask();
    expect(taskServiceSpy.getTask).toHaveBeenCalledWith('task-id-1');
  });

  it('should show success notification on updateTask success', () => {
    component.currentId = 'task-id-1';
    component.task = { ...mockTask } as Task;
    component.onSave();
    expect(notificationServiceSpy.showSuccess).toHaveBeenCalledWith('TASK_UPDATE', { taskName: mockTask.name });
  });

  it('should call setRequestInProgress(false) when getTask returns an error', () => {
    (taskServiceSpy.getTask as any).mockReturnValue(throwError(() => new Error('not found')));
    component.currentId = 'some-id';
    component.getTask();
    expect(requestInProgressServiceSpy.setRequestInProgress).toHaveBeenCalledWith(false);
  });

  it('should call setRequestInProgress(false) when updateTask returns an error', () => {
    (taskServiceSpy.updateTask as any).mockReturnValue(throwError(() => new Error('update failed')));
    component.currentId = 'task-id-1';
    component.task = { ...mockTask } as Task;
    component.onSave();
    expect(requestInProgressServiceSpy.setRequestInProgress).toHaveBeenCalledWith(false);
  });

  it('should call setRequestInProgress(false) when createTask returns an error', () => {
    (taskServiceSpy.createTask as any).mockReturnValue(throwError(() => new Error('create failed')));
    component.currentId = 'new-task';
    component.task = new Task('', new ObjectReference(), mockWorkbasket as any);
    component.onSave();
    expect(requestInProgressServiceSpy.setRequestInProgress).toHaveBeenCalledWith(false);
  });

  it('should navigate when openTask is called', () => {
    component.currentId = 'task-id-1';
    expect(() => component.openTask()).not.toThrow();
  });
});
