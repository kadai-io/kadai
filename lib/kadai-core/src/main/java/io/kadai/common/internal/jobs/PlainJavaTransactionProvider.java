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

package io.kadai.common.internal.jobs;

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.KadaiEngine.ConnectionManagementMode;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.internal.KadaiEngineImpl;
import io.kadai.common.internal.transaction.KadaiTransactionProvider;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;

public class PlainJavaTransactionProvider implements KadaiTransactionProvider {

  private final KadaiEngine kadaiEngine;
  private final DataSource dataSource;
  private final ConnectionManagementMode defaultConnectionManagementMode;

  public PlainJavaTransactionProvider(KadaiEngine kadaiEngine, DataSource dataSource) {
    this.kadaiEngine = kadaiEngine;
    this.dataSource = dataSource;
    defaultConnectionManagementMode = kadaiEngine.getConnectionManagementMode();
  }

  @Override
  public <T> T executeInTransaction(Supplier<T> supplier) {
    if (((KadaiEngineImpl) kadaiEngine).getConnection() != null) {
      return supplier.get();
    }
    try (Connection connection = dataSource.getConnection()) {
      kadaiEngine.setConnection(connection);
      final T t = supplier.get();
      connection.commit();
      kadaiEngine.closeConnection();
      return t;
    } catch (SQLException ex) {
      throw new SystemException("caught exception", ex);
    } finally {
      kadaiEngine.setConnectionManagementMode(defaultConnectionManagementMode);
    }
  }
}
