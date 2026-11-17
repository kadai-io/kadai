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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.ScheduledJob;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.internal.JobServiceImpl;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobLockGuardTest {

  private static final Duration LOCK_EXPIRATION = Duration.ofMinutes(5);

  @Mock KadaiEngine kadaiEngine;
  @Mock JobServiceImpl jobService;
  @Mock ScheduledJob scheduledJob;

  @Test
  void should_NotUseJobServiceForDirectExecution() {
    JobLockGuard guard =
        JobLockGuard.forScheduledJob(kadaiEngine, null, LOCK_EXPIRATION, "Test job");

    guard.renewOrThrow();

    verify(kadaiEngine, never()).getJobService();
  }

  @Test
  void should_RenewScheduledJobLock() {
    when(kadaiEngine.getJobService()).thenReturn(jobService);
    when(jobService.renewLock(scheduledJob, LOCK_EXPIRATION)).thenReturn(true);
    JobLockGuard guard =
        JobLockGuard.forScheduledJob(kadaiEngine, scheduledJob, LOCK_EXPIRATION, "Test job");

    guard.renewOrThrow();

    verify(jobService).renewLock(scheduledJob, LOCK_EXPIRATION);
  }

  @Test
  void should_AbortWhenScheduledJobLockIsLost() {
    when(kadaiEngine.getJobService()).thenReturn(jobService);
    when(jobService.renewLock(scheduledJob, LOCK_EXPIRATION)).thenReturn(false);
    JobLockGuard guard =
        JobLockGuard.forScheduledJob(kadaiEngine, scheduledJob, LOCK_EXPIRATION, "Test job");

    assertThatThrownBy(guard::renewOrThrow)
        .isInstanceOf(SystemException.class)
        .hasMessageContaining("Test job lock was lost");
  }
}
