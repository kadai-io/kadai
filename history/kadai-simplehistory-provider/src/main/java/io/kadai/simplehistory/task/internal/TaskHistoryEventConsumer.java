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

package io.kadai.simplehistory.task.internal;

import static java.util.function.Predicate.not;

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.spi.history.api.BatchKadaiEventConsumer;
import io.kadai.spi.history.api.events.task.TaskDeletedEvent;
import io.kadai.spi.history.api.events.task.TaskHistoryEvent;
import java.util.Collection;
import java.util.List;

public class TaskHistoryEventConsumer implements BatchKadaiEventConsumer<TaskHistoryEvent> {

  private KadaiEngine kadaiEngine;
  private TaskHistoryServiceImpl taskHistoryService;

  @Override
  public void consume(TaskHistoryEvent event) {
    final boolean deletionEnabled =
        kadaiEngine.getConfiguration().isDeleteHistoryEventsOnTaskDeletionEnabled();
    if (event instanceof TaskDeletedEvent && deletionEnabled) {
      final String taskId = event.getTaskId();
      try {
        taskHistoryService.deleteTaskHistoryEventsByTaskId(taskId);
      } catch (NotAuthorizedException e) {
        final String msg =
            String.format(
                "Caught exception while trying to delete TaskHistoryEvents for task-event-id '%s'",
                taskId);
        throw new SystemException(msg, e);
      }
    } else {
      taskHistoryService.createTaskHistoryEvent(event);
    }
  }

  @Override
  public void consumeAll(Collection<TaskHistoryEvent> events) {
    final boolean deletionEnabled =
        kadaiEngine.getConfiguration().isDeleteHistoryEventsOnTaskDeletionEnabled();

    if (!deletionEnabled) {
      events.forEach(taskHistoryService::createTaskHistoryEvent);
      return;
    }

    List<String> taskIdsForHistoryDeletion =
        events.stream()
            .filter(TaskDeletedEvent.class::isInstance)
            .map(TaskHistoryEvent::getTaskId)
            .toList();
    if (!taskIdsForHistoryDeletion.isEmpty()) {
      try {
        taskHistoryService.deleteTaskHistoryEventsByTaskIds(taskIdsForHistoryDeletion);
      } catch (NotAuthorizedException e) {
        throw new SystemException(
            "Caught exception while trying to delete TaskHistoryEvents for deleted tasks", e);
      }
    }

    events.stream()
        .filter(not(TaskDeletedEvent.class::isInstance))
        .forEach(taskHistoryService::createTaskHistoryEvent);
  }

  @Override
  public Class<TaskHistoryEvent> reify() {
    return TaskHistoryEvent.class;
  }

  @Override
  public void initialize(KadaiEngine kadaiEngine) {
    this.kadaiEngine = kadaiEngine;
    this.taskHistoryService = new TaskHistoryServiceImpl();
    taskHistoryService.initialize(kadaiEngine);
  }
}
