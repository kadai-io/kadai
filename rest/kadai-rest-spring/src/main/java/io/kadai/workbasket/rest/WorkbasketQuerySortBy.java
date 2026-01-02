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
import io.kadai.workbasket.api.WorkbasketCustomField;
import io.kadai.workbasket.api.WorkbasketQuery;
import java.util.function.BiConsumer;

public enum WorkbasketQuerySortBy implements QuerySortBy<WorkbasketQuery> {
  NAME(WorkbasketQuery::orderByName),
  KEY(WorkbasketQuery::orderByKey),
  OWNER(WorkbasketQuery::orderByOwner),
  TYPE(WorkbasketQuery::orderByType),
  DESCRIPTION(WorkbasketQuery::orderByDescription),
  CUSTOM_1((query, sort) -> query.orderByCustomAttribute(WorkbasketCustomField.CUSTOM_1, sort)),
  CUSTOM_2((query, sort) -> query.orderByCustomAttribute(WorkbasketCustomField.CUSTOM_2, sort)),
  CUSTOM_3((query, sort) -> query.orderByCustomAttribute(WorkbasketCustomField.CUSTOM_3, sort)),
  CUSTOM_4((query, sort) -> query.orderByCustomAttribute(WorkbasketCustomField.CUSTOM_4, sort)),
  CUSTOM_5((query, sort) -> query.orderByCustomAttribute(WorkbasketCustomField.CUSTOM_5, sort)),
  CUSTOM_6((query, sort) -> query.orderByCustomAttribute(WorkbasketCustomField.CUSTOM_6, sort)),
  CUSTOM_7((query, sort) -> query.orderByCustomAttribute(WorkbasketCustomField.CUSTOM_7, sort)),
  CUSTOM_8((query, sort) -> query.orderByCustomAttribute(WorkbasketCustomField.CUSTOM_8, sort)),
  DOMAIN(WorkbasketQuery::orderByDomain),
  ORG_LEVEL_1(WorkbasketQuery::orderByOrgLevel1),
  ORG_LEVEL_2(WorkbasketQuery::orderByOrgLevel2),
  ORG_LEVEL_3(WorkbasketQuery::orderByOrgLevel3),
  ORG_LEVEL_4(WorkbasketQuery::orderByOrgLevel4);

  private final BiConsumer<WorkbasketQuery, SortDirection> consumer;

  WorkbasketQuerySortBy(BiConsumer<WorkbasketQuery, SortDirection> consumer) {
    this.consumer = consumer;
  }

  @Override
  public void applySortByForQuery(WorkbasketQuery query, SortDirection sortDirection) {
    consumer.accept(query, sortDirection);
  }
}
