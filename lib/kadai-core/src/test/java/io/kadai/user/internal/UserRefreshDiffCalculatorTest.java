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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class UserRefreshDiffCalculatorTest {

  @Test
  void should_CalculateExactScalarAndMembershipDifferences() {
    UserRefreshState oldState = state("changed", "Old", Set.of("keep", "remove"), Set.of("p"));
    UserRefreshState desired = state("changed", "New", Set.of("keep", "add"), Set.of("p2"));
    UserRefreshState stale = state("stale", "Stale", Set.of("stale-group"), Set.of());
    UserRefreshState inserted = state("inserted", "Inserted", Set.of("new-group"), Set.of());

    UserRefreshPlan plan =
        UserRefreshDiffCalculator.calculate(
            new UserDatabaseSnapshot(
                Map.of("changed", oldState, "stale", stale), List.of(), List.of()),
            Map.of("changed", desired, "inserted", inserted));

    assertThat(plan.usersToInsert()).extracting(UserRefreshState::id).containsExactly("inserted");
    assertThat(plan.usersToUpdate()).containsExactly(desired);
    assertThat(plan.userIdsToDelete()).containsExactly("stale");
    assertThat(plan.groupsToInsert())
        .containsExactly(
            new UserAccessIdRow("changed", "add"), new UserAccessIdRow("inserted", "new-group"));
    assertThat(plan.groupsToDelete())
        .containsExactly(
            new UserAccessIdRow("changed", "remove"),
            new UserAccessIdRow("stale", "stale-group"));
    assertThat(plan.permissionsToInsert())
        .containsExactly(new UserAccessIdRow("changed", "p2"));
    assertThat(plan.permissionsToDelete())
        .containsExactly(new UserAccessIdRow("changed", "p"));
    assertThat(plan.updatedUsers()).isEqualTo(1);
  }

  @Test
  void should_DeleteOrphansAsExactRowsAndKeepPlanImmutable() {
    UserRefreshState desired = state("new", "New", Set.of("desired"), Set.of("permission"));
    UserRefreshPlan plan =
        UserRefreshDiffCalculator.calculate(
            new UserDatabaseSnapshot(
                Map.of(),
                List.of(new UserAccessIdRow("new", "stale")),
                List.of(new UserAccessIdRow("new", "stale-permission"))),
            Map.of("new", desired));

    assertThat(plan.orphanGroupsToDelete()).containsExactly(new UserAccessIdRow("new", "stale"));
    assertThat(plan.orphanPermissionsToDelete())
        .containsExactly(new UserAccessIdRow("new", "stale-permission"));
    assertThat(plan.usersToInsert()).containsExactly(desired);
    assertThat(plan.isEmpty()).isFalse();
  }

  @Test
  void should_ReturnEmptyPlanForIdenticalState() {
    UserRefreshState state = state("same", "Same", Set.of("g"), Set.of("p"));
    UserRefreshPlan plan =
        UserRefreshDiffCalculator.calculate(
            new UserDatabaseSnapshot(Map.of("same", state), List.of(), List.of()),
            Map.of("same", state));

    assertThat(plan.isEmpty()).isTrue();
    assertThat(plan.unchangedUsers()).isEqualTo(1);
    assertThat(plan.updatedUsers()).isZero();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("scalarChanges")
  void should_DetectEveryPersistedScalarChange(
      String field, UnaryOperator<UserRefreshState> change) {
    UserRefreshState oldState = state("scalar-user", "First", Set.of(), Set.of());
    UserRefreshState desired = change.apply(oldState);

    UserRefreshPlan plan =
        UserRefreshDiffCalculator.calculate(
            new UserDatabaseSnapshot(Map.of(oldState.id(), oldState), List.of(), List.of()),
            Map.of(desired.id(), desired));

    assertThat(plan.usersToUpdate()).containsExactly(desired);
    assertThat(plan.usersToInsert()).isEmpty();
    assertThat(plan.userIdsToDelete()).isEmpty();
    assertThat(plan.groupsToInsert()).isEmpty();
    assertThat(plan.groupsToDelete()).isEmpty();
    assertThat(plan.permissionsToInsert()).isEmpty();
    assertThat(plan.permissionsToDelete()).isEmpty();
    assertThat(plan.updatedUsers()).isEqualTo(1);
    assertThat(plan.unchangedUsers()).isZero();
  }

  @Test
  void should_DetectInsertDeleteAndKeepForEveryCollectionField() {
    UserRefreshState oldState =
        state(
            "collection-user",
            "First",
            Set.of("group-keep", "group-delete"),
            Set.of("permission-keep", "permission-delete"));
    UserRefreshState desired =
        state(
            "collection-user",
            "First",
            Set.of("group-keep", "group-insert"),
            Set.of("permission-keep", "permission-insert"));

    UserRefreshPlan plan =
        UserRefreshDiffCalculator.calculate(
            new UserDatabaseSnapshot(Map.of(oldState.id(), oldState), List.of(), List.of()),
            Map.of(desired.id(), desired));

    assertThat(plan.usersToUpdate()).isEmpty();
    assertThat(plan.groupsToInsert())
        .containsExactly(new UserAccessIdRow("collection-user", "group-insert"));
    assertThat(plan.groupsToDelete())
        .containsExactly(new UserAccessIdRow("collection-user", "group-delete"));
    assertThat(plan.permissionsToInsert())
        .containsExactly(new UserAccessIdRow("collection-user", "permission-insert"));
    assertThat(plan.permissionsToDelete())
        .containsExactly(new UserAccessIdRow("collection-user", "permission-delete"));
    assertThat(plan.updatedUsers()).isEqualTo(1);
    assertThat(plan.unchangedUsers()).isZero();
  }

  static Stream<Arguments> scalarChanges() {
    return Stream.of(
        Arguments.of(
            "firstName", (UnaryOperator<UserRefreshState>) s -> with(s, "firstName", "Changed")),
        Arguments.of(
            "lastName", (UnaryOperator<UserRefreshState>) s -> with(s, "lastName", "Changed")),
        Arguments.of(
            "fullName", (UnaryOperator<UserRefreshState>) s -> with(s, "fullName", "Changed")),
        Arguments.of(
            "longName", (UnaryOperator<UserRefreshState>) s -> with(s, "longName", "Changed")),
        Arguments.of(
            "email",
            (UnaryOperator<UserRefreshState>) s -> with(s, "email", "changed@example.com")),
        Arguments.of("phone", (UnaryOperator<UserRefreshState>) s -> with(s, "phone", "999")),
        Arguments.of(
            "mobilePhone", (UnaryOperator<UserRefreshState>) s -> with(s, "mobilePhone", "888")),
        Arguments.of(
            "orgLevel1", (UnaryOperator<UserRefreshState>) s -> with(s, "orgLevel1", "Changed")),
        Arguments.of(
            "orgLevel2", (UnaryOperator<UserRefreshState>) s -> with(s, "orgLevel2", "Changed")),
        Arguments.of(
            "orgLevel3", (UnaryOperator<UserRefreshState>) s -> with(s, "orgLevel3", "Changed")),
        Arguments.of(
            "orgLevel4", (UnaryOperator<UserRefreshState>) s -> with(s, "orgLevel4", "Changed")),
        Arguments.of("data", (UnaryOperator<UserRefreshState>) s -> with(s, "data", null)));
  }

  private static UserRefreshState state(
      String id, String firstName, Set<String> groups, Set<String> permissions) {
    return new UserRefreshState(
        id,
        firstName,
        "Last",
        "Full",
        "Long",
        "mail@example.com",
        "123",
        "456",
        "one",
        "two",
        "three",
        "four",
        "data",
        groups,
        permissions);
  }

  private static UserRefreshState with(UserRefreshState state, String field, String value) {
    return new UserRefreshState(
        state.id(),
        field.equals("firstName") ? value : state.firstName(),
        field.equals("lastName") ? value : state.lastName(),
        field.equals("fullName") ? value : state.fullName(),
        field.equals("longName") ? value : state.longName(),
        field.equals("email") ? value : state.email(),
        field.equals("phone") ? value : state.phone(),
        field.equals("mobilePhone") ? value : state.mobilePhone(),
        field.equals("orgLevel1") ? value : state.orgLevel1(),
        field.equals("orgLevel2") ? value : state.orgLevel2(),
        field.equals("orgLevel3") ? value : state.orgLevel3(),
        field.equals("orgLevel4") ? value : state.orgLevel4(),
        field.equals("data") ? value : state.data(),
        state.groups(),
        state.permissions());
  }
}
