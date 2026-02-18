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

package acceptance.events.task;

import static org.assertj.core.api.Assertions.assertThat;

import acceptance.AbstractAccTest;
import io.kadai.common.internal.util.CheckedRunnable;
import io.kadai.spi.history.api.events.task.TaskHistoryEvent;
import io.kadai.spi.history.api.events.task.TaskHistoryEventType;
import io.kadai.task.api.TaskService;
import java.util.List;
import org.junit.jupiter.api.Test;

class CreateHistoryEventOnTaskTerminationAccTest extends AbstractAccTest {

  private final TaskService taskService = kadaiEngine.getTaskService();

  @Test
  void should_CreateTerminatedHistoryEvent_When_TerminatingTaskInStateClaimed() throws Exception {
    kadaiEngine.runAsAdmin(
        CheckedRunnable.rethrowing(
            () -> {
              final String taskId = "TKI:000000000000000000000000000000000001";

              List<TaskHistoryEvent> events =
                  taskHistoryService.createTaskHistoryQuery().taskIdIn(taskId).list();

              assertThat(events).isEmpty();

              taskService.terminateTask(taskId);

              events = taskHistoryService.createTaskHistoryQuery().taskIdIn(taskId).list();

              assertThat(events).hasSize(1);

              String eventType = events.get(0).getEventType();

              assertThat(eventType).isEqualTo(TaskHistoryEventType.TERMINATED.getName());
              assertThat(events.get(0).getUserId()).isEqualTo("user-1-3");
              assertThat(events.get(0).getProxyAccessId()).isEqualTo("admin");
            }),
        "user-1-3");
  }

  @Test
  void should_CreateTerminatedHistoryEvent_When_TerminatingTaskInStateReady() throws Exception {
    kadaiEngine.runAsAdmin(
        CheckedRunnable.rethrowing(
            () -> {
              final String taskId = "TKI:000000000000000000000000000000000003";

              List<TaskHistoryEvent> events =
                  taskHistoryService.createTaskHistoryQuery().taskIdIn(taskId).list();

              assertThat(events).isEmpty();

              taskService.terminateTask(taskId);

              events = taskHistoryService.createTaskHistoryQuery().taskIdIn(taskId).list();

              assertThat(events).hasSize(1);

              String eventType = events.get(0).getEventType();

              assertThat(eventType).isEqualTo(TaskHistoryEventType.TERMINATED.getName());
              assertThat(events.get(0).getUserId()).isEqualTo("teamlead-1");
              assertThat(events.get(0).getProxyAccessId()).isEqualTo("admin");
            }),
        "teamlead-1");
  }
}
