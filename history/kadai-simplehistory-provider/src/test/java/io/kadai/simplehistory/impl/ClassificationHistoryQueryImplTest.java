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

package io.kadai.simplehistory.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.when;

import io.kadai.common.internal.InternalKadaiEngine;
import io.kadai.common.internal.util.IdGenerator;
import io.kadai.spi.history.api.events.classification.ClassificationHistoryEvent;
import io.kadai.spi.history.api.events.classification.ClassificationHistoryEventType;
import java.util.ArrayList;
import java.util.List;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit Test for ClassificationQueryImplTest. */
@ExtendWith(MockitoExtension.class)
class ClassificationHistoryQueryImplTest {

  private ClassificationHistoryQueryImpl historyQueryImpl;

  @Mock private InternalKadaiEngine internalKadaiEngineMock;

  @Mock private SqlSession sqlSessionMock;

  @BeforeEach
  void setup() {
    historyQueryImpl = new ClassificationHistoryQueryImpl(internalKadaiEngineMock);
  }

  @Test
  void should_returnList_When_CallingListMethodOnTaskHistoryQuery() {
    ClassificationHistoryEvent historyEvent = new ClassificationHistoryEvent();
    historyEvent.setId(
        IdGenerator.generateWithPrefix(IdGenerator.ID_PREFIX_CLASSIFICATION_HISTORY_EVENT));
    historyEvent.setUserId("admin");
    historyEvent.setDetails("someDetails");
    historyEvent.setEventType(ClassificationHistoryEventType.CREATED.getName());

    List<ClassificationHistoryEvent> returnList = List.of(historyEvent);

    doNothing().when(internalKadaiEngineMock).openConnection();
    doNothing().when(internalKadaiEngineMock).returnConnection();
    when(internalKadaiEngineMock.getSqlSession()).thenReturn(sqlSessionMock);
    when(sqlSessionMock.selectList(any(), any())).thenReturn(new ArrayList<>(returnList));

    List<ClassificationHistoryEvent> result =
        historyQueryImpl
            .userIdIn("admin")
            .typeIn(ClassificationHistoryEventType.CREATED.getName())
            .list();

    validateMockitoUsage();
    assertThat(result).isEqualTo(returnList);
  }
}
