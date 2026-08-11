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

package io.kadai.common.internal.jobs;

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.ScheduledJob;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.internal.JobServiceImpl;
import io.kadai.common.internal.transaction.KadaiTransactionProvider;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This is the runner for Tasks jobs. */
public class JobRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(JobRunner.class);
  private final KadaiEngine kadaiEngine;
  private final JobServiceImpl jobService;
  private KadaiTransactionProvider txProvider;

  public JobRunner(KadaiEngine kadaiEngine) {
    this.kadaiEngine = kadaiEngine;
    jobService = (JobServiceImpl) kadaiEngine.getJobService();
  }

  public void registerTransactionProvider(KadaiTransactionProvider txProvider) {
    this.txProvider = txProvider;
  }

  public void runJobs() {
    KadaiTransactionProvider.executeInTransactionIfPossible(
            txProvider, () -> jobService.findJobsToRun().stream().map(this::lockJob))
        .forEach(this::runJob);
  }

  private void runJob(ScheduledJob scheduledJob) {
    kadaiEngine.runAsAdmin(
        () -> {
          try {
            AbstractKadaiJob job =
                (AbstractKadaiJob)
                    AbstractKadaiJob.createFromScheduledJob(kadaiEngine, txProvider, scheduledJob);
            if (job.getTransactionPolicy() == JobTransactionPolicy.WHOLE_JOB) {
              KadaiTransactionProvider.executeInTransactionIfPossible(
                  txProvider, () -> executeAndFinalize(job, scheduledJob));
            } else {
              executeJob(job);
              KadaiTransactionProvider.executeInTransactionIfPossible(
                  txProvider, () -> finalizeScheduledJob(job, scheduledJob));
            }
          } catch (Exception e) {
            LOGGER.error("Error running job: {} ", scheduledJob.getType(), e);
          }
        });
  }

  private void executeAndFinalize(AbstractKadaiJob job, ScheduledJob scheduledJob) {
    executeJob(job);
    finalizeScheduledJob(job, scheduledJob);
  }

  private void executeJob(AbstractKadaiJob job) {
    try {
      job.execute();
    } catch (Exception e) {
      throw new SystemException("Error running job: " + job.getClass().getName(), e);
    }
  }

  private void finalizeScheduledJob(AbstractKadaiJob job, ScheduledJob scheduledJob) {
    if (job.isAsync()) {
      job.scheduleNextJob();
    }
    jobService.deleteJob(scheduledJob);
  }

  private ScheduledJob lockJob(ScheduledJob job) {
    String hostAddress = getHostAddress();
    String owner = hostAddress + " - " + UUID.randomUUID();
    job.setLockedBy(owner);
    ScheduledJob lockedJob = jobService.lockJob(job, owner);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Locked job: {}", lockedJob);
    }
    return lockedJob;
  }

  private String getHostAddress() {
    String hostAddress;
    try {
      hostAddress = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      hostAddress = "UNKNOWN_ADDRESS";
    }
    return hostAddress;
  }
}
