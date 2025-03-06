package io.kadai.spi.history.api;

import io.kadai.common.api.KadaiInitializable;
import io.kadai.common.internal.util.Reifiable;
import io.kadai.spi.history.api.events.KadaiEvent;

public interface KadaiEventConsumer<T extends KadaiEvent> extends KadaiInitializable, Reifiable<T> {

  void consume(T event);
}
