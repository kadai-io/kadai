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

import { FormControl, FormGroup, Validators } from '@angular/forms';
import { describe, expect, it } from 'vitest';
import { intervalValidator, jsonValidator } from './settings.validators';

describe('Settings Validators', () => {
  describe('Text Validators', () => {
    it('should pass validation for Text within min and max bounds', () => {
      const control = new FormControl('hello', [Validators.minLength(2), Validators.maxLength(10)]);
      expect(control.errors).toBeNull();
      expect(control.valid).toBe(true);
    });

    it('should mark Text as invalid when length is below min', () => {
      const control = new FormControl('hi', [Validators.minLength(5), Validators.maxLength(20)]);
      expect(control.errors?.['minlength']).toBeDefined();
      expect(control.valid).toBe(false);
    });

    it('should mark Text as invalid when length exceeds max', () => {
      const control = new FormControl('toolong', [Validators.minLength(1), Validators.maxLength(3)]);
      expect(control.errors?.['maxlength']).toBeDefined();
      expect(control.valid).toBe(false);
    });

    it('should mark Text as invalid when below min-only constraint', () => {
      const control = new FormControl('ab', [Validators.minLength(5)]);
      expect(control.errors?.['minlength']).toBeDefined();
      expect(control.valid).toBe(false);
    });

    it('should mark Text as invalid when exceeding max-only constraint', () => {
      const control = new FormControl('waytoolong', [Validators.maxLength(3)]);
      expect(control.errors?.['maxlength']).toBeDefined();
      expect(control.valid).toBe(false);
    });

    it('should pass validation for empty string when min is 0', () => {
      const control = new FormControl('', [Validators.minLength(0), Validators.maxLength(10)]);
      expect(control.errors).toBeNull();
      expect(control.valid).toBe(true);
    });
  });

  describe('intervalValidator', () => {
    it('should return null for a valid interval within bounds', () => {
      const group = new FormGroup(
        {
          lower: new FormControl(10),
          upper: new FormControl(80)
        },
        { validators: [intervalValidator(0, 100)] }
      );

      expect(group.errors).toBeNull();
      expect(group.valid).toBe(true);
    });

    it('should mark interval as invalid when lower bound is below minBound', () => {
      const group = new FormGroup(
        {
          lower: new FormControl(2),
          upper: new FormControl(80)
        },
        { validators: [intervalValidator(5, 100)] }
      );

      expect(group.errors?.['belowMin']).toBe(true);
      expect(group.valid).toBe(false);
    });

    it('should mark interval as invalid when upper bound exceeds maxBound', () => {
      const group = new FormGroup(
        {
          lower: new FormControl(10),
          upper: new FormControl(80)
        },
        { validators: [intervalValidator(0, 50)] }
      );

      expect(group.errors?.['exceedsMax']).toBe(true);
      expect(group.valid).toBe(false);
    });

    it('should mark interval as invalid when lower > upper (invalid order)', () => {
      const group = new FormGroup(
        {
          lower: new FormControl(90),
          upper: new FormControl(10)
        },
        { validators: [intervalValidator()] }
      );

      expect(group.errors?.['invalidOrder']).toBe(true);
      expect(group.valid).toBe(false);
    });

    it('should return null when bounds strictly equal minBound and maxBound (inclusive check)', () => {
      const group = new FormGroup(
        {
          lower: new FormControl(0),
          upper: new FormControl(100)
        },
        { validators: [intervalValidator(0, 100)] }
      );

      expect(group.errors).toBeNull();
      expect(group.valid).toBe(true);
    });

    it('should return null when lower equals upper bound', () => {
      const group = new FormGroup(
        {
          lower: new FormControl(50),
          upper: new FormControl(50)
        },
        { validators: [intervalValidator(0, 100)] }
      );

      expect(group.errors).toBeNull();
      expect(group.valid).toBe(true);
    });

    it('should handle null or undefined control values gracefully without crashing', () => {
      const group = new FormGroup(
        {
          lower: new FormControl(null),
          upper: new FormControl(null)
        },
        { validators: [intervalValidator(0, 100)] }
      );

      expect(() => group.updateValueAndValidity()).not.toThrow();
    });
  });

  describe('jsonValidator', () => {
    it('should return null for a valid JSON string', () => {
      const control = new FormControl('{"key": "value"}', [jsonValidator()]);
      expect(control.errors).toBeNull();
      expect(control.valid).toBe(true);
    });

    it('should return null for empty JSON control value', () => {
      const control = new FormControl('', [jsonValidator()]);
      expect(control.errors).toBeNull();
      expect(control.valid).toBe(true);
    });

    it('should return invalidJson error for a malformed JSON string', () => {
      const control = new FormControl('{not valid json', [jsonValidator()]);
      expect(control.errors?.['invalidJson']).toBe(true);
      expect(control.valid).toBe(false);
    });

    it('should return null for null or undefined control value', () => {
      const control = new FormControl(null, [jsonValidator()]);
      expect(control.errors).toBeNull();
      expect(control.valid).toBe(true);
    });

    it('should return null for a valid JSON array or primitive number', () => {
      const arrayControl = new FormControl('[1, 2, 3]', [jsonValidator()]);
      expect(arrayControl.errors).toBeNull();
      
      const numberControl = new FormControl('123', [jsonValidator()]);
      expect(numberControl.errors).toBeNull();
    });
  });
});
