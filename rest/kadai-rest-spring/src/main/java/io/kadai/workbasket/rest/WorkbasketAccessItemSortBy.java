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

package io.kadai.workbasket.rest;

import io.kadai.common.api.BaseQuery.SortDirection;
import io.kadai.common.rest.QuerySortBy;
import io.kadai.workbasket.api.WorkbasketAccessItemQuery;
import java.util.function.BiConsumer;

public enum WorkbasketAccessItemSortBy implements QuerySortBy<WorkbasketAccessItemQuery> {
  WORKBASKET_KEY(WorkbasketAccessItemQuery::orderByWorkbasketKey),
  ACCESS_ID(WorkbasketAccessItemQuery::orderByAccessId);

  private final BiConsumer<WorkbasketAccessItemQuery, SortDirection> consumer;

  WorkbasketAccessItemSortBy(BiConsumer<WorkbasketAccessItemQuery, SortDirection> consumer) {
    this.consumer = consumer;
  }

  @Override
  public void applySortByForQuery(WorkbasketAccessItemQuery query, SortDirection sortDirection) {
    consumer.accept(query, sortDirection);
  }
}
