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

import static java.time.temporal.ChronoUnit.SECONDS;

import io.kadai.KadaiConfiguration;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.KadaiEngine.ConnectionManagementMode;
import io.kadai.common.internal.jobs.JobTransactionPolicy;
import io.kadai.common.internal.jobs.PlainJavaTransactionProvider;
import io.kadai.common.internal.util.SpiLoader;
import io.kadai.spi.priority.api.PriorityServiceProvider;
import io.kadai.task.api.TaskState;
import io.kadai.task.internal.jobs.TaskUpdatePriorityJob;
import io.kadai.testapi.DockerContainerCreator;
import io.kadai.testapi.benchmark.SlaPriorityServiceProvider.TimeBudgetExhaustedException;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

/**
 * Benchmarks the {@link TaskUpdatePriorityJob} against a database sized like the production system
 * of an insurer: by default 15 million tasks of which 1 million are open.
 *
 * <p>The runner seeds a fresh PostgreSQL container with {@link TaskTestDataGenerator} and a {@link
 * TaskGenerationProfile}, then runs the job once the way the {@code JobScheduler} does: as admin,
 * in the transaction the job's policy asks for. Priorities come from the {@link
 * SlaPriorityServiceProvider}, which gives every open task a new priority: the job has to
 * recalculate and update all of them, its worst case. If the job outlasts the time budget, it is
 * stopped and its total duration is extrapolated from the share of tasks it processed.
 *
 * <p>Parameters (system properties):
 *
 * <ul>
 *   <li>{@code benchmark.workbaskets}: number of workbaskets (default 3,000)
 *   <li>{@code benchmark.tasksPerWorkbasket}: average number of tasks per workbasket (default
 *       5,000); group and topic workbaskets hold more tasks than personal ones
 *   <li>{@code benchmark.openTasks}: number of tasks in a non-final state (default 1,000,000)
 *   <li>{@code benchmark.jobBatchSize}: {@code kadai.jobs.priority.task.batchSize} (default:
 *       KADAI's default)
 *   <li>{@code benchmark.timeBudget}: ISO-8601 duration after which the job is stopped (default
 *       PT20M)
 * </ul>
 *
 * <p>Run it from the IDE, or with Maven after installing the modules it depends on - including your
 * changes to {@code kadai-core} - with {@code ./mvnw install -pl lib/kadai-test-api -am
 * -DskipTests}:
 *
 * <pre>{@code
 * ./mvnw -pl lib/kadai-test-api test-compile exec:java -Dexec.classpathScope=test \
 *   -Dexec.mainClass=io.kadai.testapi.benchmark.TaskUpdatePriorityJobBenchmarkRunner \
 *   -Dexec.cleanupDaemonThreads=false -Dbenchmark.timeBudget=PT30M
 * }</pre>
 *
 * <p>The default data set takes about 40 GB of disk inside Docker. For the job, the database is
 * limited to 16 GiB of memory, too little to cache the data set, as on many production servers. So
 * the job mostly waits for the disk: compare runs on the same machine only.
 */
public final class TaskUpdatePriorityJobBenchmarkRunner {

  private static final String SCHEMA_NAME = "kadai";
  // Too little to cache the default data set, as on many production database servers. The fixed
  // limit also keeps the results independent of what else runs on the host.
  private static final long DATABASE_MEMORY_BYTES = 16L * 1024 * 1024 * 1024;
  private static final int SEED_BATCH_SIZE = 25_000;
  private static final Duration PROGRESS_INTERVAL = Duration.ofSeconds(30);
  private static final String IS_OPEN =
      Arrays.stream(TaskState.END_STATES)
          .map(state -> "'" + state.name() + "'")
          .collect(Collectors.joining(", ", "STATE NOT IN (", ")"));
  // a dedicated, mid-sized database server; fsync stays on as in production
  private static final List<String> POSTGRES_SETTINGS =
      List.of(
          "shared_buffers=4GB",
          "effective_cache_size=12GB",
          "work_mem=32MB",
          "maintenance_work_mem=1GB",
          "max_wal_size=16GB",
          "checkpoint_timeout=30min",
          "random_page_cost=1.1");

  private TaskUpdatePriorityJobBenchmarkRunner() {
    throw new IllegalStateException("Utility class");
  }

  public static void main(String[] args) throws Exception {
    int workbaskets = Integer.getInteger("benchmark.workbaskets", 3_000);
    long tasksPerWorkbasket = Long.getLong("benchmark.tasksPerWorkbasket", 5_000L);
    long openTasks = Long.getLong("benchmark.openTasks", 1_000_000L);
    Integer jobBatchSize = Integer.getInteger("benchmark.jobBatchSize");
    Duration timeBudget = Duration.parse(System.getProperty("benchmark.timeBudget", "PT20M"));
    long tasks = workbaskets * tasksPerWorkbasket;
    if (workbaskets <= 0 || tasksPerWorkbasket <= 0 || openTasks <= 0 || openTasks > tasks) {
      throw new IllegalArgumentException(
          "Expected positive workbaskets and tasksPerWorkbasket and 0 < openTasks <= " + tasks);
    }

    try (PostgreSQLContainer<?> postgres = startPostgres()) {
      DataSource dataSource = DockerContainerCreator.createDataSource(postgres);
      SlaPriorityServiceProvider priorityProvider = new SlaPriorityServiceProvider();
      KadaiEngine kadaiEngine = createKadaiEngine(dataSource, jobBatchSize, priorityProvider);

      // without the master domain, which holds no workbaskets
      int domains =
          (int)
              kadaiEngine.getConfiguration().getDomains().stream()
                  .filter(domain -> !domain.isBlank())
                  .count();
      TaskGenerationProfile profile =
          new TaskGenerationProfile((double) openTasks / tasks, Math.ceilDiv(workbaskets, domains));
      print("Seeding %,d tasks (%,d open) into %,d workbaskets ...", tasks, openTasks, workbaskets);
      long seedStart = System.nanoTime();
      seed(TaskTestDataGenerator.from(kadaiEngine, profile), tasks, dataSource);
      Duration seedDuration = Duration.ofNanos(System.nanoTime() - seedStart);
      print("Seeded in %s (%,.0f tasks/s)", format(seedDuration), tasks / seconds(seedDuration));

      settle(dataSource);
      limitMemory(postgres);
      DataSet dataSet = describe(dataSource);
      print("%s", dataSet);

      JobRun run = runJob(kadaiEngine, priorityProvider, timeBudget, dataSet);
      report(kadaiEngine, timeBudget, seedDuration, dataSet, run);
      if (!run.stopped()) {
        verifyPersistedPriorities(dataSource, run.changes());
      }
    }
  }

  // A single COPY stream keeps one database core busy, so disjoint ranges are loaded concurrently.
  private static void seed(TaskTestDataGenerator generator, long tasks, DataSource dataSource)
      throws Exception {
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

  private static PostgreSQLContainer<?> startPostgres() {
    String settings =
        POSTGRES_SETTINGS.stream().map(setting -> "-c " + setting).collect(Collectors.joining(" "));
    // same German locale as DockerContainerCreator, plus the settings above
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
                    .withStartupTimeout(Duration.of(60, SECONDS)));
    postgres.start();
    return postgres;
  }

  private static KadaiEngine createKadaiEngine(
      DataSource dataSource, Integer jobBatchSize, PriorityServiceProvider priorityProvider)
      throws SQLException {
    KadaiConfiguration.Builder configuration =
        new KadaiConfiguration.Builder(dataSource, false, SCHEMA_NAME).initKadaiProperties();
    if (jobBatchSize != null) {
      configuration.taskUpdatePriorityJobBatchSize(jobBatchSize);
    }
    // register the provider the way the test API's @WithServiceProvider does
    try (MockedStatic<SpiLoader> spiLoader = Mockito.mockStatic(SpiLoader.class)) {
      spiLoader
          .when(() -> SpiLoader.load(PriorityServiceProvider.class))
          .thenReturn(List.of(priorityProvider));
      return KadaiEngine.buildKadaiEngine(
          configuration.build(), ConnectionManagementMode.AUTOCOMMIT);
    }
  }

  // Brings the freshly loaded database into the state of a production database: statistics are up
  // to date and no maintenance work is pending that would compete with the job.
  private static void settle(DataSource dataSource) throws SQLException {
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

  // Applied after seeding, which runs faster without the limit.
  private static void limitMemory(PostgreSQLContainer<?> postgres) {
    try {
      postgres
          .getDockerClient()
          .updateContainerCmd(postgres.getContainerId())
          .withMemory(DATABASE_MEMORY_BYTES)
          .withMemorySwap(DATABASE_MEMORY_BYTES)
          .exec();
      print("Limited the database to %d GiB of memory", DATABASE_MEMORY_BYTES >> 30);
    } catch (RuntimeException e) {
      print(
          "WARNING: could not limit the memory of the database, the results depend on the free"
              + " memory of the host: %s",
          e.getMessage());
    }
  }

  private static DataSet describe(DataSource dataSource) throws SQLException {
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

  private static JobRun runJob(
      KadaiEngine kadaiEngine,
      SlaPriorityServiceProvider priorityProvider,
      Duration timeBudget,
      DataSet dataSet) {
    PlainJavaTransactionProvider txProvider =
        new PlainJavaTransactionProvider(
            kadaiEngine, kadaiEngine.getConfiguration().getDataSource());
    TaskUpdatePriorityJob job = new TaskUpdatePriorityJob(kadaiEngine, txProvider, null);
    print(
        "Running TaskUpdatePriorityJob (batch size %,d, time budget %s) ...",
        job.getBatchSize(), format(timeBudget));

    ScheduledExecutorService progress = Executors.newSingleThreadScheduledExecutor();
    long start = System.nanoTime();
    priorityProvider.startMetering(timeBudget);
    progress.scheduleAtFixedRate(
        () -> printProgress(priorityProvider, dataSet, start),
        PROGRESS_INTERVAL.toSeconds(),
        PROGRESS_INTERVAL.toSeconds(),
        TimeUnit.SECONDS);
    boolean stopped = false;
    try {
      // like JobRunner, without scheduling and locking the job
      kadaiEngine.runAsAdmin(() -> {
        if (job.getTransactionPolicy() == JobTransactionPolicy.WHOLE_JOB) {
          txProvider.executeInTransaction(() -> {
            job.execute();
            return null;
          });
        } else {
          job.execute();
        }
      });
    } catch (RuntimeException e) {
      if (!causedByExhaustedTimeBudget(e)) {
        throw e;
      }
      stopped = true;
    } finally {
      progress.shutdownNow();
    }
    long end = System.nanoTime();
    return new JobRun(
        stopped,
        Duration.ofNanos(end - start),
        firstCalculationOffset(priorityProvider, start, end),
        priorityProvider.calculations(),
        priorityProvider.changes());
  }

  private static void report(
      KadaiEngine kadaiEngine,
      Duration timeBudget,
      Duration seedDuration,
      DataSet dataSet,
      JobRun run) {
    long expected = dataSet.openTasksWithoutManualPriority();
    Duration processingDuration = run.duration().minus(run.timeToFirstCalculation());
    Duration estimatedDuration = run.duration();
    if (run.stopped() && run.calculations() > 0) {
      estimatedDuration =
          run.timeToFirstCalculation()
              .plus(
                  Duration.ofNanos(
                      (long)
                          (processingDuration.toNanos()
                              * ((double) expected / run.calculations()))));
    }

    double throughput = run.calculations() / Math.max(seconds(processingDuration), 1e-9);

    if (!run.stopped()) {
      print("Job finished in %s", format(run.duration()));
      if (run.calculations() != expected) {
        print(
            "WARNING: the job calculated %,d priorities, but %,d open tasks have no manual"
                + " priority",
            run.calculations(), expected);
      }
    } else if (run.calculations() == 0) {
      print("Job STOPPED after %s before it calculated a single priority", format(timeBudget));
    } else {
      print(
          "Job STOPPED after %s - estimated total duration %s, extrapolated from %.1f%% processed",
          format(run.duration()), format(estimatedDuration), 100.0 * run.calculations() / expected);
    }
    print(
        "  time to first priority calculation: %s (collecting the ids of the open tasks)",
        format(run.timeToFirstCalculation()));
    print(
        "  priorities calculated: %,d of %,d (%,.0f tasks/s), changed: %,d",
        run.calculations(), expected, throughput, run.changes());
    if (run.changes() != run.calculations()) {
      print(
          "WARNING: only %,d of %,d calculated priorities changed - the job did not have to update"
              + " every task",
          run.changes(), run.calculations());
    }

    System.out.printf(
        Locale.ROOT,
        "BENCHMARK_RESULT outcome=%s tasks=%d openTasks=%d expectedCalculations=%d workbaskets=%d"
            + " attachments=%d objectReferences=%d databaseBytes=%d jobBatchSize=%d"
            + " timeBudgetSeconds=%d seedSeconds=%.1f timeToFirstCalculationSeconds=%.1f"
            + " jobSeconds=%.1f estimatedJobSeconds=%.1f calculations=%d changes=%d"
            + " tasksPerSecond=%.1f cpus=%d%n",
        run.stopped() ? "STOPPED" : "FINISHED",
        dataSet.tasks(),
        dataSet.openTasks(),
        expected,
        dataSet.workbaskets(),
        dataSet.attachments(),
        dataSet.objectReferences(),
        dataSet.databaseBytes(),
        kadaiEngine.getConfiguration().getTaskUpdatePriorityJobBatchSize(),
        timeBudget.toSeconds(),
        seconds(seedDuration),
        seconds(run.timeToFirstCalculation()),
        seconds(run.duration()),
        seconds(estimatedDuration),
        run.calculations(),
        run.changes(),
        throughput,
        Runtime.getRuntime().availableProcessors());
  }

  // Seeded tasks carry their classification's priority, so after a complete run exactly the changed
  // tasks deviate from it. This catches jobs that calculate priorities but don't persist them.
  private static void verifyPersistedPriorities(DataSource dataSource, long changes)
      throws SQLException {
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {
      connection.setSchema(SCHEMA_NAME);
      long deviating =
          queryLong(
              statement,
              "SELECT COUNT(*) FROM TASK t JOIN CLASSIFICATION c ON c.ID = t.CLASSIFICATION_ID"
                  + " WHERE "
                  + IS_OPEN
                  + " AND t.MANUAL_PRIORITY < 0 AND t.PRIORITY <> c.PRIORITY");
      if (deviating == changes) {
        print("Verified that all %,d changed priorities are persisted", changes);
      } else {
        print(
            "WARNING: %,d priorities changed, but %,d open tasks deviate from the priority of"
                + " their classification",
            changes, deviating);
      }
    }
  }

  private static void printProgress(
      SlaPriorityServiceProvider priorityProvider, DataSet dataSet, long start) {
    long now = System.nanoTime();
    long calculations = priorityProvider.calculations();
    long firstCalculation = priorityProvider.firstCalculationNanos();
    Duration elapsed = Duration.ofNanos(now - start);
    if (calculations == 0) {
      print("  %s: collecting the ids of the open tasks ...", format(elapsed));
      return;
    }
    double rate = calculations / seconds(Duration.ofNanos(now - firstCalculation));
    long remaining = dataSet.openTasksWithoutManualPriority() - calculations;
    print(
        "  %s: %,d of %,d priorities calculated (%.1f%%), %,.0f tasks/s, %s to go",
        format(elapsed),
        calculations,
        dataSet.openTasksWithoutManualPriority(),
        100.0 * calculations / dataSet.openTasksWithoutManualPriority(),
        rate,
        format(Duration.ofSeconds((long) (remaining / rate))));
  }

  private static Duration firstCalculationOffset(
      SlaPriorityServiceProvider priorityProvider, long start, long end) {
    long firstCalculation = priorityProvider.firstCalculationNanos();
    return Duration.ofNanos((firstCalculation == 0 ? end : firstCalculation) - start);
  }

  private static boolean causedByExhaustedTimeBudget(Throwable throwable) {
    for (Throwable cause = throwable; cause != null; cause = cause.getCause()) {
      if (cause instanceof TimeBudgetExhaustedException) {
        return true;
      }
    }
    return false;
  }

  private static long queryLong(Statement statement, String sql) throws SQLException {
    try (ResultSet resultSet = statement.executeQuery(sql)) {
      resultSet.next();
      return resultSet.getLong(1);
    }
  }

  private static double seconds(Duration duration) {
    return duration.toNanos() / 1e9;
  }

  private static String format(Duration duration) {
    long seconds = duration.toSeconds();
    if (seconds < 60) {
      return String.format(Locale.ROOT, "%.1fs", seconds(duration));
    }
    return String.format(
        Locale.ROOT, "%dh %02dm %02ds", seconds / 3600, seconds % 3600 / 60, seconds % 60);
  }

  private static void print(String format, Object... args) {
    System.out.printf(Locale.ROOT, "[benchmark] " + format + "%n", args);
  }

  private record DataSet(
      Map<TaskState, Long> tasksByState,
      long openTasksWithoutManualPriority,
      long workbaskets,
      long attachments,
      long objectReferences,
      long databaseBytes) {

    long tasks() {
      return count(false) + count(true);
    }

    long openTasks() {
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

  private record JobRun(
      boolean stopped,
      Duration duration,
      Duration timeToFirstCalculation,
      long calculations,
      long changes) {}
}
