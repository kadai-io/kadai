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

import java.util.Objects;

/** A flat membership row used by the refresh snapshot mapper. */
public final class UserAccessIdRow {
  private String userId;
  private String accessId;

  public UserAccessIdRow() {}

  public UserAccessIdRow(String userId, String accessId) {
    this.userId = userId;
    this.accessId = accessId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getAccessId() {
    return accessId;
  }

  public void setAccessId(String accessId) {
    this.accessId = accessId;
  }

  public String userId() {
    return userId;
  }

  public String accessId() {
    return accessId;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof UserAccessIdRow row)) {
      return false;
    }
    return Objects.equals(userId, row.userId) && Objects.equals(accessId, row.accessId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, accessId);
  }

  @Override
  public String toString() {
    return "UserAccessIdRow[userId=" + userId + ", accessId=" + accessId + "]";
  }
}
