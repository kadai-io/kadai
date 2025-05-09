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

import static io.kadai.common.internal.util.CheckedConsumer.rethrowing;
import static org.assertj.core.api.Assertions.assertThat;

import acceptance.AbstractAccTest;
import io.kadai.common.internal.util.Quadruple;
import io.kadai.common.internal.util.Triplet;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.common.test.security.WithAccessId;
import io.kadai.simplehistory.impl.SimpleHistoryServiceImpl;
import io.kadai.simplehistory.impl.TaskHistoryQueryImpl;
import io.kadai.simplehistory.impl.task.TaskHistoryQueryMapper;
import io.kadai.spi.history.api.events.task.TaskHistoryEvent;
import io.kadai.spi.history.api.events.task.TaskHistoryEventType;
import io.kadai.task.api.TaskService;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.models.Workbasket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.ThrowingConsumer;

@ExtendWith(JaasExtension.class)
class CreateHistoryEventOnTaskTransferAccTest extends AbstractAccTest {

  private final TaskService taskService = kadaiEngine.getTaskService();
  private final SimpleHistoryServiceImpl historyService = getHistoryService();

  @WithAccessId(user = "admin")
  @TestFactory
  Stream<DynamicTest> should_CreateTransferredHistoryEvent_When_TaskIsTransferred()
      throws Exception {
    List<Quadruple<String, String, String, Consumer<String>>> testCases =
        List.of(
            /*
            The workbasketId of the source Workbasket is parametrized. Putting the tested Tasks
            into the same Workbasket would result in changes to the test data. This would require
            changing tests that already use the tested Tasks. That's why workbasketId is
            parametrized.
            */
            Quadruple.of(
                "Using WorkbasketId; Task doesn't have an Attachment"
                    + " or any secondary Object References",
                "TKI:000000000000000000000000000000000003",
                "WBI:100000000000000000000000000000000001",
                rethrowing(
                    (String taskId) ->
                        taskService.transfer(taskId, "WBI:100000000000000000000000000000000006"))),
            Quadruple.of(
                "Using WorkbasketId; Task has Attachment and secondary Object Reference",
                "TKI:000000000000000000000000000000000053",
                "WBI:100000000000000000000000000000000015",
                rethrowing(
                    (String taskId) ->
                        taskService.transfer(taskId, "WBI:100000000000000000000000000000000006"))),
            Quadruple.of(
                "Using WorkbasketKey and Domain",
                "TKI:000000000000000000000000000000000004",
                "WBI:100000000000000000000000000000000001",
                rethrowing(
                    (String taskId) -> taskService.transfer(taskId, "USER-1-1", "DOMAIN_A"))));

    ThrowingConsumer<Quadruple<String, String, String, Consumer<String>>> test =
        q -> {
          String taskId = q.getSecond();
          Consumer<String> transferMethod = q.getFourth();

          TaskHistoryQueryMapper taskHistoryQueryMapper = getHistoryQueryMapper();

          List<TaskHistoryEvent> events =
              taskHistoryQueryMapper.queryHistoryEvents(
                  (TaskHistoryQueryImpl) historyService.createTaskHistoryQuery().taskIdIn(taskId));

          assertThat(events).isEmpty();

          transferMethod.accept(taskId);

          events =
              taskHistoryQueryMapper.queryHistoryEvents(
                  (TaskHistoryQueryImpl) historyService.createTaskHistoryQuery().taskIdIn(taskId));

          assertThat(events).hasSize(1);
          String sourceWorkbasketId = q.getThird();
          assertTransferHistoryEvent(
              events.get(0).getId(),
              sourceWorkbasketId,
              "WBI:100000000000000000000000000000000006",
              "admin");
        };

    return DynamicTest.stream(testCases.iterator(), Quadruple::getFirst, test);
  }

  @WithAccessId(user = "admin")
  @TestFactory
  Stream<DynamicTest> should_CreateTransferredHistoryEvents_When_TaskBulkTransfer()
      throws Exception {
    List<Triplet<String, Map<String, String>, Consumer<List<String>>>> testCases =
        List.of(
            /*
            The workbasketId of the source Workbasket is parametrized. Putting the tested Tasks
            into the same Workbasket would result in changes to the test data. This would require
            changing tests that already use the tested Tasks. That's why workbasketId is
            parametrized.
            */
            Triplet.of(
                "Using WorkbasketId",
                Map.ofEntries(
                    Map.entry(
                        "TKI:000000000000000000000000000000000021",
                        "WBI:100000000000000000000000000000000001"),
                    Map.entry(
                        "TKI:000000000000000000000000000000000022",
                        "WBI:100000000000000000000000000000000001"),
                    Map.entry(
                        "TKI:000000000000000000000000000000000002",
                        "WBI:100000000000000000000000000000000006")),
                rethrowing(
                    (List<String> taskIds) ->
                        taskService.transferTasks(
                            "WBI:100000000000000000000000000000000007", taskIds))),
            Triplet.of(
                "Using WorkbasketKey and Domain",
                Map.ofEntries(
                    Map.entry(
                        "TKI:000000000000000000000000000000000023",
                        "WBI:100000000000000000000000000000000001"),
                    Map.entry(
                        "TKI:000000000000000000000000000000000024",
                        "WBI:100000000000000000000000000000000001"),
                    Map.entry(
                        "TKI:000000000000000000000000000000000055",
                        "WBI:100000000000000000000000000000000015")),
                rethrowing(
                    (List<String> taskIds) ->
                        taskService.transferTasks("USER-1-2", "DOMAIN_A", taskIds))));

    ThrowingConsumer<Triplet<String, Map<String, String>, Consumer<List<String>>>> test =
        t -> {
          Map<String, String> taskIds = t.getMiddle();
          Consumer<List<String>> transferMethod = t.getRight();

          TaskHistoryQueryMapper taskHistoryQueryMapper = getHistoryQueryMapper();

          List<TaskHistoryEvent> events =
              taskHistoryQueryMapper.queryHistoryEvents(
                  (TaskHistoryQueryImpl)
                      historyService
                          .createTaskHistoryQuery()
                          .taskIdIn(taskIds.keySet().toArray(new String[0])));

          assertThat(events).isEmpty();

          transferMethod.accept(new ArrayList<>(taskIds.keySet()));

          events =
              taskHistoryQueryMapper.queryHistoryEvents(
                  (TaskHistoryQueryImpl)
                      historyService
                          .createTaskHistoryQuery()
                          .taskIdIn(taskIds.keySet().toArray(new String[0])));

          assertThat(events)
              .extracting(TaskHistoryEvent::getTaskId)
              .containsExactlyInAnyOrderElementsOf(taskIds.keySet());

          for (TaskHistoryEvent event : events) {
            assertTransferHistoryEvent(
                event.getId(),
                taskIds.get(event.getTaskId()),
                "WBI:100000000000000000000000000000000007",
                "admin");
          }
        };

    return DynamicTest.stream(testCases.iterator(), Triplet::getLeft, test);
  }

  @WithAccessId(user = "admin")
  @TestFactory
  Stream<DynamicTest> should_CreateTransferredHistoryEvent_When_TaskIsTransferredWithOwner()
      throws Exception {
    List<Quadruple<String, String, String, Consumer<String>>> testCases =
        List.of(
            /*
            The workbasketId of the source Workbasket is parametrized. Putting the tested Tasks
            into the same Workbasket would result in changes to the test data. This would require
            changing tests that already use the tested Tasks. That's why workbasketId is
            parametrized.
            */
            Quadruple.of(
                "Using WorkbasketId; Task doesn't have an Attachment"
                    + " or any secondary Object References",
                "TKI:000000000000000000000000000000000005",
                "WBI:100000000000000000000000000000000001",
                rethrowing(
                    (String taskId) ->
                        taskService.transferWithOwner(
                            taskId, "WBI:100000000000000000000000000000000007", "user-1-2"))),
            Quadruple.of(
                "Using WorkbasketId; Task has Attachment and secondary Object Reference",
                "TKI:000000000000000000000000000000000001",
                "WBI:100000000000000000000000000000000006",
                rethrowing(
                    (String taskId) ->
                        taskService.transferWithOwner(
                            taskId, "WBI:100000000000000000000000000000000007", "user-1-2"))),
            Quadruple.of(
                "Using WorkbasketKey and Domain",
                "TKI:000000000000000000000000000000000006",
                "WBI:100000000000000000000000000000000001",
                rethrowing(
                    (String taskId) ->
                        taskService.transferWithOwner(
                            taskId, "USER-1-2", "DOMAIN_A", "user-1-2"))));
    ThrowingConsumer<Quadruple<String, String, String, Consumer<String>>> test =
        q -> {
          String taskId = q.getSecond();
          Consumer<String> transferMethod = q.getFourth();

          TaskHistoryQueryMapper taskHistoryQueryMapper = getHistoryQueryMapper();

          List<TaskHistoryEvent> events =
              taskHistoryQueryMapper.queryHistoryEvents(
                  (TaskHistoryQueryImpl) historyService.createTaskHistoryQuery().taskIdIn(taskId));

          assertThat(events).isEmpty();

          transferMethod.accept(taskId);

          events =
              taskHistoryQueryMapper.queryHistoryEvents(
                  (TaskHistoryQueryImpl) historyService.createTaskHistoryQuery().taskIdIn(taskId));

          assertThat(events).hasSize(1);
          String sourceWorkbasketId = q.getThird();
          assertTransferHistoryEvent(
              events.get(0).getId(),
              sourceWorkbasketId,
              "WBI:100000000000000000000000000000000007",
              "admin");
        };
    return DynamicTest.stream(testCases.iterator(), Quadruple::getFirst, test);
  }

  @WithAccessId(user = "admin")
  @TestFactory
  Stream<DynamicTest> should_CreateTransferredHistoryEvents_When_TaskBulkTransferWithOwner()
      throws Exception {
    List<Triplet<String, Map<String, String>, Consumer<List<String>>>> testCases =
        List.of(
            /*
            The workbasketId of the source Workbasket is parametrized. Putting the tested Tasks
            into the same Workbasket would result in changes to the test data. This would require
            changing tests that already use the tested Tasks. That's why workbasketId is
            parametrized.
            */
            Triplet.of(
                "Using WorkbasketId",
                Map.ofEntries(
                    Map.entry(
                        "TKI:000000000000000000000000000000000010",
                        "WBI:100000000000000000000000000000000001"),
                    Map.entry(
                        "TKI:000000000000000000000000000000000011",
                        "WBI:100000000000000000000000000000000001"),
                    Map.entry(
                        "TKI:000000000000000000000000000000000012",
                        "WBI:100000000000000000000000000000000001")),
                rethrowing(
                    (List<String> taskIds) ->
                        taskService.transferTasksWithOwner(
                            "WBI:100000000000000000000000000000000007", taskIds, "user-1-2"))),
            Triplet.of(
                "Using WorkbasketKey and Domain",
                Map.ofEntries(
                    Map.entry(
                        "TKI:000000000000000000000000000000000013",
                        "WBI:100000000000000000000000000000000001"),
                    Map.entry(
                        "TKI:000000000000000000000000000000000014",
                        "WBI:100000000000000000000000000000000001"),
                    Map.entry(
                        "TKI:000000000000000000000000000000000015",
                        "WBI:100000000000000000000000000000000001")),
                rethrowing(
                    (List<String> taskIds) ->
                        taskService.transferTasksWithOwner(
                            "USER-1-2", "DOMAIN_A", taskIds, "user-1-2"))));
    ThrowingConsumer<Triplet<String, Map<String, String>, Consumer<List<String>>>> test =
        t -> {
          Map<String, String> taskIds = t.getMiddle();
          Consumer<List<String>> transferMethod = t.getRight();

          TaskHistoryQueryMapper taskHistoryQueryMapper = getHistoryQueryMapper();

          List<TaskHistoryEvent> events =
              taskHistoryQueryMapper.queryHistoryEvents(
                  (TaskHistoryQueryImpl)
                      historyService
                          .createTaskHistoryQuery()
                          .taskIdIn(taskIds.keySet().toArray(new String[0])));

          assertThat(events).isEmpty();

          transferMethod.accept(new ArrayList<>(taskIds.keySet()));

          events =
              taskHistoryQueryMapper.queryHistoryEvents(
                  (TaskHistoryQueryImpl)
                      historyService
                          .createTaskHistoryQuery()
                          .taskIdIn(taskIds.keySet().toArray(new String[0])));

          assertThat(events)
              .extracting(TaskHistoryEvent::getTaskId)
              .containsExactlyInAnyOrderElementsOf(taskIds.keySet());

          for (TaskHistoryEvent event : events) {
            assertTransferHistoryEvent(
                event.getId(),
                taskIds.get(event.getTaskId()),
                "WBI:100000000000000000000000000000000007",
                "admin");
          }
        };

    return DynamicTest.stream(testCases.iterator(), Triplet::getLeft, test);
  }

  private void assertTransferHistoryEvent(
      String eventId, String expectedOldValue, String expectedNewValue, String expectedUser)
      throws Exception {
    TaskHistoryEvent event = historyService.getTaskHistoryEvent(eventId);
    assertThat(event.getDetails()).isNotNull();
    JSONArray changes = new JSONObject(event.getDetails()).getJSONArray("changes");
    assertThat(changes.length()).isPositive();
    boolean foundField = false;
    for (int i = 0; i < changes.length() && !foundField; i++) {
      JSONObject change = changes.getJSONObject(i);
      if (change.get("fieldName").equals("workbasketSummary")) {
        foundField = true;
        String oldWorkbasketStr = change.get("oldValue").toString();
        String newWorkbasketStr = change.get("newValue").toString();
        WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
        Workbasket oldWorkbasket = workbasketService.getWorkbasket(expectedOldValue);
        assertThat(oldWorkbasketStr)
            .isEqualTo(JSONObject.wrap(oldWorkbasket.asSummary()).toString());
        Workbasket newWorkbasket = workbasketService.getWorkbasket(expectedNewValue);
        assertThat(newWorkbasketStr)
            .isEqualTo(JSONObject.wrap(newWorkbasket.asSummary()).toString());
      }
    }
    assertThat(foundField).describedAs("changes do not contain field 'workbasketSummary'").isTrue();

    assertThat(event.getId()).startsWith("THI:");
    assertThat(event.getOldValue()).isEqualTo(expectedOldValue);
    assertThat(event.getNewValue()).isEqualTo(expectedNewValue);
    assertThat(event.getUserId()).isEqualTo(expectedUser);
    assertThat(event.getEventType()).isEqualTo(TaskHistoryEventType.TRANSFERRED.getName());
  }
}
