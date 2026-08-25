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

package io.kadai.user.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import io.kadai.KadaiConfiguration;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.internal.InternalKadaiEngine;
import io.kadai.user.api.models.User;
import io.kadai.user.internal.models.UserImpl;
import io.kadai.workbasket.api.WorkbasketService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRefreshPreparationTest {

  @Mock InternalKadaiEngine internalKadaiEngine;
  @Mock KadaiEngine kadaiEngine;
  @Mock KadaiConfiguration configuration;
  @Mock WorkbasketService workbasketService;

  private UserServiceImpl userService;

  @BeforeEach
  void setUp() {
    when(internalKadaiEngine.getEngine()).thenReturn(kadaiEngine);
    when(kadaiEngine.getConfiguration()).thenReturn(configuration);
    when(kadaiEngine.getWorkbasketService()).thenReturn(workbasketService);
    when(configuration.getMinimalPermissionsToAssignDomains()).thenReturn(Set.of());
    userService = new UserServiceImpl(internalKadaiEngine, null);
  }

  @Test
  void should_NormalizeLocaleIndependentlyGenerateDefaultsAndNotMutateSource() {
    Locale original = Locale.getDefault();
    try {
      Locale.setDefault(Locale.forLanguageTag("tr-TR"));
      User source = user("USER-I");
      source.setGroups(Set.of("GROUP-I"));
      source.setPermissions(Set.of("PERMISSION-I"));
      source.setFullName(null);
      source.setLongName(null);

      PreparedUserRefreshInput prepared = userService.prepareUserRefresh(List.of(source));
      UserRefreshState state = prepared.usersById().get("user-i");

      assertThat(state.groups()).containsExactly("group-i");
      assertThat(state.permissions()).containsExactly("permission-i");
      assertThat(state.fullName()).isEqualTo("Last, First");
      assertThat(state.longName()).isEqualTo("Last, First - (user-i)");
      assertThat(source.getId()).isEqualTo("USER-I");
      assertThat(source.getFullName()).isNull();
    } finally {
      Locale.setDefault(original);
    }
  }

  @Test
  void should_TreatDataAsAuthoritativeIncludingNull() {
    User set = user("set-data");
    set.setData("new");
    User clear = user("clear-data");
    clear.setData(null);

    PreparedUserRefreshInput prepared = userService.prepareUserRefresh(List.of(set, clear));

    assertThat(prepared.usersById().get("set-data").data()).isEqualTo("new");
    assertThat(prepared.usersById().get("clear-data").data()).isNull();
  }

  @Test
  void should_IsolateInvalidEntriesAndRejectDuplicateCanonicalIds() {
    User invalid = user("invalid");
    invalid.setFirstName(null);
    PreparedUserRefreshInput prepared =
        userService.prepareUserRefresh(
            Arrays.asList(user("valid-a"), null, invalid, user("valid-b")));

    assertThat(prepared.usersById()).containsOnlyKeys("valid-a", "valid-b");
    assertThat(prepared.rejectedUsers()).isEqualTo(2);
    List<User> duplicateUsers = List.of(user("Duplicate"), user("duplicate"));

    assertThatThrownBy(() -> userService.prepareUserRefresh(duplicateUsers))
        .isInstanceOf(InvalidArgumentException.class)
        .hasMessageContaining("duplicate");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("overlongValues")
  void should_RejectValuesLongerThanTheirDatabaseColumn(
      String description, Consumer<User> invalidator) {
    User invalid = user("length-user");
    invalidator.accept(invalid);

    PreparedUserRefreshInput prepared = userService.prepareUserRefresh(List.of(invalid));

    assertThat(prepared.usersById()).isEmpty();
    assertThat(prepared.rejectedUsers()).isOne();
  }

  @Test
  void should_ValidateGeneratedNamesAndDeeplyCopyPreparedInput() {
    User invalid = user("generated");
    invalid.setFirstName("f".repeat(32));
    invalid.setLastName("l".repeat(32));
    invalid.setFullName(null);
    invalid.setLongName(null);
    assertThat(userService.prepareUserRefresh(List.of(invalid)).usersById()).isEmpty();

    List<User> source = new ArrayList<>(List.of(user("stable")));
    PreparedUserRefreshInput prepared = userService.prepareUserRefresh(source);
    source.clear();
    assertThat(prepared.usersById()).containsOnlyKeys("stable");
    Map<String, UserRefreshState> usersById = prepared.usersById();
    UserRefreshState stableUser = usersById.get("stable");

    assertThatThrownBy(usersById::clear)
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> stableUser.groups().add("x"))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  static Stream<Arguments> overlongValues() {
    return Stream.of(
        Arguments.of("id", (Consumer<User>) user -> user.setId("x".repeat(33))),
        Arguments.of("first name", (Consumer<User>) user -> user.setFirstName("x".repeat(33))),
        Arguments.of("last name", (Consumer<User>) user -> user.setLastName("x".repeat(33))),
        Arguments.of("full name", (Consumer<User>) user -> user.setFullName("x".repeat(65))),
        Arguments.of("long name", (Consumer<User>) user -> user.setLongName("x".repeat(65))),
        Arguments.of("email", (Consumer<User>) user -> user.setEmail("x".repeat(65))),
        Arguments.of("phone", (Consumer<User>) user -> user.setPhone("x".repeat(33))),
        Arguments.of(
            "mobile phone", (Consumer<User>) user -> user.setMobilePhone("x".repeat(33))),
        Arguments.of("org 1", (Consumer<User>) user -> user.setOrgLevel1("x".repeat(33))),
        Arguments.of("org 2", (Consumer<User>) user -> user.setOrgLevel2("x".repeat(33))),
        Arguments.of("org 3", (Consumer<User>) user -> user.setOrgLevel3("x".repeat(33))),
        Arguments.of("org 4", (Consumer<User>) user -> user.setOrgLevel4("x".repeat(33))),
        Arguments.of("group", (Consumer<User>) user -> user.setGroups(Set.of("x".repeat(257)))),
        Arguments.of(
            "permission",
            (Consumer<User>) user -> user.setPermissions(Set.of("x".repeat(257)))));
  }

  private static User user(String id) {
    UserImpl user = new UserImpl();
    user.setId(id);
    user.setFirstName("First");
    user.setLastName("Last");
    user.setFullName("Full");
    user.setLongName("Long");
    user.setEmail("user@example.com");
    user.setPhone("123");
    user.setMobilePhone("456");
    user.setOrgLevel1("one");
    user.setOrgLevel2("two");
    user.setOrgLevel3("three");
    user.setOrgLevel4("four");
    user.setGroups(Set.of());
    user.setPermissions(Set.of());
    return user;
  }
}
