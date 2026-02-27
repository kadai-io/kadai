/*
 * Copyright [2026] [envite consulting GmbH]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *
 */

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
