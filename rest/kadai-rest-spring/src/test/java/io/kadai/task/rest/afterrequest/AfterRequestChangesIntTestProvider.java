package io.kadai.task.rest.afterrequest;

import io.kadai.common.api.KadaiEngine;
import io.kadai.spi.task.api.AfterRequestChangesProvider;
import io.kadai.task.api.models.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AfterRequestChangesIntTestProvider implements AfterRequestChangesProvider {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(AfterRequestChangesIntTestProvider.class);
  private KadaiEngine kadaiEngine;

  @Override
  public void initialize(KadaiEngine kadaiEngine) {
    this.kadaiEngine = kadaiEngine;
  }

  @Override
  public Task afterRequestChanges(Task task) throws Exception {
    return task;
  }

  @Override
  public Task afterRequestChanges(Task task, String workbasketId, String ownerId) throws Exception {
    Task newTask = null;

    try {
      newTask = kadaiEngine.getTaskService().transferWithOwner(task.getId(), workbasketId, ownerId);
    } catch (Exception e) {
      LOGGER.error("Error getting task: {}", e.getMessage());
    }
    return newTask;
  }
}
