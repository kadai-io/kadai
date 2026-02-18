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

import io.kadai.common.api.KadaiInitializable;
import io.kadai.common.api.Reifiable;
import io.kadai.spi.history.api.events.KadaiEvent;
import java.util.Collection;

/**
 * Interface specifying how to consume {@linkplain KadaiEvent KadaiEvents}.
 *
 * @param <T> the type of event this consumer consumes
 */
public interface KadaiEventConsumer<T extends KadaiEvent> extends KadaiInitializable, Reifiable<T> {

  /**
   * Consumes an event.
   *
   * @param event the event to consume
   */
  void consume(T event);

  /**
   * Consumes multiple events without guaranteeing order.
   *
   * @param events the events to consume
   */
  default void consumeAll(Collection<T> events) {
    events.forEach(this::consume);
  }
}
