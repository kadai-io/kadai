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

package io.kadai.task.api;

import java.util.Arrays;

/** The TaskState contains all status of a {@linkplain io.kadai.task.api.models.Task Task}. */
public enum TaskState {
  READY,
  CLAIMED,
  READY_FOR_REVIEW,
  IN_REVIEW,
  COMPLETED,
  CANCELLED,
  TERMINATED;

  public static final TaskState[] END_STATES = {COMPLETED, CANCELLED, TERMINATED};
  public static final TaskState[] FINAL_STATES = {TERMINATED};
  public static final TaskState[] CLAIMED_STATES = {CLAIMED, IN_REVIEW};

  public boolean in(TaskState... states) {
    return Arrays.asList(states).contains(this);
  }

  public boolean isEndState() {
    return in(END_STATES);
  }

  public boolean isFinalState() {
    return in(FINAL_STATES);
  }

  public boolean isClaimedState() {
    return in(CLAIMED_STATES);
  }
}
