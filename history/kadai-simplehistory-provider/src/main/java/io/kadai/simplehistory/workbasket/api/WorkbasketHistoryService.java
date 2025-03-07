package io.kadai.simplehistory.workbasket.api;

import io.kadai.spi.history.api.events.workbasket.WorkbasketHistoryEvent;
import io.kadai.spi.history.api.exceptions.WorkbasketHistoryEventNotFoundException;

/**
 * The WorkbasketHistoryService manages all operations on {@linkplain WorkbasketHistoryEvent
 * WorkbasketHistoryEvents}.
 */
public interface WorkbasketHistoryService {

  // region CREATE

  /**
   * Inserts a {@link WorkbasketHistoryEvent} that doesn't exist in the database yet.
   *
   * @param event the event to insert
   * @return the inserted event
   */
  WorkbasketHistoryEvent createWorkbasketHistoryEvent(WorkbasketHistoryEvent event);

  // endregion

  // region READ

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

  // endregion

  /**
   * Creates an empty {@link WorkbasketHistoryQuery}.
   *
   * @return the created query
   */
  WorkbasketHistoryQuery createWorkbasketHistoryQuery();
}
