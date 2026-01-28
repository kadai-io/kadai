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

package acceptance.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.kadai.KadaiConfiguration;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.internal.configuration.DbSchemaCreator;
import io.kadai.common.test.config.DataSourceGenerator;
import io.kadai.sampledata.SampleDataGenerator;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KadaiSecurityConfigAccTest {

  @BeforeEach
  void cleanDb() throws Exception {
    DataSource dataSource = DataSourceGenerator.getDataSource();
    String schemaName = DataSourceGenerator.getSchemaName();

    DbSchemaCreator dbSchemaCreator = new DbSchemaCreator(dataSource, schemaName);
    dbSchemaCreator.run();
    SampleDataGenerator sampleDataGenerator = new SampleDataGenerator(dataSource, schemaName);
    sampleDataGenerator.clearDb();
  }

  @Test
  void should_ThrowException_When_CreatingUnsecuredEngineCfgWhileSecurityIsEnforced()
      throws Exception {

    setSecurityFlag(true);

    assertThatThrownBy(() -> createKadaiEngine(false))
        .isInstanceOf(SystemException.class)
        .hasMessageContaining("Secured KADAI mode is enforced, can't start in unsecured mode");
  }

  @Test
  void should_StartUpNormally_When_CreatingUnsecuredEngineCfgWhileSecurityIsNotEnforced()
      throws Exception {

    setSecurityFlag(false);

    assertThatCode(() -> createKadaiEngine(false)).doesNotThrowAnyException();
  }

  @Test
  void should_SetSecurityFlagToFalse_When_CreatingUnsecureEngineCfgAndSecurityFlagIsNotSet()
      throws Exception {

    assertThat(retrieveSecurityFlag()).isNull();

    assertThatCode(() -> createKadaiEngine(false)).doesNotThrowAnyException();

    assertThat(retrieveSecurityFlag()).isFalse();
  }

  @Test
  void should_SetSecurityFlagToTrue_When_CreatingSecureEngineCfgAndSecurityFlagIsNotSet()
      throws Exception {

    assertThat(retrieveSecurityFlag()).isNull();

    assertThatCode(() -> createKadaiEngine(true)).doesNotThrowAnyException();

    assertThat(retrieveSecurityFlag()).isTrue();
  }

  private void createKadaiEngine(boolean securityEnabled) throws SQLException {
    KadaiEngine.buildKadaiEngine(
        new KadaiConfiguration.Builder(
                DataSourceGenerator.getDataSource(),
                false,
                DataSourceGenerator.getSchemaName(),
                securityEnabled)
            .initKadaiProperties()
            .build());
  }

  private Boolean retrieveSecurityFlag() throws Exception {

    try (Connection connection = DataSourceGenerator.getDataSource().getConnection()) {

      String selectSecurityFlagSql =
          String.format(
              "SELECT ENFORCE_SECURITY FROM %s.CONFIGURATION WHERE NAME = 'MASTER'",
              DataSourceGenerator.getSchemaName());

      Statement statement = connection.createStatement();
      ResultSet resultSet = statement.executeQuery(selectSecurityFlagSql);

      if (resultSet.next()) {
        Boolean securityEnabled = resultSet.getBoolean(1);
        if (resultSet.wasNull()) {
          return null;
        } else {
          return securityEnabled;
        }
      }
      statement.close();
      return null;
    }
  }

  private void setSecurityFlag(boolean securityFlag) throws Exception {

    try (Connection connection = DataSourceGenerator.getDataSource().getConnection()) {

      String sql;
      final String securityFlagAsString = String.valueOf(securityFlag);

      sql =
          String.format(
              "UPDATE %s.CONFIGURATION SET ENFORCE_SECURITY = %s WHERE NAME = 'MASTER'",
              DataSourceGenerator.getSchemaName(), securityFlagAsString);

      Statement statement = connection.createStatement();
      statement.execute(sql);
      if (!connection.getAutoCommit()) {
        connection.commit();
      }
      statement.close();
    }
  }
}
