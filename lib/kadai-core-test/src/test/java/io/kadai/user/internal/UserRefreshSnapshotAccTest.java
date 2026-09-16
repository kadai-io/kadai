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

import io.kadai.common.api.KadaiEngine;
import io.kadai.testapi.KadaiInject;
import io.kadai.testapi.KadaiIntegrationTest;
import io.kadai.testapi.security.WithAccessId;
import io.kadai.user.api.UserService;
import io.kadai.user.internal.models.UserImpl;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@KadaiIntegrationTest
class UserRefreshSnapshotAccTest {

  @KadaiInject KadaiEngine kadaiEngine;
  @KadaiInject UserService userService;

  UserServiceImpl userServiceImpl;

  @BeforeEach
  @WithAccessId(user = "businessadmin")
  void setUp() throws Exception {
    userServiceImpl = (UserServiceImpl) userService;
    userServiceImpl.deleteAllUsersGroupsPermissions();
  }

  @AfterEach
  @WithAccessId(user = "businessadmin")
  void tearDown() throws Exception {
    userServiceImpl.deleteAllUsersGroupsPermissions();
  }

  @Test
  @WithAccessId(user = "businessadmin")
  void should_LoadFlatMembershipRowsWithTheirExplicitColumnValues() throws Exception {
    userService.createUser(user("snapshot-user", Set.of("group-a", "group-b"),
        Set.of("permission-a", "permission-b")));

    UserDatabaseSnapshot snapshot = userServiceImpl.loadUserDatabaseSnapshot();

    assertThat(snapshot.usersById()).containsOnlyKeys("snapshot-user");
    assertThat(snapshot.usersById().get("snapshot-user").groups())
        .containsExactlyInAnyOrder("group-a", "group-b");
    assertThat(snapshot.usersById().get("snapshot-user").permissions())
        .containsExactlyInAnyOrder("permission-a", "permission-b");
    assertThat(snapshot.orphanGroups()).isEmpty();
    assertThat(snapshot.orphanPermissions()).isEmpty();
  }

  @Test
  @WithAccessId(user = "businessadmin")
  void should_LoadExactOrphanRows() throws Exception {
    insert("GROUP_INFO", "GROUP_ID", "missing-user", "group-a");
    insert("GROUP_INFO", "GROUP_ID", "missing-user", "group-b");
    insert("PERMISSION_INFO", "PERMISSION_ID", "missing-user", "permission-a");

    UserDatabaseSnapshot snapshot = userServiceImpl.loadUserDatabaseSnapshot();

    assertThat(snapshot.usersById()).isEmpty();
    assertThat(snapshot.orphanGroups())
        .containsExactlyInAnyOrder(
            new UserAccessIdRow("missing-user", "group-a"),
            new UserAccessIdRow("missing-user", "group-b"));
    assertThat(snapshot.orphanPermissions())
        .containsExactly(new UserAccessIdRow("missing-user", "permission-a"));
  }

  private UserImpl user(String id, Set<String> groups, Set<String> permissions) {
    UserImpl user = new UserImpl();
    user.setId(id);
    user.setFirstName("First");
    user.setLastName("Last");
    user.setFullName("Full");
    user.setLongName("Long");
    user.setEmail("snapshot@example.com");
    user.setPhone("123");
    user.setMobilePhone("456");
    user.setOrgLevel1("one");
    user.setOrgLevel2("two");
    user.setOrgLevel3("three");
    user.setOrgLevel4("four");
    user.setData("data");
    user.setGroups(groups);
    user.setPermissions(permissions);
    return user;
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

  private Connection connection() throws Exception {
    Connection connection =
        kadaiEngine.getConfiguration().getDataSource().getConnection();
    connection.setSchema(kadaiEngine.getConfiguration().getSchemaName());
    return connection;
  }
}
