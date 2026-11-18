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

package io.kadai.gmt.benchmark;

import static io.kadai.testapi.benchmark.BenchmarkDatabase.IS_OPEN;
import static io.kadai.testapi.benchmark.BenchmarkDatabase.SCHEMA_NAME;
import static io.kadai.testapi.benchmark.BenchmarkLog.format;
import static io.kadai.testapi.benchmark.BenchmarkLog.print;
import static io.kadai.testapi.benchmark.BenchmarkLog.seconds;

import io.kadai.KadaiConfiguration;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.KadaiEngine.ConnectionManagementMode;
import io.kadai.common.internal.jobs.JobTransactionPolicy;
import io.kadai.common.internal.jobs.PlainJavaTransactionProvider;
import io.kadai.common.internal.util.SpiLoader;
import io.kadai.gmt.GmtApplication;
import io.kadai.spi.priority.api.PriorityServiceProvider;
import io.kadai.task.internal.jobs.TaskUpdatePriorityJob;
import io.kadai.testapi.benchmark.BenchmarkDatabase;
import io.kadai.testapi.benchmark.BenchmarkDatabase.DataSet;
import io.kadai.testapi.benchmark.BenchmarkLog;
import io.kadai.testapi.benchmark.SlaPriorityServiceProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.utility.DockerImageName;

/**
 * Measures whether the {@link TaskUpdatePriorityJob} slows down the API: the response times of the
 * Blue Angel standard usage scenario, first without the job, then while the job runs.
 *
 * <p>The setup follows production: the {@link GmtApplication} serves the REST API and runs the job
 * in its own JVM, with the engine and transaction handling its {@code JobScheduler} uses. The
 * database is the one of the {@code TaskUpdatePriorityJobBenchmarkRunner}: 15 million tasks of
 * which 1 million are open, limited to 16 GiB of memory, and the {@link SlaPriorityServiceProvider}
 * gives every open task a new priority. Unlike there, the database checkpoints every 5 minutes, so
 * the writes of the job reach the disk while the traffic runs.
 *
 * <p>k6 replays {@code qa/gmt/k6/benchmark/sustained_standard_usage.js}: the steps of the Blue
 * Angel scenario, repeated by every user with 4 s pauses. After a warm-up, the same traffic runs
 * twice, each time with its own slice of tasks and workbaskets to read:
 *
 * <ol>
 *   <li>without the job,
 *   <li>with the job, which starts before and keeps running until the traffic ends.
 * </ol>
 *
 * <p>Parameters (system properties): those of the {@code TaskUpdatePriorityJobBenchmarkRunner} for
 * the data set ({@code benchmark.workbaskets}, {@code benchmark.tasksPerWorkbasket}, {@code
 * benchmark.openTasks}, {@code benchmark.jobBatchSize}), plus
 *
 * <ul>
 *   <li>{@code benchmark.users}: number of concurrent API users (default 100, as in the Blue Angel
 *       scenario)
 *   <li>{@code benchmark.trafficDuration}: ISO-8601 duration of each run (default PT10M)
 *   <li>{@code benchmark.warmUp}: ISO-8601 duration of the warm-up (default PT2M)
 * </ul>
 *
 * <p>Run it on Linux, as k6 reaches the application through the host network, after installing the
 * modules it depends on with {@code ./mvnw install -pl rest/kadai-gmt -am -DskipTests}:
 *
 * <pre>{@code
 * ./mvnw -pl rest/kadai-gmt test-compile exec:java -Dexec.classpathScope=test \
 *   -Dexec.mainClass=io.kadai.gmt.benchmark.TaskUpdatePriorityJobTrafficBenchmarkRunner \
 *   -Dexec.cleanupDaemonThreads=false
 * }</pre>
 */
public final class TaskUpdatePriorityJobTrafficBenchmarkRunner {

  // the k6 image of qa/gmt/compose.yml
  private static final String K6_IMAGE = "grafana/k6:2.3.0";
  private static final String K6_SCRIPT = "benchmark/sustained_standard_usage.js";
  // brings the job past collecting the ids of the open tasks, into its steady state
  private static final Duration JOB_LEAD_TIME = Duration.ofSeconds(30);
  private static final Duration JOB_PROGRESS_INTERVAL = Duration.ofMinutes(1);
  private static final List<String> RUNS = List.of("warm-up", "without-job", "with-job");
  private static final int TASKS_TO_READ_PER_RUN = 5_000;
  private static final int MAX_FORWARDED_K6_LINES = 10;

  private TaskUpdatePriorityJobTrafficBenchmarkRunner() {
    throw new IllegalStateException("Utility class");
  }

  public static void main(String[] args) throws Exception {
    BenchmarkLog.printTo(System.out);
    run();
    // the embedded LDAP server of the application uses non-daemon threads
    System.exit(0);
  }

  private static void run() throws Exception {
    int workbaskets = Integer.getInteger("benchmark.workbaskets", 3_000);
    long tasksPerWorkbasket = Long.getLong("benchmark.tasksPerWorkbasket", 5_000L);
    long openTasks = Long.getLong("benchmark.openTasks", 1_000_000L);
    Integer jobBatchSize = Integer.getInteger("benchmark.jobBatchSize");
    int users = Integer.getInteger("benchmark.users", 100);
    Duration trafficDuration =
        Duration.parse(System.getProperty("benchmark.trafficDuration", "PT10M"));
    Duration warmUp = Duration.parse(System.getProperty("benchmark.warmUp", "PT2M"));
    long tasks = workbaskets * tasksPerWorkbasket;
    if (workbaskets <= 0 || tasksPerWorkbasket <= 0 || openTasks <= 0 || openTasks > tasks) {
      throw new IllegalArgumentException(
          "Expected positive workbaskets and tasksPerWorkbasket and 0 < openTasks <= " + tasks);
    }
    Path k6Scripts = locateK6Scripts();

    try (PostgreSQLContainer<?> postgres = BenchmarkDatabase.start()) {
      // DockerContainerCreator needs the Testcontainers modules this application excludes
      PGSimpleDataSource dataSource = new PGSimpleDataSource();
      dataSource.setUrl(postgres.getJdbcUrl());
      dataSource.setUser(postgres.getUsername());
      dataSource.setPassword(postgres.getPassword());
      SlaPriorityServiceProvider priorityProvider = new SlaPriorityServiceProvider();
      try (Application application = startApplication(postgres, jobBatchSize, priorityProvider)) {
        BenchmarkDatabase.seed(
            application.context().getBean(KadaiEngine.class),
            workbaskets,
            tasks,
            openTasks,
            dataSource);
        BenchmarkDatabase.settle(dataSource);
        BenchmarkDatabase.checkpointByDefault(dataSource);
        BenchmarkDatabase.limitMemory(postgres);
        final DataSet dataSet = BenchmarkDatabase.describe(dataSource);
        print("%s", dataSet);
        Traffic traffic =
            new Traffic(k6Scripts, writeTrafficData(dataSource), application.port(), users);

        print("Warming up with %,d users for %s ...", users, format(warmUp));
        traffic.run("warm-up", warmUp);
        print("Run 1: %,d users for %s WITHOUT the job ...", users, format(trafficDuration));
        final Map<String, StepResult> withoutJob = traffic.run("without-job", trafficDuration);
        print("Run 2: %,d users for %s WITH the job ...", users, format(trafficDuration));
        BackgroundJob job = BackgroundJob.start(application.jobEngine(), priorityProvider);
        job.awaitSteadyState();
        long calculationsBefore = priorityProvider.calculations();
        long trafficStart = System.nanoTime();
        Map<String, StepResult> withJob = traffic.run("with-job", trafficDuration);
        long trafficEnd = System.nanoTime();
        double jobThroughput =
            (priorityProvider.calculations() - calculationsBefore)
                / seconds(Duration.ofNanos(trafficEnd - trafficStart));
        job.stop(trafficStart, trafficEnd);

        report(users, trafficDuration, dataSet, withoutJob, withJob, jobThroughput, traffic.data());
      }
    }
  }

  private static Application startApplication(
      PostgreSQLContainer<?> postgres,
      Integer jobBatchSize,
      PriorityServiceProvider priorityProvider)
      throws SQLException {
    String[] args = {
      "--server.port=0",
      "--management.server.port=0",
      "--spring.datasource.url=" + postgres.getJdbcUrl(),
      "--spring.datasource.username=" + postgres.getUsername(),
      "--spring.datasource.password=" + postgres.getPassword(),
      "--kadai.schemaName=" + SCHEMA_NAME,
      "--spring.main.banner-mode=off",
      "--logging.level.root=WARN"
    };
    // registers the provider for both engines; other service providers load as usual
    try (MockedStatic<SpiLoader> spiLoader =
        Mockito.mockStatic(SpiLoader.class, Mockito.CALLS_REAL_METHODS)) {
      spiLoader
          .when(() -> SpiLoader.load(PriorityServiceProvider.class))
          .thenReturn(List.of(priorityProvider));
      print("Starting the application ...");
      ConfigurableApplicationContext context = SpringApplication.run(GmtApplication.class, args);
      // the engine the JobScheduler of the application builds to run the jobs
      KadaiConfiguration.Builder jobConfiguration =
          new KadaiConfiguration.Builder(context.getBean(KadaiConfiguration.class))
              .jobSchedulerEnabled(false);
      if (jobBatchSize != null) {
        jobConfiguration.taskUpdatePriorityJobBatchSize(jobBatchSize);
      }
      KadaiEngine jobEngine =
          KadaiEngine.buildKadaiEngine(
              jobConfiguration.build(),
              ConnectionManagementMode.EXPLICIT,
              new SpringManagedTransactionFactory());
      int port = context.getEnvironment().getRequiredProperty("local.server.port", Integer.class);
      return new Application(context, jobEngine, port);
    }
  }

  // The tasks and workbaskets the users read are split between the runs, so no run reads what an
  // earlier run brought into the cache.
  private static Path writeTrafficData(DataSource dataSource) throws SQLException, IOException {
    Path directory = Files.createTempDirectory("kadai-traffic-benchmark");
    // k6 does not run as the current user
    Files.setPosixFilePermissions(directory, PosixFilePermissions.fromString("rwxrwxrwx"));
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {
      connection.setSchema(SCHEMA_NAME);
      List<List<String>> workbaskets =
          queryRows(statement, "SELECT ID, KEY, DOMAIN, TYPE FROM WORKBASKET ORDER BY ID");
      List<List<String>> classifications =
          queryRows(
              statement,
              "SELECT ID, KEY, DOMAIN, TYPE FROM CLASSIFICATION"
                  + " WHERE TYPE = 'TASK' AND DOMAIN <> '' ORDER BY ID");
      int tasksToRead = RUNS.size() * TASKS_TO_READ_PER_RUN;
      double openTasks =
          BenchmarkDatabase.queryLong(statement, "SELECT COUNT(*) FROM TASK WHERE " + IS_OPEN);
      double samplePercent = Math.clamp(200.0 * tasksToRead / openTasks, 0.01, 100);
      List<List<String>> openTaskIds =
          queryRows(
              statement,
              String.format(
                  Locale.ROOT,
                  "SELECT ID FROM TASK TABLESAMPLE SYSTEM (%f) REPEATABLE (42) WHERE %s LIMIT %d",
                  samplePercent,
                  IS_OPEN,
                  tasksToRead));

      String workbasketsJson =
          workbaskets.stream()
              .map(row ->
                  String.format(
                      "{\"workbasketId\":%s,\"key\":%s,\"domain\":%s,\"type\":%s}",
                      json(row.get(0)), json(row.get(1)), json(row.get(2)), json(row.get(3))))
              .collect(Collectors.joining(",", "[", "]"));
      String classificationsJson =
          classifications.stream()
              .map(row ->
                  String.format(
                      "{\"classificationId\":%s,\"key\":%s,\"domain\":%s,\"type\":%s}",
                      json(row.get(0)), json(row.get(1)), json(row.get(2)), json(row.get(3))))
              .collect(Collectors.joining(",", "[", "]"));
      for (int run = 0; run < RUNS.size(); run++) {
        String data =
            String.format(
                "{\"workbaskets\":%s,\"classifications\":%s,\"browsedWorkbasketIds\":%s,"
                    + "\"taskIds\":%s}",
                workbasketsJson,
                classificationsJson,
                jsonIds(slice(workbaskets, run)),
                jsonIds(slice(openTaskIds, run)));
        Files.writeString(directory.resolve("data-" + RUNS.get(run) + ".json"), data);
      }
    }
    return directory;
  }

  private static void report(
      int users,
      Duration trafficDuration,
      DataSet dataSet,
      Map<String, StepResult> withoutJob,
      Map<String, StepResult> withJob,
      double jobThroughput,
      Path resultsDirectory) {
    print(
        "Response times in ms of %,d users over %s - without the job | with the job:",
        users, format(trafficDuration));
    print(
        "  %-30s %-17s %-23s %-23s %-17s %-19s %s",
        "request", "   requests", "   median", "   p95", "   p99", "    max", "failed");
    for (StepResult without : withoutJob.values()) {
      StepResult with = withJob.get(without.step());
      print(
          "  %-30s %7d | %-7d %7.1f | %-7.1f%6s %7.1f | %-7.1f%6s %7.1f | %-7.1f %8.0f | %-8.0f"
              + " %4d | %-4d",
          without.label(),
          without.requests(),
          with.requests(),
          without.median(),
          with.median(),
          change(without.median(), with.median()),
          without.p95(),
          with.p95(),
          change(without.p95(), with.p95()),
          without.p99(),
          with.p99(),
          without.max(),
          with.max(),
          without.failed(),
          with.failed());
    }
    print("  job throughput while the traffic ran: %,.0f tasks/s", jobThroughput);
    print("  raw k6 results: %s", resultsDirectory);

    for (String run : List.of("without-job", "with-job")) {
      StepResult all = (run.equals("without-job") ? withoutJob : withJob).get("all");
      System.out.printf(
          Locale.ROOT,
          "BENCHMARK_RESULT run=%s tasks=%d openTasks=%d users=%d trafficSeconds=%d requests=%d"
              + " failed=%d avgMs=%.1f medianMs=%.1f p95Ms=%.1f p99Ms=%.1f maxMs=%.1f%s cpus=%d%n",
          run,
          dataSet.tasks(),
          dataSet.openTasks(),
          users,
          trafficDuration.toSeconds(),
          all.requests(),
          all.failed(),
          all.avg(),
          all.median(),
          all.p95(),
          all.p99(),
          all.max(),
          run.equals("with-job")
              ? String.format(Locale.ROOT, " jobTasksPerSecond=%.1f", jobThroughput)
              : "",
          Runtime.getRuntime().availableProcessors());
    }
  }

  private static String change(double before, double after) {
    if (before <= 0) {
      return "";
    }
    return String.format(Locale.ROOT, " %+.0f%%", 100 * (after - before) / before);
  }

  private static Path locateK6Scripts() {
    String configured = System.getProperty("benchmark.k6Scripts");
    if (configured != null) {
      return Path.of(configured).toAbsolutePath();
    }
    for (Path directory = Path.of("").toAbsolutePath();
        directory != null;
        directory = directory.getParent()) {
      Path scripts = directory.resolve("qa/gmt/k6");
      if (Files.isRegularFile(scripts.resolve(K6_SCRIPT))) {
        return scripts;
      }
    }
    throw new IllegalStateException(
        "Could not find qa/gmt/k6 above the working directory, set benchmark.k6Scripts");
  }

  private static List<List<String>> queryRows(Statement statement, String sql) throws SQLException {
    List<List<String>> rows = new ArrayList<>();
    try (ResultSet resultSet = statement.executeQuery(sql)) {
      int columns = resultSet.getMetaData().getColumnCount();
      while (resultSet.next()) {
        List<String> row = new ArrayList<>(columns);
        for (int column = 1; column <= columns; column++) {
          row.add(resultSet.getString(column));
        }
        rows.add(row);
      }
    }
    return rows;
  }

  private static List<List<String>> slice(List<List<String>> rows, int run) {
    List<List<String>> slice = new ArrayList<>();
    for (int index = run; index < rows.size(); index += RUNS.size()) {
      slice.add(rows.get(index));
    }
    if (slice.isEmpty()) {
      throw new IllegalStateException("Too few rows to split between the runs: " + rows.size());
    }
    return slice;
  }

  private static String jsonIds(List<List<String>> rows) {
    return rows.stream().map(row -> json(row.get(0))).collect(Collectors.joining(",", "[", "]"));
  }

  private static String json(String value) {
    return '"' + value.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
  }

  private record Application(
      ConfigurableApplicationContext context, KadaiEngine jobEngine, int port)
      implements AutoCloseable {

    @Override
    public void close() {
      context.close();
    }
  }

  private record StepResult(
      String step,
      String label,
      long requests,
      long failed,
      double avg,
      double median,
      double p95,
      double p99,
      double max) {

    static StepResult parse(String line) {
      String[] values = line.split("\t");
      return new StepResult(
          values[0],
          values[1],
          (long) Double.parseDouble(values[2]),
          (long) Double.parseDouble(values[3]),
          Double.parseDouble(values[4]),
          Double.parseDouble(values[5]),
          Double.parseDouble(values[6]),
          Double.parseDouble(values[7]),
          Double.parseDouble(values[8]));
    }
  }

  // Replays the usage scenario with k6, which reaches the application through the host network.
  private record Traffic(Path scripts, Path data, int port, int users) {

    Map<String, StepResult> run(String run, Duration duration) throws IOException {
      AtomicInteger forwardedLines = new AtomicInteger();
      try (GenericContainer<?> k6 =
          new GenericContainer<>(DockerImageName.parse(K6_IMAGE))
              .withNetworkMode("host")
              .withFileSystemBind(scripts.toString(), "/scripts", BindMode.READ_ONLY)
              .withFileSystemBind(data.toString(), "/work", BindMode.READ_WRITE)
              .withEnv("TARGET_HOSTNAME", "localhost")
              .withEnv("TARGET_PORT", String.valueOf(port))
              .withEnv("USERS", String.valueOf(users))
              .withEnv("DURATION", duration.toSeconds() + "s")
              .withEnv("DATA", "/work/data-" + run + ".json")
              .withEnv("RESULTS", "/work/results-" + run + ".tsv")
              .withEnv("K6_NO_USAGE_REPORT", "true")
              .withCommand("run", "--quiet", "/scripts/" + K6_SCRIPT)
              .withLogConsumer(frame -> {
                String line = frame.getUtf8StringWithoutLineEnding();
                if ((line.contains("level=warning") || line.contains("level=error"))
                    && forwardedLines.incrementAndGet() <= MAX_FORWARDED_K6_LINES) {
                  print("  k6: %s", line);
                }
              })
              .withStartupCheckStrategy(
                  new OneShotStartupCheckStrategy()
                      .withTimeout(duration.plus(Duration.ofMinutes(5))))) {
        k6.start();
      }
      Map<String, StepResult> results = new LinkedHashMap<>();
      List<String> lines = Files.readAllLines(data.resolve("results-" + run + ".tsv"));
      for (String line : lines.subList(1, lines.size())) {
        StepResult result = StepResult.parse(line);
        results.put(result.step(), result);
      }
      return results;
    }
  }

  // Runs the job like the JobRunner of the application's JobScheduler: as admin, in a transaction
  // if the job asks for one.
  private static final class BackgroundJob {

    private final SlaPriorityServiceProvider priorityProvider;
    private final Thread thread;
    private final ScheduledExecutorService progress = Executors.newSingleThreadScheduledExecutor();
    private volatile long finishedNanos;
    private volatile RuntimeException failure;

    private BackgroundJob(KadaiEngine jobEngine, SlaPriorityServiceProvider priorityProvider) {
      this.priorityProvider = priorityProvider;
      PlainJavaTransactionProvider txProvider =
          new PlainJavaTransactionProvider(jobEngine, jobEngine.getConfiguration().getDataSource());
      TaskUpdatePriorityJob job = new TaskUpdatePriorityJob(jobEngine, txProvider, null);
      thread =
          Thread.ofPlatform()
              .name("task-update-priority-job")
              .unstarted(() -> execute(jobEngine, txProvider, job));
    }

    static BackgroundJob start(KadaiEngine jobEngine, SlaPriorityServiceProvider priorityProvider) {
      BackgroundJob job = new BackgroundJob(jobEngine, priorityProvider);
      job.thread.start();
      job.progress.scheduleAtFixedRate(
          job::printProgress,
          JOB_PROGRESS_INTERVAL.toSeconds(),
          JOB_PROGRESS_INTERVAL.toSeconds(),
          TimeUnit.SECONDS);
      return job;
    }

    void awaitSteadyState() throws InterruptedException {
      print("  started the job, waiting until it calculates priorities ...");
      while (priorityProvider.calculations() == 0 && thread.isAlive()) {
        Thread.sleep(100);
      }
      if (!thread.isAlive()) {
        throw new IllegalStateException("The job ended before it calculated a priority", failure);
      }
      Thread.sleep(JOB_LEAD_TIME.toMillis());
      print("  the job runs, starting the traffic");
    }

    void stop(long trafficStart, long trafficEnd) throws InterruptedException {
      priorityProvider.endTimeBudget();
      thread.join();
      progress.shutdownNow();
      if (failure != null) {
        throw failure;
      }
      if (finishedNanos != 0 && finishedNanos < trafficEnd) {
        print(
            "WARNING: the job finished early, it ran during only %.0f%% of the traffic with the"
                + " job",
            Math.max(0, 100.0 * (finishedNanos - trafficStart) / (trafficEnd - trafficStart)));
      }
    }

    private void execute(
        KadaiEngine jobEngine, PlainJavaTransactionProvider txProvider, TaskUpdatePriorityJob job) {
      priorityProvider.startMetering(Duration.ofDays(1));
      try {
        jobEngine.runAsAdmin(() -> {
          if (job.getTransactionPolicy() == JobTransactionPolicy.WHOLE_JOB) {
            txProvider.executeInTransaction(() -> {
              job.execute();
              return null;
            });
          } else {
            job.execute();
          }
        });
        finishedNanos = System.nanoTime();
      } catch (RuntimeException e) {
        if (!priorityProvider.isTimeBudgetExhausted()) {
          failure = e;
        }
      }
    }

    private void printProgress() {
      long first = priorityProvider.firstCalculationNanos();
      if (first == 0) {
        return;
      }
      long calculations = priorityProvider.calculations();
      print(
          "  job: %,d priorities calculated, %,.0f tasks/s",
          calculations, calculations / seconds(Duration.ofNanos(System.nanoTime() - first)));
    }
  }
}
