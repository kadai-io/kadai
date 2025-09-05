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

package acceptance.jobs;

import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.exceptions.KadaiException;
import io.kadai.common.internal.util.CheckedConsumer;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.TaskState;
import io.kadai.task.api.models.ObjectReference;
import io.kadai.task.api.models.TaskSummary;
import io.kadai.task.internal.jobs.TaskCleanupJob;
import io.kadai.testapi.DefaultTestEntities;
import io.kadai.testapi.KadaiInject;
import io.kadai.testapi.KadaiIntegrationTest;
import io.kadai.testapi.builder.TaskBuilder;
import io.kadai.testapi.security.WithAccessId;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import io.kadai.workbasket.internal.jobs.WorkbasketCleanupJob;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Acceptance test for all "jobs workbasket runner" scenarios. */
@KadaiIntegrationTest
class WorkbasketCleanupJobAccTest {

  @KadaiInject KadaiEngine kadaiEngine;
  @KadaiInject TaskService taskService;
  @KadaiInject WorkbasketService workbasketService;
  @KadaiInject ClassificationService classificationService;
  WorkbasketSummary defaultWorkbasketSummary;
  ClassificationSummary classification;
  ObjectReference primaryObjRef;

  @WithAccessId(user = "admin")
  @BeforeEach
  void setup() throws Exception {
    defaultWorkbasketSummary =
        DefaultTestEntities.defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);
    classification =
        DefaultTestEntities.defaultTestClassification()
            .buildAndStoreAsSummary(classificationService);
    primaryObjRef = DefaultTestEntities.defaultTestObjectReference().build();
    TaskBuilder.newTask()
        .workbasketSummary(defaultWorkbasketSummary)
        .classificationSummary(classification)
        .primaryObjRef(primaryObjRef)
        .state(TaskState.COMPLETED)
        .completed(Instant.now().minus(18, ChronoUnit.DAYS))
        .buildAndStoreAsSummary(taskService);
  }

  @WithAccessId(user = "admin")
  @AfterEach
  void after() throws KadaiException {
    List<TaskSummary> taskSummaryList = taskService.createTaskQuery().list();

    taskSummaryList.stream()
        .map(TaskSummary::getId)
        .forEach(CheckedConsumer.rethrowing(taskService::deleteTask));

    List<WorkbasketSummary> workbasketSummaryList =
        workbasketService.createWorkbasketQuery().list();

    workbasketSummaryList.stream()
        .map(WorkbasketSummary::getId)
        .forEach(CheckedConsumer.rethrowing(workbasketService::deleteWorkbasket));
  }

  @WithAccessId(user = "admin")
  @Test
  void should_CleanWorkbasket_When_MarkedForDeletionWithoutTasks() throws Exception {
    long totalWorkbasketCount = workbasketService.createWorkbasketQuery().count();
    assertThat(totalWorkbasketCount).isOne();
    WorkbasketSummary workbasket = workbasketService.createWorkbasketQuery().single();

    assertThat(getNumberTaskNotCompleted(workbasket.getId())).isZero();
    assertThat(getNumberTaskCompleted(workbasket.getId())).isOne();

    // Workbasket with completed task will be marked for deletion.
    workbasketService.deleteWorkbasket(workbasket.getId());

    // Run taskCleanupJob for deleting completing tasks before running workbasketCleanupJob
    TaskCleanupJob taskCleanupJob = new TaskCleanupJob(kadaiEngine, null, null);
    taskCleanupJob.run();

    assertThat(getNumberTaskCompleted(workbasket.getId())).isZero();

    WorkbasketCleanupJob workbasketCleanupJob = new WorkbasketCleanupJob(kadaiEngine, null, null);
    workbasketCleanupJob.run();

    totalWorkbasketCount = workbasketService.createWorkbasketQuery().count();
    assertThat(totalWorkbasketCount).isZero();
  }

  @WithAccessId(user = "admin")
  @Test
  void should_NotCleanWorkbasket_When_MarkedForDeletionIfWorkbasketHasTasks() throws Exception {
    long totalWorkbasketCount = workbasketService.createWorkbasketQuery().count();
    assertThat(totalWorkbasketCount).isOne();
    WorkbasketSummary workbasket = workbasketService.createWorkbasketQuery().single();

    assertThat(getNumberTaskCompleted(workbasket.getId())).isPositive();

    // Workbasket with completed task will be marked for deletion.
    workbasketService.deleteWorkbasket(workbasket.getId());

    WorkbasketCleanupJob job = new WorkbasketCleanupJob(kadaiEngine, null, null);
    job.run();

    totalWorkbasketCount = workbasketService.createWorkbasketQuery().count();
    assertThat(totalWorkbasketCount).isOne();
  }

  private long getNumberTaskNotCompleted(String workbasketId) {
    return taskService
        .createTaskQuery()
        .workbasketIdIn(workbasketId)
        .stateNotIn(TaskState.COMPLETED)
        .count();
  }

  private long getNumberTaskCompleted(String workbasketId) {
    return taskService
        .createTaskQuery()
        .workbasketIdIn(workbasketId)
        .stateIn(TaskState.COMPLETED)
        .count();
  }
}
