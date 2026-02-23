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

import { describe, expect, it, vi } from 'vitest';
import { trimForm, trimObject } from './form-trimmer';

describe('form-trimmer', () => {
  describe('trimForm', () => {
    it('should trim string values of form controls', () => {
      const mockControl = { value: '  hello  ', setValue: vi.fn() };
      const mockForm = { form: { controls: { name: mockControl } } } as any;

      trimForm(mockForm);

      expect(mockControl.setValue).toHaveBeenCalledWith('hello');
    });

    it('should not call setValue for non-string control values', () => {
      const mockControl = { value: 42, setValue: vi.fn() };
      const mockForm = { form: { controls: { count: mockControl } } } as any;

      trimForm(mockForm);

      expect(mockControl.setValue).not.toHaveBeenCalled();
    });

    it('should not call setValue for boolean control values', () => {
      const mockControl = { value: true, setValue: vi.fn() };
      const mockForm = { form: { controls: { flag: mockControl } } } as any;

      trimForm(mockForm);

      expect(mockControl.setValue).not.toHaveBeenCalled();
    });

    it('should not call setValue for null control values', () => {
      const mockControl = { value: null, setValue: vi.fn() };
      const mockForm = { form: { controls: { field: mockControl } } } as any;

      trimForm(mockForm);

      expect(mockControl.setValue).not.toHaveBeenCalled();
    });

    it('should process multiple controls', () => {
      const mockControl1 = { value: '  first  ', setValue: vi.fn() };
      const mockControl2 = { value: '  second  ', setValue: vi.fn() };
      const mockControl3 = { value: 99, setValue: vi.fn() };
      const mockForm = {
        form: { controls: { c1: mockControl1, c2: mockControl2, c3: mockControl3 } }
      } as any;

      trimForm(mockForm);

      expect(mockControl1.setValue).toHaveBeenCalledWith('first');
      expect(mockControl2.setValue).toHaveBeenCalledWith('second');
      expect(mockControl3.setValue).not.toHaveBeenCalled();
    });

    it('should trim leading and trailing whitespace from string values', () => {
      const mockControl = { value: '\t  padded  \n', setValue: vi.fn() };
      const mockForm = { form: { controls: { field: mockControl } } } as any;

      trimForm(mockForm);

      expect(mockControl.setValue).toHaveBeenCalledWith('padded');
    });

    it('should keep empty string as empty string after trim', () => {
      const mockControl = { value: '   ', setValue: vi.fn() };
      const mockForm = { form: { controls: { field: mockControl } } } as any;

      trimForm(mockForm);

      expect(mockControl.setValue).toHaveBeenCalledWith('');
    });
  });

  describe('trimObject', () => {
    it('should trim string properties of an object', () => {
      const obj = { name: '  Alice  ', city: '  Berlin  ' };

      trimObject(obj);

      expect(obj.name).toBe('Alice');
      expect(obj.city).toBe('Berlin');
    });

    it('should not modify non-string primitive properties', () => {
      const obj: any = { count: 5, active: false };

      trimObject(obj);

      expect(obj.count).toBe(5);
      expect(obj.active).toBe(false);
    });

    it('should recursively trim nested object string properties', () => {
      const obj: any = { person: { firstName: '  Bob  ', lastName: '  Smith  ' } };

      trimObject(obj);

      expect(obj.person.firstName).toBe('Bob');
      expect(obj.person.lastName).toBe('Smith');
    });

    it('should handle null property values without throwing', () => {
      const obj: any = { name: '  test  ', nullField: null };

      expect(() => trimObject(obj)).not.toThrow();
      expect(obj.name).toBe('test');
      expect(obj.nullField).toBeNull();
    });

    it('should not recurse into null properties', () => {
      const obj: any = { nested: null, value: '  hello  ' };

      trimObject(obj);

      expect(obj.nested).toBeNull();
      expect(obj.value).toBe('hello');
    });

    it('should handle deeply nested objects', () => {
      const obj: any = { a: { b: { c: '  deep  ' } } };

      trimObject(obj);

      expect(obj.a.b.c).toBe('deep');
    });

    it('should handle empty object without throwing', () => {
      const obj: any = {};

      expect(() => trimObject(obj)).not.toThrow();
    });

    it('should trim string even within mixed-type object', () => {
      const obj: any = {
        name: '  padded  ',
        count: 3,
        active: true,
        nested: { label: '  inner  ' }
      };

      trimObject(obj);

      expect(obj.name).toBe('padded');
      expect(obj.count).toBe(3);
      expect(obj.active).toBe(true);
      expect(obj.nested.label).toBe('inner');
    });
  });
});
