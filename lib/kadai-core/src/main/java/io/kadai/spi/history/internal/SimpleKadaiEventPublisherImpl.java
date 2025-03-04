package io.kadai.spi.history.internal;

import io.kadai.spi.history.api.KadaiEventPublisher;
import io.kadai.spi.history.api.events.KadaiEvent;

public class SimpleKadaiEventPublisherImpl<T extends KadaiEvent> implements KadaiEventPublisher<T> {

  private final HistoryEventManager historyEventManager;

  public SimpleKadaiEventPublisherImpl(HistoryEventManager historyEventManager) {
    this.historyEventManager = historyEventManager;
  }

  @Override
  public void publish(T event) {
    historyEventManager.forward(event);
  }
}
