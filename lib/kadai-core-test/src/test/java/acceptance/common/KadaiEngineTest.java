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

package acceptance.common;

import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.KadaiConfiguration;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.KadaiEngine.ConnectionManagementMode;
import io.kadai.common.internal.configuration.DbSchemaCreator;
import io.kadai.testapi.extensions.TestContainerExtension;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class KadaiEngineTest {

  private KadaiEngine kadaiEngine;

  @BeforeEach
  void setupKadaiEngine() throws Exception {
    String schemaName = TestContainerExtension.determineSchemaName();
    DataSource dataSource = TestContainerExtension.DATA_SOURCE;
    KadaiConfiguration kadaiConfiguration =
        new KadaiConfiguration.Builder(dataSource, false, schemaName, false)
            .initKadaiProperties()
            .build();
    this.kadaiEngine = KadaiEngine.buildKadaiEngine(kadaiConfiguration);
  }

  @Test
  void should_DefaultToParticipateMode_When_BuildingKadaiEngineJustByConfiguration() {
    assertThat(kadaiEngine.getConnectionManagementMode())
        .isEqualTo(ConnectionManagementMode.PARTICIPATE);
  }

  @ParameterizedTest
  @EnumSource(ConnectionManagementMode.class)
  void should_CreateKadaiEngine_For_ConnectionManagementMode(ConnectionManagementMode mode) {
    kadaiEngine.setConnectionManagementMode(mode);

    DbSchemaCreator dsc =
        new DbSchemaCreator(
            kadaiEngine.getConfiguration().getDataSource(),
            kadaiEngine.getConfiguration().getSchemaName());

    assertThat(dsc.isValidSchemaVersion(KadaiEngine.MINIMAL_KADAI_SCHEMA_VERSION)).isTrue();
  }

  @ParameterizedTest
  @EnumSource(ConnectionManagementMode.class)
  void should_SetModeExplicit_When_SetConnection(ConnectionManagementMode mode) throws Exception {
    kadaiEngine.setConnectionManagementMode(mode);
    kadaiEngine.setConnection(kadaiEngine.getConfiguration().getDataSource().getConnection());

    assertThat(kadaiEngine.getConnectionManagementMode())
        .isEqualTo(ConnectionManagementMode.EXPLICIT);
  }

  @Test
  void should_SetModeParticipate_When_CloseConnectionForModeExplicit() {
    kadaiEngine.setConnectionManagementMode(ConnectionManagementMode.EXPLICIT);
    kadaiEngine.closeConnection();

    assertThat(kadaiEngine.getConnectionManagementMode())
        .isEqualTo(ConnectionManagementMode.PARTICIPATE);
  }
}
