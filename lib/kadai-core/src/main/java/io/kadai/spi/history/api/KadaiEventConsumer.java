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
