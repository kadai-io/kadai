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

package io.kadai.classification.rest.assembler;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kadai.classification.rest.models.ClassificationDefinitionRepresentationModel;
import io.kadai.common.rest.models.CollectionRepresentationModel;
import java.beans.ConstructorProperties;
import java.util.Collection;

public class ClassificationDefinitionCollectionRepresentationModel
    extends CollectionRepresentationModel<ClassificationDefinitionRepresentationModel> {

  @ConstructorProperties("classifications")
  public ClassificationDefinitionCollectionRepresentationModel(
      Collection<ClassificationDefinitionRepresentationModel> content) {
    super(content);
  }

  /** the embedded classification definitions. */
  @JsonProperty("classifications")
  @Override
  public Collection<ClassificationDefinitionRepresentationModel> getContent() {
    return super.getContent();
  }
}
