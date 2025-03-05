/*
 * Copyright [2024] [envite consulting GmbH]
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
import io.kadai.common.internal.util.Pair;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.common.test.security.WithAccessId;
import io.kadai.simplehistory.task.internal.TaskHistoryQueryImpl;
import io.kadai.simplehistory.task.internal.TaskHistoryQueryMapper;
import io.kadai.spi.history.api.events.task.TaskHistoryEvent;
import io.kadai.spi.history.api.events.task.TaskHistoryEventType;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.TaskState;
import io.kadai.task.api.models.Task;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.ThrowingConsumer;

@ExtendWith(JaasExtension.class)
class CreateHistoryEventOnTaskClaimAccTest extends AbstractAccTest {

  private final TaskService taskService = kadaiEngine.getTaskService();
  private final SimpleHistoryServiceImpl historyService = getHistoryService();

  @WithAccessId(user = "admin")
  @TestFactory
  Stream<DynamicTest> should_CreateClaimedHistoryEvent_When_TaskIsClaimed() {
    List<Pair<String, String>> list =
        List.of(
            Pair.of(
                "With Attachment and secondary Object Reference",
                "TKI:000000000000000000000000000000000053"),
            Pair.of(
                "Without Attachment and secondary Object References",
                "TKI:000000000000000000000000000000000047"));

    ThrowingConsumer<Pair<String, String>> test =
        pair -> {
          final Instant oldModified = taskService.getTask(pair.getRight()).getModified();

          TaskHistoryQueryMapper taskHistoryQueryMapper = getHistoryQueryMapper();

          List<TaskHistoryEvent> events =
              taskHistoryQueryMapper.queryHistoryEvents(
                  (TaskHistoryQueryImpl)
                      historyService.createTaskHistoryQuery().taskIdIn(pair.getRight()));

          assertThat(events).isEmpty();

          assertThat(taskService.getTask(pair.getRight()).getState()).isEqualTo(TaskState.READY);
          Task task = taskService.claim(pair.getRight());

          assertThat(task.getState()).isEqualTo(TaskState.CLAIMED);

          events =
              taskHistoryQueryMapper.queryHistoryEvents(
                  (TaskHistoryQueryImpl)
                      historyService.createTaskHistoryQuery().taskIdIn(pair.getRight()));

          TaskHistoryEvent event = events.get(0);

          assertThat(event.getEventType()).isEqualTo(TaskHistoryEventType.CLAIMED.getName());

          event = historyService.getTaskHistoryEvent(event.getId());

          assertThat(event.getDetails()).isNotNull();

          JSONArray changes = new JSONObject(event.getDetails()).getJSONArray("changes");

          JSONObject expectedClaimed =
              new JSONObject()
                  .put("newValue", task.getModified().toString())
                  .put("fieldName", "claimed")
                  .put("oldValue", "");
          JSONObject expectedModified =
              new JSONObject()
                  .put("newValue", task.getModified().toString())
                  .put("fieldName", "modified")
                  .put("oldValue", oldModified.toString());
          JSONObject expectedState =
              new JSONObject()
                  .put("newValue", TaskState.CLAIMED.name())
                  .put("fieldName", "state")
                  .put("oldValue", TaskState.READY.name());
          JSONObject expectedOwner =
              new JSONObject()
                  .put("newValue", "admin")
                  .put("fieldName", "owner")
                  .put("oldValue", "");
          JSONObject expectedIsRead =
              new JSONObject()
                  .put("newValue", true)
                  .put("fieldName", "isRead")
                  .put("oldValue", false);

          JSONArray expectedChanges =
              new JSONArray()
                  .put(expectedClaimed)
                  .put(expectedModified)
                  .put(expectedState)
                  .put(expectedOwner)
                  .put(expectedIsRead);

          assertThat(changes.similar(expectedChanges)).isTrue();
        };
    return DynamicTest.stream(list.iterator(), Pair::getLeft, test);
  }
}
