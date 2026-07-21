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

package acceptance.task.query;

import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.task.internal.TaskQuerySqlProvider;
import org.junit.jupiter.api.Test;

class TaskQuerySqlProviderTest {

  @Test
  void should_IncludeCreatorAndOwnerLongNameSqlFragments_When_CreatingTaskSummaryQuery() {
    String sql = TaskQuerySqlProvider.queryTaskSummaries();

    assertThat(sql)
        .contains(
            "<if test=\"joinWithUserInfo\">, owner_info.LONG_NAME AS OWNER_LONG_NAME</if>",
            "<if test=\"joinWithCreatorUserInfo\">, "
                + "creator_info.LONG_NAME AS CREATOR_LONG_NAME</if>",
            "LEFT JOIN USER_INFO owner_info ON t.OWNER = owner_info.USER_ID",
            "LEFT JOIN USER_INFO creator_info ON t.CREATOR = creator_info.USER_ID",
            "creatorLongNameIn",
            "creatorLongNameNotIn",
            "creatorLongNameLike",
            "creatorLongNameNotLike",
            "ownerLongNameIn",
            "ownerLongNameNotIn",
            "ownerLongNameLike",
            "ownerLongNameNotLike");
  }

  @Test
  void should_IncludeCreatorAndOwnerLongNameSqlFragments_When_CreatingDb2TaskSummaryQuery() {
    String sql = TaskQuerySqlProvider.queryTaskSummariesDb2();

    assertThat(sql)
        .contains(
            "<if test=\"joinWithUserInfo\">, owner_info.LONG_NAME </if>",
            "<if test=\"joinWithCreatorUserInfo\">, creator_info.LONG_NAME </if>",
            "LEFT JOIN USER_INFO owner_info ON t.OWNER = owner_info.USER_ID",
            "LEFT JOIN USER_INFO creator_info ON t.CREATOR = creator_info.USER_ID",
            "creatorLongNameIn",
            "creatorLongNameNotIn",
            "creatorLongNameLike",
            "creatorLongNameNotLike",
            "ownerLongNameIn",
            "ownerLongNameNotIn",
            "ownerLongNameLike",
            "ownerLongNameNotLike");
  }

  @Test
  void should_IncludeCreatorAndOwnerLongNameJoins_When_CreatingCountQueries() {
    assertThat(TaskQuerySqlProvider.countQueryTasks())
        .contains(
            "LEFT JOIN USER_INFO owner_info ON t.OWNER = owner_info.USER_ID",
            "LEFT JOIN USER_INFO creator_info ON t.CREATOR = creator_info.USER_ID",
            "creatorLongNameIn",
            "ownerLongNameIn");
    assertThat(TaskQuerySqlProvider.countQueryTasksDb2())
        .contains(
            "LEFT JOIN USER_INFO owner_info ON t.OWNER = owner_info.USER_ID",
            "LEFT JOIN USER_INFO creator_info ON t.CREATOR = creator_info.USER_ID",
            "creatorLongNameIn",
            "ownerLongNameIn");
  }

  @Test
  void should_IncludeCreatorAndOwnerLongNameSqlFragments_When_CreatingListValuesQuery() {
    String sql = TaskQuerySqlProvider.queryTaskColumnValues();

    assertThat(sql)
        .contains(
            "<if test=\"joinWithUserInfo\">, owner_info.LONG_NAME </if>",
            "<if test=\"joinWithCreatorUserInfo\">, creator_info.LONG_NAME </if>",
            "LEFT JOIN USER_INFO owner_info ON t.OWNER = owner_info.USER_ID",
            "LEFT JOIN USER_INFO creator_info ON t.CREATOR = creator_info.USER_ID",
            "creatorLongNameIn",
            "ownerLongNameIn");
  }
}
