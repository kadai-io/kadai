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

import acceptance.AbstractAccTest;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.KadaiEngine.ConnectionManagementMode;
import io.kadai.common.api.ScheduledJob;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.internal.JobServiceImpl;
import io.kadai.common.internal.jobs.JobRunner;
import io.kadai.common.internal.jobs.PlainJavaTransactionProvider;
import io.kadai.common.test.config.DataSourceGenerator;
import io.kadai.common.test.util.ParallelThreadHelper;
import io.kadai.task.internal.jobs.TaskCleanupJob;
import io.kadai.task.internal.jobs.TaskUpdatePriorityJob;
import io.kadai.workbasket.internal.jobs.WorkbasketCleanupJob;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JobRunnerAccTest extends AbstractAccTest {

  private static final Duration TASK_CLEANUP_JOB_LOCK_EXPIRATION_PERIOD = Duration.ofMinutes(4);
  private static final Duration WORKBASKET_CLEANUP_JOB_LOCK_EXPIRATION_PERIOD =
      Duration.ofMinutes(3);
  private static final Duration TASK_UPDATE_PRIORITY_LOCK_EXPIRATION_PERIOD = Duration.ofMinutes(1);

  private JobServiceImpl jobService;

  @BeforeEach
  void setUp() throws Exception {
    resetDb(true);
    this.jobService = (JobServiceImpl) kadaiEngine.getJobService();
    assertThat(jobService.findJobsToRun()).isEmpty();
  }

  @Test
  void should_onlyExecuteJobOnce_When_MultipleThreadsTryToRunJobsAtTheSameTime() throws Exception {
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

  @ParameterizedTest
  @MethodSource("provideJobCreationClassNameWithExpirationPeriod")
  @Disabled(
      "There's probably some faulty session behaviour as fields of the retrieved jobs aren't set.")
  void should_setTheLockExpirationDateCorrectly_When_CreatingJobs(
      String jobTypeName, Duration expirationPeriod) throws Exception {
    createJob(Instant.now().minus(5, ChronoUnit.MINUTES), jobTypeName);
    DataSource dataSource = DataSourceGenerator.getDataSource();
    PlainJavaTransactionProvider transactionProvider =
        new PlainJavaTransactionProvider(kadaiEngine, dataSource);
    JobRunner runner = new JobRunner(kadaiEngine);
    runner.registerTransactionProvider(transactionProvider);

    runner.runJobs();
    List<ScheduledJob> resultJobs =
        getJobMapper(kadaiEngine).findJobsToRun(Instant.now().plus(2, ChronoUnit.DAYS));

    assertThat(resultJobs).hasSize(1);
    assertThat(resultJobs.get(0).getType()).isEqualTo(jobTypeName);
    assertThat(resultJobs.get(0).getLockExpires())
        .isBetween(
            resultJobs.get(0).getCreated().plus(expirationPeriod),
            resultJobs.get(0).getCreated().plus(expirationPeriod).plusSeconds(1));
  }

  private static Stream<Arguments> provideJobCreationClassNameWithExpirationPeriod() {
    return Stream.of(
        Arguments.of(TaskCleanupJob.class.getName(), TASK_CLEANUP_JOB_LOCK_EXPIRATION_PERIOD),
        Arguments.of(
            TaskUpdatePriorityJob.class.getName(), TASK_UPDATE_PRIORITY_LOCK_EXPIRATION_PERIOD),
        Arguments.of(
            WorkbasketCleanupJob.class.getName(), WORKBASKET_CLEANUP_JOB_LOCK_EXPIRATION_PERIOD));
  }

  private ScheduledJob createJob(Instant firstDue, String type) {
    ScheduledJob job = new ScheduledJob();
    job.setType(type);
    job.setDue(firstDue);
    jobService.createJob(job);
    return job;
  }
}
