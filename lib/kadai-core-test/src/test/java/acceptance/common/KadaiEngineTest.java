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
import io.kadai.common.internal.configuration.DbSchemaCreator;
import io.kadai.testapi.extensions.TestContainerExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class KadaiEngineTest {

  @ParameterizedTest
  @EnumSource(ConnectionManagementMode.class)
  void should_CreateKadaiEngine_When_GivenConnectionManagementModeIsActive(
      ConnectionManagementMode connectionManagementMode) throws Exception {

    String schemaName = TestContainerExtension.determineSchemaName();

    KadaiConfiguration kadaiConfiguration =
        new KadaiConfiguration.Builder(TestContainerExtension.DATA_SOURCE, false, schemaName, true)
            .initKadaiProperties()
            .build();

    KadaiEngine.buildKadaiEngine(kadaiConfiguration, connectionManagementMode);

    DbSchemaCreator dsc =
        new DbSchemaCreator(kadaiConfiguration.getDataSource(), kadaiConfiguration.getSchemaName());
    assertThat(dsc.isValidSchemaVersion(KadaiEngine.MINIMAL_KADAI_SCHEMA_VERSION)).isTrue();
  }
}
