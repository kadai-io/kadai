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

package io.kadai.task.api.exceptions;

import io.kadai.common.api.exceptions.ErrorCode;
import io.kadai.common.api.exceptions.KadaiException;
import io.kadai.task.api.models.TaskComment;
import java.util.Map;

/** This exception is thrown when a specific {@linkplain TaskComment} is not in the database. */
public class TaskCommentNotFoundException extends KadaiException {

  public static final String ERROR_KEY = "TASK_COMMENT_NOT_FOUND";
  private final String taskCommentId;

  public TaskCommentNotFoundException(String taskCommentId) {
    super(
        String.format("TaskComment with id '%s' was not found", taskCommentId),
        ErrorCode.of(ERROR_KEY, Map.of("taskCommentId", ensureNullIsHandled(taskCommentId))));
    this.taskCommentId = taskCommentId;
  }

  public String getTaskCommentId() {
    return taskCommentId;
  }
}
