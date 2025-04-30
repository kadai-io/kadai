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
import io.kadai.common.rest.models.PageMetadata;
import io.kadai.common.rest.models.PagedRepresentationModel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.beans.ConstructorProperties;
import java.util.Collection;

public class TaskSummaryPagedRepresentationModel
    extends PagedRepresentationModel<TaskSummaryRepresentationModel> {

  @ConstructorProperties({"tasks", "page"})
  public TaskSummaryPagedRepresentationModel(
      Collection<TaskSummaryRepresentationModel> content, PageMetadata pageMetadata) {
    super(content, pageMetadata);
  }

  @Schema(name = "tasks", description = "The embedded tasks.")
  @JsonProperty("tasks")
  @Override
  public Collection<TaskSummaryRepresentationModel> getContent() {
    return super.getContent();
  }
}
