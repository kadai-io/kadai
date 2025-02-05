package acceptance.common;

import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.KadaiConfiguration;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.KadaiEngine.ConnectionManagementMode;
import io.kadai.testapi.builder.UserBuilder;
import io.kadai.testapi.extensions.TestContainerExtension;
import io.kadai.user.api.models.User;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class KadaiEngineAutocommitModeTest {

  private KadaiEngine thisKadaiEngine;
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
        KadaiEngine.buildKadaiEngine(kadaiConfiguration, ConnectionManagementMode.AUTOCOMMIT);
    this.thatKadaiEngine = KadaiEngine.buildKadaiEngine(kadaiConfiguration);
  }

  @Test
  void should_RetrieveCreated() throws Exception {
    User expected =
        UserBuilder.newUser()
            .id("user-1-1")
            .firstName("Max")
            .lastName("Mustermann")
            .longName("Long name of user-1-1")
            .buildAndStore(thisKadaiEngine.getUserService());

    User actual = thatKadaiEngine.getUserService().getUser(expected.getId());

    assertThat(actual).isEqualTo(expected);
  }
}
