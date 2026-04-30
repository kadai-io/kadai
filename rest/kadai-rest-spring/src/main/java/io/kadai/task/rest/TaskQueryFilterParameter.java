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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kadai.common.api.IntInterval;
import io.kadai.common.api.KeyDomain;
import io.kadai.common.api.TimeInterval;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.internal.util.LogSanitizer;
import io.kadai.common.rest.QueryParameter;
import io.kadai.task.api.CallbackState;
import io.kadai.task.api.TaskQuery;
import io.kadai.task.api.TaskState;
import io.kadai.task.api.WildcardSearchField;
import io.kadai.task.api.models.ObjectReference;
import io.swagger.v3.oas.annotations.Parameter;
import java.beans.ConstructorProperties;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import org.apache.commons.lang3.ArrayUtils;

public class TaskQueryFilterParameter implements QueryParameter<TaskQuery, Void> {

  private static final TaskQueryFilterParameterValidation VALIDATOR =
      new TaskQueryFilterParameterValidation();

  // region id
  @Parameter(name = "task-id", description = "Filter by task id. This is an exact match.")
  @JsonProperty("task-id")
  private final String[] taskIdIn;

  @Parameter(
      name = "task-id-not",
      description = "Filter by what the task id shouldn't be. This is an exact match.")
  @JsonProperty("task-id-not")
  private final String[] taskIdNotIn;

  // endregion
  // region externalId
  @Parameter(
      name = "external-id",
      description = "Filter by the external id of the Task. This is an exact match.")
  @JsonProperty("external-id")
  private final String[] externalIdIn;

  @Parameter(
      name = "external-id-not",
      description =
          "Filter by what the external id of the Task shouldn't be. This is an exact match.")
  @JsonProperty("external-id-not")
  private final String[] externalIdNotIn;

  // endregion
  // region received
  @Parameter(
      name = "received",
      description =
          "Filter by a time interval within which the Task was received. To create an open "
              + "interval you can just leave it blank.<p>The format is ISO-8601.<p>This "
              + "parameter can't be used together with 'received-from' or 'received-until'.")
  @JsonProperty("received")
  private final Instant[] receivedWithin;

  @Parameter(
      name = "receivedFrom",
      description =
          "Filter since a given received timestamp.<p>The format is ISO-8601."
              + "<p>This parameter can't be used together with 'received'.")
  @JsonProperty("received-from")
  private final Instant receivedFrom;

  @Parameter(
      name = "received-until",
      description =
          "Filter until a given received timestamp.<p>The format is ISO-8601."
              + "<p>This parameter can't be used together with 'received'.")
  @JsonProperty("received-until")
  private final Instant receivedUntil;

  @Parameter(
      name = "received-not",
      description =
          "Filter by a time interval within which the Task wasn't received. To "
              + "create an open interval you can just leave it blank.<p>The format is ISO-8601."
              + "<p>This parameter can't be used together with 'received-not-in-from' or "
              + "'received-not-in-until'.")
  @JsonProperty("received-not")
  private final Instant[] receivedNotIn;

  @Parameter(
      name = "received-from-not",
      description =
          "Filter since a given timestamp where it wasn't received.<p>The format is "
              + "ISO-8601.<p>This parameter can't be used together with 'received-not-in'.")
  @JsonProperty("received-from-not")
  private final Instant receivedFromNot;

  @Parameter(
      name = "received-until-not",
      description =
          "Filter until a given timestamp where it wasn't received.<p>The format is "
              + "ISO-8601.<p>This parameter can't be used together with 'received-not-in'.")
  @JsonProperty("received-until-not")
  private final Instant receivedUntilNot;

  // endregion
  // region created
  @Parameter(
      name = "created",
      description =
          "Filter by a time interval within which the Task was created. To create an open "
              + "interval you can just leave it blank.<p>The format is ISO-8601.<p>This "
              + "parameter can't be used together with 'created-from' or 'created-until'.")
  @JsonProperty("created")
  private final Instant[] createdWithin;

  @Parameter(
      name = "created-from",
      description =
          "Filter since a given created timestamp.<p>The format is ISO-8601.<p>This parameter "
              + "can't be used together with 'created'.")
  @JsonProperty("created-from")
  private final Instant createdFrom;

  @Parameter(
      name = "created-until",
      description =
          "Filter until a given created timestamp.<p>The format is ISO-8601.<p>This parameter "
              + "can't be used together with 'created'.")
  @JsonProperty("created-until")
  private final Instant createdUntil;

  @Parameter(
      name = "created-not",
      description =
          "Filter by a time interval within which the Task wasn't created. To create an open "
              + "interval you can just leave it blank.<p>The format is ISO-8601.<p>This "
              + "parameter can't be used together with 'created-not-in-from' or "
              + "'created-not-in-until'.")
  @JsonProperty("created-not")
  private final Instant[] createdNotWithin;

  @Parameter(
      name = "created-from-not",
      description =
          "Filter not since a given timestamp where it wasn't created.<p>The format is ISO-8601."
              + "<p>This parameter can't be used together with 'created-not-in'.")
  @JsonProperty("created-from-not")
  private final Instant createdFromNot;

  @Parameter(
      name = "created-until-not",
      description =
          "Filter not until a given timestamp where it wasn't created.<p>The format is ISO-8601."
              + "<p>This parameter can't be used together with 'created-not-in'.")
  @JsonProperty("created-until-not")
  private final Instant createdUntilNot;

  // endregion
  // region claimed
  @Parameter(
      name = "claimed",
      description =
          "Filter by a time interval within which the Task was claimed. To create an open "
              + "interval you can just leave it blank.<p>The format is ISO-8601.")
  @JsonProperty("claimed")
  private final Instant[] claimedWithin;

  @Parameter(
      name = "claimed-not",
      description =
          "Filter by a time interval within which the Task wasn't claimed. To create an open "
              + "interval you can just leave it blank.<p>The format is ISO-8601.")
  @JsonProperty("claimed-not")
  private final Instant[] claimedNotWithin;

  // endregion
  // region modified
  @Parameter(
      name = "modified",
      description =
          "Filter by a time interval within which the Task was modified. To create an open "
              + "interval you can just leave it blank.<p>The format is ISO-8601.")
  @JsonProperty("modified")
  private final Instant[] modifiedWithin;

  @Parameter(
      name = "modified-not",
      description =
          "Filter by a time interval within which the Task wasn't modified. To create an open "
              + "interval you can just leave it blank.<p>The format is ISO-8601.")
  @JsonProperty("modified-not")
  private final Instant[] modifiedNotWithin;

  // endregion
  // region planned
  @Parameter(
      name = "planned",
      description =
          "Filter by a time interval within which the Task was planned. To create an open "
              + "interval you can just leave it blank.<p>The format is ISO-8601.<p>This "
              + "parameter can't be used together with 'planned-from' or 'planned-until'.")
  @JsonProperty("planned")
  private final Instant[] plannedWithin;

  @Parameter(
      name = "planned-from",
      description =
          "Filter since a given planned timestamp.<p>The format is ISO-8601.<p>This parameter "
              + "can't be used together with 'planned'.")
  @JsonProperty("planned-from")
  private final Instant plannedFrom;

  @Parameter(
      name = "planned-until",
      description =
          "Filter until a given planned timestamp.<p>The format is ISO-8601.<p>This parameter "
              + "can't be used together with 'planned'.")
  @JsonProperty("planned-until")
  private final Instant plannedUntil;

  @Parameter(
      name = "planned-not",
      description =
          "Filter by a time interval within which the Task was planned. To create an open "
              + "interval you can just leave it blank.<p>The format is ISO-8601.<p>This "
              + "parameter can't be used together with 'planned-not-in-from' or "
              + "'planned-not-in-until'.")
  @JsonProperty("planned-not")
  private final Instant[] plannedNotWithin;

  @Parameter(
      name = "planned-from-not",
      description =
          "Filter since a given timestamp where it wasn't planned.<p>The format is ISO-8601."
              + "<p>This parameter can't be used together with 'planned-not-in'.")
  @JsonProperty("planned-from-not")
  private final Instant plannedFromNot;

  @Parameter(
      name = "planned-until-not",
      description =
          "Filter until a given timestamp where it wasn't planned.<p>The format is ISO-8601."
              + "<p>This parameter can't be used together with 'planned-not-in'.")
  @JsonProperty("planned-until-not")
  private final Instant plannedUntilNot;

  // endregion
  // region due
  @Parameter(
      name = "due",
      description =
          "Filter by a time interval within which the Task was due. To create an open interval "
              + "you can just leave it blank.<p>The format is ISO-8601.<p>This parameter can't be"
              + " used together with 'due-from' or 'due-until'.")
  @JsonProperty("due")
  private final Instant[] dueWithin;

  @Parameter(
      name = "due-from",
      description =
          "Filter since a given due timestamp.<p>The format is ISO-8601.<p>This parameter can't "
              + "be used together with 'due'.")
  @JsonProperty("due-from")
  private final Instant dueFrom;

  @Parameter(
      name = "due-until",
      description =
          "Filter until a given due timestamp.<p>The format is ISO-8601.<p>This parameter can't "
              + "be used together with 'due'.")
  @JsonProperty("due-until")
  private final Instant dueUntil;

  @Parameter(
      name = "due-not",
      description =
          "Filter by a time interval within which the Task wasn't due. To create an open interval"
              + " you can just leave it blank.<p>The format is ISO-8601.<p>This parameter can't be"
              + " used together with 'due-not-in-from' or 'due-not-in-until'.")
  @JsonProperty("due-not")
  private final Instant[] dueNotWithin;

  @Parameter(
      name = "due-from-not",
      description =
          "Filter since a given timestamp where it isn't due.<p>The format is ISO-8601.<p>This "
              + "parameter can't be used together with 'due-not-in'.")
  @JsonProperty("due-from-not")
  private final Instant dueFromNot;

  @Parameter(
      name = "due-until-not",
      description =
          "Filter until a given timestamp where it isn't due.<p>The format is ISO-8601.<p>This "
              + "parameter can't be used together with 'due-not-in'.")
  @JsonProperty("due-until-not")
  private final Instant dueUntilNot;

  // endregion
  // region completed
  @Parameter(
      name = "completed",
      description =
          "Filter by a time interval within which the Task was completed. To create an open "
              + "interval you can just leave it blank.<p>The format is ISO-8601.<p>This parameter"
              + " can't be used together with 'completed-from' or 'completed-until'.")
  @JsonProperty("completed")
  private final Instant[] completedWithin;

  @Parameter(
      name = "completed-from",
      description =
          "Filter since a given completed timestamp.<p>The format is ISO-8601.<p>This parameter "
              + "can't be used together with 'completed'.")
  @JsonProperty("completed-from")
  private final Instant completedFrom;

  @Parameter(
      name = "completed-until",
      description =
          "Filter until a given completed timestamp.<p>The format is ISO-8601.<p>This parameter "
              + "can't be used together with 'completed'.")
  @JsonProperty("completed-until")
  private final Instant completedUntil;

  @Parameter(
      name = "completed-not",
      description =
          "Filter by a time interval within which the Task wasn't completed. To create an open "
              + "interval you can just leave it blank.<p>The format is ISO-8601.<p>This parameter "
              + "can't be used together with 'completed-not-in-from' or 'completed-not-in-until'.")
  @JsonProperty("completed-not")
  private final Instant[] completedNotWithin;

  @Parameter(
      name = "completed-from-not",
      description =
          "Filter since a given timestamp where it wasn't completed. <p>The format is ISO-8601. "
              + "<p>This parameter can't be used together with 'completed-not-in'.")
  @JsonProperty("completed-from-not")
  private final Instant completedFromNot;

  @Parameter(
      name = "completed-until-not",
      description =
          "Filter until a given timestamp where it wasn't completed. <p>The format is ISO-8601. "
              + "<p>This parameter can't be used together with 'completed-not-in'.")
  @JsonProperty("completed-until-not")
  private final Instant completedUntilNot;

  // endregion
  // region name
  @Parameter(name = "name", description = "Filter by the name of the Task. This is an exact match.")
  @JsonProperty("name")
  private final String[] nameIn;

  @Parameter(
      name = "name-not",
      description = "Filter by what the name of the Task shouldn't be. This is an exact match.")
  @JsonProperty("name-not")
  private final String[] nameNotIn;

  @Parameter(
      name = "name-like",
      description =
          "Filter by the name of the Task. This results in a substring search (% is appended to "
              + "the front and end of the requested value). Further SQL \"LIKE\" wildcard "
              + "characters will be resolved correctly.")
  @JsonProperty("name-like")
  private final String[] nameLike;

  @Parameter(
      name = "name-not-like",
      description =
          "Filter by what the name of the Task shouldn't be. This results in a substring search "
              + "(% is appended to the front and end of the requested value). Further SQL "
              + "\"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("name-not-like")
  private final String[] nameNotLike;

  // endregion
  // region creator
  @Parameter(
      name = "creator",
      description = "Filter by creator of the Task. This is an exact match.")
  @JsonProperty("creator")
  private final String[] creatorIn;

  @Parameter(
      name = "creator-not",
      description = "Filter by what the creator of the Task shouldn't be. This is an exact match.")
  @JsonProperty("creator-not")
  private final String[] creatorNotIn;

  @Parameter(
      name = "creator-like",
      description =
          "Filter by the creator of the Task. This results in a substring search (% is appended to"
              + " the front and end of the requested value). Further SQL \"LIKE\" wildcard "
              + "characters will be resolved correctly.")
  @JsonProperty("creator-like")
  private final String[] creatorLike;

  @Parameter(
      name = "creator-not-like",
      description =
          "Filter by what the creator of the Task shouldn't be. This results in a substring "
              + "search (% is appended to the front and end of the requested value). Further SQL "
              + "\"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("creator-not-like")
  private final String[] creatorNotLike;

  // endregion
  // region note
  @Parameter(
      name = "note-like",
      description =
          "Filter by the note of the Task. This results in a substring search (% is appended to"
              + " the front and end of the requested value). Further SQL \"LIKE\" wildcard "
              + "characters will be resolved correctly.")
  @JsonProperty("note-like")
  private final String[] noteLike;

  @Parameter(
      name = "note-not-like",
      description =
          "Filter by what the note of the Task shouldn't be. This results in a substring search "
              + "(% is appended to the front and end of the requested value). Further SQL \"LIKE\""
              + " wildcard characters will be resolved correctly.")
  @JsonProperty("note-not-like")
  private final String[] noteNotLike;

  // endregion
  // region description
  @Parameter(
      name = "description-like",
      description =
          "Filter by the description of the Task. This results in a substring search (% is "
              + "appended to the front and end of the requested value). Further SQL \"LIKE\" "
              + "wildcard characters will be resolved correctly.")
  @JsonProperty("description-like")
  private final String[] descriptionLike;

  @Parameter(
      name = "description-not-like",
      description =
          "Filter by what the description of the Task shouldn't be. This results in a substring "
              + "search (% is appended to the front and end of the requested value). Further SQL "
              + "\"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("description-not-like")
  private final String[] descriptionNotLike;

  // endregion
  // region priority
  @Parameter(
      name = "priority",
      description = "Filter by the priority of the Task. This is an exact match.")
  @JsonProperty("priority")
  private final int[] priorityIn;

  @Parameter(
      name = "priority-not",
      description = "Filter by what the priority of the Task shouldn't be. This is an exact match.")
  @JsonProperty("priority-not")
  private final int[] priorityNotIn;

  @Parameter(
      name = "priority-within",
      description = "Filter by the range of values of the priority field of the Task.")
  @JsonProperty("priority-within")
  private final Integer[] priorityWithin;

  @Parameter(
      name = "priority-from",
      description = "Filter by priority starting from the given value (inclusive).")
  @JsonProperty("priority-from")
  private final Integer priorityFrom;

  @Parameter(
      name = "priority-until",
      description = "Filter by priority up to the given value (inclusive).")
  @JsonProperty("priority-until")
  private final Integer priorityUntil;

  @Parameter(
      name = "priority-not-within",
      description = "Filter by exclusing the range of values of the priority field of the Task.")
  @JsonProperty("priority-not-within")
  private final Integer[] priorityNotWithin;

  @Parameter(
      name = "priority-not-from",
      description = "Filter by excluding priority starting from the given value (inclusive).")
  @JsonProperty("priority-not-from")
  private final Integer priorityNotFrom;

  @Parameter(
      name = "priority-not-until",
      description = "Filter by excluding priority up to the given value (inclusive).")
  @JsonProperty("priority-not-until")
  private final Integer priorityNotUntil;

  // endregion
  // region state
  @Parameter(name = "state", description = "Filter by the Task state. This is an exact match.")
  @JsonProperty("state")
  private final TaskState[] stateIn;

  @Parameter(
      name = "state-not",
      description = "Filter by what the Task state shouldn't be. This is an exact match.")
  @JsonProperty("state-not")
  private final TaskState[] stateNotIn;

  @Parameter(
      name = "has-comments",
      description = "Filter by the has-comments flag of the Task. This is an exact match.")
  @JsonProperty("has-comments")
  private final Boolean hasComments;

  // endregion
  // region classificationId
  @Parameter(
      name = "classification-id",
      description = "Filter by the classification id of the Task. This is an exact match.")
  @JsonProperty("classification-id")
  private final String[] classificationIdIn;

  @Parameter(
      name = "classification-id-not",
      description =
          "Filter by what the classification id of the Task shouldn't be. This is an exact match.")
  @JsonProperty("classification-id-not")
  private final String[] classificationIdNotIn;

  // endregion
  // region classificationKey
  @Parameter(
      name = "classification-key",
      description = "Filter by the classification key of the Task. This is an exact match.")
  @JsonProperty("classification-key")
  private final String[] classificationKeyIn;

  @Parameter(
      name = "classification-key-not",
      description = "Filter by the classification key of the Task. This is an exact match.")
  @JsonProperty("classification-key-not")
  private final String[] classificationKeyNotIn;

  @Parameter(
      name = "classification-key-like",
      description =
          "Filter by the classification key of the Task. This results in a substring search (% is"
              + " appended to the front and end of the requested value). Further SQL \"LIKE\" "
              + "wildcard characters will be resolved correctly.")
  @JsonProperty("classification-key-like")
  private final String[] classificationKeyLike;

  @Parameter(
      name = "classification-key-not-like",
      description =
          "Filter by what the classification key of the Task shouldn't be. This results in a "
              + "substring search (% is appended to the front and end of the requested value). "
              + "Further SQL \"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("classification-key-not-like")
  private final String[] classificationKeyNotLike;

  // endregion
  // region classificationParentKey
  @Parameter(
      name = "classification-parent-key",
      description =
          "Filter by the key of the parent Classification of the Classification of the Task. This"
              + " is an exact match.")
  @JsonProperty("classification-parent-key")
  private final String[] classificationParentKeyIn;

  @Parameter(
      name = "classification-parent-key-not",
      description =
          "Filter by what the key of the parent Classification of the Classification of the Task "
              + "shouldn't be. This is an exact match.")
  @JsonProperty("classification-parent-key-not")
  private final String[] classificationParentKeyNotIn;

  @Parameter(
      name = "classification-parent-key-like",
      description =
          "Filter by the key of the parent Classification of the Classification of the Task. This"
              + " results in a substring search (% is appended to the front and end of the "
              + "requested value). Further SQL \"LIKE\" wildcard characters will be resolved "
              + "correctly.")
  @JsonProperty("classification-parent-key-like")
  private final String[] classificationParentKeyLike;

  @Parameter(
      name = "classification-parent-key-not-like",
      description =
          "Filter by what the key of the parent Classification of the Classification of the Task "
              + "shouldn't be. This results in a substring search (% is appended to the front and "
              + "end of the requested value). Further SQL \"LIKE\" wildcard characters will be "
              + "resolved correctly.")
  @JsonProperty("classification-parent-key-not-like")
  private final String[] classificationParentKeyNotLike;

  // endregion
  // region classificationCategory
  @Parameter(
      name = "classification-category",
      description = "Filter by the classification category of the Task. This is an exact match.")
  @JsonProperty("classification-category")
  private final String[] classificationCategoryIn;

  @Parameter(
      name = "classification-category-not",
      description =
          "Filter by what the classification category of the Task shouldn't be. This is an exact "
              + "match.")
  @JsonProperty("classification-category-not")
  private final String[] classificationCategoryNotIn;

  @Parameter(
      name = "classification-category-like",
      description =
          "Filter by the classification category of the Task. This results in a substring search "
              + "(% is appended to the front and end of the requested value). Further SQL \"LIKE\""
              + " wildcard characters will be resolved correctly.")
  @JsonProperty("classification-category-like")
  private final String[] classificationCategoryLike;

  @Parameter(
      name = "classification-category-not-like",
      description =
          "Filter by what the classification category of the Task shouldn't be. This results in a "
              + "substring search (% is appended to the front and end of the requested value). "
              + "Further SQL \"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("classification-category-not-like")
  private final String[] classificationCategoryNotLike;

  // endregion
  // region classificationName
  @Parameter(
      name = "classification-name",
      description = "Filter by the classification name of the Task. This is an exact match.")
  @JsonProperty("classification-name")
  private final String[] classificationNameIn;

  @Parameter(
      name = "classification-name-not",
      description =
          "Filter by what the classification name of the Task shouldn't be. This is an exact "
              + "match.")
  @JsonProperty("classification-name-not")
  private final String[] classificationNameNotIn;

  @Parameter(
      name = "classification-name-like",
      description =
          "Filter by the classification name of the Task. This results in a substring search (% "
              + "is appended to the front and end of the requested value). Further SQL \"LIKE\" "
              + "wildcard characters will be resolved correctly.")
  @JsonProperty("classification-name-like")
  private final String[] classificationNameLike;

  @Parameter(
      name = "classification-name-not-like",
      description =
          "Filter by what the classification name of the Task shouldn't be. This results in a "
              + "substring search (% is appended to the front and end of the requested value). "
              + "Further SQL \"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("classification-name-not-like")
  private final String[] classificationNameNotLike;

  // endregion
  // region workbasketId
  @Parameter(
      name = "workbasket-id",
      description = "Filter by workbasket id of the Task. This is an exact match.")
  @JsonProperty("workbasket-id")
  private final String[] workbasketIdIn;

  @Parameter(
      name = "workbasket-id-not",
      description =
          "Filter by what the workbasket id of the Task shouldn't be. This is an exact match.")
  @JsonProperty("workbasket-id-not")
  private final String[] workbasketIdNotIn;

  // endregion
  // region workbasketKeyDomain
  @Parameter(
      name = "workbasket-key",
      description =
          "Filter by workbasket keys of the Task. This parameter can only be used in combination "
              + "with 'domain'")
  @JsonProperty("workbasket-key")
  private final String[] workbasketKeyIn;

  @Parameter(
      name = "workbasket-key-not",
      description =
          "Filter by what the workbasket keys of the Task aren't. This parameter can only be used "
              + "in combination with 'domain'")
  @JsonProperty("workbasket-key-not")
  private final String[] workbasketKeyNotIn;

  @Parameter(name = "domain", description = "Filter by domain of the Task. This is an exact match.")
  @JsonProperty("domain")
  private final String domain;

  // endregion
  // region businessProcessId
  @Parameter(
      name = "business-process-id",
      description = "Filter by the business process id of the Task. This is an exact match.")
  @JsonProperty("business-process-id")
  private final String[] businessProcessIdIn;

  @Parameter(
      name = "business-process-id-not",
      description =
          "Filter by what the business process id of the Task shouldn't be. This is an exact "
              + "match.")
  @JsonProperty("business-process-id-not")
  private final String[] businessProcessIdNot;

  @Parameter(
      name = "business-process-id-like",
      description =
          "Filter by the business process id of the Task. This results in a substring search (% is"
              + " appended to the front and end of the requested value). Further SQL \"LIKE\" "
              + "wildcard characters will be resolved correctly.")
  @JsonProperty("business-process-id-like")
  private final String[] businessProcessIdLike;

  @Parameter(
      name = "business-process-id-not-like",
      description =
          "Filter by the business process id of the Task shouldn't be. This results in a substring"
              + " search (% is appended to the front and end of the requested value). Further SQL "
              + "\"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("business-process-id-not-like")
  private final String[] businessProcessIdNotLike;

  // endregion
  // region parentBusinessProcessId
  @Parameter(
      name = "parent-business-process-id",
      description = "Filter by the parent business process id of the Task. This is an exact match.")
  @JsonProperty("parent-business-process-id")
  private final String[] parentBusinessProcessIdIn;

  @Parameter(
      name = "parent-business-process-id-not",
      description =
          "Filter by what the parent business process id of the Task shouldn't be. This is an "
              + "exact match.")
  @JsonProperty("parent-business-process-id-not")
  private final String[] parentBusinessProcessIdNotIn;

  @Parameter(
      name = "parent-business-process-id-like",
      description =
          "Filter by the parent business process id of the Task. This results in a substring "
              + "search (% is appended to the front and end of the requested value). Further SQL "
              + "\"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("parent-business-process-id-like")
  private final String[] parentBusinessProcessIdLike;

  @Parameter(
      name = "parent-business-process-id-not-like",
      description =
          "Filter by the parent business process id of the Task shouldn't be. This results in a "
              + "substring search (% is appended to the front and end of the requested value). "
              + "Further SQL \"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("parent-business-process-id-not-like")
  private final String[] parentBusinessProcessIdNotLike;

  // endregion
  // region owner
  @JsonProperty("owner")
  private final String[] ownerIn;

  @Parameter(
      name = "owner-not",
      description = "Filter by what the owner of the Task shouldn't be. This is an exact match.")
  @JsonProperty("owner-not")
  private final String[] ownerNotIn;

  @Parameter(
      name = "owner-like",
      description =
          "Filter by the owner of the Task. This results in a substring search (% is appended to "
              + "the front and end of the requested value). Further SQL \"LIKE\" wildcard "
              + "characters will be resolved correctly.")
  @JsonProperty("owner-like")
  private final String[] ownerLike;

  @Parameter(
      name = "owner-not-like",
      description =
          "Filter by what the owner of the Task shouldn't be. This results in a substring search "
              + "(% is appended to the front and end of the requested value). Further SQL \"LIKE\""
              + " wildcard characters will be resolved correctly.")
  @JsonProperty("owner-not-like")
  private final String[] ownerNotLike;

  @Parameter(
      name = "owner-is-null",
      description =
          "Filter by tasks that have no owner. Either use it as a Query-Flag without any value, "
              + "with the empty value \"\" or with the value \"true\".",
      allowEmptyValue = true)
  @JsonProperty("owner-is-null")
  private final String ownerNull;

  // endregion
  // region primaryObjectReference
  @Parameter(
      name = "por",
      description =
          "Filter by the primary object reference of the Task. This is an exact match. \"por\" is"
              + " a parameter of complex type. Its following attributes from por[].id to "
              + "por[].value can be specified according to the description of complex parameters "
              + "in the overview, e.g. por={\"value\":\"exampleValue\"}")
  @JsonProperty("por")
  private final ObjectReference[] primaryObjectReferenceIn;

  // endregion
  // region primaryObjectReferenceCompany
  @Parameter(
      name = "por-company",
      description =
          "Filter by the company of the primary object reference of the Task. This is an exact "
              + "match.")
  @JsonProperty("por-company")
  private final String[] porCompanyIn;

  @Parameter(
      name = "por-company-not",
      description =
          "Filter by what the company of the primary object reference of the Task shouldn't be. "
              + "This is an exact match.")
  @JsonProperty("por-company-not")
  private final String[] porCompanyNotIn;

  @Parameter(
      name = "por-company-like",
      description =
          "Filter by the company of the primary object reference of the Task. This results in a "
              + "substring search (% is appended to the front and end of the requested value). "
              + "Further SQL \"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("por-company-like")
  private final String[] porCompanyLike;

  @Parameter(
      name = "por-company-not-like",
      description =
          "Filter by what the company of the primary object reference of the Task shouldn't be. "
              + "This results in a substring search (% is appended to the front and end of the "
              + "requested value). Further SQL \"LIKE\" wildcard characters will be resolved "
              + "correctly.")
  @JsonProperty("por-company-not-like")
  private final String[] porCompanyNotLike;

  // endregion
  // region primaryObjectReferenceSystem
  @Parameter(
      name = "por-system",
      description =
          "Filter by the system of the primary object reference of the Task. This is an exact "
              + "match.")
  @JsonProperty("por-system")
  private final String[] porSystemIn;

  @Parameter(
      name = "por-system-not",
      description =
          "Filter by what the system of the primary object reference of the Task shouldn't be. "
              + "This is an exact match.")
  @JsonProperty("por-system-not")
  private final String[] porSystemNotIn;

  @Parameter(
      name = "por-system-like",
      description =
          "Filter by the system of the primary object reference of the Task. This results in a "
              + "substring search (% is appended to the front and end of the requested value). "
              + "Further SQL \"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("por-system-like")
  private final String[] porSystemLike;

  @Parameter(
      name = "por-system-not-like",
      description =
          "Filter by what the system of the primary object reference of the Task shouldn't be. "
              + "This results in a substring search (% is appended to the front and end of the "
              + "requested value). Further SQL \"LIKE\" wildcard characters will be resolved "
              + "correctly.")
  @JsonProperty("por-system-not-like")
  private final String[] porSystemNotLike;

  // endregion
  // region primaryObjectReferenceSystemInstance
  @Parameter(
      name = "por-instance",
      description =
          "Filter by the system instance of the primary object reference of the Task. This is an "
              + "exact match.")
  @JsonProperty("por-instance")
  private final String[] porInstanceIn;

  @Parameter(
      name = "por-instance-not",
      description =
          "Filter by what the system instance of the primary object reference of the Task "
              + "shouldn't be. This is an exact match.")
  @JsonProperty("por-instance-not")
  private final String[] porInstanceNotIn;

  @Parameter(
      name = "por-instance-like",
      description =
          "Filter by the system instance of the primary object reference of the Task. This results"
              + " in a substring search (% is appended to the front and end of the requested "
              + "value). Further SQL \"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("por-instance-like")
  private final String[] porInstanceLike;

  @Parameter(
      name = "por-instance-not-like",
      description =
          "Filter by what the system instance of the primary object reference of the Task "
              + "shouldn't be. This results in a substring search (% is appended to the front and"
              + " end of the requested value). Further SQL \"LIKE\" wildcard characters will be "
              + "resolved correctly.")
  @JsonProperty("por-instance-not-like")
  private final String[] porInstanceNotLike;

  // endregion
  // region primaryObjectReferenceSystemType
  @Parameter(
      name = "por-type",
      description =
          "Filter by the type of the primary object reference of the Task. This is an exact match.")
  @JsonProperty("por-type")
  private final String[] porTypeIn;

  @Parameter(
      name = "por-type-not",
      description =
          "Filter by what the type of the primary object reference of the Task shouldn't be. This "
              + "is an exact match.")
  @JsonProperty("por-type-not")
  private final String[] porTypeNotIn;

  @Parameter(
      name = "por-type-like",
      description =
          "Filter by the type of the primary object reference of the Task. This results in a "
              + "substring search (% is appended to the front and end of the requested value)."
              + " Further SQL \"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("por-type-like")
  private final String[] porTypeLike;

  @Parameter(
      name = "por-type-not-like",
      description =
          "Filter by what the type of the primary object reference of the Task shouldn't be. This "
              + "results in a substring search (% is appended to the front and end of the "
              + "requested value). Further SQL \"LIKE\" wildcard characters will be resolved "
              + "correctly.")
  @JsonProperty("por-type-not-like")
  private final String[] porTypeNotLike;

  // endregion
  // region primaryObjectReferenceSystemValue
  @Parameter(
      name = "por-value",
      description =
          "Filter by the value of the primary object reference of the Task. This is an exact "
              + "match.")
  @JsonProperty("por-value")
  private final String[] porValueIn;

  @Parameter(
      name = "por-value-not",
      description =
          "Filter by what the value of the primary object reference of the Task shouldn't be. This"
              + " is an exact match.")
  @JsonProperty("por-value-not")
  private final String[] porValueNotIn;

  @Parameter(
      name = "por-value-like",
      description =
          "Filter by the value of the primary object reference of the Task. This results in a "
              + "substring search (% is appended to the front and end of the requested value). "
              + "Further SQL \"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("por-value-like")
  private final String[] porValueLike;

  @Parameter(
      name = "por-value-not-like",
      description =
          "Filter by what the value of the primary object reference of the Task shouldn't be. This"
              + " results in a substring search (% is appended to the front and end of the "
              + "requested value). Further SQL \"LIKE\" wildcard characters will be resolved "
              + "correctly.")
  @JsonProperty("por-value-not-like")
  private final String[] porValueNotLike;

  // endregion
  // region secondaryObjectReference
  @Parameter(
      name = "sor",
      description =
          "Filter by the primary object reference of the Task. This is an exact match. \"sor\" is"
              + " a parameter of complex type. Its following attributes from sor[].id to "
              + "sor[].value can be specified according to the description of complex parameters"
              + " in the overview, e.g. sor={\"value\":\"exampleValue\"}")
  @JsonProperty("sor")
  private final ObjectReference[] secondaryObjectReferenceIn;

  // endregion
  // region secondaryObjectReferenceCompany
  @Parameter(
      name = "sor-company",
      description =
          "Filter by the company of the secondary object reference of the Task. This is an exact "
              + "match.")
  @JsonProperty("sor-company")
  private final String[] sorCompanyIn;

  @Parameter(
      name = "sor-company-like",
      description =
          "Filter by the company of the secondary object references of the Task. This results in a"
              + " substring search (% is appended to the front and end of the requested value). "
              + "Further SQL \"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("sor-company-like")
  private final String[] sorCompanyLike;

  // endregion
  // region secondaryObjectReferenceSystem
  @Parameter(
      name = "sor-system",
      description =
          "Filter by the system of the secondary object reference of the Task. This is an exact "
              + "match.")
  @JsonProperty("sor-system")
  private final String[] sorSystemIn;

  @Parameter(
      name = "sor-system-like",
      description =
          "Filter by the system of the secondary object reference of the Task. This results in a "
              + "substring search (% is appended to the front and end of the requested value). "
              + "Further SQL \"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("sor-system-like")
  private final String[] sorSystemLike;

  // endregion
  // region secondaryObjectReferenceSystemInstance
  @Parameter(
      name = "sor-instance",
      description =
          "Filter by the system instance of the secondary object reference of the Task. This is "
              + "an exact match.")
  @JsonProperty("sor-instance")
  private final String[] sorInstanceIn;

  @Parameter(
      name = "sor-instance-like",
      description =
          "Filter by the system instance of the secondary object reference of the Task. This "
              + "results in a substring search (% is appended to the front and end of the "
              + "requested value). Further SQL \"LIKE\" wildcard characters will be resolved "
              + "correctly.")
  @JsonProperty("sor-instance-like")
  private final String[] sorInstanceLike;

  // endregion
  // region secondaryObjectReferenceSystemType
  @Parameter(
      name = "sor-type",
      description =
          "Filter by the type of the secondary object reference of the Task. This is an exact "
              + "match.")
  @JsonProperty("sor-type")
  private final String[] sorTypeIn;

  @Parameter(
      name = "sor-type-like",
      description =
          "Filter by the type of the secondary object reference of the Task. This results in a "
              + "substring search (% is appended to the front and end of the requested value). "
              + "Further SQL \"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("sor-type-like")
  private final String[] sorTypeLike;

  // endregion
  // region primaryObjectReferenceSystemValue
  @Parameter(
      name = "sor-value",
      description =
          "Filter by the value of the secondary object reference of the Task. This is an exact "
              + "match.")
  @JsonProperty("sor-value")
  private final String[] sorValueIn;

  @Parameter(
      name = "sor-value-like",
      description =
          "Filter by the value of the secondary object reference of the Task. This results in a "
              + "substring search (% is appended to the front and end of the requested value). "
              + "Further SQL \"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("sor-value-like")
  private final String[] sorValueLike;

  // endregion
  // region read
  @Parameter(
      name = "is-read",
      description = "Filter by the is read flag of the Task. This is an exact match.")
  @JsonProperty("is-read")
  private final Boolean read;

  // endregion
  // region transferred
  @Parameter(
      name = "is-transferred",
      description = "Filter by the is transferred flag of the Task. This is an exact match.")
  @JsonProperty("is-transferred")
  private final Boolean transferred;

  // endregion
  // region reopened
  @Parameter(
      name = "is-reopened",
      description = "Filter by the is reopened flag of the Task. This is an exact match.")
  @JsonProperty("is-reopened")
  private final Boolean reopened;

  // endregion
  // region attachmentClassificationId
  @Parameter(
      name = "attachment-classification-id",
      description =
          "Filter by the attachment classification id of the Task. This is an exact match.")
  @JsonProperty("attachment-classification-id")
  private final String[] attachmentClassificationIdIn;

  @Parameter(
      name = "attachment-classification-id-not",
      description =
          "Filter by what the attachment classification id of the Task shouldn't be. This is an "
              + "exact match.")
  @JsonProperty("attachment-classification-id-not")
  private final String[] attachmentClassificationIdNotIn;

  // endregion
  // region attachmentClassificationKey
  @Parameter(
      name = "attachment-classification-key",
      description =
          "Filter by the attachment classification key of the Task. This is an exact match.")
  @JsonProperty("attachment-classification-key")
  private final String[] attachmentClassificationKeyIn;

  @Parameter(
      name = "attachment-classification-key-not",
      description =
          "Filter by what the attachment classification key of the Task shouldn't be. This is an "
              + "exact match.")
  @JsonProperty("attachment-classification-key-not")
  private final String[] attachmentClassificationKeyNotIn;

  @Parameter(
      name = "attachment-classification-key-like",
      description =
          "Filter by the attachment classification key of the Task. This results in a substring "
              + "search (% is appended to the front and end of the requested value). Further SQL "
              + "\"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("attachment-classification-key-like")
  private final String[] attachmentClassificationKeyLike;

  @Parameter(
      name = "attachment-classification-key-not-like",
      description =
          "Filter by what the attachment classification key of the Task shouldn't be. This results"
              + " in a substring search (% is appended to the front and end of the requested "
              + "value). Further SQL \"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("attachment-classification-key-not-like")
  private final String[] attachmentClassificationKeyNotLike;

  // endregion
  // region attachmentClassificationName
  @Parameter(
      name = "attachment-classification-name",
      description =
          "Filter by the attachment classification name of the Task. This is an exact match.")
  @JsonProperty("attachment-classification-name")
  private final String[] attachmentClassificationNameIn;

  @Parameter(
      name = "attachment-classification-name-not",
      description =
          "Filter by what the attachment classification name of the Task shouldn't be. This is an "
              + "exact match.")
  @JsonProperty("attachment-classification-name-not")
  private final String[] attachmentClassificationNameNotIn;

  @Parameter(
      name = "attachment-classification-name-like",
      description =
          "Filter by the attachment classification name of the Task. This results in a substring "
              + "search (% is appended to the front and end of the requested value). Further SQL "
              + "\"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("attachment-classification-name-like")
  private final String[] attachmentClassificationNameLike;

  @Parameter(
      name = "attachment-classification-name-not-like",
      description =
          "Filter by what the attachment classification name of the Task shouldn't be. This "
              + "results in a substring search (% is appended to the front and end of the "
              + "requested value). Further SQL \"LIKE\" wildcard characters will be resolved "
              + "correctly.")
  @JsonProperty("attachment-classification-name-not-like")
  private final String[] attachmentClassificationNameNotLike;

  // endregion
  // region attachmentChannel
  @Parameter(
      name = "attachment-channel",
      description = "Filter by the attachment channel of the Task. This is an exact match.")
  @JsonProperty("attachment-channel")
  private final String[] attachmentChannelIn;

  @Parameter(
      name = "attachment-channel-not",
      description =
          "Filter by what the attachment channel of the Task shouldn't be. This is an exact "
              + "match.")
  @JsonProperty("attachment-channel-not")
  private final String[] attachmentChannelNotIn;

  @Parameter(
      name = "attachment-channel-like",
      description =
          "Filter by the attachment channel of the Task. This results in a substring search (% is "
              + "appended to the front and end of the requested value). Further SQL \"LIKE\" "
              + "wildcard characters will be resolved correctly.")
  @JsonProperty("attachment-channel-like")
  private final String[] attachmentChannelLike;

  @Parameter(
      name = "attachment-channel-not-like",
      description =
          "Filter by what the attachment channel of the Task shouldn't be. This results in a "
              + "substring search (% is appended to the front and end of the requested value). "
              + "Further SQL \"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("attachment-channel-not-like")
  private final String[] attachmentChannelNotLike;

  // endregion
  // region attachmentReferenceValue
  @Parameter(
      name = "attachment-reference",
      description = "Filter by the attachment reference of the Task. This is an exact match.")
  @JsonProperty("attachment-reference")
  private final String[] attachmentReferenceIn;

  @Parameter(
      name = "attachment-reference-not",
      description =
          "Filter by what the attachment reference of the Task shouldn't be. This is an exact "
              + "match.")
  @JsonProperty("attachment-reference-not")
  private final String[] attachmentReferenceNotIn;

  @Parameter(
      name = "attachment-reference-like",
      description =
          "Filter by the attachment reference of the Task. This results in a substring search (% "
              + "is appended to the front and end of the requested value). Further SQL \"LIKE\" "
              + "wildcard characters will be resolved correctly.")
  @JsonProperty("attachment-reference-like")
  private final String[] attachmentReferenceLike;

  @Parameter(
      name = "attachment-reference-not-like",
      description =
          "Filter by what the attachment reference of the Task shouldn't be. This results in a "
              + "substring search (% is appended to the front and end of the requested value). "
              + "Further SQL \"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("attachment-reference-not-like")
  private final String[] attachmentReferenceNotLike;

  // endregion
  // region attachmentReceived
  @Parameter(
      name = "attachment-received",
      description =
          "Filter by a time interval within which the attachment of the Task was received. To "
              + "create an open interval you can just leave it blank. <p>The format is ISO-8601.")
  @JsonProperty("attachment-received")
  private final Instant[] attachmentReceivedWithin;

  @Parameter(
      name = "attachment-received-not",
      description =
          "Filter by a time interval within which the attachment of the Task wasn't received. To "
              + "create an open interval you can just leave it blank. <p>The format is ISO-8601.")
  @JsonProperty("attachment-received-not")
  private final Instant[] attachmentReceivedNotWithin;

  // endregion
  // region withoutAttachment
  @Parameter(
      name = "without-attachment",
      description =
          "In order to filter Tasks that don't have any Attachments, set 'without-attachment' to "
              + "'true'. Any other value for 'without-attachment' is invalid.")
  @JsonProperty("without-attachment")
  private final Boolean withoutAttachment;

  // endregion
  // region callbackState
  @Parameter(
      name = "callback-state",
      description = "Filter by the callback state of the Task. This is an exact match.")
  @JsonProperty("callback-state")
  private final CallbackState[] callbackStateIn;

  @Parameter(
      name = "callback-state-not",
      description =
          "Filter by what the callback state of the Task shouldn't be. This is an exact match.")
  @JsonProperty("callback-state-not")
  private final CallbackState[] callbackStateNotIn;

  // endregion
  // region wildcardSearchValue
  @Parameter(
      name = "wildcard-search-fields",
      description =
          "Filter by wildcard search field of the Task. <p>This must be used in combination with "
              + "'wildcard-search-value'")
  @JsonProperty("wildcard-search-fields")
  private final WildcardSearchField[] wildcardSearchFieldIn;

  @Parameter(
      name = "wildcard-search-value",
      description =
          "Filter by wildcard search field of the Task. This is an exact match. <p>This must be"
              + " used in combination with 'wildcard-search-fields'")
  @JsonProperty("wildcard-search-value")
  private final String wildcardSearchValue;

  @ConstructorProperties({
    "task-id",
    "task-id-not",
    "external-id",
    "external-id-not",
    "received",
    "received-from",
    "received-until",
    "received-not",
    "received-from-not",
    "received-until-not",
    "created",
    "created-from",
    "created-until",
    "created-not",
    "created-from-not",
    "created-until-not",
    "claimed",
    "claimed-not",
    "modified",
    "modified-not",
    "planned",
    "planned-from",
    "planned-until",
    "planned-not",
    "planned-from-not",
    "planned-until-not",
    "due",
    "due-from",
    "due-until",
    "due-not",
    "due-from-not",
    "due-until-not",
    "completed",
    "completed-from",
    "completed-until",
    "completed-not",
    "completed-from-not",
    "completed-until-not",
    "name",
    "name-not",
    "name-like",
    "name-not-like",
    "creator",
    "creator-not",
    "creator-like",
    "creator-not-like",
    "note-like",
    "note-not-like",
    "description-like",
    "description-not-like",
    "priority",
    "priority-not",
    "priority-within",
    "priority-from",
    "priority-until",
    "priority-not-within",
    "priority-not-from",
    "priority-not-until",
    "state",
    "state-not",
    "has-comments",
    "classification-id",
    "classification-id-not",
    "classification-key",
    "classification-key-not",
    "classification-key-like",
    "classification-key-not-like",
    "classification-parent-key",
    "classification-parent-key-not",
    "classification-parent-key-like",
    "classification-parent-key-not-like",
    "classification-category",
    "classification-category-not",
    "classification-category-like",
    "classification-category-not-like",
    "classification-name",
    "classification-name-not",
    "classification-name-like",
    "classification-name-not-like",
    "workbasket-id",
    "workbasket-id-not",
    "workbasket-key",
    "workbasket-key-not",
    "domain",
    "business-process-id",
    "business-process-id-not",
    "business-process-id-like",
    "business-process-id-not-like",
    "parent-business-process-id",
    "parent-business-process-id-not",
    "parent-business-process-id-like",
    "parent-business-process-id-not-like",
    "owner",
    "owner-not",
    "owner-like",
    "owner-not-like",
    "owner-is-null",
    "por",
    "por-company",
    "por-company-not",
    "por-company-like",
    "por-company-not-like",
    "por-system",
    "por-system-not",
    "por-system-like",
    "por-system-not-like",
    "por-instance",
    "por-instance-not",
    "por-instance-like",
    "por-instance-not-like",
    "por-type",
    "por-type-not",
    "por-type-like",
    "por-type-not-like",
    "por-value",
    "por-value-not",
    "por-value-like",
    "por-value-not-like",
    "sor",
    "sor-company",
    "sor-company-like",
    "sor-system",
    "sor-system-like",
    "sor-instance",
    "sor-instance-like",
    "sor-type",
    "sor-type-like",
    "sor-value",
    "sor-value-like",
    "is-read",
    "is-transferred",
    "is-reopened",
    "attachment-classification-id",
    "attachment-classification-id-not",
    "attachment-classification-key",
    "attachment-classification-key-not",
    "attachment-classification-key-like",
    "attachment-classification-key-not-like",
    "attachment-classification-name",
    "attachment-classification-name-not",
    "attachment-classification-name-like",
    "attachment-classification-name-not-like",
    "attachment-channel",
    "attachment-channel-not",
    "attachment-channel-like",
    "attachment-channel-not-like",
    "attachment-reference",
    "attachment-reference-not",
    "attachment-reference-like",
    "attachment-reference-not-like",
    "attachment-received",
    "attachment-received-not",
    "without-attachment",
    "callback-state",
    "callback-state-not",
    "wildcard-search-fields",
    "wildcard-search-value"
  })
  public TaskQueryFilterParameter(
      String[] taskIdIn,
      String[] taskIdNotIn,
      String[] externalIdIn,
      String[] externalIdNotIn,
      Instant[] receivedWithin,
      Instant receivedFrom,
      Instant receivedUntil,
      Instant[] receivedNotIn,
      Instant receivedFromNot,
      Instant receivedUntilNot,
      Instant[] createdWithin,
      Instant createdFrom,
      Instant createdUntil,
      Instant[] createdNotWithin,
      Instant createdFromNot,
      Instant createdUntilNot,
      Instant[] claimedWithin,
      Instant[] claimedNotWithin,
      Instant[] modifiedWithin,
      Instant[] modifiedNotWithin,
      Instant[] plannedWithin,
      Instant plannedFrom,
      Instant plannedUntil,
      Instant[] plannedNotWithin,
      Instant plannedFromNot,
      Instant plannedUntilNot,
      Instant[] dueWithin,
      Instant dueFrom,
      Instant dueUntil,
      Instant[] dueNotWithin,
      Instant dueFromNot,
      Instant dueUntilNot,
      Instant[] completedWithin,
      Instant completedFrom,
      Instant completedUntil,
      Instant[] completedNotWithin,
      Instant completedFromNot,
      Instant completedUntilNot,
      String[] nameIn,
      String[] nameNotIn,
      String[] nameLike,
      String[] nameNotLike,
      String[] creatorIn,
      String[] creatorNotIn,
      String[] creatorLike,
      String[] creatorNotLike,
      String[] noteLike,
      String[] noteNotLike,
      String[] descriptionLike,
      String[] descriptionNotLike,
      int[] priorityIn,
      int[] priorityNotIn,
      Integer[] priorityWithin,
      Integer priorityFrom,
      Integer priorityUntil,
      Integer[] priorityNotWithin,
      Integer priorityNotFrom,
      Integer priorityNotUntil,
      TaskState[] stateIn,
      TaskState[] stateNotIn,
      Boolean hasComments,
      String[] classificationIdIn,
      String[] classificationIdNotIn,
      String[] classificationKeyIn,
      String[] classificationKeyNotIn,
      String[] classificationKeyLike,
      String[] classificationKeyNotLike,
      String[] classificationParentKeyIn,
      String[] classificationParentKeyNotIn,
      String[] classificationParentKeyLike,
      String[] classificationParentKeyNotLike,
      String[] classificationCategoryIn,
      String[] classificationCategoryNotIn,
      String[] classificationCategoryLike,
      String[] classificationCategoryNotLike,
      String[] classificationNameIn,
      String[] classificationNameNotIn,
      String[] classificationNameLike,
      String[] classificationNameNotLike,
      String[] workbasketIdIn,
      String[] workbasketIdNotIn,
      String[] workbasketKeyIn,
      String[] workbasketKeyNotIn,
      String domain,
      String[] businessProcessIdIn,
      String[] businessProcessIdNot,
      String[] businessProcessIdLike,
      String[] businessProcessIdNotLike,
      String[] parentBusinessProcessIdIn,
      String[] parentBusinessProcessIdNotIn,
      String[] parentBusinessProcessIdLike,
      String[] parentBusinessProcessIdNotLike,
      String[] ownerIn,
      String[] ownerNotIn,
      String[] ownerLike,
      String[] ownerNotLike,
      String ownerNull,
      ObjectReference[] primaryObjectReferenceIn,
      String[] porCompanyIn,
      String[] porCompanyNotIn,
      String[] porCompanyLike,
      String[] porCompanyNotLike,
      String[] porSystemIn,
      String[] porSystemNotIn,
      String[] porSystemLike,
      String[] porSystemNotLike,
      String[] porInstanceIn,
      String[] porInstanceNotIn,
      String[] porInstanceLike,
      String[] porInstanceNotLike,
      String[] porTypeIn,
      String[] porTypeNotIn,
      String[] porTypeLike,
      String[] porTypeNotLike,
      String[] porValueIn,
      String[] porValueNotIn,
      String[] porValueLike,
      String[] porValueNotLike,
      ObjectReference[] secondaryObjectReferenceIn,
      String[] sorCompanyIn,
      String[] sorCompanyLike,
      String[] sorSystemIn,
      String[] sorSystemLike,
      String[] sorInstanceIn,
      String[] sorInstanceLike,
      String[] sorTypeIn,
      String[] sorTypeLike,
      String[] sorValueIn,
      String[] sorValueLike,
      Boolean read,
      Boolean transferred,
      Boolean reopened,
      String[] attachmentClassificationIdIn,
      String[] attachmentClassificationIdNotIn,
      String[] attachmentClassificationKeyIn,
      String[] attachmentClassificationKeyNotIn,
      String[] attachmentClassificationKeyLike,
      String[] attachmentClassificationKeyNotLike,
      String[] attachmentClassificationNameIn,
      String[] attachmentClassificationNameNotIn,
      String[] attachmentClassificationNameLike,
      String[] attachmentClassificationNameNotLike,
      String[] attachmentChannelIn,
      String[] attachmentChannelNotIn,
      String[] attachmentChannelLike,
      String[] attachmentChannelNotLike,
      String[] attachmentReferenceIn,
      String[] attachmentReferenceNotIn,
      String[] attachmentReferenceLike,
      String[] attachmentReferenceNotLike,
      Instant[] attachmentReceivedWithin,
      Instant[] attachmentReceivedNotWithin,
      Boolean withoutAttachment,
      CallbackState[] callbackStateIn,
      CallbackState[] callbackStateNotIn,
      WildcardSearchField[] wildcardSearchFieldIn,
      String wildcardSearchValue)
      throws InvalidArgumentException {
    this.taskIdIn = taskIdIn;
    this.taskIdNotIn = taskIdNotIn;
    this.externalIdIn = externalIdIn;
    this.externalIdNotIn = externalIdNotIn;
    this.receivedWithin = receivedWithin;
    this.receivedFrom = receivedFrom;
    this.receivedUntil = receivedUntil;
    this.receivedNotIn = receivedNotIn;
    this.receivedFromNot = receivedFromNot;
    this.receivedUntilNot = receivedUntilNot;
    this.createdWithin = createdWithin;
    this.createdFrom = createdFrom;
    this.createdUntil = createdUntil;
    this.createdNotWithin = createdNotWithin;
    this.createdFromNot = createdFromNot;
    this.createdUntilNot = createdUntilNot;
    this.claimedWithin = claimedWithin;
    this.claimedNotWithin = claimedNotWithin;
    this.modifiedWithin = modifiedWithin;
    this.modifiedNotWithin = modifiedNotWithin;
    this.plannedWithin = plannedWithin;
    this.plannedFrom = plannedFrom;
    this.plannedUntil = plannedUntil;
    this.plannedNotWithin = plannedNotWithin;
    this.plannedFromNot = plannedFromNot;
    this.plannedUntilNot = plannedUntilNot;
    this.dueWithin = dueWithin;
    this.dueFrom = dueFrom;
    this.dueUntil = dueUntil;
    this.dueNotWithin = dueNotWithin;
    this.dueFromNot = dueFromNot;
    this.dueUntilNot = dueUntilNot;
    this.completedWithin = completedWithin;
    this.completedFrom = completedFrom;
    this.completedUntil = completedUntil;
    this.completedNotWithin = completedNotWithin;
    this.completedFromNot = completedFromNot;
    this.completedUntilNot = completedUntilNot;
    this.nameIn = nameIn;
    this.nameNotIn = nameNotIn;
    this.nameLike = nameLike;
    this.nameNotLike = nameNotLike;
    this.creatorIn = creatorIn;
    this.creatorNotIn = creatorNotIn;
    this.creatorLike = creatorLike;
    this.creatorNotLike = creatorNotLike;
    this.noteLike = noteLike;
    this.noteNotLike = noteNotLike;
    this.descriptionLike = descriptionLike;
    this.descriptionNotLike = descriptionNotLike;
    this.priorityIn = priorityIn;
    this.priorityNotIn = priorityNotIn;
    this.priorityWithin = priorityWithin;
    this.priorityFrom = priorityFrom;
    this.priorityUntil = priorityUntil;
    this.priorityNotWithin = priorityNotWithin;
    this.priorityNotFrom = priorityNotFrom;
    this.priorityNotUntil = priorityNotUntil;
    this.stateIn = stateIn;
    this.stateNotIn = stateNotIn;
    this.hasComments = hasComments;
    this.classificationIdIn = classificationIdIn;
    this.classificationIdNotIn = classificationIdNotIn;
    this.classificationKeyIn = classificationKeyIn;
    this.classificationKeyNotIn = classificationKeyNotIn;
    this.classificationKeyLike = classificationKeyLike;
    this.classificationKeyNotLike = classificationKeyNotLike;
    this.classificationParentKeyIn = classificationParentKeyIn;
    this.classificationParentKeyNotIn = classificationParentKeyNotIn;
    this.classificationParentKeyLike = classificationParentKeyLike;
    this.classificationParentKeyNotLike = classificationParentKeyNotLike;
    this.classificationCategoryIn = classificationCategoryIn;
    this.classificationCategoryNotIn = classificationCategoryNotIn;
    this.classificationCategoryLike = classificationCategoryLike;
    this.classificationCategoryNotLike = classificationCategoryNotLike;
    this.classificationNameIn = classificationNameIn;
    this.classificationNameNotIn = classificationNameNotIn;
    this.classificationNameLike = classificationNameLike;
    this.classificationNameNotLike = classificationNameNotLike;
    this.workbasketIdIn = workbasketIdIn;
    this.workbasketIdNotIn = workbasketIdNotIn;
    this.workbasketKeyIn = workbasketKeyIn;
    this.workbasketKeyNotIn = workbasketKeyNotIn;
    this.domain = domain;
    this.businessProcessIdIn = businessProcessIdIn;
    this.businessProcessIdNot = businessProcessIdNot;
    this.businessProcessIdLike = businessProcessIdLike;
    this.businessProcessIdNotLike = businessProcessIdNotLike;
    this.parentBusinessProcessIdIn = parentBusinessProcessIdIn;
    this.parentBusinessProcessIdNotIn = parentBusinessProcessIdNotIn;
    this.parentBusinessProcessIdLike = parentBusinessProcessIdLike;
    this.parentBusinessProcessIdNotLike = parentBusinessProcessIdNotLike;
    this.ownerIn = ownerIn;
    this.ownerNotIn = ownerNotIn;
    this.ownerLike = ownerLike;
    this.ownerNotLike = ownerNotLike;
    this.ownerNull = ownerNull;
    this.primaryObjectReferenceIn = primaryObjectReferenceIn;
    this.porCompanyIn = porCompanyIn;
    this.porCompanyNotIn = porCompanyNotIn;
    this.porCompanyLike = porCompanyLike;
    this.porCompanyNotLike = porCompanyNotLike;
    this.porSystemIn = porSystemIn;
    this.porSystemNotIn = porSystemNotIn;
    this.porSystemLike = porSystemLike;
    this.porSystemNotLike = porSystemNotLike;
    this.porInstanceIn = porInstanceIn;
    this.porInstanceNotIn = porInstanceNotIn;
    this.porInstanceLike = porInstanceLike;
    this.porInstanceNotLike = porInstanceNotLike;
    this.porTypeIn = porTypeIn;
    this.porTypeNotIn = porTypeNotIn;
    this.porTypeLike = porTypeLike;
    this.porTypeNotLike = porTypeNotLike;
    this.porValueIn = porValueIn;
    this.porValueNotIn = porValueNotIn;
    this.porValueLike = porValueLike;
    this.porValueNotLike = porValueNotLike;
    this.secondaryObjectReferenceIn = secondaryObjectReferenceIn;
    this.sorCompanyIn = sorCompanyIn;
    this.sorCompanyLike = sorCompanyLike;
    this.sorSystemIn = sorSystemIn;
    this.sorSystemLike = sorSystemLike;
    this.sorInstanceIn = sorInstanceIn;
    this.sorInstanceLike = sorInstanceLike;
    this.sorTypeIn = sorTypeIn;
    this.sorTypeLike = sorTypeLike;
    this.sorValueIn = sorValueIn;
    this.sorValueLike = sorValueLike;
    this.read = read;
    this.transferred = transferred;
    this.reopened = reopened;
    this.attachmentClassificationIdIn = attachmentClassificationIdIn;
    this.attachmentClassificationIdNotIn = attachmentClassificationIdNotIn;
    this.attachmentClassificationKeyIn = attachmentClassificationKeyIn;
    this.attachmentClassificationKeyNotIn = attachmentClassificationKeyNotIn;
    this.attachmentClassificationKeyLike = attachmentClassificationKeyLike;
    this.attachmentClassificationKeyNotLike = attachmentClassificationKeyNotLike;
    this.attachmentClassificationNameIn = attachmentClassificationNameIn;
    this.attachmentClassificationNameNotIn = attachmentClassificationNameNotIn;
    this.attachmentClassificationNameLike = attachmentClassificationNameLike;
    this.attachmentClassificationNameNotLike = attachmentClassificationNameNotLike;
    this.attachmentChannelIn = attachmentChannelIn;
    this.attachmentChannelNotIn = attachmentChannelNotIn;
    this.attachmentChannelLike = attachmentChannelLike;
    this.attachmentChannelNotLike = attachmentChannelNotLike;
    this.attachmentReferenceIn = attachmentReferenceIn;
    this.attachmentReferenceNotIn = attachmentReferenceNotIn;
    this.attachmentReferenceLike = attachmentReferenceLike;
    this.attachmentReferenceNotLike = attachmentReferenceNotLike;
    this.attachmentReceivedWithin = attachmentReceivedWithin;
    this.attachmentReceivedNotWithin = attachmentReceivedNotWithin;
    this.withoutAttachment = withoutAttachment;
    this.callbackStateIn = callbackStateIn;
    this.callbackStateNotIn = callbackStateNotIn;
    this.wildcardSearchFieldIn = wildcardSearchFieldIn;
    this.wildcardSearchValue = wildcardSearchValue;

    VALIDATOR.validate(this);
  }

  public String[] getTaskIdIn() {
    return taskIdIn;
  }

  public String[] getExternalIdIn() {
    return externalIdIn;
  }

  public String[] getExternalIdNotIn() {
    return externalIdNotIn;
  }

  public Instant[] getReceivedWithin() {
    return receivedWithin;
  }

  public Instant getReceivedFrom() {
    return receivedFrom;
  }

  public Instant getReceivedUntil() {
    return receivedUntil;
  }

  public Instant[] getReceivedNotIn() {
    return receivedNotIn;
  }

  public Instant getReceivedFromNot() {
    return receivedFromNot;
  }

  public Instant getReceivedUntilNot() {
    return receivedUntilNot;
  }

  public Instant[] getCreatedWithin() {
    return createdWithin;
  }

  public Instant getCreatedFrom() {
    return createdFrom;
  }

  public Instant getCreatedUntil() {
    return createdUntil;
  }

  public Instant[] getCreatedNotWithin() {
    return createdNotWithin;
  }

  public Instant getCreatedFromNot() {
    return createdFromNot;
  }

  public Instant getCreatedUntilNot() {
    return createdUntilNot;
  }

  public Instant[] getClaimedWithin() {
    return claimedWithin;
  }

  public Instant[] getClaimedNotWithin() {
    return claimedNotWithin;
  }

  public Instant[] getModifiedWithin() {
    return modifiedWithin;
  }

  public Instant[] getModifiedNotWithin() {
    return modifiedNotWithin;
  }

  public Instant[] getPlannedWithin() {
    return plannedWithin;
  }

  public Instant getPlannedFrom() {
    return plannedFrom;
  }

  public Instant getPlannedUntil() {
    return plannedUntil;
  }

  public Instant[] getPlannedNotWithin() {
    return plannedNotWithin;
  }

  public Instant getPlannedFromNot() {
    return plannedFromNot;
  }

  public Instant getPlannedUntilNot() {
    return plannedUntilNot;
  }

  public Instant[] getDueWithin() {
    return dueWithin;
  }

  public Instant getDueFrom() {
    return dueFrom;
  }

  public Instant getDueUntil() {
    return dueUntil;
  }

  public Instant[] getDueNotWithin() {
    return dueNotWithin;
  }

  public Instant getDueFromNot() {
    return dueFromNot;
  }

  public Instant getDueUntilNot() {
    return dueUntilNot;
  }

  public Instant[] getCompletedWithin() {
    return completedWithin;
  }

  public Instant getCompletedFrom() {
    return completedFrom;
  }

  public Instant getCompletedUntil() {
    return completedUntil;
  }

  public Instant[] getCompletedNotWithin() {
    return completedNotWithin;
  }

  public Instant getCompletedFromNot() {
    return completedFromNot;
  }

  public Instant getCompletedUntilNot() {
    return completedUntilNot;
  }

  public String[] getNameIn() {
    return nameIn;
  }

  public String[] getNameNotIn() {
    return nameNotIn;
  }

  public String[] getNameLike() {
    return nameLike;
  }

  public String[] getNameNotLike() {
    return nameNotLike;
  }

  public String[] getCreatorIn() {
    return creatorIn;
  }

  public String[] getCreatorNotIn() {
    return creatorNotIn;
  }

  public String[] getCreatorLike() {
    return creatorLike;
  }

  public String[] getCreatorNotLike() {
    return creatorNotLike;
  }

  public String[] getNoteLike() {
    return noteLike;
  }

  public String[] getNoteNotLike() {
    return noteNotLike;
  }

  public String[] getDescriptionLike() {
    return descriptionLike;
  }

  public String[] getDescriptionNotLike() {
    return descriptionNotLike;
  }

  public int[] getPriorityIn() {
    return priorityIn;
  }

  public int[] getPriorityNotIn() {
    return priorityNotIn;
  }

  public Integer[] getPriorityWithin() {
    return priorityWithin;
  }

  public Integer getPriorityFrom() {
    return priorityFrom;
  }

  public Integer getPriorityUntil() {
    return priorityUntil;
  }

  public Integer[] getPriorityNotWithin() {
    return priorityNotWithin;
  }

  public Integer getPriorityNotFrom() {
    return priorityNotFrom;
  }

  public Integer getPriorityNotUntil() {
    return priorityNotUntil;
  }

  public TaskState[] getStateIn() {
    return stateIn;
  }

  // endregion
  // region comments

  public Boolean getHasComments() {
    return hasComments;
  }

  public TaskState[] getStateNotIn() {
    return stateNotIn;
  }

  public String[] getClassificationIdIn() {
    return classificationIdIn;
  }

  public String[] getClassificationIdNotIn() {
    return classificationIdNotIn;
  }

  public String[] getClassificationKeyIn() {
    return classificationKeyIn;
  }

  public String[] getClassificationKeyNotIn() {
    return classificationKeyNotIn;
  }

  public String[] getClassificationKeyLike() {
    return classificationKeyLike;
  }

  public String[] getClassificationKeyNotLike() {
    return classificationKeyNotLike;
  }

  public String[] getClassificationParentKeyIn() {
    return classificationParentKeyIn;
  }

  public String[] getClassificationParentKeyNotIn() {
    return classificationParentKeyNotIn;
  }

  public String[] getClassificationParentKeyLike() {
    return classificationParentKeyLike;
  }

  public String[] getClassificationParentKeyNotLike() {
    return classificationParentKeyNotLike;
  }

  public String[] getClassificationCategoryIn() {
    return classificationCategoryIn;
  }

  public String[] getClassificationCategoryNotIn() {
    return classificationCategoryNotIn;
  }

  public String[] getClassificationCategoryLike() {
    return classificationCategoryLike;
  }

  public String[] getClassificationCategoryNotLike() {
    return classificationCategoryNotLike;
  }

  public String[] getClassificationNameIn() {
    return classificationNameIn;
  }

  public String[] getClassificationNameNotIn() {
    return classificationNameNotIn;
  }

  public String[] getClassificationNameLike() {
    return classificationNameLike;
  }

  public String[] getClassificationNameNotLike() {
    return classificationNameNotLike;
  }

  public String[] getWorkbasketIdIn() {
    return workbasketIdIn;
  }

  public String[] getWorkbasketIdNotIn() {
    return workbasketIdNotIn;
  }

  public String[] getWorkbasketKeyIn() {
    return workbasketKeyIn;
  }

  public String[] getWorkbasketKeyNotIn() {
    return workbasketKeyNotIn;
  }

  public String getDomain() {
    return domain;
  }

  public String[] getBusinessProcessIdIn() {
    return businessProcessIdIn;
  }

  public String[] getBusinessProcessIdNot() {
    return businessProcessIdNot;
  }

  public String[] getBusinessProcessIdLike() {
    return businessProcessIdLike;
  }

  public String[] getBusinessProcessIdNotLike() {
    return businessProcessIdNotLike;
  }

  public String[] getParentBusinessProcessIdIn() {
    return parentBusinessProcessIdIn;
  }

  public String[] getParentBusinessProcessIdNotIn() {
    return parentBusinessProcessIdNotIn;
  }

  public String[] getParentBusinessProcessIdLike() {
    return parentBusinessProcessIdLike;
  }

  public String[] getParentBusinessProcessIdNotLike() {
    return parentBusinessProcessIdNotLike;
  }

  public String[] getOwnerIn() {
    return ownerIn;
  }

  public String[] getOwnerNotIn() {
    return ownerNotIn;
  }

  public String[] getOwnerLike() {
    return ownerLike;
  }

  public String[] getOwnerNotLike() {
    return ownerNotLike;
  }

  public String getOwnerNull() {
    return ownerNull;
  }

  public ObjectReference[] getPrimaryObjectReferenceIn() {
    return primaryObjectReferenceIn;
  }

  public String[] getPorCompanyIn() {
    return porCompanyIn;
  }

  public String[] getPorCompanyNotIn() {
    return porCompanyNotIn;
  }

  public String[] getPorCompanyLike() {
    return porCompanyLike;
  }

  public String[] getPorCompanyNotLike() {
    return porCompanyNotLike;
  }

  public String[] getPorSystemIn() {
    return porSystemIn;
  }

  public String[] getPorSystemNotIn() {
    return porSystemNotIn;
  }

  public String[] getPorSystemLike() {
    return porSystemLike;
  }

  public String[] getPorSystemNotLike() {
    return porSystemNotLike;
  }

  public String[] getPorInstanceIn() {
    return porInstanceIn;
  }

  public String[] getPorInstanceNotIn() {
    return porInstanceNotIn;
  }

  public String[] getPorInstanceLike() {
    return porInstanceLike;
  }

  public String[] getPorInstanceNotLike() {
    return porInstanceNotLike;
  }

  public String[] getPorTypeIn() {
    return porTypeIn;
  }

  public String[] getPorTypeNotIn() {
    return porTypeNotIn;
  }

  public String[] getPorTypeLike() {
    return porTypeLike;
  }

  public String[] getPorTypeNotLike() {
    return porTypeNotLike;
  }

  public String[] getPorValueIn() {
    return porValueIn;
  }

  public String[] getPorValueNotIn() {
    return porValueNotIn;
  }

  public String[] getPorValueLike() {
    return porValueLike;
  }

  public String[] getPorValueNotLike() {
    return porValueNotLike;
  }

  public ObjectReference[] getSecondaryObjectReferenceIn() {
    return secondaryObjectReferenceIn;
  }

  public String[] getSorCompanyIn() {
    return sorCompanyIn;
  }

  public String[] getSorCompanyLike() {
    return sorCompanyLike;
  }

  public String[] getSorSystemIn() {
    return sorSystemIn;
  }

  public String[] getSorSystemLike() {
    return sorSystemLike;
  }

  public String[] getSorInstanceIn() {
    return sorInstanceIn;
  }

  public String[] getSorInstanceLike() {
    return sorInstanceLike;
  }

  public String[] getSorTypeIn() {
    return sorTypeIn;
  }

  public String[] getSorTypeLike() {
    return sorTypeLike;
  }

  public String[] getSorValueIn() {
    return sorValueIn;
  }

  public String[] getSorValueLike() {
    return sorValueLike;
  }

  public Boolean getRead() {
    return read;
  }

  public Boolean getTransferred() {
    return transferred;
  }

  public Boolean getReopened() {
    return reopened;
  }

  public String[] getAttachmentClassificationIdIn() {
    return attachmentClassificationIdIn;
  }

  public String[] getAttachmentClassificationIdNotIn() {
    return attachmentClassificationIdNotIn;
  }

  public String[] getAttachmentClassificationKeyIn() {
    return attachmentClassificationKeyIn;
  }

  public String[] getAttachmentClassificationKeyNotIn() {
    return attachmentClassificationKeyNotIn;
  }

  public String[] getAttachmentClassificationKeyLike() {
    return attachmentClassificationKeyLike;
  }

  public String[] getAttachmentClassificationKeyNotLike() {
    return attachmentClassificationKeyNotLike;
  }

  public String[] getAttachmentClassificationNameIn() {
    return attachmentClassificationNameIn;
  }

  public String[] getAttachmentClassificationNameNotIn() {
    return attachmentClassificationNameNotIn;
  }

  public String[] getAttachmentClassificationNameLike() {
    return attachmentClassificationNameLike;
  }

  public String[] getAttachmentClassificationNameNotLike() {
    return attachmentClassificationNameNotLike;
  }

  public String[] getAttachmentChannelIn() {
    return attachmentChannelIn;
  }

  public String[] getAttachmentChannelNotIn() {
    return attachmentChannelNotIn;
  }

  public String[] getAttachmentChannelLike() {
    return attachmentChannelLike;
  }

  public String[] getAttachmentChannelNotLike() {
    return attachmentChannelNotLike;
  }

  public String[] getAttachmentReferenceIn() {
    return attachmentReferenceIn;
  }

  public String[] getAttachmentReferenceNotIn() {
    return attachmentReferenceNotIn;
  }

  public String[] getAttachmentReferenceLike() {
    return attachmentReferenceLike;
  }

  public String[] getAttachmentReferenceNotLike() {
    return attachmentReferenceNotLike;
  }

  public Instant[] getAttachmentReceivedWithin() {
    return attachmentReceivedWithin;
  }

  public Instant[] getAttachmentReceivedNotWithin() {
    return attachmentReceivedNotWithin;
  }

  public Boolean getWithoutAttachment() {
    return withoutAttachment;
  }

  public CallbackState[] getCallbackStateIn() {
    return callbackStateIn;
  }

  public CallbackState[] getCallbackStateNotIn() {
    return callbackStateNotIn;
  }

  public WildcardSearchField[] getWildcardSearchFieldIn() {
    return wildcardSearchFieldIn;
  }

  public String getWildcardSearchValue() {
    return wildcardSearchValue;
  }

  // endregion

  // region constructor

  public String[] getTaskIdNotIn() {
    return taskIdNotIn;
  }

  // endregion

  @Override
  public Void apply(TaskQuery query) {
    Optional.ofNullable(taskIdIn).ifPresent(query::idIn);
    Optional.ofNullable(taskIdNotIn).ifPresent(query::idNotIn);

    Optional.ofNullable(externalIdIn).ifPresent(query::externalIdIn);
    Optional.ofNullable(externalIdNotIn).ifPresent(query::externalIdNotIn);

    Optional.ofNullable(receivedWithin)
        .map(this::extractTimeIntervals)
        .ifPresent(query::receivedWithin);
    queryWithinInterval(query::receivedWithin, receivedFrom, receivedUntil);
    Optional.ofNullable(receivedNotIn)
        .map(this::extractTimeIntervals)
        .ifPresent(query::receivedNotWithin);
    queryWithinInterval(query::receivedNotWithin, receivedFromNot, receivedUntilNot);

    Optional.ofNullable(createdWithin)
        .map(this::extractTimeIntervals)
        .ifPresent(query::createdWithin);
    queryWithinInterval(query::createdWithin, createdFrom, createdUntil);
    Optional.ofNullable(createdNotWithin)
        .map(this::extractTimeIntervals)
        .ifPresent(query::createdNotWithin);
    queryWithinInterval(query::createdNotWithin, createdFromNot, createdUntilNot);

    Optional.ofNullable(claimedWithin)
        .map(this::extractTimeIntervals)
        .ifPresent(query::claimedWithin);
    Optional.ofNullable(claimedNotWithin)
        .map(this::extractTimeIntervals)
        .ifPresent(query::claimedNotWithin);

    Optional.ofNullable(modifiedWithin)
        .map(this::extractTimeIntervals)
        .ifPresent(query::modifiedWithin);
    Optional.ofNullable(modifiedNotWithin)
        .map(this::extractTimeIntervals)
        .ifPresent(query::modifiedNotWithin);

    Optional.ofNullable(plannedWithin)
        .map(this::extractTimeIntervals)
        .ifPresent(query::plannedWithin);
    queryWithinInterval(query::plannedWithin, plannedFrom, plannedUntil);
    Optional.ofNullable(plannedNotWithin)
        .map(this::extractTimeIntervals)
        .ifPresent(query::plannedNotWithin);
    queryWithinInterval(query::plannedNotWithin, plannedFromNot, plannedUntilNot);

    Optional.ofNullable(dueWithin).map(this::extractTimeIntervals).ifPresent(query::dueWithin);
    queryWithinInterval(query::dueWithin, dueFrom, dueUntil);
    Optional.ofNullable(dueNotWithin)
        .map(this::extractTimeIntervals)
        .ifPresent(query::dueNotWithin);
    queryWithinInterval(query::dueNotWithin, dueFromNot, dueUntilNot);

    Optional.ofNullable(completedWithin)
        .map(this::extractTimeIntervals)
        .ifPresent(query::completedWithin);
    queryWithinInterval(query::completedWithin, completedFrom, completedUntil);
    Optional.ofNullable(completedNotWithin)
        .map(this::extractTimeIntervals)
        .ifPresent(query::completedNotWithin);
    queryWithinInterval(query::completedNotWithin, completedFromNot, completedUntilNot);

    Optional.ofNullable(nameIn).ifPresent(query::nameIn);
    Optional.ofNullable(nameNotIn).ifPresent(query::nameNotIn);
    Optional.ofNullable(nameLike).map(this::wrapElementsInLikeStatement).ifPresent(query::nameLike);
    Optional.ofNullable(nameNotLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::nameNotLike);

    Optional.ofNullable(creatorIn).ifPresent(query::creatorIn);
    Optional.ofNullable(creatorNotIn).ifPresent(query::creatorNotIn);
    Optional.ofNullable(creatorLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::creatorLike);
    Optional.ofNullable(creatorNotLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::creatorNotLike);

    Optional.ofNullable(noteLike).map(this::wrapElementsInLikeStatement).ifPresent(query::noteLike);
    Optional.ofNullable(noteNotLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::noteNotLike);

    Optional.ofNullable(descriptionLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::descriptionLike);
    Optional.ofNullable(descriptionNotLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::descriptionNotLike);

    Optional.ofNullable(priorityIn).ifPresent(query::priorityIn);
    Optional.ofNullable(priorityNotIn).ifPresent(query::priorityNotIn);

    Optional.ofNullable(priorityWithin)
        .map(this::extractIntIntervals)
        .ifPresent(query::priorityWithin);
    queryWithinInterval(query::priorityWithin, priorityFrom, priorityUntil);
    Optional.ofNullable(priorityNotWithin)
        .map(this::extractIntIntervals)
        .ifPresent(query::priorityNotWithin);
    queryWithinInterval(query::priorityNotWithin, priorityNotFrom, priorityNotUntil);

    Optional.ofNullable(stateIn).ifPresent(query::stateIn);
    Optional.ofNullable(stateNotIn).ifPresent(query::stateNotIn);

    Optional.ofNullable(classificationIdIn).ifPresent(query::classificationIdIn);
    Optional.ofNullable(classificationIdNotIn).ifPresent(query::classificationIdNotIn);

    Optional.ofNullable(classificationKeyIn).ifPresent(query::classificationKeyIn);
    Optional.ofNullable(classificationKeyNotIn).ifPresent(query::classificationKeyNotIn);
    Optional.ofNullable(classificationKeyLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::classificationKeyLike);
    Optional.ofNullable(classificationKeyNotLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::classificationKeyNotLike);

    Optional.ofNullable(classificationParentKeyIn).ifPresent(query::classificationParentKeyIn);
    Optional.ofNullable(classificationParentKeyNotIn)
        .ifPresent(query::classificationParentKeyNotIn);
    Optional.ofNullable(classificationParentKeyLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::classificationParentKeyLike);
    Optional.ofNullable(classificationParentKeyNotLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::classificationParentKeyNotLike);

    Optional.ofNullable(classificationCategoryIn).ifPresent(query::classificationCategoryIn);
    Optional.ofNullable(classificationCategoryNotIn).ifPresent(query::classificationCategoryNotIn);
    Optional.ofNullable(classificationCategoryLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::classificationCategoryLike);
    Optional.ofNullable(classificationCategoryNotLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::classificationCategoryNotLike);

    Optional.ofNullable(classificationNameIn).ifPresent(query::classificationNameIn);
    Optional.ofNullable(classificationNameNotIn).ifPresent(query::classificationNameNotIn);
    Optional.ofNullable(classificationNameLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::classificationNameLike);
    Optional.ofNullable(classificationNameNotLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::classificationNameNotLike);

    Optional.ofNullable(workbasketIdIn).ifPresent(query::workbasketIdIn);
    Optional.ofNullable(workbasketIdNotIn).ifPresent(query::workbasketIdNotIn);

    Optional.ofNullable(workbasketKeyIn)
        .map(
            keys ->
                Arrays.stream(keys)
                    .map(key -> new KeyDomain(key, domain))
                    .toArray(KeyDomain[]::new))
        .ifPresent(query::workbasketKeyDomainIn);
    Optional.ofNullable(workbasketKeyNotIn)
        .map(
            keys ->
                Arrays.stream(keys)
                    .map(key -> new KeyDomain(key, domain))
                    .toArray(KeyDomain[]::new))
        .ifPresent(query::workbasketKeyDomainNotIn);

    Optional.ofNullable(businessProcessIdIn).ifPresent(query::businessProcessIdIn);
    Optional.ofNullable(businessProcessIdNot).ifPresent(query::businessProcessIdNotIn);
    Optional.ofNullable(businessProcessIdLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::businessProcessIdLike);
    Optional.ofNullable(businessProcessIdNotLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::businessProcessIdNotLike);

    Optional.ofNullable(parentBusinessProcessIdIn).ifPresent(query::parentBusinessProcessIdIn);
    Optional.ofNullable(parentBusinessProcessIdNotIn)
        .ifPresent(query::parentBusinessProcessIdNotIn);
    Optional.ofNullable(parentBusinessProcessIdLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::parentBusinessProcessIdLike);
    Optional.ofNullable(parentBusinessProcessIdNotLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::parentBusinessProcessIdNotLike);

    final String[] ownerInWithNull = addNullToOwnerInIfOwnerNullSet();
    Optional.ofNullable(ownerInWithNull).ifPresent(query::ownerIn);
    Optional.ofNullable(ownerNotIn).ifPresent(query::ownerNotIn);
    Optional.ofNullable(ownerLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::ownerLike);
    Optional.ofNullable(ownerNotLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::ownerNotLike);

    Optional.ofNullable(primaryObjectReferenceIn).ifPresent(query::primaryObjectReferenceIn);

    Optional.ofNullable(porCompanyIn).ifPresent(query::primaryObjectReferenceCompanyIn);
    Optional.ofNullable(porCompanyNotIn).ifPresent(query::primaryObjectReferenceCompanyNotIn);
    Optional.ofNullable(porCompanyLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::primaryObjectReferenceCompanyLike);
    Optional.ofNullable(porCompanyNotLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::primaryObjectReferenceCompanyNotLike);

    Optional.ofNullable(porSystemIn).ifPresent(query::primaryObjectReferenceSystemIn);
    Optional.ofNullable(porSystemNotIn).ifPresent(query::primaryObjectReferenceSystemNotIn);
    Optional.ofNullable(porSystemLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::primaryObjectReferenceSystemLike);
    Optional.ofNullable(porSystemNotLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::primaryObjectReferenceSystemNotLike);

    Optional.ofNullable(porInstanceIn).ifPresent(query::primaryObjectReferenceSystemInstanceIn);
    Optional.ofNullable(porInstanceNotIn)
        .ifPresent(query::primaryObjectReferenceSystemInstanceNotIn);
    Optional.ofNullable(porInstanceLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::primaryObjectReferenceSystemInstanceLike);
    Optional.ofNullable(porInstanceNotLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::primaryObjectReferenceSystemInstanceNotLike);

    Optional.ofNullable(porTypeIn).ifPresent(query::primaryObjectReferenceTypeIn);
    Optional.ofNullable(porTypeNotIn).ifPresent(query::primaryObjectReferenceTypeNotIn);
    Optional.ofNullable(porTypeLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::primaryObjectReferenceTypeLike);
    Optional.ofNullable(porTypeNotLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::primaryObjectReferenceTypeNotLike);

    Optional.ofNullable(porValueIn).ifPresent(query::primaryObjectReferenceValueIn);
    Optional.ofNullable(porValueNotIn).ifPresent(query::primaryObjectReferenceValueNotIn);
    Optional.ofNullable(porValueLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::primaryObjectReferenceValueLike);
    Optional.ofNullable(porValueNotLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::primaryObjectReferenceValueNotLike);

    Optional.ofNullable(secondaryObjectReferenceIn).ifPresent(query::secondaryObjectReferenceIn);

    Optional.ofNullable(sorCompanyIn).ifPresent(query::sorCompanyIn);
    Optional.ofNullable(sorCompanyLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::sorCompanyLike);

    Optional.ofNullable(sorSystemIn).ifPresent(query::sorSystemIn);
    Optional.ofNullable(sorSystemLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::sorSystemLike);

    Optional.ofNullable(sorInstanceIn).ifPresent(query::sorSystemInstanceIn);
    Optional.ofNullable(sorInstanceLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::sorSystemInstanceLike);
    Optional.ofNullable(sorTypeIn).ifPresent(query::sorTypeIn);
    Optional.ofNullable(sorTypeLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::sorTypeLike);
    Optional.ofNullable(sorValueIn).ifPresent(query::sorValueIn);
    Optional.ofNullable(sorValueLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::sorValueLike);

    Optional.ofNullable(read).ifPresent(query::readEquals);

    Optional.ofNullable(transferred).ifPresent(query::transferredEquals);

    Optional.ofNullable(reopened).ifPresent(query::reopenedEquals);

    Optional.ofNullable(hasComments).ifPresent(query::hasComments);

    Optional.ofNullable(attachmentClassificationIdIn)
        .ifPresent(query::attachmentClassificationIdIn);
    Optional.ofNullable(attachmentClassificationIdNotIn)
        .ifPresent(query::attachmentClassificationIdNotIn);

    Optional.ofNullable(attachmentClassificationKeyIn)
        .ifPresent(query::attachmentClassificationKeyIn);
    Optional.ofNullable(attachmentClassificationKeyNotIn)
        .ifPresent(query::attachmentClassificationKeyNotIn);
    Optional.ofNullable(attachmentClassificationKeyLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::attachmentClassificationKeyLike);
    Optional.ofNullable(attachmentClassificationKeyNotLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::attachmentClassificationKeyNotLike);

    Optional.ofNullable(attachmentClassificationNameIn)
        .ifPresent(query::attachmentClassificationNameIn);
    Optional.ofNullable(attachmentClassificationNameNotIn)
        .ifPresent(query::attachmentClassificationNameNotIn);
    Optional.ofNullable(attachmentClassificationNameLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::attachmentClassificationNameLike);
    Optional.ofNullable(attachmentClassificationNameNotLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::attachmentClassificationNameNotLike);

    Optional.ofNullable(attachmentChannelIn).ifPresent(query::attachmentChannelIn);
    Optional.ofNullable(attachmentChannelNotIn).ifPresent(query::attachmentChannelNotIn);
    Optional.ofNullable(attachmentChannelLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::attachmentChannelLike);
    Optional.ofNullable(attachmentChannelNotLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::attachmentChannelNotLike);

    Optional.ofNullable(attachmentReferenceIn).ifPresent(query::attachmentReferenceValueIn);
    Optional.ofNullable(attachmentReferenceNotIn).ifPresent(query::attachmentReferenceValueNotIn);
    Optional.ofNullable(attachmentReferenceLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::attachmentReferenceValueLike);
    Optional.ofNullable(attachmentReferenceNotLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::attachmentReferenceValueNotLike);

    Optional.ofNullable(attachmentReceivedWithin)
        .map(this::extractTimeIntervals)
        .ifPresent(query::attachmentReceivedWithin);
    Optional.ofNullable(attachmentReceivedNotWithin)
        .map(this::extractTimeIntervals)
        .ifPresent(query::attachmentNotReceivedWithin);

    if (Boolean.TRUE.equals(withoutAttachment)) {
      query.withoutAttachment();
    }

    Optional.ofNullable(callbackStateIn).ifPresent(query::callbackStateIn);
    Optional.ofNullable(callbackStateNotIn).ifPresent(query::callbackStateNotIn);

    if (wildcardSearchFieldIn != null) {
      query.wildcardSearchFieldsIn(wildcardSearchFieldIn);
      query.wildcardSearchValueLike("%" + wildcardSearchValue + "%");
    }

    return null;
  }

  /**
   * Returns {@linkplain #getOwnerIn() owner-in} and adds NULL to it if {@linkplain #getOwnerNull()
   * owner-is-null} is true.
   *
   * @return {@linkplain #getOwnerIn() owner-in} and adds NULL to it if {@linkplain #getOwnerNull()
   *     owner-is-null} is true
   * @throws InvalidArgumentException if {@linkplain #getOwnerNull() owner-is-null} has any
   *     non-blank value other than 'true'
   */
  private String[] addNullToOwnerInIfOwnerNullSet() throws InvalidArgumentException {
    if (ownerNull == null) {
      return ownerIn;
    }
    if (ownerNull.isBlank() || ownerNull.equalsIgnoreCase("true")) {
      return this.ownerIn == null ? new String[] {null} : ArrayUtils.add(this.ownerIn, null);
    } else {
      throw new InvalidArgumentException(
          String.format(
              "owner-is-null parameter with value '%s' is invalid.",
              LogSanitizer.stripLineBreakingChars(ownerNull)));
    }
  }

  private static void queryWithinInterval(Function<TimeInterval, TaskQuery> queryFunction,
      Instant from, Instant until) {
    if (from != null || until != null) {
      queryFunction.apply(new TimeInterval(from, until));
    }
  }

  private static void queryWithinInterval(Function<IntInterval, TaskQuery> queryFunction,
      Integer from, Integer until) {
    if (from != null || until != null) {
      queryFunction.apply(new IntInterval(from, until));
    }
  }
}
