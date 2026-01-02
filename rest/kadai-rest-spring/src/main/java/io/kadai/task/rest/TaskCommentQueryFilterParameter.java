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
import io.kadai.common.rest.QueryParameter;
import io.kadai.task.api.TaskCommentQuery;
import io.swagger.v3.oas.annotations.Parameter;
import java.beans.ConstructorProperties;
import java.time.Instant;
import java.util.Optional;

public class TaskCommentQueryFilterParameter implements QueryParameter<TaskCommentQuery, Void> {

  @Parameter(
      name = "id",
      description = "Filter by the id of the TaskComment. This is an exact match.")
  @JsonProperty("id")
  private final String[] idIn;

  @Parameter(
      name = "id-not",
      description =
          "Filter by what the id of the TaskComment shouldn't be. This is an exact match.")
  @JsonProperty("id-not")
  private final String[] idNotIn;

  @Parameter(
      name = "id-like",
      description =
          "Filter by the id of the TaskComment. This results in a substring search (% is appended "
              + "to the front and end of the requested value). Further SQL 'LIKE' wildcard "
              + "characters will be resolved correctly.")
  @JsonProperty("id-like")
  private final String[] idLike;

  @Parameter(
      name = "id-not-like",
      description =
          "Filter by what the id of the TaskComment shouldn't be. This results in a substring "
              + "search (% is appended to the front and end of the requested value). Further SQL "
              + "'LIKE' wildcard characters will be resolved correctly.")
  @JsonProperty("id-not-like")
  private final String[] idNotLike;

  @Parameter(
      name = "task-id",
      description = "Filter by the task id of the TaskComment. This is an exact match.")
  @JsonProperty("task-id")
  private final String[] taskIdIn;

  @Parameter(
      name = "creator",
      description = "Filter by the creator of the TaskComment. This is an exact match.")
  @JsonProperty("creator")
  private final String[] creatorIn;

  @Parameter(
      name = "creator-not",
      description =
          "Filter by what the creator of the TaskComment shouldn't be. This is an exact match.")
  @JsonProperty("creator-not")
  private final String[] creatorNotIn;

  @Parameter(
      name = "creator-like",
      description =
          "Filter by the creator of the TaskComment. This results in a substring search (% is "
              + "appended to the front and end of the requested value). Further SQL 'LIKE' wildcard"
              + " characters will be resolved correctly.")
  @JsonProperty("creator-like")
  private final String[] creatorLike;

  @Parameter(
      name = "creator-not-like",
      description =
          "Filter by what the creator of the TaskComment shouldn't be. This results in a substring "
              + "search (% is appended to the front and end of the requested value). Further SQL "
              + "'LIKE' wildcard characters will be resolved correctly.")
  @JsonProperty("creator-not-like")
  private final String[] creatorNotLike;

  @Parameter(
      name = "textfield-like",
      description =
          "Filter by the textfield of the TaskComment. This results in a substring search (% is "
              + "appended to the front and end of the requested value). Further SQL 'LIKE' wildcard"
              + " characters will be resolved correctly.")
  @JsonProperty("textfield-like")
  private final String[] textfieldLike;

  @Parameter(
      name = "textfield-not-like",
      description =
          "Filter by what the textfield of the TaskComment shouldn't be. This results in a "
              + "substring search (% is appended to the front and end of the requested value). "
              + "Further SQL 'LIKE' wildcard characters will be resolved correctly.")
  @JsonProperty("textfield-not-like")
  private final String[] textfieldNotLike;

  @Parameter(
      name = "modified",
      description =
          "Filter by a time interval within which the TaskComment was modified. To create an open "
              + "interval you can just leave it blank. The format is ISO-8601.")
  @JsonProperty("modified")
  private final Instant[] modifiedWithin;

  @Parameter(
      name = "modified-not",
      description =
          "Filter by a time interval within which the TaskComment wasn't modified. To create an "
              + "open interval you can just leave it blank. The format is ISO-8601.")
  @JsonProperty("modified-not")
  private final Instant[] modifiedNotWithin;

  @Parameter(
      name = "created",
      description =
          "Filter by a time interval within which the TaskComment was created. To create an open "
              + "interval you can just leave it blank. The format is ISO-8601.")
  @JsonProperty("created")
  private final Instant[] createdWithin;

  @Parameter(
      name = "created-not",
      description =
          "Filter by a time interval within which the TaskComment wasn't created. To create an "
              + "open interval you can just leave it blank. The format is ISO-8601.")
  @JsonProperty("created-not")
  private final Instant[] createdNotWithin;

  @SuppressWarnings("indentation")
  @ConstructorProperties({
    "id",
    "id-not",
    "id-like",
    "id-not-like",
    "task-id",
    "creator",
    "creator-not",
    "creator-like",
    "creator-not-like",
    "textfield-like",
    "textfield-not-like",
    "modified",
    "modified-not",
    "created",
    "created-not"
  })
  public TaskCommentQueryFilterParameter(
      String[] idIn,
      String[] idNotIn,
      String[] idLike,
      String[] idNotLike,
      String[] taskIdIn,
      String[] creatorIn,
      String[] creatorNotIn,
      String[] creatorLike,
      String[] creatorNotLike,
      String[] textfieldLike,
      String[] textfieldNotLike,
      Instant[] modifiedWithin,
      Instant[] modifiedNotWithin,
      Instant[] createdWithin,
      Instant[] createdNotWithin) {
    this.idIn = idIn;
    this.idNotIn = idNotIn;
    this.idLike = idLike;
    this.idNotLike = idNotLike;
    this.taskIdIn = taskIdIn;
    this.creatorIn = creatorIn;
    this.creatorNotIn = creatorNotIn;
    this.creatorLike = creatorLike;
    this.creatorNotLike = creatorNotLike;
    this.textfieldLike = textfieldLike;
    this.textfieldNotLike = textfieldNotLike;
    this.modifiedWithin = modifiedWithin;
    this.modifiedNotWithin = modifiedNotWithin;
    this.createdWithin = createdWithin;
    this.createdNotWithin = createdNotWithin;
  }

  @Override
  public Void apply(TaskCommentQuery query) {
    Optional.ofNullable(idIn).ifPresent(query::idIn);
    Optional.ofNullable(idNotIn).ifPresent(query::idNotIn);
    Optional.ofNullable(idLike).map(this::wrapElementsInLikeStatement).ifPresent(query::idLike);
    Optional.ofNullable(idNotLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::idNotLike);
    Optional.ofNullable(taskIdIn).ifPresent(query::taskIdIn);
    Optional.ofNullable(creatorIn).ifPresent(query::creatorIn);
    Optional.ofNullable(creatorNotIn).ifPresent(query::creatorNotIn);
    Optional.ofNullable(creatorLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::creatorLike);
    Optional.ofNullable(creatorNotLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::creatorNotLike);
    Optional.ofNullable(textfieldLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::textFieldLike);
    Optional.ofNullable(textfieldNotLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::textFieldNotLike);
    Optional.ofNullable(modifiedWithin)
        .map(this::extractTimeIntervals)
        .ifPresent(query::modifiedWithin);
    Optional.ofNullable(modifiedNotWithin)
        .map(this::extractTimeIntervals)
        .ifPresent(query::modifiedNotWithin);
    Optional.ofNullable(createdWithin)
        .map(this::extractTimeIntervals)
        .ifPresent(query::createdWithin);
    Optional.ofNullable(createdNotWithin)
        .map(this::extractTimeIntervals)
        .ifPresent(query::createdNotWithin);
    return null;
  }
}
