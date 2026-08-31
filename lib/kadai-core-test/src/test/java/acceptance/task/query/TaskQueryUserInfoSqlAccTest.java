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

import static io.kadai.common.api.BaseQuery.SortDirection.ASCENDING;
import static io.kadai.testapi.DefaultTestEntities.defaultTestClassification;
import static io.kadai.testapi.DefaultTestEntities.defaultTestObjectReference;
import static io.kadai.testapi.DefaultTestEntities.defaultTestWorkbasket;
import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.KadaiConfiguration;
import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.common.internal.InternalKadaiEngine;
import io.kadai.task.api.TaskQuery;
import io.kadai.task.api.TaskQueryColumnName;
import io.kadai.task.api.TaskService;
import io.kadai.testapi.KadaiConfigurationModifier;
import io.kadai.testapi.KadaiInject;
import io.kadai.testapi.KadaiIntegrationTest;
import io.kadai.testapi.builder.TaskBuilder;
import io.kadai.testapi.security.WithAccessId;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import java.sql.Statement;
import java.util.Locale;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@KadaiIntegrationTest
class TaskQueryUserInfoSqlAccTest implements KadaiConfigurationModifier {

  @KadaiInject InternalKadaiEngine internalKadaiEngine;
  @KadaiInject TaskService taskService;

  @Override
  public KadaiConfiguration.Builder modify(KadaiConfiguration.Builder builder) {
    return builder.useSpecificDb2Taskquery(false);
  }

  @BeforeAll
  void registerSqlCaptureInterceptor() {
    internalKadaiEngine
        .getSqlSession()
        .getConfiguration()
        .addInterceptor(new SqlCaptureInterceptor());
  }

  @WithAccessId(user = "admin")
  @Test
  void should_NotJoinUserInfo_When_CountHasNoLongNameFilter() {
    String sql = captureSql(() -> taskService.createTaskQuery().count());

    assertThat(sql).doesNotContain("user_info owner_info", "user_info creator_info");
  }

  @WithAccessId(user = "admin")
  @Test
  void should_NotJoinUserInfo_When_CountOnlySortsByLongName() {
    String ownerSql =
        captureSql(() -> taskService.createTaskQuery().orderByOwnerLongName(ASCENDING).count());
    String creatorSql =
        captureSql(() -> taskService.createTaskQuery().orderByCreatorLongName(ASCENDING).count());

    assertThat(ownerSql).doesNotContain("user_info owner_info", "user_info creator_info");
    assertThat(creatorSql).doesNotContain("user_info owner_info", "user_info creator_info");
  }

  @WithAccessId(user = "admin")
  @Test
  void should_JoinOnlyRequiredUserInfo_When_CountFiltersByLongName() {
    String ownerSql =
        captureSql(() -> taskService.createTaskQuery().ownerLongNameLike("%user-1-1%").count());
    String creatorSql =
        captureSql(() -> taskService.createTaskQuery().creatorLongNameLike("%user-1-1%").count());
    String bothSql =
        captureSql(
            () ->
                taskService
                    .createTaskQuery()
                    .ownerLongNameLike("%user-1-1%")
                    .creatorLongNameLike("%user-1-1%")
                    .count());

    assertThat(ownerSql).contains("user_info owner_info").doesNotContain("user_info creator_info");
    assertThat(creatorSql)
        .contains("user_info creator_info")
        .doesNotContain("user_info owner_info");
    assertThat(bothSql).contains("user_info owner_info", "user_info creator_info");
  }

  @WithAccessId(user = "admin")
  @Test
  void should_JoinOnlyRequiredUserInfo_When_ListingScalarValues() {
    String nameSql =
        captureSql(
            () -> taskService.createTaskQuery().listValues(TaskQueryColumnName.NAME, ASCENDING));
    String ownerSql =
        captureSql(
            () ->
                taskService
                    .createTaskQuery()
                    .listValues(TaskQueryColumnName.OWNER_LONG_NAME, ASCENDING));
    String creatorSql =
        captureSql(
            () ->
                taskService
                    .createTaskQuery()
                    .listValues(TaskQueryColumnName.CREATOR_LONG_NAME, ASCENDING));
    String filteredSql =
        captureSql(
            () ->
                taskService
                    .createTaskQuery()
                    .creatorLongNameLike("%user-1-1%")
                    .listValues(TaskQueryColumnName.NAME, ASCENDING));
    String selectedAndFilteredSql =
        captureSql(
            () ->
                taskService
                    .createTaskQuery()
                    .creatorLongNameLike("%user-1-1%")
                    .listValues(TaskQueryColumnName.OWNER_LONG_NAME, ASCENDING));

    assertThat(nameSql).doesNotContain("user_info owner_info", "user_info creator_info");
    assertThat(ownerSql).contains("user_info owner_info").doesNotContain("user_info creator_info");
    assertThat(creatorSql)
        .contains("user_info creator_info")
        .doesNotContain("user_info owner_info");
    assertThat(filteredSql)
        .contains("user_info creator_info")
        .doesNotContain("user_info owner_info");
    assertThat(selectedAndFilteredSql).contains("user_info owner_info", "user_info creator_info");
  }

  @WithAccessId(user = "admin")
  @Test
  void should_ProjectOnlyRequestedValue_When_ListingOwnerLongNames() {
    String sql =
        captureSql(
            () ->
                taskService
                    .createTaskQuery()
                    .listValues(TaskQueryColumnName.OWNER_LONG_NAME, ASCENDING));

    String selectClause = sql.substring(0, sql.indexOf("from task"));
    assertThat(selectClause).isEqualTo("select distinct owner_info.long_name ");
  }

  @WithAccessId(user = "admin")
  @Test
  void should_NotLeakScalarColumnJoinIntoLaterCount() {
    TaskQuery query = taskService.createTaskQuery();

    String scalarSql =
        captureSql(() -> query.listValues(TaskQueryColumnName.OWNER_LONG_NAME, ASCENDING));
    String countSql = captureSql(query::count);

    assertThat(scalarSql).contains("user_info owner_info");
    assertThat(countSql).doesNotContain("user_info owner_info", "user_info creator_info");
  }

  @WithAccessId(user = "admin")
  @Test
  void should_KeepSummaryUserInfoJoin_When_SortingOrFilteringByLongName() {
    TaskQuery ownerSortQuery = taskService.createTaskQuery().orderByOwnerLongName(ASCENDING);
    String countSql = captureSql(ownerSortQuery::count);
    String ownerSortSql = captureSql(ownerSortQuery::list);
    String creatorFilterSql =
        captureSql(() -> taskService.createTaskQuery().creatorLongNameLike("%user-1-1%").list());

    assertThat(countSql).doesNotContain("user_info owner_info", "user_info creator_info");
    assertThat(ownerSortSql)
        .contains("user_info owner_info")
        .doesNotContain("user_info creator_info");
    assertThat(creatorFilterSql)
        .contains("user_info creator_info")
        .doesNotContain("user_info owner_info");
  }

  private String captureSql(Runnable query) {
    SqlCaptureInterceptor.reset();
    query.run();
    return SqlCaptureInterceptor.get().toLowerCase(Locale.ROOT);
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class WithSpecificDb2Taskquery implements KadaiConfigurationModifier {

    @KadaiInject InternalKadaiEngine specificInternalKadaiEngine;
    @KadaiInject TaskService specificTaskService;

    @Override
    public KadaiConfiguration.Builder modify(KadaiConfiguration.Builder builder) {
      return builder.addAdditionalUserInfo(false).useSpecificDb2Taskquery(true);
    }

    @BeforeAll
    void registerSqlCaptureInterceptor() {
      specificInternalKadaiEngine
          .getSqlSession()
          .getConfiguration()
          .addInterceptor(new SqlCaptureInterceptor());
    }

    @WithAccessId(user = "admin")
    @Test
    void should_UseFilterDependenciesForSpecificDb2Count() {
      String ownerFilterSql =
          captureSql(
              () -> specificTaskService.createTaskQuery().ownerLongNameLike("%user-1-1%").count());
      String ownerSortSql =
          captureSql(
              () -> specificTaskService.createTaskQuery().orderByOwnerLongName(ASCENDING).count());

      assertThat(ownerFilterSql)
          .contains("user_info owner_info")
          .doesNotContain("user_info creator_info");
      assertThat(ownerSortSql).doesNotContain("user_info owner_info", "user_info creator_info");
    }

    @WithAccessId(user = "admin")
    @Test
    void should_UseFilterAndSortDependenciesForSpecificDb2Summary() {
      String ownerSortSql =
          captureSql(
              () -> specificTaskService.createTaskQuery().orderByOwnerLongName(ASCENDING).list());
      String creatorFilterSql =
          captureSql(
              () -> specificTaskService.createTaskQuery().creatorLongNameLike("%user-1-1%").list());

      assertThat(ownerSortSql)
          .contains("user_info owner_info")
          .doesNotContain("user_info creator_info");
      assertThat(creatorFilterSql)
          .contains("user_info creator_info")
          .doesNotContain("user_info owner_info");
    }
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class WithAdditionalUserInfo implements KadaiConfigurationModifier {

    @KadaiInject InternalKadaiEngine configuredInternalKadaiEngine;
    @KadaiInject TaskService configuredTaskService;
    @KadaiInject ClassificationService configuredClassificationService;
    @KadaiInject WorkbasketService configuredWorkbasketService;

    @Override
    public KadaiConfiguration.Builder modify(KadaiConfiguration.Builder builder) {
      return builder.addAdditionalUserInfo(true).useSpecificDb2Taskquery(true);
    }

    @BeforeAll
    void registerSqlCaptureInterceptor() {
      configuredInternalKadaiEngine
          .getSqlSession()
          .getConfiguration()
          .addInterceptor(new SqlCaptureInterceptor());
    }

    @WithAccessId(user = "admin")
    @Test
    void should_NotJoinUserInfo_When_EnrichmentIsEnabledForCount() {
      String sql = captureSql(() -> configuredTaskService.createTaskQuery().count());

      assertThat(sql).doesNotContain("user_info owner_info", "user_info creator_info");
    }

    @WithAccessId(user = "admin")
    @Test
    void should_NotJoinUserInfoOrProjectLongNames_When_EnrichmentIsEnabledForScalarValues() {
      String sql =
          captureSql(
              () ->
                  configuredTaskService
                      .createTaskQuery()
                      .listValues(TaskQueryColumnName.NAME, ASCENDING));

      assertThat(sql).doesNotContain("user_info owner_info", "user_info creator_info");
      assertThat(sql.substring(0, sql.indexOf("from task"))).isEqualTo("select distinct t.name ");
    }

    @WithAccessId(user = "admin")
    @Test
    void should_KeepBothUserInfoJoins_When_EnrichmentIsEnabledForSummaryList() {
      String sql = captureSql(() -> configuredTaskService.createTaskQuery().list());

      assertThat(sql).contains("user_info owner_info", "user_info creator_info");
    }

    @WithAccessId(user = "admin")
    @Test
    void should_PreserveDistinctValues_When_EnrichmentIsEnabled() throws Exception {
      ClassificationSummary classificationSummary =
          defaultTestClassification()
              .buildAndStoreAsSummary(configuredClassificationService, "admin");
      WorkbasketSummary workbasketSummary =
          defaultTestWorkbasket().buildAndStoreAsSummary(configuredWorkbasketService, "admin");

      TaskBuilder.newTask()
          .name("same-name-for-distinct-test")
          .owner("user-1-1")
          .classificationSummary(classificationSummary)
          .primaryObjRef(defaultTestObjectReference().build())
          .workbasketSummary(workbasketSummary)
          .buildAndStore(configuredTaskService, "admin");
      TaskBuilder.newTask()
          .name("same-name-for-distinct-test")
          .owner("user-1-2")
          .classificationSummary(classificationSummary)
          .primaryObjRef(defaultTestObjectReference().build())
          .workbasketSummary(workbasketSummary)
          .buildAndStore(configuredTaskService, "admin");

      assertThat(
              configuredTaskService
                  .createTaskQuery()
                  .nameIn("same-name-for-distinct-test")
                  .listValues(TaskQueryColumnName.NAME, ASCENDING))
          .containsExactly("same-name-for-distinct-test");
    }
  }

  @Intercepts({
    @Signature(
        type = StatementHandler.class,
        method = "parameterize",
        args = {Statement.class})
  })
  private static class SqlCaptureInterceptor implements Interceptor {
    private static volatile String sql;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
      sql = ((StatementHandler) invocation.getTarget()).getBoundSql().getSql();
      return invocation.proceed();
    }

    static String get() {
      return sql;
    }

    static void reset() {
      sql = null;
    }
  }
}
