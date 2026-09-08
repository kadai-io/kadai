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
 */

package io.kadai.common.internal.jobs;

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.ScheduledJob;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.internal.JobServiceImpl;
import java.time.Duration;
import java.util.Objects;

/** Renews and fences a scheduled job lock. */
@FunctionalInterface
public interface JobLockGuard {

  /** Renews the lock or aborts when this runner no longer owns it. */
  void renewOrThrow();

  /**
   * Creates a lock guard for a scheduled job. Direct job execution receives a no-op guard.
   *
   * @param kadaiEngine engine owning the scheduled job service
   * @param scheduledJob scheduled job, or {@code null} for direct execution
   * @param lockExpirationPeriod duration for the renewed lock
   * @param jobName name used in the lock-loss error message
   * @return a guard that renews the lock before or after a critical job phase
   */
  static JobLockGuard forScheduledJob(
      KadaiEngine kadaiEngine,
      ScheduledJob scheduledJob,
      Duration lockExpirationPeriod,
      String jobName) {
    Objects.requireNonNull(kadaiEngine, "kadaiEngine");
    Objects.requireNonNull(lockExpirationPeriod, "lockExpirationPeriod");
    Objects.requireNonNull(jobName, "jobName");
    if (scheduledJob == null) {
      return () -> {};
    }
    return () -> {
      boolean renewed =
          ((JobServiceImpl) kadaiEngine.getJobService())
              .renewLock(scheduledJob, lockExpirationPeriod);
      if (!renewed) {
        throw new SystemException(
            jobName + " lock was lost. Stopping execution to avoid concurrent processing.");
      }
    };
  }
}
