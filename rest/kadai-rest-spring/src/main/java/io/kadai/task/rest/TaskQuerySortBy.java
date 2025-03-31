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

package io.kadai.task.rest;

import io.kadai.common.api.BaseQuery.SortDirection;
import io.kadai.common.rest.QuerySortBy;
import io.kadai.task.api.TaskCustomField;
import io.kadai.task.api.TaskQuery;
import java.util.function.BiConsumer;

public enum TaskQuerySortBy implements QuerySortBy<TaskQuery> {
  CLASSIFICATION_KEY(TaskQuery::orderByClassificationKey),
  CLASSIFICATION_NAME(TaskQuery::orderByClassificationName),
  POR_TYPE(TaskQuery::orderByPrimaryObjectReferenceType),
  POR_VALUE(TaskQuery::orderByPrimaryObjectReferenceValue),
  POR_COMPANY(TaskQuery::orderByPrimaryObjectReferenceCompany),
  POR_SYSTEM(TaskQuery::orderByPrimaryObjectReferenceSystem),
  POR_SYSTEM_INSTANCE(TaskQuery::orderByPrimaryObjectReferenceSystemInstance),
  STATE(TaskQuery::orderByState),
  NAME(TaskQuery::orderByName),
  DUE(TaskQuery::orderByDue),
  PLANNED(TaskQuery::orderByPlanned),
  RECEIVED(TaskQuery::orderByReceived),
  PRIORITY(TaskQuery::orderByPriority),
  CREATED(TaskQuery::orderByCreated),
  CLAIMED(TaskQuery::orderByClaimed),
  DOMAIN(TaskQuery::orderByDomain),
  TASK_ID(TaskQuery::orderByTaskId),
  MODIFIED(TaskQuery::orderByModified),
  CREATOR(TaskQuery::orderByCreator),
  NOTE(TaskQuery::orderByNote),
  OWNER(TaskQuery::orderByOwner),
  OWNER_LONG_NAME(TaskQuery::orderByOwnerLongName),
  BUSINESS_PROCESS_ID(TaskQuery::orderByBusinessProcessId),
  PARENT_BUSINESS_PROCESS_ID(TaskQuery::orderByParentBusinessProcessId),
  WORKBASKET_KEY(TaskQuery::orderByWorkbasketKey),
  CUSTOM_1((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_1, sort)),
  CUSTOM_2((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_2, sort)),
  CUSTOM_3((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_3, sort)),
  CUSTOM_4((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_4, sort)),
  CUSTOM_5((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_5, sort)),
  CUSTOM_6((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_6, sort)),
  CUSTOM_7((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_7, sort)),
  CUSTOM_8((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_8, sort)),
  CUSTOM_9((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_9, sort)),
  CUSTOM_10((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_10, sort)),
  CUSTOM_11((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_11, sort)),
  CUSTOM_12((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_12, sort)),
  CUSTOM_13((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_13, sort)),
  CUSTOM_14((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_14, sort)),
  CUSTOM_15((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_15, sort)),
  CUSTOM_16((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_16, sort)),
  WORKBASKET_ID(TaskQuery::orderByWorkbasketId),
  WORKBASKET_NAME(TaskQuery::orderByWorkbasketName),
  ATTACHMENT_CLASSIFICATION_KEY(TaskQuery::orderByAttachmentClassificationKey),
  ATTACHMENT_CLASSIFICATION_NAME(TaskQuery::orderByAttachmentClassificationName),
  ATTACHMENT_CLASSIFICATION_ID(TaskQuery::orderByAttachmentClassificationId),
  ATTACHMENT_CHANNEL(TaskQuery::orderByAttachmentChannel),
  ATTACHMENT_REFERENCE(TaskQuery::orderByAttachmentReference),
  ATTACHMENT_RECEIVED(TaskQuery::orderByAttachmentReceived),
  COMPLETED(TaskQuery::orderByCompleted);

  private final BiConsumer<TaskQuery, SortDirection> consumer;

  TaskQuerySortBy(BiConsumer<TaskQuery, SortDirection> consumer) {
    this.consumer = consumer;
  }

  @Override
  public void applySortByForQuery(TaskQuery query, SortDirection sortDirection) {
    consumer.accept(query, sortDirection);
  }
}
