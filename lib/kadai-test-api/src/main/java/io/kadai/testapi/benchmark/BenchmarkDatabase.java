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

package io.kadai.testapi.benchmark;

import static io.kadai.testapi.benchmark.BenchmarkLog.format;
import static io.kadai.testapi.benchmark.BenchmarkLog.print;
import static io.kadai.testapi.benchmark.BenchmarkLog.seconds;

import io.kadai.common.api.KadaiEngine;
import io.kadai.task.api.TaskState;
import io.kadai.testapi.generator.GenerationSummary;
import io.kadai.testapi.generator.TaskGenerationProfile;
import io.kadai.testapi.generator.TaskTestDataGenerator;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

/**
 * The PostgreSQL database of the {@code TaskUpdatePriorityJob} benchmarks: a dedicated container on
 * the settings of a mid-sized database server, seeded like the production system of an insurer.
 */
public final class BenchmarkDatabase {

  public static final String SCHEMA_NAME = "kadai";

  /** SQL condition that selects the tasks in a non-final state. */
  public static final String IS_OPEN =
      Arrays.stream(TaskState.END_STATES)
          .map(state -> "'" + state.name() + "'")
          .collect(Collectors.joining(", ", "STATE NOT IN (", ")"));

  // Too little to cache the default data set, as on many production database servers. The fixed
  // limit also keeps the results independent of what else runs on the host.
  private static final long MEMORY_BYTES = 16L * 1024 * 1024 * 1024;
  private static final int SEED_BATCH_SIZE = 25_000;
  // a dedicated, mid-sized database server; fsync stays on as in production
  private static final List<String> POSTGRES_SETTINGS =
      List.of(
          "shared_buffers=4GB",
          "effective_cache_size=12GB",
          "work_mem=32MB",
          "maintenance_work_mem=1GB",
          "max_wal_size=16GB",
          "random_page_cost=1.1");
  // checkpointing rarely loads the data faster
  private static final String SEEDING_CHECKPOINT_TIMEOUT = "30min";

  private BenchmarkDatabase() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Starts PostgreSQL 17 with the German locale of {@code DockerContainerCreator} and the settings
   * of a dedicated, mid-sized database server.
   *
   * @return the started container
   * @throws SQLException if the database cannot be configured
   */
  public static PostgreSQLContainer<?> start() throws SQLException {
    String settings =
        POSTGRES_SETTINGS.stream().map(setting -> "-c " + setting).collect(Collectors.joining(" "));
    PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>(DockerImageName.parse("postgres:17"))
            .withUsername("postgres")
            .withPassword("postgres")
            .withDatabaseName("postgres")
            .withSharedMemorySize(1024L * 1024 * 1024)
            .withCommand(
                "/bin/sh",
                "-c",
                "localedef -i de_DE -c -f UTF-8 -A /usr/share/locale/locale.alias de_DE.UTF-8"
                    + " && export LANG=de_DE.UTF-8"
                    + " && /usr/local/bin/docker-entrypoint.sh postgres "
                    + settings)
            .waitingFor(
                new LogMessageWaitStrategy()
                    .withRegEx(".*Datenbanksystem ist bereit, um Verbindungen anzunehmen.*\\s")
                    .withTimes(2)
                    .withStartupTimeout(Duration.ofSeconds(60)));
    postgres.start();
    // not on the command line, which would take precedence over a later ALTER SYSTEM
    try (Connection connection = postgres.createConnection("");
        Statement statement = connection.createStatement()) {
      setCheckpointTimeout(statement, SEEDING_CHECKPOINT_TIMEOUT);
    }
    return postgres;
  }

  /**
   * Makes the database checkpoint every 5 minutes, PostgreSQL's default, instead of every 30
   * minutes as while seeding. Then the writes of the job reach the disk during a measurement of a
   * few minutes, too.
   *
   * @param dataSource the database
   * @throws SQLException if the database cannot be configured
   */
  public static void checkpointByDefault(DataSource dataSource) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {
      connection.setAutoCommit(true);
      setCheckpointTimeout(statement, "5min");
    }
  }

  /**
   * Persists production-like tasks with the {@link TaskTestDataGenerator} and a {@link
   * TaskGenerationProfile}. A single COPY stream keeps one database core busy, so disjoint ranges
   * of tasks are loaded concurrently.
   *
   * @param kadaiEngine the engine whose configuration the data has to match
   * @param workbaskets the number of workbaskets, spread over the configured domains
   * @param tasks the number of tasks
   * @param openTasks the number of tasks in a non-final state
   * @param dataSource the database, to report the progress
   * @return the duration of the seeding
   * @throws InterruptedException if interrupted while waiting for the seeding
   * @throws ExecutionException if a part of the seeding failed
   */
  public static Duration seed(
      KadaiEngine kadaiEngine, int workbaskets, long tasks, long openTasks, DataSource dataSource)
      throws InterruptedException, ExecutionException {
    // without the master domain, which holds no workbaskets
    long domains =
        kadaiEngine.getConfiguration().getDomains().stream()
            .filter(domain -> !domain.isBlank())
            .count();
    TaskTestDataGenerator generator =
        TaskTestDataGenerator.from(
            kadaiEngine,
            new TaskGenerationProfile(
                (double) openTasks / tasks, (int) Math.ceilDiv(workbaskets, domains)));
    print("Seeding %,d tasks (%,d open) into %,d workbaskets ...", tasks, openTasks, workbaskets);
    int streams = Math.max(1, Runtime.getRuntime().availableProcessors() * 2 / 3);
    long tasksPerStream = Math.ceilDiv(tasks, streams);
    long start = System.nanoTime();
    try (ExecutorService executor = Executors.newFixedThreadPool(streams);
        ScheduledExecutorService heartbeat = Executors.newSingleThreadScheduledExecutor()) {
      heartbeat.scheduleAtFixedRate(
          () -> printSeedingProgress(dataSource, start), 1, 1, TimeUnit.MINUTES);
      List<Future<GenerationSummary>> parts = new ArrayList<>();
      for (long firstIndex = 0; firstIndex < tasks; firstIndex += tasksPerStream) {
        long first = firstIndex;
        long count = Math.min(tasksPerStream, tasks - firstIndex);
        parts.add(executor.submit(() -> generator.persist(first, count, SEED_BATCH_SIZE)));
      }
      for (Future<GenerationSummary> part : parts) {
        part.get();
      }
      heartbeat.shutdownNow();
    }
    Duration duration = Duration.ofNanos(System.nanoTime() - start);
    print("Seeded in %s (%,.0f tasks/s)", format(duration), tasks / seconds(duration));
    return duration;
  }

  /**
   * Brings the freshly loaded database into the state of a production database: statistics are up
   * to date and no maintenance work is pending that would compete with the measurement.
   *
   * @param dataSource the database
   * @throws SQLException if the maintenance fails
   */
  public static void settle(DataSource dataSource) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {
      connection.setAutoCommit(true);
      connection.setSchema(SCHEMA_NAME);
      print("Vacuuming and analyzing ...");
      statement.execute(
          "VACUUM (ANALYZE) TASK, ATTACHMENT, OBJECT_REFERENCE, CLASSIFICATION, WORKBASKET");
      statement.execute("CHECKPOINT");
    }
  }

  /**
   * Limits the memory of the database container, after the seeding, which runs faster without the
   * limit.
   *
   * @param postgres the database container
   */
  public static void limitMemory(PostgreSQLContainer<?> postgres) {
    try {
      postgres
          .getDockerClient()
          .updateContainerCmd(postgres.getContainerId())
          .withMemory(MEMORY_BYTES)
          .withMemorySwap(MEMORY_BYTES)
          .exec();
      print("Limited the database to %d GiB of memory", MEMORY_BYTES >> 30);
    } catch (RuntimeException e) {
      print(
          "WARNING: could not limit the memory of the database, the results depend on the free"
              + " memory of the host: %s",
          e.getMessage());
    }
  }

  /**
   * Counts what the database holds.
   *
   * @param dataSource the database
   * @return the counts
   * @throws SQLException if a count fails
   */
  public static DataSet describe(DataSource dataSource) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {
      connection.setSchema(SCHEMA_NAME);
      Map<TaskState, Long> tasksByState = new EnumMap<>(TaskState.class);
      long openTasksWithoutManualPriority = 0;
      try (ResultSet resultSet =
          statement.executeQuery(
              "SELECT STATE, COUNT(*), COUNT(*) FILTER (WHERE MANUAL_PRIORITY < 0) FROM TASK"
                  + " GROUP BY STATE")) {
        while (resultSet.next()) {
          TaskState state = TaskState.valueOf(resultSet.getString(1));
          tasksByState.put(state, resultSet.getLong(2));
          if (!state.isEndState()) {
            openTasksWithoutManualPriority += resultSet.getLong(3);
          }
        }
      }
      return new DataSet(
          tasksByState,
          openTasksWithoutManualPriority,
          queryLong(statement, "SELECT COUNT(*) FROM WORKBASKET"),
          queryLong(statement, "SELECT COUNT(*) FROM ATTACHMENT"),
          queryLong(statement, "SELECT COUNT(*) FROM OBJECT_REFERENCE"),
          queryLong(statement, "SELECT pg_database_size(current_database())"));
    }
  }

  /**
   * Runs a query that returns a single number.
   *
   * @param statement the statement to run the query with
   * @param sql the query
   * @return the number in the first column of the first row
   * @throws SQLException if the query fails
   */
  public static long queryLong(Statement statement, String sql) throws SQLException {
    try (ResultSet resultSet = statement.executeQuery(sql)) {
      resultSet.next();
      return resultSet.getLong(1);
    }
  }

  private static void setCheckpointTimeout(Statement statement, String timeout)
      throws SQLException {
    statement.execute("ALTER SYSTEM SET checkpoint_timeout = '" + timeout + "'");
    statement.execute("SELECT pg_reload_conf()");
  }

  private static void printSeedingProgress(DataSource dataSource, long start) {
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {
      print(
          "  %s: database at %.1f GB",
          format(Duration.ofNanos(System.nanoTime() - start)),
          queryLong(statement, "SELECT pg_database_size(current_database())") / 1e9);
    } catch (SQLException e) {
      print("  could not determine the database size: %s", e.getMessage());
    }
  }

  /**
   * What the database holds.
   *
   * @param tasksByState the number of tasks per state
   * @param openTasksWithoutManualPriority the number of tasks the priority job recalculates
   * @param workbaskets the number of workbaskets
   * @param attachments the number of attachments
   * @param objectReferences the number of secondary object references
   * @param databaseBytes the size of the database
   */
  public record DataSet(
      Map<TaskState, Long> tasksByState,
      long openTasksWithoutManualPriority,
      long workbaskets,
      long attachments,
      long objectReferences,
      long databaseBytes) {

    public long tasks() {
      return count(false) + count(true);
    }

    public long openTasks() {
      return count(false);
    }

    @Override
    public String toString() {
      return String.format(
          Locale.ROOT,
          "Data set: %,d tasks, %,d workbaskets, %,d attachments, %,d object references, database"
              + " %.1f GB%n  open tasks: %,d (%s), %,d without manual priority%n  finished tasks:"
              + " %,d (%s)",
          tasks(),
          workbaskets,
          attachments,
          objectReferences,
          databaseBytes / 1e9,
          openTasks(),
          breakdown(false),
          openTasksWithoutManualPriority,
          count(true),
          breakdown(true));
    }

    private long count(boolean finished) {
      return tasksByState.entrySet().stream()
          .filter(entry -> entry.getKey().isEndState() == finished)
          .mapToLong(Map.Entry::getValue)
          .sum();
    }

    private String breakdown(boolean finished) {
      return tasksByState.entrySet().stream()
          .filter(entry -> entry.getKey().isEndState() == finished)
          .map(entry -> String.format(Locale.ROOT, "%s %,d", entry.getKey(), entry.getValue()))
          .collect(Collectors.joining(", "));
    }
  }
}
