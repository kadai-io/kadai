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

package io.kadai.simplehistory.rest;

import io.kadai.common.api.BaseQuery.SortDirection;
import io.kadai.common.rest.QuerySortBy;
import io.kadai.simplehistory.impl.task.TaskHistoryQuery;
import io.kadai.spi.history.api.events.task.TaskHistoryCustomField;
import java.util.function.BiConsumer;

public enum TaskHistoryQuerySortBy implements QuerySortBy<TaskHistoryQuery> {
  TASK_HISTORY_EVENT_ID(TaskHistoryQuery::orderByTaskHistoryEventId),
  BUSINESS_PROCESS_ID(TaskHistoryQuery::orderByBusinessProcessId),
  PARENT_BUSINESS_PROCESS_ID(TaskHistoryQuery::orderByParentBusinessProcessId),
  TASK_ID(TaskHistoryQuery::orderByTaskId),
  EVENT_TYPE(TaskHistoryQuery::orderByEventType),
  CREATED(TaskHistoryQuery::orderByCreated),
  USER_ID(TaskHistoryQuery::orderByUserId),
  DOMAIN(TaskHistoryQuery::orderByDomain),
  WORKBASKET_KEY(TaskHistoryQuery::orderByWorkbasketKey),
  POR_COMPANY(TaskHistoryQuery::orderByPorCompany),
  POR_SYSTEM(TaskHistoryQuery::orderByPorSystem),
  POR_INSTANCE(TaskHistoryQuery::orderByPorInstance),
  POR_TYPE(TaskHistoryQuery::orderByPorType),
  POR_VALUE(TaskHistoryQuery::orderByPorValue),
  TASK_CLASSIFICATION_KEY(TaskHistoryQuery::orderByTaskClassificationKey),
  TASK_CLASSIFICATION_CATEGORY(TaskHistoryQuery::orderByTaskClassificationCategory),
  ATTACHMENT_CLASSIFICATION_KEY(TaskHistoryQuery::orderByAttachmentClassificationKey),
  CUSTOM_1((query, sort) -> query.orderByCustomAttribute(TaskHistoryCustomField.CUSTOM_1, sort)),
  CUSTOM_2((query, sort) -> query.orderByCustomAttribute(TaskHistoryCustomField.CUSTOM_2, sort)),
  CUSTOM_3((query, sort) -> query.orderByCustomAttribute(TaskHistoryCustomField.CUSTOM_3, sort)),
  CUSTOM_4((query, sort) -> query.orderByCustomAttribute(TaskHistoryCustomField.CUSTOM_4, sort)),
  OLD_VALUE(TaskHistoryQuery::orderByOldValue);

  private final BiConsumer<TaskHistoryQuery, SortDirection> consumer;

  TaskHistoryQuerySortBy(BiConsumer<TaskHistoryQuery, SortDirection> consumer) {
    this.consumer = consumer;
  }

  @Override
  public void applySortByForQuery(TaskHistoryQuery query, SortDirection sortDirection) {
    consumer.accept(query, sortDirection);
  }
}
