/*
 * Copyright [2025] [envite consulting GmbH]
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

package acceptance.events.task;

import static org.assertj.core.api.Assertions.assertThat;

import acceptance.AbstractAccTest;
import io.kadai.KadaiConfiguration;
import io.kadai.spi.history.api.events.task.TaskHistoryEvent;
import io.kadai.spi.history.api.events.task.TaskHistoryEventType;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;

class GetTaskHistoryEventAccTest extends AbstractAccTest {

  @Test
  void should_ReturnSpecificTaskHistoryEventWithDetails_For_HistoryEventId() throws Exception {

    String detailsJson =
        "{\"changes\":[{"
            + "\"newValue\":\"BPI:01\","
            + "\"fieldName\":\"businessProcessId\","
            + "\"oldValue\":\"BPI:02\"},"
            + "{\"newValue\":\"user-1-1\","
            + "\"fieldName\":\"owner\","
            + "\"oldValue\":\"owner1\"}]}";

    TaskHistoryEvent taskHistoryEvent =
        getHistoryService().getTaskHistoryEvent("THI:000000000000000000000000000000000000");
    assertThat(taskHistoryEvent.getBusinessProcessId()).isEqualTo("BPI:01");
    assertThat(taskHistoryEvent.getUserId()).isEqualTo("user-1-1");
    assertThat(taskHistoryEvent.getProxyAccessId()).isNull();
    assertThat(taskHistoryEvent.getEventType()).isEqualTo(TaskHistoryEventType.UPDATED.getName());
    assertThat(taskHistoryEvent.getDetails()).isEqualTo(detailsJson);
  }

  @Test
  void should_SetTaskOwnerLongNameOfTask_When_PropertyEnabled() throws Exception {

    createKadaiEngineWithNewConfig(true);

    TaskHistoryEvent taskHistoryEvent =
        getHistoryService().getTaskHistoryEvent("THI:000000000000000000000000000000000000");
    assertThat(taskHistoryEvent.getUserId()).isEqualTo("user-1-1");
    assertThat(taskHistoryEvent.getProxyAccessId()).isNull();

    String userLongName =
        kadaiEngine.getUserService().getUser(taskHistoryEvent.getUserId()).getLongName();
    assertThat(taskHistoryEvent)
        .extracting(TaskHistoryEvent::getUserLongName)
        .isEqualTo(userLongName);
  }

  @Test
  void should_NotSetTaskOwnerLongNameOfTask_When_PropertyDisabled() throws Exception {

    createKadaiEngineWithNewConfig(false);

    TaskHistoryEvent taskHistoryEvent =
        getHistoryService().getTaskHistoryEvent("THI:000000000000000000000000000000000000");

    assertThat(taskHistoryEvent.getUserId()).isEqualTo("user-1-1");
    assertThat(taskHistoryEvent.getProxyAccessId()).isNull();

    assertThat(taskHistoryEvent).extracting(TaskHistoryEvent::getUserLongName).isNull();
  }

  private void createKadaiEngineWithNewConfig(boolean addAdditionalUserInfo) throws SQLException {
    KadaiConfiguration configuration =
        new KadaiConfiguration.Builder(AbstractAccTest.kadaiConfiguration)
            .addAdditionalUserInfo(addAdditionalUserInfo)
            .build();
    initKadaiEngine(configuration);
  }
}
