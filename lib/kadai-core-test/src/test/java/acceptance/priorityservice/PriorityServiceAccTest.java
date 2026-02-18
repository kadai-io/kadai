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

import static io.kadai.testapi.DefaultTestEntities.defaultTestClassification;
import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.models.Classification;
import io.kadai.common.api.ScheduledJob;
import io.kadai.common.internal.InternalKadaiEngine;
import io.kadai.common.internal.JobMapper;
import io.kadai.spi.priority.api.PriorityServiceProvider;
import io.kadai.testapi.KadaiInject;
import io.kadai.testapi.KadaiIntegrationTest;
import io.kadai.testapi.WithServiceProvider;
import io.kadai.testapi.security.WithAccessId;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Acceptance test for all priority computation scenarios. */
@KadaiIntegrationTest
@WithServiceProvider(
    serviceProviderInterface = PriorityServiceProvider.class,
    serviceProviders = TestPriorityServiceProvider.class)
class PriorityServiceAccTest {

  @KadaiInject InternalKadaiEngine internalKadaiEngine;
  @KadaiInject ClassificationService classificationService;
  Classification classification;

  @BeforeEach
  @WithAccessId(user = "businessadmin")
  void setup() throws Exception {
    classification = defaultTestClassification().buildAndStore(classificationService);
  }

  @Test
  @WithAccessId(user = "businessadmin")
  void should_NotCreateClassificationChangedJob_When_PriorityProviderExisting() throws Exception {
    classification.setPriority(10);
    classificationService.updateClassification(classification);

    List<ScheduledJob> jobsToRun =
        internalKadaiEngine.getSqlSession().getMapper(JobMapper.class).findJobsToRun(Instant.now());
    assertThat(jobsToRun).isEmpty();

    classification.setServiceLevel("P4D");
    classificationService.updateClassification(classification);
    jobsToRun =
        internalKadaiEngine.getSqlSession().getMapper(JobMapper.class).findJobsToRun(Instant.now());
    assertThat(jobsToRun).isEmpty();
  }
}
