package io.kadai.spi.history.api;

import io.kadai.spi.history.api.events.KadaiEvent;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

public interface KadaiEventPublisher<T extends KadaiEvent> {

  void publish(T event);

  default void publishing(Supplier<T> supplyEvent) {
    publish(supplyEvent.get());
  }

  default void tryPublishing(Supplier<Optional<T>> trySupplyEvent) {
    trySupplyEvent.get().ifPresent(this::publish);
  }

  default void publishAll(Collection<T> events) {
    events.forEach(this::publish);
  }

  default void publishingAll(Supplier<Collection<T>> supplyEvents) {
    supplyEvents.get().forEach(this::publish);
  }
}
