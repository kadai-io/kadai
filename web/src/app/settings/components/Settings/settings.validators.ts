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
    if (!control.value) return null;
    try {
      JSON.parse(control.value);
      return null;
    } catch {
      return { invalidJson: true };
    }
  };
}

export function intervalValidator(minBound?: number, maxBound?: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const lower = control.get('lower')?.value;
    const upper = control.get('upper')?.value;

    const errors: ValidationErrors = {};

    if (lower !== null && lower !== undefined && upper !== null && upper !== undefined) {
      if (lower > upper) {
        errors.invalidOrder = true;
      }
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
