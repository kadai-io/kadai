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
import io.kadai.workbasket.api.WorkbasketCustomField;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.WorkbasketType;
import io.kadai.workbasket.api.models.Workbasket;
import java.util.List;
import org.junit.jupiter.api.Test;

class CreateHistoryEventOnWorkbasketUpdateAccTest extends AbstractAccTest {

  private final WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
  private final WorkbasketHistoryService historyService = AbstractAccTest.workbasketHistoryService;
  private final WorkbasketHistoryEventMapper workbasketHistoryEventMapper =
      getWorkbasketHistoryEventMapper();

  @Test
  void should_CreateWorkbasketUpdatedHistoryEvent_When_WorkbasketIsUpdated() throws Exception {
    kadaiEngine.runAs(
        CheckedRunnable.rethrowing(
            () -> {
              Workbasket workbasket = workbasketService.getWorkbasket("GPK_KSC", "DOMAIN_A");

              List<WorkbasketHistoryEvent> events =
                  historyService
                      .createWorkbasketHistoryQuery()
                      .workbasketIdIn(workbasket.getId())
                      .list();

              assertThat(events).isEmpty();

              workbasket.setName("new name");
              workbasket.setDescription("new description");
              workbasket.setType(WorkbasketType.TOPIC);
              workbasket.setOrgLevel1("new level 1");
              workbasket.setOrgLevel2("new level 2");
              workbasket.setOrgLevel3("new level 3");
              workbasket.setOrgLevel4("new level 4");
              workbasket.setCustomField(WorkbasketCustomField.CUSTOM_1, "new custom 1");
              workbasket.setCustomField(WorkbasketCustomField.CUSTOM_2, "new custom 2");
              workbasket.setCustomField(WorkbasketCustomField.CUSTOM_3, "new custom 3");
              workbasket.setCustomField(WorkbasketCustomField.CUSTOM_4, "new custom 4");
              workbasket.setDescription("new description");
              workbasketService.updateWorkbasket(workbasket);

              events =
                  historyService
                      .createWorkbasketHistoryQuery()
                      .workbasketIdIn(workbasket.getId())
                      .list();

              assertThat(events).hasSize(1);

              String eventType = events.get(0).getEventType();
              String details =
                  workbasketHistoryEventMapper.findById(events.get(0).getId()).getDetails();

              assertThat(eventType).isEqualTo(WorkbasketHistoryEventType.UPDATED.getName());
              assertThat(details).contains("\"new level 1\"");
              assertThat(events.get(0).getUserId()).isEqualTo("user-8-8");
              assertThat(events.get(0).getProxyAccessId())
                  .isEqualTo("cn=business-admins,cn=groups,ou=test,o=kadai");
            }),
        KadaiRole.BUSINESS_ADMIN,
        "user-8-8");
  }
}
