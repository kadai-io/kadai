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

package acceptance.events.classification;

import static org.assertj.core.api.Assertions.assertThat;

import acceptance.AbstractAccTest;
import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.models.Classification;
import io.kadai.common.api.KadaiRole;
import io.kadai.common.internal.util.CheckedRunnable;
import io.kadai.simplehistory.impl.SimpleHistoryServiceImpl;
import io.kadai.simplehistory.impl.classification.ClassificationHistoryEventMapper;
import io.kadai.spi.history.api.events.classification.ClassificationHistoryEvent;
import io.kadai.spi.history.api.events.classification.ClassificationHistoryEventType;
import java.util.List;
import org.junit.jupiter.api.Test;

class CreateHistoryEventOnClassificationsCreationAccTest extends AbstractAccTest {

  private final SimpleHistoryServiceImpl historyService = getHistoryService();
  private final ClassificationService classificationService =
      kadaiEngine.getClassificationService();
  private final ClassificationHistoryEventMapper classificationHistoryEventMapper =
      getClassificationHistoryEventMapper();

  @Test
  void should_CreateClassificationCreatedHistoryEvents_When_ClassificationIsDeleted()
      throws Exception {

    kadaiEngine.runAs(
        CheckedRunnable.rethrowing(
            () -> {
              Classification newClassification =
                  classificationService.newClassification("somekey", "DOMAIN_A", "TASK");
              newClassification.setDescription("some description");
              newClassification.setServiceLevel("P1D");
              newClassification = classificationService.createClassification(newClassification);

              List<ClassificationHistoryEvent> events =
                  historyService
                      .createClassificationHistoryQuery()
                      .classificationIdIn(newClassification.getId())
                      .list();

              assertThat(events).hasSize(1);

              String eventType = events.get(0).getEventType();
              String details =
                  classificationHistoryEventMapper.findById(events.get(0).getId()).getDetails();

              assertThat(eventType).isEqualTo(ClassificationHistoryEventType.CREATED.getName());
              assertThat(details).contains("\"newValue\":\"some description\"");
              assertThat(events.get(0).getUserId()).isEqualTo("user-1-2");
              assertThat(events.get(0).getProxyAccessId()).isEqualTo("admin");
            }),
        KadaiRole.ADMIN,
        "user-1-2");
  }
}
