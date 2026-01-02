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

package io.kadai.example.ldap;

import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.common.rest.models.AccessIdRepresentationModel;
import io.kadai.rest.test.KadaiSpringBootTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/** Test Ldap attachment. */
@KadaiSpringBootTest
@TestPropertySource(properties = {"kadai.ldap.useDnForGroups=false",
    "kadai.ldap.groupIdAttribute=gid", "kadai.ldap.permissionIdAttribute=pid"})
@ActiveProfiles({"emptySearchRoots"})
class LdapGroupAndPermissionIdAttributeTest extends LdapForUseDnForGroupsDisabledTest {

  @Override
  @Test
  void should_FindGroupsForUser_When_UserIdIsProvided() throws Exception {
    List<AccessIdRepresentationModel> groups =
        ldapClient.searchGroupsAccessIdIsMemberOf("user-2-2");
    assertThat(groups)
        .extracting(AccessIdRepresentationModel::getAccessId)
        .containsExactlyInAnyOrder("ksc-users", "organisationseinheit ksc 2");
  }

  @Test
  void should_ReturnFullDnForGroup_When_AccessIdOfGroupIsGiven() throws Exception {
    String dn = ldapClient.searchDnForAccessId("monitor-users-id");
    assertThat(dn).isEqualTo("cn=monitor-users,cn=groups,ou=test,o=kadai");
  }

  @Test
  void should_ReturnGroupWithMatchingAccessId_When_SearchingUsersGroupsAndPermissions()
      throws Exception {
    List<AccessIdRepresentationModel> accessIds = ldapClient.searchUsersAndGroupsAndPermissions(
        "monitor-users-id");
    assertThat(accessIds)
        .extracting(AccessIdRepresentationModel::getAccessId)
        .containsExactlyInAnyOrder("monitor-users-id");
  }

  @Test
  void should_ReturnPermissionWithMatchingAccessId_When_SearchingUsersGroupsAndPermissions()
      throws Exception {
    List<AccessIdRepresentationModel> accessIds = ldapClient.searchUsersAndGroupsAndPermissions(
        "g03-permission-id");
    assertThat(accessIds)
        .extracting(AccessIdRepresentationModel::getAccessId)
        .containsExactlyInAnyOrder("g03-permission-id");
  }


}
