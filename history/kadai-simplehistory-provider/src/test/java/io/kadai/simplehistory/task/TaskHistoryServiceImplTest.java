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

package io.kadai.simplehistory.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import acceptance.AbstractAccTest;
import io.kadai.KadaiConfiguration;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.internal.InternalKadaiEngine;
import io.kadai.simplehistory.task.internal.TaskHistoryEventMapper;
import io.kadai.simplehistory.task.internal.TaskHistoryServiceImpl;
import io.kadai.spi.history.api.events.task.TaskHistoryEvent;
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
class TaskHistoryServiceImplTest {

  @InjectMocks @Spy private TaskHistoryServiceImpl cutSpy;
  @Mock private TaskHistoryEventMapper taskHistoryEventMapperMock;
  @Mock private KadaiConfiguration kadaiConfiguration;
  @Mock private KadaiEngine kadaiEngine;
  @Mock private InternalKadaiEngine internalKadaiEngine;
  @Mock private SqlSession sqlSessionMock;

  @Test
  void should_VerifyMethodInvocations_When_CreateTaskHistoryEvent() {
    TaskHistoryEvent expectedWb =
        AbstractAccTest.createTaskHistoryEvent(
            "wbKey1", "taskId1", "type1", "wbKey2", "someUserId", "someDetails");

    cutSpy.createTaskHistoryEvent(expectedWb);
    verify(taskHistoryEventMapperMock, times(1)).insert(expectedWb);
    assertThat(expectedWb.getCreated()).isNotNull();
  }

  @Test
  void should_VerifyMethodInvocations_When_QueryTaskHistoryEvent() {
    List<TaskHistoryEvent> returnList = new ArrayList<>();
    returnList.add(
        AbstractAccTest.createTaskHistoryEvent(
            "wbKey1", "taskId1", "type1", "wbKey2", "someUserId", "someDetails"));

    when(kadaiConfiguration.isAddAdditionalUserInfo()).thenReturn(false);

    when(internalKadaiEngine.getSqlSession()).thenReturn(sqlSessionMock);
    when(sqlSessionMock.selectList(any(), any())).thenReturn(new ArrayList<>(returnList));

    when(internalKadaiEngine.getEngine()).thenReturn(kadaiEngine);
    when(kadaiEngine.getConfiguration()).thenReturn(kadaiConfiguration);
    final List<TaskHistoryEvent> result =
        cutSpy.createTaskHistoryQuery().taskIdIn("taskId1").list();

    verify(internalKadaiEngine, times(1)).openConnection();
    verify(internalKadaiEngine, times(1)).getSqlSession();
    verify(sqlSessionMock, times(1)).selectList(any(), any());

    verify(internalKadaiEngine, times(1)).returnConnection();
    assertThat(result).hasSize(returnList.size());
    assertThat(result.get(0).getWorkbasketKey()).isEqualTo(returnList.get(0).getWorkbasketKey());
  }
}
