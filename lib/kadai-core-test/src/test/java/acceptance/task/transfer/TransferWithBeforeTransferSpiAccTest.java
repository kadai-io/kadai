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

package acceptance.task.transfer;

import static io.kadai.testapi.DefaultTestEntities.defaultTestClassification;
import static io.kadai.testapi.DefaultTestEntities.defaultTestWorkbasket;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;

import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.common.api.BulkOperationResults;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.exceptions.KadaiException;
import io.kadai.spi.task.api.BeforeTransferTaskProvider;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.TaskState;
import io.kadai.task.api.exceptions.TransferCheckException;
import io.kadai.task.api.models.ObjectReference;
import io.kadai.task.api.models.Task;
import io.kadai.testapi.DefaultTestEntities;
import io.kadai.testapi.KadaiInject;
import io.kadai.testapi.KadaiIntegrationTest;
import io.kadai.testapi.WithServiceProvider;
import io.kadai.testapi.builder.TaskBuilder;
import io.kadai.testapi.builder.WorkbasketAccessItemBuilder;
import io.kadai.testapi.security.WithAccessId;
import io.kadai.workbasket.api.WorkbasketPermission;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.models.Workbasket;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import java.time.Instant;
import java.util.List;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@KadaiIntegrationTest
public class TransferWithBeforeTransferSpiAccTest {

  ClassificationSummary defaultClassificationSummary;
  WorkbasketSummary defaultWorkbasketSummary;
  WorkbasketSummary destinationWorkbasketSummary;
  ObjectReference defaultObjectReference;

  @WithAccessId(user = "businessadmin")
  @BeforeAll
  void setup(ClassificationService classificationService, WorkbasketService workbasketService)
      throws Exception {
    defaultClassificationSummary =
        defaultTestClassification().buildAndStoreAsSummary(classificationService);
    defaultWorkbasketSummary = defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);
    destinationWorkbasketSummary =
        defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);

    WorkbasketAccessItemBuilder.newWorkbasketAccessItem()
        .workbasketId(defaultWorkbasketSummary.getId())
        .accessId("user-1-1")
        .permission(WorkbasketPermission.READ)
        .permission(WorkbasketPermission.READTASKS)
        .permission(WorkbasketPermission.EDITTASKS)
        .permission(WorkbasketPermission.APPEND)
        .permission(WorkbasketPermission.TRANSFER)
        .buildAndStore(workbasketService);

    WorkbasketAccessItemBuilder.newWorkbasketAccessItem()
        .workbasketId(destinationWorkbasketSummary.getId())
        .accessId("user-1-1")
        .permission(WorkbasketPermission.READ)
        .permission(WorkbasketPermission.READTASKS)
        .permission(WorkbasketPermission.EDITTASKS)
        .permission(WorkbasketPermission.APPEND)
        .permission(WorkbasketPermission.TRANSFER)
        .buildAndStore(workbasketService);

    defaultObjectReference = DefaultTestEntities.defaultTestObjectReference().build();
  }

  private TaskBuilder createTaskInState(TaskState state) {
    TaskBuilder builder =
        TaskBuilder.newTask()
            .classificationSummary(defaultClassificationSummary)
            .workbasketSummary(defaultWorkbasketSummary)
            .primaryObjRef(defaultObjectReference);
    if (state == TaskState.CLAIMED) {
      builder.owner("user-1-1").state(TaskState.CLAIMED).claimed(Instant.now());
    } else {
      builder.state(state);
    }
    return builder;
  }

  private TaskBuilder createReadyTask() {
    return createTaskInState(TaskState.READY);
  }

  static class AlwaysDenyTransfer implements BeforeTransferTaskProvider {
    @Override
    public void initialize(KadaiEngine kadaiEngine) {
      // no-op
    }

    @Override
    public void checkTransferAllowed(Task task, Workbasket destinationWorkbasket)
        throws TransferCheckException {
      throw new TransferCheckException(
          "Transfer always denied by Test-SPI",
          task.getWorkbasketSummary().getId(),
          destinationWorkbasket.getId());
    }
  }

  static class AlwaysAllowTransfer implements BeforeTransferTaskProvider {
    @Override
    public void initialize(KadaiEngine kadaiEngine) {
      // no-op
    }

    @Override
    public void checkTransferAllowed(Task task, Workbasket destinationWorkbasket) {
      // allowed
    }
  }

  static class DenySpecificWorkbasket implements BeforeTransferTaskProvider {
    static String deniedWorkbasketId;

    @Override
    public void initialize(KadaiEngine kadaiEngine) {
      // no-op
    }

    @Override
    public void checkTransferAllowed(Task task, Workbasket destinationWorkbasket)
        throws TransferCheckException {
      if (destinationWorkbasket.getId().equals(deniedWorkbasketId)) {
        throw new TransferCheckException(
            "Transfer to this workbasket is not allowed",
            task.getWorkbasketSummary().getId(),
            destinationWorkbasket.getId());
      }
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  @WithServiceProvider(
      serviceProviderInterface = BeforeTransferTaskProvider.class,
      serviceProviders = AlwaysAllowTransfer.class)
  class SpiAllowsTransfer {

    @KadaiInject TaskService taskService;

    @WithAccessId(user = "user-1-1")
    @Test
    void should_TransferTask_When_SpiAllowsTransfer() throws Exception {
      Task task = createReadyTask().buildAndStore(taskService);

      Task transferred = taskService.transfer(task.getId(), destinationWorkbasketSummary.getId());

      assertThat(transferred.getWorkbasketSummary().getId())
          .isEqualTo(destinationWorkbasketSummary.getId());
      assertThat(transferred.isTransferred()).isTrue();
    }

    @WithAccessId(user = "user-1-1")
    @Test
    void should_TransferTaskWithOwner_When_SpiAllowsTransfer() throws Exception {
      Task task = createReadyTask().buildAndStore(taskService);

      Task transferred =
          taskService.transferWithOwner(
              task.getId(), destinationWorkbasketSummary.getId(), "user-1-1");

      assertThat(transferred.getWorkbasketSummary().getId())
          .isEqualTo(destinationWorkbasketSummary.getId());
      assertThat(transferred.getOwner()).isEqualTo("user-1-1");
    }

    @WithAccessId(user = "user-1-1")
    @Test
    void should_TransferBulkTasks_When_SpiAllowsTransfer() throws Exception {
      Task task1 = createReadyTask().buildAndStore(taskService);
      Task task2 = createReadyTask().buildAndStore(taskService);

      BulkOperationResults<String, KadaiException> result =
          taskService.transferTasks(
              destinationWorkbasketSummary.getId(), List.of(task1.getId(), task2.getId()));

      assertThat(result.containsErrors()).isFalse();

      Task transferred1 = taskService.getTask(task1.getId());
      Task transferred2 = taskService.getTask(task2.getId());
      assertThat(transferred1.getWorkbasketSummary().getId())
          .isEqualTo(destinationWorkbasketSummary.getId());
      assertThat(transferred2.getWorkbasketSummary().getId())
          .isEqualTo(destinationWorkbasketSummary.getId());
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  @WithServiceProvider(
      serviceProviderInterface = BeforeTransferTaskProvider.class,
      serviceProviders = AlwaysDenyTransfer.class)
  class SpiDeniesTransfer {

    @KadaiInject TaskService taskService;

    @WithAccessId(user = "user-1-1")
    @Test
    void should_ThrowTransferCheckException_When_SpiDeniesTransfer() throws Exception {
      Task task = createReadyTask().buildAndStore(taskService);

      ThrowingCallable call =
          () -> taskService.transfer(task.getId(), destinationWorkbasketSummary.getId());

      TransferCheckException ex = catchThrowableOfType(TransferCheckException.class, call);
      assertThat(ex).isNotNull();
      assertThat(ex.getReason()).isEqualTo("Transfer always denied by Test-SPI");
      assertThat(ex.getSourceWorkbasketId()).isEqualTo(defaultWorkbasketSummary.getId());
      assertThat(ex.getDestinationWorkbasketId()).isEqualTo(destinationWorkbasketSummary.getId());
    }

    @WithAccessId(user = "user-1-1")
    @Test
    void should_ThrowTransferCheckException_When_SpiDeniesTransferWithOwner() throws Exception {
      Task task = createReadyTask().buildAndStore(taskService);

      ThrowingCallable call =
          () ->
              taskService.transferWithOwner(
                  task.getId(), destinationWorkbasketSummary.getId(), "user-1-1");

      TransferCheckException ex = catchThrowableOfType(TransferCheckException.class, call);
      assertThat(ex).isNotNull();
      assertThat(ex.getReason()).isEqualTo("Transfer always denied by Test-SPI");
    }

    @SuppressWarnings("checkstyle:CatchParameterName")
    @WithAccessId(user = "user-1-1")
    @Test
    void should_NotTransferTask_When_SpiDeniesTransfer() throws Exception {
      Task task = createReadyTask().buildAndStore(taskService);

      try {
        taskService.transfer(task.getId(), destinationWorkbasketSummary.getId());
      } catch (TransferCheckException _) {
        // expected
      }

      Task persistentTask = taskService.getTask(task.getId());
      assertThat(persistentTask.getWorkbasketSummary().getId())
          .isEqualTo(defaultWorkbasketSummary.getId());
    }

    @WithAccessId(user = "user-1-1")
    @Test
    void should_NotTransferAnyBulkTasks_When_SpiDeniesTransfer() throws Exception {
      Task task1 = createReadyTask().buildAndStore(taskService);
      Task task2 = createReadyTask().buildAndStore(taskService);

      BulkOperationResults<String, KadaiException> result =
          taskService.transferTasks(
              destinationWorkbasketSummary.getId(), List.of(task1.getId(), task2.getId()));

      assertThat(result.containsErrors()).isTrue();
      assertThat(result.getFailedIds()).containsExactlyInAnyOrder(task1.getId(), task2.getId());

      // Verify no tasks were transferred
      Task persistentTask1 = taskService.getTask(task1.getId());
      Task persistentTask2 = taskService.getTask(task2.getId());
      assertThat(persistentTask1.getWorkbasketSummary().getId())
          .isEqualTo(defaultWorkbasketSummary.getId());
      assertThat(persistentTask2.getWorkbasketSummary().getId())
          .isEqualTo(defaultWorkbasketSummary.getId());
    }

    @WithAccessId(user = "user-1-1")
    @Test
    void should_NotTransferAnyBulkTasksWithOwner_When_SpiDeniesTransfer() throws Exception {
      Task task1 = createReadyTask().buildAndStore(taskService);
      Task task2 = createReadyTask().buildAndStore(taskService);

      BulkOperationResults<String, KadaiException> result =
          taskService.transferTasksWithOwner(
              destinationWorkbasketSummary.getId(),
              List.of(task1.getId(), task2.getId()),
              "user-1-1");

      assertThat(result.containsErrors()).isTrue();

      // Verify no tasks were transferred
      Task persistentTask1 = taskService.getTask(task1.getId());
      Task persistentTask2 = taskService.getTask(task2.getId());
      assertThat(persistentTask1.getWorkbasketSummary().getId())
          .isEqualTo(defaultWorkbasketSummary.getId());
      assertThat(persistentTask2.getWorkbasketSummary().getId())
          .isEqualTo(defaultWorkbasketSummary.getId());
    }

    @WithAccessId(user = "user-1-1")
    @Test
    void should_ContainProperErrorCodeInException_When_SpiDeniesTransfer() throws Exception {
      Task task = createReadyTask().buildAndStore(taskService);

      ThrowingCallable call =
          () -> taskService.transfer(task.getId(), destinationWorkbasketSummary.getId());

      TransferCheckException ex = catchThrowableOfType(TransferCheckException.class, call);
      assertThat(ex.getErrorCode().getKey()).isEqualTo("TASK_TRANSFER_CHECK_FAILED");
      assertThat(ex.getErrorCode().getMessageVariables())
          .containsEntry("reason", "Transfer always denied by Test-SPI")
          .containsEntry("sourceWorkbasketId", defaultWorkbasketSummary.getId())
          .containsEntry("destinationWorkbasketId", destinationWorkbasketSummary.getId());
    }

    @WithAccessId(user = "user-1-1")
    @Test
    void should_TransferByKeyDomainDenied_When_SpiDeniesTransfer() throws Exception {
      Task task = createReadyTask().buildAndStore(taskService);

      ThrowingCallable call =
          () ->
              taskService.transfer(
                  task.getId(),
                  destinationWorkbasketSummary.getKey(),
                  destinationWorkbasketSummary.getDomain());

      assertThatThrownBy(call).isInstanceOf(TransferCheckException.class);
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  @WithServiceProvider(
      serviceProviderInterface = BeforeTransferTaskProvider.class,
      serviceProviders = DenySpecificWorkbasket.class)
  class SpiDeniesSpecificWorkbasket {

    @KadaiInject TaskService taskService;

    @WithAccessId(user = "user-1-1")
    @Test
    void should_AllowTransfer_When_SpiDoesNotDenyDestination() throws Exception {
      DenySpecificWorkbasket.deniedWorkbasketId = "non-existing-id";
      Task task = createReadyTask().buildAndStore(taskService);

      Task transferred = taskService.transfer(task.getId(), destinationWorkbasketSummary.getId());

      assertThat(transferred.getWorkbasketSummary().getId())
          .isEqualTo(destinationWorkbasketSummary.getId());
    }

    @WithAccessId(user = "user-1-1")
    @Test
    void should_DenyTransfer_When_SpiDeniesSpecificDestination() throws Exception {
      DenySpecificWorkbasket.deniedWorkbasketId = destinationWorkbasketSummary.getId();
      Task task = createReadyTask().buildAndStore(taskService);

      ThrowingCallable call =
          () -> taskService.transfer(task.getId(), destinationWorkbasketSummary.getId());

      assertThatThrownBy(call)
          .isInstanceOf(TransferCheckException.class)
          .hasMessageContaining("Transfer to this workbasket is not allowed");
    }

    @WithAccessId(user = "user-1-1")
    @Test
    void should_ReturnEarlyAndNotTransferAny_When_SingleTaskFailsBulkCheck() throws Exception {
      DenySpecificWorkbasket.deniedWorkbasketId = destinationWorkbasketSummary.getId();
      Task task1 = createReadyTask().buildAndStore(taskService);
      Task task2 = createReadyTask().buildAndStore(taskService);

      BulkOperationResults<String, KadaiException> result =
          taskService.transferTasks(
              destinationWorkbasketSummary.getId(), List.of(task1.getId(), task2.getId()));

      assertThat(result.containsErrors()).isTrue();
      // Both tasks should fail because all are denied for this workbasket
      assertThat(result.getFailedIds()).containsExactlyInAnyOrder(task1.getId(), task2.getId());

      // Verify no tasks were transferred
      Task persistentTask1 = taskService.getTask(task1.getId());
      Task persistentTask2 = taskService.getTask(task2.getId());
      assertThat(persistentTask1.getWorkbasketSummary().getId())
          .isEqualTo(defaultWorkbasketSummary.getId());
      assertThat(persistentTask2.getWorkbasketSummary().getId())
          .isEqualTo(defaultWorkbasketSummary.getId());
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  @WithServiceProvider(
      serviceProviderInterface = BeforeTransferTaskProvider.class,
      serviceProviders = {AlwaysAllowTransfer.class, AlwaysDenyTransfer.class})
  class MultipleSpisAreDefined {

    @KadaiInject TaskService taskService;

    @WithAccessId(user = "user-1-1")
    @Test
    void should_DenyTransfer_When_AnySpiDenies() throws Exception {
      Task task = createReadyTask().buildAndStore(taskService);

      ThrowingCallable call =
          () -> taskService.transfer(task.getId(), destinationWorkbasketSummary.getId());

      assertThatThrownBy(call).isInstanceOf(TransferCheckException.class);
    }

    @WithAccessId(user = "user-1-1")
    @Test
    void should_DenyBulkTransfer_When_AnySpiDenies() throws Exception {
      Task task1 = createReadyTask().buildAndStore(taskService);
      Task task2 = createReadyTask().buildAndStore(taskService);

      BulkOperationResults<String, KadaiException> result =
          taskService.transferTasks(
              destinationWorkbasketSummary.getId(), List.of(task1.getId(), task2.getId()));

      assertThat(result.containsErrors()).isTrue();

      // Verify no tasks were transferred
      Task persistentTask1 = taskService.getTask(task1.getId());
      Task persistentTask2 = taskService.getTask(task2.getId());
      assertThat(persistentTask1.getWorkbasketSummary().getId())
          .isEqualTo(defaultWorkbasketSummary.getId());
      assertThat(persistentTask2.getWorkbasketSummary().getId())
          .isEqualTo(defaultWorkbasketSummary.getId());
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  @WithServiceProvider(
      serviceProviderInterface = BeforeTransferTaskProvider.class,
      serviceProviders = AlwaysDenyTransfer.class)
  class SpiDeniesClaimedTaskTransfer {

    @KadaiInject TaskService taskService;

    @WithAccessId(user = "user-1-1")
    @Test
    void should_DenyTransfer_When_SpiDeniesClaimedTask() throws Exception {
      Task task = createTaskInState(TaskState.CLAIMED).buildAndStore(taskService);

      ThrowingCallable call =
          () -> taskService.transfer(task.getId(), destinationWorkbasketSummary.getId());

      assertThatThrownBy(call).isInstanceOf(TransferCheckException.class);
    }
  }
}
