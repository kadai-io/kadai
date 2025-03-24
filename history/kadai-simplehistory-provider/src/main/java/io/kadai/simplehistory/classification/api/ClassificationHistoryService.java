/*
 * Copyright [2025] [envite consulting GmbH]
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

package io.kadai.simplehistory.classification.api;

import io.kadai.spi.history.api.events.classification.ClassificationHistoryEvent;
import io.kadai.spi.history.api.exceptions.ClassificationHistoryEventNotFoundException;

/**
 * The ClassificationHistoryService manages all operations on {@linkplain ClassificationHistoryEvent
 * ClassificationHistoryEvents}.
 */
public interface ClassificationHistoryService {

  // region CREATE

  /**
   * Inserts a {@link ClassificationHistoryEvent} that doesn't exist in the database yet.
   *
   * @param event the event to insert
   * @return the inserted event
   */
  ClassificationHistoryEvent createClassificationHistoryEvent(ClassificationHistoryEvent event);

  // endregion

  // region READ

  /**
   * Fetches a {@link ClassificationHistoryEvent} from the database by the specified {@linkplain
   * ClassificationHistoryEvent#getId() id}.
   *
   * @param eventId id of the event to fetch
   * @return the fetched event
   * @throws ClassificationHistoryEventNotFoundException if the event could not be found in the
   *     database
   */
  ClassificationHistoryEvent getClassificationHistoryEvent(String eventId)
      throws ClassificationHistoryEventNotFoundException;

  // endregion

  /**
   * Creates an empty {@link ClassificationHistoryQuery}.
   *
   * @return the created query
   */
  ClassificationHistoryQuery createClassificationHistoryQuery();
}
