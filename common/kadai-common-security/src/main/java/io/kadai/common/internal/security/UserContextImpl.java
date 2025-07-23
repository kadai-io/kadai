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
import java.util.Optional;

public class UserContextImpl implements UserContext {

  private final String puppet;
  private final String puppeteer;

  public UserContextImpl(String puppet, String puppeteer) {
    this.puppet = puppet;
    this.puppeteer = puppeteer;
  }

  public UserContextImpl(String puppet) {
    this.puppet = puppet;
    this.puppeteer = null;
  }

  @Override
  public String getPuppet() {
    return this.puppet;
  }

  @Override
  public Optional<String> getPuppeteer() {
    return Optional.ofNullable(this.puppeteer);
  }

  @Override
  public String toString() {
    return "UserContextImpl [puppet="
        + puppet
        + ", puppeteer="
        + puppeteer
        + "]";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    UserContextImpl other = (UserContextImpl) obj;
    return Objects.equals(puppet, other.puppet)
        && Objects.equals(puppeteer, other.puppeteer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(puppet, puppeteer);
  }
}