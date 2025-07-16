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

import io.kadai.common.api.BulkOperationResults;
import io.kadai.common.api.exceptions.KadaiException;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;

/**
 * Response DTO for a bulk operation.
 */
@Schema(description = "Response model for bulk operations")
public class BulkOperationResponseModel {
  @Schema(name = "results",
          description = "The list of individual bulk operation results")
  private List<BulkOperationResultModel> failedIds;

  public BulkOperationResponseModel() { }

  public BulkOperationResponseModel(List<BulkOperationResultModel> failedIds) {
    this.failedIds = failedIds;
  }

  public List<BulkOperationResultModel> getResults() {
    return failedIds;
  }

  public void setResults(List<BulkOperationResultModel> failedIds) {
    this.failedIds = failedIds;
  }

  public static BulkOperationResponseModel getFailures(
            List<String> taskIds,
            BulkOperationResults<String, KadaiException> errorCollector) {

    List<BulkOperationResultModel> failures = taskIds.stream()
        .map(id -> {
          KadaiException ex = errorCollector.getErrorForId(id);
          if (ex == null) {
            return null;
          }
          return new BulkOperationResultModel(id, ex.getErrorCode().getKey());
        })
        .filter(Objects::nonNull)
        .toList();

    return new BulkOperationResponseModel(failures);
  }
}
