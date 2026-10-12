/*
 * Copyright [2026] [envite consulting GmbH]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.kadai.common.rest.ldap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.kadai.rest.test.KadaiSpringBootTest;
import io.kadai.user.api.models.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.naming.Name;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;

@KadaiSpringBootTest
class LdapClientPagingIntTest {

  private static final String USER_ROLE_DN = "cn=ksc-users,cn=groups,OU=Test,O=KADAI";

  @Autowired LdapClient ldapClient;
  @Autowired LdapTemplate ldapTemplate;
  private final List<Name> createdDns = new ArrayList<>();

  @AfterEach
  void cleanUpUsers() {
    for (int index = createdDns.size() - 1; index >= 0; index--) {
      ldapTemplate.unbind(createdDns.get(index));
    }
    createdDns.clear();
  }

  @Test
  void should_ReturnMoreUsersThanInteractiveLimit() {
    Set<String> expected = createUsers("complete-limit-", 75);

    LdapUserSnapshot snapshot = ldapClient.searchAllUsersInUserRole();

    Set<String> actual =
        snapshot.users().stream()
            .map(User::getId)
            .filter(id -> id.startsWith("complete-limit-"))
            .collect(Collectors.toSet());
    assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    assertThat(snapshot.resultCount()).isEqualTo(snapshot.users().size());
  }

  @Test
  void should_ReturnEveryUserAcrossARealPageBoundary() {
    int numberOfUsers = LdapClient.COMPLETE_USER_SEARCH_PAGE_SIZE + 1;
    Set<String> expected = createUsers("complete-page-", numberOfUsers);

    LdapUserSnapshot snapshot = ldapClient.searchAllUsersInUserRole();

    Set<String> actual =
        snapshot.users().stream()
            .map(User::getId)
            .filter(id -> id.startsWith("complete-page-"))
            .collect(Collectors.toSet());
    assertThat(snapshot.pageCount()).isGreaterThan(1);
    assertThat(actual).containsExactlyInAnyOrderElementsOf(expected).hasSize(numberOfUsers);
  }

  @Test
  void should_ReturnIndependentImmutableSnapshots() {
    createUsers("independent-", 2);

    LdapUserSnapshot first = ldapClient.searchAllUsersInUserRole();
    LdapUserSnapshot second = ldapClient.searchAllUsersInUserRole();

    assertThat(first).isNotSameAs(second);
    assertThat(first.users()).isNotSameAs(second.users()).isEqualTo(second.users());
    List<User> firstUsers = first.users();
    assertThatThrownBy(firstUsers::clear)
        .isInstanceOf(UnsupportedOperationException.class);
  }

  private Set<String> createUsers(String prefix, int count) {
    List<String> ids = new ArrayList<>();
    for (int index = 0; index < count; index++) {
      String id = prefix + String.format("%04d", index);
      final Name dn =
          LdapNameBuilder.newInstance().add("cn", "users").add("uid", id).build();
      final BasicAttributes attributes = new BasicAttributes(true);
      BasicAttribute objectClass = new BasicAttribute("objectclass");
      objectClass.add("top");
      objectClass.add("person");
      objectClass.add("organizationalPerson");
      objectClass.add("inetOrgPerson");
      attributes.put(objectClass);
      attributes.put("uid", id);
      attributes.put("givenName", "Paged");
      attributes.put("sn", "User");
      attributes.put("cn", "Paged User " + index);
      attributes.put("memberOf", USER_ROLE_DN);
      ldapTemplate.bind(dn, null, attributes);
      createdDns.add(dn);
      ids.add(id);
    }
    return Set.copyOf(ids);
  }
}
