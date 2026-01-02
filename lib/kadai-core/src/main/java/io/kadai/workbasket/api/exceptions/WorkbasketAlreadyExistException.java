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

package io.kadai.workbasket.api.exceptions;

import io.kadai.common.api.exceptions.ErrorCode;
import io.kadai.common.api.exceptions.KadaiException;
import io.kadai.workbasket.api.models.Workbasket;
import java.util.Map;

/**
 * This exception is thrown when an already existing {@linkplain Workbasket} was tried to be
 * created.
 */
public class WorkbasketAlreadyExistException extends KadaiException {

  public static final String ERROR_KEY = "WORKBASKET_ALREADY_EXISTS";
  private final String key;
  private final String domain;

  public WorkbasketAlreadyExistException(String key, String domain) {
    super(
        String.format("A Workbasket with key '%s' already exists in domain '%s'.", key, domain),
        ErrorCode.of(
            ERROR_KEY,
            Map.ofEntries(
                Map.entry("workbasketKey", ensureNullIsHandled(key)),
                Map.entry("domain", ensureNullIsHandled(domain)))));
    this.key = key;
    this.domain = domain;
  }

  public String getKey() {
    return key;
  }

  public String getDomain() {
    return domain;
  }
}
