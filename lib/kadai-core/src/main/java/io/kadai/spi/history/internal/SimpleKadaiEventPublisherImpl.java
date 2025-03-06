package io.kadai.spi.history.internal;

import io.kadai.spi.history.api.KadaiEventPublisher;
import io.kadai.spi.history.api.events.KadaiEvent;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

public class SimpleKadaiEventPublisherImpl<T extends KadaiEvent> implements KadaiEventPublisher<T> {

  private final KadaiEventBroker kadaiEventBroker;

  public SimpleKadaiEventPublisherImpl(KadaiEventBroker kadaiEventBroker) {
    this.kadaiEventBroker = kadaiEventBroker;
  }

  @Override
  public void publish(T event) {
    kadaiEventBroker.forward(event);
  }

  @Override
  public void publishing(Supplier<T> supplyEvent) {
    if (kadaiEventBroker.isEnabled()) {
      kadaiEventBroker.forward(supplyEvent.get());
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
