/*
 * Copyright [2024] [envite consulting GmbH]
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

package io.kadai.task.internal;

import static io.kadai.common.internal.util.SqlProviderUtil.CLOSING_SCRIPT_TAG;
import static io.kadai.common.internal.util.SqlProviderUtil.CLOSING_WHERE_TAG;
import static io.kadai.common.internal.util.SqlProviderUtil.DB2_WITH_UR;
import static io.kadai.common.internal.util.SqlProviderUtil.OPENING_SCRIPT_TAG;
import static io.kadai.common.internal.util.SqlProviderUtil.OPENING_WHERE_TAG;
import static io.kadai.common.internal.util.SqlProviderUtil.whereIn;
import static io.kadai.common.internal.util.SqlProviderUtil.whereInInterval;
import static io.kadai.common.internal.util.SqlProviderUtil.whereLike;
import static io.kadai.common.internal.util.SqlProviderUtil.whereNotIn;
import static io.kadai.common.internal.util.SqlProviderUtil.whereNotInInterval;
import static io.kadai.common.internal.util.SqlProviderUtil.whereNotLike;
import static io.kadai.task.api.TaskCommentQueryColumnName.CREATED;
import static io.kadai.task.api.TaskCommentQueryColumnName.ID;
import static io.kadai.task.api.TaskCommentQueryColumnName.TASK_ID;
import static io.kadai.task.api.TaskCommentQueryColumnName.CREATOR;
import static io.kadai.task.api.TaskCommentQueryColumnName.MODIFIED;
import static io.kadai.task.api.TaskCommentQueryColumnName.TEXT_FIELD;

import io.kadai.task.api.TaskCommentQueryColumnName;
import java.util.Arrays;
import java.util.stream.Collectors;

public class TaskCommentQuerySqlProvider {

  private TaskCommentQuerySqlProvider() {}

  @SuppressWarnings("unused")
  public static String queryTaskComments() {
    return OPENING_SCRIPT_TAG
        + "SELECT "
        + commonSelectFields()
        + "<if test=\"joinWithUserInfo\">"
        + ", u.FULL_NAME"
        + "</if>"
        + "FROM TASK_COMMENT tc "
        + "LEFT JOIN Task t ON tc.TASK_ID = t.ID "
        + "<if test=\"joinWithUserInfo\">"
        + "LEFT JOIN USER_INFO u ON tc.CREATOR = u.USER_ID "
        + "</if>"
        + OPENING_WHERE_TAG
        + checkForAuthorization()
        + commonTaskCommentWhereStatement()
        + CLOSING_WHERE_TAG
        + "<if test='!orderBy.isEmpty()'>"
        + "ORDER BY <foreach item='item' collection='orderBy' separator=',' >${item}</foreach>"
        + "</if> "
        + CLOSING_SCRIPT_TAG;
  }

  @SuppressWarnings("unused")
  public static String countQueryTaskComments() {
    return OPENING_SCRIPT_TAG
        + "SELECT COUNT(tc.ID) "
        + "FROM TASK_COMMENT tc "
        + "LEFT JOIN Task t ON tc.TASK_ID = t.ID "
        + OPENING_WHERE_TAG
        + checkForAuthorization()
        + commonTaskCommentWhereStatement()
        + CLOSING_WHERE_TAG
        + CLOSING_SCRIPT_TAG;
  }

  @SuppressWarnings("unused")
  public static String queryTaskCommentColumnValues() {
    return OPENING_SCRIPT_TAG
        + "SELECT DISTINCT ${queryColumnName} "
        + "FROM TASK_COMMENT tc "
        + "LEFT JOIN Task t ON tc.TASK_ID = t.ID "
        + "<if test=\"joinWithUserInfo\">"
        + "LEFT JOIN USER_INFO u ON tc.CREATOR = u.USER_ID "
        + "</if>"
        + OPENING_WHERE_TAG
        + checkForAuthorization()
        + commonTaskCommentWhereStatement()
        + CLOSING_WHERE_TAG
        + DB2_WITH_UR
        + CLOSING_SCRIPT_TAG;
  }

  private static String commonSelectFields() {
    // includes only the names that start with tc, because other columns are conditional
    return Arrays.stream(TaskCommentQueryColumnName.values())
        .map(TaskCommentQueryColumnName::toString)
        .filter(column -> column.startsWith("tc"))
        .collect(Collectors.joining(", "));
  }

  private static String commonTaskCommentWhereStatement() {
    StringBuilder sb = new StringBuilder();
    whereIn("idIn", ID.toString(), sb);
    whereNotIn("idNotIn", ID.toString(), sb);
    whereLike("idLike", ID.toString(), sb);
    whereNotLike("idNotLike", ID.toString(), sb);
    whereIn("taskIdIn", TASK_ID.toString(), sb);
    whereLike("textFieldLike", TEXT_FIELD.toString(), sb);
    whereNotLike("textFieldNotLike", TEXT_FIELD.toString(), sb);
    whereIn("creatorIn", CREATOR.toString(), sb);
    whereNotIn("creatorNotIn", CREATOR.toString(), sb);
    whereLike("creatorLike", CREATOR.toString(), sb);
    whereNotLike("creatorNotLike", CREATOR.toString(), sb);
    whereInInterval("createdIn", CREATED.toString(), sb);
    whereNotInInterval("createdNotIn", CREATED.toString(), sb);
    whereInInterval("modifiedIn", MODIFIED.toString(), sb);
    whereNotInInterval("modifiedNotIn", MODIFIED.toString(), sb);
    return sb.toString();
  }

  private static String checkForAuthorization() {
    return "<if test='accessIdIn != null'> AND t.WORKBASKET_ID IN ("
        + "SELECT WID "
        + "FROM ("
        + "<choose>"
        + "<when test=\"_databaseId == 'db2'\">"
        + "SELECT WORKBASKET_ID as WID, MAX(PERM_READ) as MAX_READ "
        + "</when>"
        + "<otherwise>"
        + "SELECT WORKBASKET_ID as WID, MAX(PERM_READ::int) as MAX_READ "
        + "</otherwise>"
        + "</choose>"
        + "FROM WORKBASKET_ACCESS_LIST s "
        + "WHERE ACCESS_ID IN "
        + "(<foreach item='item' collection='accessIdIn' separator=',' >#{item}</foreach>) "
        + "GROUP by WORKBASKET_ID) f "
        + "WHERE MAX_READ = 1) "
        + "</if>";
  }
}
