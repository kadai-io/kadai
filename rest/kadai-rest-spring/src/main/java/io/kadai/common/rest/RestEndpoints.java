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

package io.kadai.common.rest;

/** Collection of Url to Controller mappings. */
public final class RestEndpoints {

  public static final String API_V1 = "/api/v1/";

  // configuration endpoints
  public static final String URL_VERSION = API_V1 + "version";
  public static final String URL_DOMAIN = API_V1 + "domains";
  public static final String URL_CURRENT_USER = API_V1 + "current-user-info";
  public static final String URL_CLASSIFICATION_CATEGORIES = API_V1 + "classification-categories";
  public static final String URL_CLASSIFICATION_TYPES = API_V1 + "classification-types";
  public static final String URL_CLASSIFICATION_CATEGORIES_BY_TYPES =
      API_V1 + "classifications-by-type";
  public static final String URL_HISTORY_ENABLED = API_V1 + "history-provider-enabled";
  public static final String URL_CUSTOM_ATTRIBUTES = API_V1 + "config/custom-attributes";

  // access id endpoints
  public static final String URL_ACCESS_ID = API_V1 + "access-ids";
  public static final String URL_ACCESS_ID_WITH_NAME = URL_ACCESS_ID + "/with-name";
  public static final String URL_ACCESS_ID_GROUPS = URL_ACCESS_ID + "/groups";
  public static final String URL_ACCESS_ID_PERMISSIONS = URL_ACCESS_ID + "/permissions";

  // import / export endpoints
  public static final String URL_CLASSIFICATION_DEFINITIONS = API_V1 + "classification-definitions";
  public static final String URL_WORKBASKET_DEFINITIONS = API_V1 + "workbasket-definitions";

  // classification endpoints
  public static final String URL_CLASSIFICATIONS = API_V1 + "classifications";
  public static final String URL_CLASSIFICATIONS_ID = API_V1 + "classifications/{classificationId}";

  // workbasket endpoints
  public static final String URL_WORKBASKET = API_V1 + "workbaskets";
  public static final String URL_WORKBASKET_ID = API_V1 + "workbaskets/{workbasketId}";
  public static final String URL_WORKBASKET_ID_ACCESS_ITEMS =
      API_V1 + "workbaskets/{workbasketId}/workbasketAccessItems";
  public static final String URL_WORKBASKET_ID_DISTRIBUTION =
      API_V1 + "workbaskets/{workbasketId}/distribution-targets";

  // access item endpoints
  public static final String URL_WORKBASKET_ACCESS_ITEMS = API_V1 + "workbasket-access-items";

  // task endpoints
  public static final String URL_TASKS = API_V1 + "tasks";
  public static final String URL_TASKS_ID = API_V1 + "tasks/{taskId}";
  public static final String URL_TASKS_ID_FORCE = API_V1 + "tasks/{taskId}/force";
  public static final String URL_TASKS_ID_CLAIM = API_V1 + "tasks/{taskId}/claim";
  public static final String URL_TASKS_ID_CLAIM_FORCE = API_V1 + "tasks/{taskId}/claim/force";
  public static final String URL_TASKS_ID_SELECT_AND_CLAIM = API_V1 + "tasks/select-and-claim";
  public static final String URL_TASKS_ID_REQUEST_REVIEW = API_V1 + "tasks/{taskId}/request-review";
  public static final String URL_TASKS_ID_REQUEST_REVIEW_FORCE =
      API_V1 + "tasks/{taskId}/request-review/force";
  public static final String URL_TASKS_ID_REQUEST_CHANGES =
      API_V1 + "tasks/{taskId}/request-changes";
  public static final String URL_TASKS_ID_REQUEST_CHANGES_FORCE =
      API_V1 + "tasks/{taskId}/request-changes/force";
  public static final String URL_TASKS_ID_COMPLETE = API_V1 + "tasks/{taskId}/complete";
  public static final String URL_TASKS_ID_COMPLETE_FORCE = API_V1 + "tasks/{taskId}/complete/force";
  public static final String URL_TASKS_ID_CANCEL = API_V1 + "tasks/{taskId}/cancel";
  public static final String URL_TASKS_ID_TERMINATE = API_V1 + "tasks/{taskId}/terminate";
  public static final String URL_TASKS_ID_REOPEN = API_V1 + "tasks/{taskId}/reopen";
  public static final String URL_TASKS_ID_TRANSFER_WORKBASKET_ID =
      API_V1 + "tasks/{taskId}/transfer/{workbasketId}";
  public static final String URL_TRANSFER_WORKBASKET_ID = API_V1 + "tasks/transfer/{workbasketId}";
  public static final String URL_TASKS_ID_SET_READ = API_V1 + "tasks/{taskId}/set-read";
  public static final String URL_DISTRIBUTE =
      API_V1 + "tasks/distribute/{workbasketId}";

  // task comment endpoints
  public static final String URL_TASK_COMMENTS = API_V1 + "tasks/{taskId}/comments";
  public static final String URL_TASK_COMMENT = API_V1 + "tasks/comments/{taskCommentId}";

  // monitor endpoints
  public static final String URL_MONITOR_WORKBASKET_REPORT = API_V1 + "monitor/workbasket-report";
  public static final String URL_MONITOR_WORKBASKET_PRIORITY_REPORT =
      API_V1 + "monitor/workbasket-priority-report";
  public static final String URL_MONITOR_CLASSIFICATION_CATEGORY_REPORT =
      API_V1 + "monitor/classification-category-report";
  public static final String URL_MONITOR_CLASSIFICATION_REPORT =
      API_V1 + "monitor/classification-report";
  public static final String URL_MONITOR_DETAILED_CLASSIFICATION_REPORT =
      API_V1 + "monitor/detailed-classification-report";
  public static final String URL_MONITOR_TASK_CUSTOM_FIELD_VALUE_REPORT =
      API_V1 + "monitor/task-custom-field-value-report";
  public static final String URL_MONITOR_TASK_STATUS_REPORT = API_V1 + "monitor/task-status-report";
  public static final String URL_MONITOR_TIMESTAMP_REPORT = API_V1 + "monitor/timestamp-report";

  // user endpoints
  public static final String URL_USERS = API_V1 + "users";
  public static final String URL_USERS_ID = API_V1 + "users/{userId}";

  private RestEndpoints() {}
}
