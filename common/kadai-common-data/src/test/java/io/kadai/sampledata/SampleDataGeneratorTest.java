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

package io.kadai.sampledata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.kadai.common.internal.configuration.DbSchemaCreator;
import io.kadai.common.internal.util.ComparableVersion;
import java.sql.Connection;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.jdbc.SqlRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Test SampleDataGenerator. */
class SampleDataGeneratorTest {

  private PooledDataSource pooledDataSource;

  private static final String JDBC_URL =
      "jdbc:h2:mem:kadai;NON_KEYWORDS=KEY,VALUE;"
          + "IGNORECASE=TRUE;LOCK_MODE=0;INIT=CREATE SCHEMA IF NOT EXISTS KADAI";

  @BeforeEach
  void setUp() {
    this.pooledDataSource = new PooledDataSource("org.h2.Driver", JDBC_URL, "sa", "sa");
  }

  @AfterEach
  void tearDown() {
    pooledDataSource.forceCloseAll();
  }

  @Test
  void should_generateSchemaAndSchemaData() {
    assertThatCode(() -> new DbSchemaCreator(pooledDataSource, "KADAI").run())
        .doesNotThrowAnyException();
    assertThatCode(() -> new SampleDataGenerator(pooledDataSource, "KADAI").generateSampleData())
        .doesNotThrowAnyException();
  }

  @Test
  void should_ReturnMaximalSchemaVersion() throws Exception {
    final DbSchemaCreator schemaCreator = new DbSchemaCreator(pooledDataSource, "KADAI");
    schemaCreator.run();

    try (Connection connection = schemaCreator.getDataSource().getConnection()) {
      connection.setSchema(schemaCreator.getSchemaName());

      final SqlRunner runner = new SqlRunner(connection);
      runner.run(
          "INSERT INTO KADAI_SCHEMA_VERSION (ID, VERSION, CREATED) "
              + "VALUES (nextval('KADAI_SCHEMA_VERSION_ID_SEQ'), '9.0.0', CURRENT_TIMESTAMP)");
      runner.run(
          "INSERT INTO KADAI_SCHEMA_VERSION (ID, VERSION, CREATED) "
              + "VALUES (nextval('KADAI_SCHEMA_VERSION_ID_SEQ'), '42.0.0', CURRENT_TIMESTAMP)");

      final ComparableVersion actual = schemaCreator.getActualMaxVersion(runner);

      assertThat(actual).isEqualTo(ComparableVersion.of("42.0.0"));
    }
  }
}
