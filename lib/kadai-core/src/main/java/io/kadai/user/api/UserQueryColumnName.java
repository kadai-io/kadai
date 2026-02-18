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

package io.kadai.user.api;

import io.kadai.common.api.QueryColumnName;
import io.kadai.user.internal.UserQueryImpl;

/**
 * Enum containing the column names for {@linkplain
 * io.kadai.user.internal.UserQueryMapper#queryUserColumnValues(UserQueryImpl)
 * UserQueryMapper#queryUserColumnValues}.
 */
public enum UserQueryColumnName implements QueryColumnName {
  USER_ID("user_id"),
  GROUPS("groups"),
  PERMISSIONS("permissions"),
  FIRST_NAME("first_name"),
  LAST_NAME("last_name"),
  FULL_NAME("full_name"),
  LONG_NAME("long_name"),
  E_MAIL("e_mail"),
  PHONE("phone"),
  MOBILE_PHONE("mobile_phone"),
  ORG_LEVEL_4("org_level_4"),
  ORG_LEVEL_3("org_level_3"),
  ORG_LEVEL_2("org_level_2"),
  ORG_LEVEL_1("org_level_1"),
  DATA("data");

  private final String name;

  UserQueryColumnName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
