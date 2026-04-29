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

package acceptance.jobs;

import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.KadaiConfiguration.Builder;
import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.common.api.KadaiEngine;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.TaskState;
import io.kadai.task.api.models.ObjectReference;
import io.kadai.task.api.models.TaskSummary;
import io.kadai.task.internal.jobs.TaskCleanupJob;
import io.kadai.testapi.DefaultTestEntities;
import io.kadai.testapi.KadaiConfigurationModifier;
import io.kadai.testapi.KadaiInject;
import io.kadai.testapi.KadaiIntegrationTest;
import io.kadai.testapi.builder.TaskBuilder;
import io.kadai.testapi.security.WithAccessId;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.function.ThrowingConsumer;

// All tests are executed as admin, because the jobrunner needs admin rights.
@KadaiIntegrationTest
class TaskCleanupJobAccTest {

  @KadaiInject TaskService taskService;
  @KadaiInject WorkbasketService workbasketService;
  @KadaiInject ClassificationService classificationService;

  ClassificationSummary classification;
  ObjectReference primaryObjRef;

  @WithAccessId(user = "businessadmin")
  @BeforeAll
  void setup() throws Exception {
    classification =
        DefaultTestEntities.defaultTestClassification()
            .buildAndStoreAsSummary(classificationService);
    primaryObjRef = DefaultTestEntities.defaultTestObjectReference().build();
  }

  private TaskBuilder newTaskBuilder(WorkbasketSummary workbasket) {
    return TaskBuilder.newTask()
        .workbasketSummary(workbasket)
        .classificationSummary(classification)
        .primaryObjRef(primaryObjRef);
  }

  private List<TaskSummary> tasksForWorkbasket(WorkbasketSummary workbasket) throws Exception {
    return taskService.createTaskQuery().list().stream()
        .filter(t -> t.getWorkbasketSummary().equals(workbasket))
        .toList();
  }

  private void runTaskCleanupJob(KadaiEngine kadaiEngine) throws Exception {
    new TaskCleanupJob(kadaiEngine, null, null).run();
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class CleanCompletedTasks implements KadaiConfigurationModifier {

    WorkbasketSummary workbasket;

    @KadaiInject KadaiEngine kadaiEngine;

    @Override
    public Builder modify(Builder builder) {
      return builder
          .taskCleanupJobEnabled(true)
          .jobFirstRun(Instant.now().minus(10, ChronoUnit.MILLIS))
          .jobRunEvery(Duration.ofMillis(1))
          .taskCleanupJobMinimumAge(Duration.ofDays(5))
          .taskCleanupJobAllCompletedSameParentBusiness(false);
    }

    @WithAccessId(user = "businessadmin")
    @BeforeAll
    void setup() throws Exception {
      workbasket =
          DefaultTestEntities.defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);
    }

    @WithAccessId(user = "admin")
    @Test
    void should_CleanCompletedTasks_When_CompletedTimestampIsOlderThenTaskCleanupJobMinimumAge()
        throws Exception {
      TaskBuilder.newTask()
          .workbasketSummary(workbasket)
          .classificationSummary(classification)
          .primaryObjRef(primaryObjRef)
          .state(TaskState.COMPLETED)
          .completed(Instant.now().minus(6, ChronoUnit.DAYS))
          .buildAndStoreAsSummary(taskService);

      TaskCleanupJob job = new TaskCleanupJob(kadaiEngine, null, null);
      job.run();

      List<TaskSummary> taskSummaries = taskService.createTaskQuery().list();
      assertThat(taskSummaries)
          .filteredOn(t -> t.getWorkbasketSummary().equals(workbasket))
          .isEmpty();
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class NotCleanCompletedTasksWhereDateIsNotReached implements KadaiConfigurationModifier {

    @KadaiInject KadaiEngine kadaiEngine;

    @Override
    public Builder modify(Builder builder) {
      return builder
          .taskCleanupJobEnabled(true)
          .jobFirstRun(Instant.now().minus(10, ChronoUnit.MILLIS))
          .jobRunEvery(Duration.ofMillis(1))
          .taskCleanupJobMinimumAge(Duration.ofDays(5))
          .taskCleanupJobAllCompletedSameParentBusiness(false);
    }

    @WithAccessId(user = "admin")
    @Test
    void should_NotCleanCompletedTasksAfterDefinedDay() throws Exception {
      WorkbasketSummary workbasket =
          DefaultTestEntities.defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);
      TaskSummary taskSummary =
          TaskBuilder.newTask()
              .workbasketSummary(workbasket)
              .classificationSummary(classification)
              .primaryObjRef(primaryObjRef)
              .state(TaskState.COMPLETED)
              .completed(Instant.now().minus(3, ChronoUnit.DAYS))
              .buildAndStoreAsSummary(taskService);

      TaskCleanupJob job = new TaskCleanupJob(kadaiEngine, null, null);
      job.run();

      List<TaskSummary> taskSummaries = taskService.createTaskQuery().list();
      assertThat(taskSummaries)
          .filteredOn(t -> t.getWorkbasketSummary().equals(workbasket))
          .containsExactlyInAnyOrder(taskSummary);
    }

    @WithAccessId(user = "admin")
    @Test
    void should_OnlyCleanCompletedTasks_When_DefinedCompletedExceedThreshold() throws Exception {
      WorkbasketSummary workbasket =
          DefaultTestEntities.defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);
      TaskBuilder taskBuilder =
          TaskBuilder.newTask()
              .workbasketSummary(workbasket)
              .classificationSummary(classification)
              .primaryObjRef(primaryObjRef)
              .state(TaskState.COMPLETED)
              .completed(Instant.now().minus(3, ChronoUnit.DAYS));
      TaskSummary taskSummary1 = taskBuilder.buildAndStoreAsSummary(taskService);
      TaskSummary taskSummary2 =
          taskBuilder
              .completed(Instant.now().minus(10, ChronoUnit.DAYS))
              .buildAndStoreAsSummary(taskService);
      TaskCleanupJob job = new TaskCleanupJob(kadaiEngine, null, null);
      job.run();

      List<TaskSummary> taskSummaries = taskService.createTaskQuery().list();
      assertThat(taskSummaries)
          .filteredOn(t -> t.getWorkbasketSummary().equals(workbasket))
          .containsExactlyInAnyOrder(taskSummary1)
          .doesNotContain(taskSummary2);
    }

    @WithAccessId(user = "admin")
    @Test
    void should_CleanOldCompletedTask_When_GroupConstraintIsDisabled() throws Exception {
      WorkbasketSummary workbasket =
          DefaultTestEntities.defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);

      TaskSummary taskSummaryCompleted =
          newTaskBuilder(workbasket)
              .state(TaskState.COMPLETED)
              .completed(Instant.now().minus(10, ChronoUnit.DAYS))
              .parentBusinessProcessId("ParentProcessId_withoutConstraint")
              .buildAndStoreAsSummary(taskService);
      TaskSummary taskSummaryClaimed =
          newTaskBuilder(workbasket)
              .state(TaskState.CLAIMED)
              .completed(null)
              .parentBusinessProcessId("ParentProcessId_withoutConstraint")
              .buildAndStoreAsSummary(taskService);

      runTaskCleanupJob(kadaiEngine);

      assertThat(tasksForWorkbasket(workbasket))
          .containsExactlyInAnyOrder(taskSummaryClaimed)
          .doesNotContain(taskSummaryCompleted);
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class CleanCompletedTasksWithBusinessProcessId implements KadaiConfigurationModifier {

    @KadaiInject KadaiEngine kadaiEngine;

    @Override
    public Builder modify(Builder builder) {
      return builder
          .taskCleanupJobEnabled(true)
          .jobFirstRun(Instant.now().minus(10, ChronoUnit.MILLIS))
          .jobRunEvery(Duration.ofMillis(1))
          .taskCleanupJobMinimumAge(Duration.ofDays(5))
          .taskCleanupJobAllCompletedSameParentBusiness(true);
    }

    @WithAccessId(user = "admin")
    @Test
    void should_CleanCompletedTasksWithSameParentBusiness() throws Exception {
      WorkbasketSummary workbasket =
          DefaultTestEntities.defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);
      TaskBuilder taskBuilder =
          newTaskBuilder(workbasket)
              .state(TaskState.COMPLETED)
              .completed(Instant.now().minus(10, ChronoUnit.DAYS))
              .parentBusinessProcessId("ParentProcessId_1");
      taskBuilder.buildAndStoreAsSummary(taskService);
      taskBuilder.buildAndStoreAsSummary(taskService);

      runTaskCleanupJob(kadaiEngine);

      assertThat(tasksForWorkbasket(workbasket)).isEmpty();
    }

    @WithAccessId(user = "admin")
    @Test
    void should_CleanSingleCompletedTaskWithParentBusinessProcessId_When_NoSiblingExists()
        throws Exception {
      WorkbasketSummary workbasket =
          DefaultTestEntities.defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);

      newTaskBuilder(workbasket)
          .state(TaskState.COMPLETED)
          .completed(Instant.now().minus(10, ChronoUnit.DAYS))
          .parentBusinessProcessId("ParentProcessId_single_oldEnough")
          .buildAndStoreAsSummary(taskService);

      runTaskCleanupJob(kadaiEngine);

      assertThat(tasksForWorkbasket(workbasket)).isEmpty();
    }

    @WithAccessId(user = "admin")
    @Test
    void should_NotCleanSingleParentBusinessTask_When_NotOldEnough() throws Exception {
      WorkbasketSummary workbasket =
          DefaultTestEntities.defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);

      TaskSummary taskSummary =
          newTaskBuilder(workbasket)
              .state(TaskState.COMPLETED)
              .completed(Instant.now().minus(3, ChronoUnit.DAYS))
              .parentBusinessProcessId("ParentProcessId_single_notOldEnough")
              .buildAndStoreAsSummary(taskService);

      runTaskCleanupJob(kadaiEngine);

      assertThat(tasksForWorkbasket(workbasket)).containsExactlyInAnyOrder(taskSummary);
    }

    @WithAccessId(user = "admin")
    @Test
    void should_NotCleanCompletedTasksWithSameParentBusiness_When_OneSubTaskIsIncomplete()
        throws Exception {
      WorkbasketSummary workbasket =
          DefaultTestEntities.defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);
      TaskBuilder taskBuilder =
          newTaskBuilder(workbasket)
              .state(TaskState.COMPLETED)
              .completed(Instant.now().minus(10, ChronoUnit.DAYS))
              .parentBusinessProcessId("ParentProcessId_3");

      TaskSummary taskSummaryCompleted1 = taskBuilder.buildAndStoreAsSummary(taskService);
      TaskSummary taskSummaryCompleted2 = taskBuilder.buildAndStoreAsSummary(taskService);

      TaskSummary taskSummaryCompleted =
          taskBuilder
              .parentBusinessProcessId("ParentProcessId_4")
              .buildAndStoreAsSummary(taskService);
      TaskSummary taskSummaryClaimed =
          taskBuilder
              .parentBusinessProcessId("ParentProcessId_4")
              .state(TaskState.CLAIMED)
              .completed(null)
              .buildAndStoreAsSummary(taskService);

      runTaskCleanupJob(kadaiEngine);

      assertThat(tasksForWorkbasket(workbasket))
          .containsExactlyInAnyOrder(taskSummaryCompleted, taskSummaryClaimed)
          .doesNotContain(taskSummaryCompleted1, taskSummaryCompleted2);
    }

    @WithAccessId(user = "admin")
    @Test
    void should_NotCleanCompletedTasksWithSameParentBusiness_When_OneSubTaskIsNotOldEnough()
        throws Exception {
      WorkbasketSummary workbasket =
          DefaultTestEntities.defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);
      TaskBuilder taskBuilder =
          newTaskBuilder(workbasket)
              .state(TaskState.COMPLETED)
              .completed(Instant.now().minus(10, ChronoUnit.DAYS))
              .parentBusinessProcessId("ParentProcessId_5");

      TaskSummary taskSummaryOldEnough = taskBuilder.buildAndStoreAsSummary(taskService);
      TaskSummary taskSummaryNotOldEnough =
          taskBuilder
              .completed(Instant.now().minus(3, ChronoUnit.DAYS))
              .buildAndStoreAsSummary(taskService);

      runTaskCleanupJob(kadaiEngine);

      assertThat(tasksForWorkbasket(workbasket))
          .containsExactlyInAnyOrder(taskSummaryOldEnough, taskSummaryNotOldEnough);
    }

    @WithAccessId(user = "admin")
    @Test
    void should_CleanCompletedTasksWithSameParentBusinessAcrossWorkbaskets_When_AllAreOldEnough()
        throws Exception {
      WorkbasketSummary workbasket1 =
          DefaultTestEntities.defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);
      WorkbasketSummary workbasket2 =
          DefaultTestEntities.defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);

      newTaskBuilder(workbasket1)
          .state(TaskState.COMPLETED)
          .completed(Instant.now().minus(10, ChronoUnit.DAYS))
          .parentBusinessProcessId("ParentProcessId_acrossWorkbaskets_oldEnough")
          .buildAndStoreAsSummary(taskService);
      newTaskBuilder(workbasket2)
          .state(TaskState.COMPLETED)
          .completed(Instant.now().minus(8, ChronoUnit.DAYS))
          .parentBusinessProcessId("ParentProcessId_acrossWorkbaskets_oldEnough")
          .buildAndStoreAsSummary(taskService);

      runTaskCleanupJob(kadaiEngine);

      assertThat(tasksForWorkbasket(workbasket1)).isEmpty();
      assertThat(tasksForWorkbasket(workbasket2)).isEmpty();
    }

    @WithAccessId(user = "admin")
    @Test
    void should_NotCleanParentBusinessGroupAcrossWorkbaskets_When_OtherTaskIsIncomplete()
        throws Exception {
      WorkbasketSummary workbasket1 =
          DefaultTestEntities.defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);
      WorkbasketSummary workbasket2 =
          DefaultTestEntities.defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);

      TaskSummary taskSummaryCompleted =
          newTaskBuilder(workbasket1)
              .state(TaskState.COMPLETED)
              .completed(Instant.now().minus(10, ChronoUnit.DAYS))
              .parentBusinessProcessId("ParentProcessId_acrossWorkbaskets_incomplete")
              .buildAndStoreAsSummary(taskService);
      TaskSummary taskSummaryClaimed =
          newTaskBuilder(workbasket2)
              .state(TaskState.CLAIMED)
              .completed(null)
              .parentBusinessProcessId("ParentProcessId_acrossWorkbaskets_incomplete")
              .buildAndStoreAsSummary(taskService);

      runTaskCleanupJob(kadaiEngine);

      assertThat(tasksForWorkbasket(workbasket1)).containsExactlyInAnyOrder(taskSummaryCompleted);
      assertThat(tasksForWorkbasket(workbasket2)).containsExactlyInAnyOrder(taskSummaryClaimed);
    }

    @WithAccessId(user = "admin")
    @Test
    void should_CleanOnlyEligibleTaskGroups_When_MixingGroupedAndUngroupedTasks() throws Exception {
      WorkbasketSummary workbasket =
          DefaultTestEntities.defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);

      TaskSummary eligibleGroupedTask1 =
          newTaskBuilder(workbasket)
              .state(TaskState.COMPLETED)
              .completed(Instant.now().minus(10, ChronoUnit.DAYS))
              .parentBusinessProcessId("ParentProcessId_mixed_eligible")
              .buildAndStoreAsSummary(taskService);
      TaskSummary eligibleGroupedTask2 =
          newTaskBuilder(workbasket)
              .state(TaskState.COMPLETED)
              .completed(Instant.now().minus(7, ChronoUnit.DAYS))
              .parentBusinessProcessId("ParentProcessId_mixed_eligible")
              .buildAndStoreAsSummary(taskService);
      TaskSummary blockedByIncompleteCompleted =
          newTaskBuilder(workbasket)
              .state(TaskState.COMPLETED)
              .completed(Instant.now().minus(10, ChronoUnit.DAYS))
              .parentBusinessProcessId("ParentProcessId_mixed_incomplete")
              .buildAndStoreAsSummary(taskService);
      TaskSummary blockedByIncompleteClaimed =
          newTaskBuilder(workbasket)
              .state(TaskState.CLAIMED)
              .completed(null)
              .parentBusinessProcessId("ParentProcessId_mixed_incomplete")
              .buildAndStoreAsSummary(taskService);
      TaskSummary blockedByRecentOldEnough =
          newTaskBuilder(workbasket)
              .state(TaskState.COMPLETED)
              .completed(Instant.now().minus(10, ChronoUnit.DAYS))
              .parentBusinessProcessId("ParentProcessId_mixed_recent")
              .buildAndStoreAsSummary(taskService);
      TaskSummary blockedByRecentNotOldEnough =
          newTaskBuilder(workbasket)
              .state(TaskState.COMPLETED)
              .completed(Instant.now().minus(3, ChronoUnit.DAYS))
              .parentBusinessProcessId("ParentProcessId_mixed_recent")
              .buildAndStoreAsSummary(taskService);
      TaskSummary nullParentCompleted =
          newTaskBuilder(workbasket)
              .state(TaskState.COMPLETED)
              .completed(Instant.now().minus(10, ChronoUnit.DAYS))
              .parentBusinessProcessId(null)
              .buildAndStoreAsSummary(taskService);
      TaskSummary nullParentClaimed =
          newTaskBuilder(workbasket)
              .state(TaskState.CLAIMED)
              .completed(null)
              .parentBusinessProcessId(null)
              .buildAndStoreAsSummary(taskService);
      TaskSummary emptyParentCompleted =
          newTaskBuilder(workbasket)
              .state(TaskState.COMPLETED)
              .completed(Instant.now().minus(10, ChronoUnit.DAYS))
              .parentBusinessProcessId("")
              .buildAndStoreAsSummary(taskService);
      TaskSummary emptyParentNotOldEnough =
          newTaskBuilder(workbasket)
              .state(TaskState.COMPLETED)
              .completed(Instant.now().minus(3, ChronoUnit.DAYS))
              .parentBusinessProcessId("")
              .buildAndStoreAsSummary(taskService);
      TaskSummary singleGroupedOldEnough =
          newTaskBuilder(workbasket)
              .state(TaskState.COMPLETED)
              .completed(Instant.now().minus(10, ChronoUnit.DAYS))
              .parentBusinessProcessId("ParentProcessId_mixed_single")
              .buildAndStoreAsSummary(taskService);

      runTaskCleanupJob(kadaiEngine);

      assertThat(tasksForWorkbasket(workbasket))
          .containsExactlyInAnyOrder(
              blockedByIncompleteCompleted,
              blockedByIncompleteClaimed,
              blockedByRecentOldEnough,
              blockedByRecentNotOldEnough,
              nullParentClaimed,
              emptyParentNotOldEnough)
          .doesNotContain(
              eligibleGroupedTask1,
              eligibleGroupedTask2,
              nullParentCompleted,
              emptyParentCompleted,
              singleGroupedOldEnough);
    }

    @WithAccessId(user = "admin")
    @TestFactory
    Stream<DynamicTest>
        should_DeleteCompletedTaskWithParentBusinessEmptyOrNull_When_RunningCleanupJob() {
      Iterator<String> iterator = Arrays.asList("", null).iterator();

      ThrowingConsumer<String> test =
          parentBusinessId -> {
            WorkbasketSummary workbasket =
                DefaultTestEntities.defaultTestWorkbasket()
                    .buildAndStoreAsSummary(workbasketService);
            TaskBuilder taskBuilder =
                newTaskBuilder(workbasket)
                    .state(TaskState.COMPLETED)
                    .completed(Instant.now().minus(10, ChronoUnit.DAYS))
                    .parentBusinessProcessId(parentBusinessId);

            TaskSummary taskSummaryCompleted = taskBuilder.buildAndStoreAsSummary(taskService);
            TaskSummary taskSummaryClaimed =
                taskBuilder
                    .state(TaskState.CLAIMED)
                    .completed(null)
                    .buildAndStoreAsSummary(taskService);

            runTaskCleanupJob(kadaiEngine);

            assertThat(tasksForWorkbasket(workbasket))
                .containsExactlyInAnyOrder(taskSummaryClaimed)
                .doesNotContain(taskSummaryCompleted);
          };
      return DynamicTest.stream(iterator, c -> "for parentBusinessProcessId = '" + c + "'", test);
    }

    @WithAccessId(user = "admin")
    @TestFactory
    Stream<DynamicTest>
        should_DeleteCompletedTaskWithParentBusinessEmptyOrNull_When_SiblingIsNotOldEnough() {
      Iterator<String> iterator = Arrays.asList("", null).iterator();

      ThrowingConsumer<String> test =
          parentBusinessId -> {
            WorkbasketSummary workbasket =
                DefaultTestEntities.defaultTestWorkbasket()
                    .buildAndStoreAsSummary(workbasketService);
            TaskSummary taskSummaryCompleted =
                newTaskBuilder(workbasket)
                    .state(TaskState.COMPLETED)
                    .completed(Instant.now().minus(10, ChronoUnit.DAYS))
                    .parentBusinessProcessId(parentBusinessId)
                    .buildAndStoreAsSummary(taskService);
            TaskSummary taskSummaryNotOldEnough =
                newTaskBuilder(workbasket)
                    .state(TaskState.COMPLETED)
                    .completed(Instant.now().minus(3, ChronoUnit.DAYS))
                    .parentBusinessProcessId(parentBusinessId)
                    .buildAndStoreAsSummary(taskService);

            runTaskCleanupJob(kadaiEngine);

            assertThat(tasksForWorkbasket(workbasket))
                .containsExactlyInAnyOrder(taskSummaryNotOldEnough)
                .doesNotContain(taskSummaryCompleted);
          };
      return DynamicTest.stream(iterator, c -> "for parentBusinessProcessId = '" + c + "'", test);
    }
  }
}
