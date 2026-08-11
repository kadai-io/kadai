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

package io.kadai.task.internal.jobs;

import io.kadai.KadaiConfiguration;
import io.kadai.common.api.BulkOperationResults;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.ScheduledJob;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.KadaiException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.internal.JobServiceImpl;
import io.kadai.common.internal.jobs.AbstractKadaiJob;
import io.kadai.common.internal.jobs.JobTransactionPolicy;
import io.kadai.common.internal.transaction.KadaiTransactionProvider;
import io.kadai.common.internal.util.CollectionUtil;
import io.kadai.common.internal.util.LogSanitizer;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Job to clean up completed tasks after a period of time. */
public class TaskCleanupJob extends AbstractKadaiJob {

  private static final Logger LOGGER = LoggerFactory.getLogger(TaskCleanupJob.class);

  private final Duration minimumAge;
  private final int batchSize;
  private final boolean allCompletedSameParentBusiness;

  public TaskCleanupJob(
      KadaiEngine kadaiEngine, KadaiTransactionProvider txProvider, ScheduledJob scheduledJob) {
    super(kadaiEngine, txProvider, scheduledJob, true);
    minimumAge = kadaiEngine.getConfiguration().getTaskCleanupJobMinimumAge();
    batchSize = kadaiEngine.getConfiguration().getTaskCleanupJobBatchSize();
    allCompletedSameParentBusiness =
        kadaiEngine.getConfiguration().isTaskCleanupJobAllCompletedSameParentBusiness();
  }

  public static Duration getLockExpirationPeriod(KadaiConfiguration kadaiConfiguration) {
    return kadaiConfiguration.getTaskCleanupJobLockExpirationPeriod();
  }

  @Override
  public JobTransactionPolicy getTransactionPolicy() {
    return JobTransactionPolicy.JOB_MANAGED;
  }

  @Override
  public void execute() {
    Instant completedBefore = Instant.now().minus(minimumAge);
    long jobStartedAt = System.nanoTime();
    LOGGER.info("Running job to delete all tasks completed before ({})", completedBefore);
    try {
      long selectionStartedAt = System.nanoTime();
      List<String> tasksCompletedBefore = getTasksCompletedBeforeTransactionally(completedBefore);
      LOGGER.info(
          "Selected {} tasks for cleanup in {} ms.",
          tasksCompletedBefore.size(),
          Duration.ofNanos(System.nanoTime() - selectionStartedAt).toMillis());

      int totalNumberOfTasksDeleted =
          CollectionUtil.partitionBasedOnSize(tasksCompletedBefore, batchSize).stream()
              .mapToInt(this::deleteTasksTransactionally)
              .sum();

      LOGGER.info(
          "Job ended successfully. {} tasks deleted in {} ms.",
          totalNumberOfTasksDeleted,
          Duration.ofNanos(System.nanoTime() - jobStartedAt).toMillis());
    } catch (Exception e) {
      throw new SystemException("Error while processing TaskCleanupJob.", e);
    }
  }

  @Override
  protected String getType() {
    return TaskCleanupJob.class.getName();
  }

  private List<String> getTasksCompletedBefore(Instant untilDate) {
    return kadaiEngineImpl.executeInDatabaseConnection(
        () ->
            allCompletedSameParentBusiness
                ? kadaiEngineImpl
                    .getTaskMapper()
                    .findTasksCompletedBeforeWithParentBusinessProcessConstraint(untilDate)
                : kadaiEngineImpl.getTaskMapper().findTasksCompletedBefore(untilDate));
  }

  private List<String> getTasksCompletedBeforeTransactionally(Instant untilDate) {
    return KadaiTransactionProvider.executeInTransactionIfPossible(
        txProvider,
        () -> {
          renewLock();
          return getTasksCompletedBefore(untilDate);
        });
  }

  private int deleteTasksTransactionally(List<String> tasksToBeDeleted) {
    return KadaiTransactionProvider.executeInTransactionIfPossible(
        txProvider,
        () -> {
          int deletedTasks;
          try {
            deletedTasks = deleteTasks(tasksToBeDeleted);
          } catch (Exception ex) {
            LOGGER.warn("Could not delete tasks.", ex);
            return 0;
          }
          renewLock();
          return deletedTasks;
        });
  }

  private void renewLock() {
    if (scheduledJob == null) {
      return;
    }
    boolean lockRenewed =
        ((JobServiceImpl) kadaiEngineImpl.getJobService())
            .renewLock(
                scheduledJob,
                kadaiEngineImpl.getConfiguration().getTaskCleanupJobLockExpirationPeriod());
    if (!lockRenewed) {
      throw new SystemException(
          "Task cleanup job lock was lost. Stopping cleanup to avoid concurrent processing.");
    }
  }

  private int deleteTasks(List<String> tasksToBeDeleted)
      throws InvalidArgumentException, NotAuthorizedException {

    BulkOperationResults<String, KadaiException> results =
        kadaiEngineImpl.getTaskService().deleteTasks(tasksToBeDeleted);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("{} tasks deleted.", tasksToBeDeleted.size() - results.getFailedIds().size());
    }
    for (String failedId : results.getFailedIds()) {
      if (LOGGER.isWarnEnabled()) {
        LOGGER.warn(
            "Task with id {} could not be deleted. Reason: {}",
            LogSanitizer.stripLineBreakingChars(failedId),
            LogSanitizer.stripLineBreakingChars(results.getErrorForId(failedId)));
      }
    }
    return tasksToBeDeleted.size() - results.getFailedIds().size();
  }

  @Override
  public String toString() {
    return "TaskCleanupJob [firstRun="
        + firstRun
        + ", runEvery="
        + runEvery
        + ", kadaiEngineImpl="
        + kadaiEngineImpl
        + ", txProvider="
        + txProvider
        + ", scheduledJob="
        + scheduledJob
        + ", minimumAge="
        + minimumAge
        + ", batchSize="
        + batchSize
        + ", allCompletedSameParentBusiness="
        + allCompletedSameParentBusiness
        + "]";
  }
}
