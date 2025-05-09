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

package io.kadai.common.test.config;

import io.kadai.common.internal.configuration.DB;
import io.kadai.common.test.DockerContainerCreator;
import java.util.Optional;
import javax.sql.DataSource;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.testcontainers.containers.JdbcDatabaseContainer;

/**
 * The DataSourceGenerator provides the proper {@linkplain DataSource} for all Integration tests.
 *
 * <p>Additionally the property <b>schemaName</b> can be defined. If that property is missing, or
 * the file doesn't exist the schemaName KADAI will be used.
 */
public final class DataSourceGenerator {

  private static final DataSource DATA_SOURCE;
  private static final String SCHEMA_NAME;

  private static final int POOL_TIME_TO_WAIT = 50;

  static {
    DB db = retrieveDatabaseFromEnv();
    Optional<JdbcDatabaseContainer<?>> dockerContainer =
        DockerContainerCreator.createDockerContainer(db);

    SCHEMA_NAME = determineSchemaName(db);
    if (dockerContainer.isPresent()) {
      dockerContainer.get().start();
      DATA_SOURCE = DockerContainerCreator.createDataSource(dockerContainer.get());
    } else {
      DATA_SOURCE = createDataSourceForH2();
    }
  }

  private DataSourceGenerator() {}

  public static DataSource getDataSource() {
    return DATA_SOURCE;
  }

  public static String getSchemaName() {
    return SCHEMA_NAME;
  }

  public static DataSource createDataSourceForH2() {
    String jdbcDriver = "org.h2.Driver";
    String jdbcUrl =
        "jdbc:h2:mem:kadai;NON_KEYWORDS=KEY,VALUE;LOCK_MODE=0;"
            + "INIT=CREATE SCHEMA IF NOT EXISTS KADAI\\;"
            + "SET COLLATION DEFAULT_de_DE ";
    String dbUserName = "sa";
    String dbPassword = "sa";
    PooledDataSource ds =
        new PooledDataSource(
            Thread.currentThread().getContextClassLoader(),
            jdbcDriver,
            jdbcUrl,
            dbUserName,
            dbPassword);
    ds.setPoolTimeToWait(POOL_TIME_TO_WAIT);
    ds.forceCloseAll(); // otherwise, the MyBatis pool is not initialized correctly

    return ds;
  }

  private static String determineSchemaName(DB db) {
    return db == DB.POSTGRES ? "kadai" : "KADAI";
  }

  private static DB retrieveDatabaseFromEnv() {
    String property = System.getenv("DB");
    DB db;
    try {
      db = DB.valueOf(property);
    } catch (Exception ex) {
      db = DB.H2;
    }
    return db;
  }
}
