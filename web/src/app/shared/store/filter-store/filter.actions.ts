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

import { WorkbasketQueryFilterParameter } from '../../models/workbasket-query-filter-parameter';
import { TaskQueryFilterParameter } from '../../models/task-query-filter-parameter';

// Workbasket Filter
export class SetWorkbasketFilter {
  static readonly type = '[Workbasket filter] Set workbasket filter parameter';

  constructor(
    public parameters: WorkbasketQueryFilterParameter,
    public component: string
  ) {}
}

export class ClearWorkbasketFilter {
  static readonly type = '[Workbasket filter] Clear workbasket filter parameter';

  constructor(public component: string) {}
}

// Task Filter
export class SetTaskFilter {
  static readonly type = '[Task filter] Set task filter parameter';

  constructor(public parameters: TaskQueryFilterParameter) {}
}

export class ClearTaskFilter {
  static readonly type = '[Task filter] Clear task filter parameter';
}
