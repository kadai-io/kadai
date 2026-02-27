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

package io.kadai.simplehistory.workbasket.api;

import io.kadai.spi.history.api.events.workbasket.WorkbasketHistoryEvent;
import io.kadai.spi.history.api.exceptions.WorkbasketHistoryEventNotFoundException;

/**
 * The WorkbasketHistoryService manages all operations on {@linkplain WorkbasketHistoryEvent
 * WorkbasketHistoryEvents}.
 */
public interface WorkbasketHistoryService {

  /**
   * Inserts a {@link WorkbasketHistoryEvent} that doesn't exist in the database yet.
   *
   * @param event the event to insert
   * @return the inserted event
   */
  WorkbasketHistoryEvent createWorkbasketHistoryEvent(WorkbasketHistoryEvent event);

  /**
   * Fetches a {@link WorkbasketHistoryEvent} from the database by the specified {@linkplain
   * WorkbasketHistoryEvent#getId() id}.
   *
   * @param eventId id of the event to fetch
   * @return the fetched event
   * @throws WorkbasketHistoryEventNotFoundException if the event could not be found in the database
   */
  WorkbasketHistoryEvent getWorkbasketHistoryEvent(String eventId)
      throws WorkbasketHistoryEventNotFoundException;

  /**
   * Creates an empty {@link WorkbasketHistoryQuery}.
   *
   * @return the created query
   */
  WorkbasketHistoryQuery createWorkbasketHistoryQuery();
}
