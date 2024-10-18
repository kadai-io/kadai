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
