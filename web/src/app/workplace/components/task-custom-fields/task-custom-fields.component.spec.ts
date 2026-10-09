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
import { beforeEach, describe, expect, it } from 'vitest';
import { TaskCustomFieldsComponent } from './task-custom-fields.component';
import { Task } from '../../models/task';

describe('TaskCustomFieldsComponent', () => {
  let component: TaskCustomFieldsComponent;
  let fixture: ComponentFixture<TaskCustomFieldsComponent>;

  const createTask = (): Task =>
    new Task(
      'task-id-1',
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
      undefined,
      undefined,
      undefined,
      'Test Task',
      undefined,
      undefined,
      undefined,
      undefined,
      false,
      false,
      false,
      1,
      [],
      [],
      'custom1-value',
      'custom2-value',
      'custom3-value',
      'custom4-value'
    );

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TaskCustomFieldsComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TaskCustomFieldsComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('task', createTask());
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit()', () => {
    it('should set customFields from task keys starting with "custom" and containing a digit', () => {
      expect(component.customFields).toBeDefined();
      expect(component.customFields).toContain('custom1');
      expect(component.customFields).toContain('custom2');
      expect(component.customFields).toContain('custom3');
      expect(component.customFields).toContain('custom4');
    });

    it('should exclude task keys that start with "custom" but contain no digit', () => {
      expect(component.customFields).not.toContain('customAttributes');
    });
  });

  describe('customFields filtering', () => {
    it('should include all custom numeric fields present on the task', () => {
      const task = createTask();
      const allKeys = Object.keys(task);
      const expectedCustomFields = allKeys.filter((key) => key.startsWith('custom') && /\d/.test(key));

      expect(component.customFields).toEqual(expectedCustomFields);
    });

    it('should not include "customAttributes" in customFields', () => {
      expect(component.customFields).not.toContain('customAttributes');
    });

    it('should return custom fields even when task custom values are empty strings', () => {
      component.task.set(
        new Task(
          'task-id-2',
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
          undefined,
          undefined,
          undefined,
          'Name',
          undefined,
          undefined,
          undefined,
          undefined,
          false,
          false,
          false,
          1,
          [],
          [],
          '',
          '',
          ''
        )
      );
      component.ngOnInit();

      expect(component.customFields).toContain('custom1');
      expect(component.customFields).toContain('custom2');
      expect(component.customFields).toContain('custom3');
    });
  });

  describe('template rendering & overflow directives', () => {
    it('should not render anything when task is null', () => {
      fixture.componentRef.setInput('task', null);
      fixture.detectChanges();
      const container = fixture.nativeElement.querySelector('.task-custom-fields');
      expect(container).toBeNull();
    });

    it('should render custom field inputs when task is set', () => {
      const inputs = fixture.nativeElement.querySelectorAll('input[id^="task-custom-"]');
      expect(inputs.length).toBeGreaterThan(0);
    });

    it('should display overflow error when a custom field value exceeds the character limit', () => {
      const input: HTMLInputElement = fixture.nativeElement.querySelector('#task-custom-1');
      expect(input).toBeTruthy();

      input.value = 'a'.repeat(256);
      input.dispatchEvent(new Event('input'));
      fixture.detectChanges();

      const errorEl = fixture.nativeElement.querySelector('.error');
      expect(errorEl).toBeTruthy();
    });

    it('should not show overflow error when custom field value is within limit', () => {
      const input: HTMLInputElement = fixture.nativeElement.querySelector('#task-custom-1');
      expect(input).toBeTruthy();

      input.value = 'Valid text';
      input.dispatchEvent(new Event('input'));
      fixture.detectChanges();

      const errorEl = fixture.nativeElement.querySelector('.error');
      expect(errorEl).toBeNull();
    });

    it('should render spacer elements for even-index custom fields', () => {
      const spacers = fixture.nativeElement.querySelectorAll('.task-custom-fields__spacer');
      expect(spacers.length).toBeGreaterThan(0);
    });
  });
});
