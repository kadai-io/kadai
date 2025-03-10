package io.kadai.user.rest.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.beans.ConstructorProperties;
import java.util.Collection;

import io.kadai.common.rest.models.PageMetadata;
import io.kadai.common.rest.models.PagedRepresentationModel;

public class UserPagedRepresentationModel extends
    PagedRepresentationModel<UserRepresentationModel> {

  @ConstructorProperties({"users", "page"})
  public UserPagedRepresentationModel(
      Collection<UserRepresentationModel> content, PageMetadata pageMetadata) {
    super(content, pageMetadata);
  }

  @Schema(name = "users", description = "the embedded users.")
  @JsonProperty("users")
  @Override
  public Collection<UserRepresentationModel> getContent() {
    return super.getContent();
  }
}
