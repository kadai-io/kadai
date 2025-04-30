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

package io.kadai.spi.history.api;

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.spi.history.api.events.classification.ClassificationHistoryEvent;
import io.kadai.spi.history.api.events.task.TaskHistoryEvent;
import io.kadai.spi.history.api.events.workbasket.WorkbasketHistoryEvent;
import java.util.List;

/** Interface for KADAI History Service Provider. */
public interface KadaiHistory {

  /**
   * Initialize KadaiHistory service.
   *
   * @param kadaiEngine {@linkplain KadaiEngine} The Kadai engine for needed initialization.
   */
  void initialize(KadaiEngine kadaiEngine);

  /**
   * Create a new {@linkplain TaskHistoryEvent}.
   *
   * @param event {@linkplain TaskHistoryEvent} The event to be created.
   */
  void create(TaskHistoryEvent event);

  /**
   * Create a new {@linkplain WorkbasketHistoryEvent}.
   *
   * @param event {@linkplain WorkbasketHistoryEvent} The event to be created.
   */
  void create(WorkbasketHistoryEvent event);

  /**
   * Create a new {@linkplain ClassificationHistoryEvent}.
   *
   * @param event {@linkplain ClassificationHistoryEvent} The event to be created.
   */
  void create(ClassificationHistoryEvent event);

  /**
   * Delete history events by taskIds. Invalid/non-existing taskIds will be ignored
   *
   * @param taskIds the task ids for which all history events must be deleted
   * @throws InvalidArgumentException If the list of taskIds is null
   * @throws NotAuthorizedException if the current user is not member of {@linkplain
   *     io.kadai.common.api.KadaiRole#ADMIN}
   */
  void deleteHistoryEventsByTaskIds(List<String> taskIds)
      throws InvalidArgumentException, NotAuthorizedException;
}
