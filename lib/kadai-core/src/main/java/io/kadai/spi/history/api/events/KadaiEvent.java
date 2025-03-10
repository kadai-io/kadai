package io.kadai.spi.history.api.events;

import java.time.Instant;

/** Interface specifying events in Kadai. */
public interface KadaiEvent {

  /**
   * Returns an {@link Instant} representing when this event was created.
   *
   * @return the instant representing the point this event was created
   */
  Instant getCreated();

  /**
   * Sets the {@link Instant} when this event was created on the given one.
   *
   * @param created the instant to set this events' creation to
   */
  void setCreated(Instant created);

  /** Sets the {@link Instant} when this event was created to {@linkplain Instant#now() now}. */
  default void setCreatedNow() {
    setCreated(Instant.now());
  }
}
