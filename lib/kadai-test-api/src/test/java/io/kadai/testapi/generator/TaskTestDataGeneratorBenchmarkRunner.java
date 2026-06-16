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

package io.kadai.testapi.generator;

import io.kadai.KadaiConfiguration;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.KadaiEngine.ConnectionManagementMode;
import io.kadai.common.internal.configuration.DB;
import io.kadai.task.api.TaskService;
import io.kadai.testapi.DockerContainerCreator;
import io.kadai.testapi.extensions.TestContainerExtension;
import java.util.Locale;
import java.util.Optional;
import javax.sql.DataSource;
import org.testcontainers.containers.JdbcDatabaseContainer;

/** Standalone benchmark runner for {@link TaskTestDataGenerator#persist(long, int)}. */
public final class TaskTestDataGeneratorBenchmarkRunner {

  private static final long DEFAULT_TASK_COUNT = 1_000_000L;
  private static final int DEFAULT_BATCH_SIZE = 25_000;
  private static final long DEFAULT_WARMUP_TASK_COUNT = 10_000L;

  private TaskTestDataGeneratorBenchmarkRunner() {
    throw new IllegalStateException("Utility class");
  }

  static void main(String[] args) throws Exception {
    DB db = determineDatabase(args);
    long taskCount = Long.getLong("benchmark.tasks", DEFAULT_TASK_COUNT);
    int batchSize = Integer.getInteger("benchmark.batchSize", DEFAULT_BATCH_SIZE);
    long warmupTaskCount = Long.getLong("benchmark.warmupTasks", DEFAULT_WARMUP_TASK_COUNT);

    Optional<JdbcDatabaseContainer<?>> container = DockerContainerCreator.createDockerContainer(db);
    try {
      container.ifPresent(JdbcDatabaseContainer::start);
      DataSource dataSource =
          container
              .map(DockerContainerCreator::createDataSource)
              .orElseGet(TestContainerExtension::createDataSourceForH2);

      if (warmupTaskCount > 0) {
        KadaiEngine warmupEngine = createKadaiEngine(dataSource);
        TaskTestDataGenerator.from(warmupEngine).persist(warmupTaskCount, batchSize);
      }

      KadaiEngine kadaiEngine = createKadaiEngine(dataSource);
      TaskService taskService = kadaiEngine.getTaskService();
      TaskTestDataGenerator generator = TaskTestDataGenerator.from(kadaiEngine);

      long beforeCount = kadaiEngine.runAsAdmin(() -> taskService.createTaskQuery().count());
      long startNanos = System.nanoTime();
      GenerationSummary summary = generator.persist(taskCount, batchSize);
      long durationNanos = System.nanoTime() - startNanos;
      long afterCount = kadaiEngine.runAsAdmin(() -> taskService.createTaskQuery().count());

      double durationSeconds = durationNanos / 1_000_000_000.0d;
      double throughput = taskCount / durationSeconds;

      System.out.printf(
          Locale.ROOT,
          "BENCHMARK_RESULT db=%s tasks=%d warmupTasks=%d batchSize=%d durationSeconds=%.3f "
              + "throughputTasksPerSecond=%.2f beforeCount=%d afterCount=%d inserted=%d "
              + "summaryProcessed=%d summaryBatches=%d%n",
          db,
          taskCount,
          warmupTaskCount,
          batchSize,
          durationSeconds,
          throughput,
          beforeCount,
          afterCount,
          afterCount - beforeCount,
          summary.processedTaskCount(),
          summary.batchCount());
    } finally {
      container.ifPresent(JdbcDatabaseContainer::stop);
    }
  }

  private static DB determineDatabase(String[] args) {
    String configuredDb = System.getProperty("benchmark.db");
    if ((configuredDb == null || configuredDb.isBlank()) && args.length > 0) {
      configuredDb = args[0];
    }
    if (configuredDb == null || configuredDb.isBlank()) {
      configuredDb = System.getenv("DB");
    }
    if (configuredDb == null || configuredDb.isBlank()) {
      return DB.POSTGRES;
    }
    return DB.valueOf(configuredDb.trim().toUpperCase(Locale.ROOT));
  }

  private static KadaiEngine createKadaiEngine(DataSource dataSource) throws Exception {
    String schemaName = TestContainerExtension.determineSchemaName();
    KadaiConfiguration configuration =
        new KadaiConfiguration.Builder(dataSource, false, schemaName).initKadaiProperties().build();
    return KadaiEngine.buildKadaiEngine(configuration, ConnectionManagementMode.AUTOCOMMIT);
  }
}
