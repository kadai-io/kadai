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

import java.util.Map;
import java.util.Objects;

/** Validated authoritative source for one refresh generation. */
public record PreparedUserRefreshInput(
    Map<String, UserRefreshState> usersById,
    int inputUsers,
    int acceptedUsers,
    int rejectedUsers) {

  public PreparedUserRefreshInput {
    usersById = Map.copyOf(Objects.requireNonNull(usersById, "usersById"));
  }
}
