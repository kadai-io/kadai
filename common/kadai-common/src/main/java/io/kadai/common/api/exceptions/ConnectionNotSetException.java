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

/**
 * This exception is thrown when using KADAI with the EXPLICIT ConnectionManagementMode and an
 * attempt is made to call an API method before the KadainEngine#setConnection() method has been
 * called.
 */
public class ConnectionNotSetException extends KadaiRuntimeException {

  public static final String ERROR_KEY = "CONNECTION_NOT_SET";

  public ConnectionNotSetException() {
    super("Connection not set", ErrorCode.of(ERROR_KEY));
  }
}
