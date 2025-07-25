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

import static org.assertj.core.api.Assertions.assertThat;

import acceptance.AbstractAccTest;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.common.test.security.WithAccessId;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.models.TaskSummary;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

/** Acceptance test for task queries and authorization. */
@ExtendWith(JaasExtension.class)
class QueryTasksByRoleAccTest extends AbstractAccTest {

  @Nested
  class RoleTest {

    @Test
    void should_ReturnNoResult_When_UserIsNotAuthenticated() {
      TaskService taskService = kadaiEngine.getTaskService();

      List<TaskSummary> results = taskService.createTaskQuery().list();

      assertThat(results).isEmpty();
    }

    @WithAccessId(user = "admin")
    @WithAccessId(user = "taskadmin")
    @WithAccessId(user = "businessadmin")
    @WithAccessId(user = "monitor")
    @WithAccessId(user = "teamlead-1")
    @WithAccessId(user = "user-1-1")
    @WithAccessId(user = "user-taskrouter")
    @TestTemplate
    void should_FindAllAccessibleTasksDependentOnTheUser_When_MakingTaskQuery() {
      TaskService taskService = kadaiEngine.getTaskService();
      List<TaskSummary> results = taskService.createTaskQuery().list();

      int expectedSize =
          switch (kadaiEngine.getCurrentUserContext().getUserContext().getUserId()) {
            case "admin", "taskadmin" -> 100;
            case "businessadmin", "monitor" -> 0;
            case "teamlead-1" -> 26;
            case "user-1-1" -> 10;
            case "user-taskrouter" -> 0;
            default ->
                throw new SystemException(
                    String.format(
                        "Invalid User: '%s'",
                        kadaiEngine.getCurrentUserContext().getUserContext().getUserId()));
          };

      assertThat(results).hasSize(expectedSize);
    }
  }
}
