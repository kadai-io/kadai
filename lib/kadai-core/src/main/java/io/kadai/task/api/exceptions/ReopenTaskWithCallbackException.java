package io.kadai.task.api.exceptions;

import io.kadai.common.api.exceptions.ErrorCode;
import io.kadai.common.api.exceptions.KadaiException;
import io.kadai.task.api.models.Task;
import java.util.Map;

/**
 * This exception is thrown when a {@linkplain Task} with a registered callback is attempted to be
 * reopened.
 */
public class ReopenTaskWithCallbackException extends KadaiException {

  public static final String ERROR_KEY = "REOPEN_TASK_WITH_CALLBACK";

  private final String taskId;

  public ReopenTaskWithCallbackException(String taskId) {
    super(
        String.format(
            "Task with id '%s' can't be reopened because it has a callback registered.", taskId),
        ErrorCode.of(ERROR_KEY, Map.ofEntries(Map.entry("taskId", taskId))));
    this.taskId = taskId;
  }

  public String getTaskId() {
    return taskId;
  }
}
