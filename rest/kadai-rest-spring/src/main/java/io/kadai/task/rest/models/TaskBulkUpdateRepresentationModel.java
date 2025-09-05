package io.kadai.task.rest.models;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "EntityModel class for bulk updating multiple tasks.")
public class TaskBulkUpdateRepresentationModel {

  @Schema(description = "List of task IDs to update.")
  private List<String> taskIds;

  @Schema(description = "Fields to update on the tasks. Only non-null fields will be updated.")
  private TaskPatchRepresentationModel fieldsToUpdate;

  public List<String> getTaskIds() {
    return taskIds;
  }

  public void setTaskIds(List<String> taskIds) {
    this.taskIds = taskIds;
  }

  public TaskPatchRepresentationModel getFieldsToUpdate() {
    return fieldsToUpdate;
  }

  public void setFieldsToUpdate(TaskPatchRepresentationModel fieldsToUpdate) {
    this.fieldsToUpdate = fieldsToUpdate;
  }
}
