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

  // As per construction this is a dependent map:
  // forall (k: Class<T>, v : List<KadaiEventConsumer<T'>>) in consumers: T == T'
  private final Map<Class<? extends KadaiEvent>, List<KadaiEventConsumer<? extends KadaiEvent>>>
      consumers;
  private final KadaiEngine kadaiEngine;
  private boolean enabled;

  @SuppressWarnings({
    "rawtypes", // loading generic SPIs is only possible raw
    "unchecked" // Safe cast by specification of 'Reifiable::reify'
  })
  public KadaiEventBroker(KadaiEngine kadaiEngine) {
    this.kadaiEngine = kadaiEngine;
    final List<KadaiEventConsumer> rawConsumers = SpiLoader.load(KadaiEventConsumer.class);
    this.enabled = !rawConsumers.isEmpty();
    rawConsumers.forEach(
        consumer -> {
          consumer.initialize(kadaiEngine);
          LOGGER.info("Registered provided KadaiEvent-Consumer: {}", consumer.getClass().getName());
        });
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
    if (consumers.isEmpty()) {
      LOGGER.info("No provided KadaiEvent-Consumers found. Running without History.");
    }
  }

  /**
   * Forwards an {@linkplain KadaiEvent event} to all {@linkplain #getConsumers(Class) eligible}
   * {@linkplain KadaiEventConsumer consumers}.
   *
   * @param <T> the type of the event to forward
   * @param event the event to forward
   */
  @SuppressWarnings("unchecked") // Safe cast by definition of 'Object::getClass'
  public <T extends KadaiEvent> void forward(T event) {
    getConsumers((Class<T>) event.getClass())
        .forEach(
            consumer -> {
              try {
                consumer.consume(event);
                LOGGER.info(
                    "Forwarded event '{}' to consumer '{}'", event, consumer.getClass().getName());
              } catch (RuntimeException e) {
                LOGGER.error("Consumer '{}' failed", consumer.getClass().getName(), e);
              }
            });
  }

  /**
   * Returns true if at least one {@linkplain KadaiEventConsumer consumer} is registered.
   *
   * @return true if at least one consumer is registered, false otherwise
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Returns all registered {@linkplain KadaiEventConsumer consumers} that can consume {@linkplain
   * KadaiEvent events} for the given class.
   *
   * @param mostSpecificClazz the class of the event of the most specific consumers to return
   * @param <T> the type of event of the most specific consumers to return
   * @return the list of all consumers who can consume events of the given class
   */
  @SuppressWarnings({
    "unchecked", // Safe cast by construction of 'consumers'
  })
  public <T extends KadaiEvent> List<KadaiEventConsumer<? super T>> getConsumers(
      Class<T> mostSpecificClazz) {
    final List<KadaiEventConsumer<? super T>> eligible = new ArrayList<>();
    Class<? super T> clazz = mostSpecificClazz;
    do {
      consumers.getOrDefault(clazz, new ArrayList<>()).stream()
          .map(consumer -> (KadaiEventConsumer<? super T>) consumer)
          .forEachOrdered(eligible::add);

      clazz = clazz.getSuperclass();
    } while (clazz != KadaiEvent.class.getSuperclass());

    return eligible;
  }

  /**
   * Registers the given {@linkplain KadaiEventConsumer consumer} and {@linkplain
   * KadaiEventConsumer#initialize(KadaiEngine) initializes} it.
   *
   * @param <T> the type of event the given consumer subscribes to
   * @param consumer the consumer to register and initialize
   */
  public <T extends KadaiEvent> void subscribes(KadaiEventConsumer<T> consumer) {
    consumers.merge(consumer.reify(), new ArrayList<>(List.of(consumer)), CollectionUtil::append);
    LOGGER.info("Subscribed KadaiEvent-Consumer: {}", consumer.getClass().getName());
    consumer.initialize(kadaiEngine);
    enabled = true;
  }

  /**
   * Removes the given {@linkplain KadaiEventConsumer consumer}.
   *
   * @param <T> the type of event the given consumer will have had subscribed to
   * @param consumer the consumer to remove
   */
  public <T extends KadaiEvent> void unsubscribes(KadaiEventConsumer<T> consumer) {
    final Class<T> eventKey = consumer.reify();
    if (consumers.containsKey(eventKey)) {
      consumers.get(eventKey).remove(consumer);
      LOGGER.info("Unsubscribed KadaiEvent-Consumer: {}", consumer.getClass().getName());
      if (consumers.values().stream().allMatch(Collection::isEmpty)) {
        enabled = false;
      }
    }
  }
}
