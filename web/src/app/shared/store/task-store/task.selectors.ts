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

import { Selector } from '@ngxs/store';
import { TaskWorkflowState, TaskWorkflowStateModel } from './task.state';
import { Task } from '../../../workplace/models/task';
import { Workbasket } from '../../models/workbasket';
import { Page } from '../../models/page';
import { SearchType } from '../../../workplace/models/search';
import { Sorting, TaskQuerySortParameter } from '../../models/sorting';
import { QueryPagingParameter } from '../../models/query-paging-parameter';

export class TaskSelectors {
  @Selector([TaskWorkflowState])
  static getTasks(state: TaskWorkflowStateModel): Task[] {
    return state.tasks;
  }

  @Selector([TaskWorkflowState])
  static getPage(state: TaskWorkflowStateModel): Page {
    return state.page;
  }

  @Selector([TaskWorkflowState])
  static getSelectedTask(state: TaskWorkflowStateModel): Task | undefined {
    return state.selectedTask;
  }

  @Selector([TaskWorkflowState])
  static getSelectedWorkbasket(state: TaskWorkflowStateModel): Workbasket | undefined {
    return state.selectedWorkbasket;
  }

  @Selector([TaskWorkflowState])
  static getSearchType(state: TaskWorkflowStateModel): SearchType {
    return state.selectedSearchType;
  }

  @Selector([TaskWorkflowState])
  static getSort(state: TaskWorkflowStateModel): Sorting<TaskQuerySortParameter> {
    return state.sort;
  }

  @Selector([TaskWorkflowState])
  static getPaging(state: TaskWorkflowStateModel): QueryPagingParameter {
    return state.paging;
  }
}
