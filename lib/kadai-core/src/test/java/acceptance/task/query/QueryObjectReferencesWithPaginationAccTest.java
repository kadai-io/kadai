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

import acceptance.AbstractAccTest;
import acceptance.ParameterizedQuerySqlCaptureInterceptor;
import io.kadai.common.internal.KadaiEngineImpl;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.task.api.ObjectReferenceQuery;
import io.kadai.task.api.TaskQuery;
import io.kadai.task.api.models.ObjectReference;
import java.lang.reflect.Field;
import java.util.List;
import org.apache.ibatis.session.SqlSessionManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Acceptance test for all "query classifications with pagination" scenarios. */
@ExtendWith(JaasExtension.class)
class QueryObjectReferencesWithPaginationAccTest extends AbstractAccTest {

  private ObjectReferenceQuery objRefQuery;

  @BeforeEach
  void before() {
    TaskQuery taskQuery = kadaiEngine.getTaskService().createTaskQuery();
    objRefQuery = taskQuery.createObjectReferenceQuery();
  }

  @Test
  void testGetFirstPageOfObjectRefQueryWithOffset() {

    List<ObjectReference> results = objRefQuery.list(0, 5);
    assertThat(results).hasSize(4);
  }

  @Test
  void testGetSecondPageOfObjectRefQueryWithOffset() {
    List<ObjectReference> results = objRefQuery.list(2, 5);
    assertThat(results).hasSize(2);
  }

  @Test
  void testListOffsetAndLimitOutOfBounds() {
    // both will be 0, working
    List<ObjectReference> results = objRefQuery.list(-1, -3);
    assertThat(results).isEmpty();

    // limit will be 0
    results = objRefQuery.list(1, -3);
    assertThat(results).isEmpty();

    // offset will be 0
    results = objRefQuery.list(-1, 3);
    assertThat(results).hasSize(3);
  }

  @Test
  void testPaginationWithPages() {
    // Getting full page
    int pageNumber = 1;
    int pageSize = 10;
    List<ObjectReference> results = objRefQuery.listPage(pageNumber, pageSize);
    assertThat(results).hasSize(4);

    // Getting full page
    pageNumber = 2;
    pageSize = 2;
    results = objRefQuery.listPage(pageNumber, pageSize);
    assertThat(results).hasSize(2);

    // Getting last results on 1 big page
    pageNumber = 1;
    pageSize = 100;
    results = objRefQuery.listPage(pageNumber, pageSize);
    assertThat(results).hasSize(4);
  }

  @Test
  void testPaginationNullAndNegativeLimitsIgnoring() {
    // 0 limit/size = 0 results
    int pageNumber = 2;
    int pageSize = 0;
    List<ObjectReference> results = objRefQuery.listPage(pageNumber, pageSize);
    assertThat(results).isEmpty();

    // Negative will be 0 = all results
    pageNumber = 2;
    pageSize = -1;
    results = objRefQuery.listPage(pageNumber, pageSize);
    assertThat(results).isEmpty();

    // Negative page = first page
    pageNumber = -1;
    pageSize = 10;
    results = objRefQuery.listPage(pageNumber, pageSize);
    assertThat(results).hasSize(4);
  }

  @Test
  void testCountOfClassificationsQuery() {
    long count = objRefQuery.count();
    assertThat(count).isEqualTo(4L);
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
      kadaiEngine
          .getTaskService()
          .createTaskQuery()
          .createObjectReferenceQuery()
          .list(offset, limit);
      final String sql = ParameterizedQuerySqlCaptureInterceptor.getCapturedSql();
      final String physicalPattern1 = String.format("LIMIT %d OFFSET %d", limit, offset);
      final String physicalPattern2 =
          String.format("OFFSET %d ROWS FETCH FIRST %d ROWS ONLY", offset, limit);

      assertThat(sql).containsAnyOf(physicalPattern1, physicalPattern2);
    }
  }
}
