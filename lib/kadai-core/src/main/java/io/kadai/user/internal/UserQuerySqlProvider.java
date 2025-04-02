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

package io.kadai.user.internal;

import static io.kadai.common.internal.util.SqlProviderUtil.CLOSING_SCRIPT_TAG;
import static io.kadai.common.internal.util.SqlProviderUtil.CLOSING_WHERE_TAG;
import static io.kadai.common.internal.util.SqlProviderUtil.DB2_WITH_UR;
import static io.kadai.common.internal.util.SqlProviderUtil.OPENING_SCRIPT_TAG;
import static io.kadai.common.internal.util.SqlProviderUtil.OPENING_WHERE_TAG;
import static io.kadai.user.api.UserQueryColumnName.ORG_LEVEL_1;
import static io.kadai.user.api.UserQueryColumnName.ORG_LEVEL_2;
import static io.kadai.user.api.UserQueryColumnName.ORG_LEVEL_3;
import static io.kadai.user.api.UserQueryColumnName.ORG_LEVEL_4;
import static io.kadai.user.api.UserQueryColumnName.USER_ID;

import io.kadai.user.api.UserQueryColumnName;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

public class UserQuerySqlProvider {

  private static final String USER_INFO_COLUMNS =
      Arrays.stream(UserQueryColumnName.values())
              .filter(
                  column ->
                      !EnumSet.of(UserQueryColumnName.GROUPS, UserQueryColumnName.PERMISSIONS)
                          .contains(column))
              .map(UserQueryColumnName::toString)
              .collect(Collectors.joining(", "))
          + " ";

  private UserQuerySqlProvider() {}

  @SuppressWarnings("unused")
  public static String queryUsers() {
    return OPENING_SCRIPT_TAG
        + "SELECT "
        + USER_INFO_COLUMNS
        + "FROM USER_INFO "
        + OPENING_WHERE_TAG
        + "<if test='idIn != null'>AND "
        + USER_ID
        + " IN"
        + "(<foreach item='item' collection='idIn' separator=',' >#{item}</foreach>)</if> "
        + "<if test='orgLevel1In != null'>AND "
        + ORG_LEVEL_1
        + " IN"
        + "(<foreach item='item' collection='orgLevel1In' separator=',' >#{item}</foreach>)</if> "
        + "<if test='orgLevel2In != null'>AND "
        + ORG_LEVEL_2
        + " IN"
        + "(<foreach item='item' collection='orgLevel2In' separator=',' >#{item}</foreach>)</if> "
        + "<if test='orgLevel3In != null'>AND "
        + ORG_LEVEL_3
        + " IN"
        + "(<foreach item='item' collection='orgLevel3In' separator=',' >#{item}</foreach>)</if> "
        + "<if test='orgLevel4In != null'>AND "
        + ORG_LEVEL_4
        + " IN"
        + "(<foreach item='item' collection='orgLevel4In' separator=',' >#{item}</foreach>)</if> "
        + CLOSING_WHERE_TAG
        + "<if test='!orderBy.isEmpty()'>ORDER BY "
        + "<foreach item='item' collection='orderBy' separator=',' >${item}</foreach></if> "
        + DB2_WITH_UR
        + CLOSING_SCRIPT_TAG;
  }

  @SuppressWarnings("unused")
  public static String countQueryUsers() {
    return OPENING_SCRIPT_TAG
        + "SELECT COUNT(USER_ID) FROM USER_INFO "
        + OPENING_WHERE_TAG
        + "<if test='idIn != null'>AND USER_ID IN"
        + "(<foreach item='item' collection='idIn' separator=',' >#{item}</foreach>)</if> "
        + "<if test='orgLevel1In != null'>AND "
        + ORG_LEVEL_1
        + " IN"
        + "(<foreach item='item' collection='orgLevel1In' separator=',' >#{item}</foreach>)</if> "
        + "<if test='orgLevel2In != null'>AND "
        + ORG_LEVEL_2
        + " IN"
        + "(<foreach item='item' collection='orgLevel2In' separator=',' >#{item}</foreach>)</if> "
        + "<if test='orgLevel3In != null'>AND "
        + ORG_LEVEL_3
        + " IN"
        + "(<foreach item='item' collection='orgLevel3In' separator=',' >#{item}</foreach>)</if> "
        + "<if test='orgLevel4In != null'>AND "
        + ORG_LEVEL_4
        + " IN"
        + "(<foreach item='item' collection='orgLevel4In' separator=',' >#{item}</foreach>)</if> "
        + CLOSING_WHERE_TAG
        + DB2_WITH_UR
        + CLOSING_SCRIPT_TAG;
  }

  @SuppressWarnings("unsued")
  public static String queryUserColumnValues() {
    return OPENING_SCRIPT_TAG
        + "SELECT DISTINCT ${columnName} FROM USER_INFO "
        + OPENING_WHERE_TAG
        + "<if test='idIn != null'>AND USER_ID IN"
        + "(<foreach item='item' collection='idIn' separator=',' >#{item}</foreach>)</if> "
        + "<if test='orgLevel1In != null'>AND "
        + ORG_LEVEL_1
        + " IN"
        + "(<foreach item='item' collection='orgLevel1In' separator=',' >#{item}</foreach>)</if> "
        + "<if test='orgLevel2In != null'>AND "
        + ORG_LEVEL_2
        + " IN"
        + "(<foreach item='item' collection='orgLevel2In' separator=',' >#{item}</foreach>)</if> "
        + "<if test='orgLevel3In != null'>AND "
        + ORG_LEVEL_3
        + " IN"
        + "(<foreach item='item' collection='orgLevel3In' separator=',' >#{item}</foreach>)</if> "
        + "<if test='orgLevel4In != null'>AND "
        + ORG_LEVEL_4
        + " IN"
        + "(<foreach item='item' collection='orgLevel4In' separator=',' >#{item}</foreach>)</if> "
        + CLOSING_WHERE_TAG
        + "<if test='!orderBy.isEmpty()'>ORDER BY "
        + "<foreach item='item' collection='orderBy' separator=',' >${item}</foreach></if> "
        + DB2_WITH_UR
        + CLOSING_SCRIPT_TAG;
  }
}
