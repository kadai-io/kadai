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

package io.kadai.user.rest;

import io.kadai.spi.user.api.RefreshUserPostprocessor;
import io.kadai.user.api.models.User;

public class SecondRefreshUserPostprocessor implements RefreshUserPostprocessor {
  @Override
  public User processUserAfterRefresh(User userToProcess) {
    if (userToProcess.getId().equals("user-2-2")) {
      userToProcess.setOrgLevel1(userToProcess.getOrgLevel1() + "Second");
    }
    return userToProcess;
  }
}
