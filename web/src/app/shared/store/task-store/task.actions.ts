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

import { Task } from '../../../workplace/models/task';
import { SearchType } from '../../../workplace/models/search';
import { Workbasket } from '../../models/workbasket';
import { Sorting, TaskQuerySortParameter } from '../../models/sorting';

// Task list
export class LoadTasks {
  static readonly type = '[Task list] Load tasks for the current search context';
}

export class SelectWorkbasket {
  static readonly type = '[Task list] Select the workbasket to search tasks in';

  constructor(public workbasket: Workbasket | undefined) {}
}

export class SetSearchType {
  static readonly type = '[Task list] Set search type';

  constructor(public searchType: SearchType) {}
}

export class SetSort {
  static readonly type = '[Task list] Set sort order';

  constructor(public sort: Sorting<TaskQuerySortParameter>) {}
}

export class SetPage {
  static readonly type = '[Task list] Set page';

  constructor(public page: number) {}
}

export class SetPageSize {
  static readonly type = '[Task list] Set page size';

  constructor(public pageSize: number) {}
}

// Task selection and CRUD
export class SelectTask {
  static readonly type = '[Task] Select a task';

  constructor(public task: Task | undefined) {}
}

export class GetTask {
  static readonly type = '[Task] Get a task';

  constructor(public taskId: string) {}
}

export class CreateTask {
  static readonly type = '[Task] Create a new task';

  constructor(public task: Task) {}
}

export class UpdateTask {
  static readonly type = '[Task] Update a task';

  constructor(public task: Task) {}
}

export class DeleteTask {
  static readonly type = '[Task] Delete a task';

  constructor(public task: Task) {}
}
