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

package acceptance.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import io.kadai.KadaiConfiguration;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.KadaiEngine.ConnectionManagementMode;
import io.kadai.testapi.KadaiEngineProxy;
import io.kadai.testapi.extensions.TestContainerExtension;
import io.kadai.user.api.UserQuery;
import io.kadai.user.api.exceptions.UserNotFoundException;
import io.kadai.user.api.models.User;
import io.kadai.user.api.models.UserSummary;
import io.kadai.user.internal.UserMapper;
import io.kadai.user.internal.models.UserImpl;
import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserTransactionIsolationAccTest {

  private Connection writerConnection;
  private KadaiEngineProxy writerEngine;
  private KadaiEngineProxy readerEngine;

  @BeforeEach
  void setupKadaiEngines() throws Exception {
    String schemaName = TestContainerExtension.determineSchemaName();
    DataSource dataSource = TestContainerExtension.DATA_SOURCE;
    KadaiConfiguration kadaiConfiguration =
        new KadaiConfiguration.Builder(dataSource, false, schemaName, false)
            .initKadaiProperties()
            .build();
    writerEngine =
        new KadaiEngineProxy(
            KadaiEngine.buildKadaiEngine(kadaiConfiguration, ConnectionManagementMode.EXPLICIT));
    writerConnection = dataSource.getConnection();
    writerEngine.getEngine().getEngine().setConnection(writerConnection);
    readerEngine = new KadaiEngineProxy(KadaiEngine.buildKadaiEngine(kadaiConfiguration));
  }

  @AfterEach
  void cleanConnections() {
    writerEngine.getEngine().getEngine().closeConnection();
  }

  @Test
  void should_NotDirtyReadUser_When_ReadThroughProductionUserService() throws Exception {
    User expected = newProductionUser("production-user");
    UserMapper writerMapper = writerEngine.getSqlSession().getMapper(UserMapper.class);

    writerEngine.getEngine().executeInDatabaseConnection(() -> writerMapper.insert(expected));

    assertThatExceptionOfType(UserNotFoundException.class)
        .isThrownBy(
            () -> readerEngine.getEngine().getEngine().getUserService().getUser(expected.getId()));

    writerConnection.commit();
    clearReaderSqlSessionCache();

    User actual = readerEngine.getEngine().getEngine().getUserService().getUser(expected.getId());
    assertThat(actual.getId()).isEqualTo(expected.getId());
  }

  @Test
  void should_NotDirtyReadUsers_When_UsingProductionUserQuery() throws Exception {
    User expected = newProductionUser("production-query-user");
    UserMapper writerMapper = writerEngine.getSqlSession().getMapper(UserMapper.class);
    UserQuery userQuery = readerEngine.getEngine().getEngine().getUserService().createUserQuery();
    userQuery.idIn(expected.getId());

    writerEngine.getEngine().executeInDatabaseConnection(() -> writerMapper.insert(expected));

    assertThat(userQuery.list()).isEmpty();
    assertThat(userQuery.count()).isZero();

    writerConnection.commit();
    clearReaderSqlSessionCache();

    List<UserSummary> actual = userQuery.list();
    assertThat(actual).extracting(UserSummary::getId).containsExactly(expected.getId());
    assertThat(userQuery.count()).isEqualTo(1L);
  }

  private void clearReaderSqlSessionCache() {
    readerEngine.openConnection();
    try {
      readerEngine.getEngine().getEngine().clearSqlSessionCache();
    } finally {
      readerEngine.returnConnection();
    }
  }

  private User newProductionUser(String id) {
    UserImpl user = new UserImpl();
    user.setId(id);
    user.setFirstName("First");
    user.setLastName("Last");
    user.setFullName("Last, First");
    user.setLongName("Last, First - (" + id + ")");
    return user;
  }
}
