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

/**
 * Interface for {@linkplain KadaiEventConsumer event consumers} that can process multiple events in
 * one batch.
 *
 * @param <T> the type of event this consumer consumes
 */
public interface BatchKadaiEventConsumer<T extends KadaiEvent> extends KadaiEventConsumer<T> {

  /**
   * Consumes multiple events in one batch without guaranteeing order.
   *
   * @param events the events to consume
   */
  void consumeAll(Collection<T> events);
}
