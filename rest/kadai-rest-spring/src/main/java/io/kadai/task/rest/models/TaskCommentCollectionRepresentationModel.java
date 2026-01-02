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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kadai.common.rest.models.CollectionRepresentationModel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.beans.ConstructorProperties;
import java.util.Collection;

public class TaskCommentCollectionRepresentationModel
    extends CollectionRepresentationModel<TaskCommentRepresentationModel> {

  @ConstructorProperties("taskComments")
  public TaskCommentCollectionRepresentationModel(
      Collection<TaskCommentRepresentationModel> content) {
    super(content);
  }

  @Schema(name = "taskComments", description = "The embedded task comments.")
  @JsonProperty("taskComments")
  @Override
  public Collection<TaskCommentRepresentationModel> getContent() {
    return super.getContent();
  }
}
