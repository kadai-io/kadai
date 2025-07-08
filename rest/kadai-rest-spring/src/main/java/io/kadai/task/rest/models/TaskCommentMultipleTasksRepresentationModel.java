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
import java.util.List;
import org.springframework.hateoas.RepresentationModel;

/**
 * EntityModel class for adding a comment to multiple tasks.
 */
@Schema(description = "EntityModel class for adding a comment to multiple tasks.")
public class TaskCommentMultipleTasksRepresentationModel
        extends RepresentationModel<TaskCommentMultipleTasksRepresentationModel> {
  @Schema(name = "taskIds",
          description = "List of Task IDs. The comment will be added to each task in this list.")
  private List<String> taskIds;

  @Schema(name = "textField",
          description = "The content of the comment.")
  private String textField;

  public List<String> getTaskIds() {
    return taskIds;
  }

  public void setTaskIds(List<String> taskIds) {
    this.taskIds = taskIds;
  }

  public String getTextField() {
    return textField;
  }

  public void setTextField(String textField) {
    this.textField = textField;
  }
}
