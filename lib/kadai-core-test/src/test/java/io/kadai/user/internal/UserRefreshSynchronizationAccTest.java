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
 */

package io.kadai.user.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.testapi.KadaiInject;
import io.kadai.testapi.KadaiIntegrationTest;
import io.kadai.testapi.security.WithAccessId;
import io.kadai.user.api.UserService;
import io.kadai.user.api.models.User;
import io.kadai.user.internal.models.UserImpl;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@KadaiIntegrationTest
class UserRefreshSynchronizationAccTest {

  private static final int BATCH_SIZE = 5;

  @KadaiInject KadaiEngine kadaiEngine;
  @KadaiInject UserService userService;

  UserServiceImpl userServiceImpl;

  @BeforeEach
  @WithAccessId(user = "businessadmin")
  void setUp() throws Exception {
    userServiceImpl = (UserServiceImpl) userService;
    userServiceImpl.deleteAllUsersGroupsPermissions();
    dropGuardTable();
  }

  @AfterEach
  @WithAccessId(user = "businessadmin")
  void tearDown() throws Exception {
    dropGuardTable();
    userServiceImpl.deleteAllUsersGroupsPermissions();
  }

  @Test
  @WithAccessId(user = "businessadmin")
  void should_ApplyExactScalarAndMembershipDiff() throws Exception {
    create(user("unchanged", "Same", null, Set.of("g-a"), Set.of("p-a")));
    create(user("changed", "Old", "old", Set.of("g-old", "g-keep"), Set.of("p-old")));
    create(user("removed", "Removed", null, Set.of("g-removed"), Set.of("p-removed")));

    UserRefreshResult result =
        synchronize(
            List.of(
                user("unchanged", "Same", null, Set.of("g-a"), Set.of("p-a")),
                user("changed", "New", "new", Set.of("g-new", "g-keep"), Set.of("p-new")),
                user("inserted", "Inserted", null, Set.of("g-insert"), Set.of("p-insert"))));

    assertThat(result.insertedUsers()).isEqualTo(1);
    assertThat(result.updatedUsers()).isEqualTo(1);
    assertThat(result.removedUsers()).isEqualTo(1);
    assertThat(result.unchangedUsers()).isEqualTo(1);
    assertThat(result.addedGroups()).isEqualTo(2);
    assertThat(result.removedGroups()).isEqualTo(2);
    assertThat(result.addedPermissions()).isEqualTo(2);
    assertThat(result.removedPermissions()).isEqualTo(2);
    assertThat(userIds()).containsExactly("changed", "inserted", "unchanged");
    assertThat(userService.getUser("changed").getGroups())
        .containsExactlyInAnyOrder("g-keep", "g-new");
    assertThat(userService.getUser("changed").getPermissions()).containsExactly("p-new");
  }

  @Test
  @WithAccessId(user = "businessadmin")
  void should_BeIdempotentAndClearDataAuthoritatively() throws Exception {
    create(user("data-user", "Same", "old", Set.of("group"), Set.of("permission")));

    UserRefreshResult first =
        synchronize(
            List.of(
                user("data-user", "Same", null, Set.of("group"), Set.of("permission"))));
    UserRefreshResult second =
        synchronize(
            List.of(
                user("data-user", "Same", null, Set.of("group"), Set.of("permission"))));

    assertThat(first.updatedUsers()).isEqualTo(1);
    assertThat(second.updatedUsers()).isZero();
    assertThat(second.unchangedUsers()).isEqualTo(1);
    assertThat(userService.getUser("data-user").getData()).isNull();
  }

  @Test
  @WithAccessId(user = "businessadmin")
  void should_RemoveOrphansAndKeepDesiredMembershipsForNewUser() throws Exception {
    insert("GROUP_INFO", "GROUP_ID", "new-user", "stale-group");
    insert("PERMISSION_INFO", "PERMISSION_ID", "new-user", "stale-permission");

    UserRefreshResult result =
        synchronize(
            List.of(
                user(
                    "new-user",
                    "New",
                    null,
                    Set.of("desired-group"),
                    Set.of("desired-permission"))));

    assertThat(result.orphanGroupsRemoved()).isEqualTo(1);
    assertThat(result.orphanPermissionsRemoved()).isEqualTo(1);
    assertThat(userService.getUser("new-user").getGroups()).containsExactly("desired-group");
    assertThat(userService.getUser("new-user").getPermissions())
        .containsExactly("desired-permission");
  }

  @Test
  @WithAccessId(user = "businessadmin")
  void should_RollBackDirectSynchronizationAfterLateFailure() throws Exception {
    create(user("a-change", "Old", "old", Set.of("g-old"), Set.of("p-old")));
    create(user("z-protected", "Protected", null, Set.of("g-protected"), Set.of()));
    createGuardReference("z-protected");
    Map<String, List<List<String>>> before = dumpTables();

    assertThatThrownBy(
            () ->
                synchronize(
                    List.of(
                        user("a-change", "New", "new", Set.of("g-new"), Set.of("p-new")),
                        user("b-insert", "Inserted", null, Set.of("g"), Set.of("p")))))
        .isInstanceOf(RuntimeException.class);

    assertThat(dumpTables()).isEqualTo(before);
    assertThat(userIds()).doesNotContain("b-insert");
    assertThat(userService.getUser("a-change").getFirstName()).isEqualTo("Old");
  }

  @Test
  @WithAccessId(user = "businessadmin")
  void should_FlushManyBatchesWithoutSplittingTheTransaction() throws Exception {
    List<User> users =
        IntStream.range(0, 11)
            .mapToObj(index -> user("batch-" + index, "Batch", null,
                Set.of("group-" + index), Set.of("permission-" + index)))
            .map(User.class::cast)
            .toList();

    UserRefreshResult result = synchronize(users);

    assertThat(result.insertedUsers()).isEqualTo(11);
    assertThat(result.addedGroups()).isEqualTo(11);
    assertThat(result.addedPermissions()).isEqualTo(11);
    assertThat(userIds()).hasSize(11);
  }

  @Test
  @WithAccessId(user = "businessadmin")
  void should_RejectInvalidBatchSizeBeforeWrites() throws Exception {
    PreparedUserRefreshInput prepared = userServiceImpl.prepareUserRefresh(List.of());

    assertThatThrownBy(() -> userServiceImpl.synchronizeUsers(prepared, 0))
        .isInstanceOf(InvalidArgumentException.class)
        .hasMessageContaining("positive");
  }

  private UserRefreshResult synchronize(List<User> users) throws Exception {
    return synchronize(users, BATCH_SIZE);
  }

  private UserRefreshResult synchronize(List<User> users, int batchSize) throws Exception {
    PreparedUserRefreshInput prepared = userServiceImpl.prepareUserRefresh(users);
    return userServiceImpl.synchronizeUsers(prepared, batchSize);
  }

  private void create(User user) throws Exception {
    userService.createUser(user);
  }

  private UserImpl user(
      String id, String firstName, String data, Set<String> groups, Set<String> permissions) {
    UserImpl user = new UserImpl();
    user.setId(id);
    user.setFirstName(firstName);
    user.setLastName("Last");
    user.setFullName(firstName + " Last");
    user.setLongName(firstName + " Last - (" + id + ")");
    user.setEmail(id + "@example.com");
    user.setPhone("123");
    user.setMobilePhone("456");
    user.setOrgLevel1("one");
    user.setOrgLevel2("two");
    user.setOrgLevel3("three");
    user.setOrgLevel4("four");
    user.setData(data);
    user.setGroups(groups);
    user.setPermissions(permissions);
    return user;
  }

  private List<String> userIds() throws Exception {
    try (Connection connection = connection();
        Statement statement = connection.createStatement();
        ResultSet resultSet =
            statement.executeQuery("SELECT USER_ID FROM USER_INFO ORDER BY USER_ID")) {
      List<String> ids = new ArrayList<>();
      while (resultSet.next()) {
        ids.add(resultSet.getString(1));
      }
      return ids;
    }
  }

  private void insert(String table, String column, String userId, String accessId)
      throws Exception {
    try (Connection connection = connection();
        PreparedStatement statement =
            connection.prepareStatement(
                "INSERT INTO " + table + " (USER_ID, " + column + ") VALUES (?, ?)")) {
      statement.setString(1, userId);
      statement.setString(2, accessId);
      statement.executeUpdate();
    }
  }

  private void createGuardReference(String userId) throws Exception {
    try (Connection connection = connection(); Statement statement = connection.createStatement()) {
      statement.execute(
          "CREATE TABLE USER_REFRESH_TEST_REFERENCE (USER_ID VARCHAR(32) NOT NULL, "
              + "CONSTRAINT USER_REFRESH_TEST_REFERENCE_USER FOREIGN KEY (USER_ID) "
              + "REFERENCES USER_INFO (USER_ID))");
    }
    try (Connection connection = connection();
        PreparedStatement statement =
            connection.prepareStatement(
                "INSERT INTO USER_REFRESH_TEST_REFERENCE (USER_ID) VALUES (?)")) {
      statement.setString(1, userId);
      statement.executeUpdate();
    }
  }

  private void dropGuardTable() throws Exception {
    try (Connection connection = connection(); Statement statement = connection.createStatement()) {
      try {
        statement.execute("DROP TABLE USER_REFRESH_TEST_REFERENCE");
      } catch (java.sql.SQLException ignored) {
        // The test table is absent on the first run.
      }
    }
  }

  private Map<String, List<List<String>>> dumpTables() throws Exception {
    return Map.of(
        "USER_INFO", dump("SELECT USER_ID, FIRST_NAME, DATA FROM USER_INFO ORDER BY USER_ID", 3),
        "GROUP_INFO",
            dump("SELECT USER_ID, GROUP_ID FROM GROUP_INFO ORDER BY USER_ID, GROUP_ID", 2),
        "PERMISSION_INFO",
            dump("SELECT USER_ID, PERMISSION_ID FROM PERMISSION_INFO "
                + "ORDER BY USER_ID, PERMISSION_ID", 2));
  }

  private List<List<String>> dump(String sql, int columns) throws Exception {
    try (Connection connection = connection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql)) {
      List<List<String>> rows = new ArrayList<>();
      while (resultSet.next()) {
        List<String> row = new ArrayList<>();
        for (int index = 1; index <= columns; index++) {
          row.add(resultSet.getString(index));
        }
        rows.add(row);
      }
      return rows;
    }
  }

  private Connection connection() throws Exception {
    Connection connection =
        kadaiEngine.getConfiguration().getDataSource().getConnection();
    connection.setSchema(kadaiEngine.getConfiguration().getSchemaName());
    return connection;
  }
}
