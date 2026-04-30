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

package io.kadai.spi.task.internal;

import io.kadai.common.internal.util.CheckedConsumer;
import io.kadai.common.internal.util.SpiLoader;
import io.kadai.spi.task.api.TaskEndstatePreprocessor;
import io.kadai.task.api.models.Task;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskEndstatePreprocessorManager {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(TaskEndstatePreprocessorManager.class);
  private final List<TaskEndstatePreprocessor> taskEndstatePreprocessors;

  public TaskEndstatePreprocessorManager() {
    taskEndstatePreprocessors = SpiLoader.load(TaskEndstatePreprocessor.class);
    for (TaskEndstatePreprocessor preprocessor : taskEndstatePreprocessors) {
      LOGGER.info(
          "Registered TaskEndstatePreprocessor provider: {}", preprocessor.getClass().getName());
    }
    if (taskEndstatePreprocessors.isEmpty()) {
      LOGGER.info("No TaskEndstatePreprocessor found. Running without TaskEndstatePreprocessor.");
    }
  }

  public Task processTaskBeforeEndstate(Task taskToProcess) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Sending task to TaskEndstatePreprocessor providers: {}", taskToProcess);
    }
    taskEndstatePreprocessors.forEach(
        CheckedConsumer.wrapping(
            taskEndstatePreprocessor ->
                taskEndstatePreprocessor.processTaskBeforeEndstate(taskToProcess)));
    return taskToProcess;
  }
}
