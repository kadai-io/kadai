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

package io.kadai.common.api.exceptions;

/** Thrown when an LDAP refresh contains duplicate normalized user IDs. */
public class DuplicateUserRefreshIdException extends InvalidArgumentException {

  public DuplicateUserRefreshIdException(String userId) {
    super(String.format("Duplicate normalized user id in LDAP refresh: %s", userId));
  }
}
