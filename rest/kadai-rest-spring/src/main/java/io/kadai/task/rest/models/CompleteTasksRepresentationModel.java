package io.kadai.task.rest.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class CompleteTasksRepresentationModel {

  /** The value to set the Task property taskIds. */
  @JsonProperty("taskIds")
  private final List<String> taskIds;

  public CompleteTasksRepresentationModel(
        @JsonProperty("taskIds") List<String> taskIds) {
    this.taskIds = taskIds;
  }

  public List<String> getTaskIds() {
    return taskIds;
  }
}
