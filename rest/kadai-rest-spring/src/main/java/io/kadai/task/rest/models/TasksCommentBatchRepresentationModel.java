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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.beans.ConstructorProperties;
import java.util.List;
import org.springframework.hateoas.RepresentationModel;

public class TasksCommentBatchRepresentationModel
        extends RepresentationModel<TasksCommentBatchRepresentationModel> {

  /** List of Task IDs. The comment will be added to each task in this list. */
  @JsonProperty("taskIds")
  private final List<String> taskIds;

  /** The content of the comment. */
  @JsonProperty("textField")
  private final String textField;

  @ConstructorProperties({"taskIds", "textField"})
  public TasksCommentBatchRepresentationModel(
          List<String> taskIds,
          String textField) {
    this.taskIds = taskIds;
    this.textField = textField;
  }

  public List<String> getTaskIds() {
    return taskIds;
  }

  public String getTextField() {
    return textField;
  }
}
