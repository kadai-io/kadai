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
 *
 *
 */

package io.kadai.common.api.exceptions;

import io.kadai.common.api.KadaiRole;
import java.util.Arrays;
import java.util.Map;

/**
 * This exception is thrown when the current user is not in a certain {@linkplain KadaiRole role} it
 * is supposed to be.
 */
public class NotAuthorizedException extends KadaiException {

  public static final String ERROR_KEY = "NOT_AUTHORIZED";
  private final String currentUserId;
  private final KadaiRole[] roles;

  public NotAuthorizedException(String currentUserId, KadaiRole... roles) {
    super(
        String.format(
            "Not authorized. The current user '%s' is not member of role(s) '%s'.",
            currentUserId, Arrays.toString(roles)),
        ErrorCode.of(
            ERROR_KEY,
            Map.ofEntries(
                Map.entry("roles", ensureNullIsHandled(roles)),
                Map.entry("currentUserId", ensureNullIsHandled(currentUserId)))));

    this.currentUserId = currentUserId;
    this.roles = roles;
  }

  public KadaiRole[] getRoles() {
    return roles;
  }

  public String getCurrentUserId() {
    return currentUserId;
  }
}
