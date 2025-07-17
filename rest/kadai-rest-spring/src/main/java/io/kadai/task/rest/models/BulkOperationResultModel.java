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
