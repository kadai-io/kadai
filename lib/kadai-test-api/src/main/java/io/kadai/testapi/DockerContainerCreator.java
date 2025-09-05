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

package io.kadai.testapi;

import static java.time.temporal.ChronoUnit.SECONDS;

import io.kadai.common.internal.configuration.DB;
import java.time.Duration;
import java.util.Optional;
import javax.sql.DataSource;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.testcontainers.containers.Db2Container;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

/**
 * Utility-Class for creating dockerized databases and obtaining their
 * {@linkplain DataSource DataSources}.
 */
public class DockerContainerCreator {

  private DockerContainerCreator() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Creates a {@link JdbcDatabaseContainer} for a {@linkplain DB database}.
   *
   * @param db the database to create the container for
   * @return the container for the database if creation was successful, nothing otherwise
   */
  public static Optional<JdbcDatabaseContainer<?>> createDockerContainer(DB db) {
    switch (db) {
      case DB2:
        try (Db2Container db2Container =
            new Db2Container(
                DockerImageName.parse("taskana/db2:11.5")
                    .asCompatibleSubstituteFor("ibmcom/db2"))) {

          return Optional.of(
              db2Container
                  .waitingFor(
                      new LogMessageWaitStrategy()
                          .withRegEx(".*DB2START processing was successful.*")
                          .withStartupTimeout(Duration.of(60, SECONDS)))
                  .withUsername("db2inst1")
                  .withPassword("db2inst1-pwd")
                  .withDatabaseName("TSKDB"));
        }
      case POSTGRES:
        try (PostgreSQLContainer<?> selfPostgreSQLContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:14.7"))) {

          return Optional.of(
              selfPostgreSQLContainer
                  .withUsername("postgres")
                  .withPassword("postgres")
                  .withDatabaseName("postgres")
                  .withCommand(
                      "/bin/sh",
                      "-c",
                      """
                          localedef -i de_DE -c -f UTF-8 -A \
                          /usr/share/locale/locale.alias de_DE.UTF-8 \
                          && export LANG=de_DE.UTF-8 \
                          && /usr/local/bin/docker-entrypoint.sh postgres -c fsync=off""")
                  .waitingFor(
                      new LogMessageWaitStrategy()
                          .withRegEx(
                              ".*Datenbanksystem ist bereit, um Verbindungen anzunehmen.*\\s")
                          .withTimes(2)
                          .withStartupTimeout(Duration.of(60, SECONDS))));
        }
      default:
        return Optional.empty();
    }
  }

  /**
   * Creates a {@link DataSource} from a {@link JdbcDatabaseContainer}.
   *
   * @param container the container to create the data source from
   * @return the created data source
   */
  public static DataSource createDataSource(JdbcDatabaseContainer<?> container) {
    PooledDataSource ds =
        new PooledDataSource(
            Thread.currentThread().getContextClassLoader(),
            container.getDriverClassName(),
            container.getJdbcUrl(),
            container.getUsername(),
            container.getPassword());
    ds.setPoolTimeToWait(50);
    ds.forceCloseAll(); // otherwise, the MyBatis pool is not initialized correctly
    return ds;
  }
}
