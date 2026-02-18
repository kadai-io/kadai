/*
 * Copyright [2026] [envite consulting GmbH]
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
