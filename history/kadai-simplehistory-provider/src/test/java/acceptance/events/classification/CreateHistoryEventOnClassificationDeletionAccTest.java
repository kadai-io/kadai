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
import io.kadai.common.api.KadaiRole;
import io.kadai.common.internal.util.CheckedRunnable;
import io.kadai.simplehistory.impl.SimpleHistoryServiceImpl;
import io.kadai.simplehistory.impl.classification.ClassificationHistoryEventMapper;
import io.kadai.spi.history.api.events.classification.ClassificationHistoryEvent;
import io.kadai.spi.history.api.events.classification.ClassificationHistoryEventType;
import java.util.List;
import org.junit.jupiter.api.Test;

class CreateHistoryEventOnClassificationDeletionAccTest extends AbstractAccTest {

  private final SimpleHistoryServiceImpl historyService = getHistoryService();
  private final ClassificationService classificationService =
      kadaiEngine.getClassificationService();
  private final ClassificationHistoryEventMapper classificationHistoryEventMapper =
      getClassificationHistoryEventMapper();

  @Test
  void should_CreateClassificationDeletedHistoryEvent_When_ClassificationIsDeleted()
      throws Exception {
    kadaiEngine.runAs(
        CheckedRunnable.rethrowing(
            () -> {
              final String classificationId = "CLI:200000000000000000000000000000000015";

              List<ClassificationHistoryEvent> events =
                  historyService
                      .createClassificationHistoryQuery()
                      .classificationIdIn(classificationId)
                      .list();

              assertThat(events).isEmpty();

              classificationService.deleteClassification(classificationId);

              events =
                  historyService
                      .createClassificationHistoryQuery()
                      .classificationIdIn(classificationId)
                      .list();
              assertThat(events).hasSize(1);

              String eventType = events.get(0).getEventType();
              String details =
                  classificationHistoryEventMapper.findById(events.get(0).getId()).getDetails();

              assertThat(eventType).isEqualTo(ClassificationHistoryEventType.DELETED.getName());
              assertThat(details)
                  .contains("\"oldValue\":\"CLI:200000000000000000000000000000000015\"");
              assertThat(events.get(0).getUserId()).isEqualTo("user-4-2");
              assertThat(events.get(0).getProxyAccessId())
                  .isEqualTo("cn=business-admins,cn=groups,ou=test,o=kadai");
            }),
        KadaiRole.BUSINESS_ADMIN,
        "user-4-2");
  }
}
