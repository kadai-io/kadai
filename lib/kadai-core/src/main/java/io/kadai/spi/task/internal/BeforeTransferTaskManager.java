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
import io.kadai.spi.task.api.BeforeTransferTaskProvider;
import io.kadai.task.api.exceptions.TransferCheckException;
import io.kadai.task.api.models.Task;
import io.kadai.workbasket.api.models.Workbasket;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeforeTransferTaskManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(BeforeTransferTaskManager.class);

  private final List<BeforeTransferTaskProvider> beforeTransferTaskProviders;

  public BeforeTransferTaskManager(KadaiEngine kadaiEngine) {
    beforeTransferTaskProviders = SpiLoader.load(BeforeTransferTaskProvider.class);
    for (BeforeTransferTaskProvider serviceProvider : beforeTransferTaskProviders) {
      serviceProvider.initialize(kadaiEngine);
      LOGGER.info(
          "Registered BeforeTransferTaskProvider service provider: {}",
          serviceProvider.getClass().getName());
    }
    if (beforeTransferTaskProviders.isEmpty()) {
      LOGGER.info(
          "No BeforeTransferTaskProvider service provider found. "
              + "Running without any BeforeTransferTaskProvider implementation.");
    }
  }

  public boolean isEnabled() {
    return !beforeTransferTaskProviders.isEmpty();
  }

  public void checkTransferAllowed(Task task, Workbasket destinationWorkbasket)
      throws TransferCheckException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "Sending Task to BeforeTransferTaskProvider service providers: {}", task);
    }
    for (BeforeTransferTaskProvider serviceProvider : beforeTransferTaskProviders) {
      try {
        serviceProvider.checkTransferAllowed(task, destinationWorkbasket);
      } catch (TransferCheckException e) {
        throw e;
      } catch (Exception e) {
        throw new SystemException(
            String.format(
                "service provider '%s' threw an exception", serviceProvider.getClass().getName()),
            e);
      }
    }
  }
}

