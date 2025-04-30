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

package io.kadai.workbasket.api;

import io.kadai.common.api.QueryColumnName;

/**
 * Enum containing the column names for
 * io.kadai.workbasket.internal.WorkbasketQueryMapper.queryWorkbasketColumnValues.
 */
public enum WorkbasketQueryColumnName implements QueryColumnName {
  OWNER("w.owner"),
  ID("w.id"),
  KEY("w.key"),
  NAME("w.name"),
  DOMAIN("w.domain"),
  TYPE("w.type"),
  CUSTOM_1("w.custom_1"),
  CUSTOM_2("w.custom_2"),
  CUSTOM_3("w.custom_3"),
  CUSTOM_4("w.custom_4"),
  ORG_LEVEL_1("w.org_level_1"),
  ORG_LEVEL_2("w.org_level_2"),
  ORG_LEVEL_3("w.org_level_3"),
  ORG_LEVEL_4("w.org_level_4");

  private final String name;

  WorkbasketQueryColumnName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
