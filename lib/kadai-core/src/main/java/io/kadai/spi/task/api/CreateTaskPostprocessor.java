package io.kadai.spi.task.api;

import io.kadai.common.api.security.GroupPrincipal;
import io.kadai.common.api.security.UserPrincipal;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.models.Task;

/**
 * The CreateTaskPostProcessor allows to implement customized behavior after the given {@linkplain
 * Task} has been created.
 */
public interface CreateTaskPostprocessor {
  /**
   * Perform any action after a {@linkplain Task} has been created through {@linkplain
   * TaskService#createTask(Task)}.
   *
   * <p>This SPI is executed within the same transaction staple as {@linkplain
   * TaskService#createTask(Task)}.
   *
   * <p>This SPI is executed with the same {@linkplain UserPrincipal}
   * and {@linkplain GroupPrincipal} as in {@linkplain
   * TaskService#createTask(Task)}.
   *
   * @param createdTask the {@linkplain Task} to postprocess
   * @return the postprocessed {@linkplain Task} - must not be null
   */
  Task processTaskAfterCreation(Task createdTask);
}
