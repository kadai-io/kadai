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

export interface TaskHistoryQueryFilterParameter {
  'event-type': string[];
  'event-type-like': string[];
  'user-id': string[];
  'user-id-like': string[];
  created: string[];
  domain: string[];
  'task-id': string[];
  'task-id-like': string[];
  'business-process-id': string[];
  'business-process-id-like': string[];
  'parent-business-process-id': string[];
  'parent-business-process-id-like': string[];
  'task-classification-key': string[];
  'task-classification-key-like': string[];
  'task-classification-category': string[];
  'task-classification-category-like': string[];
  'attachment-classification-key': string[];
  'attachment-classification-key-like': string[];
  'workbasket-key-like': string[];
  'por-company': string[];
  'por-company-like': string[];
  'por-system': string[];
  'por-system-like': string[];
  'por-instance': string[];
  'por-instance-like': string[];
  'por-value': string[];
  'por-value-like': string[];
  'custom-1': string[];
  'custom-1-like': string[];
  'custom-2': string[];
  'custom-2-like': string[];
  'custom-3': string[];
  'custom-3-like': string[];
  'custom-4': string[];
  'custom-4-like': string[];
}
