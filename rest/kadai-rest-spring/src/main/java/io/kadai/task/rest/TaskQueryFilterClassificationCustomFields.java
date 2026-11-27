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

import static io.kadai.common.internal.util.Quadruple.of;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kadai.classification.api.ClassificationCustomField;
import io.kadai.common.internal.util.Pair;
import io.kadai.common.rest.QueryParameter;
import io.kadai.task.api.TaskQuery;
import io.swagger.v3.oas.annotations.Parameter;
import java.beans.ConstructorProperties;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Filters {@linkplain io.kadai.task.api.models.Task Tasks} by the custom attributes of their
 * current {@linkplain io.kadai.classification.api.models.Classification Classification}.
 */
public class TaskQueryFilterClassificationCustomFields implements QueryParameter<TaskQuery, Void> {

  @Parameter(
      name = "classification-custom-1",
      description =
          "Filter by the value of the field custom1 of the Classification of the Task. This is an "
              + "exact match.")
  @JsonProperty("classification-custom-1")
  private final String[] classificationCustom1In;

  @Parameter(
      name = "classification-custom-1-not",
      description =
          "Filter out by values of the field custom1 of the Classification of the Task. This is "
              + "an exact match. Tasks whose Classification has no value for custom1 are not "
              + "filtered out.")
  @JsonProperty("classification-custom-1-not")
  private final String[] classificationCustom1NotIn;

  @Parameter(
      name = "classification-custom-1-like",
      description =
          "Filter by the custom1 field of the Classification of the Task. This results in a "
              + "substring search (% is appended to the front and end of the requested value). "
              + "Further SQL \"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("classification-custom-1-like")
  private final String[] classificationCustom1Like;

  @Parameter(
      name = "classification-custom-1-not-like",
      description =
          "Filter by what the custom1 field of the Classification of the Task shouldn't be. This "
              + "results in a substring search (% is appended to the front and end of the "
              + "requested value). Further SQL \"LIKE\" wildcard characters will be resolved "
              + "correctly. Tasks whose Classification has no value for custom1 are filtered "
              + "out, too.")
  @JsonProperty("classification-custom-1-not-like")
  private final String[] classificationCustom1NotLike;

  @Parameter(
      name = "classification-custom-2",
      description =
          "Filter by the value of the field custom2 of the Classification of the Task. This is an "
              + "exact match.")
  @JsonProperty("classification-custom-2")
  private final String[] classificationCustom2In;

  @Parameter(
      name = "classification-custom-2-not",
      description =
          "Filter out by values of the field custom2 of the Classification of the Task. This is "
              + "an exact match. Tasks whose Classification has no value for custom2 are not "
              + "filtered out.")
  @JsonProperty("classification-custom-2-not")
  private final String[] classificationCustom2NotIn;

  @Parameter(
      name = "classification-custom-2-like",
      description =
          "Filter by the custom2 field of the Classification of the Task. This results in a "
              + "substring search (% is appended to the front and end of the requested value). "
              + "Further SQL \"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("classification-custom-2-like")
  private final String[] classificationCustom2Like;

  @Parameter(
      name = "classification-custom-2-not-like",
      description =
          "Filter by what the custom2 field of the Classification of the Task shouldn't be. This "
              + "results in a substring search (% is appended to the front and end of the "
              + "requested value). Further SQL \"LIKE\" wildcard characters will be resolved "
              + "correctly. Tasks whose Classification has no value for custom2 are filtered "
              + "out, too.")
  @JsonProperty("classification-custom-2-not-like")
  private final String[] classificationCustom2NotLike;

  @Parameter(
      name = "classification-custom-3",
      description =
          "Filter by the value of the field custom3 of the Classification of the Task. This is an "
              + "exact match.")
  @JsonProperty("classification-custom-3")
  private final String[] classificationCustom3In;

  @Parameter(
      name = "classification-custom-3-not",
      description =
          "Filter out by values of the field custom3 of the Classification of the Task. This is "
              + "an exact match. Tasks whose Classification has no value for custom3 are not "
              + "filtered out.")
  @JsonProperty("classification-custom-3-not")
  private final String[] classificationCustom3NotIn;

  @Parameter(
      name = "classification-custom-3-like",
      description =
          "Filter by the custom3 field of the Classification of the Task. This results in a "
              + "substring search (% is appended to the front and end of the requested value). "
              + "Further SQL \"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("classification-custom-3-like")
  private final String[] classificationCustom3Like;

  @Parameter(
      name = "classification-custom-3-not-like",
      description =
          "Filter by what the custom3 field of the Classification of the Task shouldn't be. This "
              + "results in a substring search (% is appended to the front and end of the "
              + "requested value). Further SQL \"LIKE\" wildcard characters will be resolved "
              + "correctly. Tasks whose Classification has no value for custom3 are filtered "
              + "out, too.")
  @JsonProperty("classification-custom-3-not-like")
  private final String[] classificationCustom3NotLike;

  @Parameter(
      name = "classification-custom-4",
      description =
          "Filter by the value of the field custom4 of the Classification of the Task. This is an "
              + "exact match.")
  @JsonProperty("classification-custom-4")
  private final String[] classificationCustom4In;

  @Parameter(
      name = "classification-custom-4-not",
      description =
          "Filter out by values of the field custom4 of the Classification of the Task. This is "
              + "an exact match. Tasks whose Classification has no value for custom4 are not "
              + "filtered out.")
  @JsonProperty("classification-custom-4-not")
  private final String[] classificationCustom4NotIn;

  @Parameter(
      name = "classification-custom-4-like",
      description =
          "Filter by the custom4 field of the Classification of the Task. This results in a "
              + "substring search (% is appended to the front and end of the requested value). "
              + "Further SQL \"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("classification-custom-4-like")
  private final String[] classificationCustom4Like;

  @Parameter(
      name = "classification-custom-4-not-like",
      description =
          "Filter by what the custom4 field of the Classification of the Task shouldn't be. This "
              + "results in a substring search (% is appended to the front and end of the "
              + "requested value). Further SQL \"LIKE\" wildcard characters will be resolved "
              + "correctly. Tasks whose Classification has no value for custom4 are filtered "
              + "out, too.")
  @JsonProperty("classification-custom-4-not-like")
  private final String[] classificationCustom4NotLike;

  @Parameter(
      name = "classification-custom-5",
      description =
          "Filter by the value of the field custom5 of the Classification of the Task. This is an "
              + "exact match.")
  @JsonProperty("classification-custom-5")
  private final String[] classificationCustom5In;

  @Parameter(
      name = "classification-custom-5-not",
      description =
          "Filter out by values of the field custom5 of the Classification of the Task. This is "
              + "an exact match. Tasks whose Classification has no value for custom5 are not "
              + "filtered out.")
  @JsonProperty("classification-custom-5-not")
  private final String[] classificationCustom5NotIn;

  @Parameter(
      name = "classification-custom-5-like",
      description =
          "Filter by the custom5 field of the Classification of the Task. This results in a "
              + "substring search (% is appended to the front and end of the requested value). "
              + "Further SQL \"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("classification-custom-5-like")
  private final String[] classificationCustom5Like;

  @Parameter(
      name = "classification-custom-5-not-like",
      description =
          "Filter by what the custom5 field of the Classification of the Task shouldn't be. This "
              + "results in a substring search (% is appended to the front and end of the "
              + "requested value). Further SQL \"LIKE\" wildcard characters will be resolved "
              + "correctly. Tasks whose Classification has no value for custom5 are filtered "
              + "out, too.")
  @JsonProperty("classification-custom-5-not-like")
  private final String[] classificationCustom5NotLike;

  @Parameter(
      name = "classification-custom-6",
      description =
          "Filter by the value of the field custom6 of the Classification of the Task. This is an "
              + "exact match.")
  @JsonProperty("classification-custom-6")
  private final String[] classificationCustom6In;

  @Parameter(
      name = "classification-custom-6-not",
      description =
          "Filter out by values of the field custom6 of the Classification of the Task. This is "
              + "an exact match. Tasks whose Classification has no value for custom6 are not "
              + "filtered out.")
  @JsonProperty("classification-custom-6-not")
  private final String[] classificationCustom6NotIn;

  @Parameter(
      name = "classification-custom-6-like",
      description =
          "Filter by the custom6 field of the Classification of the Task. This results in a "
              + "substring search (% is appended to the front and end of the requested value). "
              + "Further SQL \"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("classification-custom-6-like")
  private final String[] classificationCustom6Like;

  @Parameter(
      name = "classification-custom-6-not-like",
      description =
          "Filter by what the custom6 field of the Classification of the Task shouldn't be. This "
              + "results in a substring search (% is appended to the front and end of the "
              + "requested value). Further SQL \"LIKE\" wildcard characters will be resolved "
              + "correctly. Tasks whose Classification has no value for custom6 are filtered "
              + "out, too.")
  @JsonProperty("classification-custom-6-not-like")
  private final String[] classificationCustom6NotLike;

  @Parameter(
      name = "classification-custom-7",
      description =
          "Filter by the value of the field custom7 of the Classification of the Task. This is an "
              + "exact match.")
  @JsonProperty("classification-custom-7")
  private final String[] classificationCustom7In;

  @Parameter(
      name = "classification-custom-7-not",
      description =
          "Filter out by values of the field custom7 of the Classification of the Task. This is "
              + "an exact match. Tasks whose Classification has no value for custom7 are not "
              + "filtered out.")
  @JsonProperty("classification-custom-7-not")
  private final String[] classificationCustom7NotIn;

  @Parameter(
      name = "classification-custom-7-like",
      description =
          "Filter by the custom7 field of the Classification of the Task. This results in a "
              + "substring search (% is appended to the front and end of the requested value). "
              + "Further SQL \"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("classification-custom-7-like")
  private final String[] classificationCustom7Like;

  @Parameter(
      name = "classification-custom-7-not-like",
      description =
          "Filter by what the custom7 field of the Classification of the Task shouldn't be. This "
              + "results in a substring search (% is appended to the front and end of the "
              + "requested value). Further SQL \"LIKE\" wildcard characters will be resolved "
              + "correctly. Tasks whose Classification has no value for custom7 are filtered "
              + "out, too.")
  @JsonProperty("classification-custom-7-not-like")
  private final String[] classificationCustom7NotLike;

  @Parameter(
      name = "classification-custom-8",
      description =
          "Filter by the value of the field custom8 of the Classification of the Task. This is an "
              + "exact match.")
  @JsonProperty("classification-custom-8")
  private final String[] classificationCustom8In;

  @Parameter(
      name = "classification-custom-8-not",
      description =
          "Filter out by values of the field custom8 of the Classification of the Task. This is "
              + "an exact match. Tasks whose Classification has no value for custom8 are not "
              + "filtered out.")
  @JsonProperty("classification-custom-8-not")
  private final String[] classificationCustom8NotIn;

  @Parameter(
      name = "classification-custom-8-like",
      description =
          "Filter by the custom8 field of the Classification of the Task. This results in a "
              + "substring search (% is appended to the front and end of the requested value). "
              + "Further SQL \"LIKE\" wildcard characters will be resolved correctly.")
  @JsonProperty("classification-custom-8-like")
  private final String[] classificationCustom8Like;

  @Parameter(
      name = "classification-custom-8-not-like",
      description =
          "Filter by what the custom8 field of the Classification of the Task shouldn't be. This "
              + "results in a substring search (% is appended to the front and end of the "
              + "requested value). Further SQL \"LIKE\" wildcard characters will be resolved "
              + "correctly. Tasks whose Classification has no value for custom8 are filtered "
              + "out, too.")
  @JsonProperty("classification-custom-8-not-like")
  private final String[] classificationCustom8NotLike;

  @ConstructorProperties({
    "classification-custom-1",
    "classification-custom-1-not",
    "classification-custom-1-like",
    "classification-custom-1-not-like",
    "classification-custom-2",
    "classification-custom-2-not",
    "classification-custom-2-like",
    "classification-custom-2-not-like",
    "classification-custom-3",
    "classification-custom-3-not",
    "classification-custom-3-like",
    "classification-custom-3-not-like",
    "classification-custom-4",
    "classification-custom-4-not",
    "classification-custom-4-like",
    "classification-custom-4-not-like",
    "classification-custom-5",
    "classification-custom-5-not",
    "classification-custom-5-like",
    "classification-custom-5-not-like",
    "classification-custom-6",
    "classification-custom-6-not",
    "classification-custom-6-like",
    "classification-custom-6-not-like",
    "classification-custom-7",
    "classification-custom-7-not",
    "classification-custom-7-like",
    "classification-custom-7-not-like",
    "classification-custom-8",
    "classification-custom-8-not",
    "classification-custom-8-like",
    "classification-custom-8-not-like"
  })
  public TaskQueryFilterClassificationCustomFields(
      String[] classificationCustom1In,
      String[] classificationCustom1NotIn,
      String[] classificationCustom1Like,
      String[] classificationCustom1NotLike,
      String[] classificationCustom2In,
      String[] classificationCustom2NotIn,
      String[] classificationCustom2Like,
      String[] classificationCustom2NotLike,
      String[] classificationCustom3In,
      String[] classificationCustom3NotIn,
      String[] classificationCustom3Like,
      String[] classificationCustom3NotLike,
      String[] classificationCustom4In,
      String[] classificationCustom4NotIn,
      String[] classificationCustom4Like,
      String[] classificationCustom4NotLike,
      String[] classificationCustom5In,
      String[] classificationCustom5NotIn,
      String[] classificationCustom5Like,
      String[] classificationCustom5NotLike,
      String[] classificationCustom6In,
      String[] classificationCustom6NotIn,
      String[] classificationCustom6Like,
      String[] classificationCustom6NotLike,
      String[] classificationCustom7In,
      String[] classificationCustom7NotIn,
      String[] classificationCustom7Like,
      String[] classificationCustom7NotLike,
      String[] classificationCustom8In,
      String[] classificationCustom8NotIn,
      String[] classificationCustom8Like,
      String[] classificationCustom8NotLike) {
    this.classificationCustom1In = classificationCustom1In;
    this.classificationCustom1NotIn = classificationCustom1NotIn;
    this.classificationCustom1Like = classificationCustom1Like;
    this.classificationCustom1NotLike = classificationCustom1NotLike;
    this.classificationCustom2In = classificationCustom2In;
    this.classificationCustom2NotIn = classificationCustom2NotIn;
    this.classificationCustom2Like = classificationCustom2Like;
    this.classificationCustom2NotLike = classificationCustom2NotLike;
    this.classificationCustom3In = classificationCustom3In;
    this.classificationCustom3NotIn = classificationCustom3NotIn;
    this.classificationCustom3Like = classificationCustom3Like;
    this.classificationCustom3NotLike = classificationCustom3NotLike;
    this.classificationCustom4In = classificationCustom4In;
    this.classificationCustom4NotIn = classificationCustom4NotIn;
    this.classificationCustom4Like = classificationCustom4Like;
    this.classificationCustom4NotLike = classificationCustom4NotLike;
    this.classificationCustom5In = classificationCustom5In;
    this.classificationCustom5NotIn = classificationCustom5NotIn;
    this.classificationCustom5Like = classificationCustom5Like;
    this.classificationCustom5NotLike = classificationCustom5NotLike;
    this.classificationCustom6In = classificationCustom6In;
    this.classificationCustom6NotIn = classificationCustom6NotIn;
    this.classificationCustom6Like = classificationCustom6Like;
    this.classificationCustom6NotLike = classificationCustom6NotLike;
    this.classificationCustom7In = classificationCustom7In;
    this.classificationCustom7NotIn = classificationCustom7NotIn;
    this.classificationCustom7Like = classificationCustom7Like;
    this.classificationCustom7NotLike = classificationCustom7NotLike;
    this.classificationCustom8In = classificationCustom8In;
    this.classificationCustom8NotIn = classificationCustom8NotIn;
    this.classificationCustom8Like = classificationCustom8Like;
    this.classificationCustom8NotLike = classificationCustom8NotLike;
  }

  public String[] getClassificationCustom1In() {
    return classificationCustom1In;
  }

  public String[] getClassificationCustom1NotIn() {
    return classificationCustom1NotIn;
  }

  public String[] getClassificationCustom1Like() {
    return classificationCustom1Like;
  }

  public String[] getClassificationCustom1NotLike() {
    return classificationCustom1NotLike;
  }

  public String[] getClassificationCustom2In() {
    return classificationCustom2In;
  }

  public String[] getClassificationCustom2NotIn() {
    return classificationCustom2NotIn;
  }

  public String[] getClassificationCustom2Like() {
    return classificationCustom2Like;
  }

  public String[] getClassificationCustom2NotLike() {
    return classificationCustom2NotLike;
  }

  public String[] getClassificationCustom3In() {
    return classificationCustom3In;
  }

  public String[] getClassificationCustom3NotIn() {
    return classificationCustom3NotIn;
  }

  public String[] getClassificationCustom3Like() {
    return classificationCustom3Like;
  }

  public String[] getClassificationCustom3NotLike() {
    return classificationCustom3NotLike;
  }

  public String[] getClassificationCustom4In() {
    return classificationCustom4In;
  }

  public String[] getClassificationCustom4NotIn() {
    return classificationCustom4NotIn;
  }

  public String[] getClassificationCustom4Like() {
    return classificationCustom4Like;
  }

  public String[] getClassificationCustom4NotLike() {
    return classificationCustom4NotLike;
  }

  public String[] getClassificationCustom5In() {
    return classificationCustom5In;
  }

  public String[] getClassificationCustom5NotIn() {
    return classificationCustom5NotIn;
  }

  public String[] getClassificationCustom5Like() {
    return classificationCustom5Like;
  }

  public String[] getClassificationCustom5NotLike() {
    return classificationCustom5NotLike;
  }

  public String[] getClassificationCustom6In() {
    return classificationCustom6In;
  }

  public String[] getClassificationCustom6NotIn() {
    return classificationCustom6NotIn;
  }

  public String[] getClassificationCustom6Like() {
    return classificationCustom6Like;
  }

  public String[] getClassificationCustom6NotLike() {
    return classificationCustom6NotLike;
  }

  public String[] getClassificationCustom7In() {
    return classificationCustom7In;
  }

  public String[] getClassificationCustom7NotIn() {
    return classificationCustom7NotIn;
  }

  public String[] getClassificationCustom7Like() {
    return classificationCustom7Like;
  }

  public String[] getClassificationCustom7NotLike() {
    return classificationCustom7NotLike;
  }

  public String[] getClassificationCustom8In() {
    return classificationCustom8In;
  }

  public String[] getClassificationCustom8NotIn() {
    return classificationCustom8NotIn;
  }

  public String[] getClassificationCustom8Like() {
    return classificationCustom8Like;
  }

  public String[] getClassificationCustom8NotLike() {
    return classificationCustom8NotLike;
  }

  @Override
  public Void apply(TaskQuery query) {
    Stream.of(
            Pair.of(
                ClassificationCustomField.CUSTOM_1,
                of(
                    classificationCustom1In,
                    classificationCustom1NotIn,
                    classificationCustom1Like,
                    classificationCustom1NotLike)),
            Pair.of(
                ClassificationCustomField.CUSTOM_2,
                of(
                    classificationCustom2In,
                    classificationCustom2NotIn,
                    classificationCustom2Like,
                    classificationCustom2NotLike)),
            Pair.of(
                ClassificationCustomField.CUSTOM_3,
                of(
                    classificationCustom3In,
                    classificationCustom3NotIn,
                    classificationCustom3Like,
                    classificationCustom3NotLike)),
            Pair.of(
                ClassificationCustomField.CUSTOM_4,
                of(
                    classificationCustom4In,
                    classificationCustom4NotIn,
                    classificationCustom4Like,
                    classificationCustom4NotLike)),
            Pair.of(
                ClassificationCustomField.CUSTOM_5,
                of(
                    classificationCustom5In,
                    classificationCustom5NotIn,
                    classificationCustom5Like,
                    classificationCustom5NotLike)),
            Pair.of(
                ClassificationCustomField.CUSTOM_6,
                of(
                    classificationCustom6In,
                    classificationCustom6NotIn,
                    classificationCustom6Like,
                    classificationCustom6NotLike)),
            Pair.of(
                ClassificationCustomField.CUSTOM_7,
                of(
                    classificationCustom7In,
                    classificationCustom7NotIn,
                    classificationCustom7Like,
                    classificationCustom7NotLike)),
            Pair.of(
                ClassificationCustomField.CUSTOM_8,
                of(
                    classificationCustom8In,
                    classificationCustom8NotIn,
                    classificationCustom8Like,
                    classificationCustom8NotLike)))
        .forEach(pair -> {
          ClassificationCustomField customField = pair.getLeft();
          Optional.ofNullable(pair.getRight().getFirst())
              .ifPresent(l -> query.classificationCustomAttributeIn(customField, l));
          Optional.ofNullable(pair.getRight().getSecond())
              .ifPresent(l -> query.classificationCustomAttributeNotIn(customField, l));
          Optional.ofNullable(pair.getRight().getThird())
              .map(this::wrapElementsInLikeStatement)
              .ifPresent(l -> query.classificationCustomAttributeLike(customField, l));
          Optional.ofNullable(pair.getRight().getFourth())
              .map(this::wrapElementsInLikeStatement)
              .ifPresent(l -> query.classificationCustomAttributeNotLike(customField, l));
        });
    return null;
  }
}
