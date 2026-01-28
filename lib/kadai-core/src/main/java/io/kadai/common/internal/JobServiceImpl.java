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

package io.kadai.common.internal;

import io.kadai.KadaiConfiguration;
import io.kadai.common.api.JobService;
import io.kadai.common.api.ScheduledJob;
import io.kadai.common.api.exceptions.SystemException;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Controls all job activities. */
public class JobServiceImpl implements JobService {

  public static final int JOB_DEFAULT_PRIORITY = 50;

  private static final Logger LOGGER = LoggerFactory.getLogger(JobServiceImpl.class);
  private final JobMapper jobMapper;
  private final InternalKadaiEngine kadaiEngineImpl;

  public JobServiceImpl(InternalKadaiEngine kadaiEngine, JobMapper jobMapper) {
    this.kadaiEngineImpl = kadaiEngine;
    this.jobMapper = jobMapper;
  }

  @Override
  public ScheduledJob createJob(ScheduledJob job) {
    initializeDefaultJobProperties(job);
    Integer id = kadaiEngineImpl.executeInDatabaseConnection(() -> jobMapper.insertJob(job));
    job.setJobId(id);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Created job {}", job);
    }
    return job;
  }

  public void deleteJobs(String jobType) {
    kadaiEngineImpl.executeInDatabaseConnection(() -> jobMapper.deleteMultiple(jobType));
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Deleted jobs of type: {}", jobType);
    }
  }

  public ScheduledJob lockJob(ScheduledJob job, String owner) {
    job.setLockedBy(owner);
    Class<?> jobClass = null;
    try {
      jobClass = Thread.currentThread().getContextClassLoader().loadClass(job.getType());
      job.setLockExpires(
          Instant.now()
              .plus(
                  (Duration)
                      jobClass
                          .getMethod("getLockExpirationPeriod", KadaiConfiguration.class)
                          .invoke(null, kadaiEngineImpl.getEngine().getConfiguration())));
    } catch (ClassNotFoundException | NoSuchMethodException e) {
      throw new SystemException(
          String.format(
              "Job '%s' does not have a method matching ('getLockExpirationPeriod', %s",
              jobClass, KadaiConfiguration.class));
    } catch (InvocationTargetException | IllegalAccessException e) {
      throw new SystemException(
              "Caught Exception while invoking method 'getLockExpirationPeriod' by reflection");
    }

    job.setRetryCount(job.getRetryCount() - 1);
    kadaiEngineImpl.executeInDatabaseConnection(() -> jobMapper.update(job));
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Job {} locked. Remaining retries: {}", job.getJobId(), job.getRetryCount());
    }
    return job;
  }

  public List<ScheduledJob> findJobsToRun() {
    List<ScheduledJob> availableJobs =
        kadaiEngineImpl.executeInDatabaseConnection(() -> jobMapper.findJobsToRun(Instant.now()));
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Found available jobs: {}", availableJobs);
    }
    return availableJobs;
  }

  public void deleteJob(ScheduledJob job) {
    kadaiEngineImpl.executeInDatabaseConnection(() -> jobMapper.delete(job));
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Deleted job: {}", job);
    }
  }

  private void initializeDefaultJobProperties(ScheduledJob job) {
    Instant now = Instant.now();
    job.setCreated(now);
    job.setState(ScheduledJob.State.READY);
    job.setPriority(JOB_DEFAULT_PRIORITY);
    if (job.getDue() == null) {
      job.setDue(now);
    }
    job.setRetryCount(kadaiEngineImpl.getEngine().getConfiguration().getMaxNumberOfJobRetries());
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Job after initialization: {}", job);
    }
  }
}
