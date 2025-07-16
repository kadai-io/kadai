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
