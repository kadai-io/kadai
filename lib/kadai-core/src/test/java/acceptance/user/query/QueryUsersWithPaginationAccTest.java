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

package acceptance.user.query;

import static org.assertj.core.api.Assertions.assertThat;

import acceptance.AbstractAccTest;
import acceptance.ParameterizedQuerySqlCaptureInterceptor;
import io.kadai.common.api.BaseQuery.SortDirection;
import io.kadai.common.internal.KadaiEngineImpl;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.user.api.UserService;
import io.kadai.user.api.models.UserSummary;
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

/** Acceptance test for all "query users with pagination" scenarios. */
@ExtendWith(JaasExtension.class)
class QueryUsersWithPaginationAccTest extends AbstractAccTest {

  private static final int ALL_USERS_COUNT = 18;

  QueryUsersWithPaginationAccTest() {
    super();
  }

  @Test
  void testGetFirstPageOfUserQueryWithOffset() {
    UserService userService = kadaiEngine.getUserService();
    List<UserSummary> results = userService.createUserQuery().list(0, 5);
    assertThat(results).hasSize(5);
  }

  @Test
  void testGetSecondPageOfUserQueryWithOffset() {
    UserService userService = kadaiEngine.getUserService();
    List<UserSummary> results = userService.createUserQuery().list(ALL_USERS_COUNT - 4, 5);
    assertThat(results).hasSize(4);
  }

  @ParameterizedTest
  @CsvSource({"-1, -3, 0", "1, -3, 0", "-1, 3, 3"})
  void testListOffsetAndLimitOutOfBounds(int offset, int limit, int expectedSize) {
    UserService userService = kadaiEngine.getUserService();

    List<UserSummary> results = userService.createUserQuery().list(offset, limit);
    assertThat(results).hasSize(expectedSize);
  }

  @ParameterizedTest
  @CsvSource({"2, 4, 4", "4, 1, 1", "1, 100, 18", "4, 5, 3", "2, 0, 0", "2, -1, 0", "-1, 10, 10"})
  void testPaginationWithPages(int pageNumber, int pageSize, int expectedSize) {
    UserService userService = kadaiEngine.getUserService();

    List<UserSummary> results = userService.createUserQuery().listPage(pageNumber, pageSize);
    assertThat(results).hasSize(expectedSize);
  }

  @ParameterizedTest
  @CsvSource({"ASCENDING, Alice, Elena", "DESCENDING, Wiebke, Simone"})
  void testGetFirstPageOfUserQueryWithSorting(
      SortDirection sortDirection, String firstName, String fourthName) {
    UserService userService = kadaiEngine.getUserService();
    List<UserSummary> results =
        userService.createUserQuery().orderByFirstName(sortDirection).list(0, 5);
    assertThat(results).hasSize(5);
    assertThat(results.get(0).getFirstName()).isEqualTo(firstName);
    assertThat(results.get(4).getFirstName()).isEqualTo(fourthName);
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
      kadaiEngine.getUserService().createUserQuery().list(offset, limit);
      final String sql = ParameterizedQuerySqlCaptureInterceptor.getCapturedSql();
      final String physicalPattern1 = String.format("LIMIT %d OFFSET %d", limit, offset);
      final String physicalPattern2 =
          String.format("OFFSET %d ROWS FETCH FIRST %d ROWS ONLY", offset, limit);

      assertThat(sql).containsAnyOf(physicalPattern1, physicalPattern2);
    }
  }
}
