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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Builds a refresh snapshot in linear time from three flat query results. */
final class UserRefreshSnapshotAssembler {
  private UserRefreshSnapshotAssembler() {}

  static UserDatabaseSnapshot assemble(
      List<UserRefreshState> scalarUsers,
      List<UserAccessIdRow> groupRows,
      List<UserAccessIdRow> permissionRows) {
    Objects.requireNonNull(scalarUsers, "scalarUsers");
    Objects.requireNonNull(groupRows, "groupRows");
    Objects.requireNonNull(permissionRows, "permissionRows");

    Map<String, MutableUserRefreshState> users = new LinkedHashMap<>();
    for (UserRefreshState scalarUser : scalarUsers) {
      Objects.requireNonNull(scalarUser, "scalar user");
      if (users.put(scalarUser.id(), new MutableUserRefreshState(scalarUser)) != null) {
        throw new IllegalArgumentException("Duplicate user id in database refresh snapshot");
      }
    }
    List<UserAccessIdRow> orphanGroups = new ArrayList<>();
    List<UserAccessIdRow> orphanPermissions = new ArrayList<>();
    addGroups(users, groupRows, orphanGroups);
    addPermissions(users, permissionRows, orphanPermissions);

    Map<String, UserRefreshState> frozen = new HashMap<>();
    users.forEach((id, user) -> frozen.put(id, user.freeze()));
    return new UserDatabaseSnapshot(frozen, orphanGroups, orphanPermissions);
  }

  private static void addGroups(
      Map<String, MutableUserRefreshState> users,
      List<UserAccessIdRow> rows,
      List<UserAccessIdRow> orphans) {
    for (UserAccessIdRow row : rows) {
      validate(row);
      MutableUserRefreshState user = users.get(row.userId());
      if (user == null) {
        orphans.add(row);
      } else {
        user.groups.add(row.accessId());
      }
    }
  }

  private static void addPermissions(
      Map<String, MutableUserRefreshState> users,
      List<UserAccessIdRow> rows,
      List<UserAccessIdRow> orphans) {
    for (UserAccessIdRow row : rows) {
      validate(row);
      MutableUserRefreshState user = users.get(row.userId());
      if (user == null) {
        orphans.add(row);
      } else {
        user.permissions.add(row.accessId());
      }
    }
  }

  private static void validate(UserAccessIdRow row) {
    if (row == null || row.userId() == null) {
      throw new IllegalArgumentException("Refresh membership row has null USER_ID");
    }
    if (row.accessId() == null) {
      throw new IllegalArgumentException("Refresh membership row has null ACCESS_ID");
    }
  }

  private static final class MutableUserRefreshState {
    private final UserRefreshState scalar;
    private final java.util.Set<String> groups = new java.util.HashSet<>();
    private final java.util.Set<String> permissions = new java.util.HashSet<>();

    private MutableUserRefreshState(UserRefreshState scalar) {
      this.scalar = scalar;
    }

    private UserRefreshState freeze() {
      return new UserRefreshState(
          scalar.id(),
          scalar.firstName(),
          scalar.lastName(),
          scalar.fullName(),
          scalar.longName(),
          scalar.email(),
          scalar.phone(),
          scalar.mobilePhone(),
          scalar.orgLevel1(),
          scalar.orgLevel2(),
          scalar.orgLevel3(),
          scalar.orgLevel4(),
          scalar.data(),
          groups,
          permissions);
    }
  }
}
