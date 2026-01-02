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

package io.kadai.monitor.rest.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.monitor.api.reports.header.PriorityColumnHeader;
import io.kadai.monitor.rest.models.PriorityColumnHeaderRepresentationModel;
import io.kadai.rest.test.KadaiSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@KadaiSpringBootTest
class PriorityColumnHeaderRepresentationModelAssemblerTest {

  private final PriorityColumnHeaderRepresentationModelAssembler assembler;

  @Autowired
  public PriorityColumnHeaderRepresentationModelAssemblerTest(
      PriorityColumnHeaderRepresentationModelAssembler assembler) {
    this.assembler = assembler;
  }

  @Test
  void should_convertEntityToRepresentationModel() {
    PriorityColumnHeader columnHeader = new PriorityColumnHeader(10, 20);
    PriorityColumnHeaderRepresentationModel expectedRepModel =
        new PriorityColumnHeaderRepresentationModel(10, 20);

    PriorityColumnHeaderRepresentationModel repModel = assembler.toModel(columnHeader);

    assertThat(repModel).usingRecursiveComparison().isEqualTo(expectedRepModel);
  }

  @Test
  void should_convertRepresentationModelToEntity() {
    PriorityColumnHeaderRepresentationModel repModel =
        new PriorityColumnHeaderRepresentationModel(10, 20);
    PriorityColumnHeader expectedColumnHeader = new PriorityColumnHeader(10, 20);

    PriorityColumnHeader columnHeader = assembler.toEntityModel(repModel);

    assertThat(columnHeader).usingRecursiveComparison().isEqualTo(expectedColumnHeader);
  }
}
