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

import acceptance.AbstractAccTest;
import io.kadai.common.api.BaseQuery;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.common.test.security.WithAccessId;
import io.kadai.task.api.TaskState;
import io.kadai.task.internal.jobs.TaskCleanupJob;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import io.kadai.workbasket.internal.jobs.WorkbasketCleanupJob;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Acceptance test for all "jobs workbasket runner" scenarios. */
@ExtendWith(JaasExtension.class)
class WorkbasketCleanupJobAccTest extends AbstractAccTest {

  WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();

  @AfterEach
  void after() throws Exception {
    resetDb(true);
  }

  @WithAccessId(user = "admin")
  @Test
  void shouldCleanWorkbasketMarkedForDeletionWithoutTasks() throws Exception {
    long totalWorkbasketCount = workbasketService.createWorkbasketQuery().count();
    assertThat(totalWorkbasketCount).isEqualTo(26);
    List<WorkbasketSummary> workbaskets =
        workbasketService
            .createWorkbasketQuery()
            .keyIn("TEAMLEAD-1")
            .orderByKey(BaseQuery.SortDirection.ASCENDING)
            .list();

    assertThat(getNumberTaskNotCompleted(workbaskets.get(0).getId())).isZero();
    assertThat(getNumberTaskCompleted(workbaskets.get(0).getId())).isOne();

    // Workbasket with completed task will be marked for deletion.
    workbasketService.deleteWorkbasket(workbaskets.get(0).getId());

    // Run taskCleanupJob for deleting completing tasks before running workbasketCleanupJob
    TaskCleanupJob taskCleanupJob = new TaskCleanupJob(kadaiEngine, null, null);
    taskCleanupJob.run();

    assertThat(getNumberTaskCompleted(workbaskets.get(0).getId())).isZero();

    WorkbasketCleanupJob workbasketCleanupJob = new WorkbasketCleanupJob(kadaiEngine, null, null);
    workbasketCleanupJob.run();

    totalWorkbasketCount = workbasketService.createWorkbasketQuery().count();
    assertThat(totalWorkbasketCount).isEqualTo(25);
  }

  @WithAccessId(user = "admin")
  @Test
  void shouldNotCleanWorkbasketMarkedForDeletionIfWorkbasketHasTasks() throws Exception {
    long totalWorkbasketCount = workbasketService.createWorkbasketQuery().count();
    assertThat(totalWorkbasketCount).isEqualTo(26);
    List<WorkbasketSummary> workbaskets =
        workbasketService
            .createWorkbasketQuery()
            .keyIn("TEAMLEAD-1")
            .orderByKey(BaseQuery.SortDirection.ASCENDING)
            .list();

    assertThat(getNumberTaskCompleted(workbaskets.get(0).getId())).isPositive();

    // Workbasket with completed task will be marked for deletion.
    workbasketService.deleteWorkbasket(workbaskets.get(0).getId());

    WorkbasketCleanupJob job = new WorkbasketCleanupJob(kadaiEngine, null, null);
    job.run();

    totalWorkbasketCount = workbasketService.createWorkbasketQuery().count();
    assertThat(totalWorkbasketCount).isEqualTo(26);
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
