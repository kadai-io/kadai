package io.kadai.spi.history.api.events;

import java.time.Instant;

public interface KadaiEvent {

  Instant getCreated();

  void setCreated(Instant created);

  default void setCreatedNow() {
    setCreated(Instant.now());
  }
}
