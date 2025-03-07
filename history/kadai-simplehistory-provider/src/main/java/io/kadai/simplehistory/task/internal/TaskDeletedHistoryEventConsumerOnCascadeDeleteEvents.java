package io.kadai.simplehistory.task.internal;

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.spi.history.api.KadaiEventConsumer;
import io.kadai.spi.history.api.events.task.TaskDeletedEvent;

public class TaskDeletedHistoryEventConsumerOnCascadeDeleteEvents
    implements KadaiEventConsumer<TaskDeletedEvent> {

  private KadaiEngine kadaiEngine;
  private TaskHistoryServiceImpl taskHistoryService;

  @Override
  public void consume(TaskDeletedEvent event) {
    if (kadaiEngine.getConfiguration().isDeleteHistoryEventsOnTaskDeletionEnabled()) {
      try {
        taskHistoryService.deleteTaskHistoryEventsByTaskId(event.getTaskId());
      } catch (NotAuthorizedException e) {
        throw new SystemException("Caught exception while trying to delete TaskHistoryEvents:", e);
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
