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

package io.kadai.common.api.exceptions;

import java.util.Map;

/**
 * This exception is thrown when the database name doesn't match to one of the desired databases.
 */
public class UnsupportedDatabaseException extends KadaiRuntimeException {

  public static final String ERROR_KEY = "DATABASE_UNSUPPORTED";
  private final String databaseProductName;

  public UnsupportedDatabaseException(String databaseProductName) {
    super(
        String.format("Database '%s' is not supported", databaseProductName),
        ErrorCode.of(
            ERROR_KEY, Map.of("databaseProductName", ensureNullIsHandled(databaseProductName))));
    this.databaseProductName = databaseProductName;
  }

  public String getDatabaseProductName() {
    return databaseProductName;
  }
}
