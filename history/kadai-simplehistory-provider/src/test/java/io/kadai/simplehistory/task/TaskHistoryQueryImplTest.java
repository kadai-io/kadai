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

package io.kadai.simplehistory.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.when;

import io.kadai.KadaiConfiguration;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.TimeInterval;
import io.kadai.common.internal.InternalKadaiEngine;
import io.kadai.common.internal.util.IdGenerator;
import io.kadai.simplehistory.task.internal.TaskHistoryQueryImpl;
import io.kadai.spi.history.api.events.task.TaskHistoryEvent;
import io.kadai.spi.history.api.events.task.TaskHistoryEventType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit Test for TaskHistoryQueryImplTest. */
@ExtendWith(MockitoExtension.class)
class TaskHistoryQueryImplTest {

  @Mock private InternalKadaiEngine internalKadaiEngineMock;
  @Mock private KadaiEngine kadaiEngineMock;
  @Mock private SqlSession sqlSessionMock;
  @Mock private KadaiConfiguration kadaiConfigurationMock;
  private TaskHistoryQueryImpl historyQueryImpl;

  @BeforeEach
  void setup() {
    when(internalKadaiEngineMock.getEngine()).thenReturn(kadaiEngineMock);
    when(internalKadaiEngineMock.getEngine().getConfiguration()).thenReturn(kadaiConfigurationMock);
    historyQueryImpl = new TaskHistoryQueryImpl(internalKadaiEngineMock);
  }

  @Test
  void should_ReturnList_When_CallingListMethodOnTaskHistoryQuery() {
    TaskHistoryEvent historyEvent = new TaskHistoryEvent();
    historyEvent.setId(IdGenerator.generateWithPrefix(IdGenerator.ID_PREFIX_TASK_HISTORY_EVENT));
    historyEvent.setUserId("someUserId");
    historyEvent.setDetails("someDetails");
    historyEvent.setEventType(TaskHistoryEventType.CREATED.getName());
    historyEvent.setCreated(null);

    List<TaskHistoryEvent> returnList = List.of(historyEvent);
    TimeInterval interval = new TimeInterval(Instant.now().minusNanos(1000), Instant.now());

    doNothing().when(internalKadaiEngineMock).openConnection();
    doNothing().when(internalKadaiEngineMock).returnConnection();
    when(internalKadaiEngineMock.getSqlSession()).thenReturn(sqlSessionMock);
    when(sqlSessionMock.selectList(any(), any())).thenReturn(new ArrayList<>(returnList));

    List<TaskHistoryEvent> result =
        historyQueryImpl.userIdIn("someUserId").createdWithin(interval).list();

    validateMockitoUsage();
    assertThat(result).isEqualTo(returnList);
  }
}
