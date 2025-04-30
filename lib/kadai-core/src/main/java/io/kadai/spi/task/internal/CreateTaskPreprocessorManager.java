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

package io.kadai.spi.task.internal;

import io.kadai.common.internal.util.CheckedConsumer;
import io.kadai.common.internal.util.SpiLoader;
import io.kadai.spi.task.api.CreateTaskPreprocessor;
import io.kadai.task.api.models.Task;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateTaskPreprocessorManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateTaskPreprocessorManager.class);
  private final List<CreateTaskPreprocessor> createTaskPreprocessors;

  public CreateTaskPreprocessorManager() {
    createTaskPreprocessors = SpiLoader.load(CreateTaskPreprocessor.class);
    for (CreateTaskPreprocessor preprocessor : createTaskPreprocessors) {
      LOGGER.info(
          "Registered CreateTaskPreprocessor provider: {}", preprocessor.getClass().getName());
    }
    if (createTaskPreprocessors.isEmpty()) {
      LOGGER.info("No CreateTaskPreprocessor found. Running without CreateTaskPreprocessor.");
    }
  }

  public Task processTaskBeforeCreation(Task taskToProcess) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Sending task to CreateTaskPreprocessor providers: {}", taskToProcess);
    }
    createTaskPreprocessors.forEach(
        CheckedConsumer.wrapping(
            createTaskPreprocessor ->
                createTaskPreprocessor.processTaskBeforeCreation(taskToProcess)));
    return taskToProcess;
  }

  public boolean isEnabled() {
    return !createTaskPreprocessors.isEmpty();
  }
}
