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

package io.kadai.task.internal.jobs;

import static java.util.Objects.nonNull;
import static java.util.function.Predicate.not;

import io.kadai.KadaiConfiguration;
import io.kadai.common.api.BulkOperationResults;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.ScheduledJob;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.KadaiException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.internal.jobs.AbstractKadaiJob;
import io.kadai.common.internal.transaction.KadaiTransactionProvider;
import io.kadai.common.internal.util.CollectionUtil;
import io.kadai.common.internal.util.LogSanitizer;
import io.kadai.task.internal.TaskMapper;
import io.kadai.task.internal.models.TaskCleanupSummary;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Job to cleanup completed tasks after a period of time. */
public class TaskCleanupJob extends AbstractKadaiJob {

  private static final Logger LOGGER = LoggerFactory.getLogger(TaskCleanupJob.class);

  private final Duration minimumAge;
  private final int batchSize;
  private final boolean allCompletedSameParentBusiness;

  public TaskCleanupJob(
      KadaiEngine kadaiEngine, KadaiTransactionProvider txProvider, ScheduledJob scheduledJob) {
    super(kadaiEngine, txProvider, scheduledJob, true);
    minimumAge = kadaiEngine.getConfiguration().getTaskCleanupJobMinimumAge();
    batchSize = kadaiEngine.getConfiguration().getJobBatchSize();
    allCompletedSameParentBusiness =
        kadaiEngine.getConfiguration().isTaskCleanupJobAllCompletedSameParentBusiness();
  }

  public static Duration getLockExpirationPeriod(KadaiConfiguration kadaiConfiguration) {
    return kadaiConfiguration.getTaskCleanupJobLockExpirationPeriod();
  }

  @Override
  public void execute() {
    Instant completedBefore = Instant.now().minus(minimumAge);
    LOGGER.info("Running job to delete all tasks completed before ({})", completedBefore);
    try {
      List<TaskCleanupSummary> tasksCompletedBefore = getTasksCompletedBefore(completedBefore);

      int totalNumberOfTasksDeleted =
          CollectionUtil.partitionBasedOnSize(tasksCompletedBefore, batchSize).stream()
              .mapToInt(this::deleteTasksTransactionally)
              .sum();

      LOGGER.info("Job ended successfully. {} tasks deleted.", totalNumberOfTasksDeleted);
    } catch (Exception e) {
      throw new SystemException("Error while processing TaskCleanupJob.", e);
    }
  }

  @Override
  protected String getType() {
    return TaskCleanupJob.class.getName();
  }

  private List<TaskCleanupSummary> getTasksCompletedBefore(Instant untilDate) {
    TaskMapper taskMapper = kadaiEngineImpl.getTaskMapper();

    List<TaskCleanupSummary> tasksToDelete =
        taskMapper.findCompletedTasksCompletedBefore(untilDate);

    if (allCompletedSameParentBusiness) {
      Map<String, Long> numberParentTasksShouldHave = new HashMap<>();
      Map<String, Long> countParentTask = new HashMap<>();

      tasksToDelete.forEach(
          task -> {
            if (!numberParentTasksShouldHave.containsKey(task.getParentBusinessProcessId())) {
              long count =
                  taskMapper.countTasksByParentBusinessProcessId(task.getParentBusinessProcessId());
              numberParentTasksShouldHave.put(task.getParentBusinessProcessId(), count);
            }
            countParentTask.merge(task.getParentBusinessProcessId(), 1L, Long::sum);
          });

      List<String> taskIdsNotAllCompletedSameParentBusiness =
          numberParentTasksShouldHave.entrySet().stream()
              .filter(entry -> nonNull(entry.getKey()))
              .filter(not(entry -> entry.getKey().isEmpty()))
              .filter(not(entry -> entry.getValue().equals(countParentTask.get(entry.getKey()))))
              .map(Map.Entry::getKey)
              .toList();

      tasksToDelete =
          tasksToDelete.stream()
              .filter(
                  taskSummary ->
                      !taskIdsNotAllCompletedSameParentBusiness.contains(
                          taskSummary.getParentBusinessProcessId()))
              .toList();
    }

    return tasksToDelete;
  }

  private int deleteTasksTransactionally(List<TaskCleanupSummary> tasksToBeDeleted) {
    return KadaiTransactionProvider.executeInTransactionIfPossible(
        txProvider,
        () -> {
          try {
            return deleteTasks(tasksToBeDeleted);
          } catch (Exception ex) {
            LOGGER.warn("Could not delete tasks.", ex);
            return 0;
          }
        });
  }

  private int deleteTasks(List<TaskCleanupSummary> tasksToBeDeleted)
      throws InvalidArgumentException, NotAuthorizedException {

    List<String> tasksIdsToBeDeleted =
        tasksToBeDeleted.stream().map(TaskCleanupSummary::getTaskId).toList();
    BulkOperationResults<String, KadaiException> results =
        kadaiEngineImpl.getTaskService().deleteTasks(tasksIdsToBeDeleted);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("{} tasks deleted.", tasksIdsToBeDeleted.size() - results.getFailedIds().size());
    }
    for (String failedId : results.getFailedIds()) {
      if (LOGGER.isWarnEnabled()) {
        LOGGER.warn(
            "Task with id {} could not be deleted. Reason: {}",
            LogSanitizer.stripLineBreakingChars(failedId),
            LogSanitizer.stripLineBreakingChars(results.getErrorForId(failedId)));
      }
    }
    return tasksIdsToBeDeleted.size() - results.getFailedIds().size();
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
