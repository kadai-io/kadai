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
