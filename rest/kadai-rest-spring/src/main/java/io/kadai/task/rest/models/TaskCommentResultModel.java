package io.kadai.task.rest.models;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response model for a single task comment creation.
 */
@Schema(description = "Response model for a single task comment creation")
public class TaskCommentResultModel {
  @Schema(description = "ID der Task")
  private String taskId;

  @Schema(description = "Error code if creation failed; null otherwise")
  private String errorCode;

  public TaskCommentResultModel() {}

  public TaskCommentResultModel(String taskId, String errorCode) {
    this.taskId = taskId;
    this.errorCode = errorCode;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }
}
