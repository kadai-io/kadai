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

package io.kadai.task.rest.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.rest.test.KadaiSpringBootTest;
import io.kadai.task.api.models.ObjectReference;
import io.kadai.task.internal.models.ObjectReferenceImpl;
import io.kadai.task.rest.models.ObjectReferenceRepresentationModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@KadaiSpringBootTest
class ObjectReferenceRepresentationModelAssemblerTest {

  private final ObjectReferenceRepresentationModelAssembler assembler;

  @Autowired
  ObjectReferenceRepresentationModelAssemblerTest(
      ObjectReferenceRepresentationModelAssembler assembler) {
    this.assembler = assembler;
  }

  static void testEquality(
      ObjectReference objectReference, ObjectReferenceRepresentationModel repModel) {
    assertThat(objectReference).isNotNull();
    assertThat(objectReference.getId()).isEqualTo(repModel.getId());
    assertThat(objectReference.getCompany()).isEqualTo(repModel.getCompany());
    assertThat(objectReference.getSystem()).isEqualTo(repModel.getSystem());
    assertThat(objectReference.getSystemInstance()).isEqualTo(repModel.getSystemInstance());
    assertThat(objectReference.getType()).isEqualTo(repModel.getType());
    assertThat(objectReference.getValue()).isEqualTo(repModel.getValue());
  }

  @Test
  void should_ReturnEntity_When_ConvertingRepresentationModelToEntity() {
    ObjectReferenceRepresentationModel repModel = new ObjectReferenceRepresentationModel();
    repModel.setId("id");
    repModel.setValue("value");
    repModel.setType("type");
    repModel.setSystem("system");
    repModel.setSystemInstance("instance");
    repModel.setCompany("company");

    ObjectReference objectReference = assembler.toEntity(repModel);

    testEquality(objectReference, repModel);
  }

  @Test
  void should_ReturnRepresentationModel_When_ConvertingEntityToRepresentationModel() {
    ObjectReferenceImpl entity = new ObjectReferenceImpl();
    entity.setId("id");
    entity.setValue("value");
    entity.setType("type");
    entity.setSystem("system");
    entity.setSystemInstance("instance");
    entity.setCompany("company");

    ObjectReferenceRepresentationModel representationModel = assembler.toModel(entity);

    testEquality(entity, representationModel);
  }
}
