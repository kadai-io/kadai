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

import static io.kadai.common.internal.util.CollectionUtil.difference;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Calculates a pure, deterministic refresh plan. */
final class UserRefreshDiffCalculator {
  private static final Comparator<UserRefreshState> USER_ORDER =
      Comparator.comparing(UserRefreshState::id);
  private static final Comparator<UserAccessIdRow> ROW_ORDER =
      Comparator.comparing(UserAccessIdRow::userId).thenComparing(UserAccessIdRow::accessId);

  private UserRefreshDiffCalculator() {}

  static UserRefreshPlan calculate(
      UserDatabaseSnapshot current, Map<String, UserRefreshState> desiredById) {
    Objects.requireNonNull(current, "current");
    Objects.requireNonNull(desiredById, "desiredById");

    List<UserRefreshState> usersToInsert = new ArrayList<>();
    List<UserRefreshState> usersToUpdate = new ArrayList<>();
    List<String> userIdsToDelete = new ArrayList<>();
    List<UserAccessIdRow> groupsToInsert = new ArrayList<>();
    List<UserAccessIdRow> groupsToDelete = new ArrayList<>();
    List<UserAccessIdRow> permissionsToInsert = new ArrayList<>();
    List<UserAccessIdRow> permissionsToDelete = new ArrayList<>();
    int unchangedUsers = 0;
    int updatedUsers = 0;

    for (UserRefreshState desired : desiredById.values()) {
      UserRefreshState existing = current.usersById().get(desired.id());
      if (existing == null) {
        usersToInsert.add(desired);
        desired.groups()
            .forEach(group -> groupsToInsert.add(new UserAccessIdRow(desired.id(), group)));
        desired.permissions()
            .forEach(
                permission ->
                    permissionsToInsert.add(new UserAccessIdRow(desired.id(), permission)));
        continue;
      }

      boolean scalarChanged = !desired.hasSameScalars(existing);
      Set<String> desiredGroups = desired.groups();
      Set<String> existingGroups = existing.groups();
      Set<String> desiredPermissions = desired.permissions();
      Set<String> existingPermissions = existing.permissions();
      Set<String> addedGroups = difference(desiredGroups, existingGroups);
      Set<String> removedGroups = difference(existingGroups, desiredGroups);
      final Set<String> addedPermissions = difference(desiredPermissions, existingPermissions);
      final Set<String> removedPermissions = difference(existingPermissions, desiredPermissions);

      if (scalarChanged) {
        usersToUpdate.add(desired);
      }
      addedGroups.forEach(group -> groupsToInsert.add(new UserAccessIdRow(desired.id(), group)));
      removedGroups.forEach(group -> groupsToDelete.add(new UserAccessIdRow(desired.id(), group)));
      addedPermissions.forEach(
          permission -> permissionsToInsert.add(new UserAccessIdRow(desired.id(), permission)));
      removedPermissions.forEach(
          permission -> permissionsToDelete.add(new UserAccessIdRow(desired.id(), permission)));

      if (scalarChanged || !addedGroups.isEmpty() || !removedGroups.isEmpty()
          || !addedPermissions.isEmpty() || !removedPermissions.isEmpty()) {
        updatedUsers++;
      } else {
        unchangedUsers++;
      }
    }

    for (UserRefreshState existing : current.usersById().values()) {
      if (!desiredById.containsKey(existing.id())) {
        userIdsToDelete.add(existing.id());
        existing.groups()
            .forEach(group -> groupsToDelete.add(new UserAccessIdRow(existing.id(), group)));
        existing.permissions()
            .forEach(
                permission ->
                    permissionsToDelete.add(new UserAccessIdRow(existing.id(), permission)));
      }
    }

    usersToInsert.sort(USER_ORDER);
    usersToUpdate.sort(USER_ORDER);
    userIdsToDelete.sort(Comparator.naturalOrder());
    groupsToInsert.sort(ROW_ORDER);
    groupsToDelete.sort(ROW_ORDER);
    permissionsToInsert.sort(ROW_ORDER);
    permissionsToDelete.sort(ROW_ORDER);
    List<UserAccessIdRow> orphanGroupsToDelete = new ArrayList<>(current.orphanGroups());
    List<UserAccessIdRow> orphanPermissionsToDelete = new ArrayList<>(current.orphanPermissions());
    orphanGroupsToDelete.sort(ROW_ORDER);
    orphanPermissionsToDelete.sort(ROW_ORDER);

    return new UserRefreshPlan(
        usersToInsert,
        usersToUpdate,
        userIdsToDelete,
        groupsToInsert,
        groupsToDelete,
        permissionsToInsert,
        permissionsToDelete,
        orphanGroupsToDelete,
        orphanPermissionsToDelete,
        unchangedUsers,
        updatedUsers);
  }
}
