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

package acceptance.priorityservice;

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.WorkingTimeCalculator;
import io.kadai.spi.priority.api.PriorityServiceProvider;
import io.kadai.task.api.TaskCustomField;
import io.kadai.task.api.models.TaskSummary;
import java.time.Duration;
import java.time.Instant;
import java.util.OptionalInt;

public class TestPriorityServiceProvider implements PriorityServiceProvider {

  private static final int MULTIPLIER = 10;

  private WorkingTimeCalculator calculator;

  @Override
  public void initialize(KadaiEngine kadaiEngine) {
    calculator = kadaiEngine.getWorkingTimeCalculator();
  }

  @Override
  public OptionalInt calculatePriority(TaskSummary taskSummary) {

    long priority;
    try {
      priority =
          calculator.workingTimeBetween(taskSummary.getCreated(), Instant.now()).toMinutes() + 1;
    } catch (Exception e) {
      priority = Duration.between(taskSummary.getCreated(), Instant.now()).toMinutes();
    }

    if (Boolean.parseBoolean(taskSummary.getCustomField(TaskCustomField.CUSTOM_6))) {
      priority *= MULTIPLIER;
    }

    return OptionalInt.of(Math.toIntExact(priority));
  }
}
