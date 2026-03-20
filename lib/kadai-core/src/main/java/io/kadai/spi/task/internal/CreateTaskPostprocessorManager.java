package io.kadai.spi.task.internal;

import io.kadai.common.internal.util.LogSanitizer;
import io.kadai.common.internal.util.SpiLoader;
import io.kadai.spi.task.api.CreateTaskPostprocessor;
import io.kadai.task.api.models.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CreateTaskPostprocessorManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateTaskPostprocessorManager.class);
  private final List<CreateTaskPostprocessor> createTaskPostProcessors;

  public CreateTaskPostprocessorManager() {
    createTaskPostProcessors = SpiLoader.load(CreateTaskPostprocessor.class);
    for (CreateTaskPostprocessor postProcessor : createTaskPostProcessors) {
      LOGGER.info(
              "Registered CreateTaskPostProcessor provider: {}",
              postProcessor.getClass().getName());
    }
    if (createTaskPostProcessors.isEmpty()) {
      LOGGER.info("No CreateTaskPostProcessor found. Running without CreateTaskPostProcessor.");
    }
  }

  public Task processTaskAfterCreation(Task taskToProcess) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
              "Sending task to CreateTaskPreprocessor providers: {}",
              LogSanitizer.stripLineBreakingChars(taskToProcess));
    }
    Task result = taskToProcess;
    for (CreateTaskPostprocessor postProcessor : createTaskPostProcessors) {
      result = postProcessor.processTaskAfterCreation(result);
    }
    return result;
  }

  public boolean isEnabled() {
    return !createTaskPostProcessors.isEmpty();
  }
}
