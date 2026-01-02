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

import { Injectable } from '@angular/core';
import { messageByErrorCode } from './message-by-error-code';
import { messageTypes } from './message-types';

@Injectable({
  providedIn: 'root'
})
export class ObtainMessageService {
  getMessage(key: string, messageVariables: object = {}, type: messageTypes): string {
    let message =
      messageByErrorCode[type][key] ||
      messageByErrorCode[type]['FALLBACK'] ||
      `The message with type '${type}' and key '${key}' is not configured`;

    for (const [replacementKey, value] of Object.entries(messageVariables)) {
      message = message.replace(`{${replacementKey}}`, `'${value}'`);
    }

    return message;
  }
}
