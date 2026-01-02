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

package acceptance.task.reopen;

import static io.kadai.testapi.DefaultTestEntities.defaultTestClassification;
import static io.kadai.testapi.DefaultTestEntities.defaultTestObjectReference;
import static io.kadai.testapi.DefaultTestEntities.defaultTestWorkbasket;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import acceptance.task.reopen.ReopenTaskAccTest.DummyPriorityServiceProvider;
import io.kadai.KadaiConfiguration;
import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.security.UserPrincipal;
import io.kadai.common.internal.util.EnumUtil;
import io.kadai.spi.priority.api.PriorityServiceProvider;
import io.kadai.task.api.CallbackState;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.TaskState;
import io.kadai.task.api.exceptions.InvalidTaskStateException;
import io.kadai.task.api.exceptions.ReopenTaskWithCallbackException;
import io.kadai.task.api.models.ObjectReference;
import io.kadai.task.api.models.Task;
import io.kadai.task.api.models.TaskSummary;
import io.kadai.task.internal.jobs.TaskCleanupJob;
import io.kadai.testapi.KadaiConfigurationModifier;
import io.kadai.testapi.KadaiInject;
import io.kadai.testapi.KadaiIntegrationTest;
import io.kadai.testapi.WithServiceProvider;
import io.kadai.testapi.builder.TaskBuilder;
import io.kadai.testapi.builder.UserBuilder;
import io.kadai.testapi.builder.WorkbasketAccessItemBuilder;
import io.kadai.testapi.security.WithAccessId;
import io.kadai.user.api.UserService;
import io.kadai.workbasket.api.WorkbasketPermission;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.exceptions.NotAuthorizedOnWorkbasketException;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import java.security.Principal;
import java.security.PrivilegedExceptionAction;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.OptionalInt;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.security.auth.Subject;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@KadaiIntegrationTest
@WithServiceProvider(
    serviceProviderInterface = PriorityServiceProvider.class,
    serviceProviders = DummyPriorityServiceProvider.class)
public class ReopenTaskAccTest implements KadaiConfigurationModifier {

  @KadaiInject KadaiEngine kadaiEngine;
  @KadaiInject TaskService taskService;
  @KadaiInject ClassificationService classificationService;
  @KadaiInject WorkbasketService workbasketService;
  @KadaiInject UserService userService;

  ClassificationSummary defaultClassificationSummary;
  WorkbasketSummary defaultWorkbasketSummary;
  ObjectReference defaultObjectReference;

  @Override
  public KadaiConfiguration.Builder modify(KadaiConfiguration.Builder builder) {
    return builder
        .addAdditionalUserInfo(true)
        .taskCleanupJobEnabled(true)
        .taskCleanupJobMinimumAge(Duration.ofNanos(0L));
  }

  @BeforeAll
  @WithAccessId(user = "businessadmin")
  void setup() throws Exception {
    defaultClassificationSummary =
        defaultTestClassification().buildAndStoreAsSummary(classificationService);
    defaultWorkbasketSummary = defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);
    defaultObjectReference = defaultTestObjectReference().build();

    WorkbasketAccessItemBuilder.newWorkbasketAccessItem()
        .workbasketId(defaultWorkbasketSummary.getId())
        .accessId("user-1-2")
        .permission(WorkbasketPermission.OPEN)
        .permission(WorkbasketPermission.READ)
        .permission(WorkbasketPermission.READTASKS)
        .permission(WorkbasketPermission.EDITTASKS)
        .permission(WorkbasketPermission.APPEND)
        .buildAndStore(workbasketService);
    UserBuilder.newUser()
        .id("user-1-2")
        .firstName("Max")
        .lastName("Mustermann")
        .longName("Long name of user-1-2")
        .buildAndStore(userService);

    WorkbasketAccessItemBuilder.newWorkbasketAccessItem()
        .workbasketId(defaultWorkbasketSummary.getId())
        .accessId("user-1-3")
        .permission(WorkbasketPermission.OPEN)
        .permission(WorkbasketPermission.READ)
        .permission(WorkbasketPermission.READTASKS)
        .permission(WorkbasketPermission.EDITTASKS)
        .permission(WorkbasketPermission.APPEND)
        .buildAndStore(workbasketService);
    UserBuilder.newUser()
        .id("user-1-3")
        .firstName("Maximiliane")
        .lastName("Musterfrau")
        .longName("Long name of user-1-3")
        .buildAndStore(userService);

    WorkbasketAccessItemBuilder.newWorkbasketAccessItem()
        .workbasketId(defaultWorkbasketSummary.getId())
        .accessId("user-1-4")
        .permission(WorkbasketPermission.READ)
        .permission(WorkbasketPermission.READTASKS)
        .buildAndStore(workbasketService);
    UserBuilder.newUser()
        .id("user-1-4")
        .firstName("Maximiliane")
        .lastName("Musterfrau")
        .longName("Long name of user-1-4")
        .buildAndStore(userService);
  }

  @ParameterizedTest
  @MethodSource("provideNonFinalEndStates")
  @WithAccessId(user = "user-1-2")
  void should_ReopenTask_For_NonFinalEndState(TaskState state) throws Exception {
    Task task =
        TaskBuilder.newTask()
            .classificationSummary(defaultClassificationSummary)
            .workbasketSummary(defaultWorkbasketSummary)
            .primaryObjRef(defaultObjectReference)
            .state(state)
            .buildAndStore(taskService);

    Task reopenedTask = taskService.reopen(task.getId());

    assertThat(reopenedTask).isNotNull().extracting(Task::getState).isEqualTo(TaskState.CLAIMED);
  }

  @ParameterizedTest
  @MethodSource("provideInvalidStates")
  @WithAccessId(user = "user-1-2")
  void should_ThrowInvalidTaskStateException_For_InvalidStates(TaskState state) throws Exception {
    Task task =
        TaskBuilder.newTask()
            .classificationSummary(defaultClassificationSummary)
            .workbasketSummary(defaultWorkbasketSummary)
            .primaryObjRef(defaultObjectReference)
            .state(state)
            .buildAndStore(taskService);

    ThrowingCallable call = () -> taskService.reopen(task.getId());

    assertThatExceptionOfType(InvalidTaskStateException.class)
        .isThrownBy(call)
        .extracting(InvalidTaskStateException::getTaskState)
        .isEqualTo(state);
  }

  @Test
  @WithAccessId(user = "user-1-2")
  void should_ReopenTask_When_CallbackIsNone() throws Exception {
    Task task =
        TaskBuilder.newTask()
            .classificationSummary(defaultClassificationSummary)
            .workbasketSummary(defaultWorkbasketSummary)
            .primaryObjRef(defaultObjectReference)
            .state(TaskState.COMPLETED)
            .callbackState(CallbackState.NONE)
            .buildAndStore(taskService);

    Task reopenedTask = taskService.reopen(task.getId());

    assertThat(reopenedTask).isNotNull().extracting(Task::getState).isEqualTo(TaskState.CLAIMED);
  }

  @Test
  @WithAccessId(user = "user-1-2")
  void should_ReopenTask_When_CallbackIsNull() throws Exception {
    Task task =
        TaskBuilder.newTask()
            .classificationSummary(defaultClassificationSummary)
            .workbasketSummary(defaultWorkbasketSummary)
            .primaryObjRef(defaultObjectReference)
            .state(TaskState.COMPLETED)
            .callbackState(null)
            .buildAndStore(taskService);

    Task reopenedTask = taskService.reopen(task.getId());

    assertThat(reopenedTask).isNotNull().extracting(Task::getState).isEqualTo(TaskState.CLAIMED);
  }

  @ParameterizedTest
  @MethodSource("provideActualCallbackStates")
  @WithAccessId(user = "user-1-2")
  void should_ThrowReopenTaskWithCallbackException_For_Callback(CallbackState callbackState)
      throws Exception {
    Task task =
        TaskBuilder.newTask()
            .classificationSummary(defaultClassificationSummary)
            .workbasketSummary(defaultWorkbasketSummary)
            .primaryObjRef(defaultObjectReference)
            .state(TaskState.COMPLETED)
            .callbackState(callbackState)
            .buildAndStore(taskService);

    ThrowingCallable call = () -> taskService.reopen(task.getId());

    assertThatExceptionOfType(ReopenTaskWithCallbackException.class)
        .isThrownBy(call)
        .extracting(ReopenTaskWithCallbackException::getTaskId)
        .isEqualTo(task.getId());
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  @WithAccessId(user = "user-1-2")
  void should_ReopenTask_When_CompletedReopenedForTimes(int n) throws Exception {
    Task task =
        TaskBuilder.newTask()
            .classificationSummary(defaultClassificationSummary)
            .workbasketSummary(defaultWorkbasketSummary)
            .primaryObjRef(defaultObjectReference)
            .state(TaskState.READY)
            .buildAndStore(taskService);

    taskService.claim(task.getId());

    Task reopenedTask = null;

    for (int i = 0; i < n; i++) {
      taskService.completeTask(task.getId());
      reopenedTask = taskService.reopen(task.getId());
    }

    assertThat(reopenedTask).isNotNull().extracting(Task::getState).isEqualTo(TaskState.CLAIMED);
  }

  @Test
  @WithAccessId(user = "user-1-2")
  void should_SetOwnerToReopener_When_ReopenSuccessful() throws Exception {
    Task task =
        TaskBuilder.newTask()
            .classificationSummary(defaultClassificationSummary)
            .workbasketSummary(defaultWorkbasketSummary)
            .primaryObjRef(defaultObjectReference)
            .state(TaskState.COMPLETED)
            .buildAndStore(taskService);

    Subject subject = new Subject();
    Principal thatPrincipal = new UserPrincipal("user-1-3");
    subject.getPrincipals().add(thatPrincipal);
    PrivilegedExceptionAction<Task> reopenAction = () -> taskService.reopen(task.getId());
    Task reopenedTask = Subject.doAs(subject, reopenAction);

    assertThat(reopenedTask).isNotNull().extracting(Task::getState).isEqualTo(TaskState.CLAIMED);
    assertThat(reopenedTask).extracting(TaskSummary::getOwner).isEqualTo(thatPrincipal.getName());
    assertThat(reopenedTask)
        .extracting(TaskSummary::getOwnerLongName)
        .isEqualTo("Long name of user-1-3");
  }

  @Test
  @WithAccessId(user = "user-1-2")
  void should_SetModifiedNow_When_ReopenSuccessful() throws Exception {
    Instant then = Instant.now();
    Task task =
        TaskBuilder.newTask()
            .classificationSummary(defaultClassificationSummary)
            .workbasketSummary(defaultWorkbasketSummary)
            .primaryObjRef(defaultObjectReference)
            .state(TaskState.COMPLETED)
            .modified(then)
            .buildAndStore(taskService);

    Task reopenedTask = taskService.reopen(task.getId());

    assertThat(reopenedTask).isNotNull().extracting(Task::getState).isEqualTo(TaskState.CLAIMED);
    assertThat(reopenedTask.getModified()).isAfter(then);
  }

  @Test
  @WithAccessId(user = "user-1-2")
  void should_SetClaimedNow_When_ReopenSuccessful() throws Exception {
    Instant then = Instant.now();
    Task task =
        TaskBuilder.newTask()
            .classificationSummary(defaultClassificationSummary)
            .workbasketSummary(defaultWorkbasketSummary)
            .primaryObjRef(defaultObjectReference)
            .state(TaskState.COMPLETED)
            .claimed(then)
            .buildAndStore(taskService);

    Task reopenedTask = taskService.reopen(task.getId());

    assertThat(reopenedTask).isNotNull().extracting(Task::getState).isEqualTo(TaskState.CLAIMED);
    assertThat(reopenedTask.getClaimed()).isAfter(then);
  }

  @Test
  @WithAccessId(user = "user-1-2")
  void should_SetCompletedNull_When_ReopenSuccessful() throws Exception {
    Task task =
        TaskBuilder.newTask()
            .classificationSummary(defaultClassificationSummary)
            .workbasketSummary(defaultWorkbasketSummary)
            .primaryObjRef(defaultObjectReference)
            .state(TaskState.COMPLETED)
            .completed(Instant.now())
            .buildAndStore(taskService);

    Task reopenedTask = taskService.reopen(task.getId());

    assertThat(reopenedTask).isNotNull().extracting(Task::getState).isEqualTo(TaskState.CLAIMED);
    assertThat(reopenedTask.getCompleted()).isNull();
  }

  @Test
  @WithAccessId(user = "user-1-2")
  void should_SetReadFalse_When_ReopenSuccessful() throws Exception {
    Task task =
        TaskBuilder.newTask()
            .classificationSummary(defaultClassificationSummary)
            .workbasketSummary(defaultWorkbasketSummary)
            .primaryObjRef(defaultObjectReference)
            .state(TaskState.COMPLETED)
            .read(true)
            .buildAndStore(taskService);

    Task reopenedTask = taskService.reopen(task.getId());

    assertThat(reopenedTask).isNotNull().extracting(Task::getState).isEqualTo(TaskState.CLAIMED);
    assertThat(reopenedTask.isRead()).isFalse();
  }

  @Test
  @WithAccessId(user = "user-1-2")
  void should_SetReopenedTrue_When_ReopenSuccessful() throws Exception {
    Task task =
        TaskBuilder.newTask()
            .classificationSummary(defaultClassificationSummary)
            .workbasketSummary(defaultWorkbasketSummary)
            .primaryObjRef(defaultObjectReference)
            .state(TaskState.COMPLETED)
            .reopened(false)
            .buildAndStore(taskService);

    Task reopenedTask = taskService.reopen(task.getId());

    assertThat(reopenedTask).isNotNull().extracting(Task::getState).isEqualTo(TaskState.CLAIMED);
    assertThat(reopenedTask.isReopened()).isTrue();
  }

  @Test
  @WithAccessId(user = "user-1-2")
  void should_RecalculatePriority_When_ReopenSuccessful() throws Exception {
    final int previousPriority = DummyPriorityServiceProvider.SPI_PRIORITY + 1;
    Task task =
        TaskBuilder.newTask()
            .classificationSummary(defaultClassificationSummary)
            .workbasketSummary(defaultWorkbasketSummary)
            .primaryObjRef(defaultObjectReference)
            .state(TaskState.COMPLETED)
            .priority(previousPriority)
            .buildAndStore(taskService);

    Task reopenedTask = taskService.reopen(task.getId());

    assertThat(reopenedTask).isNotNull();
    assertThat(reopenedTask.getPriority()).isNotEqualTo(previousPriority);
  }

  @Test
  @WithAccessId(user = "admin")
  void should_NotCleanUpReopenedTask() throws Exception {
    Task task =
        TaskBuilder.newTask()
            .classificationSummary(defaultClassificationSummary)
            .workbasketSummary(defaultWorkbasketSummary)
            .primaryObjRef(defaultObjectReference)
            .state(TaskState.COMPLETED)
            .buildAndStore(taskService);

    Task reopenedTask = taskService.reopen(task.getId());
    assertThat(reopenedTask.getState()).isEqualTo(TaskState.CLAIMED);

    TaskCleanupJob job = new TaskCleanupJob(kadaiEngine, null, null);
    job.run();

    Task retrievedTask = taskService.getTask(task.getId());

    assertThat(retrievedTask).isNotNull();
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class PermissionsTest {

    Task task;

    @BeforeEach
    @WithAccessId(user = "user-1-2")
    void setUp() throws Exception {
      task =
          TaskBuilder.newTask()
              .classificationSummary(defaultClassificationSummary)
              .workbasketSummary(defaultWorkbasketSummary)
              .primaryObjRef(defaultObjectReference)
              .state(TaskState.COMPLETED)
              .buildAndStore(taskService);
    }

    @Test
    @WithAccessId(user = "user-1-4")
    void should_ThrowNotAuthorizedOnWorkbasketException_When_UserNotHasEditTasksPerm() {
      ThrowingCallable call = () -> taskService.reopen(task.getId());

      assertThatExceptionOfType(NotAuthorizedOnWorkbasketException.class).isThrownBy(call);
    }
  }

  static class DummyPriorityServiceProvider implements PriorityServiceProvider {
    static final int SPI_PRIORITY = 10;

    @Override
    public OptionalInt calculatePriority(TaskSummary taskSummary) {
      return OptionalInt.of(SPI_PRIORITY);
    }
  }

  private static Stream<Arguments> provideNonFinalEndStates() {
    return Arrays.stream(TaskState.END_STATES)
        .filter(Predicate.not(TaskState::isFinalState))
        .map(Arguments::of);
  }

  private static Stream<Arguments> provideFinalStates() {
    return Arrays.stream(TaskState.FINAL_STATES).map(Arguments::of);
  }

  private static Stream<Arguments> provideNonEndStates() {
    return Arrays.stream(EnumUtil.allValuesExceptFor(TaskState.END_STATES)).map(Arguments::of);
  }

  private static Stream<Arguments> provideInvalidStates() {
    return Stream.concat(provideFinalStates(), provideNonEndStates());
  }

  private static Stream<Arguments> provideActualCallbackStates() {
    return Arrays.stream(EnumUtil.allValuesExceptFor(CallbackState.NONE)).map(Arguments::of);
  }
}
