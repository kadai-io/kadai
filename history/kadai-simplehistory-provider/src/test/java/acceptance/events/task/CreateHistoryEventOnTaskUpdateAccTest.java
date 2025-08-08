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
import io.kadai.common.api.TimeInterval;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.common.test.security.WithAccessId;
import io.kadai.simplehistory.impl.SimpleHistoryServiceImpl;
import io.kadai.spi.history.api.events.task.TaskHistoryEvent;
import io.kadai.spi.history.api.events.task.TaskHistoryEventType;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.models.Task;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JaasExtension.class)
class CreateHistoryEventOnTaskUpdateAccTest extends AbstractAccTest {

  private final TaskService taskService = kadaiEngine.getTaskService();
  private final SimpleHistoryServiceImpl historyService = getHistoryService();

  @Test
  @WithAccessId(user = "admin")
  void should_CreateUpdatedHistoryEvent_When_TaskIsCreated() throws Exception {
    final String taskId = "TKI:000000000000000000000000000000000000";
    Instant before = Instant.now();

    Task task = taskService.getTask(taskId);
    task.setName("someUpdatedName");
    taskService.updateTask(task);
    List<TaskHistoryEvent> events =
        historyService
            .createTaskHistoryQuery()
            .taskIdIn(taskId)
            .createdWithin(new TimeInterval(before, null))
            .list();

    assertThat(events)
        .extracting(TaskHistoryEvent::getEventType)
        .containsExactly(TaskHistoryEventType.UPDATED.getName());
    assertThat(events).extracting(TaskHistoryEvent::getUserId).containsExactly("admin");
    assertThat(events)
        .extracting(TaskHistoryEvent::getProxyAccessId)
        .containsExactly((String) null);
  }
}
