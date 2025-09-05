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

package acceptance.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import acceptance.common.TestUserMapper.TestUser;
import io.kadai.KadaiConfiguration;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.KadaiEngine.ConnectionManagementMode;
import io.kadai.common.internal.SpringKadaiEngine;
import io.kadai.testapi.KadaiEngineProxy;
import io.kadai.testapi.extensions.TestContainerExtension;
import java.sql.Connection;
import javax.sql.DataSource;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public class KadaiEngineModesTest {

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  public class KadaiEngineParticipateModeTest {

    private KadaiEngineProxy thisKadaiEngineProxy;
    private KadaiEngineProxy thatKadaiEngineProxy;
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    public void setupKadaiEngines() throws Exception {
      String schemaName = TestContainerExtension.determineSchemaName();
      DataSource dataSource = TestContainerExtension.DATA_SOURCE;
      KadaiConfiguration kadaiConfiguration =
          new KadaiConfiguration.Builder(dataSource, true, schemaName, false)
              .initKadaiProperties()
              .build();
      thisKadaiEngineProxy =
          new KadaiEngineProxy(
              SpringKadaiEngine.buildKadaiEngine(
                  kadaiConfiguration, ConnectionManagementMode.PARTICIPATE));
      thisKadaiEngineProxy.getSqlSession().getConfiguration().addMapper(TestUserMapper.class);
      transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(dataSource));

      thatKadaiEngineProxy = new KadaiEngineProxy(KadaiEngine.buildKadaiEngine(kadaiConfiguration));
      thatKadaiEngineProxy.getSqlSession().getConfiguration().addMapper(TestUserMapper.class);
    }

    @Test
    void should_RetrieveCreated_When_ParticipatingTransactionSucceeded() {
      TestUserMapper thisMapper =
          thisKadaiEngineProxy.getSqlSession().getMapper(TestUserMapper.class);
      TestUserMapper thatMapper =
          thatKadaiEngineProxy.getSqlSession().getMapper(TestUserMapper.class);
      TestUser expected =
          new TestUser("teamlead-42", "Max", "Mustermann", "Long name of teamlead-42");

      transactionTemplate.execute(
          status -> {
            thisKadaiEngineProxy
                .getEngine()
                .executeInDatabaseConnection(() -> thisMapper.insert(expected));
            return new Object();
          });

      TestUser actual =
          thatKadaiEngineProxy
              .getEngine()
              .executeInDatabaseConnection(() -> thatMapper.findById(expected.getId()));

      assertThat(actual).isEqualTo(expected);
    }

    @Test
    void should_NotRetrieveCreated_When_ParticipatingTransactionFailed() {
      TestUserMapper thisMapper =
          thisKadaiEngineProxy.getSqlSession().getMapper(TestUserMapper.class);
      TestUserMapper thatMapper =
          thatKadaiEngineProxy.getSqlSession().getMapper(TestUserMapper.class);
      TestUser expected =
          new TestUser("teamlead-42", "Max", "Mustermann", "Long name of teamlead-42");

      final IllegalStateException expectedException = new IllegalStateException();

      ThrowingCallable call =
          () ->
              transactionTemplate.execute(
                  status -> {
                    status.setRollbackOnly();
                    thisKadaiEngineProxy
                        .getEngine()
                        .executeInDatabaseConnection(() -> thisMapper.insert(expected));
                    throw expectedException;
                  });

      assertThatExceptionOfType(IllegalStateException.class)
          .isThrownBy(call)
          .isSameAs(expectedException);

      TestUser actual =
          thatKadaiEngineProxy
              .getEngine()
              .executeInDatabaseConnection(() -> thatMapper.findById(expected.getId()));

      assertThat(actual).isNull();
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class KadaiEngineAutocommitModeTest {

    private KadaiEngineProxy thisKadaiEngineProxy;
    private KadaiEngineProxy thatKadaiEngineProxy;

    @BeforeEach
    public void setupKadaiEngines() throws Exception {
      String schemaName = TestContainerExtension.determineSchemaName();
      DataSource dataSource = TestContainerExtension.DATA_SOURCE;
      KadaiConfiguration kadaiConfiguration =
          new KadaiConfiguration.Builder(dataSource, false, schemaName, false)
              .initKadaiProperties()
              .build();
      thisKadaiEngineProxy =
          new KadaiEngineProxy(
              KadaiEngine.buildKadaiEngine(
                  kadaiConfiguration, ConnectionManagementMode.AUTOCOMMIT));
      thisKadaiEngineProxy.getSqlSession().getConfiguration().addMapper(TestUserMapper.class);

      thatKadaiEngineProxy = new KadaiEngineProxy(KadaiEngine.buildKadaiEngine(kadaiConfiguration));
      thatKadaiEngineProxy.getSqlSession().getConfiguration().addMapper(TestUserMapper.class);
    }

    @Test
    void should_RetrieveCreated_When_NotManuallyCommited() {
      TestUserMapper thisMapper =
          thisKadaiEngineProxy.getSqlSession().getMapper(TestUserMapper.class);
      TestUserMapper thatMapper =
          thatKadaiEngineProxy.getSqlSession().getMapper(TestUserMapper.class);
      TestUser expected =
          new TestUser("teamlead-42", "Max", "Mustermann", "Long name of teamlead-42");

      thisKadaiEngineProxy
          .getEngine()
          .executeInDatabaseConnection(() -> thisMapper.insert(expected));

      TestUser actual =
          thatKadaiEngineProxy
              .getEngine()
              .executeInDatabaseConnection(() -> thatMapper.findById(expected.getId()));

      assertThat(actual).isEqualTo(expected);
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class KadaiEngineExplicitModeTest {

    private Connection thisConnection;
    private KadaiEngineProxy thisKadaiEngineProxy;
    private KadaiEngineProxy thatKadaiEngineProxy;

    @BeforeEach
    public void setupKadaiEngines() throws Exception {
      String schemaName = TestContainerExtension.determineSchemaName();
      DataSource dataSource = TestContainerExtension.DATA_SOURCE;
      KadaiConfiguration kadaiConfiguration =
          new KadaiConfiguration.Builder(dataSource, false, schemaName, false)
              .initKadaiProperties()
              .build();
      thisKadaiEngineProxy =
          new KadaiEngineProxy(
              KadaiEngine.buildKadaiEngine(kadaiConfiguration, ConnectionManagementMode.EXPLICIT));
      thisKadaiEngineProxy.getSqlSession().getConfiguration().addMapper(TestUserMapper.class);
      thisConnection = dataSource.getConnection();
      thisKadaiEngineProxy.getEngine().getEngine().setConnection(thisConnection);

      thatKadaiEngineProxy = new KadaiEngineProxy(KadaiEngine.buildKadaiEngine(kadaiConfiguration));
      thatKadaiEngineProxy.getSqlSession().getConfiguration().addMapper(TestUserMapper.class);
    }

    @AfterEach
    void cleanConnections() {
      thisKadaiEngineProxy.getEngine().getEngine().closeConnection();
    }

    @Test
    void should_RetrieveCreated_When_Committed() throws Exception {
      TestUserMapper thisMapper =
          thisKadaiEngineProxy.getSqlSession().getMapper(TestUserMapper.class);
      TestUserMapper thatMapper =
          thatKadaiEngineProxy.getSqlSession().getMapper(TestUserMapper.class);
      TestUser expected =
          new TestUser("teamlead-42", "Max", "Mustermann", "Long name of teamlead-42");

      thisKadaiEngineProxy
          .getEngine()
          .executeInDatabaseConnection(() -> thisMapper.insert(expected));
      thisConnection.commit();

      TestUser actual =
          thatKadaiEngineProxy
              .getEngine()
              .executeInDatabaseConnection(() -> thatMapper.findById(expected.getId()));

      assertThat(actual).isEqualTo(expected);
    }

    @Test
    void should_NotRetrieveCreated_When_NotCommitted() {
      TestUserMapper thisMapper =
          thisKadaiEngineProxy.getSqlSession().getMapper(TestUserMapper.class);
      TestUserMapper thatMapper =
          thatKadaiEngineProxy.getSqlSession().getMapper(TestUserMapper.class);
      TestUser expected =
          new TestUser("teamlead-42", "Max", "Mustermann", "Long name of teamlead-42");

      thisKadaiEngineProxy
          .getEngine()
          .executeInDatabaseConnection(() -> thisMapper.insert(expected));

      TestUser actual =
          thatKadaiEngineProxy
              .getEngine()
              .executeInDatabaseConnection(() -> thatMapper.findById(expected.getId()));

      assertThat(actual).isNull();
    }
  }
}
