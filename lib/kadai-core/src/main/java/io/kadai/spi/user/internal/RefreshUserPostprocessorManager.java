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

package io.kadai.spi.user.internal;

import io.kadai.common.internal.util.CheckedConsumer;
import io.kadai.common.internal.util.SpiLoader;
import io.kadai.spi.user.api.RefreshUserPostprocessor;
import io.kadai.user.api.models.User;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefreshUserPostprocessorManager {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(RefreshUserPostprocessorManager.class);
  private final List<RefreshUserPostprocessor> refreshUserPostprocessors;

  public RefreshUserPostprocessorManager() {
    refreshUserPostprocessors = SpiLoader.load(RefreshUserPostprocessor.class);
    for (RefreshUserPostprocessor postprocessor : refreshUserPostprocessors) {
      LOGGER.info(
          "Registered RefreshUserPostprocessor provider: {}", postprocessor.getClass().getName());
    }
    if (refreshUserPostprocessors.isEmpty()) {
      LOGGER.info("No RefreshUserPostprocessor found. Running without RefreshUserPostprocessor.");
    }
  }

  public User processUserAfterRefresh(User userToProcess) {
    LOGGER.debug("Sending user to RefreshUserPostprocessor providers: {}", userToProcess);

    refreshUserPostprocessors.forEach(
        CheckedConsumer.wrapping(
            refreshUserPostprocessor ->
                refreshUserPostprocessor.processUserAfterRefresh(userToProcess)));
    return userToProcess;
  }

  public boolean isEnabled() {
    return !refreshUserPostprocessors.isEmpty();
  }
}
