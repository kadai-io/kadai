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

package configuration;

import static org.assertj.core.api.Assertions.assertThat;

import acceptance.AbstractAccTest;
import io.kadai.KadaiConfiguration;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.test.config.DataSourceGenerator;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

class KadaiConfigurationTest extends AbstractAccTest {

  @Test
  void testCreateKadaiEngine() throws Exception {
    DataSource ds = DataSourceGenerator.getDataSource();
    KadaiConfiguration configuration =
        new KadaiConfiguration.Builder(ds, false, DataSourceGenerator.getSchemaName(), false)
            .initKadaiProperties()
            .build();

    KadaiEngine te = KadaiEngine.buildKadaiEngine(configuration);

    assertThat(te).isNotNull();
  }

  @Test
  void testCreateKadaiHistoryEventWithNonDefaultSchemaName() throws Exception {
    resetDb("SOMECUSTOMSCHEMANAME");
    long count = taskHistoryService.createTaskHistoryQuery().workbasketKeyIn("wbKey1").count();
    assertThat(count).isZero();
    taskHistoryService.createTaskHistoryEvent(
        AbstractAccTest.createTaskHistoryEvent(
            "wbKey1", "taskId1", "type1", "Some comment", "wbKey2", "someUserId"));
    count = taskHistoryService.createTaskHistoryQuery().workbasketKeyIn("wbKey1").count();
    assertThat(count).isOne();
  }
}
