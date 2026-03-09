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

package io.kadai.loghistory.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import io.kadai.KadaiConfiguration;
import io.kadai.common.api.KadaiEngine;
import io.kadai.spi.history.api.events.classification.ClassificationHistoryEvent;
import io.kadai.spi.history.api.events.classification.ClassificationHistoryEventType;
import io.kadai.spi.history.api.events.task.TaskHistoryEvent;
import io.kadai.spi.history.api.events.task.TaskHistoryEventType;
import io.kadai.spi.history.api.events.workbasket.WorkbasketHistoryEvent;
import io.kadai.spi.history.api.events.workbasket.WorkbasketHistoryEventType;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tools.jackson.databind.json.JsonMapper;

class KadaiEventLoggerTest {

  static KadaiEngine kadaiEngineMock;
  private final JsonMapper jsonMapper = new JsonMapper();
  private final KadaiEventLogger kadaiEventLogger =
      new KadaiEventLogger();
  private final TestLogger logger = TestLoggerFactory.getTestLogger("AUDIT");

  @AfterEach
  public void clearLoggers() {
    TestLoggerFactory.clear();
  }

  @BeforeAll
  static void setupJsonMapper() {
    KadaiConfiguration kadaiConfiguration = Mockito.mock(KadaiConfiguration.class);
    kadaiEngineMock = Mockito.mock(KadaiEngine.class);
    Mockito.when(kadaiEngineMock.getConfiguration()).thenReturn(kadaiConfiguration);
    Mockito.when(kadaiConfiguration.getLogHistoryLoggerName()).thenReturn("AUDIT");
  }

  @Test
  void should_LogTaskEventAsJson_When_CreateIsCalled() throws Exception {

    kadaiEventLogger.initialize(kadaiEngineMock);
    TaskHistoryEvent eventToBeLogged = new TaskHistoryEvent();
    eventToBeLogged.setId("someId");
    eventToBeLogged.setUserId("someUser");
    eventToBeLogged.setEventType(TaskHistoryEventType.CREATED.getName());
    eventToBeLogged.setDomain("DOMAIN_A");
    eventToBeLogged.setCreated(Instant.now());
    eventToBeLogged.setNewValue("someNewValue");
    eventToBeLogged.setOldValue("someOldValue");
    eventToBeLogged.setBusinessProcessId("someBusinessProcessId");
    eventToBeLogged.setWorkbasketKey("someWorkbasketKey");
    eventToBeLogged.setTaskClassificationKey("someTaskClassificationKey");
    eventToBeLogged.setTaskClassificationCategory("someTaskClassificationCategory");
    eventToBeLogged.setDetails("someDetails");

    kadaiEventLogger.consume(eventToBeLogged);

    String logMessage = logger.getLoggingEvents().get(0).getMessage();

    TaskHistoryEvent deserializedEventFromLogMessage =
        jsonMapper.readValue(logMessage, TaskHistoryEvent.class);

    assertThat(eventToBeLogged).isEqualTo(deserializedEventFromLogMessage);
  }

  @Test
  void should_LogWorkbasketEventAsJson_When_CreateIsCalled() throws Exception {
    kadaiEventLogger.initialize(kadaiEngineMock);
    WorkbasketHistoryEvent eventToBeLogged = new WorkbasketHistoryEvent();
    eventToBeLogged.setId("someId");
    eventToBeLogged.setUserId("someUser");
    eventToBeLogged.setEventType(WorkbasketHistoryEventType.CREATED.getName());
    eventToBeLogged.setDomain("DOMAIN_A");
    eventToBeLogged.setCreated(Instant.now());
    eventToBeLogged.setKey("someWorkbasketKey");
    eventToBeLogged.setDetails("someDetails");

    kadaiEventLogger.consume(eventToBeLogged);

    String logMessage = logger.getLoggingEvents().get(0).getMessage();

    WorkbasketHistoryEvent deserializedEventFromLogMessage =
        jsonMapper.readValue(logMessage, WorkbasketHistoryEvent.class);

    assertThat(eventToBeLogged).isEqualTo(deserializedEventFromLogMessage);
  }

  @Test
  void should_LogClassificationEventAsJson_When_CreateIsCalled() throws Exception {

    kadaiEventLogger.initialize(kadaiEngineMock);
    ClassificationHistoryEvent eventToBeLogged = new ClassificationHistoryEvent();
    eventToBeLogged.setId("someId");
    eventToBeLogged.setUserId("someUser");
    eventToBeLogged.setEventType(ClassificationHistoryEventType.CREATED.getName());
    eventToBeLogged.setDomain("DOMAIN_A");
    eventToBeLogged.setCreated(Instant.now());
    eventToBeLogged.setKey("someClassificationKey");
    eventToBeLogged.setDetails("someDetails");

    kadaiEventLogger.consume(eventToBeLogged);

    String logMessage = logger.getLoggingEvents().get(0).getMessage();

    ClassificationHistoryEvent deserializedEventFromLogMessage =
        jsonMapper.readValue(logMessage, ClassificationHistoryEvent.class);

    assertThat(eventToBeLogged).isEqualTo(deserializedEventFromLogMessage);
  }
}
