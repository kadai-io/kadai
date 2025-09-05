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

package io.kadai.user.rest.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.rest.test.KadaiSpringBootTest;
import io.kadai.user.api.UserService;
import io.kadai.user.api.models.User;
import io.kadai.user.internal.models.UserImpl;
import io.kadai.user.rest.models.UserRepresentationModel;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Test for {@linkplain UserRepresentationModelAssembler}. */
@KadaiSpringBootTest
class UserRepresentationModelAssemblerTest {
  private final UserRepresentationModelAssembler assembler;
  private final UserService userService;

  @Autowired
  UserRepresentationModelAssemblerTest(
      UserRepresentationModelAssembler assembler, UserService userService) {
    this.assembler = assembler;
    this.userService = userService;
  }

  private static void testEquality(User entity, UserRepresentationModel repModel) {
    UserSummaryRepresentationModelAssemblerTest.testEquality(entity, repModel);
  }

  @Test
  void should_ReturnRepresentationModel_When_ConvertingUserEntityToRepresentationModel() {
    UserImpl user = (UserImpl) userService.newUser();
    user.setId("user-1-2");
    user.setGroups(Set.of("group1", "group2"));
    user.setPermissions(Set.of("perm1", "perm2"));
    user.setFirstName("Hans");
    user.setLastName("Georg");
    user.setFullName("Hans Georg");
    user.setLongName("Georg, Hans - user-1-2");
    user.setEmail("hans.georg@web.com");
    user.setPhone("1234");
    user.setMobilePhone("01574275632");
    user.setOrgLevel4("Envite");
    user.setOrgLevel3("BPM");
    user.setOrgLevel2("Human Workflow");
    user.setOrgLevel1("KADAI");
    user.setData("xy");
    user.setDomains(Set.of("DOMAIN_A", "DOMAIN_B"));

    UserRepresentationModel repModel = assembler.toModel(user);
    testEquality(user, repModel);
  }

  @Test
  void should_ReturnEntity_When_ConvertingUserRepresentationModelToEntity() {
    UserRepresentationModel repModel = new UserRepresentationModel();
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
    repModel.setData("xy");
    repModel.setDomains(Set.of("DOMAIN_A", "DOMAIN_B"));

    User user = assembler.toEntityModel(repModel);
    testEquality(user, repModel);
  }

  @Test
  void should_BeEqual_When_ConvertingEntityToRepModelAndBackToEntity() {
    UserImpl user = (UserImpl) userService.newUser();
    user.setId("user-1-2");
    user.setGroups(Set.of("group1", "group2"));
    user.setPermissions(Set.of("perm1", "perm2"));
    user.setFirstName("Hans");
    user.setLastName("Georg");
    user.setFullName("Hans Georg");
    user.setLongName("Georg, Hans - user-1-2");
    user.setEmail("hans.georg@web.com");
    user.setPhone("1234");
    user.setMobilePhone("01574275632");
    user.setOrgLevel4("Envite");
    user.setOrgLevel3("BPM");
    user.setOrgLevel2("Human Workflow");
    user.setOrgLevel1("KADAI");
    user.setData("xy");
    user.setDomains(Set.of("DOMAIN_A", "DOMAIN_B"));

    UserRepresentationModel repModel = assembler.toModel(user);
    User userAfterConversion = assembler.toEntityModel(repModel);

    assertThat(user)
        .hasNoNullFieldsOrProperties()
        .isNotSameAs(userAfterConversion)
        .isEqualTo(userAfterConversion);
  }
}
