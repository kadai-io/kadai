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
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.KadaiEngine.ConnectionManagementMode;
import io.kadai.common.api.ScheduledJob;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.internal.JobServiceImpl;
import io.kadai.common.internal.jobs.JobRunner;
import io.kadai.common.internal.jobs.PlainJavaTransactionProvider;
import io.kadai.common.internal.util.Pair;
import io.kadai.common.test.config.DataSourceGenerator;
import io.kadai.common.test.util.ParallelThreadHelper;
import io.kadai.task.internal.jobs.TaskCleanupJob;
import io.kadai.task.internal.jobs.TaskUpdatePriorityJob;
import io.kadai.workbasket.internal.jobs.WorkbasketCleanupJob;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.ThrowingConsumer;

@Disabled
class JobRunnerAccTest extends AbstractAccTest {

  private static final Duration TASK_CLEANUP_JOB_LOCK_EXPIRATION_PERIOD = Duration.ofMinutes(4);
  private static final Duration WORKBASKET_CLEANUP_JOB_LOCK_EXPIRATION_PERIOD =
      Duration.ofMinutes(3);
  private static final Duration TASK_UPDATE_PRIORITY_LOCK_EXPIRATION_PERIOD = Duration.ofMinutes(1);
  private final JobServiceImpl jobService = (JobServiceImpl) kadaiEngine.getJobService();

  @Test
  void should_onlyExecuteJobOnce_When_MultipleThreadsTryToRunJobsAtTheSameTime() throws Exception {
    resetDb(true); // for some reason clearing the job table is not enough..

    assertThat(jobService.findJobsToRun()).isEmpty();
    ScheduledJob job =
        createJob(Instant.now().minus(5, ChronoUnit.MINUTES), TaskCleanupJob.class.getName());
    assertThat(jobService.findJobsToRun()).containsExactly(job);

    ParallelThreadHelper.runInThread(
        () -> {
          try {
            KadaiEngine kadaiEngine =
                KadaiEngine.buildKadaiEngine(
                    kadaiConfiguration, ConnectionManagementMode.AUTOCOMMIT);
            DataSource dataSource = DataSourceGenerator.getDataSource();
            PlainJavaTransactionProvider transactionProvider =
                new PlainJavaTransactionProvider(kadaiEngine, dataSource);
            JobRunner runner = new JobRunner(kadaiEngine);
            runner.registerTransactionProvider(transactionProvider);
            runner.runJobs();
          } catch (Exception e) {
            throw new SystemException("Caught Exception", e);
          }
        },
        2);

    // runEvery is set to P1D Therefore we need to check which jobs run tomorrow.
    // Just to be sure the jobs are found we will look for any job scheduled in the next 2 days.
    List<ScheduledJob> jobsToRun =
        getJobMapper(kadaiEngine).findJobsToRun(Instant.now().plus(2, ChronoUnit.DAYS));

    assertThat(jobsToRun).hasSize(1).doesNotContain(job);
  }

  @TestFactory
  Stream<DynamicTest> should_setTheLockExpirationDateCorrectly_When_CreatingJobs() {
    List<Pair<String, Duration>> list =
        List.of(
            Pair.of(TaskCleanupJob.class.getName(), TASK_CLEANUP_JOB_LOCK_EXPIRATION_PERIOD),
            Pair.of(
                TaskUpdatePriorityJob.class.getName(), TASK_UPDATE_PRIORITY_LOCK_EXPIRATION_PERIOD),
            Pair.of(
                WorkbasketCleanupJob.class.getName(),
                WORKBASKET_CLEANUP_JOB_LOCK_EXPIRATION_PERIOD));
    ThrowingConsumer<Pair<String, Duration>> testSettingLockExpirationDate =
        p -> {
          resetDb(true);
          assertThat(jobService.findJobsToRun()).isEmpty();
          createJob(Instant.now().minus(5, ChronoUnit.MINUTES), p.getLeft());
          ParallelThreadHelper.runInThread(
              () -> {
                KadaiEngine kadaiEngine;
                try {
                  kadaiEngine =
                      KadaiEngine.buildKadaiEngine(
                          kadaiConfiguration, ConnectionManagementMode.AUTOCOMMIT);
                } catch (SQLException e) {
                  throw new RuntimeException("Could not build the KadaiEngine");
                }

                DataSource dataSource = DataSourceGenerator.getDataSource();
                PlainJavaTransactionProvider transactionProvider =
                    new PlainJavaTransactionProvider(kadaiEngine, dataSource);
                JobRunner runner = new JobRunner(kadaiEngine);
                runner.registerTransactionProvider(transactionProvider);
                runner.runJobs();
              },
              1);
          List<ScheduledJob> resultJobs =
              getJobMapper(kadaiEngine).findJobsToRun(Instant.now().plus(2, ChronoUnit.DAYS));
          assertThat(resultJobs).hasSize(1);
          assertThat(resultJobs.get(0).getType()).isEqualTo(p.getLeft());
          assertThat(resultJobs.get(0).getLockExpires())
              .isBetween(
                  resultJobs.get(0).getCreated().plus(p.getRight()),
                  resultJobs.get(0).getCreated().plus(p.getRight()).plusSeconds(1));
        };
    return DynamicTest.stream(list.iterator(), Pair::getLeft, testSettingLockExpirationDate);
  }

  private ScheduledJob createJob(Instant firstDue, String type) {
    ScheduledJob job = new ScheduledJob();
    job.setType(type);
    job.setDue(firstDue);
    jobService.createJob(job);
    return job;
  }
}
