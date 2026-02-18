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

import { animate, keyframes, style, transition, trigger } from '@angular/animations';

export const highlight = trigger('validation', [
  transition(
    'true => false, false => true, * => true',
    animate(
      '1500ms',
      keyframes([
        style({ opacity: '1' }),
        style({ opacity: '0.3' }),
        style({ opacity: '1' }),
        style({ opacity: '0.3' }),
        style({ opacity: '1' }),
        style({ opacity: '0.3' }),
        style({ opacity: '1' })
      ])
    )
  )
]);
