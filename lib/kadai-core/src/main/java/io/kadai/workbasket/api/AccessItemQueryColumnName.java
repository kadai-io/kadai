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

package io.kadai.workbasket.api;

import io.kadai.common.api.QueryColumnName;

/**
 * Enum containing the column names for {@link
 * io.kadai.workbasket.internal.WorkbasketQueryMapper#queryWorkbasketAccessItemColumnValues}.
 */
public enum AccessItemQueryColumnName implements QueryColumnName {
  ID("id"),
  WORKBASKET_ID("workbasket_id"),
  WORKBASKET_KEY("wb.key"),
  ACCESS_ID("access_id");

  private String name;

  AccessItemQueryColumnName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
