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

import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.user.internal.models.UserImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Loads the three flat refresh queries and assembles them into one snapshot. */
final class UserRefreshSnapshotLoader {
  private final UserMapper userMapper;

  UserRefreshSnapshotLoader(UserMapper userMapper) {
    this.userMapper = userMapper;
  }

  UserDatabaseSnapshot load() {
    List<UserRefreshState> users = new ArrayList<>();
    for (UserImpl user : userMapper.findAllUsersForRefresh()) {
      users.add(toDatabaseRefreshState(user));
    }
    return UserRefreshSnapshotAssembler.assemble(
        users, userMapper.findAllGroupsForRefresh(), userMapper.findAllPermissionsForRefresh());
  }

  static UserRefreshState toDatabaseRefreshState(UserImpl user) {
    if (user == null || user.getId() == null || user.getId().isEmpty()) {
      throw new InvalidArgumentException("Invalid user in database refresh snapshot");
    }
    return new UserRefreshState(
        user.getId(),
        user.getFirstName(),
        user.getLastName(),
        user.getFullName(),
        user.getLongName(),
        user.getEmail(),
        user.getPhone(),
        user.getMobilePhone(),
        user.getOrgLevel1(),
        user.getOrgLevel2(),
        user.getOrgLevel3(),
        user.getOrgLevel4(),
        user.getData(),
        Set.of(),
        Set.of());
  }
}
