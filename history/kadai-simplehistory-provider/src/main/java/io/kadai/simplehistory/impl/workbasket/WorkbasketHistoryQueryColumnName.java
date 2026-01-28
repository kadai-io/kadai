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

package io.kadai.simplehistory.impl.workbasket;

import io.kadai.common.api.QueryColumnName;

/** Enum containing the column names for {@link WorkbasketHistoryQueryMapper}. */
public enum WorkbasketHistoryQueryColumnName implements QueryColumnName {
  ID("id"),
  WORKBASKET_ID("workbasket_id"),
  EVENT_TYPE("event_type"),
  CREATED("created"),
  USER_ID("user_id"),
  PROXY_ACCESS_ID("proxy_access_id"),
  DOMAIN("domain"),
  KEY("key"),
  TYPE("type"),
  OWNER("owner"),
  CUSTOM_1("custom_1"),
  CUSTOM_2("custom_2"),
  CUSTOM_3("custom_3"),
  CUSTOM_4("custom_4"),
  ORG_LEVEL_1("org_level_1"),
  ORG_LEVEL_2("org_level_2"),
  ORG_LEVEL_3("org_level_3"),
  ORG_LEVEL_4("org_level_4");

  private final String name;

  WorkbasketHistoryQueryColumnName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
