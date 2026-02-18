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

import io.kadai.spi.history.api.KadaiEventPublisher;
import io.kadai.spi.history.api.events.KadaiEvent;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleKadaiEventPublisherImpl<T extends KadaiEvent> implements KadaiEventPublisher<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleKadaiEventPublisherImpl.class);

  private final KadaiEventBroker kadaiEventBroker;

  public SimpleKadaiEventPublisherImpl(KadaiEventBroker kadaiEventBroker) {
    this.kadaiEventBroker = kadaiEventBroker;
  }

  @Override
  public void publish(T event) {
    kadaiEventBroker.forward(event);
    LOGGER.info("Published event {}", event);
  }

  @Override
  public void publishing(Supplier<T> supplyEvent) {
    if (kadaiEventBroker.isEnabled()) {
      publish(supplyEvent.get());
    }
  }

  @Override
  public void tryPublishing(Supplier<Optional<T>> trySupplyEvent) {
    if (kadaiEventBroker.isEnabled()) {
      trySupplyEvent.get().ifPresent(this::publish);
    }
  }

  @Override
  public void publishingAll(Supplier<Collection<T>> supplyEvents) {
    if (kadaiEventBroker.isEnabled()) {
      supplyEvents.get().forEach(this::publish);
    }
  }
}
