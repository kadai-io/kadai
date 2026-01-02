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

export enum WorkbasketType {
  ALL = 'ALL',
  PERSONAL = 'PERSONAL',
  GROUP = 'GROUP',
  CLEARANCE = 'CLEARANCE',
  TOPIC = 'TOPIC'
}

export const ALL_TYPES: Map<WorkbasketType, string> = new Map([
  [WorkbasketType.ALL, 'All'],
  [WorkbasketType.PERSONAL, 'Personal'],
  [WorkbasketType.GROUP, 'Group'],
  [WorkbasketType.CLEARANCE, 'Clearance'],
  [WorkbasketType.TOPIC, 'Topic']
]);
