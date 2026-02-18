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

import static io.kadai.common.api.BaseQuery.SortDirection.ASCENDING;
import static org.assertj.core.api.Assertions.assertThat;

import acceptance.AbstractAccTest;
import io.kadai.common.internal.util.CheckedRunnable;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.common.test.security.WithAccessId;
import io.kadai.simplehistory.workbasket.api.WorkbasketHistoryService;
import io.kadai.simplehistory.workbasket.internal.WorkbasketHistoryEventMapper;
import io.kadai.spi.history.api.events.workbasket.WorkbasketHistoryEvent;
import io.kadai.spi.history.api.events.workbasket.WorkbasketHistoryEventType;
import io.kadai.workbasket.api.WorkbasketService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JaasExtension.class)
class CreateHistoryEventOnWorkbasketAccessItemsDeletionAccTest extends AbstractAccTest {

  private final WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
  private final WorkbasketHistoryService historyService = AbstractAccTest.workbasketHistoryService;
  private final WorkbasketHistoryEventMapper workbasketHistoryEventMapper =
      getWorkbasketHistoryEventMapper();

  @Test
  void should_CreateWorkbasketAccessItemDeletedHistoryEvents_When_AccessItemsAreDeleted()
      throws Exception {
    kadaiEngine.runAsAdmin(
        CheckedRunnable.rethrowing(
            () -> {
              final String accessId = "teamlead-1";

              String[] workbasketIds =
                  new String[] {
                    "WBI:100000000000000000000000000000000001",
                    "WBI:100000000000000000000000000000000004",
                    "WBI:100000000000000000000000000000000005",
                    "WBI:100000000000000000000000000000000010"
                  };

              List<WorkbasketHistoryEvent> events =
                  historyService
                      .createWorkbasketHistoryQuery()
                      .workbasketIdIn(workbasketIds)
                      .list();

              assertThat(events).isEmpty();

              workbasketService.deleteWorkbasketAccessItemsForAccessId(accessId);

              events =
                  historyService
                      .createWorkbasketHistoryQuery()
                      .workbasketIdIn(workbasketIds)
                      .orderByWorkbasketId(ASCENDING)
                      .list();

              assertThat(events).hasSize(4);

              String details =
                  workbasketHistoryEventMapper.findById(events.get(0).getId()).getDetails();

              assertThat(events)
                  .extracting(WorkbasketHistoryEvent::getEventType)
                  .containsOnly(WorkbasketHistoryEventType.ACCESS_ITEM_DELETED.getName());
              assertThat(events)
                  .extracting(WorkbasketHistoryEvent::getUserId)
                  .containsOnly("user-6-6");
              assertThat(events)
                  .extracting(WorkbasketHistoryEvent::getProxyAccessId)
                  .containsOnly("admin");

              assertThat(details)
                  .contains("\"oldValue\":\"WBI:100000000000000000000000000000000001\"");
            }),
        "user-6-6");
  }

  @WithAccessId(user = "admin")
  @Test
  void should_NotCreateWorkbasketAccessItemDeletedHistoryEvents_When_ProvidingInvalidAccessId()
      throws Exception {

    final String workbasketId = "WBI:100000000000000000000000000000000011";

    final String accessId = "NonExistingWorkbasketAccessItemID";

    List<WorkbasketHistoryEvent> events =
        historyService.createWorkbasketHistoryQuery().workbasketIdIn(workbasketId).list();

    assertThat(events).isEmpty();

    workbasketService.deleteWorkbasketAccessItemsForAccessId(accessId);

    events = historyService.createWorkbasketHistoryQuery().workbasketIdIn(workbasketId).list();

    assertThat(events).isEmpty();
  }
}
