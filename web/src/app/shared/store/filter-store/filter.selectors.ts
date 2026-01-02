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

import { FilterState, FilterStateModel } from './filter.state';
import { Selector } from '@ngxs/store';
import { WorkbasketQueryFilterParameter } from '../../models/workbasket-query-filter-parameter';
import { TaskQueryFilterParameter } from '../../models/task-query-filter-parameter';

export class FilterSelectors {
  @Selector([FilterState])
  static getAvailableDistributionTargetsFilter(state: FilterStateModel): WorkbasketQueryFilterParameter {
    return state?.availableDistributionTargets;
  }

  @Selector([FilterState])
  static getSelectedDistributionTargetsFilter(state: FilterStateModel): WorkbasketQueryFilterParameter {
    return state.selectedDistributionTargets;
  }

  @Selector([FilterState])
  static getWorkbasketListFilter(state: FilterStateModel): WorkbasketQueryFilterParameter {
    return state?.workbasketList;
  }

  @Selector([FilterState])
  static getTaskFilter(state: FilterStateModel): TaskQueryFilterParameter {
    return state.tasks;
  }
}
