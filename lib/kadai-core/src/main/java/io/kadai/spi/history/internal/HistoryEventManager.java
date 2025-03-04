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

package io.kadai.spi.history.internal;

import io.kadai.spi.history.api.KadaiEventConsumer;
import io.kadai.spi.history.api.events.KadaiEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Creates and deletes events and emits them to the registered history service providers. */
public final class HistoryEventManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(HistoryEventManager.class);

  private final Map<Class<? extends KadaiEvent>, List<KadaiEventConsumer<? extends KadaiEvent>>>
      consumers = new HashMap<>();

  public void forward(KadaiEvent event) {}

  public boolean isEnabled() {
    return consumers.values().stream().allMatch(List::isEmpty);
  }

  @SuppressWarnings("unchecked") // Valid cast per construction of 'consumers'
  private <T extends KadaiEvent> List<KadaiEventConsumer<T>> getConsumers(Class<T> clazz) {
    return consumers
        .getOrDefault(clazz, new ArrayList<>())
        .stream()
        .map(consumer -> (KadaiEventConsumer<T>) consumer)
        .toList();
  }
}
