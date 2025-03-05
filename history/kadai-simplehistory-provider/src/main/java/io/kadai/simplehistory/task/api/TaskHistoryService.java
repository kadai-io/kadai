package io.kadai.simplehistory.task.api;

import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.spi.history.api.events.task.TaskHistoryEvent;
import io.kadai.spi.history.api.exceptions.TaskHistoryEventNotFoundException;
import java.util.List;

public interface TaskHistoryService {

  // region CREATE

  TaskHistoryEvent createTaskHistoryEvent(TaskHistoryEvent event);

  // endregion

  // region READ

  TaskHistoryEvent getTaskHistoryEvent(String eventId) throws TaskHistoryEventNotFoundException;

  // endregion

  // region DELETE

  void deleteTaskHistoryEventsByTaskIds(List<String> taskIds)
      throws NotAuthorizedException, InvalidArgumentException;

  // endregion

  TaskHistoryQuery createTaskHistoryQuery();
}
