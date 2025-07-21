package io.kadai.common.api.security;

import java.security.Principal;
import java.util.Objects;

public class PuppeteerPrincipal implements Principal {

  private final String name;

  public PuppeteerPrincipal(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof PuppeteerPrincipal)) {
      return false;
    }
    PuppeteerPrincipal other = (PuppeteerPrincipal) obj;
    return Objects.equals(name, other.name);
  }

  @Override
  public String toString() {
    return "PuppeteerPrincipal [name=" + name + "]";
  }
}
