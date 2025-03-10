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

package acceptance.user.query;

import static org.assertj.core.api.Assertions.assertThat;

import acceptance.AbstractAccTest;
import io.kadai.common.api.BaseQuery.SortDirection;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.user.api.UserService;
import io.kadai.user.api.models.UserSummary;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

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

  @Test
  void testListOffsetAndLimitOutOfBounds() {
    UserService userService = kadaiEngine.getUserService();

    // both will be 0, working
    List<UserSummary> results = userService.createUserQuery().list(-1, -3);
    assertThat(results).isEmpty();

    // limit will be 0
    results = userService.createUserQuery().list(1, -3);
    assertThat(results).isEmpty();

    // offset will be 0
    results = userService.createUserQuery().list(-1, 3);
    assertThat(results).hasSize(3);
  }

  @Test
  void testPaginationWithPages() {
    UserService userService = kadaiEngine.getUserService();

    // Getting full page
    int pageNumber = 2;
    int pageSize = 4;
    List<UserSummary> results = userService.createUserQuery().listPage(pageNumber, pageSize);
    assertThat(results).hasSize(4);

    // Getting full page
    pageNumber = 4;
    pageSize = 1;
    results = userService.createUserQuery().listPage(pageNumber, pageSize);
    assertThat(results).hasSize(1);

    // Getting last results on 1 big page
    pageNumber = 1;
    pageSize = 100;
    results = userService.createUserQuery().listPage(pageNumber, pageSize);
    assertThat(results).hasSize(ALL_USERS_COUNT);

    // Getting last results on multiple pages
    pageNumber = (ALL_USERS_COUNT - ALL_USERS_COUNT % 5) / 5 + 1;
    pageSize = 5;
    results = userService.createUserQuery().listPage(pageNumber, pageSize);
    assertThat(results).hasSize(ALL_USERS_COUNT % 5);
  }

  @Test
  void testPaginationNullAndNegativeLimitsIgnoring() {
    UserService userService = kadaiEngine.getUserService();

    // 0 limit/size = 0 results
    int pageNumber = 2;
    int pageSize = 0;
    List<UserSummary> results = userService.createUserQuery().listPage(pageNumber, pageSize);
    assertThat(results).isEmpty();

    // Negative size will be 0 = 0 results
    pageNumber = 2;
    pageSize = -1;
    results = userService.createUserQuery().listPage(pageNumber, pageSize);
    assertThat(results).isEmpty();

    // Negative page = first page
    pageNumber = -1;
    pageSize = 10;
    results = userService.createUserQuery().listPage(pageNumber, pageSize);
    assertThat(results).hasSize(10);
  }

  @Test
  void testGetFirstPageOfUserQueryWithSorting() {
    UserService userService = kadaiEngine.getUserService();
    List<UserSummary> results =
        userService.createUserQuery().orderByFirstName(SortDirection.ASCENDING).list(0, 5);
    assertThat(results).hasSize(5);
    assertThat(results.get(0).getFirstName()).isEqualTo("Alice");
    assertThat(results.get(4).getFirstName()).isEqualTo("Elena");

    results = userService.createUserQuery().orderByFirstName(SortDirection.DESCENDING).list(0, 5);
    assertThat(results).hasSize(5);
    assertThat(results.get(0).getFirstName()).isEqualTo("Wiebke");
    assertThat(results.get(4).getFirstName()).isEqualTo("Simone");
  }
}
