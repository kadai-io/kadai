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
 * This exception is thrown when a {@linkplain User} was tried to be created with an {@linkplain
 * User#getId() id} already existing.
 */
public class UserAlreadyExistException extends KadaiException {
  public static final String ERROR_KEY = "USER_ALREADY_EXISTS";
  private final String userId;

  public UserAlreadyExistException(String userId, Exception cause) {
    super(
        String.format("User with id '%s' already exists.", userId),
        ErrorCode.of(ERROR_KEY, Map.of("userId", ensureNullIsHandled(userId))),
        cause);
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }
}
