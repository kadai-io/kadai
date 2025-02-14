package acceptance.common;

import static org.assertj.core.api.Assertions.assertThat;

import acceptance.common.TestUserMapper.TestUser;
import io.kadai.KadaiConfiguration;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.KadaiEngine.ConnectionManagementMode;
import io.kadai.testapi.KadaiEngineProxy;
import io.kadai.testapi.extensions.TestContainerExtension;
import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

public class KadaiEngineModesTest {

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  public class KadaiEngineParticipateModeTest {

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
                  kadaiConfiguration, ConnectionManagementMode.PARTICIPATE));
      thisKadaiEngineProxy.getSqlSession().getConfiguration().addMapper(TestUserMapper.class);

      thatKadaiEngineProxy = new KadaiEngineProxy(KadaiEngine.buildKadaiEngine(kadaiConfiguration));
      thatKadaiEngineProxy.getSqlSession().getConfiguration().addMapper(TestUserMapper.class);
    }

    @Test
    void should_NotRetrieveCreated_When_NotCommitted() {
      TestUserMapper thisMapper =
          thisKadaiEngineProxy.getSqlSession().getMapper(TestUserMapper.class);
      TestUserMapper thatMapper =
          thatKadaiEngineProxy.getSqlSession().getMapper(TestUserMapper.class);
      TestUser expected = new TestUser("user-1-1", "Max", "Mustermann", "Long name of user-1-1");

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
      TestUser expected = new TestUser("user-1-1", "Max", "Mustermann", "Long name of user-1-1");

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
      TestUser expected = new TestUser("user-1-1", "Max", "Mustermann", "Long name of user-1-1");

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
      TestUser expected = new TestUser("user-1-1", "Max", "Mustermann", "Long name of user-1-1");

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
