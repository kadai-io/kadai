/*
 * Copyright [2024] [envite consulting GmbH]
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
import io.kadai.common.test.security.JaasExtension;
import io.kadai.common.test.security.WithAccessId;
import io.kadai.simplehistory.SimpleHistoryServiceImpl;
import io.kadai.simplehistory.workbasket.internal.WorkbasketHistoryEventMapper;
import io.kadai.spi.history.api.events.workbasket.WorkbasketHistoryEvent;
import io.kadai.spi.history.api.events.workbasket.WorkbasketHistoryEventType;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.models.WorkbasketAccessItem;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JaasExtension.class)
class CreateHistoryEventOnWorkbasketAccessItemCreationAccTest extends AbstractAccTest {

  private final WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
  private final SimpleHistoryServiceImpl historyService = getHistoryService();
  private final WorkbasketHistoryEventMapper workbasketHistoryEventMapper =
      getWorkbasketHistoryEventMapper();

  @WithAccessId(user = "admin")
  @Test
  void should_CreateWorkbasketAccessItemCreatedHistoryEvent_When_AccessItemIsCreated()
      throws Exception {

    final String workbasketId = "WBI:100000000000000000000000000000000004";

    List<WorkbasketHistoryEvent> events =
        historyService.createWorkbasketHistoryQuery().workbasketIdIn(workbasketId).list();

    assertThat(events).isEmpty();

    WorkbasketAccessItem newWorkbasketAccessItem =
        workbasketService.newWorkbasketAccessItem(workbasketId, "peter");

    workbasketService.createWorkbasketAccessItem(newWorkbasketAccessItem);

    events = historyService.createWorkbasketHistoryQuery().workbasketIdIn(workbasketId).list();

    assertThat(events).hasSize(1);

    String eventType = events.get(0).getEventType();
    String details = workbasketHistoryEventMapper.findById(events.get(0).getId()).getDetails();

    assertThat(eventType).isEqualTo(WorkbasketHistoryEventType.ACCESS_ITEM_CREATED.getName());

    assertThat(details).contains("\"newValue\":\"peter\"");
  }
}
