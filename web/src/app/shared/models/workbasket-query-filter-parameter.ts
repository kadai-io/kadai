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

import { WorkbasketType } from './workbasket-type';
import { WorkbasketPermission } from './workbasket-permission';

export interface WorkbasketQueryFilterParameter {
  name?: string[];
  'name-like'?: string[];
  key?: string[];
  'key-like'?: string[];
  owner?: string[];
  'owner-like'?: string[];
  'description-like'?: string[];
  domain?: string[];
  type?: WorkbasketType[];
  'required-permission'?: WorkbasketPermission[];
}
