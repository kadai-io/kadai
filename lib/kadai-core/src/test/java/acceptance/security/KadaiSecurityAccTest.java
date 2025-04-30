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

  @WithAccessId(user = "businessadmin")
  @Test
  void should_RunAsAdminOnlyTemorarily_When_RunAsAdminMethodIsCalled() throws Exception {
    assertThat(kadaiEngine.isUserInRole(KadaiRole.BUSINESS_ADMIN)).isTrue();
    assertThat(kadaiEngine.isUserInRole(KadaiRole.ADMIN)).isFalse();

    new KadaiEngineProxy(kadaiEngine)
        .getEngine()
        .getEngine()
        .runAsAdmin(() -> assertThat(kadaiEngine.isUserInRole(KadaiRole.ADMIN)).isTrue());

    assertThat(kadaiEngine.isUserInRole(KadaiRole.ADMIN)).isFalse();
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
