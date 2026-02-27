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
import io.kadai.classification.api.ClassificationCustomField;
import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.models.Classification;
import io.kadai.common.api.KadaiRole;
import io.kadai.common.internal.util.CheckedRunnable;
import io.kadai.simplehistory.classification.internal.ClassificationHistoryEventMapper;
import io.kadai.spi.history.api.events.classification.ClassificationHistoryEvent;
import io.kadai.spi.history.api.events.workbasket.WorkbasketHistoryEventType;
import java.util.List;
import org.junit.jupiter.api.Test;

class CreateHistoryEventOnClassificationsUpdateAccTest extends AbstractAccTest {

  private final ClassificationService classificationService =
      kadaiEngine.getClassificationService();
  private final ClassificationHistoryEventMapper classificationHistoryEventMapper =
      getClassificationHistoryEventMapper();

  @Test
  void should_CreateClassificationUpdatedHistoryEvent_When_ClassificationIsUpdated()
      throws Exception {
    kadaiEngine.runAs(
        CheckedRunnable.rethrowing(
            () -> {
              Classification classification =
                  classificationService.getClassification(
                      "CLI:000000000000000000000000000000000017");

              List<ClassificationHistoryEvent> events =
                  classificationHistoryService
                      .createClassificationHistoryQuery()
                      .classificationIdIn(classification.getId())
                      .list();

              assertThat(events).isEmpty();

              classification.setName("new name");
              classification.setDescription("new description");
              classification.setCategory("EXTERNAL");
              classification.setCustomField(ClassificationCustomField.CUSTOM_1, "new custom 1");
              classification.setCustomField(ClassificationCustomField.CUSTOM_2, "new custom 2");
              classification.setCustomField(ClassificationCustomField.CUSTOM_3, "new custom 3");
              classification.setCustomField(ClassificationCustomField.CUSTOM_4, "new custom 4");
              classificationService.updateClassification(classification);

              events =
                  classificationHistoryService
                      .createClassificationHistoryQuery()
                      .classificationIdIn(classification.getId())
                      .list();

              assertThat(events).hasSize(1);

              String eventType = events.get(0).getEventType();
              String details =
                  classificationHistoryEventMapper.findById(events.get(0).getId()).getDetails();

              assertThat(eventType).isEqualTo(WorkbasketHistoryEventType.UPDATED.getName());
              assertThat(details).contains("\"newValue\":\"new description\"");
              assertThat(events.get(0).getUserId()).isEqualTo("teamlead-1");
              assertThat(events.get(0).getProxyAccessId())
                  .isEqualTo("cn=business-admins,cn=groups,ou=test,o=kadai");
            }),
        KadaiRole.BUSINESS_ADMIN,
        "teamlead-1");
  }
}
