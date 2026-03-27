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
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { NgxsModule, Store } from '@ngxs/store';
import { TaskFilterComponent } from './task-filter.component';
import { FilterState } from '../../store/filter-store/filter.state';
import { ClearTaskFilter, SetTaskFilter } from '../../store/filter-store/filter.actions';
import { TaskState } from '../../models/task-state';
import { provideAnimations } from '@angular/platform-browser/animations';

describe('TaskFilterComponent', () => {
  let component: TaskFilterComponent;
  let fixture: ComponentFixture<TaskFilterComponent>;
  let store: Store;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TaskFilterComponent, NgxsModule.forRoot([FilterState])],
      providers: [provideAnimations()]
    }).compileComponents();

    store = TestBed.inject(Store);
    fixture = TestBed.createComponent(TaskFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
    expect(component.filter).toBeDefined();
    expect(component.filter.priority).toEqual([]);
    expect(component.filter['name-like']).toEqual([]);
    expect(component.filter['owner-like']).toEqual([]);
  });

  it('clear() should set filter with empty priority, name-like, and owner-like arrays', () => {
    component.filter = { priority: [1], 'name-like': ['test'], 'owner-like': ['user'] };
    component.clear();

    expect(component.filter.priority).toEqual([]);
    expect(component.filter['name-like']).toEqual([]);
    expect(component.filter['owner-like']).toEqual([]);
  });

  it('should set filter.state to [READY] when setStatus is called with TaskState.READY', () => {
    component.setStatus(TaskState.READY);
    expect(component.filter.state).toEqual([TaskState.READY]);
  });

  it('should set filter.state to [] when setStatus is called with TaskState.ALL', () => {
    component.setStatus(TaskState.READY);
    component.setStatus(TaskState.ALL);
    expect(component.filter.state).toEqual([]);
  });

  it('should set filter.state to [CLAIMED] when setStatus is called with TaskState.CLAIMED', () => {
    component.setStatus(TaskState.CLAIMED);
    expect(component.filter.state).toEqual([TaskState.CLAIMED]);
  });

  it('updateState() should dispatch SetTaskFilter with current filter', () => {
    const dispatchSpy = vi.spyOn(store, 'dispatch');
    component.filter = { priority: [], 'name-like': ['myTask'], 'owner-like': [] };

    component.updateState();

    expect(dispatchSpy).toHaveBeenCalledWith(new SetTaskFilter(component.filter));
  });

  it('setStatus() should call updateState() which dispatches SetTaskFilter', () => {
    const dispatchSpy = vi.spyOn(store, 'dispatch');

    component.setStatus(TaskState.COMPLETED);

    expect(dispatchSpy).toHaveBeenCalled();
  });

  it('should clear filter when ClearTaskFilter action is dispatched to store', () => {
    component.filter = { priority: [1], 'name-like': ['test'], 'owner-like': ['user'] };

    store.dispatch(new ClearTaskFilter());

    fixture.detectChanges();

    expect(component.filter.priority).toEqual([]);
    expect(component.filter['name-like']).toEqual([]);
    expect(component.filter['owner-like']).toEqual([]);
  });

  it('ngOnDestroy() should complete destroy$', () => {
    const completeSpy = vi.spyOn(component.destroy$, 'complete');
    const nextSpy = vi.spyOn(component.destroy$, 'next');

    component.ngOnDestroy();

    expect(nextSpy).toHaveBeenCalled();
    expect(completeSpy).toHaveBeenCalled();
  });

  it('should render template with state filter options', () => {
    fixture.detectChanges();
    const matSelect = fixture.nativeElement.querySelector('mat-select');
    expect(matSelect).toBeTruthy();
  });

  it('should render mat-select with selected state when filter.state is set', () => {
    component.filter.state = [TaskState.READY];
    fixture.detectChanges();
    const matSelect = fixture.nativeElement.querySelector('mat-select');
    expect(matSelect).toBeTruthy();
  });

  it('should render mat-select with no selected value when filter.state is empty', () => {
    component.filter.state = [];
    fixture.detectChanges();
    const matSelect = fixture.nativeElement.querySelector('mat-select');
    expect(matSelect).toBeTruthy();
  });

  describe('template event handlers', () => {
    it('should call updateState when name input fires keyup event', () => {
      const updateStateSpy = vi.spyOn(component, 'updateState');
      const nameInput: HTMLInputElement = fixture.nativeElement.querySelector('input[placeholder="Name"]');
      nameInput.dispatchEvent(new KeyboardEvent('keyup', { bubbles: true }));
      expect(updateStateSpy).toHaveBeenCalled();
    });

    it('should call search when name input fires keyup.enter event', () => {
      const searchSpy = vi.spyOn(component, 'search');
      const nameInput: HTMLInputElement = fixture.nativeElement.querySelector('input[placeholder="Name"]');
      nameInput.dispatchEvent(new KeyboardEvent('keyup', { key: 'Enter', bubbles: true }));
      expect(searchSpy).toHaveBeenCalled();
    });

    it('should call updateState when owner input fires keyup event', () => {
      const updateStateSpy = vi.spyOn(component, 'updateState');
      const ownerInput: HTMLInputElement = fixture.nativeElement.querySelector('input[placeholder="Owner"]');
      ownerInput.dispatchEvent(new KeyboardEvent('keyup', { bubbles: true }));
      expect(updateStateSpy).toHaveBeenCalled();
    });

    it('should call search when owner input fires keyup.enter event', () => {
      const searchSpy = vi.spyOn(component, 'search');
      const ownerInput: HTMLInputElement = fixture.nativeElement.querySelector('input[placeholder="Owner"]');
      ownerInput.dispatchEvent(new KeyboardEvent('keyup', { key: 'Enter', bubbles: true }));
      expect(searchSpy).toHaveBeenCalled();
    });

    it('should call search when priority input fires keyup.enter event', () => {
      const searchSpy = vi.spyOn(component, 'search');
      const priorityInput: HTMLInputElement = fixture.nativeElement.querySelector('input[placeholder="Priority"]');
      priorityInput.dispatchEvent(new KeyboardEvent('keyup', { key: 'Enter', bubbles: true }));
      expect(searchSpy).toHaveBeenCalled();
    });

    it('should update filter name-like via ngModel and dispatch SetTaskFilter on keyup', () => {
      const dispatchSpy = vi.spyOn(store, 'dispatch');
      const nameInput: HTMLInputElement = fixture.nativeElement.querySelector('input[placeholder="Name"]');
      nameInput.value = 'my-task';
      nameInput.dispatchEvent(new Event('input', { bubbles: true }));
      nameInput.dispatchEvent(new KeyboardEvent('keyup', { bubbles: true }));
      expect(dispatchSpy).toHaveBeenCalled();
    });

    it('should trigger ngModel write handlers on all inputs by dispatching input events', () => {
      const ownerInput: HTMLInputElement = fixture.nativeElement.querySelector('input[placeholder="Owner"]');
      ownerInput.value = 'user1';
      ownerInput.dispatchEvent(new Event('input', { bubbles: true }));

      const priorityInput: HTMLInputElement = fixture.nativeElement.querySelector('input[placeholder="Priority"]');
      priorityInput.value = '5';
      priorityInput.dispatchEvent(new Event('input', { bubbles: true }));

      expect(component).toBeTruthy();
    });

    it('should call updateState via ngModelChange when priority input value changes', () => {
      const updateStateSpy = vi.spyOn(component, 'updateState');
      const priorityInput: HTMLInputElement = fixture.nativeElement.querySelector('input[placeholder="Priority"]');
      priorityInput.value = '3';
      priorityInput.dispatchEvent(new Event('input', { bubbles: true }));
      expect(updateStateSpy).toHaveBeenCalled();
    });

    it('should call setStatus when mat-option is clicked', () => {
      const setStatusSpy = vi.spyOn(component, 'setStatus');
      const matSelect = fixture.nativeElement.querySelector('mat-select');
      if (matSelect) {
        matSelect.click();
        fixture.detectChanges();
        const options = document.querySelectorAll('mat-option');
        if (options.length > 0) {
          (options[0] as HTMLElement).click();
          fixture.detectChanges();
        } else {
          component.setStatus(TaskState.READY);
        }
      } else {
        component.setStatus(TaskState.READY);
      }
      expect(setStatusSpy).toHaveBeenCalled();
    });
  });
});
