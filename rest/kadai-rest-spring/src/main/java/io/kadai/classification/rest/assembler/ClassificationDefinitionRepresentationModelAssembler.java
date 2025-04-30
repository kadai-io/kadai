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

package io.kadai.classification.rest.assembler;

import io.kadai.classification.api.models.Classification;
import io.kadai.classification.rest.models.ClassificationDefinitionRepresentationModel;
import io.kadai.classification.rest.models.ClassificationRepresentationModel;
import io.kadai.common.rest.assembler.CollectionRepresentationModelAssembler;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ClassificationDefinitionRepresentationModelAssembler
    implements RepresentationModelAssembler<
            Classification, ClassificationDefinitionRepresentationModel>,
        CollectionRepresentationModelAssembler<
            Classification,
            ClassificationDefinitionRepresentationModel,
            ClassificationDefinitionCollectionRepresentationModel> {

  private final ClassificationRepresentationModelAssembler classificationAssembler;

  @Autowired
  public ClassificationDefinitionRepresentationModelAssembler(
      ClassificationRepresentationModelAssembler classificationAssembler) {
    this.classificationAssembler = classificationAssembler;
  }

  @Override
  @NonNull
  public ClassificationDefinitionRepresentationModel toModel(
      @NonNull Classification classification) {
    ClassificationRepresentationModel classificationRepModel =
        classificationAssembler.toModel(classification);
    return new ClassificationDefinitionRepresentationModel(classificationRepModel);
  }

  @Override
  public ClassificationDefinitionCollectionRepresentationModel buildCollectionEntity(
      List<ClassificationDefinitionRepresentationModel> content) {
    return new ClassificationDefinitionCollectionRepresentationModel(content);
  }

  public Classification toEntityModel(ClassificationDefinitionRepresentationModel repModel) {
    return classificationAssembler.toEntityModel(repModel.getClassification());
  }
}
