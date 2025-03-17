package io.kadai.simplehistory.task.internal;

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.spi.history.api.KadaiEventConsumer;
import io.kadai.spi.history.api.events.task.TaskDeletedEvent;
import java.util.Collection;
import java.util.List;

/**
 * A {@link KadaiEventConsumer} that deletes all {@linkplain
 * io.kadai.spi.history.api.events.task.TaskHistoryEvent TaskHistoryEvents} associated with the
 * {@linkplain io.kadai.task.api.models.Task Task} related to the consumed {@link TaskDeletedEvent}.
 */
public class TaskDeletedHistoryEventConsumerOnCascadeDeleteEvents
    implements KadaiEventConsumer<TaskDeletedEvent> {

  private KadaiEngine kadaiEngine;
  private TaskHistoryServiceImpl taskHistoryService;

  @Override
  public void consume(TaskDeletedEvent event) {
    if (kadaiEngine.getConfiguration().isDeleteHistoryEventsOnTaskDeletionEnabled()) {
      final String taskEventId = event.getTaskId();
      try {
        taskHistoryService.deleteTaskHistoryEventsByTaskId(taskEventId);
      } catch (NotAuthorizedException e) {
        throw new SystemException(
            "Caught exception while trying to delete TaskHistoryEvents: " + taskEventId, e);
      }
    }
  }

  @Override
  public void consumeAll(Collection<TaskDeletedEvent> events) {
    if (kadaiEngine.getConfiguration().isDeleteHistoryEventsOnTaskDeletionEnabled()) {
      final List<String> taskEventIds = events.stream().map(TaskDeletedEvent::getTaskId).toList();
      try {
        taskHistoryService.deleteTaskHistoryEventsByTaskIds(taskEventIds);
      } catch (NotAuthorizedException e) {
        throw new SystemException(
            "Caught exception while trying to delete TaskHistoryEvents: " + taskEventIds, e);
      }
    }
  }

  @Override
  public void initialize(KadaiEngine kadaiEngine) {
    this.kadaiEngine = kadaiEngine;
    this.taskHistoryService = new TaskHistoryServiceImpl();
    taskHistoryService.initialize(kadaiEngine);
  }

  @Override
  public Class<TaskDeletedEvent> reify() {
    return TaskDeletedEvent.class;
  }
}
