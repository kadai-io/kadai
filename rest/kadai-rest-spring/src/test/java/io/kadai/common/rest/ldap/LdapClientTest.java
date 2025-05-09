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

package io.kadai.common.rest.ldap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.kadai.KadaiConfiguration;
import io.kadai.common.api.KadaiRole;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.rest.models.AccessIdRepresentationModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.LdapTemplate;

@ExtendWith(MockitoExtension.class)
class LdapClientTest {

  @Mock Environment environment;

  @Mock LdapTemplate ldapTemplate;

  @Mock KadaiConfiguration kadaiConfiguration;

  @Spy @InjectMocks LdapClient cut;

  @ParameterizedTest
  @CsvSource(
      value = {
        "cn=developersgroup,ou=groups,o=kadaitest;cn=developersgroup,ou=groups",
        "cn=developers:permission,cn=groups;" + "cn=developers:permission,cn=groups",
        "cn=Developersgroup,ou=groups,o=kadaitest;cn=developersgroup,ou=groups",
        "cn=Developers:Permission,cn=groups,o=kadaitest;" + "cn=developers:permission,cn=groups"
      },
      delimiter = ';')
  void should_SearchGroupOrPermissionByDnAndConvertAccessIdToLowercase_For_LdapCall(
      String arg1, String arg2) throws InvalidNameException {
    setUpEnvMock();
    cut.init();

    cut.searchAccessIdByDn(arg1);

    verify(ldapTemplate)
        .lookup(eq(new LdapName(arg2)), any(), any(LdapClient.DnContextMapper.class));
  }

  @Test
  void testLdap_searchUsersAndGroupsAndPermissions() throws Exception {

    setUpEnvMock();
    cut.init();

    AccessIdRepresentationModel permission = new AccessIdRepresentationModel("testP", "testPId");
    AccessIdRepresentationModel group = new AccessIdRepresentationModel("testG", "testGId");
    AccessIdRepresentationModel user = new AccessIdRepresentationModel("testU", "testUId");

    when(ldapTemplate.search(
            any(String.class),
            any(),
            anyInt(),
            any(),
            any(LdapClient.PermissionContextMapper.class)))
        .thenReturn(List.of(permission));
    when(ldapTemplate.search(
            any(String.class), any(), anyInt(), any(), any(LdapClient.GroupContextMapper.class)))
        .thenReturn(List.of(group));
    when(ldapTemplate.search(
            any(String.class), any(), anyInt(), any(), any(LdapClient.UserContextMapper.class)))
        .thenReturn(List.of(user));

    assertThat(cut.searchUsersAndGroupsAndPermissions("test"))
        .hasSize(3)
        .containsExactlyInAnyOrder(user, group, permission);
  }

  @Test
  void should_CorrectlySortAccessIds_When_ContainingNullAccessId() {

    AccessIdRepresentationModel model1 = new AccessIdRepresentationModel("name1", "user-1");
    AccessIdRepresentationModel model2 = new AccessIdRepresentationModel("name2", "user-2");
    AccessIdRepresentationModel model3 = new AccessIdRepresentationModel("name3", null);
    AccessIdRepresentationModel model4 = new AccessIdRepresentationModel("name4", "user-4");
    // Can't use List.of because it returns an ImmutableCollection
    List<AccessIdRepresentationModel> accessIds =
        new ArrayList<>(List.of(model1, model2, model3, model4));

    LdapClient ldapClient = new LdapClient(environment, ldapTemplate, kadaiConfiguration);
    ldapClient.sortListOfAccessIdResources(accessIds);
    assertThat(accessIds)
        .extracting(AccessIdRepresentationModel::getAccessId)
        .containsExactly("user-1", "user-2", "user-4", null);
  }

  @Test
  void should_ReturnAllUsersAndMembersOfGroupsAndMemberOfPermissionsWithKadaiUserRole() {

    setUpEnvMock();
    cut.init();

    Set<String> groupsOfUserRole = new HashSet<>();
    Map<KadaiRole, Set<String>> roleMap = new HashMap<>();
    roleMap.put(KadaiRole.USER, groupsOfUserRole);
    Set<String> permissionsOfUserRole = new HashSet<>();
    roleMap.put(KadaiRole.USER, permissionsOfUserRole);

    when(kadaiConfiguration.getRoleMap()).thenReturn(roleMap);

    AccessIdRepresentationModel user = new AccessIdRepresentationModel("testU", "testUId");

    when(ldapTemplate.search(
            any(String.class), any(), anyInt(), any(), any(LdapClient.UserContextMapper.class)))
        .thenReturn(List.of(user));

    assertThat(cut.searchUsersByNameOrAccessIdInUserRole("test")).hasSize(1).containsExactly(user);
  }

  @Test
  void testLdap_getNameWithoutBaseDnForGroup() {

    setUpEnvMock();
    cut.init();
    assertThat(cut.getNameWithoutBaseDn("cn=developersgroup,ou=groups,o=kadaitest"))
        .isEqualTo("cn=developersgroup,ou=groups");
  }

  @Test
  void testLdap_getNameWithoutBaseDnForPermission() {

    setUpEnvMock();
    cut.init();
    assertThat(cut.getNameWithoutBaseDn("cn=other:permission,cn=groups,o=kadaitest"))
        .isEqualTo("cn=other:permission,cn=groups");
  }

  @Test
  void shouldNot_CreateOrCriteriaWithDnAndAccessIdStringForGroup_When_PropertyTypeIsSet()
      throws InvalidArgumentException, InvalidNameException {

    setUpEnvMock();
    lenient().when(this.environment.getProperty("kadai.ldap.groupsOfUser.type")).thenReturn("dn");
    lenient()
        .when(this.environment.getProperty("kadai.ldap.permissionsOfUser.type"))
        .thenReturn("dn");
    lenient()
        .when(
            ldapTemplate.search(
                any(String.class),
                eq("(&(objectclass=person)(uid=user-1-1))"),
                eq(2),
                any(),
                any(LdapClient.DnStringContextMapper.class)))
        .thenReturn(Collections.singletonList("uid=user-1-1,cn=users,OU=Test,O=KADAI"));

    cut.init();

    cut.searchGroupsAccessIdIsMemberOf("user-1-1");
    cut.searchPermissionsAccessIdHas("user-1-1");

    String expectedFilterValue =
        "(&(!(permission=*))"
            + "(&(objectclass=groupOfUniqueNames)(memberUid=uid=user-1-1,cn=users,OU=Test,"
            + "O=KADAI)))";
    verify(ldapTemplate)
        .search(
            any(String.class),
            eq(expectedFilterValue),
            anyInt(),
            any(),
            any(LdapClient.GroupContextMapper.class));

    String expectedFilterValueForPermission =
        "(&(permission=*)"
            + "(&(objectclass=groupOfUniqueNames)(memberUid=uid=user-1-1,cn=users,OU=Test,"
            + "O=KADAI)))";
    verify(ldapTemplate)
        .search(
            any(String.class),
            eq(expectedFilterValueForPermission),
            anyInt(),
            any(),
            any(LdapClient.PermissionContextMapper.class));
  }

  @Test
  void testLdap_getFirstPageOfaResultList() {
    setUpEnvMock();
    cut.init();

    List<AccessIdRepresentationModel> result =
        IntStream.range(0, 100)
            .mapToObj(i -> new AccessIdRepresentationModel("" + i, "" + i))
            .toList();

    assertThat(cut.getFirstPageOfaResultList(result))
        .hasSize(cut.getMaxNumberOfReturnedAccessIds());
  }

  @Test
  void testLdap_isInitorFail() {
    assertThatThrownBy(() -> cut.isInitOrFail()).isInstanceOf(SystemException.class);
    setUpEnvMock();
    cut.init();
    assertThatCode(() -> cut.isInitOrFail()).doesNotThrowAnyException();
  }

  @Test
  void testLdap_checkForMissingConfigurations() {
    assertThat(cut.checkForMissingConfigurations()).hasSize(LdapSettings.REQUIRED_SETTINGS.length);
  }

  @Test
  void testNameIsRecognizedAsDnCorrectly() {
    setUpEnvMock();
    assertThat(cut.nameIsDn("uid=userid,cn=users,o=KadaiTest")).isTrue();
    assertThat(cut.nameIsDn("uid=userid,cn=users,o=kadaitest")).isTrue();
    assertThat(cut.nameIsDn("uid=userid,cn=users,o=kadai")).isFalse();
  }

  @Test
  void should_ReturnAccessIds_For_Groups_When_SearchingByDns() throws Exception {
    setUpEnvMock();
    cut.init();

    when(ldapTemplate.lookup(
        any(LdapName.class),
        any(),
        any(LdapClient.DnContextMapper.class)
    )).thenReturn(new AccessIdRepresentationModel("uid", "user-1-1"));

    final List<AccessIdRepresentationModel> expectedGroupAccessIds =
        List.of(new AccessIdRepresentationModel("cn", "developersgroup"));

    when(ldapTemplate.search(
        anyString(),
        anyString(),
        anyInt(),
        any(String[].class),
        any(LdapClient.GroupContextMapper.class)
    )).thenReturn(expectedGroupAccessIds);

    final List<String> actualGroupAccessIds = cut.searchAccessIdsForGroupsByDn(
        List.of("uid=user-1-1,cn=developersgroup,ou=groups,o=kadaitest"));

    assertThat(actualGroupAccessIds).containsExactlyInAnyOrderElementsOf(
        expectedGroupAccessIds.stream()
            .map(AccessIdRepresentationModel::getAccessId)
            .toList());
  }

  @Test
  void should_ReturnEmptyList_For_Groups_When_SearchingByEmptyListOfDns() throws Exception {
    setUpEnvMock();
    cut.init();

    final List<String> groupAccessIds = cut.searchAccessIdsForGroupsByDn(Collections.emptyList());

    assertThat(groupAccessIds).isEmpty();
  }

  @Test
  void should_RethrowInvalidNameException_For_Groups() throws Exception {
    setUpEnvMock();
    cut.init();

    when(cut.searchAccessIdByDn(anyString())).thenThrow(InvalidNameException.class);

    final ThrowingCallable call = () -> cut.searchAccessIdsForGroupsByDn(List.of("some-dn"));

    assertThatThrownBy(call).isInstanceOf(InvalidNameException.class);
  }

  @Test
  void should_ReturnAccessIds_For_Permissions_When_SearchingByDns() throws Exception {
    setUpEnvMock();
    cut.init();

    when(ldapTemplate.lookup(
        any(LdapName.class),
        any(),
        any(LdapClient.DnContextMapper.class)
    )).thenReturn(new AccessIdRepresentationModel("uid", "user-1-1"));

    final List<AccessIdRepresentationModel> expectedPermissionAccessIds =
        List.of(
            new AccessIdRepresentationModel(
                "permission", "Kadai:CallCenter:AB:AB/A:CallCenter-vip"));

    when(ldapTemplate.search(
        anyString(),
        anyString(),
        anyInt(),
        any(String[].class),
        any(LdapClient.PermissionContextMapper.class)
    )).thenReturn(expectedPermissionAccessIds);

    final List<String> actualPermissionAccessIds =
        cut.searchAccessIdsForPermissionsByDn(
            List.of(
                "uid=user-1-1,permission=Kadai:CallCenter:AB:AB/A:CallCenter-vip,"
                    + "cn=Developers:Permission,cn=groups,o=kadaitest"));

    assertThat(actualPermissionAccessIds).containsExactlyInAnyOrderElementsOf(
        expectedPermissionAccessIds.stream()
            .map(AccessIdRepresentationModel::getAccessId)
            .toList());
  }

  @Test
  void should_ReturnEmptyList_For_Permissions_When_SearchingByEmptyListOfDns() throws Exception {
    setUpEnvMock();
    cut.init();

    final List<String> permissionAccessIds =
        cut.searchAccessIdsForPermissionsByDn(Collections.emptyList());

    assertThat(permissionAccessIds).isEmpty();
  }

  @Test
  void should_RethrowInvalidNameException_For_Permissions() throws Exception {
    setUpEnvMock();
    cut.init();

    when(cut.searchAccessIdByDn(anyString())).thenThrow(InvalidNameException.class);

    final ThrowingCallable call =
        () -> cut.searchAccessIdsForPermissionsByDn(List.of("some-dn"));

    assertThatThrownBy(call).isInstanceOf(InvalidNameException.class);
  }

  private void setUpEnvMock() {

    Stream.of(
            new String[][] {
              {"kadai.ldap.minSearchForLength", "3"},
              {"kadai.ldap.maxNumberOfReturnedAccessIds", "50"},
              {"kadai.ldap.baseDn", "o=KadaiTest"},
              {"kadai.ldap.userSearchBase", "ou=people"},
              {"kadai.ldap.userSearchFilterName", "objectclass"},
              {"kadai.ldap.groupsOfUser.name", "memberUid"},
              {"kadai.ldap.groupNameAttribute", "cn"},
              {"kadai.ldap.userPermissionsAttribute", "permission"},
              {"kadai.ldap.groupSearchFilterValue", "groupOfUniqueNames"},
              {"kadai.ldap.groupSearchFilterName", "objectclass"},
              {"kadai.ldap.groupSearchBase", "ou=groups"},
              {"kadai.ldap.userIdAttribute", "uid"},
              {"kadai.ldap.userMemberOfGroupAttribute", "memberOf"},
              {"kadai.ldap.userLastnameAttribute", "sn"},
              {"kadai.ldap.userFirstnameAttribute", "givenName"},
              {"kadai.ldap.userFullnameAttribute", "cn"},
              {"kadai.ldap.userSearchFilterValue", "person"},
              {"kadai.ldap.userPhoneAttribute", "phoneNumber"},
              {"kadai.ldap.userMobilePhoneAttribute", "mobileNumber"},
              {"kadai.ldap.userEmailAttribute", "email"},
              {"kadai.ldap.userOrglevel1Attribute", "orgLevel1"},
              {"kadai.ldap.userOrglevel2Attribute", "orgLevel2"},
              {"kadai.ldap.userOrglevel3Attribute", "orgLevel3"},
              {"kadai.ldap.userOrglevel4Attribute", "orgLevel4"},
              {"kadai.ldap.permissionsOfUser.name", "memberUid"},
              {"kadai.ldap.permissionNameAttribute", "permission"},
              {"kadai.ldap.permissionSearchFilterValue", "groupOfUniqueNames"},
              {"kadai.ldap.permissionSearchFilterName", "objectclass"},
              {"kadai.ldap.permissionSearchBase", "ou=groups"},
              {"kadai.ldap.userPermissionsAttribute", "permission"},
              {"kadai.ldap.useDnForGroups", "false"},
            })
        .forEach(
            strings ->
                lenient().when(this.environment.getProperty(strings[0])).thenReturn(strings[1]));
  }
}
