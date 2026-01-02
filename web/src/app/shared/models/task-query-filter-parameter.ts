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

import { TaskState } from './task-state';

export interface TaskQueryFilterParameter {
  name?: string[];
  'name-like'?: string[];
  priority?: number[];
  state?: TaskState[];
  'classification-key'?: string[];
  'task-id'?: string[];
  'workbasket-id'?: string[];
  'workbasket-key'?: string[];
  domain?: string[];
  owner?: string[];
  'owner-like'?: string[];
  'por-company'?: string[];
  'por-system'?: string[];
  'por-instance'?: string[];
  'por-type'?: string[];
  'por-value'?: string[];
  planned?: string[];
  'planned-from'?: string[];
  'planned-until'?: string[];
  due?: string[];
  'due-from'?: string[];
  'due-until'?: string[];
  received?: string[];
  'received-from'?: string[];
  'received-until'?: string[];
  'wildcard-search-fields'?: string[];
  'wildcard-search-value'?: string[];
  'external-id'?: string[];
  'custom-1'?: string[];
  'custom-2'?: string[];
  'custom-3'?: string[];
  'custom-4'?: string[];
  'custom-5'?: string[];
  'custom-6'?: string[];
  'custom-7'?: string[];
  'custom-8'?: string[];
  'custom-9'?: string[];
  'custom-10'?: string[];
  'custom-11'?: string[];
  'custom-12'?: string[];
  'custom-13'?: string[];
  'custom-14'?: string[];
  'custom-15'?: string[];
  'custom-16'?: string[];
}
