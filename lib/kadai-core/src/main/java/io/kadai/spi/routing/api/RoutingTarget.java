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

package io.kadai.spi.routing.api;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public class RoutingTarget {

  @NotNull private String workbasketId;
  private String owner;

  public RoutingTarget(String workbasketId) {
    this.workbasketId = workbasketId;
  }

  public RoutingTarget(String workbasketId, String owner) {
    this.workbasketId = workbasketId;
    this.owner = owner;
  }

  public String getWorkbasketId() {
    return workbasketId;
  }

  public String getOwner() {
    return owner;
  }

  public void setWorkbasketId(String workbasketId) {
    this.workbasketId = workbasketId;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RoutingTarget)) {
      return false;
    }
    RoutingTarget other = (RoutingTarget) obj;
    return Objects.equals(workbasketId, other.workbasketId)
        && Objects.equals(owner, other.owner);
  }

  @Override
  public int hashCode() {
    return Objects.hash(workbasketId, owner);
  }
}
