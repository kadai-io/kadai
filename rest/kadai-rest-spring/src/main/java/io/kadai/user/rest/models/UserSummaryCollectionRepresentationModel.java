package io.kadai.user.rest.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kadai.common.rest.models.CollectionRepresentationModel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.beans.ConstructorProperties;
import java.util.Collection;

public class UserSummaryCollectionRepresentationModel
    extends CollectionRepresentationModel<UserSummaryRepresentationModel> {
  @ConstructorProperties("users")
  public UserSummaryCollectionRepresentationModel(
      Collection<UserSummaryRepresentationModel> content) {
    super(content);
  }

  @Schema(name = "users", description = "The embedded users.")
  @JsonProperty("users")
  @Override
  public Collection<UserSummaryRepresentationModel> getContent() {
    return super.getContent();
  }
}
