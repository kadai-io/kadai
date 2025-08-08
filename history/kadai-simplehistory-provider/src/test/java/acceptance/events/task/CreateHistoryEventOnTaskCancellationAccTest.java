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

package acceptance.events.task;

import static org.assertj.core.api.Assertions.assertThat;

import acceptance.AbstractAccTest;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.common.test.security.WithAccessId;
import io.kadai.simplehistory.impl.SimpleHistoryServiceImpl;
import io.kadai.spi.history.api.events.task.TaskHistoryEvent;
import io.kadai.spi.history.api.events.task.TaskHistoryEventType;
import io.kadai.task.api.TaskService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JaasExtension.class)
class CreateHistoryEventOnTaskCancellationAccTest extends AbstractAccTest {

  private final TaskService taskService = kadaiEngine.getTaskService();
  private final SimpleHistoryServiceImpl historyService = getHistoryService();

  @Test
  @WithAccessId(user = "admin")
  void should_CreateCancelledHistoryEvent_When_CancelTaskInStateClaimed() throws Exception {

    final String taskId = "TKI:000000000000000000000000000000000001";

    List<TaskHistoryEvent> listEvents =
        historyService.createTaskHistoryQuery().taskIdIn(taskId).list();

    assertThat(listEvents).isEmpty();

    taskService.cancelTask(taskId);

    listEvents = historyService.createTaskHistoryQuery().taskIdIn(taskId).list();

    assertThat(listEvents).hasSize(1);

    String eventType = listEvents.get(0).getEventType();

    assertThat(eventType).isEqualTo(TaskHistoryEventType.CANCELLED.getName());
    assertThat(listEvents.get(0).getUserId()).isEqualTo("admin");
    assertThat(listEvents.get(0).getProxyAccessId()).isNull();
  }

  @Test
  @WithAccessId(user = "admin")
  void should_CreateCancelledHistoryEvent_When_CancelTaskInStateReady() throws Exception {

    final String taskId = "TKI:000000000000000000000000000000000003";

    List<TaskHistoryEvent> events = historyService.createTaskHistoryQuery().taskIdIn(taskId).list();

    assertThat(events).isEmpty();

    taskService.cancelTask(taskId);

    events = historyService.createTaskHistoryQuery().taskIdIn(taskId).list();

    assertThat(events).hasSize(1);

    String eventType = events.get(0).getEventType();

    assertThat(eventType).isEqualTo(TaskHistoryEventType.CANCELLED.getName());
    assertThat(events.get(0).getUserId()).isEqualTo("admin");
    assertThat(events.get(0).getProxyAccessId()).isNull();
  }
}
