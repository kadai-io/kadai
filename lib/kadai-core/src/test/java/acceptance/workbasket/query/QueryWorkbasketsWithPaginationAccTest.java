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

package acceptance.workbasket.query;

import static org.assertj.core.api.Assertions.assertThat;

import acceptance.AbstractAccTest;
import acceptance.ParameterizedQuerySqlCaptureInterceptor;
import io.kadai.common.internal.KadaiEngineImpl;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.common.test.security.WithAccessId;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.models.WorkbasketSummary;
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

/** Acceptance test for all "query classifications with pagination" scenarios. */
@ExtendWith(JaasExtension.class)
class QueryWorkbasketsWithPaginationAccTest extends AbstractAccTest {

  QueryWorkbasketsWithPaginationAccTest() {
    super();
  }

  @WithAccessId(user = "teamlead-1", groups = GROUP_1_DN)
  @Test
  void testGetFirstPageOfWorkbasketQueryWithOffset() {
    WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
    List<WorkbasketSummary> results =
        workbasketService.createWorkbasketQuery().domainIn("DOMAIN_A").list(0, 5);
    assertThat(results).hasSize(5);
  }

  @WithAccessId(user = "teamlead-1", groups = GROUP_1_DN)
  @Test
  void testGetSecondPageOfWorkbasketQueryWithOffset() {
    WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
    List<WorkbasketSummary> results =
        workbasketService.createWorkbasketQuery().domainIn("DOMAIN_A").list(5, 5);
    assertThat(results).hasSize(4);
  }

  @WithAccessId(user = "teamlead-1")
  @Test
  void testListOffsetAndLimitOutOfBounds() {
    WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();

    // both will be 0, working
    List<WorkbasketSummary> results =
        workbasketService.createWorkbasketQuery().domainIn("DOMAIN_A").list(-1, -3);
    assertThat(results).isEmpty();

    // limit will be 0
    results = workbasketService.createWorkbasketQuery().domainIn("DOMAIN_A").list(1, -3);
    assertThat(results).isEmpty();

    // offset will be 0
    results = workbasketService.createWorkbasketQuery().domainIn("DOMAIN_A").list(-1, 3);
    assertThat(results).hasSize(3);
  }

  @WithAccessId(user = "teamlead-1", groups = GROUP_1_DN)
  @Test
  void testPaginationWithPages() {
    WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();

    // Getting full page
    int pageNumber = 2;
    int pageSize = 4;
    List<WorkbasketSummary> results =
        workbasketService
            .createWorkbasketQuery()
            .domainIn("DOMAIN_A")
            .listPage(pageNumber, pageSize);
    assertThat(results).hasSize(4);

    // Getting full page
    pageNumber = 4;
    pageSize = 1;
    results =
        workbasketService
            .createWorkbasketQuery()
            .domainIn("DOMAIN_A")
            .listPage(pageNumber, pageSize);
    assertThat(results).hasSize(1);

    // Getting last results on 1 big page
    pageNumber = 1;
    pageSize = 100;
    results =
        workbasketService
            .createWorkbasketQuery()
            .domainIn("DOMAIN_A")
            .listPage(pageNumber, pageSize);
    assertThat(results).hasSize(9);

    // Getting last results on multiple pages
    pageNumber = 2;
    pageSize = 5;
    results =
        workbasketService
            .createWorkbasketQuery()
            .domainIn("DOMAIN_A")
            .listPage(pageNumber, pageSize);
    assertThat(results).hasSize(4);
  }

  @WithAccessId(user = "teamlead-1", groups = GROUP_1_DN)
  @Test
  void testPaginationNullAndNegativeLimitsIgnoring() {
    WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();

    // 0 limit/size = 0 results
    int pageNumber = 2;
    int pageSize = 0;
    List<WorkbasketSummary> results =
        workbasketService
            .createWorkbasketQuery()
            .domainIn("DOMAIN_A")
            .listPage(pageNumber, pageSize);
    assertThat(results).isEmpty();

    // Negative size will be 0 = 0 results
    pageNumber = 2;
    pageSize = -1;
    results =
        workbasketService
            .createWorkbasketQuery()
            .domainIn("DOMAIN_A")
            .listPage(pageNumber, pageSize);
    assertThat(results).isEmpty();

    // Negative page = first page
    pageNumber = -1;
    pageSize = 10;
    results =
        workbasketService
            .createWorkbasketQuery()
            .domainIn("DOMAIN_A")
            .listPage(pageNumber, pageSize);
    assertThat(results).hasSize(9);
  }

  @WithAccessId(user = "teamlead-1", groups = GROUP_1_DN)
  @Test
  void testCountOfWorkbasketQuery() {
    WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
    long count = workbasketService.createWorkbasketQuery().domainIn("DOMAIN_A").count();
    assertThat(count).isEqualTo(9L);
  }

  @WithAccessId(user = "teamlead-1", groups = GROUP_1_DN)
  @Test
  void testWorkbasketQueryDomA() {
    WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
    List<WorkbasketSummary> result =
        workbasketService.createWorkbasketQuery().domainIn("DOMAIN_A").list();
    assertThat(result).hasSize(9);
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
      kadaiEngine.getWorkbasketService().createWorkbasketQuery().list(offset, limit);
      final String sql = ParameterizedQuerySqlCaptureInterceptor.getCapturedSql();
      final String physicalPattern1 = String.format("LIMIT %d OFFSET %d", limit, offset);
      final String physicalPattern2 =
          String.format("OFFSET %d ROWS FETCH FIRST %d ROWS ONLY", offset, limit);

      assertThat(sql).containsAnyOf(physicalPattern1, physicalPattern2);
    }
  }
}
