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

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.internal.util.SpiLoader;
import io.kadai.spi.history.api.KadaiEventConsumer;
import io.kadai.spi.history.api.events.KadaiEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HistoryEventManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(HistoryEventManager.class);

  private final boolean enabled;
  private final Map<Class<? extends KadaiEvent>, List<KadaiEventConsumer<? extends KadaiEvent>>>
      consumers;

  @SuppressWarnings("rawtypes")
  public HistoryEventManager(KadaiEngine kadaiEngine) {
    List<KadaiEventConsumer> loadedConsumers = SpiLoader.load(KadaiEventConsumer.class);
    this.enabled = !loadedConsumers.isEmpty();
    loadedConsumers.forEach(consumer -> consumer.initialize(kadaiEngine));

    // TODO: Construct map
    //  Achtung mit SimplePublisher - dort bspw. T = TaskHistoryEvent, obwohl Aufruf mit spezifischerem
    //  Dann finden wir hier mglw. nicht alle Consumer
    this.consumers = new HashMap<>();
  }

  @SuppressWarnings("unchecked") // Safe cast by definition of 'Object::getClass'
  public <T extends KadaiEvent> void forward(T event) {
    getConsumers((Class<T>) event.getClass()).forEach(consumer -> consumer.consume(event));
  }

  @SuppressWarnings("unchecked") // Safe cast by construction of 'consumers'
  private <T extends KadaiEvent> List<KadaiEventConsumer<T>> getConsumers(Class<T> clazz) {
    return consumers
        .getOrDefault(clazz, new ArrayList<>())
        .stream()
        .map(consumer -> (KadaiEventConsumer<T>) consumer)
        .toList();
  }

  public boolean isEnabled() {
    return enabled;
  }
}
