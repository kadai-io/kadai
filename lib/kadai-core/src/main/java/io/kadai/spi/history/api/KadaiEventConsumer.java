package io.kadai.spi.history.api;

import io.kadai.common.api.KadaiInitializable;
import io.kadai.spi.history.api.events.KadaiEvent;

public interface KadaiEventConsumer<T extends KadaiEvent> extends KadaiInitializable {

  void consume(T event);
}
