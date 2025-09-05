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

package io.kadai.classification.rest;

import io.kadai.classification.api.ClassificationCustomField;
import io.kadai.classification.api.ClassificationQuery;
import io.kadai.common.api.BaseQuery.SortDirection;
import io.kadai.common.rest.QuerySortBy;
import java.util.function.BiConsumer;

public enum ClassificationQuerySortBy implements QuerySortBy<ClassificationQuery> {
  APPLICATION_ENTRY_POINT(ClassificationQuery::orderByApplicationEntryPoint),
  DOMAIN(ClassificationQuery::orderByDomain),
  KEY(ClassificationQuery::orderByKey),
  CATEGORY(ClassificationQuery::orderByCategory),
  CUSTOM_1((q, sort) -> q.orderByCustomAttribute(ClassificationCustomField.CUSTOM_1, sort)),
  CUSTOM_2((q, sort) -> q.orderByCustomAttribute(ClassificationCustomField.CUSTOM_2, sort)),
  CUSTOM_3((q, sort) -> q.orderByCustomAttribute(ClassificationCustomField.CUSTOM_3, sort)),
  CUSTOM_4((q, sort) -> q.orderByCustomAttribute(ClassificationCustomField.CUSTOM_4, sort)),
  CUSTOM_5((q, sort) -> q.orderByCustomAttribute(ClassificationCustomField.CUSTOM_5, sort)),
  CUSTOM_6((q, sort) -> q.orderByCustomAttribute(ClassificationCustomField.CUSTOM_6, sort)),
  CUSTOM_7((q, sort) -> q.orderByCustomAttribute(ClassificationCustomField.CUSTOM_7, sort)),
  CUSTOM_8((q, sort) -> q.orderByCustomAttribute(ClassificationCustomField.CUSTOM_8, sort)),
  NAME(ClassificationQuery::orderByName),
  PARENT_ID(ClassificationQuery::orderByParentId),
  PARENT_KEY(ClassificationQuery::orderByParentKey),
  PRIORITY(ClassificationQuery::orderByPriority),
  SERVICE_LEVEL(ClassificationQuery::orderByServiceLevel);

  private final BiConsumer<ClassificationQuery, SortDirection> consumer;

  ClassificationQuerySortBy(BiConsumer<ClassificationQuery, SortDirection> consumer) {
    this.consumer = consumer;
  }

  @Override
  public void applySortByForQuery(ClassificationQuery query, SortDirection sortDirection) {
    consumer.accept(query, sortDirection);
  }
}
