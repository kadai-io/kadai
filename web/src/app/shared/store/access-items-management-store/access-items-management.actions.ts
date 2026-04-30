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

import { AccessId } from '../../models/access-id';
import { Sorting, WorkbasketAccessItemQuerySortParameter } from '../../models/sorting';
import { WorkbasketAccessItemQueryFilterParameter } from '../../models/workbasket-access-item-query-filter-parameter';
import { QueryPagingParameter } from '../../models/query-paging-parameter';

export class SelectAccessId {
  static readonly type = '[Access Items Management] Select access ID';

  constructor(public accessIdDefinition: AccessId) {}
}

export class GetGroupsByAccessId {
  static readonly type = '[Access Items Management] Get groups by access ID';

  constructor(public accessId: string) {}
}

export class GetPermissionsByAccessId {
  static readonly type = '[Access Items Management] Get permissions by access ID';

  constructor(public accessId: string) {}
}

export class GetAccessItems {
  static readonly type = '[Access Items Management] Get access items';

  constructor(
    public filterParameter?: WorkbasketAccessItemQueryFilterParameter,
    public sortParameter?: Sorting<WorkbasketAccessItemQuerySortParameter>,
    public pagingParameter?: QueryPagingParameter
  ) {}
}

export class RemoveAccessItemsPermissions {
  static readonly type = "[Access Items Management] Remove access items' permissions";

  constructor(public accessId: string) {}
}
