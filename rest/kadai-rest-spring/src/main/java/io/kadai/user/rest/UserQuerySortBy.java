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

package io.kadai.user.rest;

import io.kadai.common.api.BaseQuery.SortDirection;
import io.kadai.common.rest.QuerySortBy;
import io.kadai.user.api.UserQuery;
import java.util.function.BiConsumer;

public enum UserQuerySortBy implements QuerySortBy<UserQuery> {
  FIRST_NAME(UserQuery::orderByFirstName),
  LAST_NAME(UserQuery::orderByLastName),
  ORG_LEVEL_1(UserQuery::orderByOrgLevel1),
  ORG_LEVEL_2(UserQuery::orderByOrgLevel2),
  ORG_LEVEL_3(UserQuery::orderByOrgLevel3),
  ORG_LEVEL_4(UserQuery::orderByOrgLevel4);

  private final BiConsumer<UserQuery, SortDirection> consumer;

  UserQuerySortBy(BiConsumer<UserQuery, SortDirection> consumer) {
    this.consumer = consumer;
  }

  @Override
  public void applySortByForQuery(UserQuery query, SortDirection sortDirection) {
    consumer.accept(query, sortDirection);
  }
}
