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

package io.kadai.spi.history.internal;

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.internal.util.CheckedConsumer;
import io.kadai.common.internal.util.LogSanitizer;
import io.kadai.common.internal.util.SpiLoader;
import io.kadai.spi.history.api.KadaiHistory;
import io.kadai.spi.history.api.events.classification.ClassificationHistoryEvent;
import io.kadai.spi.history.api.events.task.TaskHistoryEvent;
import io.kadai.spi.history.api.events.workbasket.WorkbasketHistoryEvent;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Creates and deletes events and emits them to the registered history service providers. */
public final class HistoryEventManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(HistoryEventManager.class);
  private final List<KadaiHistory> kadaiHistories;

  public HistoryEventManager(KadaiEngine kadaiEngine) {
    kadaiHistories = SpiLoader.load(KadaiHistory.class);
    for (KadaiHistory history : kadaiHistories) {
      history.initialize(kadaiEngine);
      LOGGER.info("Registered history provider: {}", history.getClass().getName());
    }
    if (kadaiHistories.isEmpty()) {
      LOGGER.info("No KadaiHistory provider found. Running without History.");
    }
  }

  public boolean isEnabled() {
    return !kadaiHistories.isEmpty();
  }

  public void createEvent(TaskHistoryEvent event) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "Sending {} to history service providers: {}", event.getClass().getSimpleName(), event);
    }
    kadaiHistories.forEach(historyProvider -> historyProvider.create(event));
  }

  public void createEvent(WorkbasketHistoryEvent event) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "Sending {} to history service providers: {}",
          event.getClass().getSimpleName(),
          LogSanitizer.stripLineBreakingChars(event));
    }
    kadaiHistories.forEach(historyProvider -> historyProvider.create(event));
  }

  public void createEvent(ClassificationHistoryEvent event) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "Sending {} to history service providers: {}", event.getClass().getSimpleName(), event);
    }

    kadaiHistories.forEach(historyProvider -> historyProvider.create(event));
  }

  public void deleteEvents(List<String> taskIds) throws NotAuthorizedException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "Sending taskIds to history service providers: {}",
          taskIds.stream().map(LogSanitizer::stripLineBreakingChars).toList());
    }

    kadaiHistories.forEach(
        CheckedConsumer.rethrowing(
            historyProvider -> historyProvider.deleteHistoryEventsByTaskIds(taskIds)));
  }
}
