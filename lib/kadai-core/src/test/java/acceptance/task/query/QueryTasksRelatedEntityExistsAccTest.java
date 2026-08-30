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
import static io.kadai.task.api.TaskQueryColumnName.A_CHANNEL;
import static io.kadai.task.api.TaskQueryColumnName.O_VALUE;
import static org.assertj.core.api.Assertions.assertThat;

import acceptance.AbstractAccTest;
import acceptance.ParameterizedQuerySqlCaptureInterceptor;
import io.kadai.common.internal.KadaiEngineImpl;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.common.test.security.WithAccessId;
import java.lang.reflect.Field;
import org.apache.ibatis.session.SqlSessionManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JaasExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
class QueryTasksRelatedEntityExistsAccTest extends AbstractAccTest {

  @BeforeAll
  void setupSqlCapture() throws Exception {
    Field sessionManagerField = KadaiEngineImpl.class.getDeclaredField("sessionManager");
    sessionManagerField.setAccessible(true);
    SqlSessionManager sessionManager = (SqlSessionManager) sessionManagerField.get(kadaiEngine);
    sessionManager
        .getConfiguration()
        .addInterceptor(new ParameterizedQuerySqlCaptureInterceptor());
  }

  @BeforeEach
  void resetCapturedSql() {
    ParameterizedQuerySqlCaptureInterceptor.resetCapturedSql();
  }

  @WithAccessId(user = "admin")
  @Test
  void should_UseExistsWithoutAttachmentJoin_When_AttachmentIsOnlyUsedForFiltering() {
    taskService
        .createTaskQuery()
        .attachmentChannelIn("exists-sql-shape-channel")
        .list();

    String sql = normalizedCapturedSql();

    assertThat(sql)
        .contains("EXISTS (SELECT 1 FROM ATTACHMENT a WHERE a.TASK_ID = t.ID")
        .doesNotContain("LEFT JOIN ATTACHMENT a ON t.ID = a.TASK_ID")
        .doesNotContain("SELECT DISTINCT");
  }

  @WithAccessId(user = "admin")
  @Test
  void should_JoinAttachmentClassificationInsideExists_When_FilteringByAttachmentName() {
    taskService
        .createTaskQuery()
        .attachmentClassificationNameLike("exists-sql-shape%")
        .list();

    String sql = normalizedCapturedSql();

    assertThat(sql)
        .contains(
            "EXISTS (SELECT 1 FROM ATTACHMENT a "
                + "LEFT JOIN CLASSIFICATION ac ON a.CLASSIFICATION_ID = ac.ID "
                + "WHERE a.TASK_ID = t.ID")
        .doesNotContain("LEFT JOIN ATTACHMENT a ON t.ID = a.TASK_ID");
  }

  @WithAccessId(user = "admin")
  @Test
  void should_UseExistsWithoutSorJoin_When_SorIsOnlyUsedForFiltering() {
    taskService
        .createTaskQuery()
        .sorCompanyIn("exists-sql-shape-company")
        .sorValueIn("exists-sql-shape-value")
        .list();

    String sql = normalizedCapturedSql();

    assertThat(sql)
        .contains("EXISTS (SELECT 1 FROM OBJECT_REFERENCE o WHERE o.TASK_ID = t.ID")
        .doesNotContain("LEFT JOIN OBJECT_REFERENCE o ON t.ID = o.TASK_ID")
        .doesNotContain("SELECT DISTINCT");
  }

  @WithAccessId(user = "admin")
  @Test
  void should_UseNotExistsWithoutAttachmentJoin_When_QueryingWithoutAttachment() {
    taskService.createTaskQuery().withoutAttachment().list();

    String sql = normalizedCapturedSql();

    assertThat(sql)
        .contains(
            "NOT EXISTS (SELECT 1 FROM ATTACHMENT a_without "
                + "WHERE a_without.TASK_ID = t.ID)")
        .doesNotContain("LEFT JOIN ATTACHMENT a ON t.ID = a.TASK_ID");
  }

  @WithAccessId(user = "admin")
  @Test
  void should_KeepAttachmentJoin_When_AttachmentIsNeededForOrdering() {
    taskService
        .createTaskQuery()
        .attachmentChannelIn("exists-sql-shape-channel")
        .orderByAttachmentChannel(ASCENDING)
        .list();

    String sql = normalizedCapturedSql();

    assertThat(sql)
        .contains("LEFT JOIN ATTACHMENT a ON t.ID = a.TASK_ID")
        .doesNotContain("EXISTS (SELECT 1 FROM ATTACHMENT a WHERE a.TASK_ID = t.ID");
  }

  @WithAccessId(user = "admin")
  @Test
  void should_UseCountDistinctWithRawAttachmentJoin_When_OnlyAttachmentIsFiltered() {
    taskService
        .createTaskQuery()
        .attachmentChannelIn("count-distinct-attachment-channel")
        .orderByAttachmentChannel(ASCENDING)
        .count();

    String sql = normalizedCapturedSql();

    assertThat(sql)
        .satisfies(this::assertCountDistinctTaskIds)
        .contains("INNER JOIN ATTACHMENT a ON a.TASK_ID = t.ID")
        .contains("a.CHANNEL")
        .doesNotContain("SELECT DISTINCT a.TASK_ID")
        .doesNotContain("filtered_a ON filtered_a.TASK_ID = t.ID")
        .doesNotContain("EXISTS (SELECT 1 FROM ATTACHMENT a WHERE a.TASK_ID = t.ID");
  }

  @WithAccessId(user = "admin")
  @Test
  void should_UseCountDistinctWithRawSorJoin_When_OnlySorIsFiltered() {
    taskService
        .createTaskQuery()
        .sorCompanyIn("count-distinct-sor-company")
        .sorValueIn("count-distinct-sor-value")
        .count();

    String sql = normalizedCapturedSql();

    assertThat(sql)
        .satisfies(this::assertCountDistinctTaskIds)
        .contains("INNER JOIN OBJECT_REFERENCE o ON o.TASK_ID = t.ID")
        .contains("o.COMPANY")
        .contains("o.VALUE")
        .doesNotContain("SELECT DISTINCT o.TASK_ID")
        .doesNotContain("filtered_o ON filtered_o.TASK_ID = t.ID")
        .doesNotContain("EXISTS (SELECT 1 FROM OBJECT_REFERENCE o WHERE o.TASK_ID = t.ID");
  }

  @WithAccessId(user = "admin")
  @Test
  void should_JoinAttachmentClassificationWithRawAttachmentCount() {
    taskService
        .createTaskQuery()
        .attachmentClassificationNameLike("count-distinct-classification%")
        .count();

    String sql = normalizedCapturedSql();

    assertThat(sql)
        .satisfies(this::assertCountDistinctTaskIds)
        .contains("INNER JOIN ATTACHMENT a ON a.TASK_ID = t.ID")
        .contains("LEFT JOIN CLASSIFICATION ac ON a.CLASSIFICATION_ID = ac.ID")
        .contains("ac.NAME")
        .doesNotContain("filtered_a ON filtered_a.TASK_ID = t.ID");
  }

  @WithAccessId(user = "admin")
  @Test
  void should_UseSeparateDeduplicatedRelations_When_AttachmentAndSorAreBothFiltered() {
    taskService
        .createTaskQuery()
        .attachmentChannelIn("count-both-channel")
        .sorCompanyIn("count-both-company")
        .count();

    String sql = normalizedCapturedSql();

    assertThat(sql)
        .contains("COUNT(*)")
        .contains("SELECT DISTINCT a.TASK_ID")
        .contains("filtered_a ON filtered_a.TASK_ID = t.ID")
        .contains("SELECT DISTINCT o.TASK_ID")
        .contains("filtered_o ON filtered_o.TASK_ID = t.ID")
        .doesNotContain("COUNT(DISTINCT t.ID)");
  }

  @WithAccessId(user = "admin")
  @Test
  void should_KeepNotExistsForCount_When_QueryingWithoutAttachment() {
    taskService.createTaskQuery().withoutAttachment().count();

    String sql = normalizedCapturedSql();

    assertThat(sql)
        .contains("COUNT(*)")
        .contains(
            "NOT EXISTS (SELECT 1 FROM ATTACHMENT a_without "
                + "WHERE a_without.TASK_ID = t.ID)")
        .doesNotContain("COUNT(DISTINCT t.ID)")
        .doesNotContain("filtered_a ON filtered_a.TASK_ID = t.ID")
        .doesNotContain("LEFT JOIN ATTACHMENT a ON t.ID = a.TASK_ID");
  }

  @WithAccessId(user = "admin")
  @Test
  void should_NotJoinAttachmentForCount_When_AttachmentIsOnlyUsedForOrdering() {
    taskService.createTaskQuery().orderByAttachmentChannel(ASCENDING).count();

    String sql = normalizedCapturedSql();

    assertThat(sql)
        .contains("COUNT(*)")
        .doesNotContain("COUNT(DISTINCT t.ID)")
        .doesNotContain("filtered_a ON filtered_a.TASK_ID = t.ID")
        .doesNotContain("LEFT JOIN ATTACHMENT a ON t.ID = a.TASK_ID")
        .doesNotContain("EXISTS (SELECT 1 FROM ATTACHMENT a WHERE a.TASK_ID = t.ID");
  }

  @WithAccessId(user = "admin")
  @Test
  void should_KeepAttachmentJoin_When_AttachmentIsNeededForProjection() {
    taskService
        .createTaskQuery()
        .attachmentReferenceValueIn("exists-sql-shape-reference")
        .listValues(A_CHANNEL, ASCENDING);

    String sql = normalizedCapturedSql();

    assertThat(sql)
        .contains("LEFT JOIN ATTACHMENT a ON t.ID = a.TASK_ID")
        .doesNotContain("EXISTS (SELECT 1 FROM ATTACHMENT a WHERE a.TASK_ID = t.ID");
  }

  @WithAccessId(user = "admin")
  @Test
  void should_KeepSorJoin_When_SorIsNeededForProjection() {
    taskService
        .createTaskQuery()
        .sorCompanyIn("exists-sql-shape-company")
        .listValues(O_VALUE, ASCENDING);

    String sql = normalizedCapturedSql();

    assertThat(sql)
        .contains("LEFT JOIN OBJECT_REFERENCE o ON t.ID = o.TASK_ID")
        .doesNotContain("EXISTS (SELECT 1 FROM OBJECT_REFERENCE o WHERE o.TASK_ID = t.ID");
  }

  private String normalizedCapturedSql() {
    String sql = ParameterizedQuerySqlCaptureInterceptor.getCapturedSql();
    assertThat(sql).isNotNull();
    return sql.replaceAll("\\s+", " ").trim();
  }

  private void assertCountDistinctTaskIds(String sql) {
    if (isDb2()) {
      assertThat(sql).contains("SELECT DISTINCT t.ID, t.WORKBASKET_ID");
    } else {
      assertThat(sql).contains("COUNT(DISTINCT t.ID)");
    }
  }

  private boolean isDb2() {
    return "DB2".equals(System.getenv("DB"));
  }
}
