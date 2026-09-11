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

package io.kadai.user.internal;

import static io.kadai.common.internal.util.SqlProviderUtil.CLOSING_SCRIPT_TAG;
import static io.kadai.common.internal.util.SqlProviderUtil.DB2_WITH_UR;
import static io.kadai.common.internal.util.SqlProviderUtil.OPENING_SCRIPT_TAG;

@SuppressWarnings("unused")
public class UserMapperSqlProvider {

  private static final String USER_INFO_COLUMNS =
      "USER_ID, FIRST_NAME, LAST_NAME, FULL_NAME, LONG_NAME, E_MAIL, PHONE, MOBILE_PHONE, "
          + "ORG_LEVEL_4, ORG_LEVEL_3, ORG_LEVEL_2, ORG_LEVEL_1, DATA ";
  private static final String USER_INFO_VALUES =
      "#{id}, #{firstName}, #{lastName}, #{fullName}, #{longName}, #{email}, #{phone}, "
          + "#{mobilePhone}, #{orgLevel4}, #{orgLevel3}, #{orgLevel2}, #{orgLevel1}, #{data} ";
  private static final String IDS_FOREACH =
      "<foreach item='id' collection='ids' separator=','>#{id}</foreach>";
  private static final String DB2_PERMISSION_AGGREGATES =
      "MAX(a.PERM_READ) AS MAX_READ, "
          + "MAX(a.PERM_READTASKS) AS MAX_READTASKS, "
          + "MAX(a.PERM_EDITTASKS) AS MAX_EDITTASKS, "
          + "MAX(a.PERM_OPEN) AS MAX_OPEN, "
          + "MAX(a.PERM_APPEND) AS MAX_APPEND, "
          + "MAX(a.PERM_TRANSFER) AS MAX_TRANSFER, "
          + "MAX(a.PERM_DISTRIBUTE) AS MAX_DISTRIBUTE, "
          + "MAX(a.PERM_CUSTOM_1) AS MAX_CUSTOM_1, "
          + "MAX(a.PERM_CUSTOM_2) AS MAX_CUSTOM_2, "
          + "MAX(a.PERM_CUSTOM_3) AS MAX_CUSTOM_3, "
          + "MAX(a.PERM_CUSTOM_4) AS MAX_CUSTOM_4, "
          + "MAX(a.PERM_CUSTOM_5) AS MAX_CUSTOM_5, "
          + "MAX(a.PERM_CUSTOM_6) AS MAX_CUSTOM_6, "
          + "MAX(a.PERM_CUSTOM_7) AS MAX_CUSTOM_7, "
          + "MAX(a.PERM_CUSTOM_8) AS MAX_CUSTOM_8, "
          + "MAX(a.PERM_CUSTOM_9) AS MAX_CUSTOM_9, "
          + "MAX(a.PERM_CUSTOM_10) AS MAX_CUSTOM_10, "
          + "MAX(a.PERM_CUSTOM_11) AS MAX_CUSTOM_11, "
          + "MAX(a.PERM_CUSTOM_12) AS MAX_CUSTOM_12 ";
  private static final String BOOLEAN_PERMISSION_AGGREGATES =
      "MAX(a.PERM_READ::int) AS MAX_READ, "
          + "MAX(a.PERM_READTASKS::int) AS MAX_READTASKS, "
          + "MAX(a.PERM_EDITTASKS::int) AS MAX_EDITTASKS, "
          + "MAX(a.PERM_OPEN::int) AS MAX_OPEN, "
          + "MAX(a.PERM_APPEND::int) AS MAX_APPEND, "
          + "MAX(a.PERM_TRANSFER::int) AS MAX_TRANSFER, "
          + "MAX(a.PERM_DISTRIBUTE::int) AS MAX_DISTRIBUTE, "
          + "MAX(a.PERM_CUSTOM_1::int) AS MAX_CUSTOM_1, "
          + "MAX(a.PERM_CUSTOM_2::int) AS MAX_CUSTOM_2, "
          + "MAX(a.PERM_CUSTOM_3::int) AS MAX_CUSTOM_3, "
          + "MAX(a.PERM_CUSTOM_4::int) AS MAX_CUSTOM_4, "
          + "MAX(a.PERM_CUSTOM_5::int) AS MAX_CUSTOM_5, "
          + "MAX(a.PERM_CUSTOM_6::int) AS MAX_CUSTOM_6, "
          + "MAX(a.PERM_CUSTOM_7::int) AS MAX_CUSTOM_7, "
          + "MAX(a.PERM_CUSTOM_8::int) AS MAX_CUSTOM_8, "
          + "MAX(a.PERM_CUSTOM_9::int) AS MAX_CUSTOM_9, "
          + "MAX(a.PERM_CUSTOM_10::int) AS MAX_CUSTOM_10, "
          + "MAX(a.PERM_CUSTOM_11::int) AS MAX_CUSTOM_11, "
          + "MAX(a.PERM_CUSTOM_12::int) AS MAX_CUSTOM_12 ";
  private static final String PERMISSION_ALIAS =
      "<choose>"
          + "<when test=\"permission.name() == 'READ'\">awb.MAX_READ</when>"
          + "<when test=\"permission.name() == 'READTASKS'\">awb.MAX_READTASKS</when>"
          + "<when test=\"permission.name() == 'EDITTASKS'\">awb.MAX_EDITTASKS</when>"
          + "<when test=\"permission.name() == 'OPEN'\">awb.MAX_OPEN</when>"
          + "<when test=\"permission.name() == 'APPEND'\">awb.MAX_APPEND</when>"
          + "<when test=\"permission.name() == 'TRANSFER'\">awb.MAX_TRANSFER</when>"
          + "<when test=\"permission.name() == 'DISTRIBUTE'\">awb.MAX_DISTRIBUTE</when>"
          + "<when test=\"permission.name() == 'CUSTOM_1'\">awb.MAX_CUSTOM_1</when>"
          + "<when test=\"permission.name() == 'CUSTOM_2'\">awb.MAX_CUSTOM_2</when>"
          + "<when test=\"permission.name() == 'CUSTOM_3'\">awb.MAX_CUSTOM_3</when>"
          + "<when test=\"permission.name() == 'CUSTOM_4'\">awb.MAX_CUSTOM_4</when>"
          + "<when test=\"permission.name() == 'CUSTOM_5'\">awb.MAX_CUSTOM_5</when>"
          + "<when test=\"permission.name() == 'CUSTOM_6'\">awb.MAX_CUSTOM_6</when>"
          + "<when test=\"permission.name() == 'CUSTOM_7'\">awb.MAX_CUSTOM_7</when>"
          + "<when test=\"permission.name() == 'CUSTOM_8'\">awb.MAX_CUSTOM_8</when>"
          + "<when test=\"permission.name() == 'CUSTOM_9'\">awb.MAX_CUSTOM_9</when>"
          + "<when test=\"permission.name() == 'CUSTOM_10'\">awb.MAX_CUSTOM_10</when>"
          + "<when test=\"permission.name() == 'CUSTOM_11'\">awb.MAX_CUSTOM_11</when>"
          + "<when test=\"permission.name() == 'CUSTOM_12'\">awb.MAX_CUSTOM_12</when>"
          + "</choose>";

  private UserMapperSqlProvider() {}

  public static String findById() {
    return OPENING_SCRIPT_TAG
        + "SELECT "
        + USER_INFO_COLUMNS
        + "FROM USER_INFO "
        + "WHERE USER_ID = #{id} "
        + DB2_WITH_UR
        + CLOSING_SCRIPT_TAG;
  }

  public static String findByIds() {
    return OPENING_SCRIPT_TAG
        + "SELECT "
        + USER_INFO_COLUMNS
        + "FROM USER_INFO "
        + "WHERE USER_ID IN (<foreach item='id' collection='ids' separator=',' >#{id}</foreach>) "
        + DB2_WITH_UR
        + CLOSING_SCRIPT_TAG;
  }

  public static String findGroupsById() {
    return OPENING_SCRIPT_TAG
        + "SELECT GROUP_ID FROM GROUP_INFO WHERE USER_ID = #{id} "
        + DB2_WITH_UR
        + CLOSING_SCRIPT_TAG;
  }

  public static String findPermissionsById() {
    return OPENING_SCRIPT_TAG
        + "SELECT PERMISSION_ID FROM PERMISSION_INFO WHERE USER_ID = #{id} "
        + DB2_WITH_UR
        + CLOSING_SCRIPT_TAG;
  }

  public static String findGroupsByIds() {
    return OPENING_SCRIPT_TAG
        + "SELECT USER_ID, GROUP_ID AS ATTRIBUTE_VALUE "
        + "FROM GROUP_INFO WHERE USER_ID IN ("
        + IDS_FOREACH
        + ") "
        + DB2_WITH_UR
        + CLOSING_SCRIPT_TAG;
  }

  public static String findPermissionsByIds() {
    return OPENING_SCRIPT_TAG
        + "SELECT USER_ID, PERMISSION_ID AS ATTRIBUTE_VALUE "
        + "FROM PERMISSION_INFO WHERE USER_ID IN ("
        + IDS_FOREACH
        + ") "
        + DB2_WITH_UR
        + CLOSING_SCRIPT_TAG;
  }

  public static String findDomainsByIds() {
    return OPENING_SCRIPT_TAG
        + "WITH REQUESTED_USERS AS ( "
        + "SELECT USER_ID FROM USER_INFO WHERE USER_ID IN ("
        + IDS_FOREACH
        + ") "
        + "), USER_ACCESS AS ( "
        + "SELECT USER_ID, LOWER(USER_ID) AS ACCESS_ID FROM REQUESTED_USERS "
        + "UNION ALL "
        + "SELECT ru.USER_ID, LOWER(g.GROUP_ID) AS ACCESS_ID "
        + "FROM REQUESTED_USERS ru JOIN GROUP_INFO g ON g.USER_ID = ru.USER_ID "
        + "UNION ALL "
        + "SELECT ru.USER_ID, LOWER(p.PERMISSION_ID) AS ACCESS_ID "
        + "FROM REQUESTED_USERS ru JOIN PERMISSION_INFO p ON p.USER_ID = ru.USER_ID "
        + "), ACCESS_BY_WORKBASKET AS ( "
        + "SELECT ua.USER_ID, a.WORKBASKET_ID, "
        + "<choose>"
        + "<when test=\"_databaseId == 'db2'\">"
        + DB2_PERMISSION_AGGREGATES
        + "</when>"
        + "<otherwise>"
        + BOOLEAN_PERMISSION_AGGREGATES
        + "</otherwise>"
        + "</choose>"
        + "FROM USER_ACCESS ua JOIN WORKBASKET_ACCESS_LIST a "
        + "ON a.ACCESS_ID = ua.ACCESS_ID "
        + "GROUP BY ua.USER_ID, a.WORKBASKET_ID "
        + ") SELECT DISTINCT awb.USER_ID, w.DOMAIN AS ATTRIBUTE_VALUE "
        + "FROM ACCESS_BY_WORKBASKET awb JOIN WORKBASKET w ON w.ID = awb.WORKBASKET_ID "
        + "WHERE awb.MAX_READ = 1 "
        + "AND <foreach item='permission' collection='permissions' separator=' AND '>"
        + PERMISSION_ALIAS
        + " = 1</foreach> "
        + DB2_WITH_UR
        + CLOSING_SCRIPT_TAG;
  }

  public static String insert() {
    return "INSERT INTO USER_INFO ( " + USER_INFO_COLUMNS + ") VALUES(" + USER_INFO_VALUES + ")";
  }

  public static String insertGroups() {
    return OPENING_SCRIPT_TAG
        + "INSERT INTO GROUP_INFO (USER_ID, GROUP_ID) VALUES "
        + "<foreach item='group' collection='groups' open='(' separator='),(' close=')'>"
        + "#{id}, #{group}"
        + "</foreach> "
        + CLOSING_SCRIPT_TAG;
  }

  public static String insertPermissions() {
    return OPENING_SCRIPT_TAG
        + "INSERT INTO PERMISSION_INFO (USER_ID, PERMISSION_ID) VALUES "
        + "<foreach item='permission' collection='permissions' "
        + "open='(' separator='),(' close=')'>"
        + "#{id}, #{permission}"
        + "</foreach> "
        + CLOSING_SCRIPT_TAG;
  }

  public static String update() {
    return "UPDATE USER_INFO "
        + "SET FIRST_NAME = #{firstName}, "
        + "LAST_NAME = #{lastName}, FULL_NAME = #{fullName}, LONG_NAME = #{longName}, "
        + "E_MAIL = #{email}, PHONE = #{phone}, MOBILE_PHONE = #{mobilePhone}, "
        + "ORG_LEVEL_4 = #{orgLevel4}, ORG_LEVEL_3 = #{orgLevel3}, "
        + "ORG_LEVEL_2 = #{orgLevel2}, ORG_LEVEL_1 = #{orgLevel1}, DATA = #{data} "
        + "WHERE USER_ID = #{id} ";
  }

  public static String delete() {
    return "DELETE FROM USER_INFO WHERE USER_ID = #{id} ";
  }

  public static String deleteAll() {
    return "DELETE FROM USER_INFO ";
  }

  public static String deleteGroups() {
    return "DELETE FROM GROUP_INFO WHERE USER_ID = #{id} ";
  }

  public static String deleteAllGroups() {
    return "DELETE FROM GROUP_INFO ";
  }

  public static String deletePermissions() {
    return "DELETE FROM PERMISSION_INFO WHERE USER_ID = #{id} ";
  }

  public static String deleteAllPermissions() {
    return "DELETE FROM PERMISSION_INFO ";
  }
}
