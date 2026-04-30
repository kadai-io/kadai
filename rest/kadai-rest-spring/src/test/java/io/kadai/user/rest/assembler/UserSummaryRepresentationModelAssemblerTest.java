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

package io.kadai.user.rest.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.common.rest.models.PageMetadata;
import io.kadai.rest.test.KadaiSpringBootTest;
import io.kadai.user.api.UserService;
import io.kadai.user.api.models.UserSummary;
import io.kadai.user.internal.models.UserSummaryImpl;
import io.kadai.user.rest.models.UserSummaryPagedRepresentationModel;
import io.kadai.user.rest.models.UserSummaryRepresentationModel;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Test for {@linkplain UserSummaryRepresentationModelAssembler}. */
@KadaiSpringBootTest
class UserSummaryRepresentationModelAssemblerTest {
  private final UserSummaryRepresentationModelAssembler assembler;
  private final UserService userService;

  @Autowired
  UserSummaryRepresentationModelAssemblerTest(
      UserSummaryRepresentationModelAssembler assembler, UserService userService) {
    this.assembler = assembler;
    this.userService = userService;
  }

  static void testEquality(UserSummary entity, UserSummaryRepresentationModel repModel) {
    assertThat(entity).hasNoNullFieldsOrProperties();
    assertThat(repModel).hasNoNullFieldsOrProperties();

    assertThat(entity.getId()).isEqualTo(repModel.getUserId());
    assertThat(entity.getGroups()).isEqualTo(repModel.getGroups());
    assertThat(entity.getPermissions()).isEqualTo(repModel.getPermissions());
    assertThat(entity.getFirstName()).isEqualTo(repModel.getFirstName());
    assertThat(entity.getLastName()).isEqualTo(repModel.getLastName());
    assertThat(entity.getFullName()).isEqualTo(repModel.getFullName());
    assertThat(entity.getLongName()).isEqualTo(repModel.getLongName());
    assertThat(entity.getEmail()).isEqualTo(repModel.getEmail());
    assertThat(entity.getPhone()).isEqualTo(repModel.getPhone());
    assertThat(entity.getMobilePhone()).isEqualTo(repModel.getMobilePhone());
    assertThat(entity.getOrgLevel4()).isEqualTo(repModel.getOrgLevel4());
    assertThat(entity.getOrgLevel3()).isEqualTo(repModel.getOrgLevel3());
    assertThat(entity.getOrgLevel2()).isEqualTo(repModel.getOrgLevel2());
    assertThat(entity.getOrgLevel1()).isEqualTo(repModel.getOrgLevel1());
    assertThat(entity.getDomains()).isEqualTo(repModel.getDomains());
  }

  @Test
  void should_ReturnRepresentationModel_When_ConvertingUserEntityToRepresentationModel() {
    UserSummaryImpl userSummary = (UserSummaryImpl) userService.newUser().asSummary();
    userSummary.setId("user-1-2");
    userSummary.setGroups(Set.of("group1", "group2"));
    userSummary.setPermissions(Set.of("perm1", "perm2"));
    userSummary.setFirstName("Hans");
    userSummary.setLastName("Georg");
    userSummary.setFullName("Hans Georg");
    userSummary.setLongName("Georg, Hans - user-1-2");
    userSummary.setEmail("hans.georg@web.com");
    userSummary.setPhone("1234");
    userSummary.setMobilePhone("01574275632");
    userSummary.setOrgLevel4("Envite");
    userSummary.setOrgLevel3("BPM");
    userSummary.setOrgLevel2("Human Workflow");
    userSummary.setOrgLevel1("KADAI");
    userSummary.setDomains(Set.of("DOMAIN_A", "DOMAIN_B"));

    UserSummaryRepresentationModel repModel = assembler.toModel(userSummary);
    testEquality(userSummary, repModel);
  }

  @Test
  void should_ReturnEntity_When_ConvertingUserRepresentationModelToEntity() {
    UserSummaryRepresentationModel repModel = new UserSummaryRepresentationModel();
    repModel.setUserId("user-1-2");
    repModel.setGroups(Set.of("group1", "group2"));
    repModel.setPermissions(Set.of("perm1", "perm2"));
    repModel.setFirstName("Hans");
    repModel.setLastName("Georg");
    repModel.setFullName("Hans Georg");
    repModel.setLongName("Georg, Hans - user-1-2");
    repModel.setEmail("hans.georg@web.com");
    repModel.setPhone("1234");
    repModel.setMobilePhone("01574275632");
    repModel.setOrgLevel4("Envite");
    repModel.setOrgLevel3("BPM");
    repModel.setOrgLevel2("Human Workflow");
    repModel.setOrgLevel1("KADAI");
    repModel.setDomains(Set.of("DOMAIN_A", "DOMAIN_B"));

    UserSummary userSummary = assembler.toEntityModel(repModel);
    testEquality(userSummary, repModel);
  }

  @Test
  void should_BeEqual_When_ConvertingEntityToRepModelAndBackToEntity() {
    UserSummaryImpl userSummary = (UserSummaryImpl) userService.newUser().asSummary();
    userSummary.setId("user-1-2");
    userSummary.setGroups(Set.of("group1", "group2"));
    userSummary.setPermissions(Set.of("perm1", "perm2"));
    userSummary.setFirstName("Hans");
    userSummary.setLastName("Georg");
    userSummary.setFullName("Hans Georg");
    userSummary.setLongName("Georg, Hans - user-1-2");
    userSummary.setEmail("hans.georg@web.com");
    userSummary.setPhone("1234");
    userSummary.setMobilePhone("01574275632");
    userSummary.setOrgLevel4("Envite");
    userSummary.setOrgLevel3("BPM");
    userSummary.setOrgLevel2("Human Workflow");
    userSummary.setOrgLevel1("KADAI");
    userSummary.setDomains(Set.of("DOMAIN_A", "DOMAIN_B"));

    UserSummaryRepresentationModel repModel = assembler.toModel(userSummary);
    UserSummary userAfterConversion = assembler.toEntityModel(repModel);

    assertThat(userSummary)
        .hasNoNullFieldsOrProperties()
        .isNotSameAs(userAfterConversion)
        .isEqualTo(userAfterConversion);
  }

  @Test
  void should_ReturnPagedRepresentationModel_When_BuildingPageableEntity() {
    UserSummaryRepresentationModel user1 = new UserSummaryRepresentationModel();
    user1.setUserId("user-1");
    user1.setFirstName("John");
    user1.setLastName("Doe");

    UserSummaryRepresentationModel user2 = new UserSummaryRepresentationModel();
    user2.setUserId("user-2");
    user2.setFirstName("Jane");
    user2.setLastName("Doe");

    Collection<UserSummaryRepresentationModel> content = List.of(user1, user2);
    PageMetadata pageMetadata = new PageMetadata(2, 1, 2, 1);

    UserSummaryPagedRepresentationModel pagedModel =
        assembler.buildPageableEntity(content, pageMetadata);

    assertThat(pagedModel).isNotNull();
    assertThat(pagedModel.getContent()).containsExactlyInAnyOrder(user1, user2);
    assertThat(pagedModel.getPageMetadata()).isEqualTo(pageMetadata);
  }
}
