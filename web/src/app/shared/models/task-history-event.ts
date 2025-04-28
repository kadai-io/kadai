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

export interface TaskHistoryEventData {
  taskHistoryId: string;
  parentBusinessProcessId: string;
  businessProcessId: string;
  created: string;
  userId: string;
  eventType: string;
  workbasketKey: string;
  porType: string;
  porValue: string;
  domain: string;
  taskId: string;
  porCompany: string;
  porSystem: string;
  porInstance: string;
  taskClassificationKey: string;
  taskClassificationCategory: string;
  attachmentClassificationKey: string;
  custom1: string;
  custom2: string;
  custom3: string;
  custom4: string;
  comment: string;
  oldValue: string;
  newValue: string;
  oldData: string;
  newData: string;
}
