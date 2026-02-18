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

export class QueryParameters {
  SORTBY: string;
  SORTDIRECTION: string;
  // Filtering
  NAME: string;
  NAMELIKE: string;
  DESCLIKE: string;
  OWNER: string;
  OWNERLIKE: string;
  TYPE: string;
  KEY: string;
  WORKBASKET_KEY: string;
  KEYLIKE: string;
  PRIORITY: string;
  STATE: string;
  WORKBASKET_ID: string;
  TASK_PRIMARY_OBJ_REF_TYPE_LIKE: string;
  TASK_PRIMARY_OBJ_REF_VALUE_LIKE: string;
  // Access
  REQUIREDPERMISSION: string;
  ACCESSIDS: string;
  ACCESSIDLIKE: string;
  WORKBASKETKEYLIKE: string;
  // Pagination
  PAGE: string;
  PAGESIZE: string;
  // Domain
  DOMAIN: string;
  // Task history events
  TASK_ID_LIKE: string;
  PARENT_BUSINESS_PROCESS_ID_LIKE: string;
  BUSINESS_PROCESS_ID_LIKE: string;
  EVENT_TYPE_LIKE: string;
  CREATED: string;
  USER_ID_LIKE: string;
  POR_COMPANY_LIKE: string;
  POR_SYSTEM_LIKE: string;
  POR_INSTANCE_LIKE: string;
  POR_TYPE_LIKE: string;
  POR_VALUE_LIKE: string;
  TASK_CLASSIFICATION_KEY_LIKE: string;
  TASK_CLASSIFICATION_CATEGORY_LIKE: string;
  ATTACHMENT_CLASSIFICATION_KEY_LIKE: string;
  CUSTOM_1_LIKE: string;
  CUSTOM_2_LIKE: string;
  CUSTOM_3_LIKE: string;
  CUSTOM_4_LIKE: string;
  COMMENT_LIKE: string;
}
