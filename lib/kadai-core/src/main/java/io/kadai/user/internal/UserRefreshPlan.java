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

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/** Immutable, deterministic set of writes for one user refresh. */
public record UserRefreshPlan(
    List<UserRefreshState> usersToInsert,
    List<UserRefreshState> usersToUpdate,
    List<String> userIdsToDelete,
    List<UserAccessIdRow> groupsToInsert,
    List<UserAccessIdRow> groupsToDelete,
    List<UserAccessIdRow> permissionsToInsert,
    List<UserAccessIdRow> permissionsToDelete,
    List<UserAccessIdRow> orphanGroupsToDelete,
    List<UserAccessIdRow> orphanPermissionsToDelete,
    int unchangedUsers,
    int updatedUsers) {

  public UserRefreshPlan {
    usersToInsert = List.copyOf(Objects.requireNonNull(usersToInsert, "usersToInsert"));
    usersToUpdate = List.copyOf(Objects.requireNonNull(usersToUpdate, "usersToUpdate"));
    userIdsToDelete = List.copyOf(Objects.requireNonNull(userIdsToDelete, "userIdsToDelete"));
    groupsToInsert = copyRows(groupsToInsert, "groupsToInsert");
    groupsToDelete = copyRows(groupsToDelete, "groupsToDelete");
    permissionsToInsert = copyRows(permissionsToInsert, "permissionsToInsert");
    permissionsToDelete = copyRows(permissionsToDelete, "permissionsToDelete");
    orphanGroupsToDelete = copyRows(orphanGroupsToDelete, "orphanGroupsToDelete");
    orphanPermissionsToDelete = copyRows(orphanPermissionsToDelete, "orphanPermissionsToDelete");
  }

  /**
   * Returns whether the refresh would execute no database writes.
   *
   * @return true when the plan is empty
   */
  public boolean isEmpty() {
    return usersToInsert.isEmpty()
        && usersToUpdate.isEmpty()
        && userIdsToDelete.isEmpty()
        && groupsToInsert.isEmpty()
        && groupsToDelete.isEmpty()
        && permissionsToInsert.isEmpty()
        && permissionsToDelete.isEmpty()
        && orphanGroupsToDelete.isEmpty()
        && orphanPermissionsToDelete.isEmpty();
  }

  private static List<UserAccessIdRow> copyRows(
      List<UserAccessIdRow> rows, String fieldName) {
    return Objects.requireNonNull(rows, fieldName).stream()
        .map(copyRow())
        .toList();
  }

  private static Function<UserAccessIdRow, UserAccessIdRow> copyRow() {
    return row -> {
      if (row == null || row.userId() == null || row.accessId() == null) {
        throw new IllegalArgumentException("Refresh membership rows must not contain null values");
      }
      return new UserAccessIdRow(row.userId(), row.accessId());
    };
  }
}
