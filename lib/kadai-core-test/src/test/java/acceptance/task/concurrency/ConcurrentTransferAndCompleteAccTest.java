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

package acceptance.task.concurrency;

import static io.kadai.testapi.DefaultTestEntities.defaultTestClassification;
import static io.kadai.testapi.DefaultTestEntities.defaultTestObjectReference;
import static io.kadai.testapi.DefaultTestEntities.defaultTestWorkbasket;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.common.api.BulkOperationResults;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.exceptions.ConcurrencyException;
import io.kadai.common.api.exceptions.KadaiException;
import io.kadai.spi.history.api.KadaiEventConsumer;
import io.kadai.spi.history.api.events.task.TaskTransferredEvent;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.TaskState;
import io.kadai.task.api.exceptions.InvalidTaskStateException;
import io.kadai.task.api.models.ObjectReference;
import io.kadai.task.api.models.Task;
import io.kadai.testapi.KadaiInject;
import io.kadai.testapi.KadaiIntegrationTest;
import io.kadai.testapi.WithServiceProvider;
import io.kadai.testapi.builder.TaskBuilder;
import io.kadai.testapi.builder.TaskCommentBuilder;
import io.kadai.testapi.builder.WorkbasketAccessItemBuilder;
import io.kadai.testapi.security.WithAccessId;
import io.kadai.workbasket.api.WorkbasketPermission;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Acceptance tests for concurrent modifications of a Task while it is transferred, completed or
 * claimed.
 *
 * <p>The competing modification is executed by {@link ConcurrentTaskUpdateInterferer} in a separate
 * transaction after the operation under test has read and validated the Task, but before it writes
 * the Task. The operation under test must neither overwrite the concurrent modification nor act on
 * its outdated view of the Task.
 */
@KadaiIntegrationTest
@WithServiceProvider(
    serviceProviderInterface = KadaiEventConsumer.class,
    serviceProviders = ConcurrentTransferAndCompleteAccTest.TransferEventRecorder.class)
class ConcurrentTransferAndCompleteAccTest {

  private static final String USER = "user-1-1";
  private static final Instant CLAIMED_AT = Instant.parse("2026-01-01T08:00:00Z");

  @KadaiInject KadaiEngine kadaiEngine;
  @KadaiInject TaskService taskService;

  ClassificationSummary classification;
  WorkbasketSummary sourceWorkbasket;
  WorkbasketSummary destinationWorkbasket;
  ObjectReference objectReference;
  ConcurrentTaskUpdateInterferer interferer;

  @WithAccessId(user = "businessadmin")
  @BeforeAll
  void setup(ClassificationService classificationService, WorkbasketService workbasketService)
      throws Exception {
    classification = defaultTestClassification().buildAndStoreAsSummary(classificationService);
    sourceWorkbasket = defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);
    destinationWorkbasket = defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);
    for (WorkbasketSummary workbasket : List.of(sourceWorkbasket, destinationWorkbasket)) {
      WorkbasketAccessItemBuilder.newWorkbasketAccessItem()
          .workbasketId(workbasket.getId())
          .accessId(USER)
          .permission(WorkbasketPermission.READ)
          .permission(WorkbasketPermission.READTASKS)
          .permission(WorkbasketPermission.EDITTASKS)
          .permission(WorkbasketPermission.APPEND)
          .permission(WorkbasketPermission.TRANSFER)
          .buildAndStore(workbasketService);
    }
    objectReference = defaultTestObjectReference().build();
    interferer = ConcurrentTaskUpdateInterferer.registerOn(kadaiEngine);
  }

  @AfterEach
  void resetInterference() {
    interferer.disarm();
    TransferEventRecorder.clear();
  }

  // region transfer vs. complete (the reported scenario)

  @WithAccessId(user = USER)
  @Test
  void should_NotReopenTask_When_TaskIsCompletedWhileBeingTransferred() throws Exception {
    Task task = createTask(TaskState.CLAIMED);
    completeConcurrently(task.getId());

    ThrowingCallable call = () -> taskService.transfer(task.getId(), destinationWorkbasket.getId());

    assertThatThrownBy(call).isInstanceOf(InvalidTaskStateException.class);
    assertThat(interferer.getInterferenceCount()).isOne();
    Task persistedTask = taskService.getTask(task.getId());
    assertThat(persistedTask.getState()).isEqualTo(TaskState.COMPLETED);
    assertThat(persistedTask.getCompleted()).isNotNull();
    assertThat(persistedTask.getWorkbasketSummary().getId()).isEqualTo(sourceWorkbasket.getId());
    assertThat(TransferEventRecorder.getTransferredTaskIds()).isEmpty();
  }

  @WithAccessId(user = USER)
  @Test
  void should_NotReopenTask_When_TaskIsCompletedWhileBeingTransferredInBulk() throws Exception {
    Task completedTask = createTask(TaskState.CLAIMED);
    Task otherTask = createTask(TaskState.CLAIMED);
    completeConcurrently(completedTask.getId());

    BulkOperationResults<String, KadaiException> result =
        taskService.transferTasks(
            destinationWorkbasket.getId(), List.of(completedTask.getId(), otherTask.getId()));

    assertThat(interferer.getInterferenceCount()).isOne();
    assertThat(result.getFailedIds()).containsExactly(completedTask.getId());
    assertThat(result.getErrorForId(completedTask.getId()))
        .isInstanceOf(InvalidTaskStateException.class);
    Task persistedCompletedTask = taskService.getTask(completedTask.getId());
    assertThat(persistedCompletedTask.getState()).isEqualTo(TaskState.COMPLETED);
    assertThat(persistedCompletedTask.getWorkbasketSummary().getId())
        .isEqualTo(sourceWorkbasket.getId());
    Task persistedOtherTask = taskService.getTask(otherTask.getId());
    assertThat(persistedOtherTask.getState()).isEqualTo(TaskState.READY);
    assertThat(persistedOtherTask.getWorkbasketSummary().getId())
        .isEqualTo(destinationWorkbasket.getId());
    assertThat(TransferEventRecorder.getTransferredTaskIds()).containsExactly(otherTask.getId());
  }

  @WithAccessId(user = USER)
  @Test
  void should_RejectCompletion_When_TaskIsTransferredWhileBeingCompleted() throws Exception {
    Task task = createTask(TaskState.CLAIMED);
    transferConcurrently(task.getId());

    ThrowingCallable call = () -> taskService.completeTask(task.getId());

    assertThatThrownBy(call).isInstanceOf(InvalidTaskStateException.class);
    assertThat(interferer.getInterferenceCount()).isOne();
    Task persistedTask = taskService.getTask(task.getId());
    assertThat(persistedTask.getState()).isEqualTo(TaskState.READY);
    assertThat(persistedTask.getCompleted()).isNull();
    assertThat(persistedTask.getWorkbasketSummary().getId())
        .isEqualTo(destinationWorkbasket.getId());
  }

  @WithAccessId(user = USER)
  @Test
  void should_CompleteTaskInNewWorkbasket_When_TaskIsTransferredWhileBeingForceCompleted()
      throws Exception {
    Task task = createTask(TaskState.CLAIMED);
    transferConcurrently(task.getId());

    Task completedTask = taskService.forceCompleteTask(task.getId());

    assertThat(interferer.getInterferenceCount()).isOne();
    Task persistedTask = taskService.getTask(task.getId());
    assertThat(persistedTask.getState()).isEqualTo(TaskState.COMPLETED);
    assertThat(persistedTask.getOwner()).isEqualTo(USER);
    assertThat(persistedTask.getWorkbasketSummary().getId())
        .isEqualTo(destinationWorkbasket.getId());
    assertThat(completedTask.getWorkbasketSummary().getId())
        .isEqualTo(destinationWorkbasket.getId());
  }

  @WithAccessId(user = USER)
  @Test
  void should_RejectCompletion_When_TaskIsTransferredWhileBeingCompletedInBulk() throws Exception {
    Task task = createTask(TaskState.CLAIMED);
    transferConcurrently(task.getId());

    BulkOperationResults<String, KadaiException> result =
        taskService.completeTasks(List.of(task.getId()));

    assertThat(interferer.getInterferenceCount()).isOne();
    assertThat(result.getFailedIds()).containsExactly(task.getId());
    assertThat(result.getErrorForId(task.getId())).isInstanceOf(InvalidTaskStateException.class);
    Task persistedTask = taskService.getTask(task.getId());
    assertThat(persistedTask.getState()).isEqualTo(TaskState.READY);
    assertThat(persistedTask.getCompleted()).isNull();
    assertThat(persistedTask.getWorkbasketSummary().getId())
        .isEqualTo(destinationWorkbasket.getId());
  }

  @WithAccessId(user = USER)
  @Test
  void should_ClaimTaskAgain_When_TaskIsTransferredWhileBeingForceCompletedInBulk()
      throws Exception {
    Task task = createTask(TaskState.CLAIMED);
    AtomicReference<Instant> transferredAt = new AtomicReference<>();
    interferer.interfereOnce(() ->
        transferredAt.set(
            taskService.transfer(task.getId(), destinationWorkbasket.getId()).getModified()));

    BulkOperationResults<String, KadaiException> result =
        taskService.forceCompleteTasks(List.of(task.getId()));

    assertThat(interferer.getInterferenceCount()).isOne();
    assertThat(result.containsErrors()).isFalse();
    Task persistedTask = taskService.getTask(task.getId());
    assertThat(persistedTask.getState()).isEqualTo(TaskState.COMPLETED);
    assertThat(persistedTask.getOwner()).isEqualTo(USER);
    assertThat(persistedTask.getWorkbasketSummary().getId())
        .isEqualTo(destinationWorkbasket.getId());
    assertThat(persistedTask.getClaimed()).isAfter(transferredAt.get());
  }

  // endregion

  // region claim vs. complete / transfer (claim is part of force completion)

  @WithAccessId(user = USER)
  @Test
  void should_NotReopenTask_When_TaskIsCompletedWhileBeingClaimed() throws Exception {
    Task task = createTask(TaskState.READY);
    completeConcurrently(task.getId());

    ThrowingCallable call = () -> taskService.claim(task.getId());

    assertThatThrownBy(call).isInstanceOf(InvalidTaskStateException.class);
    assertThat(interferer.getInterferenceCount()).isOne();
    Task persistedTask = taskService.getTask(task.getId());
    assertThat(persistedTask.getState()).isEqualTo(TaskState.COMPLETED);
  }

  @WithAccessId(user = USER)
  @Test
  void should_ClaimTaskInNewWorkbasket_When_TaskIsTransferredWhileBeingClaimed() throws Exception {
    Task task = createTask(TaskState.READY);
    transferConcurrently(task.getId());

    taskService.claim(task.getId());

    assertThat(interferer.getInterferenceCount()).isOne();
    Task persistedTask = taskService.getTask(task.getId());
    assertThat(persistedTask.getState()).isEqualTo(TaskState.CLAIMED);
    assertThat(persistedTask.getOwner()).isEqualTo(USER);
    assertThat(persistedTask.getWorkbasketSummary().getId())
        .isEqualTo(destinationWorkbasket.getId());
  }

  // endregion

  // region retries are bounded

  @WithAccessId(user = USER)
  @Test
  void should_ThrowConcurrencyException_When_TaskIsModifiedOnEveryTransferAttempt()
      throws Exception {
    Task task = createTask(TaskState.CLAIMED);
    interferer.interfereAlways(() -> taskService.setTaskRead(task.getId(), true));

    ThrowingCallable call = () -> taskService.transfer(task.getId(), destinationWorkbasket.getId());

    assertThatThrownBy(call).isInstanceOf(ConcurrencyException.class);
    assertThat(interferer.getInterferenceCount()).isGreaterThan(1);
    Task persistedTask = taskService.getTask(task.getId());
    assertThat(persistedTask.getState()).isEqualTo(TaskState.CLAIMED);
    assertThat(persistedTask.getWorkbasketSummary().getId()).isEqualTo(sourceWorkbasket.getId());
  }

  @WithAccessId(user = USER)
  @Test
  void should_ReportConcurrencyException_When_TaskIsModifiedOnEveryBulkTransferAttempt()
      throws Exception {
    Task task = createTask(TaskState.CLAIMED);
    interferer.interfereAlways(() -> taskService.setTaskRead(task.getId(), true));

    BulkOperationResults<String, KadaiException> result =
        taskService.transferTasks(destinationWorkbasket.getId(), List.of(task.getId()));

    assertThat(interferer.getInterferenceCount()).isGreaterThan(1);
    assertThat(result.getFailedIds()).containsExactly(task.getId());
    assertThat(result.getErrorForId(task.getId())).isInstanceOf(ConcurrencyException.class);
    Task persistedTask = taskService.getTask(task.getId());
    assertThat(persistedTask.getState()).isEqualTo(TaskState.CLAIMED);
    assertThat(persistedTask.getWorkbasketSummary().getId()).isEqualTo(sourceWorkbasket.getId());
  }

  // endregion

  // region side effects

  @WithAccessId(user = USER)
  @Test
  void should_PublishOneTransferEventPerTask_When_TransferringTasksInDifferentStates()
      throws Exception {
    Task readyTask = createTask(TaskState.READY);
    Task readyForReviewTask = createTask(TaskState.READY_FOR_REVIEW);

    BulkOperationResults<String, KadaiException> result =
        taskService.transferTasks(
            destinationWorkbasket.getId(), List.of(readyTask.getId(), readyForReviewTask.getId()));

    assertThat(result.containsErrors()).isFalse();
    assertThat(TransferEventRecorder.getTransferredTaskIds())
        .containsExactlyInAnyOrder(readyTask.getId(), readyForReviewTask.getId());
  }

  @WithAccessId(user = USER)
  @Test
  void should_TransferAndCompleteTask_When_PersistedModifiedHasSubMillisecondPrecision()
      throws Exception {
    // adding a comment persists the current time with the full precision of the database
    Task task = createTask(TaskState.CLAIMED);
    TaskCommentBuilder.newTaskComment()
        .taskId(task.getId())
        .textField("comment")
        .buildAndStore(taskService);

    taskService.transfer(task.getId(), destinationWorkbasket.getId());
    TaskCommentBuilder.newTaskComment()
        .taskId(task.getId())
        .textField("comment")
        .buildAndStore(taskService);
    BulkOperationResults<String, KadaiException> result =
        taskService.forceCompleteTasks(List.of(task.getId()));

    assertThat(result.containsErrors()).isFalse();
    Task persistedTask = taskService.getTask(task.getId());
    assertThat(persistedTask.getState()).isEqualTo(TaskState.COMPLETED);
    assertThat(persistedTask.getWorkbasketSummary().getId())
        .isEqualTo(destinationWorkbasket.getId());
  }

  @WithAccessId(user = USER)
  @Test
  void should_ReturnPersistedModifiedTimestamp_When_TaskIsTransferred() throws Exception {
    Task task = createTask(TaskState.READY);

    Task transferredTask = taskService.transfer(task.getId(), destinationWorkbasket.getId());

    assertThat(transferredTask.getModified())
        .isEqualTo(taskService.getTask(task.getId()).getModified());
  }

  @WithAccessId(user = USER)
  @Test
  void should_ReturnPersistedModifiedTimestamp_When_TaskIsClaimed() throws Exception {
    Task task = createTask(TaskState.READY);

    Task claimedTask = taskService.claim(task.getId());

    assertThat(claimedTask.getModified())
        .isEqualTo(taskService.getTask(task.getId()).getModified());
  }

  @WithAccessId(user = USER)
  @Test
  void should_ReturnPersistedModifiedTimestamp_When_TaskIsForceCompleted() throws Exception {
    Task task = createTask(TaskState.READY);

    Task completedTask = taskService.forceCompleteTask(task.getId());

    Task persistedTask = taskService.getTask(task.getId());
    assertThat(completedTask.getModified()).isEqualTo(persistedTask.getModified());
    assertThat(completedTask.getCompleted()).isEqualTo(persistedTask.getCompleted());
  }

  // endregion

  private Task createTask(TaskState state) throws Exception {
    TaskBuilder builder =
        TaskBuilder.newTask()
            .classificationSummary(classification)
            .workbasketSummary(sourceWorkbasket)
            .primaryObjRef(objectReference)
            .state(state);
    if (state.isClaimedState()) {
      builder.owner(USER).claimed(CLAIMED_AT);
    }
    return builder.buildAndStore(taskService);
  }

  private void completeConcurrently(String taskId) {
    interferer.interfereOnce(() -> {
      BulkOperationResults<String, KadaiException> result =
          taskService.forceCompleteTasks(List.of(taskId));
      assertThat(result.containsErrors()).isFalse();
    });
  }

  private void transferConcurrently(String taskId) {
    interferer.interfereOnce(() -> taskService.transfer(taskId, destinationWorkbasket.getId()));
  }

  static class TransferEventRecorder implements KadaiEventConsumer<TaskTransferredEvent> {

    private static final List<String> TRANSFERRED_TASK_IDS = new ArrayList<>();

    static synchronized List<String> getTransferredTaskIds() {
      return List.copyOf(TRANSFERRED_TASK_IDS);
    }

    static synchronized void clear() {
      TRANSFERRED_TASK_IDS.clear();
    }

    private static synchronized void record(String taskId) {
      TRANSFERRED_TASK_IDS.add(taskId);
    }

    @Override
    public void initialize(KadaiEngine kadaiEngine) {
      // nothing to initialize
    }

    @Override
    public void consume(TaskTransferredEvent event) {
      record(event.getTaskId());
    }

    @Override
    public Class<TaskTransferredEvent> reify() {
      return TaskTransferredEvent.class;
    }
  }
}
