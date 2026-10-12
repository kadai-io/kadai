/*
 * Copyright [2026] [envite consulting GmbH]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.kadai.common.rest.ldap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.kadai.common.api.exceptions.SystemException;
import io.kadai.user.api.models.User;
import io.kadai.user.internal.models.UserImpl;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class LdapClientPagingTest {

  @Test
  void should_CollectEveryPageAndForwardCookies() {
    byte[] first = "first".getBytes(StandardCharsets.UTF_8);
    byte[] second = "second".getBytes(StandardCharsets.UTF_8);
    Deque<LdapUserPage> pages =
        new ArrayDeque<>(
            List.of(
                new LdapUserPage(List.of(user("a")), true, first),
                new LdapUserPage(List.of(user("b")), true, second),
                new LdapUserPage(List.of(user("c")), true, new byte[0])));
    List<byte[]> requests = new ArrayList<>();

    LdapUserSnapshot snapshot =
        LdapClient.collectCompleteUserSnapshot(
            request -> {
              requests.add(request);
              return pages.removeFirst();
            });

    assertThat(snapshot.users()).extracting(User::getId).containsExactly("a", "b", "c");
    assertThat(snapshot.pageCount()).isEqualTo(3);
    assertThat(snapshot.resultCount()).isEqualTo(3);
    assertThat(requests).extracting(bytes -> new String(bytes, StandardCharsets.UTF_8))
        .containsExactly("", "first", "second");
  }

  @Test
  void should_FailClosedWithoutResponseControl() {
    assertThatThrownBy(
            () ->
                LdapClient.collectCompleteUserSnapshot(
                    ignored -> new LdapUserPage(List.of(user("partial")), false, new byte[0])))
        .isInstanceOf(SystemException.class)
        .hasMessageContaining("response control");
  }

  @Test
  void should_RejectRepeatedNonTerminalCookieAfterReadingTheRepeatedPage() {
    byte[] repeated = "same".getBytes(StandardCharsets.UTF_8);
    AtomicInteger calls = new AtomicInteger();

    assertThatThrownBy(
            () ->
                LdapClient.collectCompleteUserSnapshot(
                    ignored -> {
                      calls.incrementAndGet();
                      return new LdapUserPage(List.of(user("a")), true, repeated);
                    }))
        .isInstanceOf(SystemException.class)
        .hasMessageContaining("repeated")
        .hasMessageContaining("cookie");
    assertThat(calls).hasValue(2);
  }

  @Test
  void should_WrapLaterPageFailureAndNotReturnPartialSnapshot() {
    RuntimeException failure = new RuntimeException("page failed");
    AtomicInteger calls = new AtomicInteger();

    assertThatThrownBy(
            () ->
                LdapClient.collectCompleteUserSnapshot(
                    ignored -> {
                      if (calls.getAndIncrement() == 0) {
                        return new LdapUserPage(
                            List.of(user("a")),
                            true,
                            "next".getBytes(StandardCharsets.UTF_8));
                      }
                      throw failure;
                    }))
        .isInstanceOf(SystemException.class)
        .hasCause(failure)
        .hasMessageContaining("complete LDAP user snapshot");
  }

  @Test
  void should_DefensivelyCopyPageAndSnapshot() {
    List<User> users = new ArrayList<>(List.of(user("a")));
    byte[] cookie = "cookie".getBytes(StandardCharsets.UTF_8);
    LdapUserPage page = new LdapUserPage(users, true, cookie);
    users.clear();
    cookie[0] = 'X';

    assertThat(page.users()).extracting(User::getId).containsExactly("a");
    assertThat(new String(page.nextCookie(), StandardCharsets.UTF_8)).isEqualTo("cookie");

    LdapUserSnapshot snapshot =
        LdapClient.collectCompleteUserSnapshot(
            ignored -> new LdapUserPage(page.users(), true, new byte[0]));
    List<User> snapshotUsers = snapshot.users();
    assertThatThrownBy(snapshotUsers::clear)
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void should_ValidateSnapshotMetadata() {
    assertThatThrownBy(() -> new LdapUserSnapshot(List.of(), 0, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("pageCount");
    List<User> users = List.of(user("a"));
    assertThatThrownBy(() -> new LdapUserSnapshot(users, 1, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("resultCount");
  }

  private static User user(String id) {
    UserImpl user = new UserImpl();
    user.setId(id);
    user.setFirstName("First");
    user.setLastName("Last");
    user.setGroups(Set.of());
    user.setPermissions(Set.of());
    return user;
  }
}
