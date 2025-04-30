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

package io.kadai.user.api.exceptions;

import io.kadai.common.api.exceptions.ErrorCode;
import io.kadai.common.api.exceptions.KadaiException;
import io.kadai.user.api.models.User;
import java.util.Map;

/**
 * This exception is thrown when a specific {@linkplain User} referenced by its {@linkplain
 * User#getId() id} is not in the database.
 */
public class UserNotFoundException extends KadaiException {
  public static final String ERROR_KEY = "USER_NOT_FOUND";
  private final String userId;

  public UserNotFoundException(String userId) {
    super(
        String.format("User with id '%s' was not found.", userId),
        ErrorCode.of(ERROR_KEY, Map.of("userId", ensureNullIsHandled(userId))));
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }
}
