package acceptance.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import io.kadai.KadaiConfiguration;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.KadaiEngine.ConnectionManagementMode;
import io.kadai.testapi.builder.UserBuilder;
import io.kadai.testapi.extensions.TestContainerExtension;
import io.kadai.user.api.exceptions.UserNotFoundException;
import io.kadai.user.api.models.User;
import java.sql.Connection;
import javax.sql.DataSource;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class KadaiEngineExplicitModeTest {

  private KadaiEngine thisKadaiEngine;
  private Connection thisConnection;
  private KadaiEngine thatKadaiEngine;

  @BeforeEach
  public void setupKadaiEngines() throws Exception {
    String schemaName = TestContainerExtension.determineSchemaName();
    DataSource dataSource = TestContainerExtension.DATA_SOURCE;
    KadaiConfiguration kadaiConfiguration =
        new KadaiConfiguration.Builder(dataSource, false, schemaName, false)
            .initKadaiProperties()
            .build();
    this.thisKadaiEngine =
        KadaiEngine.buildKadaiEngine(kadaiConfiguration, ConnectionManagementMode.EXPLICIT);
    this.thisConnection = dataSource.getConnection();
    thisKadaiEngine.setConnection(this.thisConnection);

    this.thatKadaiEngine = KadaiEngine.buildKadaiEngine(kadaiConfiguration);
  }

  @AfterEach
  void cleanConnections() {
    thisKadaiEngine.closeConnection();
  }

  @Test
  void should_RetrieveCreated_When_Committed() throws Exception {
    User expected =
        UserBuilder.newUser()
            .id("user-1-1")
            .firstName("Max")
            .lastName("Mustermann")
            .longName("Long name of user-1-1")
            .buildAndStore(thisKadaiEngine.getUserService());
    thisConnection.commit();

    User actual = thatKadaiEngine.getUserService().getUser(expected.getId());

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void should_NotRetrieveCreated_When_NotCommitted() throws Exception {
    User user =
        UserBuilder.newUser()
            .id("user-1-1")
            .firstName("Max")
            .lastName("Mustermann")
            .longName("Long name of user-1-1")
            .buildAndStore(thisKadaiEngine.getUserService());

    ThrowingCallable call = () -> thatKadaiEngine.getUserService().getUser(user.getId());

    assertThatExceptionOfType(UserNotFoundException.class).isThrownBy(call);
  }
}
