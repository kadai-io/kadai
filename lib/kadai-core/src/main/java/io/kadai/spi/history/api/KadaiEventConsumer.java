package io.kadai.spi.history.api;

import io.kadai.common.api.KadaiEngine;
import io.kadai.spi.history.api.events.KadaiEvent;

public interface KadaiEventConsumer<T extends KadaiEvent> {

  void consume(T event);

  void initialize(KadaiEngine consumer);
}
