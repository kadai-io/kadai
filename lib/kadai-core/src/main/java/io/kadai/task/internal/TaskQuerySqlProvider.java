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
import static io.kadai.common.internal.util.SqlProviderUtil.whereCustomIntStatements;
import static io.kadai.common.internal.util.SqlProviderUtil.whereCustomStatements;
import static io.kadai.common.internal.util.SqlProviderUtil.whereIn;
import static io.kadai.common.internal.util.SqlProviderUtil.whereInInterval;
import static io.kadai.common.internal.util.SqlProviderUtil.whereLike;
import static io.kadai.common.internal.util.SqlProviderUtil.whereNotIn;
import static io.kadai.common.internal.util.SqlProviderUtil.whereNotInInterval;
import static io.kadai.common.internal.util.SqlProviderUtil.whereNotLike;
import static io.kadai.task.api.TaskQueryColumnName.A_CHANNEL;
import static io.kadai.task.api.TaskQueryColumnName.A_CLASSIFICATION_ID;
import static io.kadai.task.api.TaskQueryColumnName.A_CLASSIFICATION_KEY;
import static io.kadai.task.api.TaskQueryColumnName.A_CLASSIFICATION_NAME;
import static io.kadai.task.api.TaskQueryColumnName.A_REF_VALUE;
import static io.kadai.task.api.TaskQueryColumnName.BUSINESS_PROCESS_ID;
import static io.kadai.task.api.TaskQueryColumnName.CLAIMED;
import static io.kadai.task.api.TaskQueryColumnName.CLASSIFICATION_CATEGORY;
import static io.kadai.task.api.TaskQueryColumnName.CLASSIFICATION_ID;
import static io.kadai.task.api.TaskQueryColumnName.CLASSIFICATION_KEY;
import static io.kadai.task.api.TaskQueryColumnName.CLASSIFICATION_NAME;
import static io.kadai.task.api.TaskQueryColumnName.COMPLETED;
import static io.kadai.task.api.TaskQueryColumnName.CREATED;
import static io.kadai.task.api.TaskQueryColumnName.CREATOR;
import static io.kadai.task.api.TaskQueryColumnName.DESCRIPTION;
import static io.kadai.task.api.TaskQueryColumnName.DOMAIN;
import static io.kadai.task.api.TaskQueryColumnName.DUE;
import static io.kadai.task.api.TaskQueryColumnName.EXTERNAL_ID;
import static io.kadai.task.api.TaskQueryColumnName.ID;
import static io.kadai.task.api.TaskQueryColumnName.MODIFIED;
import static io.kadai.task.api.TaskQueryColumnName.NAME;
import static io.kadai.task.api.TaskQueryColumnName.NOTE;
import static io.kadai.task.api.TaskQueryColumnName.OWNER;
import static io.kadai.task.api.TaskQueryColumnName.OWNER_LONG_NAME;
import static io.kadai.task.api.TaskQueryColumnName.O_COMPANY;
import static io.kadai.task.api.TaskQueryColumnName.O_SYSTEM;
import static io.kadai.task.api.TaskQueryColumnName.O_SYSTEM_INSTANCE;
import static io.kadai.task.api.TaskQueryColumnName.O_TYPE;
import static io.kadai.task.api.TaskQueryColumnName.O_VALUE;
import static io.kadai.task.api.TaskQueryColumnName.PARENT_BUSINESS_PROCESS_ID;
import static io.kadai.task.api.TaskQueryColumnName.PLANNED;
import static io.kadai.task.api.TaskQueryColumnName.POR_COMPANY;
import static io.kadai.task.api.TaskQueryColumnName.POR_INSTANCE;
import static io.kadai.task.api.TaskQueryColumnName.POR_SYSTEM;
import static io.kadai.task.api.TaskQueryColumnName.POR_TYPE;
import static io.kadai.task.api.TaskQueryColumnName.POR_VALUE;
import static io.kadai.task.api.TaskQueryColumnName.PRIORITY;
import static io.kadai.task.api.TaskQueryColumnName.RECEIVED;
import static io.kadai.task.api.TaskQueryColumnName.STATE;
import static io.kadai.task.api.TaskQueryColumnName.WORKBASKET_ID;
import static io.kadai.task.api.TaskQueryColumnName.WORKBASKET_KEY;

import io.kadai.task.api.TaskQueryColumnName;
import java.util.Arrays;
import java.util.stream.Collectors;

public class TaskQuerySqlProvider {
  private TaskQuerySqlProvider() {}

  @SuppressWarnings("unused")
  public static String queryTaskSummaries() {
    return OPENING_SCRIPT_TAG
        + openOuterClauseForGroupByPorOrSor()
        + "SELECT <if test=\"useDistinctKeyword\">DISTINCT</if> "
        + commonSelectFields()
        + "<if test='groupBySor != null'>, o.VALUE as SOR_VALUE </if>"
        + "<if test=\"addAttachmentColumnsToSelectClauseForOrdering\">"
        + ", "
        + A_CLASSIFICATION_ID
        + " as ACLASSIFICATION_ID, "
        + A_CLASSIFICATION_KEY
        + " as ACLASSIFICATION_KEY, "
        + A_CHANNEL
        + " as ACHANNEL, "
        + A_REF_VALUE
        + " as AREF_VALUE, a.RECEIVED as ARECEIVED"
        + "</if>"
        + "<if test=\"addClassificationNameToSelectClauseForOrdering\">, c.NAME as CNAME </if>"
        + "<if test=\"addAttachmentClassificationNameToSelectClauseForOrdering\">, "
        + A_CLASSIFICATION_NAME
        + " as ACNAME </if>"
        + "<if test=\"addWorkbasketNameToSelectClauseForOrdering\">, w.NAME as WNAME </if>"
        + "<if test=\"joinWithUserInfo\">, u.LONG_NAME</if>"
        + groupByPorIfActive()
        + groupBySorIfActive()
        + "FROM TASK t "
        + "<if test=\"joinWithAttachments\">"
        + "LEFT JOIN ATTACHMENT a ON t.ID = a.TASK_ID "
        + "</if>"
        + "<if test=\"joinWithSecondaryObjectReferences\">"
        + "LEFT JOIN OBJECT_REFERENCE o ON t.ID = o.TASK_ID "
        + "</if>"
        + "<if test=\"joinWithClassifications\">"
        + "LEFT JOIN CLASSIFICATION c ON t.CLASSIFICATION_ID = c.ID "
        + "</if>"
        + "<if test=\"joinWithAttachmentClassifications\">"
        + "LEFT JOIN CLASSIFICATION ac ON a.CLASSIFICATION_ID = ac.ID "
        + "</if>"
        + "<if test=\"joinWithWorkbaskets\">"
        + "LEFT JOIN WORKBASKET w ON t.WORKBASKET_ID = w.ID "
        + "</if>"
        + "<if test=\"joinWithUserInfo\">"
        + "LEFT JOIN USER_INFO u ON t.owner = u.USER_ID "
        + "</if>"
        + OPENING_WHERE_TAG
        + checkForAuthorization()
        + commonTaskWhereStatement()
        + "<if test='selectAndClaim == true'> AND "
        + STATE
        + " = 'READY' </if>"
        + CLOSING_WHERE_TAG
        + closeOuterClauseForGroupByPor()
        + closeOuterClauseForGroupBySor()
        + "<if test='!orderByOuter.isEmpty()'>"
        + "ORDER BY <foreach item='item' collection='orderByOuter' separator=',' >${item}</foreach>"
        + "</if> "
        + "<if test='selectAndClaim == true'> "
        + "FETCH FIRST ROW ONLY FOR UPDATE "
        + "</if>"
        + "<if test='lockResults and lockResults != 0'> "
        + "FETCH FIRST ${lockResults} ROWS ONLY FOR UPDATE "
        + "<if test=\"_databaseId == 'postgres'\">"
        + "SKIP LOCKED "
        + "</if>"
        + "<if test=\"_databaseId == 'db2'\">"
        + "SKIP LOCKED DATA "
        + "</if>"
        + "</if>"
        + "<if test=\"_databaseId == 'db2' and (selectAndClaim or lockResults != 0) \">WITH RS USE "
        + "AND KEEP UPDATE LOCKS </if>"
        + "<if test=\"_databaseId == 'db2' and !selectAndClaim and lockResults==0 \">WITH UR </if>"
        + CLOSING_SCRIPT_TAG;
  }

  @SuppressWarnings("unused")
  public static String queryTaskSummariesDb2() {
    return OPENING_SCRIPT_TAG
        + "WITH X ("
        + db2selectFields()
        + ") AS ("
        + "SELECT <if test=\"useDistinctKeyword\">DISTINCT</if> "
        + commonSelectFields()
        + "<if test=\"addAttachmentColumnsToSelectClauseForOrdering\">"
        + ", "
        + attachementColumnSelectFields()
        + ", a.RECEIVED"
        + "</if>"
        + "<if test=\"addClassificationNameToSelectClauseForOrdering\">, "
        + CLASSIFICATION_NAME
        + " </if>"
        + "<if test=\"addAttachmentClassificationNameToSelectClauseForOrdering\">, "
        + A_CLASSIFICATION_NAME
        + " </if>"
        + "<if test=\"addWorkbasketNameToSelectClauseForOrdering\">, w.NAME </if>"
        + "<if test=\"joinWithUserInfo\">, "
        + OWNER_LONG_NAME
        + " </if>"
        + "FROM TASK t "
        + "<if test=\"joinWithAttachments\">"
        + "LEFT JOIN ATTACHMENT a ON t.ID = a.TASK_ID "
        + "</if>"
        + "<if test=\"joinWithSecondaryObjectReferences\">"
        + "LEFT JOIN OBJECT_REFERENCE o ON t.ID = o.TASK_ID "
        + "</if>"
        + "<if test=\"joinWithClassifications\">"
        + "LEFT JOIN CLASSIFICATION c ON t.CLASSIFICATION_ID = c.ID "
        + "</if>"
        + "<if test=\"joinWithAttachmentClassifications\">"
        + "LEFT JOIN CLASSIFICATION ac ON a.CLASSIFICATION_ID = ac.ID "
        + "</if>"
        + "<if test=\"joinWithWorkbaskets\">"
        + "LEFT JOIN WORKBASKET w ON t.WORKBASKET_ID = w.ID "
        + "</if>"
        + "<if test=\"joinWithUserInfo\">"
        + "LEFT JOIN USER_INFO u ON t.owner = u.USER_ID "
        + "</if>"
        + OPENING_WHERE_TAG
        + commonTaskWhereStatement()
        + CLOSING_WHERE_TAG
        + "), Y ("
        + db2selectFields()
        + ", FLAG ) AS ("
        + "SELECT "
        + db2selectFields()
        + ", ("
        + "<if test='accessIdIn != null'> "
        + "SELECT 1 "
        + "FROM WORKBASKET_ACCESS_LIST s "
        + "WHERE "
        + "s.ACCESS_ID IN "
        + "(<foreach item='item' collection='accessIdIn' separator=',' >#{item}</foreach>) "
        + "and "
        + "s.WORKBASKET_ID = X.WORKBASKET_ID AND s.perm_read = 1 AND s.perm_readtasks = 1"
        + " fetch first 1 rows only"
        + "</if>"
        + "<if test='accessIdIn == null'> "
        + "VALUES(1)"
        + "</if>"
        + " ) "
        + "FROM X )"
        + "SELECT "
        + db2selectFields()
        + "FROM Y "
        + "WHERE FLAG = 1 "
        + "<if test='!orderByOuter.isEmpty()'>"
        + "ORDER BY <foreach item='item' collection='orderByOuter' separator=',' >${item}</foreach>"
        + "</if> "
        + "with UR "
        + CLOSING_SCRIPT_TAG;
  }

  @SuppressWarnings("unused")
  public static String countQueryTasks() {
    return OPENING_SCRIPT_TAG
        + "SELECT COUNT( <if test=\"useDistinctKeyword\">DISTINCT</if> t.ID) "
        + "<if test=\"groupByPor or groupBySor != null\"> "
        + "FROM (SELECT "
        + ID
        + ", "
        + POR_VALUE
        + " </if> "
        + "<if test=\"groupBySor != null\"> "
        + ", o.VALUE as SOR_VALUE "
        + "</if> "
        + groupByPorIfActive()
        + groupBySorIfActive()
        + "FROM TASK t "
        + "<if test=\"joinWithAttachments\">"
        + "LEFT JOIN ATTACHMENT a ON t.ID = a.TASK_ID "
        + "</if>"
        + "<if test=\"joinWithSecondaryObjectReferences\">"
        + "LEFT JOIN OBJECT_REFERENCE o ON t.ID = o.TASK_ID "
        + "</if>"
        + "<if test=\"joinWithClassifications\">"
        + "LEFT JOIN CLASSIFICATION c ON t.CLASSIFICATION_ID = c.ID "
        + "</if>"
        + "<if test=\"joinWithAttachmentClassifications\">"
        + "LEFT JOIN CLASSIFICATION ac ON a.CLASSIFICATION_ID = ac.ID "
        + "</if>"
        + "<if test=\"joinWithUserInfo\">"
        + "LEFT JOIN USER_INFO u ON t.owner = u.USER_ID "
        + "</if>"
        + OPENING_WHERE_TAG
        + checkForAuthorization()
        + commonTaskWhereStatement()
        + CLOSING_WHERE_TAG
        + closeOuterClauseForGroupByPor()
        + closeOuterClauseForGroupBySor()
        + CLOSING_SCRIPT_TAG;
  }

  @SuppressWarnings("unused")
  public static String countQueryTasksDb2() {
    return OPENING_SCRIPT_TAG
        + "WITH X (ID, WORKBASKET_ID) AS ("
        + "SELECT <if test=\"useDistinctKeyword\">DISTINCT</if> "
        + ID
        + ", "
        + WORKBASKET_ID
        + " FROM TASK t "
        + "<if test=\"joinWithAttachments\">"
        + "LEFT JOIN ATTACHMENT a ON t.ID = a.TASK_ID "
        + "</if>"
        + "<if test=\"joinWithClassifications\">"
        + "LEFT JOIN CLASSIFICATION c ON t.CLASSIFICATION_ID = c.ID "
        + "</if>"
        + "<if test=\"joinWithAttachmentClassifications\">"
        + "LEFT JOIN CLASSIFICATION ac ON a.CLASSIFICATION_ID = ac.ID "
        + "</if>"
        + "<if test=\"joinWithSecondaryObjectReferences\">"
        + "LEFT JOIN OBJECT_REFERENCE o ON t.ID = o.TASK_ID "
        + "</if>"
        + "<if test=\"joinWithUserInfo\">"
        + "LEFT JOIN USER_INFO u ON t.owner = u.USER_ID "
        + "</if>"
        + OPENING_WHERE_TAG
        + commonTaskWhereStatement()
        + CLOSING_WHERE_TAG
        + "), Y (ID, FLAG) AS ("
        + "SELECT ID, ("
        + "<if test='accessIdIn != null'>"
        + "SELECT 1 FROM WORKBASKET_ACCESS_LIST s "
        + "WHERE s.ACCESS_ID IN "
        + "(<foreach item='item' collection='accessIdIn' separator=',' >#{item}</foreach>) "
        + "and "
        + "s.WORKBASKET_ID = X.WORKBASKET_ID AND s.perm_read = 1 AND s.perm_readtasks = 1"
        + " fetch first 1 rows only "
        + "</if> "
        + "<if test='accessIdIn == null'>"
        + "VALUES(1)"
        + "</if> "
        + ") "
        + "FROM X ) SELECT COUNT(*) "
        + "FROM Y WHERE FLAG = 1 with UR"
        + CLOSING_SCRIPT_TAG;
  }

  @SuppressWarnings("unused")
  public static String queryTaskColumnValues() {
    return OPENING_SCRIPT_TAG
        + "SELECT DISTINCT ${columnName} "
        + "<if test=\"joinWithUserInfo\">, "
        + OWNER_LONG_NAME
        + " </if>"
        + "FROM TASK t "
        + "<if test=\"joinWithAttachments\">"
        + "LEFT JOIN ATTACHMENT a ON t.ID = a.TASK_ID "
        + "</if>"
        + "<if test=\"joinWithClassifications\">"
        + "LEFT JOIN CLASSIFICATION c ON t.CLASSIFICATION_ID = c.ID "
        + "</if>"
        + "<if test=\"joinWithAttachmentClassifications\">"
        + "LEFT JOIN CLASSIFICATION ac ON a.CLASSIFICATION_ID = ac.ID "
        + "</if>"
        + "<if test=\"joinWithSecondaryObjectReferences\">"
        + "LEFT JOIN OBJECT_REFERENCE o ON t.ID = o.TASK_ID "
        + "</if>"
        + "<if test=\"joinWithUserInfo\">"
        + "LEFT JOIN USER_INFO u ON t.owner = u.USER_ID "
        + "</if>"
        + OPENING_WHERE_TAG
        + checkForAuthorization()
        + commonTaskWhereStatement()
        + CLOSING_WHERE_TAG
        + "<if test='!orderByInner.isEmpty()'>"
        + "ORDER BY <foreach item='item' collection='orderByInner' separator=',' >"
        + "<choose>"
        + "<when test=\"item.contains('TCLASSIFICATION_KEY ASC')\">"
        + CLASSIFICATION_KEY
        + " ASC"
        + "</when>"
        + "<when test=\"item.contains('TCLASSIFICATION_KEY DESC')\">"
        + CLASSIFICATION_KEY
        + " DESC"
        + "</when>"
        + "<when test=\"item.contains('ACLASSIFICATION_KEY ASC')\">"
        + A_CLASSIFICATION_KEY
        + " ASC"
        + "</when>"
        + "<when test=\"item.contains('ACLASSIFICATION_KEY DESC')\">"
        + A_CLASSIFICATION_KEY
        + " DESC"
        + "</when>"
        + "<when test=\"item.contains('ACLASSIFICATION_ID ASC')\">"
        + A_CLASSIFICATION_ID
        + " ASC"
        + "</when>"
        + "<when test=\"item.contains('ACLASSIFICATION_ID DESC')\">"
        + A_CLASSIFICATION_ID
        + " DESC"
        + "</when>"
        + "<when test=\"item.contains('CLASSIFICATION_NAME DESC')\">"
        + CLASSIFICATION_NAME
        + " DESC"
        + "</when>"
        + "<when test=\"item.contains('CLASSIFICATION_NAME ASC')\">"
        + CLASSIFICATION_NAME
        + " ASC"
        + "</when>"
        + "<when test=\"item.contains('A_CLASSIFICATION_NAME DESC')\">"
        + A_CLASSIFICATION_NAME
        + " DESC"
        + "</when>"
        + "<when test=\"item.contains('A_CLASSIFICATION_NAME ASC')\">"
        + A_CLASSIFICATION_NAME
        + " ASC"
        + "</when>"
        + "<otherwise>${item}</otherwise>"
        + "</choose>"
        + "</foreach>"
        + "</if> "
        + DB2_WITH_UR
        + CLOSING_SCRIPT_TAG;
  }

  private static String commonSelectFields() {
    // includes only the names that start with a t, because other columns are conditional
    return Arrays.stream(TaskQueryColumnName.values())
        .map(TaskQueryColumnName::toString)
        .filter(column -> column.startsWith("t"))
        .collect(Collectors.joining(", "));
  }

  private static String db2selectFields() {
    // needs to be the same order as the commonSelectFields (TaskQueryColumnValue)
    return Arrays.stream(TaskQueryColumnName.values())
            .map(TaskQueryColumnName::toString)
            .filter(column -> column.startsWith("t"))
            .map(t -> t.replace("t.", ""))
            .collect(Collectors.joining(", "))
            .replace("classification_key", "tclassification_key")
        + "<if test=\"addClassificationNameToSelectClauseForOrdering\">, CNAME</if>"
        + "<if test=\"addAttachmentClassificationNameToSelectClauseForOrdering\">, ACNAME</if>"
        + "<if test=\"addAttachmentColumnsToSelectClauseForOrdering\">"
        + ", ACLASSIFICATION_ID, ACLASSIFICATION_KEY, CHANNEL, REF_VALUE, ARECEIVED"
        + "</if>"
        + "<if test=\"addWorkbasketNameToSelectClauseForOrdering\">, WNAME</if>"
        + "<if test=\"joinWithUserInfo\">, ULONG_NAME </if>";
  }

  private static String attachementColumnSelectFields() {
    return Arrays.stream(TaskQueryColumnName.values())
        .map(TaskQueryColumnName::toString)
        .filter(column -> column.startsWith("a."))
        .collect(Collectors.joining(", "));
  }

  private static String checkForAuthorization() {
    return "<if test='accessIdIn != null'> AND "
        + WORKBASKET_ID
        + " IN ("
        + "SELECT WID "
        + "FROM ("
        + "<choose>"
        + "<when test=\"_databaseId == 'db2'\">"
        + "SELECT WORKBASKET_ID as WID, MAX(PERM_READ) as MAX_READ, "
        + "MAX(PERM_READTASKS) as MAX_READTASKS "
        + "</when>"
        + "<otherwise>"
        + "SELECT WORKBASKET_ID as WID, MAX(PERM_READ::int) as MAX_READ, "
        + "MAX(PERM_READTASKS::int) as MAX_READTASKS "
        + "</otherwise>"
        + "</choose>"
        + "FROM WORKBASKET_ACCESS_LIST s where ACCESS_ID IN "
        + "(<foreach item='item' collection='accessIdIn' separator=',' >#{item}</foreach>) "
        + "GROUP by WORKBASKET_ID) f "
        + "WHERE MAX_READ = 1 AND MAX_READTASKS = 1) "
        + "</if>";
  }

  private static String groupByPorIfActive() {
    return "<if test=\"groupByPor\"> "
        + ", ROW_NUMBER() OVER (PARTITION BY POR_VALUE "
        + "<if test='!orderByInner.isEmpty() and !orderByInner.get(0).equals(\"POR_VALUE ASC\") "
        + "and !orderByInner.get(0).equals(\"POR_VALUE DESC\")'>"
        + "ORDER BY <foreach item='item' collection='orderByInner' separator=',' >${item}</foreach>"
        + "</if> "
        + "<if test='orderByInner.isEmpty() or orderByInner.get(0).equals(\"POR_VALUE ASC\") "
        + "or orderByInner.get(0).equals(\"POR_VALUE DESC\")'>"
        + "ORDER BY DUE ASC"
        + "</if> "
        + ")"
        + "AS rn"
        + "</if> ";
  }

  private static String groupBySorIfActive() {
    return "<if test='groupBySor != null'> "
        + ", ROW_NUMBER() OVER (PARTITION BY o.VALUE "
        + "<if test='!orderByInner.isEmpty()'>"
        + "ORDER BY <foreach item='item' collection='orderByInner' separator=',' >${item}</foreach>"
        + "</if> "
        + "<if test='orderByInner.isEmpty()'>"
        + "ORDER BY DUE ASC"
        + "</if> "
        + ")"
        + "AS rn"
        + "</if> ";
  }

  private static String openOuterClauseForGroupByPorOrSor() {
    return "<if test=\"groupByPor or groupBySor != null\"> " + "SELECT * FROM (" + "</if> ";
  }

  private static String closeOuterClauseForGroupByPor() {
    return "<if test=\"groupByPor\"> "
        + ") t LEFT JOIN"
        + " (SELECT POR_VALUE as PVALUE, COUNT(POR_VALUE) AS R_COUNT "
        + "FROM (SELECT DISTINCT "
        + ID
        + " , POR_VALUE "
        + "FROM TASK t"
        + "<if test=\"joinWithAttachments\">"
        + "LEFT JOIN ATTACHMENT a ON t.ID = a.TASK_ID "
        + "</if>"
        + "<if test=\"joinWithSecondaryObjectReferences\">"
        + "LEFT JOIN OBJECT_REFERENCE o ON t.ID = o.TASK_ID "
        + "</if>"
        + "<if test=\"joinWithClassifications\">"
        + "LEFT JOIN CLASSIFICATION c ON t.CLASSIFICATION_ID = c.ID "
        + "</if>"
        + "<if test=\"joinWithAttachmentClassifications\">"
        + "LEFT JOIN CLASSIFICATION ac ON a.CLASSIFICATION_ID = ac.ID "
        + "</if>"
        + "<if test=\"joinWithWorkbaskets\">"
        + "LEFT JOIN WORKBASKET w ON t.WORKBASKET_ID = w.ID "
        + "</if>"
        + "<if test=\"joinWithUserInfo\">"
        + "LEFT JOIN USER_INFO u ON t.owner = u.USER_ID "
        + "</if>"
        + OPENING_WHERE_TAG
        + checkForAuthorization()
        + commonTaskWhereStatement()
        + "<if test='selectAndClaim == true'> AND t.STATE = 'READY' </if>"
        + CLOSING_WHERE_TAG
        + ") as y "
        + "GROUP BY POR_VALUE) AS tt ON t.POR_VALUE=tt.PVALUE "
        + "WHERE rn = 1"
        + "</if> ";
  }

  private static String closeOuterClauseForGroupBySor() {
    return "<if test='groupBySor != null'> "
        + ") t LEFT JOIN"
        + " (SELECT "
        + O_VALUE
        + ", COUNT("
        + O_VALUE
        + ") AS R_COUNT "
        + "FROM TASK t "
        + "LEFT JOIN OBJECT_REFERENCE o on t.ID=o.TASK_ID "
        + "<if test=\"joinWithAttachments\">"
        + "LEFT JOIN ATTACHMENT a ON t.ID = a.TASK_ID "
        + "</if>"
        + "<if test=\"joinWithClassifications\">"
        + "LEFT JOIN CLASSIFICATION c ON t.CLASSIFICATION_ID = c.ID "
        + "</if>"
        + "<if test=\"joinWithAttachmentClassifications\">"
        + "LEFT JOIN CLASSIFICATION ac ON a.CLASSIFICATION_ID = ac.ID "
        + "</if>"
        + "<if test=\"joinWithWorkbaskets\">"
        + "LEFT JOIN WORKBASKET w ON t.WORKBASKET_ID = w.ID "
        + "</if>"
        + "<if test=\"joinWithUserInfo\">"
        + "LEFT JOIN USER_INFO u ON t.owner = u.USER_ID "
        + "</if>"
        + OPENING_WHERE_TAG
        + checkForAuthorization()
        + commonTaskWhereStatement()
        + "AND o.TYPE=#{groupBySor} "
        + CLOSING_WHERE_TAG
        + "GROUP BY o.VALUE) AS tt ON t.SOR_VALUE=tt.VALUE "
        + "WHERE rn = 1"
        + "</if> ";
  }

  private static String commonTaskObjectReferenceWhereStatement() {
    return "<if test='objectReferences != null'>"
        + "AND (<foreach item='item' collection='objectReferences' separator=' OR '> "
        + "<if test='item.company != null'>t.POR_COMPANY = #{item.company} </if>"
        + "<if test='item.system != null'> "
        + "<if test='item.company != null'>AND</if> "
        + POR_SYSTEM
        + " = #{item.system} </if>"
        + "<if test='item.systemInstance != null'> "
        + "<if test='item.company != null or item.system != null'>AND</if> "
        + POR_INSTANCE
        + " = #{item.systemInstance} </if>"
        + "<if test='item.type != null'> "
        + "<if test='item.company != null or item.system != null or item.systemInstance != null'>"
        + "AND</if> "
        + POR_TYPE
        + " = #{item.type} </if>"
        + "<if test='item.value != null'> "
        + "<if test='item.company != null or item.system != null "
        + "or item.systemInstance != null or item.type != null'>"
        + "AND</if> "
        + POR_VALUE
        + " = #{item.value} "
        + "</if>"
        + "</foreach>)"
        + "</if>";
  }

  private static String commonTaskSecondaryObjectReferencesWhereStatement() {
    return "<if test='secondaryObjectReferences != null'>"
        + "AND (<foreach item='item' collection='secondaryObjectReferences' separator=' OR '> "
        + "<if test='item.company != null'>"
        + O_COMPANY
        + " = #{item.company} </if>"
        + "<if test='item.system != null'> "
        + "<if test='item.company != null'>AND</if> "
        + O_SYSTEM
        + " = #{item.system} </if>"
        + "<if test='item.systemInstance != null'> "
        + "<if test='item.company != null or item.system != null'>AND</if> "
        + O_SYSTEM_INSTANCE
        + " = #{item.systemInstance} </if>"
        + "<if test='item.type != null'> "
        + "<if test='item.company != null or item.system != null or item.systemInstance != null'>"
        + "AND</if> "
        + O_TYPE
        + " = #{item.type} </if>"
        + "<if test='item.value != null'> "
        + "<if test='item.company != null or item.system != null "
        + "or item.systemInstance != null or item.type != null'>"
        + "AND</if> "
        + O_VALUE
        + " = #{item.value} "
        + "</if>"
        + "</foreach>)"
        + "</if>";
  }

  private static void commonWhereClauses(String filter, String channel, StringBuilder sb) {
    whereIn(filter + "In", channel, sb);
    whereNotIn(filter + "NotIn", channel, sb);
    whereLike(filter + "Like", channel, sb);
    whereNotLike(filter + "NotLike", channel, sb);
  }

  private static StringBuilder commonTaskWhereStatement() {
    StringBuilder sb = new StringBuilder();
    commonWhereClauses("attachmentChannel", A_CHANNEL.toString(), sb);
    commonWhereClauses("attachmentClassificationKey", A_CLASSIFICATION_KEY.toString(), sb);
    commonWhereClauses("attachmentClassificationName", A_CLASSIFICATION_NAME.toString(), sb);
    commonWhereClauses("attachmentReference", A_REF_VALUE.toString(), sb);
    commonWhereClauses("businessProcessId", BUSINESS_PROCESS_ID.toString(), sb);
    commonWhereClauses("classificationCategory", CLASSIFICATION_CATEGORY.toString(), sb);
    commonWhereClauses("classificationKey", CLASSIFICATION_KEY.toString(), sb);
    commonWhereClauses("classificationParentKey", "c.PARENT_KEY", sb);
    commonWhereClauses("classificationName", CLASSIFICATION_NAME.toString(), sb);
    commonWhereClauses("creator", CREATOR.toString(), sb);
    commonWhereClauses("name", NAME.toString(), sb);
    commonWhereClauses("owner", OWNER.toString(), sb);
    commonWhereClauses("parentBusinessProcessId", PARENT_BUSINESS_PROCESS_ID.toString(), sb);
    commonWhereClauses("porCompany", POR_COMPANY.toString(), sb);
    commonWhereClauses("porSystem", POR_SYSTEM.toString(), sb);
    commonWhereClauses("porSystemInstance", POR_INSTANCE.toString(), sb);
    commonWhereClauses("porType", POR_TYPE.toString(), sb);
    commonWhereClauses("porValue", POR_VALUE.toString(), sb);

    whereIn("sorCompanyIn", O_COMPANY.toString(), sb);
    whereLike("sorCompanyLike", O_COMPANY.toString(), sb);
    whereIn("sorSystemIn", O_SYSTEM.toString(), sb);
    whereLike("sorSystemLike", O_SYSTEM.toString(), sb);
    whereIn("sorSystemInstanceIn", O_SYSTEM_INSTANCE.toString(), sb);
    whereLike("sorSystemInstanceLike", O_SYSTEM_INSTANCE.toString(), sb);
    whereIn("sorTypeIn", O_TYPE.toString(), sb);
    whereLike("sorTypeLike", O_TYPE.toString(), sb);
    whereIn("sorValueIn", O_VALUE.toString(), sb);
    whereLike("sorValueLike", O_VALUE.toString(), sb);

    whereIn("attachmentClassificationIdIn", A_CLASSIFICATION_ID.toString(), sb);
    whereNotIn("attachmentClassificationIdNotIn", A_CLASSIFICATION_ID.toString(), sb);
    whereIn("callbackStateIn", "t.CALLBACK_STATE", sb);
    whereNotIn("callbackStateNotIn", "t.CALLBACK_STATE", sb);
    whereIn("classificationIdIn", CLASSIFICATION_ID.toString(), sb);
    whereNotIn("classificationIdNotIn", CLASSIFICATION_ID.toString(), sb);
    whereIn("externalIdIn", EXTERNAL_ID.toString(), sb);
    whereNotIn("externalIdNotIn", EXTERNAL_ID.toString(), sb);
    whereIn("priority", PRIORITY.toString(), sb);
    whereNotIn("priorityNotIn", PRIORITY.toString(), sb);
    whereIn("ownerLongNameIn", OWNER_LONG_NAME.toString(), sb);
    whereNotIn("ownerLongNameNotIn", OWNER_LONG_NAME.toString(), sb);
    whereIn("stateIn", STATE.toString(), sb);
    whereNotIn("stateNotIn", STATE.toString(), sb);
    whereIn("taskId", ID.toString(), sb);
    whereNotIn("taskIdNotIn", ID.toString(), sb);
    whereIn("workbasketIdIn", WORKBASKET_ID.toString(), sb);
    whereNotIn("workbasketIdNotIn", WORKBASKET_ID.toString(), sb);
    whereLike("descriptionLike", DESCRIPTION.toString(), sb);
    whereNotLike("descriptionNotLike", DESCRIPTION.toString(), sb);
    whereLike("noteLike", NOTE.toString(), sb);
    whereNotLike("noteNotLike", NOTE.toString(), sb);

    whereInInterval("attachmentReceivedWithin", "a.RECEIVED", sb);
    whereNotInInterval("attachmentReceivedNotWithin", "a.RECEIVED", sb);
    whereInInterval("claimedWithin", CLAIMED.toString(), sb);
    whereNotInInterval("claimedNotWithin", CLAIMED.toString(), sb);
    whereInInterval("completedWithin", COMPLETED.toString(), sb);
    whereNotInInterval("completedNotWithin", COMPLETED.toString(), sb);
    whereInInterval("createdWithin", CREATED.toString(), sb);
    whereNotInInterval("createdNotWithin", CREATED.toString(), sb);
    whereInInterval("dueWithin", DUE.toString(), sb);
    whereNotInInterval("dueNotWithin", DUE.toString(), sb);
    whereInInterval("modifiedWithin", MODIFIED.toString(), sb);
    whereNotInInterval("modifiedNotWithin", MODIFIED.toString(), sb);
    whereInInterval("plannedWithin", PLANNED.toString(), sb);
    whereNotInInterval("plannedNotWithin", PLANNED.toString(), sb);
    whereInInterval("receivedWithin", RECEIVED.toString(), sb);
    whereNotInInterval("receivedNotWithin", RECEIVED.toString(), sb);
    whereInInterval("priorityWithin", PRIORITY.toString(), sb);
    whereNotInInterval("priorityNotWithin", PRIORITY.toString(), sb);

    whereLike("ownerLongNameLike", OWNER_LONG_NAME.toString(), sb);
    whereNotLike("ownerLongNameNotLike", OWNER_LONG_NAME.toString(), sb);
    whereCustomStatements("custom", "t.CUSTOM", 16, sb);
    whereCustomIntStatements("customInt", "t.CUSTOM_INT", 8, sb);

    sb.append(
        "<if test='hasComments != null and hasComments.booleanValue()'>"
            + "AND NUMBER_OF_COMMENTS > 0</if> ");
    sb.append(
        "<if test='hasComments != null and !hasComments.booleanValue()'>"
            + "AND NUMBER_OF_COMMENTS = 0</if> ");
    sb.append("<if test='isRead != null'>AND IS_READ = #{isRead}</if> ");
    sb.append("<if test='isTransferred != null'>AND IS_TRANSFERRED = #{isTransferred}</if> ");
    sb.append("<if test='isReopened != null'>AND IS_REOPENED = #{isReopened}</if> ");
    sb.append(
        "<if test='workbasketKeyDomainIn != null'>AND (<foreach item='item'"
            + " collection='workbasketKeyDomainIn' separator=' OR '>("
            + WORKBASKET_KEY
            + "= #{item.key}"
            + " AND "
            + DOMAIN
            + " = #{item.domain})</foreach>)</if> ");
    sb.append(
        "<if test='workbasketKeyDomainNotIn != null'>AND (<foreach item='item'"
            + " collection='workbasketKeyDomainNotIn' separator=' OR '>("
            + WORKBASKET_KEY
            + " !="
            + " #{item.key} OR t.DOMAIN != #{item.domain})</foreach>)</if> ");
    sb.append(
        "<if test='wildcardSearchValueLike != null and wildcardSearchFieldIn != null'>AND ("
            + "<foreach item='item' collection='wildcardSearchFieldIn' separator=' OR '>"
            + "LOWER(t.${item}) "
            + "LIKE #{wildcardSearchValueLike}"
            + "</foreach>)"
            + "</if> ");
    sb.append("<if test='withoutAttachment'> AND a.ID IS NULL</if> ");
    sb.append(commonTaskObjectReferenceWhereStatement());
    sb.append(commonTaskSecondaryObjectReferencesWhereStatement());
    return sb;
  }
}
