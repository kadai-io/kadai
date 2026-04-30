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
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

describe('TaskAttributeValueComponent', () => {
  let component: TaskAttributeValueComponent;
  let fixture: ComponentFixture<TaskAttributeValueComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TaskAttributeValueComponent],
      providers: [provideNoopAnimations()]
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
      component.attributes.set([]);
      component.addAttribute();
      expect(component.attributes().length).toBe(1);
      expect(component.attributes()[0]).toEqual({ key: '', value: '' });
    });

    it('should append to existing attributes', () => {
      component.attributes.set([{ key: 'existing', value: 'val' }]);
      component.addAttribute();
      expect(component.attributes().length).toBe(2);
      expect(component.attributes()[1]).toEqual({ key: '', value: '' });
    });
  });

  describe('removeAttribute()', () => {
    it('should remove the attribute at the given index', () => {
      component.attributes.set([
        { key: 'key1', value: 'val1' },
        { key: 'key2', value: 'val2' },
        { key: 'key3', value: 'val3' }
      ]);
      component.removeAttribute(1);
      expect(component.attributes().length).toBe(2);
      expect(component.attributes()[0].key).toBe('key1');
      expect(component.attributes()[1].key).toBe('key3');
    });

    it('should remove the first attribute when index is 0', () => {
      component.attributes.set([
        { key: 'key1', value: 'val1' },
        { key: 'key2', value: 'val2' }
      ]);
      component.removeAttribute(0);
      expect(component.attributes().length).toBe(1);
      expect(component.attributes()[0].key).toBe('key2');
    });
  });

  describe('template rendering', () => {
    let localFixture: ComponentFixture<TaskAttributeValueComponent>;
    let localComponent: TaskAttributeValueComponent;

    beforeEach(() => {
      localFixture = TestBed.createComponent(TaskAttributeValueComponent);
      localComponent = localFixture.componentInstance;
    });

    it('should show "no custom attribute" message when attributes is empty', () => {
      localComponent.attributes.set([]);
      localFixture.detectChanges();
      expect(localFixture.nativeElement.textContent).toContain('no custom attribute');
    });

    it('should show "no callback information" message when attributes empty and callbackInfo is true', () => {
      localComponent.attributes.set([]);
      localFixture.componentRef.setInput('callbackInfo', true);
      localFixture.detectChanges();
      expect(localFixture.nativeElement.textContent).toContain('no callback information');
    });

    it('should render attribute table when attributes are present', () => {
      localComponent.attributes.set([{ key: 'key1', value: 'val1' }]);
      localFixture.detectChanges();
      const table = localFixture.nativeElement.querySelector('.task-attribute-value__table');
      expect(table).toBeTruthy();
    });

    it('should render two rows with different background for even/odd entries', () => {
      localComponent.attributes.set([
        { key: 'key1', value: 'val1' },
        { key: 'key2', value: 'val2' }
      ]);
      localFixture.detectChanges();
      const rows = localFixture.nativeElement.querySelectorAll('.task-attribute-value__row');
      expect(rows.length).toBeGreaterThan(1);
    });

    it('should apply grey background class to even-indexed rows', () => {
      localComponent.attributes.set([
        { key: 'key1', value: 'val1' },
        { key: 'key2', value: 'val2' }
      ]);
      localFixture.detectChanges();
      const entryRows = localFixture.nativeElement.querySelectorAll('.task-attribute-value__background-grey');
      expect(entryRows.length).toBeGreaterThan(0);
    });

    it('should apply white background class to odd-indexed rows', () => {
      localComponent.attributes.set([
        { key: 'key1', value: 'val1' },
        { key: 'key2', value: 'val2' }
      ]);
      localFixture.detectChanges();
      const entryRows = localFixture.nativeElement.querySelectorAll('.task-attribute-value__background-white');
      expect(entryRows.length).toBeGreaterThan(0);
    });

    it('should call removeAttribute when remove button is clicked', () => {
      const removeSpy = vi.spyOn(localComponent, 'removeAttribute');
      localComponent.attributes.set([{ key: 'key1', value: 'val1' }]);
      localFixture.detectChanges();
      const removeBtn = localFixture.nativeElement.querySelector('button[mattooltip]');
      expect(removeBtn).toBeTruthy();
      removeBtn.click();
      expect(removeSpy).toHaveBeenCalledWith(0);
    });

    it('should remove the correct entry when removing second of multiple attributes via DOM click', () => {
      localComponent.attributes.set([
        { key: 'key1', value: 'val1' },
        { key: 'key2', value: 'val2' }
      ]);
      localFixture.detectChanges();
      const removeBtns = localFixture.nativeElement.querySelectorAll('button[mattooltip]');
      expect(removeBtns.length).toBe(2);
      removeBtns[1].click();
      localFixture.detectChanges();
      expect(localComponent.attributes().length).toBe(1);
      expect(localComponent.attributes()[0].key).toBe('key1');
    });

    it('should call addAttribute when add button is clicked', () => {
      const addSpy = vi.spyOn(localComponent, 'addAttribute');
      localComponent.attributes.set([]);
      localFixture.detectChanges();
      const buttons: HTMLButtonElement[] = Array.from(localFixture.nativeElement.querySelectorAll('button'));
      const addBtn = buttons.find((b) => b.textContent?.includes('Add'));
      expect(addBtn).toBeTruthy();
      addBtn.click();
      expect(addSpy).toHaveBeenCalled();
    });

    it('should add a new entry to the DOM when add button is clicked', () => {
      localComponent.attributes.set([]);
      localFixture.detectChanges();
      const buttons: HTMLButtonElement[] = Array.from(localFixture.nativeElement.querySelectorAll('button'));
      const addBtn = buttons.find((b) => b.textContent?.includes('Add'));
      addBtn.click();
      localFixture.detectChanges();
      const table = localFixture.nativeElement.querySelector('.task-attribute-value__table');
      expect(table).toBeTruthy();
    });

    it('should show callback info label in add button when callbackInfo is true', () => {
      localFixture.componentRef.setInput('callbackInfo', true);
      localFixture.detectChanges();
      expect(localFixture.nativeElement.textContent).toContain('callback information');
    });

    it('should use "custom-attribute" as input name prefix when callbackInfo is false', () => {
      localFixture.componentRef.setInput('callbackInfo', false);
      localComponent.attributes.set([{ key: 'k', value: 'v' }]);
      localFixture.detectChanges();
      const inputs: HTMLInputElement[] = Array.from(localFixture.nativeElement.querySelectorAll('input[matinput]'));
      const keyInput = inputs.find((el) => el.name === 'custom-attribute-0');
      expect(keyInput).toBeTruthy();
    });

    it('should use "callback-info" as input name prefix when callbackInfo is true', () => {
      localFixture.componentRef.setInput('callbackInfo', true);
      localComponent.attributes.set([{ key: 'k', value: 'v' }]);
      localFixture.detectChanges();
      const inputs: HTMLInputElement[] = Array.from(localFixture.nativeElement.querySelectorAll('input[matinput]'));
      const keyInput = inputs.find((el) => el.name === 'callback-info-0');
      expect(keyInput).toBeTruthy();
    });

    it('should update entry.key via ngModel when key input value changes', () => {
      localComponent.attributes.set([{ key: '', value: '' }]);
      localFixture.detectChanges();
      const inputs: HTMLInputElement[] = Array.from(localFixture.nativeElement.querySelectorAll('input[matinput]'));
      const keyInput = inputs[0];
      expect(keyInput).toBeTruthy();
      keyInput.value = 'newKey';
      keyInput.dispatchEvent(new Event('input'));
      localFixture.detectChanges();
      expect(localComponent.attributes()[0].key).toBe('newKey');
    });

    it('should update entry.value via ngModel when value input changes', () => {
      localComponent.attributes.set([{ key: '', value: '' }]);
      localFixture.detectChanges();
      const inputs: HTMLInputElement[] = Array.from(localFixture.nativeElement.querySelectorAll('input[matinput]'));
      const valueInput = inputs[1];
      expect(valueInput).toBeTruthy();
      valueInput.value = 'newValue';
      valueInput.dispatchEvent(new Event('input'));
      localFixture.detectChanges();
      expect(localComponent.attributes()[0].value).toBe('newValue');
    });
  });
});
