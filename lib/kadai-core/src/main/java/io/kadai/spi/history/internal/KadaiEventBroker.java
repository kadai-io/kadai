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

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.internal.util.CollectionUtil;
import io.kadai.common.internal.util.SpiLoader;
import io.kadai.spi.history.api.KadaiEventConsumer;
import io.kadai.spi.history.api.events.KadaiEvent;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KadaiEventBroker {

  private static final Logger LOGGER = LoggerFactory.getLogger(KadaiEventBroker.class);
  private final Map<Class<? extends KadaiEvent>, List<KadaiEventConsumer<? extends KadaiEvent>>>
      consumers;
  private boolean enabled;

  @SuppressWarnings({
    "rawtypes", // loading generic SPIs is only possible raw
    "unchecked" // Safe cast by specification of 'Reifiable::reify'
  })
  public KadaiEventBroker(KadaiEngine kadaiEngine) {
    final List<KadaiEventConsumer> rawConsumers = SpiLoader.load(KadaiEventConsumer.class);
    this.enabled = !rawConsumers.isEmpty();
    rawConsumers.forEach(consumer -> consumer.initialize(kadaiEngine));
    this.consumers =
        rawConsumers.stream().collect(groupingBy(KadaiEventConsumer::reify)).entrySet().stream()
            .map(
                e ->
                    new SimpleEntry<
                        Class<? extends KadaiEvent>,
                        List<KadaiEventConsumer<? extends KadaiEvent>>>(
                        (Class<? extends KadaiEvent>) e.getKey(),
                        e.getValue().stream()
                            .map(c -> (KadaiEventConsumer<? extends KadaiEvent>) c)
                            .collect(toList())))
            .collect(toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
  }

  @SuppressWarnings("unchecked") // Safe cast by definition of 'Object::getClass'
  public <T extends KadaiEvent> void forward(T event) {
    getConsumers((Class<T>) event.getClass()).forEach(consumer -> consumer.consume(event));
  }

  public boolean isEnabled() {
    return enabled;
  }

  // TODO: Although not inherently expensive, one should consider caching this
  @SuppressWarnings({
    "unchecked", // Safe cast by construction of 'consumers'
    "ResultOfMethodCallIgnored"
  })
  public <T extends KadaiEvent> List<KadaiEventConsumer<? super T>> getConsumers(
      Class<T> mostSpecificClazz) {
    final List<KadaiEventConsumer<? super T>> eligible = new ArrayList<>();
    Class<? super T> clazz = mostSpecificClazz;
    do {
      consumers.getOrDefault(clazz, new ArrayList<>()).stream()
          .map(consumer -> (KadaiEventConsumer<? super T>) consumer)
          .forEach(eligible::add);

      clazz = clazz.getSuperclass();
    } while (clazz != KadaiEvent.class.getSuperclass());

    return eligible;
  }

  public <T extends KadaiEvent> void subscribes(KadaiEventConsumer<T> consumer) {
    consumers.merge(consumer.reify(), new ArrayList<>(List.of(consumer)), CollectionUtil::add);
    enabled = true;
  }

  public <T extends KadaiEvent> void unsubscribes(KadaiEventConsumer<T> consumer) {
    final Class<T> eventKey = consumer.reify();
    if (consumers.containsKey(eventKey)) {
      consumers.get(eventKey).remove(consumer);
      if (consumers.values().stream().allMatch(Collection::isEmpty)) {
        enabled = false;
      }
    }
  }
}
