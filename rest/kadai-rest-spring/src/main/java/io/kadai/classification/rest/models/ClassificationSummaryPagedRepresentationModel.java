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

package io.kadai.classification.rest.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kadai.common.rest.models.PageMetadata;
import io.kadai.common.rest.models.PagedRepresentationModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.beans.ConstructorProperties;
import java.util.Collection;

public class ClassificationSummaryPagedRepresentationModel
    extends PagedRepresentationModel<ClassificationSummaryRepresentationModel> {

  @ConstructorProperties({"classifications", "page"})
  public ClassificationSummaryPagedRepresentationModel(
      Collection<ClassificationSummaryRepresentationModel> content, PageMetadata pageMetadata) {
    super(content, pageMetadata);
  }

  @Schema(name = "classifications", description = "the embedded classifications.")
  @Override
  @JsonProperty("classifications")
  public @NotNull Collection<ClassificationSummaryRepresentationModel> getContent() {
    return super.getContent();
  }
}
