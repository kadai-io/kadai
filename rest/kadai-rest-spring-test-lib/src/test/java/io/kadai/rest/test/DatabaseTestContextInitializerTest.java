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
 */

package io.kadai.rest.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.kadai.common.internal.configuration.DB;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;

class DatabaseTestContextInitializerTest {

  @Test
  void shouldLeaveH2PropertiesUntouched() {
    try (GenericApplicationContext context = new GenericApplicationContext()) {
      DatabaseTestContextInitializer.configure(context, DB.H2, null);

      assertNull(context.getEnvironment().getPropertySources().get("kadaiTestDatabase"));
    }
  }

  @Test
  void shouldConfigurePostgres() {
    assertDatabaseConfiguration(DB.POSTGRES, "kadai");
  }

  @Test
  void shouldConfigureDb2() {
    assertDatabaseConfiguration(DB.DB2, "KADAI");
  }

  private void assertDatabaseConfiguration(DB database, String expectedSchema) {
    PooledDataSource dataSource =
        new PooledDataSource("org.h2.Driver", "jdbc:h2:mem:test", "test-user", "test-password");
    try (GenericApplicationContext context = new GenericApplicationContext()) {
      DatabaseTestContextInitializer.configure(context, database, dataSource);

      var environment = context.getEnvironment();
      assertEquals("jdbc:h2:mem:test", environment.getProperty("spring.datasource.url"));
      assertEquals("org.h2.Driver", environment.getProperty("spring.datasource.driverClassName"));
      assertEquals("test-user", environment.getProperty("spring.datasource.username"));
      assertEquals("test-password", environment.getProperty("spring.datasource.password"));
      assertEquals(expectedSchema, environment.getProperty("kadai.schemaName"));
    }
  }
}
