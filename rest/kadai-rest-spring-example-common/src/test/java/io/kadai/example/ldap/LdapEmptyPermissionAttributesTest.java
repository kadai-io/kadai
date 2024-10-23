/*
 * Copyright [2024] [envite consulting GmbH]
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

package io.kadai.example.ldap;

import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.common.rest.ldap.LdapClient;
import io.kadai.common.rest.models.AccessIdRepresentationModel;
import io.kadai.rest.test.KadaiSpringBootTest;
import io.kadai.user.api.models.User;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@KadaiSpringBootTest
@ActiveProfiles({"emptyPermissionAttributes"})
class LdapEmptyPermissionAttributesTest {
  @Autowired LdapClient ldapClient;

  @Test
  void should_FindGroupsForUser_When_UserIdIsProvided() throws Exception {
    List<AccessIdRepresentationModel> groups =
        ldapClient.searchGroupsAccessIdIsMemberOf("user-2-2");
    assertThat(groups)
        .extracting(AccessIdRepresentationModel::getAccessId)
        .containsExactlyInAnyOrder("cn=ksc-users,cn=groups,ou=test,o=kadai");
  }

  @Test
  void should_FindNoPermissionsForUser_When_UserIdIsProvided() throws Exception {
    List<AccessIdRepresentationModel> permissions =
        ldapClient.searchPermissionsAccessIdHas("user-1-2");
    assertThat(permissions).isEmpty();
  }

  @Test
  void should_FindAllUsersAndGroups_When_SearchWithSubstringOfName() throws Exception {
    List<AccessIdRepresentationModel> usersGroupsPermissions =
        ldapClient.searchUsersAndGroupsAndPermissions("lead");
    assertThat(usersGroupsPermissions)
        .extracting(AccessIdRepresentationModel::getAccessId)
        .containsExactlyInAnyOrder("teamlead-1", "teamlead-2",
            "cn=ksc-teamleads,cn=groups,ou=test,o=kadai");
  }

  @Test
  void should_FindUser_When_SearchingWithFirstAndLastname() throws Exception {
    List<AccessIdRepresentationModel> usersGroupsPermissions =
        ldapClient.searchUsersAndGroupsAndPermissions("Elena");
    assertThat(usersGroupsPermissions).hasSize(2);

    usersGroupsPermissions = ldapClient.searchUsersAndGroupsAndPermissions("Elena Faul");
    assertThat(usersGroupsPermissions).hasSize(1);
  }

  @Test
  void should_ReturnAllUsersInUserRoleWithCorrectAttributes() {

    Map<String, User> users =
        ldapClient.searchUsersInUserRole().stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));

    assertThat(users).hasSize(8);

    User teamlead1 = users.get("teamlead-1");
    assertThat(teamlead1.getId()).isEqualTo("teamlead-1");
    assertThat(teamlead1.getFirstName()).isEqualTo("Titus");
    assertThat(teamlead1.getLastName()).isEqualTo("Toll");
    assertThat(teamlead1.getFullName()).isEqualTo("Titus Toll");
    assertThat(teamlead1.getEmail()).isEqualTo("Titus.Toll@kadai.de");
    assertThat(teamlead1.getPhone()).isEqualTo("012345678");
    assertThat(teamlead1.getMobilePhone()).isEqualTo("09876554321");
    assertThat(teamlead1.getOrgLevel1()).isEqualTo("ABC");
    assertThat(teamlead1.getOrgLevel2()).isEqualTo("DEF/GHI");
    assertThat(teamlead1.getOrgLevel3()).isEqualTo("JKL");
    assertThat(teamlead1.getOrgLevel4()).isEqualTo("MNO/PQR");

    User user11 = users.get("user-1-1");
    assertThat(user11.getId()).isEqualTo("user-1-1");
    assertThat(user11.getFirstName()).isEqualTo("Max");
    assertThat(user11.getLastName()).isEqualTo("Mustermann");
    assertThat(user11.getFullName()).isEqualTo("Max Mustermann");
    assertThat(user11.getEmail()).isNull();
    assertThat(user11.getPhone()).isNull();
    assertThat(user11.getMobilePhone()).isNull();
    assertThat(user11.getOrgLevel1()).isNull();
    assertThat(user11.getOrgLevel2()).isNull();
    assertThat(user11.getOrgLevel3()).isNull();
    assertThat(user11.getOrgLevel4()).isNull();
  }

}
