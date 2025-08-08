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
import io.kadai.simplehistory.impl.TaskHistoryQueryImpl;
import io.kadai.simplehistory.impl.task.TaskHistoryQueryMapper;
import io.kadai.spi.history.api.events.task.TaskHistoryEvent;
import io.kadai.spi.history.api.events.task.TaskHistoryEventType;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.TaskState;
import io.kadai.task.api.models.Task;
import java.time.Instant;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JaasExtension.class)
class CreateHistoryEventOnTaskRequestReviewAccTest extends AbstractAccTest {

  private final TaskService taskService = kadaiEngine.getTaskService();
  private final SimpleHistoryServiceImpl historyService = getHistoryService();

  @WithAccessId(user = "user-1-1")
  @Test
  void should_CreateRequestReviewHistoryEvent_When_ReviewIsRequested() throws Exception {
    final String taskId = "TKI:200000000000000000000000000000000066";
    final Instant oldModified = taskService.getTask(taskId).getModified();

    TaskHistoryQueryMapper taskHistoryQueryMapper = getHistoryQueryMapper();

    List<TaskHistoryEvent> events =
        taskHistoryQueryMapper.queryHistoryEvents(
            (TaskHistoryQueryImpl) historyService.createTaskHistoryQuery().taskIdIn(taskId));

    assertThat(events).isEmpty();

    assertThat(taskService.getTask(taskId).getState()).isEqualTo(TaskState.CLAIMED);
    Task task = taskService.requestReview(taskId);

    assertThat(task.getState()).isEqualTo(TaskState.READY_FOR_REVIEW);

    events =
        taskHistoryQueryMapper.queryHistoryEvents(
            (TaskHistoryQueryImpl) historyService.createTaskHistoryQuery().taskIdIn(taskId));

    TaskHistoryEvent event = events.get(0);

    assertThat(event.getEventType()).isEqualTo(TaskHistoryEventType.REQUESTED_REVIEW.getName());

    event = historyService.getTaskHistoryEvent(event.getId());

    assertThat(event.getDetails()).isNotNull();
    assertThat(event.getUserId()).isEqualTo("user-1-1");
    assertThat(event.getProxyAccessId()).isNull();

    JSONArray changes = new JSONObject(event.getDetails()).getJSONArray("changes");

    JSONObject expectedModified =
        new JSONObject()
            .put("newValue", task.getModified().toString())
            .put("fieldName", "modified")
            .put("oldValue", oldModified.toString());
    JSONObject expectedState =
        new JSONObject()
            .put("newValue", TaskState.READY_FOR_REVIEW.name())
            .put("fieldName", "state")
            .put("oldValue", TaskState.CLAIMED.name());
    JSONObject expectedOwner =
        new JSONObject().put("newValue", "").put("fieldName", "owner").put("oldValue", "user-1-1");

    JSONArray expectedChanges =
        new JSONArray().put(expectedModified).put(expectedState).put(expectedOwner);
    assertThat(changes.similar(expectedChanges)).isTrue();
  }
}
