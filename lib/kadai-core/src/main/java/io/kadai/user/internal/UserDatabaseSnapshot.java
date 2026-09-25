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
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/** Immutable flat database snapshot used by the user refresh. */
public record UserDatabaseSnapshot(
    Map<String, UserRefreshState> usersById,
    List<UserAccessIdRow> orphanGroups,
    List<UserAccessIdRow> orphanPermissions) {

  public UserDatabaseSnapshot {
    usersById = Map.copyOf(Objects.requireNonNull(usersById, "usersById"));
    orphanGroups = copyRows(orphanGroups, "orphanGroups");
    orphanPermissions = copyRows(orphanPermissions, "orphanPermissions");
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
