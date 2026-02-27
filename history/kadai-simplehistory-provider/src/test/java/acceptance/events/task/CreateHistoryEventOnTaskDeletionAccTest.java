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

import io.kadai.KadaiConfiguration.Builder;
import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.internal.util.CheckedRunnable;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.common.test.security.WithAccessId;
import io.kadai.simplehistory.task.internal.TaskHistoryEventConsumer;
import io.kadai.simplehistory.task.internal.TaskHistoryServiceImpl;
import io.kadai.spi.history.api.KadaiEventConsumer;
import io.kadai.spi.history.api.events.task.TaskHistoryEvent;
import io.kadai.spi.history.api.events.task.TaskHistoryEventType;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.TaskState;
import io.kadai.task.api.models.Task;
import io.kadai.testapi.DefaultTestEntities;
import io.kadai.testapi.KadaiConfigurationModifier;
import io.kadai.testapi.KadaiInject;
import io.kadai.testapi.KadaiIntegrationTest;
import io.kadai.testapi.WithServiceProvider;
import io.kadai.testapi.builder.TaskBuilder;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@KadaiIntegrationTest
@WithServiceProvider(
    serviceProviderInterface = KadaiEventConsumer.class,
    serviceProviders = TaskHistoryEventConsumer.class)
@ExtendWith(JaasExtension.class)
class CreateHistoryEventOnTaskDeletionAccTest implements KadaiConfigurationModifier {
  @KadaiInject KadaiEngine kadaiEngine;
  @KadaiInject TaskService taskService;
  @KadaiInject WorkbasketService workbasketService;
  @KadaiInject ClassificationService classificationService;
  ClassificationSummary defaultClassificationSummary;
  WorkbasketSummary defaultWorkbasketSummary;
  Task task1;
  Task task2;
  Task task3;
  Task task4;
  TaskHistoryServiceImpl historyService;

  @Override
  public Builder modify(Builder builder) {
    return builder.deleteHistoryEventsOnTaskDeletionEnabled(false);
  }

  @WithAccessId(user = "admin")
  @BeforeAll
  void setUp() throws Exception {

    defaultClassificationSummary =
        DefaultTestEntities.defaultTestClassification()
            .buildAndStoreAsSummary(classificationService);
    defaultWorkbasketSummary =
        DefaultTestEntities.defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);

    task1 = createTask().buildAndStore(taskService);
    task2 = createTask().state(TaskState.COMPLETED).buildAndStore(taskService);
    task3 = createTask().state(TaskState.COMPLETED).buildAndStore(taskService);
    task4 = createTask().state(TaskState.COMPLETED).buildAndStore(taskService);

    historyService = new TaskHistoryServiceImpl();
    historyService.initialize(kadaiEngine);
  }

  @Test
  void should_CreateDeleteHistoryEvent_When_TaskIsDeleted() throws Exception {
    kadaiEngine.runAsAdmin(
        CheckedRunnable.rethrowing(
            () -> {
              historyService.deleteTaskHistoryEventsByTaskIds(List.of(task4.getId()));

              taskService.deleteTask(task4.getId());

              List<TaskHistoryEvent> events =
                  historyService.createTaskHistoryQuery().taskIdIn(task4.getId()).list();
              assertThat(events).hasSize(1);
              assertDeleteHistoryEvent(events.get(0).getId(), "teamlead-2", task4.getId());
            }),
        "teamlead-2");
  }

  @Test
  void should_CreateDeleteHistoryEvent_When_TaskIsForceDeleted() throws Exception {
    kadaiEngine.runAsAdmin(
        CheckedRunnable.rethrowing(
            () -> {
              historyService.deleteTaskHistoryEventsByTaskIds(List.of(task1.getId()));

              taskService.forceDeleteTask(task1.getId());

              List<TaskHistoryEvent> events =
                  historyService.createTaskHistoryQuery().taskIdIn(task1.getId()).list();
              assertThat(events).hasSize(1);
              assertDeleteHistoryEvent(events.get(0).getId(), "user-1-2", task1.getId());
            }),
        "user-1-2");
  }

  @Test
  void should_CreateDeleteHistoryEvents_When_MultipleTasksAreDeleted() throws Exception {
    kadaiEngine.runAsAdmin(
        CheckedRunnable.rethrowing(
            () -> {
              List<String> taskIds = List.of(task2.getId(), task3.getId());
              historyService.deleteTaskHistoryEventsByTaskIds(taskIds);

              taskService.deleteTasks(taskIds);

              TaskHistoryEvent eventTask2 =
                  historyService.createTaskHistoryQuery().taskIdIn(task2.getId()).single();
              TaskHistoryEvent eventTask3 =
                  historyService.createTaskHistoryQuery().taskIdIn(task3.getId()).single();
              assertDeleteHistoryEvent(eventTask2.getId(), "businessadmin", task2.getId());
              assertDeleteHistoryEvent(eventTask3.getId(), "businessadmin", task3.getId());
            }),
        "businessadmin");
  }

  private void assertDeleteHistoryEvent(String eventId, String expectedUser, String taskId)
      throws Exception {
    TaskHistoryEvent event = historyService.getTaskHistoryEvent(eventId);
    assertThat(event.getUserId()).isEqualTo(expectedUser);
    assertThat(event.getProxyAccessId()).isEqualTo("admin");
    assertThat(event.getEventType()).isEqualTo(TaskHistoryEventType.DELETED.getName());
    assertThat(event.getTaskId()).isEqualTo(taskId);
  }

  private TaskBuilder createTask() {
    return TaskBuilder.newTask()
        .classificationSummary(defaultClassificationSummary)
        .workbasketSummary(defaultWorkbasketSummary)
        .primaryObjRef(DefaultTestEntities.defaultTestObjectReference().build());
  }
}
