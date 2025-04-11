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

package io.kadai.user.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.security.CurrentUserContext;
import io.kadai.common.internal.util.LogSanitizer;
import io.kadai.common.rest.QueryParameter;
import io.kadai.user.api.UserQuery;
import io.swagger.v3.oas.annotations.Parameter;
import java.beans.ConstructorProperties;
import java.util.Optional;
import org.apache.commons.lang3.ArrayUtils;

/**
 * {@linkplain org.springdoc.core.annotations.ParameterObject @ParameterObject} for Query-Params
 * filtering {@linkplain io.kadai.user.api.models.User Users}.
 */
public class UserQueryFilterParameter implements QueryParameter<UserQuery, Void> {

  // region id

  @Parameter(
      name = "current-user",
      description =
          "Filter by the current user. Either use it as a Query-Flag without any value, "
              + "with the empty value \"\" or with the value \"true\".",
      allowEmptyValue = true)
  @JsonProperty("current-user")
  private final String currentUser;

  // endregion

  // region current-user
  @Parameter(name = "orgLevel1", description = "Filter by the org-level 1. This is an exact match.")
  @JsonProperty("orgLevel1")
  private final String[] orgLevel1;

  // endregion

  // region org-level
  @Parameter(name = "orgLevel2", description = "Filter by the org-level 2. This is an exact match.")
  @JsonProperty("orgLevel2")
  private final String[] orgLevel2;

  @Parameter(name = "orgLevel3", description = "Filter by the org-level 3. This is an exact match.")
  @JsonProperty("orgLevel3")
  private final String[] orgLevel3;

  @Parameter(name = "orgLevel4", description = "Filter by the org-level 4. This is an exact match.")
  @JsonProperty("orgLevel4")
  private final String[] orgLevel4;

  @Parameter(name = "user-id", description = "Filter by the users ids. This is an exact match.")
  @JsonProperty("user-id")
  private String[] userIds;

  // endregion

  @ConstructorProperties({
    "user-id",
    "current-user",
    "orgLevel1",
    "orgLevel2",
    "orgLevel3",
    "orgLevel4"
  })
  public UserQueryFilterParameter(
      String[] userIds,
      String currentUser,
      String[] orgLevel1,
      String[] orgLevel2,
      String[] orgLevel3,
      String[] orgLevel4) {
    this.userIds = userIds;
    this.currentUser = currentUser;
    this.orgLevel1 = orgLevel1;
    this.orgLevel2 = orgLevel2;
    this.orgLevel3 = orgLevel3;
    this.orgLevel4 = orgLevel4;
  }

  /**
   * Adds the id of the {@linkplain #getCurrentUser() current user} to the {@linkplain #getUserIds()
   * userIds}.
   *
   * @param currentUserContext the context this {@linkplain
   *     org.springdoc.core.annotations.ParameterObject @ParameterObject} is served from.
   * @throws InvalidArgumentException if {@linkplain #getCurrentUser() current-user} has any
   *     non-blank value other than 'true'
   */
  public void addCurrentUserIdIfPresentWithContext(CurrentUserContext currentUserContext)
      throws InvalidArgumentException {
    if (currentUser == null) {
      return;
    }
    if (currentUser.isBlank() || currentUser.equalsIgnoreCase("true")) {
      final String currentUserId = currentUserContext.getUserid();
      if (currentUserId != null) {
        this.userIds = ArrayUtils.add(this.userIds, currentUserId);
      }
    } else {
      throw new InvalidArgumentException(
          String.format(
              "current-user parameter '%s' with value is invalid.",
              LogSanitizer.stripLineBreakingChars(currentUser)));
    }
  }

  @Override
  public Void apply(UserQuery entity) {
    Optional.ofNullable(this.userIds).ifPresent(entity::idIn);
    Optional.ofNullable(this.orgLevel1).ifPresent(entity::orgLevel1In);
    Optional.ofNullable(this.orgLevel2).ifPresent(entity::orgLevel2In);
    Optional.ofNullable(this.orgLevel3).ifPresent(entity::orgLevel3In);
    Optional.ofNullable(this.orgLevel4).ifPresent(entity::orgLevel4In);

    return null;
  }

  public String[] getUserIds() {
    return userIds;
  }

  public String getCurrentUser() {
    return currentUser;
  }

  public String[] getOrgLevel1() {
    return orgLevel1;
  }

  public String[] getOrgLevel2() {
    return orgLevel2;
  }

  public String[] getOrgLevel3() {
    return orgLevel3;
  }

  public String[] getOrgLevel4() {
    return orgLevel4;
  }
}
