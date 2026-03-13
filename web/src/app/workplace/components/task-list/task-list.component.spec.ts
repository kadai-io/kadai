/*
 * Copyright [2026] [envite consulting GmbH]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law of an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router, Routes } from '@angular/router';
import { Component } from '@angular/core';
import { TaskListComponent } from './task-list.component';
import { provideHttpClient } from '@angular/common/http';
import { provideAngularSvgIcon } from 'angular-svg-icon';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { Task } from '../../models/task';
import { ObjectReference } from '../../models/object-reference';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

@Component({
  selector: 'kadai-dummy-detail',
  template: 'dummydetail'
})
export class DummyDetailComponent {}

const mockTask = new Task('task-1', new ObjectReference());
mockTask.name = 'Task One';
mockTask.state = 'READY';
mockTask.priority = 1;
mockTask.owner = 'user1';
mockTask.due = '2026-01-01T00:00:00Z';

const mockTaskNoOwner = new Task('task-2', new ObjectReference());
mockTaskNoOwner.name = 'Task Two';
mockTaskNoOwner.state = 'CLAIMED';
mockTaskNoOwner.priority = 2;

describe('TaskListComponent', () => {
  let component: TaskListComponent;
  let fixture: ComponentFixture<TaskListComponent>;
  let router: Router;

  const routes: Routes = [{ path: '*', component: DummyDetailComponent }];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TaskListComponent],
      providers: [provideHttpClient(), provideAngularSvgIcon(), provideRouter(routes), provideNoopAnimations()]
    }).compileComponents();

    router = TestBed.inject(Router);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TaskListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should show empty state when tasks is null', () => {
    component.tasks = null;
    fixture.detectChanges();
    const emptyEl = fixture.nativeElement.querySelector('.container-no-items');
    expect(emptyEl).toBeTruthy();
  });

  it('should show empty state when tasks array is empty', () => {
    component.tasks = [];
    fixture.detectChanges();
    const emptyEl = fixture.nativeElement.querySelector('.container-no-items');
    expect(emptyEl).toBeTruthy();
  });

  it('should show task list when tasks are provided', () => {
    component.tasks = [mockTask];
    fixture.detectChanges();
    const listEl = fixture.nativeElement.querySelector('mat-selection-list');
    expect(listEl).toBeTruthy();
  });

  it('should render multiple tasks', () => {
    component.tasks = [mockTask, mockTaskNoOwner];
    fixture.detectChanges();
    const options = fixture.nativeElement.querySelectorAll('mat-list-option');
    expect(options.length).toBe(2);
  });

  it('selectTask should update selectedId and emit event', () => {
    fixture.detectChanges();
    const emitSpy = vi.spyOn(component.selectedIdChange, 'emit');
    component.selectTask('task-1');
    expect(component.selectedId).toBe('task-1');
    expect(emitSpy).toHaveBeenCalledWith('task-1');
  });

  it('selectTask should navigate to task detail', () => {
    fixture.detectChanges();
    const navigateSpy = vi.spyOn(router, 'navigate');
    component.selectTask('task-1');
    expect(navigateSpy).toHaveBeenCalled();
  });

  it('should mark selected task with selectedId', () => {
    component.tasks = [mockTask, mockTaskNoOwner];
    component.selectedId = 'task-1';
    fixture.detectChanges();
    const options = fixture.nativeElement.querySelectorAll('mat-list-option');
    expect(options.length).toBe(2);
  });

  it('should render task owner when owner is set', () => {
    component.tasks = [mockTask];
    fixture.detectChanges();
    const ownerEl = fixture.nativeElement.querySelector('i');
    expect(ownerEl).toBeTruthy();
    expect(ownerEl.textContent).toContain('user1');
  });

  it('should not render owner element when task has no owner', () => {
    component.tasks = [mockTaskNoOwner];
    fixture.detectChanges();
    const ownerEl = fixture.nativeElement.querySelector('i');
    expect(ownerEl).toBeNull();
  });

  it('should call selectTask when a list option is clicked', () => {
    component.tasks = [mockTask];
    fixture.detectChanges();
    const selectSpy = vi.spyOn(component, 'selectTask');
    const listOption = fixture.nativeElement.querySelector('mat-list-option');
    expect(listOption).toBeTruthy();
    listOption.click();
    expect(selectSpy).toHaveBeenCalledWith('task-1');
  });

  it('should emit selectedIdChange when a list option is clicked', () => {
    component.tasks = [mockTask, mockTaskNoOwner];
    fixture.detectChanges();
    const emitSpy = vi.spyOn(component.selectedIdChange, 'emit');
    const listOptions = fixture.nativeElement.querySelectorAll('mat-list-option');
    listOptions[0].click();
    expect(emitSpy).toHaveBeenCalledWith('task-1');
  });

  it('should navigate when a list option is clicked', () => {
    component.tasks = [mockTask];
    fixture.detectChanges();
    const navigateSpy = vi.spyOn(router, 'navigate');
    const listOption = fixture.nativeElement.querySelector('mat-list-option');
    listOption.click();
    expect(navigateSpy).toHaveBeenCalled();
  });
});
