package io.kadai.user.rest.assembler;

import io.kadai.common.rest.assembler.PagedRepresentationModelAssembler;
import io.kadai.common.rest.models.PageMetadata;
import io.kadai.user.api.models.UserSummary;
import io.kadai.user.internal.models.UserSummaryImpl;
import io.kadai.user.rest.models.UserSummaryPagedRepresentationModel;
import io.kadai.user.rest.models.UserSummaryRepresentationModel;
import java.util.Collection;
import org.springframework.stereotype.Component;

@Component
public class UserSummaryRepresentationModelAssembler
    implements PagedRepresentationModelAssembler<
        UserSummary, UserSummaryRepresentationModel, UserSummaryPagedRepresentationModel> {

  @Override
  public UserSummaryRepresentationModel toModel(UserSummary entity) {
    UserSummaryRepresentationModel repModel = new UserSummaryRepresentationModel();
    repModel.setUserId(entity.getId());
    repModel.setGroups(entity.getGroups());
    repModel.setPermissions(entity.getPermissions());
    repModel.setFirstName(entity.getFirstName());
    repModel.setLastName(entity.getLastName());
    repModel.setFullName(entity.getFullName());
    repModel.setLongName(entity.getLongName());
    repModel.setEmail(entity.getEmail());
    repModel.setPhone(entity.getPhone());
    repModel.setMobilePhone(entity.getMobilePhone());
    repModel.setOrgLevel4(entity.getOrgLevel4());
    repModel.setOrgLevel3(entity.getOrgLevel3());
    repModel.setOrgLevel2(entity.getOrgLevel2());
    repModel.setOrgLevel1(entity.getOrgLevel1());
    repModel.setDomains(entity.getDomains());

    return repModel;
  }

  public UserSummary toEntityModel(UserSummaryRepresentationModel repModel) {
    UserSummaryImpl user = new UserSummaryImpl();
    user.setId(repModel.getUserId());
    user.setGroups(repModel.getGroups());
    user.setPermissions(repModel.getPermissions());
    user.setFirstName(repModel.getFirstName());
    user.setLastName(repModel.getLastName());
    user.setFullName(repModel.getFullName());
    user.setLongName(repModel.getLongName());
    user.setEmail(repModel.getEmail());
    user.setPhone(repModel.getPhone());
    user.setMobilePhone(repModel.getMobilePhone());
    user.setOrgLevel4(repModel.getOrgLevel4());
    user.setOrgLevel3(repModel.getOrgLevel3());
    user.setOrgLevel2(repModel.getOrgLevel2());
    user.setOrgLevel1(repModel.getOrgLevel1());
    user.setDomains(repModel.getDomains());

    return user;
  }

  @Override
  public UserSummaryPagedRepresentationModel buildPageableEntity(
      Collection<UserSummaryRepresentationModel> content, PageMetadata pageMetadata) {
    return new UserSummaryPagedRepresentationModel(content, pageMetadata);
  }
}
