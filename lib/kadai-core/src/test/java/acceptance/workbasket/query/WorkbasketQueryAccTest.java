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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import acceptance.AbstractAccTest;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.common.test.security.WithAccessId;
import io.kadai.workbasket.api.WorkbasketPermission;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import java.util.List;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Acceptance test for workbasket queries and authorization. */
@ExtendWith(JaasExtension.class)
class WorkbasketQueryAccTest extends AbstractAccTest {

  @Test
  void testQueryWorkbasketByUnauthenticated() {
    WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
    List<WorkbasketSummary> results =
        workbasketService.createWorkbasketQuery().nameLike("%").list();
    assertThat(results).isEmpty();
    ThrowingCallable call =
        () -> {
          workbasketService
              .createWorkbasketQuery()
              .nameLike("%")
              .accessIdsHavePermissions(
                  List.of(WorkbasketPermission.TRANSFER),
                  "teamlead-1",
                  GROUP_1_DN,
                  GROUP_2_DN,
                  PERM_1)
              .list();
        };
    assertThatThrownBy(call).isInstanceOf(NotAuthorizedException.class);
  }

  @WithAccessId(user = "unknownuser")
  @Test
  void testQueryWorkbasketByUnknownUser() {
    WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
    List<WorkbasketSummary> results =
        workbasketService.createWorkbasketQuery().nameLike("%").list();
    assertThat(results).isEmpty();
    ThrowingCallable call =
        () -> {
          workbasketService
              .createWorkbasketQuery()
              .nameLike("%")
              .accessIdsHavePermissions(
                  List.of(WorkbasketPermission.TRANSFER),
                  "teamlead-1",
                  GROUP_1_DN,
                  GROUP_2_DN,
                  PERM_1)
              .list();
        };
    assertThatThrownBy(call).isInstanceOf(NotAuthorizedException.class);
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void testQueryWorkbasketByBusinessAdmin() throws Exception {
    WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
    List<WorkbasketSummary> results =
        workbasketService.createWorkbasketQuery().nameLike("%").list();
    assertThat(results).hasSize(26);

    results =
        workbasketService
            .createWorkbasketQuery()
            .nameLike("%")
            .accessIdsHavePermissions(
                List.of(WorkbasketPermission.TRANSFER),
                "teamlead-1",
                GROUP_1_DN,
                GROUP_2_DN,
                PERM_1)
            .list();

    assertThat(results).hasSize(13);
  }

  @WithAccessId(user = "admin")
  @Test
  void testQueryWorkbasketByAdmin() throws Exception {
    WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
    List<WorkbasketSummary> results =
        workbasketService.createWorkbasketQuery().nameLike("%").list();
    assertThat(results).hasSize(26);

    results =
        workbasketService
            .createWorkbasketQuery()
            .nameLike("%")
            .accessIdsHavePermissions(
                List.of(WorkbasketPermission.TRANSFER),
                "teamlead-1",
                GROUP_1_DN,
                GROUP_2_DN,
                PERM_1)
            .list();

    assertThat(results).hasSize(13);
  }
}
