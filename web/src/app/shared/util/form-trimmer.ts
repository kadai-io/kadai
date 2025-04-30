/*
 * Copyright [2025] [envite consulting GmbH]
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

import { NgForm } from '@angular/forms';

export function trimForm(form: NgForm) {
  Object.keys(form.form.controls).forEach((controlName) => {
    let control = form.form.controls[controlName];
    if (typeof control.value === 'string') {
      control.setValue(control.value.trim());
    }
  });
}

export function trimObject(object: Object) {
  Object.keys(object).forEach((controlName) => {
    let prop = object[controlName];
    if (typeof prop === 'string') {
      object[controlName] = prop.trim();
    } else if (typeof prop === 'object' && prop !== null) {
      trimObject(prop);
    }
  });
}
