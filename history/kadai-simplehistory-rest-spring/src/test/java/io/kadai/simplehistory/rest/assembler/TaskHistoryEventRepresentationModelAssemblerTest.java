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

package io.kadai.simplehistory.rest.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.rest.test.KadaiSpringBootTest;
import io.kadai.simplehistory.rest.models.TaskHistoryEventRepresentationModel;
import io.kadai.spi.history.api.events.task.TaskHistoryCustomField;
import io.kadai.spi.history.api.events.task.TaskHistoryEvent;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Test for {@link TaskHistoryEventRepresentationModelAssembler}. */
@KadaiSpringBootTest
class TaskHistoryEventRepresentationModelAssemblerTest {

  private final TaskHistoryEventRepresentationModelAssembler assembler;

  @Autowired
  TaskHistoryEventRepresentationModelAssemblerTest(
      TaskHistoryEventRepresentationModelAssembler assembler) {
    this.assembler = assembler;
  }

  @Test
  void taskHistoryEventModelToResource() {
    TaskHistoryEvent historyEvent = new TaskHistoryEvent();

    historyEvent.setEventType("TASK_CREATED");
    historyEvent.setBusinessProcessId("BPI:01");
    historyEvent.setParentBusinessProcessId("BPI:02");
    historyEvent.setTaskId("TKI:000000000000000000000000000000000000");
    historyEvent.setTaskClassificationCategory("MANUAL");
    historyEvent.setDomain("DOMAIN_A");
    historyEvent.setWorkbasketKey("WorkbasketKey");
    historyEvent.setAttachmentClassificationKey("L1050");
    historyEvent.setUserLongName("userLongName");
    historyEvent.setCreated(Instant.now());
    historyEvent.setOldValue("oldValue");
    historyEvent.setNewValue("newValue");
    historyEvent.setPorCompany("porCompany");
    historyEvent.setPorSystem("porSystem");
    historyEvent.setPorType("porType");
    historyEvent.setPorValue("porValue");
    historyEvent.setTaskOwnerLongName("taskOwner");
    historyEvent.setCustomAttribute(TaskHistoryCustomField.CUSTOM_1, "custom1");
    historyEvent.setCustomAttribute(TaskHistoryCustomField.CUSTOM_2, "custom2");
    historyEvent.setCustomAttribute(TaskHistoryCustomField.CUSTOM_3, "custom3");
    historyEvent.setCustomAttribute(TaskHistoryCustomField.CUSTOM_4, "custom4");

    TaskHistoryEventRepresentationModel taskHistoryEventRepresentationModel =
        assembler.toModel(historyEvent);

    testEquality(historyEvent, taskHistoryEventRepresentationModel);
  }

  private void testEquality(
      TaskHistoryEvent historyEvent,
      TaskHistoryEventRepresentationModel taskHistoryEventRepresentationModel) {

    assertThat(historyEvent.getEventType())
        .isEqualTo(taskHistoryEventRepresentationModel.getEventType());
    assertThat(historyEvent.getBusinessProcessId())
        .isEqualTo(taskHistoryEventRepresentationModel.getBusinessProcessId());
    assertThat(historyEvent.getParentBusinessProcessId())
        .isEqualTo(taskHistoryEventRepresentationModel.getParentBusinessProcessId());
    assertThat(historyEvent.getTaskId()).isEqualTo(taskHistoryEventRepresentationModel.getTaskId());
    assertThat(historyEvent.getTaskClassificationCategory())
        .isEqualTo(taskHistoryEventRepresentationModel.getTaskClassificationCategory());
    assertThat(historyEvent.getDomain()).isEqualTo(taskHistoryEventRepresentationModel.getDomain());
    assertThat(historyEvent.getWorkbasketKey())
        .isEqualTo(taskHistoryEventRepresentationModel.getWorkbasketKey());
    assertThat(historyEvent.getAttachmentClassificationKey())
        .isEqualTo(taskHistoryEventRepresentationModel.getAttachmentClassificationKey());
    assertThat(historyEvent.getCreated())
        .isEqualTo(taskHistoryEventRepresentationModel.getCreated());
    assertThat(historyEvent.getOldValue())
        .isEqualTo(taskHistoryEventRepresentationModel.getOldValue());
    assertThat(historyEvent.getNewValue())
        .isEqualTo(taskHistoryEventRepresentationModel.getNewValue());
    assertThat(historyEvent.getPorCompany())
        .isEqualTo(taskHistoryEventRepresentationModel.getPorCompany());
    assertThat(historyEvent.getPorSystem())
        .isEqualTo(taskHistoryEventRepresentationModel.getPorSystem());
    assertThat(historyEvent.getPorType())
        .isEqualTo(taskHistoryEventRepresentationModel.getPorType());
    assertThat(historyEvent.getPorValue())
        .isEqualTo(taskHistoryEventRepresentationModel.getPorValue());
    assertThat(historyEvent.getUserLongName())
        .isEqualTo(taskHistoryEventRepresentationModel.getUserLongName());
    assertThat(historyEvent.getTaskOwnerLongName())
        .isEqualTo(taskHistoryEventRepresentationModel.getTaskOwnerLongName());
    assertThat(historyEvent.getCustomAttribute(TaskHistoryCustomField.CUSTOM_1))
        .isEqualTo(taskHistoryEventRepresentationModel.getCustom1());
    assertThat(historyEvent.getCustomAttribute(TaskHistoryCustomField.CUSTOM_2))
        .isEqualTo(taskHistoryEventRepresentationModel.getCustom2());
    assertThat(historyEvent.getCustomAttribute(TaskHistoryCustomField.CUSTOM_3))
        .isEqualTo(taskHistoryEventRepresentationModel.getCustom3());
    assertThat(historyEvent.getCustomAttribute(TaskHistoryCustomField.CUSTOM_4))
        .isEqualTo(taskHistoryEventRepresentationModel.getCustom4());
  }
}
