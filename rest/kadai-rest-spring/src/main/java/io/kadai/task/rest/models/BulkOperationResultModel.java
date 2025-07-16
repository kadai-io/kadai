package io.kadai.task.rest.models;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for a single entry in a bulk operation response.
 */
@Schema(description = "Result of a single bulk operation entry")
public class BulkOperationResultModel {
  @Schema(description = "ID of the Entity")
  private String taskId;

  @Schema(description = "Error code if creation failed; null otherwise")
  private String errorCode;

  public BulkOperationResultModel() { }

  public BulkOperationResultModel(String taskId, String errorCode) {
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
