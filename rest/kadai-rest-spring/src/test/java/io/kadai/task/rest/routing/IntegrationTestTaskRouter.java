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

package io.kadai.task.rest.routing;

import io.kadai.common.api.KadaiEngine;
import io.kadai.spi.routing.api.TaskRoutingProvider;
import io.kadai.task.api.models.Task;

public class IntegrationTestTaskRouter implements TaskRoutingProvider {

  public static final String DEFAULT_ROUTING_TARGET = "WBI:100000000000000000000000000000000002";


  @Override
  public void initialize(KadaiEngine kadaiEngine) {
    // NOOP
  }

  @Override
  public String determineWorkbasketId(Task task) {
    return DEFAULT_ROUTING_TARGET;
  }
}
