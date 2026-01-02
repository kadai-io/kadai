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

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import acceptance.AbstractAccTest;
import io.kadai.common.api.KeyDomain;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.common.test.security.WithAccessId;
import io.kadai.task.api.TaskService;
import io.kadai.workbasket.api.exceptions.NotAuthorizedToQueryWorkbasketException;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

/** Acceptance test for all "query tasks by workbasket" scenarios. */
@ExtendWith(JaasExtension.class)
class QueryTasksByWorkbasketAccTest extends AbstractAccTest {

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class WorkbasketTest {
    @WithAccessId(user = "user-1-1")
    @Test
    void testThrowsExceptionIfNoOpenerPermissionOnQueriedWorkbasket() {
      TaskService taskService = kadaiEngine.getTaskService();

      ThrowingCallable call =
          () ->
              taskService
                  .createTaskQuery()
                  .workbasketKeyDomainIn(new KeyDomain("USER-2-1", "DOMAIN_A"))
                  .list();
      assertThatThrownBy(call).isInstanceOf(NotAuthorizedToQueryWorkbasketException.class);
    }

    @WithAccessId(user = "user-1-1")
    @Test
    void testThrowsExceptionIfNoOpenerPermissionOnAtLeastOneQueriedWorkbasket() {
      TaskService taskService = kadaiEngine.getTaskService();
      ThrowingCallable call =
          () ->
              taskService
                  .createTaskQuery()
                  .workbasketKeyDomainIn(
                      new KeyDomain("USER-1-1", "DOMAIN_A"), new KeyDomain("USER-2-1", "DOMAIN_A"))
                  .list();
      assertThatThrownBy(call).isInstanceOf(NotAuthorizedToQueryWorkbasketException.class);
    }
  }
}
