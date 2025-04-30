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

package io.kadai.common.internal;

import io.kadai.KadaiConfiguration;
import io.kadai.common.api.KadaiEngine;
import java.sql.SQLException;

public interface SpringKadaiEngine extends KadaiEngine {

  /**
   * This method creates the {@linkplain SpringKadaiEngine} with {@linkplain
   * ConnectionManagementMode#PARTICIPATE }.
   *
   * @see SpringKadaiEngine#buildKadaiEngine(KadaiConfiguration, ConnectionManagementMode)
   */
  @SuppressWarnings("checkstyle:JavadocMethod")
  static SpringKadaiEngine buildKadaiEngine(KadaiConfiguration configuration) throws SQLException {
    return SpringKadaiEngine.buildKadaiEngine(configuration, ConnectionManagementMode.PARTICIPATE);
  }

  /**
   * Builds an {@linkplain SpringKadaiEngine} based on {@linkplain KadaiConfiguration} and
   * SqlConnectionMode.
   *
   * @param configuration complete kadaiConfig to build the engine
   * @param connectionManagementMode connectionMode for the SqlSession
   * @return a {@linkplain SpringKadaiEngineImpl}
   * @throws SQLException when the db schema could not be initialized
   */
  static SpringKadaiEngine buildKadaiEngine(
      KadaiConfiguration configuration, ConnectionManagementMode connectionManagementMode)
      throws SQLException {
    return SpringKadaiEngineImpl.createKadaiEngine(configuration, connectionManagementMode);
  }
}
