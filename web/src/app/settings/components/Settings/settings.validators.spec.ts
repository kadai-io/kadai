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

    it('should pass validation for empty string when min is 0', () => {
      const control = new FormControl('', [Validators.minLength(0), Validators.maxLength(10)]);
      expect(control.errors).toBeNull();
      expect(control.valid).toBe(true);
    });

    it.each([
      { value: 'hi', min: 5, max: 20, errorKey: 'minlength', description: 'below min length' },
      { value: 'toolong', min: 1, max: 3, errorKey: 'maxlength', description: 'exceeds max length' },
      { value: 'ab', min: 5, max: undefined, errorKey: 'minlength', description: 'below min-only constraint' },
      { value: 'waytoolong', min: undefined, max: 3, errorKey: 'maxlength', description: 'exceeds max-only constraint' }
    ])('should mark Text as invalid when $description', ({ value, min, max, errorKey }) => {
      const validators = [];
      if (min !== undefined) validators.push(Validators.minLength(min));
      if (max !== undefined) validators.push(Validators.maxLength(max));

      const control = new FormControl(value, validators);
      expect(control.errors?.[errorKey]).toBeDefined();
      expect(control.valid).toBe(false);
    });
  });

  describe('intervalValidator', () => {
    it.each([
      { lower: 10, upper: 80, min: 0, max: 100, description: 'valid interval within bounds' },
      { lower: 0, upper: 100, min: 0, max: 100, description: 'bounds strictly equal minBound and maxBound' },
      { lower: 50, upper: 50, min: 0, max: 100, description: 'lower equals upper bound' }
    ])('should return null when $description', ({ lower, upper, min, max }) => {
      const group = new FormGroup(
        {
          lower: new FormControl(lower),
          upper: new FormControl(upper)
        },
        { validators: [intervalValidator(min, max)] }
      );

      expect(group.errors).toBeNull();
      expect(group.valid).toBe(true);
    });

    it.each([
      { lower: 2, upper: 80, min: 5, max: 100, errorKey: 'belowMin', description: 'lower bound is below minBound' },
      { lower: 10, upper: 80, min: 0, max: 50, errorKey: 'exceedsMax', description: 'upper bound exceeds maxBound' },
      { lower: 90, upper: 10, min: undefined, max: undefined, errorKey: 'invalidOrder', description: 'lower > upper' }
    ])('should mark interval as invalid when $description', ({ lower, upper, min, max, errorKey }) => {
      const group = new FormGroup(
        {
          lower: new FormControl(lower),
          upper: new FormControl(upper)
        },
        { validators: [intervalValidator(min, max)] }
      );

      expect(group.errors?.[errorKey]).toBe(true);
      expect(group.valid).toBe(false);
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
    it.each([
      { value: '{"key": "value"}', description: 'valid JSON object string' },
      { value: '[1, 2, 3]', description: 'valid JSON array' },
      { value: '123', description: 'primitive number string' }
    ])('should return null for $description', ({ value }) => {
      const control = new FormControl(value, [jsonValidator()]);
      expect(control.errors).toBeNull();
      expect(control.valid).toBe(true);
    });

    it.each([
      { value: '', description: 'empty JSON string' },
      { value: null, description: 'null value' },
      { value: undefined, description: 'undefined value' },
      { value: '{not valid json', description: 'malformed JSON string' }
    ])('should return invalidJson error for $description', ({ value }) => {
      const control = new FormControl(value, [jsonValidator()]);
      expect(control.errors?.['invalidJson']).toBe(true);
      expect(control.valid).toBe(false);
    });
  });
});
