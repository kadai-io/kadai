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

import static io.kadai.testapi.benchmark.BenchmarkDatabase.IS_OPEN;
import static io.kadai.testapi.benchmark.BenchmarkDatabase.SCHEMA_NAME;
import static io.kadai.testapi.benchmark.BenchmarkDatabase.queryLong;
import static io.kadai.testapi.benchmark.BenchmarkLog.format;
import static io.kadai.testapi.benchmark.BenchmarkLog.print;
import static io.kadai.testapi.benchmark.BenchmarkLog.seconds;

import io.kadai.KadaiConfiguration;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.KadaiEngine.ConnectionManagementMode;
import io.kadai.common.internal.jobs.JobTransactionPolicy;
import io.kadai.common.internal.jobs.PlainJavaTransactionProvider;
import io.kadai.common.internal.util.SpiLoader;
import io.kadai.spi.priority.api.PriorityServiceProvider;
import io.kadai.task.internal.jobs.TaskUpdatePriorityJob;
import io.kadai.testapi.DockerContainerCreator;
import io.kadai.testapi.benchmark.BenchmarkDatabase.DataSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Benchmarks the {@link TaskUpdatePriorityJob} against a database sized like the production system
 * of an insurer: by default 15 million tasks of which 1 million are open.
 *
 * <p>The runner seeds a fresh PostgreSQL container with production-like data (see {@link
 * BenchmarkDatabase}), then runs the job once the way the {@code JobScheduler} does: as admin, in
 * the transaction the job's policy asks for. Priorities come from the {@link
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

  private static final Duration PROGRESS_INTERVAL = Duration.ofSeconds(30);

  private TaskUpdatePriorityJobBenchmarkRunner() {
    throw new IllegalStateException("Utility class");
  }

  public static void main(String[] args) throws Exception {
    BenchmarkLog.printTo(System.out);
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

    try (PostgreSQLContainer<?> postgres = BenchmarkDatabase.start()) {
      DataSource dataSource = DockerContainerCreator.createDataSource(postgres);
      SlaPriorityServiceProvider priorityProvider = new SlaPriorityServiceProvider();
      KadaiEngine kadaiEngine = createKadaiEngine(dataSource, jobBatchSize, priorityProvider);
      final Duration seedDuration =
          BenchmarkDatabase.seed(kadaiEngine, workbaskets, tasks, openTasks, dataSource);

      BenchmarkDatabase.settle(dataSource);
      BenchmarkDatabase.limitMemory(postgres);
      DataSet dataSet = BenchmarkDatabase.describe(dataSource);
      print("%s", dataSet);

      JobRun run = runJob(kadaiEngine, priorityProvider, timeBudget, dataSet);
      report(kadaiEngine, timeBudget, seedDuration, dataSet, run);
      if (!run.stopped()) {
        verifyPersistedPriorities(dataSource, run.changes());
      }
    }
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
      if (!priorityProvider.isTimeBudgetExhausted()) {
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

  private record JobRun(
      boolean stopped,
      Duration duration,
      Duration timeToFirstCalculation,
      long calculations,
      long changes) {}
}
