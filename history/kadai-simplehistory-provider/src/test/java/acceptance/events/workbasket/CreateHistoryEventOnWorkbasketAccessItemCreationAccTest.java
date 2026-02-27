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

package acceptance.events.workbasket;

import static org.assertj.core.api.Assertions.assertThat;

import acceptance.AbstractAccTest;
import io.kadai.common.api.KadaiRole;
import io.kadai.common.internal.util.CheckedRunnable;
import io.kadai.simplehistory.workbasket.api.WorkbasketHistoryService;
import io.kadai.simplehistory.workbasket.internal.WorkbasketHistoryEventMapper;
import io.kadai.spi.history.api.events.workbasket.WorkbasketHistoryEvent;
import io.kadai.spi.history.api.events.workbasket.WorkbasketHistoryEventType;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.models.WorkbasketAccessItem;
import java.util.List;
import org.junit.jupiter.api.Test;

class CreateHistoryEventOnWorkbasketAccessItemCreationAccTest extends AbstractAccTest {

  private final WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
  private final WorkbasketHistoryService historyService = AbstractAccTest.workbasketHistoryService;
  private final WorkbasketHistoryEventMapper workbasketHistoryEventMapper =
      getWorkbasketHistoryEventMapper();

  @Test
  void should_CreateWorkbasketAccessItemCreatedHistoryEvent_When_AccessItemIsCreated()
      throws Exception {
    kadaiEngine.runAs(
        CheckedRunnable.rethrowing(
            () -> {
              final String workbasketId = "WBI:100000000000000000000000000000000004";

              List<WorkbasketHistoryEvent> events =
                  historyService.createWorkbasketHistoryQuery().workbasketIdIn(workbasketId).list();

              assertThat(events).isEmpty();

              WorkbasketAccessItem newWorkbasketAccessItem =
                  workbasketService.newWorkbasketAccessItem(workbasketId, "peter");

              workbasketService.createWorkbasketAccessItem(newWorkbasketAccessItem);

              events =
                  historyService.createWorkbasketHistoryQuery().workbasketIdIn(workbasketId).list();

              assertThat(events).hasSize(1);

              String eventType = events.get(0).getEventType();
              String details =
                  workbasketHistoryEventMapper.findById(events.get(0).getId()).getDetails();

              assertThat(eventType)
                  .isEqualTo(WorkbasketHistoryEventType.ACCESS_ITEM_CREATED.getName());
              assertThat(details).contains("\"newValue\":\"peter\"");
              assertThat(events.get(0).getUserId()).isEqualTo("user-6-9");
              assertThat(events.get(0).getProxyAccessId())
                  .isEqualTo("cn=business-admins,cn=groups,ou=test,o=kadai");
            }),
        KadaiRole.BUSINESS_ADMIN,
        "user-6-9");
  }
}
