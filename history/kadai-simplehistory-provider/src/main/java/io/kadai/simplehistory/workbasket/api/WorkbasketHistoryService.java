package io.kadai.simplehistory.workbasket.api;

import io.kadai.spi.history.api.events.workbasket.WorkbasketHistoryEvent;
import io.kadai.spi.history.api.exceptions.WorkbasketHistoryEventNotFoundException;

public interface WorkbasketHistoryService {

  // region CREATE

  WorkbasketHistoryEvent createWorkbasketHistoryEvent(WorkbasketHistoryEvent event);

  // endregion

  // region READ

  WorkbasketHistoryEvent getWorkbasketHistoryEvent(String eventId)
      throws WorkbasketHistoryEventNotFoundException;

  // endregion

  WorkbasketHistoryQuery createWorkbasketHistoryQuery();
}
