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

package io.kadai.common.test.config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

/**
 * Wraps a {@link DataSource} and fails fast when SQL statements are prepared before the schema was
 * explicitly initialized on the current {@link Connection}.
 */
public final class SchemaEnforcingDataSource {

  public interface DelegatingDataSource {

    DataSource getDelegateDataSource();
  }

  private final DataSource delegate;
  private final String expectedSchema;
  private final DataSource proxiedDataSource;
  private volatile boolean enforcementEnabled;

  public SchemaEnforcingDataSource(DataSource delegate, String expectedSchema) {
    this.delegate = delegate;
    this.expectedSchema = expectedSchema;
    this.proxiedDataSource = createProxy();
  }

  public DataSource asDataSource() {
    return proxiedDataSource;
  }

  public static DataSource unwrap(DataSource dataSource) {
    if (dataSource instanceof DelegatingDataSource delegatingDataSource) {
      return delegatingDataSource.getDelegateDataSource();
    }

    return dataSource;
  }

  private DataSource createProxy() {
    return (DataSource)
        Proxy.newProxyInstance(
            SchemaEnforcingDataSource.class.getClassLoader(),
            new Class<?>[] {DataSource.class, DelegatingDataSource.class},
            (proxy, method, args) -> {
              if ("getDelegateDataSource".equals(method.getName())) {
                return delegate;
              }

              if ("getConnection".equals(method.getName())) {
                Connection connection = (Connection) invoke(method, delegate, args);
                return wrapConnection(connection);
              }

              return invoke(method, delegate, args);
            });
  }

  public void enable() {
    enforcementEnabled = true;
  }

  public void disable() {
    enforcementEnabled = false;
  }

  public static Connection initializeSchema(Connection connection, String schemaName)
      throws SQLException {
    try {
      connection.setSchema(schemaName);
    } catch (SQLException e) {
      try (Statement statement = connection.createStatement()) {
        statement.execute("SET SCHEMA " + schemaName);
      }
    }

    return connection;
  }

  private Connection wrapConnection(Connection delegate) {
    boolean[] schemaInitialized = {false};

    return (Connection)
        Proxy.newProxyInstance(
            Connection.class.getClassLoader(),
            new Class<?>[] {Connection.class},
            (proxy, method, args) -> {
              if ("setSchema".equals(method.getName()) && args != null && args.length == 1) {
                Object result = invoke(method, delegate, args);
                schemaInitialized[0] =
                    args[0] instanceof String schema && expectedSchema.equalsIgnoreCase(schema);
                return result;
              }

              if (enforcementEnabled
                  && requiresInitializedSchema(method)
                  && !schemaInitialized[0]) {
                throw new SQLException("Schema was not initialized before querying KADAI tables.");
              }

              return invoke(method, delegate, args);
            });
  }

  private static boolean requiresInitializedSchema(Method method) {
    String methodName = method.getName();
    return "prepareStatement".equals(methodName) || "prepareCall".equals(methodName);
  }

  private static Object invoke(Method method, Object target, Object[] args) throws Throwable {
    try {
      return method.invoke(target, args);
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }
}
