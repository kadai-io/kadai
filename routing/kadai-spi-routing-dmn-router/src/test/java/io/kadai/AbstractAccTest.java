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

package io.kadai;

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.KadaiEngine.ConnectionManagementMode;
import io.kadai.common.internal.configuration.DbSchemaCreator;
import io.kadai.common.test.config.DataSourceGenerator;
import io.kadai.sampledata.SampleDataGenerator;
import io.kadai.task.api.models.ObjectReference;
import io.kadai.task.internal.models.ObjectReferenceImpl;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;

public abstract class AbstractAccTest {

  protected static KadaiConfiguration kadaiConfiguration;
  protected static KadaiEngine kadaiEngine;

  @BeforeAll
  protected static void setupTest() throws Exception {
    resetDb(false);
  }

  protected static void resetDb(boolean dropTables) throws Exception {

    DataSource dataSource = DataSourceGenerator.getDataSource();
    String schemaName = DataSourceGenerator.getSchemaName();
    SampleDataGenerator sampleDataGenerator = new SampleDataGenerator(dataSource, schemaName);
    if (dropTables) {
      sampleDataGenerator.dropDb();
    }
    dataSource = DataSourceGenerator.getDataSource();
    kadaiConfiguration =
        new KadaiConfiguration.Builder(dataSource, false, schemaName)
            .initKadaiProperties()
            .germanPublicHolidaysEnabled(true)
            .build();
    DbSchemaCreator dbSchemaCreator =
        new DbSchemaCreator(dataSource, kadaiConfiguration.getSchemaName());
    dbSchemaCreator.run();
    sampleDataGenerator.clearDb();
    sampleDataGenerator.generateTestData();
    kadaiEngine =
        KadaiEngine.buildKadaiEngine(kadaiConfiguration, ConnectionManagementMode.AUTOCOMMIT);
  }

  protected ObjectReference createObjectReference(
      String company, String system, String systemInstance, String type, String value) {
    ObjectReferenceImpl objectReference = new ObjectReferenceImpl();
    objectReference.setCompany(company);
    objectReference.setSystem(system);
    objectReference.setSystemInstance(systemInstance);
    objectReference.setType(type);
    objectReference.setValue(value);
    return objectReference;
  }
}
