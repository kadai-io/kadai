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

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.spi.history.api.KadaiEventConsumer;
import io.kadai.spi.history.api.events.KadaiEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

public class KadaiEventLogger implements KadaiEventConsumer<KadaiEvent> {

  private static final Logger LOGGER = LoggerFactory.getLogger(KadaiEventLogger.class);
  private JsonMapper jsonMapper;
  private Logger historyLogger;

  @Override
  public void initialize(KadaiEngine kadaiEngine) {
    jsonMapper = new JsonMapper();

    String historyLoggerName = kadaiEngine.getConfiguration().getLogHistoryLoggerName();

    if (historyLoggerName != null) {
      historyLogger = LoggerFactory.getLogger(historyLoggerName);
    } else {
      historyLogger = LOGGER;
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "LogfileHistoryServiceProvider initialized with name: {} ", historyLogger.getName());
    }
  }

  @Override
  public void consume(KadaiEvent event) {
    try {
      if (historyLogger.isInfoEnabled()) {
        historyLogger.info(jsonMapper.writeValueAsString(event));
      }
    } catch (JacksonException e) {
      throw new SystemException("Caught exception while serializing history event to JSON ", e);
    }
  }

  @Override
  public Class<KadaiEvent> reify() {
    return KadaiEvent.class;
  }
}
