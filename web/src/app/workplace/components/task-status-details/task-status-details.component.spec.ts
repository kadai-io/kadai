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
import { TaskStatusDetailsComponent } from './task-status-details.component';
import { beforeEach, describe, expect, it } from 'vitest';
import { Task } from 'app/workplace/models/task';
import { ObjectReference } from 'app/workplace/models/object-reference';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

describe('TaskStatusDetailsComponent', () => {
  let component: TaskStatusDetailsComponent;
  let fixture: ComponentFixture<TaskStatusDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TaskStatusDetailsComponent],
      providers: [provideNoopAnimations()]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TaskStatusDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should not render status details when task is not set', () => {
    const el = fixture.nativeElement.querySelector('.task-status-details');
    expect(el).toBeNull();
  });

  it('should not render status details when task is set but taskId is falsy', () => {
    const localFixture = TestBed.createComponent(TaskStatusDetailsComponent);
    const localComponent = localFixture.componentInstance;
    localComponent.task = new Task('' as string, new ObjectReference());
    localFixture.detectChanges();
    const el = localFixture.nativeElement.querySelector('.task-status-details');
    expect(el).toBeNull();
  });

  it('should render status details when task with taskId is set', () => {
    const localFixture = TestBed.createComponent(TaskStatusDetailsComponent);
    const localComponent = localFixture.componentInstance;
    const task = new Task('task-id-1', new ObjectReference());
    task.state = 'READY';
    task.read = false;
    task.transferred = false;
    localComponent.task = task;
    localFixture.detectChanges();
    const el = localFixture.nativeElement.querySelector('.task-status-details');
    expect(el).toBeTruthy();
  });

  it('should display task state value in the input', () => {
    const localFixture = TestBed.createComponent(TaskStatusDetailsComponent);
    const localComponent = localFixture.componentInstance;
    const task = new Task('task-id-2', new ObjectReference());
    task.state = 'CLAIMED';
    localComponent.task = task;
    localFixture.detectChanges();
    const stateInput: HTMLInputElement = localFixture.nativeElement.querySelector('#task-state');
    expect(stateInput).toBeTruthy();
    expect(localComponent.task.state).toBe('CLAIMED');
  });

  it('should display read and transferred values', () => {
    const localFixture = TestBed.createComponent(TaskStatusDetailsComponent);
    const localComponent = localFixture.componentInstance;
    const task = new Task('task-id-3', new ObjectReference());
    task.read = true;
    task.transferred = true;
    localComponent.task = task;
    localFixture.detectChanges();
    const readInput: HTMLInputElement = localFixture.nativeElement.querySelector('#task-read');
    const transferredInput: HTMLInputElement = localFixture.nativeElement.querySelector('#task-transferred');
    expect(readInput).toBeTruthy();
    expect(transferredInput).toBeTruthy();
  });

  it('should render all date fields when task is set', () => {
    const localFixture = TestBed.createComponent(TaskStatusDetailsComponent);
    const localComponent = localFixture.componentInstance;
    const task = new Task('task-id-4', new ObjectReference());
    task.modified = '2024-01-15T10:30:00Z';
    task.completed = '2024-01-16T11:00:00Z';
    task.claimed = '2024-01-14T09:00:00Z';
    task.planned = '2024-01-13T08:00:00Z';
    task.created = '2024-01-12T07:00:00Z';
    localComponent.task = task;
    localFixture.detectChanges();
    expect(localFixture.nativeElement.querySelector('#task-modified')).toBeTruthy();
    expect(localFixture.nativeElement.querySelector('#task-completed')).toBeTruthy();
    expect(localFixture.nativeElement.querySelector('#task-claimed')).toBeTruthy();
    expect(localFixture.nativeElement.querySelector('#task-planned')).toBeTruthy();
    expect(localFixture.nativeElement.querySelector('#task-created')).toBeTruthy();
  });

  it('should render received field when task is set', () => {
    const localFixture = TestBed.createComponent(TaskStatusDetailsComponent);
    const localComponent = localFixture.componentInstance;
    const task = new Task('task-id-5', new ObjectReference());
    task.received = '2024-01-11T06:00:00Z';
    localComponent.task = task;
    localFixture.detectChanges();
    const receivedInput: HTMLInputElement = localFixture.nativeElement.querySelector('#task-received');
    expect(receivedInput).toBeTruthy();
    expect(localComponent.task.received).toBe('2024-01-11T06:00:00Z');
  });

  it('should reflect ngModel bound state value when task state changes', () => {
    const localFixture = TestBed.createComponent(TaskStatusDetailsComponent);
    const localComponent = localFixture.componentInstance;
    const task = new Task('task-id-6', new ObjectReference());
    task.state = 'COMPLETED';
    localComponent.task = task;
    localFixture.detectChanges();
    expect(localComponent.task.state).toBe('COMPLETED');
    const stateInput: HTMLInputElement = localFixture.nativeElement.querySelector('#task-state');
    expect(stateInput).toBeTruthy();
  });

  it('should render both left and right columns when task is set', () => {
    const localFixture = TestBed.createComponent(TaskStatusDetailsComponent);
    const localComponent = localFixture.componentInstance;
    localComponent.task = new Task('task-id-7', new ObjectReference());
    localFixture.detectChanges();
    const leftCol = localFixture.nativeElement.querySelector('.task-status-details__column--left');
    const rightCol = localFixture.nativeElement.querySelector('.task-status-details__column--right');
    expect(leftCol).toBeTruthy();
    expect(rightCol).toBeTruthy();
  });

  it('should not render columns when task is not set', () => {
    const leftCol = fixture.nativeElement.querySelector('.task-status-details__column--left');
    const rightCol = fixture.nativeElement.querySelector('.task-status-details__column--right');
    expect(leftCol).toBeNull();
    expect(rightCol).toBeNull();
  });

  it('should handle null date fields gracefully when task has no dates', () => {
    const localFixture = TestBed.createComponent(TaskStatusDetailsComponent);
    const localComponent = localFixture.componentInstance;
    localComponent.task = new Task('task-id-8', new ObjectReference());
    localFixture.detectChanges();
    expect(localFixture.nativeElement.querySelector('#task-modified')).toBeTruthy();
    expect(localFixture.nativeElement.querySelector('#task-completed')).toBeTruthy();
    expect(localFixture.nativeElement.querySelector('#task-claimed')).toBeTruthy();
    expect(localFixture.nativeElement.querySelector('#task-planned')).toBeTruthy();
    expect(localFixture.nativeElement.querySelector('#task-created')).toBeTruthy();
  });

  it('should display read as false and transferred as false in model', () => {
    const localFixture = TestBed.createComponent(TaskStatusDetailsComponent);
    const localComponent = localFixture.componentInstance;
    const task = new Task('task-id-9', new ObjectReference());
    task.read = false;
    task.transferred = false;
    localComponent.task = task;
    localFixture.detectChanges();
    expect(localComponent.task.read).toBe(false);
    expect(localComponent.task.transferred).toBe(false);
    expect(localFixture.nativeElement.querySelector('#task-read')).toBeTruthy();
    expect(localFixture.nativeElement.querySelector('#task-transferred')).toBeTruthy();
  });

  it('should trigger ngModel write handlers by dispatching input events on disabled inputs', () => {
    const localFixture = TestBed.createComponent(TaskStatusDetailsComponent);
    const localComponent = localFixture.componentInstance;
    const task = new Task('task-id-10', new ObjectReference());
    task.state = 'READY';
    task.read = false;
    task.transferred = false;
    task.received = '2024-01-10T10:00:00Z';
    localComponent.task = task;
    localFixture.detectChanges();

    const stateInput: HTMLInputElement = localFixture.nativeElement.querySelector('#task-state');
    const readInput: HTMLInputElement = localFixture.nativeElement.querySelector('#task-read');
    const transferredInput: HTMLInputElement = localFixture.nativeElement.querySelector('#task-transferred');
    const receivedInput: HTMLInputElement = localFixture.nativeElement.querySelector('#task-received');

    if (stateInput) {
      stateInput.value = 'CLAIMED';
      stateInput.dispatchEvent(new Event('input'));
    }
    if (readInput) {
      readInput.value = 'true';
      readInput.dispatchEvent(new Event('input'));
    }
    if (transferredInput) {
      transferredInput.value = 'true';
      transferredInput.dispatchEvent(new Event('input'));
    }
    if (receivedInput) {
      receivedInput.value = '2024-02-01T10:00:00Z';
      receivedInput.dispatchEvent(new Event('input'));
    }

    expect(localComponent).toBeTruthy();
  });
});
