/*
 * Copyright [2025] [envite consulting GmbH]
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
 *
 *
 */

package io.kadai.common.internal.security;

import io.kadai.common.api.security.UserContext;
import java.util.Objects;

public class UserContextImpl implements UserContext {

  private final String userId;
  private final String proxyAccessId;

  public UserContextImpl(String userId, String proxyAccessId) {
    this.userId = userId;
    this.proxyAccessId = proxyAccessId;
  }

  public UserContextImpl(String userId) {
    this.userId = userId;
    this.proxyAccessId = null;
  }

  @Override
  public String getUserId() {
    return this.userId;
  }

  @Override
  public String getProxyAccessId() {
    return this.proxyAccessId;
  }

  @Override
  public String toString() {
    return "UserContextImpl [userId=" + userId + ", proxyAccessId=" + proxyAccessId + "]";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    UserContextImpl other = (UserContextImpl) obj;
    return Objects.equals(userId, other.userId)
        && Objects.equals(proxyAccessId, other.proxyAccessId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, proxyAccessId);
  }
}
