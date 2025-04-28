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

/**
 * This exception is thrown when the current user is not the creator of the {@linkplain TaskComment}
 * it tries to modify.
 */
public class NotAuthorizedOnTaskCommentException extends KadaiException {

  public static final String ERROR_KEY = "NOT_AUTHORIZED_ON_TASK_COMMENT";
  private final String currentUserId;
  private final String taskCommentId;

  public NotAuthorizedOnTaskCommentException(String currentUserId, String taskCommentId) {
    super(
        String.format(
            "Not authorized. Current user '%s' is not the creator of TaskComment with id '%s'.",
            currentUserId, taskCommentId),
        ErrorCode.of(
            ERROR_KEY,
            Map.ofEntries(
                Map.entry("currentUserId", ensureNullIsHandled(currentUserId)),
                Map.entry("taskCommentId", ensureNullIsHandled(taskCommentId)))));

    this.currentUserId = currentUserId;
    this.taskCommentId = taskCommentId;
  }

  public String getCurrentUserId() {
    return currentUserId;
  }

  public String getTaskCommentId() {
    return taskCommentId;
  }
}
