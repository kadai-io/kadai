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
import java.sql.SQLException;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;

/** This class configures the KadaiEngine for spring. */
public class SpringKadaiEngineImpl extends KadaiEngineImpl implements SpringKadaiEngine {

  public SpringKadaiEngineImpl(KadaiConfiguration kadaiConfiguration, ConnectionManagementMode mode)
      throws SQLException {
    super(kadaiConfiguration, mode, new SpringManagedTransactionFactory());
  }

  public static SpringKadaiEngine createKadaiEngine(
      KadaiConfiguration kadaiConfiguration, ConnectionManagementMode connectionManagementMode)
      throws SQLException {
    return new SpringKadaiEngineImpl(kadaiConfiguration, connectionManagementMode);
  }
}
