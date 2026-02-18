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

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.internal.util.SpiLoader;
import io.kadai.spi.task.api.AfterRequestChangesProvider;
import io.kadai.task.api.models.Task;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AfterRequestChangesManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(AfterRequestChangesManager.class);

  private final List<AfterRequestChangesProvider> afterRequestChangesProviders;

  public AfterRequestChangesManager(KadaiEngine kadaiEngine) {
    afterRequestChangesProviders = SpiLoader.load(AfterRequestChangesProvider.class);
    for (AfterRequestChangesProvider serviceProvider : afterRequestChangesProviders) {
      serviceProvider.initialize(kadaiEngine);
      LOGGER.info(
          "Registered AfterRequestChangesProvider service provider: {}",
          serviceProvider.getClass().getName());
    }
    if (afterRequestChangesProviders.isEmpty()) {
      LOGGER.info(
          "No AfterRequestChangesProvider service provider found. "
              + "Running without any AfterRequestChangesProvider implementation.");
    }
  }

  public Task afterRequestChanges(Task task, String workbasketId, String ownerId) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Sending Task to AfterRequestChangesProvider service providers: {}", task);
    }
    for (AfterRequestChangesProvider serviceProvider : afterRequestChangesProviders) {
      try {
        if (workbasketId == null) {
          task = serviceProvider.afterRequestChanges(task);
        } else {
          task = serviceProvider.afterRequestChanges(task, workbasketId, ownerId);
        }
      } catch (Exception e) {
        throw new SystemException(
            String.format(
                "service provider '%s' threw an exception", serviceProvider.getClass().getName()),
            e);
      }
    }
    return task;
  }
}
