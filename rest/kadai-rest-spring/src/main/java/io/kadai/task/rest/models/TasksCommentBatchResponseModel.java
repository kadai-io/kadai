package io.kadai.task.rest.models;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Batch response model holding multiple TaskComment creation results.
 */
@Schema(description = "Batch response model holding multiple TaskComment creation results")
public class TasksCommentBatchResponseModel {
  @Schema(name = "results",
          description = "The list of individual task comment results")
  private List<TaskCommentResultModel> results;

  public TasksCommentBatchResponseModel() {}

  public TasksCommentBatchResponseModel(List<TaskCommentResultModel> results) {
    this.results = results;
  }

  public List<TaskCommentResultModel> getResults() {
    return results;
  }

  public void setResults(List<TaskCommentResultModel> results) {
    this.results = results;
  }
}
