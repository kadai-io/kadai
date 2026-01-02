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
import io.kadai.workbasket.api.WorkbasketType;
import io.kadai.workbasket.api.models.Workbasket;
import java.util.List;
import org.junit.jupiter.api.Test;

class CreateHistoryEventOnWorkbasketCreationAccTest extends AbstractAccTest {

  private final WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
  private final SimpleHistoryServiceImpl historyService = getHistoryService();
  private final WorkbasketHistoryEventMapper workbasketHistoryEventMapper =
      getWorkbasketHistoryEventMapper();

  @Test
  void should_CreateWorkbasketAccessItemDeletedHistoryEvents_When_AccessItemsAreDeleted()
      throws Exception {
    kadaiEngine.runAsAdmin(
        CheckedRunnable.rethrowing(
            () -> {
              Workbasket newWorkbasket = workbasketService.newWorkbasket("NT1234", "DOMAIN_A");
              newWorkbasket.setName("Megabasket");
              newWorkbasket.setType(WorkbasketType.GROUP);
              newWorkbasket.setOrgLevel1("company");
              newWorkbasket = workbasketService.createWorkbasket(newWorkbasket);

              List<WorkbasketHistoryEvent> events =
                  historyService
                      .createWorkbasketHistoryQuery()
                      .workbasketIdIn(newWorkbasket.getId())
                      .list();

              assertThat(events).hasSize(1);

              String eventType = events.get(0).getEventType();
              String details =
                  workbasketHistoryEventMapper.findById(events.get(0).getId()).getDetails();

              assertThat(eventType).isEqualTo(WorkbasketHistoryEventType.CREATED.getName());
              assertThat(details).contains("Megabasket");
              assertThat(events.get(0).getUserId()).isEqualTo("admin");
              assertThat(events.get(0).getProxyAccessId()).isNull();
            }));
  }
}
