package io.kadai.user.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kadai.common.api.security.CurrentUserContext;
import io.kadai.common.rest.QueryParameter;
import io.kadai.user.api.UserQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import java.beans.ConstructorProperties;
import java.util.Optional;
import org.apache.commons.lang3.ArrayUtils;

/**
 * {@linkplain org.springdoc.core.annotations.ParameterObject @ParameterObject} for Query-Params
 * filtering {@linkplain io.kadai.user.api.models.User Users}.
 */
public class UserQueryFilterParameter implements QueryParameter<UserQuery, Void> {

  // region id

  @Schema(name = "current-user", description = "Filter by the current user.")
  @JsonProperty("current-user")
  private final String currentUser;

  // endregion

  // region current-user
  @Schema(name = "orgLevel1", description = "Filter by the org-level 1. This is an exact match.")
  @JsonProperty("orgLevel1")
  private final String[] orgLevel1;

  // endregion

  // region org-level
  @Schema(name = "orgLevel2", description = "Filter by the org-level 2. This is an exact match.")
  @JsonProperty("orgLevel2")
  private final String[] orgLevel2;

  @Schema(name = "orgLevel3", description = "Filter by the org-level 3. This is an exact match.")
  @JsonProperty("orgLevel3")
  private final String[] orgLevel3;

  @Schema(name = "orgLevel4", description = "Filter by the org-level 4. This is an exact match.")
  @JsonProperty("orgLevel4")
  private final String[] orgLevel4;

  @Schema(name = "user-id", description = "Filter by the users ids. This is an exact match.")
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
   */
  public void addCurrentUserIdIfPresentWithContext(CurrentUserContext currentUserContext) {
    final String currentUserId = currentUserContext.getUserid();
    if (currentUserId != null) {
      this.userIds = ArrayUtils.add(this.userIds, currentUserId);
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
