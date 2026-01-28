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
import io.kadai.common.internal.util.CheckedRunnable;
import io.kadai.simplehistory.impl.SimpleHistoryServiceImpl;
import io.kadai.simplehistory.impl.workbasket.WorkbasketHistoryEventMapper;
import io.kadai.spi.history.api.events.workbasket.WorkbasketHistoryEvent;
import io.kadai.spi.history.api.events.workbasket.WorkbasketHistoryEventType;
import io.kadai.workbasket.api.WorkbasketService;
import java.util.List;
import org.junit.jupiter.api.Test;

class CreateHistoryEventOnWorkbasketDeletionAccTest extends AbstractAccTest {

  private final WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
  private final SimpleHistoryServiceImpl historyService = getHistoryService();
  private final WorkbasketHistoryEventMapper workbasketHistoryEventMapper =
      getWorkbasketHistoryEventMapper();

  @Test
  void should_CreateWorkbasketDeletedHistoryEvent_When_WorkbasketIsDeleted() throws Exception {
    kadaiEngine.runAsAdmin(
        CheckedRunnable.rethrowing(
            () -> {
              final String workbasketId = "WBI:100000000000000000000000000000000008";

              List<WorkbasketHistoryEvent> events =
                  historyService.createWorkbasketHistoryQuery().workbasketIdIn(workbasketId).list();

              assertThat(events).isEmpty();

              workbasketService.deleteWorkbasket(workbasketId);

              events =
                  historyService.createWorkbasketHistoryQuery().workbasketIdIn(workbasketId).list();
              assertThat(events).hasSize(1);

              String eventType = events.get(0).getEventType();
              String details =
                  workbasketHistoryEventMapper.findById(events.get(0).getId()).getDetails();

              assertThat(eventType).isEqualTo(WorkbasketHistoryEventType.DELETED.getName());
              assertThat(details)
                  .contains("\"oldValue\":\"WBI:100000000000000000000000000000000008\"");
              assertThat(events.get(0).getUserId()).isEqualTo("user-7-8");
              assertThat(events.get(0).getProxyAccessId()).isEqualTo("admin");
            }),
        "user-7-8");
  }
}
