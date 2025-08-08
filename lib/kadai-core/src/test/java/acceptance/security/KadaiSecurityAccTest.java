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

package acceptance.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import acceptance.AbstractAccTest;
import acceptance.KadaiEngineProxy;
import io.kadai.common.api.KadaiRole;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.common.test.security.WithAccessId;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Acceptance test for task queries and authorization. */
@ExtendWith(JaasExtension.class)
class KadaiSecurityAccTest extends AbstractAccTest {

  KadaiSecurityAccTest() {
    super();
  }

  @Test
  void should_ThrowException_When_AccessIdIsUnauthenticated() {
    assertThat(kadaiEngine.isUserInRole(KadaiRole.BUSINESS_ADMIN)).isFalse();
    assertThat(kadaiEngine.isUserInRole(KadaiRole.ADMIN)).isFalse();
    ThrowingCallable call = () -> kadaiEngine.checkRoleMembership(KadaiRole.BUSINESS_ADMIN);
    assertThatThrownBy(call).isInstanceOf(NotAuthorizedException.class);
  }

  @ParameterizedTest
  @CsvSource(
      value = {
        "ADMIN;user-1-1;uid=admin,cn=users,ou=test,o=kadai",
        "BUSINESS_ADMIN;teamlead-1;cn=business-admins,cn=groups,ou=test,o=kadai",
        "MONITOR;user-1-3;cn=monitor-users,cn=groups,ou=test,o=kadai",
        "TASK_ADMIN;user-1-4;taskadmin",
        "TASK_ROUTER;user-4-2;cn=routers,cn=groups,ou=test,o=kadai",
      },
      delimiter = ';')
  @WithAccessId(user = "user-1-1")
  void should_TemporarilyRunAsWithProxiedAccessId(
      KadaiRole proxy, String userId, String expectedProxyAccessId) throws Exception {
    assertThat(kadaiEngine.isUserInRole(proxy)).isFalse();
    assertThat(kadaiEngine.getCurrentUserContext().getUserContext().getProxyAccessId()).isNull();
    assertThat(kadaiEngine.getCurrentUserContext().getUserContext().getUserId())
        .isEqualTo("user-1-1");
    assertThat(kadaiEngine.getCurrentUserContext().getUserId()).isEqualTo("user-1-1");

    new KadaiEngineProxy(kadaiEngine)
        .getEngine()
        .getEngine()
        .runAs(
            () -> {
              assertThat(kadaiEngine.isUserInRole(proxy)).isTrue();
              assertThat(kadaiEngine.getCurrentUserContext().getUserContext().getProxyAccessId())
                  .isEqualTo(expectedProxyAccessId);
              assertThat(kadaiEngine.getCurrentUserContext().getUserContext().getUserId())
                  .isEqualTo(userId);
              assertThat(kadaiEngine.getCurrentUserContext().getUserId()).isEqualTo(userId);
            },
            proxy,
            userId);

    assertThat(kadaiEngine.isUserInRole(proxy)).isFalse();
    assertThat(kadaiEngine.getCurrentUserContext().getUserContext().getProxyAccessId()).isNull();
    assertThat(kadaiEngine.getCurrentUserContext().getUserContext().getUserId())
        .isEqualTo("user-1-1");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "user-1-1",
        "user-1-2",
        "user-1-3",
        "user-2-1",
        "user-2-5",
        "user-4-2",
        "teamlead-1",
        "teamlead-2",
        "businessadmin",
        "admin",
      })
  @WithAccessId(user = "user-1-2")
  void should_TemporarilyRunAsAdmin(String userId) throws Exception {
    assertThat(kadaiEngine.isUserInRole(KadaiRole.ADMIN)).isFalse();
    assertThat(kadaiEngine.getCurrentUserContext().getUserContext().getProxyAccessId()).isNull();
    assertThat(kadaiEngine.getCurrentUserContext().getUserContext().getUserId())
        .isEqualTo("user-1-2");
    assertThat(kadaiEngine.getCurrentUserContext().getUserId()).isEqualTo("user-1-2");

    new KadaiEngineProxy(kadaiEngine)
        .getEngine()
        .getEngine()
        .runAsAdmin(
            () -> {
              assertThat(kadaiEngine.isUserInRole(KadaiRole.ADMIN)).isTrue();
              assertThat(kadaiEngine.getCurrentUserContext().getUserContext().getProxyAccessId())
                  .isEqualTo("uid=admin,cn=users,ou=test,o=kadai");
              assertThat(kadaiEngine.getCurrentUserContext().getUserContext().getUserId())
                  .isEqualTo(userId);
              assertThat(kadaiEngine.getCurrentUserContext().getUserId()).isEqualTo(userId);
            },
            userId);

    assertThat(kadaiEngine.isUserInRole(KadaiRole.ADMIN)).isFalse();
    assertThat(kadaiEngine.getCurrentUserContext().getUserContext().getProxyAccessId()).isNull();
    assertThat(kadaiEngine.getCurrentUserContext().getUserContext().getUserId())
        .isEqualTo("user-1-2");
  }

  @WithAccessId(user = "user-1-1")
  @Test
  void should_ThrowException_When_CheckingNormalUserForAdminRoles() {
    assertThat(kadaiEngine.isUserInRole(KadaiRole.BUSINESS_ADMIN)).isFalse();
    assertThat(kadaiEngine.isUserInRole(KadaiRole.ADMIN)).isFalse();
    ThrowingCallable call = () -> kadaiEngine.checkRoleMembership(KadaiRole.BUSINESS_ADMIN);
    assertThatThrownBy(call).isInstanceOf(NotAuthorizedException.class);
  }

  @WithAccessId(user = "user-1-1", groups = "businessadmin")
  @Test
  void should_ConfirmBusinessAdminRole_When_AccessIdIsBusinessAdmin() throws Exception {
    assertThat(kadaiEngine.isUserInRole(KadaiRole.BUSINESS_ADMIN)).isTrue();
    assertThat(kadaiEngine.isUserInRole(KadaiRole.ADMIN)).isFalse();
    kadaiEngine.checkRoleMembership(KadaiRole.BUSINESS_ADMIN);
  }

  @WithAccessId(user = "user-1-1", groups = "taskadmin")
  @Test
  void should_ConfirmTaskAdminRole_When_AccessIdIsTaskAdmin() throws Exception {
    assertThat(kadaiEngine.isUserInRole(KadaiRole.TASK_ADMIN)).isTrue();
    assertThat(kadaiEngine.isUserInRole(KadaiRole.ADMIN)).isFalse();
    kadaiEngine.checkRoleMembership(KadaiRole.TASK_ADMIN);
  }

  @WithAccessId(user = "user-1-1", groups = "admin")
  @Test
  void should_ConfirmAdminRole_When_AccessIdIsAdmin() throws Exception {
    assertThat(kadaiEngine.isUserInRole(KadaiRole.BUSINESS_ADMIN)).isFalse();
    assertThat(kadaiEngine.isUserInRole(KadaiRole.ADMIN)).isTrue();
    kadaiEngine.checkRoleMembership(KadaiRole.ADMIN);
  }
}
