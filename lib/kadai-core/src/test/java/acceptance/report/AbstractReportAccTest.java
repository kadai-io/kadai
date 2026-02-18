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

package acceptance.report;

import io.kadai.KadaiConfiguration;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.test.config.DataSourceGenerator;
import io.kadai.sampledata.SampleDataGenerator;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;

/** Abstract test class for all report building tests. */
public abstract class AbstractReportAccTest {

  protected static KadaiConfiguration kadaiConfiguration;
  protected static KadaiEngine kadaiEngine;

  protected static void resetDb() throws Exception {
    DataSource dataSource = DataSourceGenerator.getDataSource();
    String schemaName = DataSourceGenerator.getSchemaName();
    kadaiConfiguration =
        new KadaiConfiguration.Builder(dataSource, false, schemaName)
            .initKadaiProperties()
            .germanPublicHolidaysEnabled(false)
            .build();
    kadaiEngine = KadaiEngine.buildKadaiEngine(kadaiConfiguration);
    kadaiEngine.setConnectionManagementMode(KadaiEngine.ConnectionManagementMode.AUTOCOMMIT);
    SampleDataGenerator sampleDataGenerator = new SampleDataGenerator(dataSource, schemaName);
    sampleDataGenerator.clearDb();
    sampleDataGenerator.generateMonitorData();
  }

  @BeforeAll
  static void setupTest() throws Exception {
    resetDb();
  }
}
