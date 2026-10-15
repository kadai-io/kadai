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

package io.kadai.testapi;

import static java.time.temporal.ChronoUnit.SECONDS;

import com.github.dockerjava.api.command.InspectContainerResponse;
import io.kadai.common.internal.configuration.DB;
import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import javax.sql.DataSource;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.ContainerLaunchException;
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
        try (Db2Container db2Container = createDb2Container()) {

          return Optional.of(
              db2Container
                  .withUsername("db2inst1")
                  .withPassword("db2inst1-pwd")
                  .withDatabaseName("KADDB"));
        }
      case POSTGRES:
        try (PostgreSQLContainer<?> selfPostgreSQLContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:17"))) {

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

  private static Db2Container createDb2Container() {
    return new Db2Container(DockerImageName.parse("icr.io/db2_community/db2:12.1.0.0")) {
      @Override
      protected void configure() {
        super.configure();
        // The image's default database has 4 KB pages; the KADAI schema needs 32 KB pages.
        addEnv("DBNAME", "");
      }

      @Override
      protected void containerIsStarted(InspectContainerResponse containerInfo) {
        super.containerIsStarted(containerInfo);
        try {
          Container.ExecResult result =
              execInContainer(
                  "su",
                  "-",
                  "db2inst1",
                  "-c",
                  "db2 create database KADDB using codeset utf-8 territory en-us "
                      + "collate using 'CLDRCOLL_LDE_AS_CX_EX_FX_HX_NX_S3' PAGESIZE 32 K");
          if (result.getExitCode() != 0) {
            throw new ContainerLaunchException(
                "Could not create KADDB: " + result.getStdout() + result.getStderr());
          }
        } catch (IOException e) {
          throw new ContainerLaunchException("Could not create KADDB", e);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new ContainerLaunchException("Interrupted while creating KADDB", e);
        }
      }
    };
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
