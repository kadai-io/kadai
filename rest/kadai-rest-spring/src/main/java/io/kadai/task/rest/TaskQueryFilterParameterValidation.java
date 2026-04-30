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

package io.kadai.task.rest;

import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.rest.QueryParameterValidation;
import io.kadai.task.api.TaskQuery;

public class TaskQueryFilterParameterValidation
    implements QueryParameterValidation<TaskQuery, Void, TaskQueryFilterParameter> {

  @Override
  public void validate(TaskQueryFilterParameter query) throws InvalidArgumentException {
    validateCombination(
        query.getPlannedWithin(),
        query.getPlannedFrom(),
        query.getPlannedUntil(),
        "planned",
        "planned-from",
        "planned-until");
    validateCombination(
        query.getPlannedNotWithin(),
        query.getPlannedFromNot(),
        query.getPlannedUntilNot(),
        "planned-not-in",
        "planned-not-in-from",
        "planned-not-in-until");
    validateCombination(
        query.getReceivedWithin(),
        query.getReceivedFrom(),
        query.getReceivedUntil(),
        "received",
        "received-from",
        "received-until");
    validateCombination(
        query.getReceivedNotIn(),
        query.getReceivedFromNot(),
        query.getReceivedUntilNot(),
        "received-not-in",
        "received-not-in-from",
        "received-not-in-until");
    validateCombination(
        query.getDueWithin(),
        query.getDueFrom(),
        query.getDueUntil(),
        "due",
        "due-from",
        "due-until");
    validateCombination(
        query.getDueNotWithin(),
        query.getDueFromNot(),
        query.getDueUntilNot(),
        "due-not-in",
        "due-not-in-from",
        "due-not-in-until");
    validateCombination(
        query.getCreatedWithin(),
        query.getCreatedFrom(),
        query.getCreatedUntil(),
        "created",
        "created-from",
        "created-until");
    validateCombination(
        query.getCreatedNotWithin(),
        query.getCreatedFromNot(),
        query.getCreatedUntilNot(),
        "created-not-in",
        "created-not-in-from",
        "created-not-in-until");
    validateCombination(
        query.getCompletedWithin(),
        query.getCompletedFrom(),
        query.getCompletedUntil(),
        "completed",
        "completed-from",
        "completed-until");
    validateCombination(
        query.getCompletedNotWithin(),
        query.getCompletedFromNot(),
        query.getCompletedUntilNot(),
        "completed-not-in",
        "completed-not-in-from",
        "completed-not-in-until");
    validateCombination(
        query.getPriorityWithin(),
        query.getPriorityFrom(),
        query.getPriorityUntil(),
        "priority-within",
        "priority-from",
        "priority-until");
    validateCombination(
        query.getPriorityNotWithin(),
        query.getPriorityNotFrom(),
        query.getPriorityNotUntil(),
        "priority-not-within",
        "priority-not-from",
        "priority-not-until");

    validateDividableByTwo(query.getPriorityWithin(), "priority-within");
    validateDividableByTwo(query.getPriorityNotWithin(), "priority-not-within");

    if (query.getWildcardSearchFieldIn() == null ^ query.getWildcardSearchValue() == null) {
      throw new InvalidArgumentException(
          "The params 'wildcard-search-field' and 'wildcard-search-value' must be used together");
    }

    if (query.getWorkbasketKeyIn() != null && query.getDomain() == null) {
      throw new InvalidArgumentException(
          "'workbasket-key' can only be used together with 'domain'.");
    }

    if (query.getWorkbasketKeyNotIn() != null && query.getDomain() == null) {
      throw new InvalidArgumentException(
          "'workbasket-key-not' can only be used together with 'domain'.");
    }

    if (query.getWorkbasketKeyIn() == null
        && query.getWorkbasketKeyNotIn() == null
        && query.getDomain() != null) {
      throw new InvalidArgumentException(
          "'domain' can only be used together with 'workbasket-key' or 'workbasket-key-not'.");
    }

    validateDividableByTwo(query.getPlannedWithin(), "planned");
    validateDividableByTwo(query.getReceivedWithin(), "received");
    validateDividableByTwo(query.getDueWithin(), "due");
    validateDividableByTwo(query.getModifiedWithin(), "modified");
    validateDividableByTwo(query.getCreatedWithin(), "created");
    validateDividableByTwo(query.getCompletedWithin(), "completed");
    validateDividableByTwo(query.getClaimedWithin(), "claimed");
    validateDividableByTwo(query.getAttachmentReceivedWithin(), "attachmentReceived");
    validateDividableByTwo(query.getPlannedNotWithin(), "planned-not-in");
    validateDividableByTwo(query.getReceivedNotIn(), "received-not-in");
    validateDividableByTwo(query.getDueNotWithin(), "due-not-in");
    validateDividableByTwo(query.getModifiedNotWithin(), "modified-not-in");
    validateDividableByTwo(query.getCreatedNotWithin(), "created-not-in");
    validateDividableByTwo(query.getCompletedNotWithin(), "completed-not-in");
    validateDividableByTwo(query.getClaimedNotWithin(), "claimed-not-in");
    validateDividableByTwo(query.getAttachmentReceivedNotWithin(), "attachment-not-received");

    if (query.getWithoutAttachment() != null && !query.getWithoutAttachment()) {
      throw new InvalidArgumentException(
          "provided value of the property 'without-attachment' must be 'true'");
    }
  }

  private void validateCombination(
      Object within,
      Object from,
      Object until,
      String withinName,
      String fromName,
      String untilName)
      throws InvalidArgumentException {
    if (within != null && (from != null || until != null)) {
      throw new InvalidArgumentException(
          "It is prohibited to use the param '"
              + withinName
              + "' in combination with the params '"
              + fromName
              + "' and / or '"
              + untilName
              + "'");
    }
  }

  private void validateDividableByTwo(Object[] array, String paramName)
      throws InvalidArgumentException {
    if (array != null && array.length % 2 != 0) {
      throw new InvalidArgumentException(
          "provided length of the property '" + paramName + "' is not dividable by 2");
    }
  }
}
