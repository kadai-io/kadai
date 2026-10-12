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

package io.kadai.common.rest.ldap;

import io.kadai.user.api.models.User;
import java.util.List;
import java.util.Objects;

/** A complete LDAP user-role result suitable for authoritative reconciliation. */
public record LdapUserSnapshot(List<User> users, int pageCount, int resultCount) {
  public LdapUserSnapshot {
    users = List.copyOf(Objects.requireNonNull(users, "users"));
    if (pageCount < 1) {
      throw new IllegalArgumentException("pageCount must be at least one");
    }
    if (resultCount != users.size()) {
      throw new IllegalArgumentException("resultCount must equal users.size()");
    }
  }
}
