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
import io.kadai.testapi.generator.TaskTestDataGenerator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Creates the data set used by Green Metrics Tool measurements. */
@RestController
public class TaskTestDataController {

  private static final int BATCH_SIZE = 1_000;

  private final KadaiEngine kadaiEngine;

  public TaskTestDataController(KadaiEngine kadaiEngine) {
    this.kadaiEngine = kadaiEngine;
  }

  /**
   * Creates compatible classifications and workbaskets as needed, then persists the requested
   * tasks. The response is returned only after the database transaction has committed.
   *
   * @param request the requested number of tasks
   * @return counts of the persisted data and supporting fixtures
   */
  @PostMapping(
      path = "/api/v1/gmt/tasks",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public GenerationSummary createTasks(@Valid @RequestBody TaskGenerationRequest request) {
    return TaskTestDataGenerator.from(kadaiEngine).persist(request.taskCount(), BATCH_SIZE);
  }

  /**
   * Request body for synchronous task test-data generation.
   *
   * @param taskCount number of tasks to create
   */
  public record TaskGenerationRequest(@NotNull @Min(0) Long taskCount) {}
}
