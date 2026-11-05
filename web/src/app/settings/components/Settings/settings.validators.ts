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

import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function jsonValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;

    if (typeof value !== 'string' || value.trim() === '') {
      return { invalidJson: true };
    }

    try {
      JSON.parse(value);
      return null;
    } catch {
      return { invalidJson: true };
    }
  };
}

export function intervalValidator(minBound?: number, maxBound?: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const lowerRaw = control.get('lower')?.value;
    const upperRaw = control.get('upper')?.value;

    const isLowerEmpty = lowerRaw === null || lowerRaw === undefined || lowerRaw === '';
    const isUpperEmpty = upperRaw === null || upperRaw === undefined || upperRaw === '';

    if (isLowerEmpty || isUpperEmpty) {
      return { requiredBounds: true };
    }

    const lower = Number(lowerRaw);
    const upper = Number(upperRaw);

    const errors: ValidationErrors = {};

    if (lower > upper) {
      errors.invalidOrder = true;
    }

    if (minBound !== undefined && (lower < minBound || upper < minBound)) {
      errors.belowMin = true;
    }

    if (maxBound !== undefined && (lower > maxBound || upper > maxBound)) {
      errors.exceedsMax = true;
    }

    return Object.keys(errors).length > 0 ? errors : null;
  };
}
