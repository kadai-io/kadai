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

import io.kadai.task.api.models.TaskComment;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import org.springframework.hateoas.RepresentationModel;

/** EntityModel class for {@link TaskComment}. */
@Schema(description = "EntityModel class for TaskComment")
public class TaskCommentRepresentationModel
    extends RepresentationModel<TaskCommentRepresentationModel> {

  @Schema(name = "taskCommentId", description = "Unique Id.")
  private String taskCommentId;

  @Schema(name = "taskId", description = "Task Id. Can identify the task the comment belongs to.")
  private String taskId;

  @Schema(name = "textField", description = "The content of the comment.")
  private String textField;

  @Schema(name = "creator", description = "The creator of the task comment.")
  private String creator;

  @Schema(name = "creatorFullName", description = "The long name of the task comment creator.")
  private String creatorFullName;

  @Schema(name = "created", description = "The creation timestamp in the system.")
  private Instant created;

  @Schema(name = "modified", description = "Timestamp of the last task comment modification.")
  private Instant modified;

  public String getTaskCommentId() {
    return taskCommentId;
  }

  public void setTaskCommentId(String taskCommentId) {
    this.taskCommentId = taskCommentId;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getTextField() {
    return textField;
  }

  public void setTextField(String textField) {
    this.textField = textField;
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public String getCreatorFullName() {
    return creatorFullName;
  }

  public void setCreatorFullName(String creatorFullName) {
    this.creatorFullName = creatorFullName;
  }

  public Instant getCreated() {
    return created;
  }

  public void setCreated(Instant created) {
    this.created = created;
  }

  public Instant getModified() {
    return modified;
  }

  public void setModified(Instant modified) {
    this.modified = modified;
  }
}
