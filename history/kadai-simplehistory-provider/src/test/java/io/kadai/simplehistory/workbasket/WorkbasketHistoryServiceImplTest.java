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

package io.kadai.simplehistory.workbasket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import acceptance.AbstractAccTest;
import io.kadai.common.internal.InternalKadaiEngine;
import io.kadai.simplehistory.workbasket.internal.WorkbasketHistoryEventMapper;
import io.kadai.simplehistory.workbasket.internal.WorkbasketHistoryServiceImpl;
import io.kadai.spi.history.api.events.workbasket.WorkbasketHistoryEvent;
import io.kadai.spi.history.api.events.workbasket.WorkbasketHistoryEventType;
import java.util.ArrayList;
import java.util.List;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkbasketHistoryServiceImplTest {

  @InjectMocks @Spy private WorkbasketHistoryServiceImpl cutSpy;
  @Mock private WorkbasketHistoryEventMapper workbasketHistoryEventMapperMock;
  @Mock private InternalKadaiEngine internalKadaiEngine;
  @Mock private SqlSession sqlSessionMock;

  @Test
  void should_VerifyMethodInvocations_When_CreateWorkbasketHistoryEvent() {
    WorkbasketHistoryEvent expectedEvent =
        AbstractAccTest.createWorkbasketHistoryEvent(
            "wbKey1", WorkbasketHistoryEventType.CREATED.getName(), "someUserId", "someDetails");

    cutSpy.createWorkbasketHistoryEvent(expectedEvent);
    verify(workbasketHistoryEventMapperMock, times(1)).insert(expectedEvent);
    assertThat(expectedEvent.getCreated()).isNotNull();
  }

  @Test
  void should_VerifyMethodInvocations_When_QueryWorkbasketHistoryEvent() {
    List<WorkbasketHistoryEvent> returnList = new ArrayList<>();
    returnList.add(
        AbstractAccTest.createWorkbasketHistoryEvent(
            "wbKey1", WorkbasketHistoryEventType.CREATED.getName(), "someUserId", "someDetails"));
    when(sqlSessionMock.selectList(any(), any())).thenReturn(new ArrayList<>(returnList));
    when(internalKadaiEngine.getSqlSession()).thenReturn(sqlSessionMock);
    final List<WorkbasketHistoryEvent> result =
        cutSpy.createWorkbasketHistoryQuery().keyIn("wbKey1").list();

    verify(internalKadaiEngine, times(1)).openConnection();
    verify(internalKadaiEngine, times(1)).getSqlSession();
    verify(sqlSessionMock, times(1)).selectList(any(), any());
    verify(internalKadaiEngine, times(1)).returnConnection();
    assertThat(result).hasSize(returnList.size());
    assertThat(result.get(0).getKey()).isEqualTo(returnList.get(0).getKey());
  }
}
