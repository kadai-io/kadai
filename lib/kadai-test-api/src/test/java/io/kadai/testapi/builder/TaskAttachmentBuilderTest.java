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

package io.kadai.testapi.builder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.models.Attachment;
import io.kadai.task.api.models.ObjectReference;
import io.kadai.task.internal.models.AttachmentImpl;
import io.kadai.testapi.DefaultTestEntities;
import io.kadai.testapi.KadaiInject;
import io.kadai.testapi.KadaiIntegrationTest;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

@KadaiIntegrationTest
class TaskAttachmentBuilderTest {

  @KadaiInject ClassificationService classificationService;
  @KadaiInject TaskService taskService;

  @Test
  void should_PopulateAttachment_When_UsingEveryBuilderFunction() throws Exception {
    ClassificationSummary classificationSummary =
        ClassificationBuilder.newClassification()
            .domain("DOMAIN_A")
            .key("key")
            .buildAndStoreAsSummary(classificationService, "businessadmin");
    ObjectReference objectReference = DefaultTestEntities.defaultTestObjectReference().build();

    final Attachment attachment =
        TaskAttachmentBuilder.newAttachment()
            .received(Instant.parse("2010-01-01T12:00:00Z"))
            .created(Instant.parse("2010-01-02T12:00:00Z"))
            .modified(Instant.parse("2010-01-03T12:00:00Z"))
            .classificationSummary(classificationSummary)
            .objectReference(objectReference)
            .channel("Channel Super Fun")
            .customAttributes(Map.of("custom", "attribute"))
            .build();

    AttachmentImpl expectedAttachment = (AttachmentImpl) taskService.newAttachment();
    expectedAttachment.setReceived(Instant.parse("2010-01-01T12:00:00Z"));
    expectedAttachment.setCreated(Instant.parse("2010-01-02T12:00:00Z"));
    expectedAttachment.setModified(Instant.parse("2010-01-03T12:00:00Z"));
    expectedAttachment.setClassificationSummary(classificationSummary);
    expectedAttachment.setObjectReference(objectReference);
    expectedAttachment.setChannel("Channel Super Fun");
    expectedAttachment.setCustomAttributes(Map.of("custom", "attribute"));

    assertThat(attachment)
        .hasNoNullFieldsOrPropertiesExcept("id", "taskId")
        .usingRecursiveComparison()
        .ignoringFields("id")
        .isEqualTo(expectedAttachment);
  }
}
