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

package io.kadai.workbasket.internal.jobs;

import io.kadai.KadaiConfiguration;
import io.kadai.common.api.BaseQuery;
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
import io.kadai.workbasket.api.WorkbasketQueryColumnName;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Job to cleanup completed workbaskets after a period of time if there are no pending tasks
 * associated to the workbasket.
 */
public class WorkbasketCleanupJob extends AbstractKadaiJob {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkbasketCleanupJob.class);

  private final int batchSize;

  public WorkbasketCleanupJob(
      KadaiEngine kadaiEngine, KadaiTransactionProvider txProvider, ScheduledJob job) {
    super(kadaiEngine, txProvider, job, true);
    batchSize = kadaiEngine.getConfiguration().getJobBatchSize();
  }

  public static Duration getLockExpirationPeriod(KadaiConfiguration kadaiConfiguration) {
    return kadaiConfiguration.getWorkbasketCleanupJobLockExpirationPeriod();
  }

  @Override
  public void execute() throws KadaiException {
    LOGGER.info("Running job to delete all workbaskets marked for deletion");
    try {
      List<String> workbasketsMarkedForDeletion = getWorkbasketsMarkedForDeletion();
      int totalNumberOfWorkbasketDeleted =
          CollectionUtil.partitionBasedOnSize(workbasketsMarkedForDeletion, batchSize).stream()
              .mapToInt(this::deleteWorkbasketsTransactionally)
              .sum();
      LOGGER.info(
          "Job ended successfully. {} workbaskets deleted.", totalNumberOfWorkbasketDeleted);
    } catch (Exception e) {
      throw new SystemException("Error while processing WorkbasketCleanupJob.", e);
    }
  }

  @Override
  protected String getType() {
    return WorkbasketCleanupJob.class.getName();
  }

  private List<String> getWorkbasketsMarkedForDeletion() {

    return kadaiEngineImpl
        .getWorkbasketService()
        .createWorkbasketQuery()
        .markedForDeletion(true)
        .listValues(WorkbasketQueryColumnName.ID, BaseQuery.SortDirection.ASCENDING);
  }

  private int deleteWorkbasketsTransactionally(List<String> workbasketsToBeDeleted) {
    return KadaiTransactionProvider.executeInTransactionIfPossible(
        txProvider,
        () -> {
          try {
            return deleteWorkbaskets(workbasketsToBeDeleted);
          } catch (Exception e) {
            LOGGER.warn("Could not delete workbaskets.", e);
            return 0;
          }
        });
  }

  private int deleteWorkbaskets(List<String> workbasketsToBeDeleted)
      throws InvalidArgumentException, NotAuthorizedException {

    BulkOperationResults<String, KadaiException> results =
        kadaiEngineImpl.getWorkbasketService().deleteWorkbaskets(workbasketsToBeDeleted);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "{} workbasket deleted.", workbasketsToBeDeleted.size() - results.getFailedIds().size());
    }
    for (String failedId : results.getFailedIds()) {
      LOGGER.warn(
          "Workbasket with id {} could not be deleted. Reason:",
          failedId,
          results.getErrorForId(failedId));
    }
    return workbasketsToBeDeleted.size() - results.getFailedIds().size();
  }
}
