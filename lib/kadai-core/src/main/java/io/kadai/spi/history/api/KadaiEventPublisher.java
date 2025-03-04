package io.kadai.spi.history.api;

import io.kadai.spi.history.api.events.KadaiEvent;

public interface KadaiEventPublisher<T extends KadaiEvent> {

  void publish(T event);
}
