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
import { Subject } from 'rxjs';
import { TaskCustomFieldsComponent } from './task-custom-fields.component';
import { FormsValidatorService } from '../../../shared/services/forms-validator/forms-validator.service';
import { Task } from '../../models/task';

describe('TaskCustomFieldsComponent', () => {
  let component: TaskCustomFieldsComponent;
  let fixture: ComponentFixture<TaskCustomFieldsComponent>;
  let inputOverflowSubject: Subject<Map<string, boolean>>;
  let mockFormsValidatorService: {
    inputOverflowObservable: Subject<Map<string, boolean>>;
    validateInputOverflow: ReturnType<typeof vi.fn>;
  };

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
      1,
      [],
      [],
      'custom1-value',
      'custom2-value',
      'custom3-value',
      'custom4-value'
    );

  beforeEach(async () => {
    inputOverflowSubject = new Subject<Map<string, boolean>>();

    mockFormsValidatorService = {
      inputOverflowObservable: inputOverflowSubject,
      validateInputOverflow: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [TaskCustomFieldsComponent],
      providers: [{ provide: FormsValidatorService, useValue: mockFormsValidatorService }]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TaskCustomFieldsComponent);
    component = fixture.componentInstance;
    component.task = createTask();
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

    it('should subscribe to inputOverflowObservable and update inputOverflowMap', () => {
      const testMap = new Map<string, boolean>([['custom1', true]]);

      inputOverflowSubject.next(testMap);

      expect(component.inputOverflowMap).toBe(testMap);
    });

    it('should set validateKeypress to a function', () => {
      expect(typeof component.validateKeypress).toBe('function');
    });

    it('should call formsValidatorService.validateInputOverflow when validateKeypress is invoked', () => {
      const fakeModel = { name: 'custom1', value: 'some-value' } as any;
      const maxLength = 255;

      component.validateKeypress(fakeModel, maxLength);

      expect(mockFormsValidatorService.validateInputOverflow).toHaveBeenCalledWith(fakeModel, maxLength);
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
      component.task = new Task(
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
        1,
        [],
        [],
        '',
        '',
        ''
      );
      component.ngOnInit();

      expect(component.customFields).toContain('custom1');
      expect(component.customFields).toContain('custom2');
      expect(component.customFields).toContain('custom3');
    });
  });

  describe('ngOnDestroy()', () => {
    it('should call next and complete on destroy$', () => {
      const completeSpy = vi.spyOn(component.destroy$, 'complete');
      const nextSpy = vi.spyOn(component.destroy$, 'next');

      component.ngOnDestroy();

      expect(nextSpy).toHaveBeenCalled();
      expect(completeSpy).toHaveBeenCalled();
    });

    it('should unsubscribe from inputOverflowObservable after destroy', () => {
      component.ngOnDestroy();

      const mapAfterDestroy = component.inputOverflowMap;
      const newMap = new Map<string, boolean>([['custom1', true]]);

      inputOverflowSubject.next(newMap);

      expect(component.inputOverflowMap).toBe(mapAfterDestroy);
    });
  });

  describe('inputOverflowMap', () => {
    it('should start as an empty Map before first emission', () => {
      const freshFixture = TestBed.createComponent(TaskCustomFieldsComponent);
      const freshComponent = freshFixture.componentInstance;
      expect(freshComponent.inputOverflowMap).toBeDefined();
      expect(freshComponent.inputOverflowMap.size).toBe(0);
    });

    it('should be updated each time inputOverflowObservable emits', () => {
      const firstMap = new Map<string, boolean>([['custom1', true]]);
      const secondMap = new Map<string, boolean>([['custom2', false]]);

      inputOverflowSubject.next(firstMap);
      expect(component.inputOverflowMap).toBe(firstMap);

      inputOverflowSubject.next(secondMap);
      expect(component.inputOverflowMap).toBe(secondMap);
    });
  });
});
