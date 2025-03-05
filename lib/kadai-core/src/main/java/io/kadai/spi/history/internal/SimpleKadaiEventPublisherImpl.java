package io.kadai.spi.history.internal;

import io.kadai.spi.history.api.KadaiEventPublisher;
import io.kadai.spi.history.api.events.KadaiEvent;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

public class SimpleKadaiEventPublisherImpl<T extends KadaiEvent> implements KadaiEventPublisher<T> {

  private final HistoryEventManager historyEventManager;

  public SimpleKadaiEventPublisherImpl(HistoryEventManager historyEventManager) {
    this.historyEventManager = historyEventManager;
  }

  @Override
  public void publish(T event) {
    historyEventManager.forward(event);
  }

  @Override
  public void publishing(Supplier<T> supplyEvent) {
    if (historyEventManager.isEnabled()) {
      historyEventManager.forward(supplyEvent.get());
    }
  }

  @Override
  public void tryPublishing(Supplier<Optional<T>> trySupplyEvent) {
    if (historyEventManager.isEnabled()) {
      trySupplyEvent.get().ifPresent(this::publish);
    }
  }

  @Override
  public void publishingAll(Supplier<Collection<T>> supplyEvents) {
    if (historyEventManager.isEnabled()) {
      supplyEvents.get().forEach(this::publish);
    }
  }
}
