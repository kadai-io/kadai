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

package io.kadai.task.internal;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

/**
 * The {@linkplain io.kadai.task.api.models.TaskSummary#getModified() modified} timestamp of a Task
 * is used as optimistic lock: a write is only applied if the Task has not been modified since it
 * was read.
 *
 * <p>Tasks hold their timestamps with millisecond precision, while the database may store values
 * with a higher precision. Therefore, a Task counts as unmodified as long as its persisted
 * timestamp lies within the same millisecond as the timestamp which was read, see {@link
 * #lowerBound(Instant)} and {@link #upperBound(Instant)}. To ensure that every guarded write is
 * detectable by concurrent writers, the timestamp written by such a write is always at least one
 * millisecond after the timestamp it replaces, see {@link #next(Instant, Instant)}.
 */
final class TaskModifiedTimestamps {

  /** The number of attempts of an operation before it gives up on concurrent modifications. */
  static final int MAX_UPDATE_ATTEMPTS = 3;

  /**
   * The maximum number of Tasks per guarded bulk update. Each Task adds three parameters to the
   * statement, which must not exceed the parameter limits of the database.
   */
  static final int GUARDED_UPDATE_BATCH_SIZE = 500;

  private TaskModifiedTimestamps() {}

  /**
   * Returns the modified timestamp to persist when replacing the given modified timestamp.
   *
   * @param now the current time
   * @param previousModified the modified timestamp which is replaced, may be null
   * @return the current time with millisecond precision, but at least one millisecond after the
   *     given modified timestamp
   */
  static Instant next(Instant now, Instant previousModified) {
    Instant next = now.truncatedTo(ChronoUnit.MILLIS);
    if (previousModified != null) {
      Instant earliest = upperBound(previousModified);
      if (next.isBefore(earliest)) {
        next = earliest;
      }
    }
    return next;
  }

  /**
   * Returns the modified timestamp to persist when replacing all the given modified timestamps.
   *
   * @param now the current time
   * @param previousModified the modified timestamps which are replaced, may contain null
   * @return the current time with millisecond precision, but at least one millisecond after each of
   *     the given modified timestamps
   */
  static Instant next(Instant now, Collection<Instant> previousModified) {
    Instant next = now.truncatedTo(ChronoUnit.MILLIS);
    for (Instant previous : previousModified) {
      next = next(next, previous);
    }
    return next;
  }

  /**
   * Returns the inclusive lower bound of persisted timestamps which are considered equal to the
   * given modified timestamp.
   *
   * @param modified the modified timestamp which was read
   * @return the inclusive lower bound
   */
  static Instant lowerBound(Instant modified) {
    return modified.truncatedTo(ChronoUnit.MILLIS);
  }

  /**
   * Returns the exclusive upper bound of persisted timestamps which are considered equal to the
   * given modified timestamp.
   *
   * @param modified the modified timestamp which was read
   * @return the exclusive upper bound
   */
  static Instant upperBound(Instant modified) {
    return lowerBound(modified).plus(1, ChronoUnit.MILLIS);
  }
}
