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
import io.kadai.common.api.KadaiRole;
import io.kadai.common.internal.util.CheckedRunnable;
import io.kadai.simplehistory.impl.SimpleHistoryServiceImpl;
import io.kadai.simplehistory.impl.TaskHistoryQueryImpl;
import io.kadai.simplehistory.impl.task.TaskHistoryQueryMapper;
import io.kadai.spi.history.api.events.task.TaskHistoryEvent;
import io.kadai.spi.history.api.events.task.TaskHistoryEventType;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.TaskState;
import io.kadai.task.api.models.Task;
import java.util.List;
import org.junit.jupiter.api.Test;

class CreateHistoryEventOnTaskCompletionAccTest extends AbstractAccTest {

  private final TaskService taskService = kadaiEngine.getTaskService();
  private final SimpleHistoryServiceImpl historyService = getHistoryService();

  @Test
  void should_CreateCompletedHistoryEvent_When_TaskIsCompleted() throws Exception {
    kadaiEngine.runAs(
        CheckedRunnable.rethrowing(
            () -> {
              final String taskId = "TKI:000000000000000000000000000000000001";

              TaskHistoryQueryMapper taskHistoryQueryMapper = getHistoryQueryMapper();

              List<TaskHistoryEvent> events =
                  taskHistoryQueryMapper.queryHistoryEvents(
                      (TaskHistoryQueryImpl)
                          historyService.createTaskHistoryQuery().taskIdIn(taskId));

              assertThat(events).isEmpty();

              assertThat(taskService.getTask(taskId).getState()).isEqualTo(TaskState.CLAIMED);
              Task task = taskService.forceCompleteTask(taskId);
              assertThat(task.getState()).isEqualTo(TaskState.COMPLETED);

              events =
                  taskHistoryQueryMapper.queryHistoryEvents(
                      (TaskHistoryQueryImpl)
                          historyService.createTaskHistoryQuery().taskIdIn(taskId));

              String eventType = events.get(0).getEventType();

              assertThat(eventType).isEqualTo(TaskHistoryEventType.COMPLETED.getName());
              assertThat(events.get(0).getUserId()).isEqualTo("user-4-4");
              assertThat(events.get(0).getProxyAccessId()).isEqualTo("taskadmin");
            }),
        KadaiRole.TASK_ADMIN,
        "user-4-4");
  }
}
