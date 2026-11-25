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

/**
 * The condition under which a guarded update may modify a Task: the Task must still have the
 * modified timestamp which was read before, see {@link TaskModifiedTimestamps}.
 */
public final class ExpectedTaskModification {

  private final String taskId;
  private final Instant modifiedFrom;
  private final Instant modifiedUntil;

  ExpectedTaskModification(String taskId, Instant expectedModified) {
    this.taskId = taskId;
    if (expectedModified == null) {
      this.modifiedFrom = null;
      this.modifiedUntil = null;
    } else {
      this.modifiedFrom = TaskModifiedTimestamps.lowerBound(expectedModified);
      this.modifiedUntil = TaskModifiedTimestamps.upperBound(expectedModified);
    }
  }

  public String getTaskId() {
    return taskId;
  }

  /**
   * Returns the inclusive lower bound of the expected modified timestamp.
   *
   * @return the lower bound or null, if the Task is expected to have no modified timestamp
   */
  public Instant getModifiedFrom() {
    return modifiedFrom;
  }

  /**
   * Returns the exclusive upper bound of the expected modified timestamp.
   *
   * @return the upper bound or null, if the Task is expected to have no modified timestamp
   */
  public Instant getModifiedUntil() {
    return modifiedUntil;
  }

  @Override
  public String toString() {
    return "ExpectedTaskModification [taskId="
        + taskId
        + ", modifiedFrom="
        + modifiedFrom
        + ", modifiedUntil="
        + modifiedUntil
        + "]";
  }
}
