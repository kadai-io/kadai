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

package io.kadai.simplehistory.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.when;

import io.kadai.common.api.TimeInterval;
import io.kadai.common.internal.InternalKadaiEngine;
import io.kadai.common.internal.util.IdGenerator;
import io.kadai.spi.history.api.events.workbasket.WorkbasketHistoryEvent;
import io.kadai.spi.history.api.events.workbasket.WorkbasketHistoryEventType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit Test for WorkbasketHistoryQueryImplTest. */
@ExtendWith(MockitoExtension.class)
class WorkbasketHistoryQueryImplTest {

  @Mock private InternalKadaiEngine internalKadaiEngineMock;

  private WorkbasketHistoryQueryImpl historyQueryImpl;

  @Mock private SqlSession sqlSessionMock;

  @BeforeEach
  void setup() {
    historyQueryImpl = new WorkbasketHistoryQueryImpl(internalKadaiEngineMock);
  }

  @Test
  void should_ReturnList_When_CallingListMethodOnWorkbasketHistoryQuery() {
    WorkbasketHistoryEvent historyEvent = new WorkbasketHistoryEvent();
    historyEvent.setId(
        IdGenerator.generateWithPrefix(IdGenerator.ID_PREFIX_WORKBASKET_HISTORY_EVENT));
    historyEvent.setUserId("someUserId");
    historyEvent.setDetails("someDetails");
    historyEvent.setKey("abcd");
    historyEvent.setEventType(WorkbasketHistoryEventType.CREATED.getName());
    historyEvent.setCreated(null);

    List<WorkbasketHistoryEvent> returnList = List.of(historyEvent);
    TimeInterval interval = new TimeInterval(Instant.now().minusNanos(1000), Instant.now());

    doNothing().when(internalKadaiEngineMock).openConnection();
    doNothing().when(internalKadaiEngineMock).returnConnection();
    when(internalKadaiEngineMock.getSqlSession()).thenReturn(sqlSessionMock);
    when(sqlSessionMock.selectList(any(), any())).thenReturn(new ArrayList<>(returnList));

    List<WorkbasketHistoryEvent> result =
        historyQueryImpl
            .workbasketIdIn("WBI:01")
            .keyIn("abcd", "some_random_string")
            .userIdIn("someUserId")
            .createdWithin(interval)
            .list();

    validateMockitoUsage();
    assertThat(result).isEqualTo(returnList);
  }
}
