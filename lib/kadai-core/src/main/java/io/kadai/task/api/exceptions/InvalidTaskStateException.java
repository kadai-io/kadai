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

package io.kadai.task.api.exceptions;

import io.kadai.common.api.exceptions.ErrorCode;
import io.kadai.common.api.exceptions.KadaiException;
import io.kadai.task.api.TaskState;
import io.kadai.task.api.models.Task;
import java.util.Arrays;
import java.util.Map;

/**
 * This exception is thrown when the {@linkplain Task#getState() state} of the {@linkplain Task}
 * doesn't allow the requested operation.
 */
public class InvalidTaskStateException extends KadaiException {

  public static final String ERROR_KEY = "TASK_INVALID_STATE";
  private final String taskId;
  private final TaskState taskState;
  private final TaskState[] requiredTaskStates;

  public InvalidTaskStateException(
      String taskId, TaskState taskState, TaskState... requiredTaskStates) {
    super(
        String.format(
            "Task with id '%s' is in state: '%s', but must be in one of these states: '%s'",
            taskId, taskState, Arrays.toString(requiredTaskStates)),
        ErrorCode.of(
            ERROR_KEY,
            Map.ofEntries(
                Map.entry("taskId", taskId),
                Map.entry("taskState", taskState),
                Map.entry("requiredTaskStates", requiredTaskStates))));

    this.taskId = taskId;
    this.taskState = taskState;
    this.requiredTaskStates = requiredTaskStates;
  }

  public String getTaskId() {
    return taskId;
  }

  public TaskState getTaskState() {
    return taskState;
  }

  public TaskState[] getRequiredTaskStates() {
    return requiredTaskStates;
  }
}
