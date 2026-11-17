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

package io.kadai.gmt;

import io.kadai.common.api.KadaiEngine;
import io.kadai.testapi.generator.GenerationSummary;
import java.util.OptionalLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Example Application showing a minimal implementation of the kadai REST service. */
@EnableScheduling
@SpringBootApplication
@ComponentScan("io.kadai")
public class GmtApplication {

  private static final Logger LOGGER = LoggerFactory.getLogger(GmtApplication.class);
  private static final String GENERATE_TASKS_ARG = "--generate-tasks=";

  public static void main(String[] args) {
    OptionalLong taskCount = parseGenerateTasks(args);
    if (taskCount.isEmpty()) {
      SpringApplication.run(GmtApplication.class, args);
      return;
    }

    GenerationSummary summary;
    // The context needs a servlet environment: the security configuration requires HttpSecurity.
    // Ephemeral ports keep the child JVM away from the ports of the app that starts afterwards.
    try (ConfigurableApplicationContext ctx =
        SpringApplication.run(
            GmtApplication.class, "--server.port=0", "--management.server.port=0")) {
      KadaiEngine kadaiEngine = ctx.getBean(KadaiEngine.class);
      summary = TaskTestDataController.generate(kadaiEngine, taskCount.getAsLong());
    }
    LOGGER.info("Generated test data: {}", summary);
    // The embedded LDAP server and the job scheduler use non-daemon threads.
    System.exit(0);
  }

  /**
   * Reads the test-data generation mode from the command line.
   *
   * @param args the command line arguments
   * @return the requested number of tasks, or empty if the application should start normally
   */
  static OptionalLong parseGenerateTasks(String[] args) {
    if (args.length != 1 || !args[0].startsWith(GENERATE_TASKS_ARG)) {
      return OptionalLong.empty();
    }
    return OptionalLong.of(Long.parseLong(args[0].substring(GENERATE_TASKS_ARG.length())));
  }
}
