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

package io.kadai.spi.history.api;

import io.kadai.spi.history.api.events.KadaiEvent;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Interface specifying a mediator publishing {@linkplain KadaiEvent KadaiEvents}.
 *
 * @param <T> the type of event this publisher publishes
 */
public interface KadaiEventPublisher<T extends KadaiEvent> {

  /**
   * Publishes an event.
   *
   * @param event the event to publish
   */
  void publish(T event);

  /**
   * Publishes an event, given as {@link Supplier}.
   *
   * @param supplyEvent the supplier supplying the event to publish
   */
  default void publishing(Supplier<T> supplyEvent) {
    publish(supplyEvent.get());
  }

  /**
   * Publishes an event if the given {@link Supplier} can supply one.
   *
   * @param trySupplyEvent the supplier supplying the event to publish
   */
  default void tryPublishing(Supplier<Optional<T>> trySupplyEvent) {
    trySupplyEvent.get().ifPresent(this::publish);
  }

  /**
   * Publishes multiple events without guaranteeing order.
   *
   * @param events the events to publish
   */
  default void publishAll(Collection<T> events) {
    events.forEach(this::publish);
  }

  /**
   * Publishes multiple events given as {@link Supplier}.
   *
   * <p>Does not provide any guarantee on order.
   *
   * @param supplyEvents the supplier supplying the events to publish
   */
  default void publishingAll(Supplier<Collection<T>> supplyEvents) {
    supplyEvents.get().forEach(this::publish);
  }
}
