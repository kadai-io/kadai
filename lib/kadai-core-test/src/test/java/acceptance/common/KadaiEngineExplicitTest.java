/*
 * Copyright [2024] [envite consulting GmbH]
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

package acceptance.common;

import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.KadaiConfiguration;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.KadaiEngine.ConnectionManagementMode;
import io.kadai.common.internal.configuration.DB;
import io.kadai.common.internal.configuration.DbSchemaCreator;
import io.kadai.testapi.OracleSchemaHelper;
import io.kadai.testapi.extensions.TestContainerExtension;
import org.junit.jupiter.api.Test;

class KadaiEngineExplicitTest {

  @Test
  void should_CreateKadaiEngine_When_ExplizitModeIsActive() throws Exception {

    String schemaName = TestContainerExtension.determineSchemaName();
    if (DB.ORACLE == TestContainerExtension.EXECUTION_DATABASE) {
      OracleSchemaHelper.initOracleSchema(TestContainerExtension.DATA_SOURCE, schemaName);
    }

    KadaiConfiguration kadaiConfiguration =
        new KadaiConfiguration.Builder(TestContainerExtension.DATA_SOURCE, false, schemaName, true)
            .initKadaiProperties()
            .build();

    KadaiEngine.buildKadaiEngine(kadaiConfiguration, ConnectionManagementMode.EXPLICIT);

    DbSchemaCreator dsc =
        new DbSchemaCreator(kadaiConfiguration.getDataSource(), kadaiConfiguration.getSchemaName());
    assertThat(dsc.isValidSchemaVersion(KadaiEngine.MINIMAL_KADAI_SCHEMA_VERSION)).isTrue();
  }
}
