/*
 * Copyright [2025] [envite consulting GmbH]
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

package io.kadai.workbasket.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kadai.common.rest.QueryParameter;
import io.kadai.workbasket.api.WorkbasketAccessItemQuery;
import io.swagger.v3.oas.annotations.Parameter;
import java.beans.ConstructorProperties;
import java.util.Optional;

public class WorkbasketAccessItemQueryFilterParameter
    implements QueryParameter<WorkbasketAccessItemQuery, Void> {
  @Parameter(
      name = "workbasket-key",
      description = "Filter by the key of the Workbasket. This is an exact match.")
  @JsonProperty("workbasket-key")
  private final String[] workbasketKey;

  @Parameter(
      name = "workbasket-key-like",
      description =
          "Filter by the key of the Workbasket. This results in a substring search.. (% is appended"
              + " to the beginning and end of the requested value). Further SQL \"LIKE\" wildcard "
              + "characters will be resolved correctly.")
  @JsonProperty("workbasket-key-like")
  private final String[] workbasketKeyLike;

  @Parameter(
      name = "access-id",
      description = "Filter by the name of the access id. This is an exact match.")
  @JsonProperty("access-id")
  private final String[] accessId;

  @Parameter(
      name = "access-id-like",
      description =
          "Filter by the name of the access id. This results in a substring search.. (% is appended"
              + " to the beginning and end of the requested value). Further SQL \"LIKE\" wildcard "
              + "characters will be resolved correctly.")
  @JsonProperty("access-id-like")
  private final String[] accessIdLike;

  @ConstructorProperties({"workbasket-key", "workbasket-key-like", "access-id", "access-id-like"})
  public WorkbasketAccessItemQueryFilterParameter(
      String[] workbasketKey,
      String[] workbasketKeyLike,
      String[] accessId,
      String[] accessIdLike) {
    this.workbasketKey = workbasketKey;
    this.workbasketKeyLike = workbasketKeyLike;
    this.accessId = accessId;
    this.accessIdLike = accessIdLike;
  }

  public String[] getWorkbasketKey() {
    return workbasketKey;
  }

  public String[] getWorkbasketKeyLike() {
    return workbasketKeyLike;
  }

  public String[] getAccessId() {
    return accessId;
  }

  public String[] getAccessIdLike() {
    return accessIdLike;
  }

  @Override
  public Void apply(WorkbasketAccessItemQuery query) {
    Optional.ofNullable(workbasketKey).ifPresent(query::workbasketKeyIn);
    Optional.ofNullable(workbasketKeyLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::workbasketKeyLike);
    Optional.ofNullable(accessId).ifPresent(query::accessIdIn);
    Optional.ofNullable(accessIdLike)
        .map(this::wrapElementsInLikeStatement)
        .ifPresent(query::accessIdLike);
    return null;
  }
}
