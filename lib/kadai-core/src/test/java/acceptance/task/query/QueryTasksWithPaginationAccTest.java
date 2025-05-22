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

package acceptance.task.query;

import static io.kadai.common.api.BaseQuery.SortDirection.DESCENDING;
import static org.assertj.core.api.Assertions.assertThat;

import acceptance.AbstractAccTest;
import acceptance.ParameterizedQuerySqlCaptureInterceptor;
import io.kadai.common.api.KeyDomain;
import io.kadai.common.internal.KadaiEngineImpl;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.common.test.security.WithAccessId;
import io.kadai.task.api.TaskQuery;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.models.TaskSummary;
import java.lang.reflect.Field;
import java.util.List;
import org.apache.ibatis.session.SqlSessionManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Acceptance test for all "query tasks by workbasket with pagination" scenarios. */
@ExtendWith(JaasExtension.class)
class QueryTasksWithPaginationAccTest extends AbstractAccTest {

  @Nested
  class PaginationTest {

    @WithAccessId(user = "admin")
    @Test
    void testQueryAllPaged() {
      TaskQuery taskQuery = kadaiEngine.getTaskService().createTaskQuery();
      long numberOfTasks = taskQuery.count();
      assertThat(numberOfTasks).isEqualTo(100);
      List<TaskSummary> tasks = taskQuery.orderByDue(DESCENDING).list();
      assertThat(tasks).hasSize(100);
      List<TaskSummary> tasksp = taskQuery.orderByDue(DESCENDING).listPage(4, 5);
      assertThat(tasksp).hasSize(5);
      tasksp = taskQuery.orderByDue(DESCENDING).listPage(5, 5);
      assertThat(tasksp).hasSize(5);
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class OffsetAndLimit {
      @WithAccessId(user = "teamlead-1")
      @Test
      void testGetFirstPageOfTaskQueryWithOffset() {
        TaskService taskService = kadaiEngine.getTaskService();
        List<TaskSummary> results = taskService.createTaskQuery().list(0, 10);
        assertThat(results).hasSize(10);
      }

      @WithAccessId(user = "teamlead-1")
      @Test
      void testSecondPageOfTaskQueryWithOffset() {
        TaskService taskService = kadaiEngine.getTaskService();
        List<TaskSummary> results = taskService.createTaskQuery().list(10, 10);
        assertThat(results).hasSize(10);
      }

      @WithAccessId(user = "teamlead-1")
      @Test
      void testListOffsetAndLimitOutOfBounds() {
        TaskService taskService = kadaiEngine.getTaskService();

        // both will be 0, working
        List<TaskSummary> results = taskService.createTaskQuery().list(-1, -3);
        assertThat(results).isEmpty();

        // limit will be 0
        results =
            taskService
                .createTaskQuery()
                .workbasketKeyDomainIn(new KeyDomain("GPK_KSC", "DOMAIN_A"))
                .list(1, -3);
        assertThat(results).isEmpty();

        // offset will be 0
        results =
            taskService
                .createTaskQuery()
                .workbasketKeyDomainIn(new KeyDomain("GPK_KSC", "DOMAIN_A"))
                .list(-1, 3);
        assertThat(results).hasSize(3);
      }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class ListPage {
      @WithAccessId(user = "teamlead-1")
      @Test
      void testPaginationWithPages() {
        TaskService taskService = kadaiEngine.getTaskService();

        // Getting full page
        int pageNumber = 2;
        int pageSize = 4;
        List<TaskSummary> results =
            taskService
                .createTaskQuery()
                .workbasketKeyDomainIn(new KeyDomain("GPK_KSC", "DOMAIN_A"))
                .listPage(pageNumber, pageSize);
        assertThat(results).hasSize(4);

        // Getting full page
        pageNumber = 4;
        pageSize = 1;
        results =
            taskService
                .createTaskQuery()
                .workbasketKeyDomainIn(new KeyDomain("GPK_KSC", "DOMAIN_A"))
                .listPage(pageNumber, pageSize);
        assertThat(results).hasSize(1);

        // Getting last results on 1 big page
        pageNumber = 1;
        pageSize = 100;
        results =
            taskService
                .createTaskQuery()
                .workbasketKeyDomainIn(new KeyDomain("GPK_KSC", "DOMAIN_A"))
                .listPage(pageNumber, pageSize);
        assertThat(results).hasSize(22);

        // Getting last results on multiple pages
        pageNumber = 3;
        pageSize = 10;
        results =
            taskService
                .createTaskQuery()
                .workbasketKeyDomainIn(new KeyDomain("GPK_KSC", "DOMAIN_A"))
                .listPage(pageNumber, pageSize);
        assertThat(results).hasSize(2);
      }

      @WithAccessId(user = "teamlead-1")
      @Test
      void testPaginationNullAndNegativeLimitsIgnoring() {
        TaskService taskService = kadaiEngine.getTaskService();

        // 0 limit/size = 0 results
        int pageNumber = 2;
        int pageSize = 0;
        List<TaskSummary> results =
            taskService
                .createTaskQuery()
                .workbasketKeyDomainIn(new KeyDomain("GPK_KSC", "DOMAIN_A"))
                .listPage(pageNumber, pageSize);
        assertThat(results).isEmpty();

        // Negative size will be 0 = 0 results
        pageNumber = 2;
        pageSize = -1;
        results =
            taskService
                .createTaskQuery()
                .workbasketKeyDomainIn(new KeyDomain("GPK_KSC", "DOMAIN_A"))
                .listPage(pageNumber, pageSize);
        assertThat(results).isEmpty();

        // Negative page = first page
        pageNumber = -1;
        pageSize = 10;
        results =
            taskService
                .createTaskQuery()
                .workbasketKeyDomainIn(new KeyDomain("GPK_KSC", "DOMAIN_A"))
                .listPage(pageNumber, pageSize);
        assertThat(results).hasSize(10);
      }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class Count {
      @WithAccessId(user = "teamlead-1")
      @Test
      void testCountOfTaskQuery() {
        TaskService taskService = kadaiEngine.getTaskService();
        long count = taskService.createTaskQuery().count();
        assertThat(count).isEqualTo(26L);
      }
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class PhysicalPagination {

    @BeforeAll
    void setup() throws Exception {
      Field sessionManagerField = KadaiEngineImpl.class.getDeclaredField("sessionManager");
      sessionManagerField.setAccessible(true);
      SqlSessionManager sessionManager = (SqlSessionManager) sessionManagerField.get(kadaiEngine);
      sessionManager
          .getConfiguration()
          .addInterceptor(new ParameterizedQuerySqlCaptureInterceptor());
    }

    @ParameterizedTest
    @CsvSource({"0,10", "5,10", "0,0", "2,4"})
    void should_UseNativeSql_For_QueryPagination(int offset, int limit) {
      ParameterizedQuerySqlCaptureInterceptor.resetCapturedSql();
      taskService.createTaskQuery().list(offset, limit);
      final String sql = ParameterizedQuerySqlCaptureInterceptor.getCapturedSql();
      final String physicalPattern1 = String.format("LIMIT %d OFFSET %d", limit, offset);
      final String physicalPattern2 =
          String.format("OFFSET %d ROWS FETCH FIRST %d ROWS ONLY", offset, limit);

      assertThat(sql).containsAnyOf(physicalPattern1, physicalPattern2);
    }
  }
}
