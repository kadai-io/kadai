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
import { TaskAttributeValueComponent } from './task-attribute-value.component';
import { beforeEach, describe, expect, it } from 'vitest';

describe('TaskAttributeValueComponent', () => {
  let component: TaskAttributeValueComponent;
  let fixture: ComponentFixture<TaskAttributeValueComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TaskAttributeValueComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TaskAttributeValueComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('addAttribute()', () => {
    it('should add a new empty attribute to the attributes array', () => {
      component.attributes = [];
      component.addAttribute();
      expect(component.attributes.length).toBe(1);
      expect(component.attributes[0]).toEqual({ key: '', value: '' });
    });

    it('should append to existing attributes', () => {
      component.attributes = [{ key: 'existing', value: 'val' }];
      component.addAttribute();
      expect(component.attributes.length).toBe(2);
      expect(component.attributes[1]).toEqual({ key: '', value: '' });
    });
  });

  describe('removeAttribute()', () => {
    it('should remove the attribute at the given index', () => {
      component.attributes = [
        { key: 'key1', value: 'val1' },
        { key: 'key2', value: 'val2' },
        { key: 'key3', value: 'val3' }
      ];
      component.removeAttribute(1);
      expect(component.attributes.length).toBe(2);
      expect(component.attributes[0].key).toBe('key1');
      expect(component.attributes[1].key).toBe('key3');
    });

    it('should remove the first attribute when index is 0', () => {
      component.attributes = [
        { key: 'key1', value: 'val1' },
        { key: 'key2', value: 'val2' }
      ];
      component.removeAttribute(0);
      expect(component.attributes.length).toBe(1);
      expect(component.attributes[0].key).toBe('key2');
    });
  });
});
