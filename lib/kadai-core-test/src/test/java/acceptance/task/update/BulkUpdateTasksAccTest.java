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

package acceptance.task.update;

import static io.kadai.testapi.DefaultTestEntities.defaultTestClassification;
import static io.kadai.testapi.DefaultTestEntities.defaultTestObjectReference;
import static io.kadai.testapi.DefaultTestEntities.defaultTestWorkbasket;
import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.common.api.BulkOperationResults;
import io.kadai.common.api.exceptions.KadaiException;
import io.kadai.common.internal.util.Pair;
import io.kadai.task.api.TaskPatch;
import io.kadai.task.api.TaskPatchBuilder;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.TaskState;
import io.kadai.task.api.models.Task;
import io.kadai.task.internal.models.TaskImpl;
import io.kadai.testapi.KadaiInject;
import io.kadai.testapi.KadaiIntegrationTest;
import io.kadai.testapi.builder.TaskBuilder;
import io.kadai.testapi.builder.WorkbasketAccessItemBuilder;
import io.kadai.testapi.security.WithAccessId;
import io.kadai.workbasket.api.WorkbasketPermission;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.exceptions.NotAuthorizedOnWorkbasketException;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@KadaiIntegrationTest
class BulkUpdateTasksAccTest {

  @KadaiInject TaskService taskService;
  @KadaiInject ClassificationService classificationService;
  @KadaiInject WorkbasketService workbasketService;

  ClassificationSummary defaultClassificationSummary;
  WorkbasketSummary defaultWorkbasketSummary;
  WorkbasketSummary wbUnauthorized;

  Task unauthorizedTask;

  @WithAccessId(user = "admin")
  @BeforeAll
  void setup() throws Exception {
    defaultClassificationSummary =
        defaultTestClassification()
            .serviceLevel("P3D")
            .buildAndStoreAsSummary(classificationService);
    defaultWorkbasketSummary = defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);
    WorkbasketAccessItemBuilder.newWorkbasketAccessItem()
        .workbasketId(defaultWorkbasketSummary.getId())
        .accessId("user-1-2")
        .permission(WorkbasketPermission.OPEN)
        .permission(WorkbasketPermission.READ)
        .permission(WorkbasketPermission.READTASKS)
        .permission(WorkbasketPermission.EDITTASKS)
        .permission(WorkbasketPermission.APPEND)
        .buildAndStore(workbasketService);
    wbUnauthorized = defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);
    unauthorizedTask =
        TaskBuilder.newTask()
            .classificationSummary(defaultClassificationSummary)
            .workbasketSummary(wbUnauthorized)
            .primaryObjRef(defaultTestObjectReference().build())
            .state(TaskState.READY)
            .buildAndStore(taskService);
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_BulkUpdateMultipleTasks_When_UpdateSimpleFields() throws Exception {
    // given
    Task t1 =
        TaskBuilder.newTask()
            .classificationSummary(defaultClassificationSummary)
            .workbasketSummary(defaultWorkbasketSummary)
            .primaryObjRef(defaultTestObjectReference().build())
            .state(TaskState.READY)
            .buildAndStore(taskService);
    Task t2 =
        TaskBuilder.newTask()
            .classificationSummary(defaultClassificationSummary)
            .workbasketSummary(defaultWorkbasketSummary)
            .primaryObjRef(defaultTestObjectReference().build())
            .state(TaskState.READY)
            .buildAndStore(taskService);

    TaskPatch patch = new TaskPatchBuilder()
        .name("Bulk Updated Task")
        .description("Bulk update description")
        .note("Bulk update note")
        .manualPriority(42)
        .isRead(true)
        .businessProcessId("BPI-BULK-001")
        .parentBusinessProcessId("PBPI-BULK-001")
        .custom1("bulk-custom1")
        .customInt1(1001)
        .planned(Instant.parse("2024-01-05T14:00:00Z"))
        .received(Instant.parse("2024-01-06T15:00:00Z"))
        .build();

    List<String> taskIds = List.of(t1.getId(), t2.getId());

    // when
    BulkOperationResults<String, KadaiException> result =
        taskService.bulkUpdateTasks(taskIds, patch);

    // then
    assertThat(result).isNotNull();
    Set<String> failed = new HashSet<>(result.getFailedIds());
    assertThat(failed).isEmpty();

    // verify each task got the updates
    List<Pair<String, Task>> updated = new ArrayList<>();
    for (String id : taskIds) {
      updated.add(Pair.of(id, taskService.getTask(id)));
    }

    for (Pair<String, Task> pair : updated) {
      Task ut = pair.getRight();
      assertThat(ut.getName()).isEqualTo("Bulk Updated Task");
      assertThat(ut.getDescription()).isEqualTo("Bulk update description");
      assertThat(ut.getNote()).isEqualTo("Bulk update note");
      assertThat(ut.getPriority()).isEqualTo(42);
      assertThat(ut.getManualPriority()).isEqualTo(42);
      assertThat(ut.isRead()).isTrue();
      assertThat(ut.getBusinessProcessId()).isEqualTo("BPI-BULK-001");
      assertThat(ut.getParentBusinessProcessId()).isEqualTo("PBPI-BULK-001");
      assertThat(((TaskImpl) ut).getCustom1()).isEqualTo("bulk-custom1");
      assertThat(((TaskImpl) ut).getCustomInt1()).isEqualTo(1001);
      assertThat(ut.getPlanned()).isEqualTo(Instant.parse("2024-01-05T14:00:00Z"));
      assertThat(ut.getReceived()).isEqualTo(Instant.parse("2024-01-06T15:00:00Z"));
    }
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_AddError_When_UserNotAuthorizedToATask() throws Exception {
    Task task1 =
        TaskBuilder.newTask()
            .classificationSummary(defaultClassificationSummary)
            .workbasketSummary(defaultWorkbasketSummary)
            .primaryObjRef(defaultTestObjectReference().build())
            .state(TaskState.READY)
            .buildAndStore(taskService);

    List<String> taskIds = List.of(task1.getId(), unauthorizedTask.getId());
    TaskPatch patch = new TaskPatchBuilder()
        .name("Bulk Updated Task")
        .description("Bulk update description")
        .build();

    BulkOperationResults<String, KadaiException> result =
        taskService.bulkUpdateTasks(taskIds, patch);
    assertThat(result.getErrorMap()).hasSize(1);
    assertThat(result.getErrorMap().get(unauthorizedTask.getId()))
        .isInstanceOf(NotAuthorizedOnWorkbasketException.class);

    Task updatedTask1 = taskService.getTask(task1.getId());
    assertThat(updatedTask1.getName()).isEqualTo("Bulk Updated Task");
    assertThat(updatedTask1.getDescription()).isEqualTo("Bulk update description");
  }
}
