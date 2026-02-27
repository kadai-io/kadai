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

package io.kadai.simplehistory.task.api;

import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.spi.history.api.events.task.TaskHistoryEvent;
import io.kadai.spi.history.api.exceptions.TaskHistoryEventNotFoundException;
import io.kadai.task.api.models.Task;
import java.util.Collections;
import java.util.List;

/**
 * The TaskHistoryService manages all operations on {@linkplain TaskHistoryEvent TaskHistoryEvents}.
 */
public interface TaskHistoryService {

  /**
   * Inserts a {@link TaskHistoryEvent} that doesn't exist in the database yet.
   *
   * @param event the event to insert
   * @return the inserted event
   */
  TaskHistoryEvent createTaskHistoryEvent(TaskHistoryEvent event);

  /**
   * Fetches a {@link TaskHistoryEvent} from the database by the specified {@linkplain
   * TaskHistoryEvent#getId() id}.
   *
   * @param eventId id of the event to fetch
   * @return the fetched event
   * @throws TaskHistoryEventNotFoundException if the event could not be found in the database
   */
  TaskHistoryEvent getTaskHistoryEvent(String eventId) throws TaskHistoryEventNotFoundException;

  /**
   * Deletes all {@linkplain TaskHistoryEvent TaskHistoryEvents} associated with any of the given
   * {@linkplain Task#getId() taskIds}.
   *
   * @param taskIds the ids of the tasks to delete all history events for
   * @throws NotAuthorizedException if the current user is not an {@linkplain
   *     io.kadai.common.api.KadaiRole#ADMIN admin}
   * @throws InvalidArgumentException if the given list of taskIds is null
   */
  void deleteTaskHistoryEventsByTaskIds(List<String> taskIds)
      throws NotAuthorizedException, InvalidArgumentException;

  /**
   * Deletes all {@linkplain TaskHistoryEvent TaskHistoryEvents} associated with the given
   * {@linkplain Task#getId() taskId}.
   *
   * @param taskId the id of the tasks to delete all history events for
   * @throws NotAuthorizedException if the current user is not an {@linkplain
   *     io.kadai.common.api.KadaiRole#ADMIN admin}
   */
  default void deleteTaskHistoryEventsByTaskId(String taskId) throws NotAuthorizedException {
    deleteTaskHistoryEventsByTaskIds(Collections.singletonList(taskId));
  }

  /**
   * Creates an empty {@link TaskHistoryQuery}.
   *
   * @return the created query
   */
  TaskHistoryQuery createTaskHistoryQuery();
}
