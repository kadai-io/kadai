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

package io.kadai.testapi.benchmark;

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.WorkingTimeCalculator;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.spi.priority.api.PriorityServiceProvider;
import io.kadai.task.api.models.TaskSummary;
import java.time.Duration;
import java.time.Instant;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Derives the priority the way insurers typically do: the classification's base priority, raised
 * for every day a task waits, shortly before the due date and for every working day it is overdue.
 *
 * <p>This is the worst case for the job: every task has waited for at least a started day, so every
 * open task ends up above the base priority it was created with, and the job has to update all of
 * them. A daily job run in production faces nearly the same, as each day adds to every task's
 * waiting time.
 *
 * <p>It also meters the job: it counts the tasks it is asked for and, once the time budget is
 * spent, stops the job by failing the calculation.
 */
final class SlaPriorityServiceProvider implements PriorityServiceProvider {

  private static final Duration DAY = Duration.ofDays(1);
  private static final int MAX_WAITING_DAYS = 30;
  private static final int DUE_SOON_WORKING_DAYS = 2;
  private static final int DUE_SOON_BONUS = 10;
  private static final int OVERDUE_BONUS = 20;
  private static final int OVERDUE_BONUS_PER_WORKING_DAY = 2;
  private static final int MAX_OVERDUE_WORKING_DAYS = 30;

  private final AtomicLong calculations = new AtomicLong();
  private final AtomicLong changes = new AtomicLong();
  private final AtomicLong firstCalculationNanos = new AtomicLong();
  private volatile long deadlineNanos;
  private volatile boolean meteringStarted;
  private WorkingTimeCalculator workingTimeCalculator;

  @Override
  public void initialize(KadaiEngine kadaiEngine) {
    workingTimeCalculator = kadaiEngine.getWorkingTimeCalculator();
  }

  @Override
  public OptionalInt calculatePriority(TaskSummary taskSummary) {
    if (meteringStarted) {
      long now = System.nanoTime();
      if (now - deadlineNanos > 0) {
        throw new TimeBudgetExhaustedException();
      }
      firstCalculationNanos.compareAndSet(0, now);
      calculations.incrementAndGet();
    }
    Instant now = Instant.now();
    int priority =
        taskSummary.getClassificationSummary().getPriority()
            + waitingBonus(taskSummary, now)
            + urgency(taskSummary, now);
    if (priority != taskSummary.getPriority()) {
      changes.incrementAndGet();
    }
    return OptionalInt.of(priority);
  }

  // resets the counters and starts the time budget of the job
  void startMetering(Duration timeBudget) {
    calculations.set(0);
    changes.set(0);
    firstCalculationNanos.set(0);
    deadlineNanos = System.nanoTime() + timeBudget.toNanos();
    meteringStarted = true;
  }

  long calculations() {
    return calculations.get();
  }

  long changes() {
    return changes.get();
  }

  // System.nanoTime() of the first calculation, or 0 if there was none
  long firstCalculationNanos() {
    return firstCalculationNanos.get();
  }

  // one point per started day since the task was created, at least 1
  private static int waitingBonus(TaskSummary taskSummary, Instant now) {
    Instant created = taskSummary.getCreated();
    long startedDays =
        created == null
            ? 1
            : Math.ceilDiv(Duration.between(created, now).toMillis(), DAY.toMillis());
    return Math.clamp(startedDays, 1, MAX_WAITING_DAYS);
  }

  private int urgency(TaskSummary taskSummary, Instant now) {
    Instant due = taskSummary.getDue();
    if (due == null) {
      return 0;
    }
    long workingDays;
    try {
      workingDays = workingTimeCalculator.workingTimeBetween(now, due).toDays();
    } catch (InvalidArgumentException e) {
      throw new IllegalStateException("Could not compute working time until " + due, e);
    }
    if (due.isAfter(now)) {
      return workingDays < DUE_SOON_WORKING_DAYS ? DUE_SOON_BONUS : 0;
    }
    return OVERDUE_BONUS
        + (int) Math.min(workingDays, MAX_OVERDUE_WORKING_DAYS) * OVERDUE_BONUS_PER_WORKING_DAY;
  }

  /** Stops the job on purpose once the benchmark's time budget is spent. */
  static final class TimeBudgetExhaustedException extends RuntimeException {

    private TimeBudgetExhaustedException() {
      super("Benchmark time budget exhausted - stopping the job on purpose", null, false, false);
    }
  }
}
