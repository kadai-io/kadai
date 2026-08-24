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

package io.kadai.user.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.kadai.common.api.exceptions.SystemException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRefreshBatchWriterTest {

  @Test
  void should_DoNothingForEmptyPlan() {
    Connection connection = mock(Connection.class);

    new UserRefreshBatchWriter(connection, 5).apply(plan());

    verifyNoInteractions(connection);
  }

  @Test
  void should_BindEveryInsertUserColumnInSchemaOrder() throws Exception {
    Connection connection = mock(Connection.class);
    PreparedStatement insert = statement(connection, UserRefreshBatchWriter.INSERT_USER_SQL);
    UserRefreshState user = user("insert", null);

    new UserRefreshBatchWriter(connection, 5).apply(plan(List.of(user), List.of()));

    InOrder order = inOrder(insert);
    order.verify(insert).setString(1, "insert");
    order.verify(insert).setString(2, "first");
    order.verify(insert).setString(3, "last");
    order.verify(insert).setString(4, "full");
    order.verify(insert).setString(5, "long");
    order.verify(insert).setString(6, "mail@example.com");
    order.verify(insert).setString(7, "phone");
    order.verify(insert).setString(8, "mobile");
    order.verify(insert).setString(9, "org-4");
    order.verify(insert).setString(10, "org-3");
    order.verify(insert).setString(11, "org-2");
    order.verify(insert).setString(12, "org-1");
    order.verify(insert).setString(13, null);
  }

  @Test
  void should_BindEveryUpdateUserColumnInSchemaOrder() throws Exception {
    Connection connection = mock(Connection.class);
    PreparedStatement update = statement(connection, UserRefreshBatchWriter.UPDATE_USER_SQL);
    UserRefreshState user = user("update", null);

    new UserRefreshBatchWriter(connection, 5).apply(plan(List.of(), List.of(user)));

    InOrder order = inOrder(update);
    order.verify(update).setString(1, "first");
    order.verify(update).setString(2, "last");
    order.verify(update).setString(3, "full");
    order.verify(update).setString(4, "long");
    order.verify(update).setString(5, "mail@example.com");
    order.verify(update).setString(6, "phone");
    order.verify(update).setString(7, "mobile");
    order.verify(update).setString(8, "org-4");
    order.verify(update).setString(9, "org-3");
    order.verify(update).setString(10, "org-2");
    order.verify(update).setString(11, "org-1");
    order.verify(update).setString(12, null);
    order.verify(update).setString(13, "update");
  }

  @Test
  void should_WriteOnlyExactGroupAndPermissionRows() throws Exception {
    Connection connection = mock(Connection.class);
    PreparedStatement groupInsert = statement(connection, UserRefreshBatchWriter.INSERT_GROUP_SQL);
    UserAccessIdRow group = new UserAccessIdRow("user", "group-add");
    UserAccessIdRow permission = new UserAccessIdRow("user", "permission-remove");
    final PreparedStatement permissionDelete =
        statement(connection, UserRefreshBatchWriter.DELETE_PERMISSION_SQL);

    new UserRefreshBatchWriter(connection, 10)
        .apply(
            plan(List.of(), List.of(), List.of(group), List.of(), List.of(), List.of(permission)));

    verify(groupInsert).setString(1, "user");
    verify(groupInsert).setString(2, "group-add");
    verify(groupInsert).addBatch();
    verify(permissionDelete).setString(1, "user");
    verify(permissionDelete).setString(2, "permission-remove");
    verify(permissionDelete).addBatch();
  }

  @Test
  void should_DeleteOrphanRowsBeforeDesiredMembershipRowsAreInserted() throws Exception {
    Connection connection = mock(Connection.class);
    PreparedStatement delete = statement(connection, UserRefreshBatchWriter.DELETE_GROUP_SQL);
    PreparedStatement insert = statement(connection, UserRefreshBatchWriter.INSERT_GROUP_SQL);

    new UserRefreshBatchWriter(connection, 10)
        .apply(
            plan(
                List.of(),
                List.of(),
                List.of(new UserAccessIdRow("user", "desired")),
                List.of(),
                List.of(new UserAccessIdRow("user", "orphan")),
                List.of()));

    InOrder order = inOrder(delete, insert);
    order.verify(delete).addBatch();
    order.verify(insert).addBatch();
  }

  @ParameterizedTest
  @MethodSource("batchBoundaries")
  void should_FlushAtConfiguredOperationBoundaries(
      int operationCount, int batchSize, int expectedFlushes) throws Exception {
    Connection connection = mock(Connection.class);
    PreparedStatement delete = statement(connection, UserRefreshBatchWriter.DELETE_USER_SQL);
    List<String> ids =
        java.util.stream.IntStream.range(0, operationCount).mapToObj(String::valueOf).toList();

    new UserRefreshBatchWriter(connection, batchSize)
        .apply(
            new UserRefreshPlan(
                List.of(),
                List.of(),
                ids,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                0,
                0));

    verify(delete, org.mockito.Mockito.times(expectedFlushes)).executeBatch();
  }

  static Stream<Arguments> batchBoundaries() {
    int size = 5;
    return Stream.of(
        Arguments.of(0, size, 0),
        Arguments.of(1, size, 1),
        Arguments.of(size, size, 1),
        Arguments.of(size + 1, size, 2),
        Arguments.of(2 * size, size, 2),
        Arguments.of(2 * size + 1, size, 3));
  }

  @Test
  void should_AcceptOneAndSuccessNoInfoUpdateCounts() throws Exception {
    Connection connection = mock(Connection.class);
    PreparedStatement insert = statement(connection, UserRefreshBatchWriter.INSERT_USER_SQL);
    when(insert.executeBatch()).thenReturn(new int[] {1, Statement.SUCCESS_NO_INFO});

    new UserRefreshBatchWriter(connection, 5)
        .apply(plan(List.of(user("one", "data"), user("two", "data")), List.of()));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 2, Statement.EXECUTE_FAILED})
  void should_RejectUnexpectedUpdateCounts(int updateCount) throws Exception {
    Connection connection = mock(Connection.class);
    PreparedStatement insert = statement(connection, UserRefreshBatchWriter.INSERT_USER_SQL);
    when(insert.executeBatch()).thenReturn(new int[] {updateCount});

    assertThatThrownBy(
            () ->
                new UserRefreshBatchWriter(connection, 5)
                    .apply(plan(List.of(user("one", "data")), List.of())))
        .isInstanceOf(SystemException.class);
  }

  @Test
  void should_WrapSqlExceptionAsSystemException() throws Exception {
    Connection connection = mock(Connection.class);
    when(connection.prepareStatement(UserRefreshBatchWriter.INSERT_USER_SQL))
        .thenThrow(new SQLException("database unavailable"));

    assertThatThrownBy(
            () ->
                new UserRefreshBatchWriter(connection, 5)
                    .apply(plan(List.of(user("one", "data")), List.of())))
        .isInstanceOf(SystemException.class)
        .hasCauseInstanceOf(SQLException.class);
  }

  @Test
  void should_CloseEveryPreparedStatementOnSuccess() throws Exception {
    Connection connection = mock(Connection.class);
    PreparedStatement insert = statement(connection, UserRefreshBatchWriter.INSERT_USER_SQL);
    PreparedStatement group = statement(connection, UserRefreshBatchWriter.INSERT_GROUP_SQL);

    new UserRefreshBatchWriter(connection, 10)
        .apply(
            plan(
                List.of(user("one", "data")),
                List.of(),
                List.of(new UserAccessIdRow("one", "group")),
                List.of(),
                List.of(),
                List.of()));

    verify(insert).close();
    verify(group).close();
  }

  @Test
  void should_CloseEveryPreparedStatementOnFailure() throws Exception {
    Connection connection = mock(Connection.class);
    PreparedStatement insert = statement(connection, UserRefreshBatchWriter.INSERT_USER_SQL);
    PreparedStatement group = statement(connection, UserRefreshBatchWriter.INSERT_GROUP_SQL);
    when(insert.executeBatch()).thenThrow(new SQLException("write failed"));

    assertThatThrownBy(
            () ->
                new UserRefreshBatchWriter(connection, 10)
                    .apply(
                        plan(
                            List.of(user("one", "data")),
                            List.of(),
                            List.of(new UserAccessIdRow("one", "group")),
                            List.of(),
                            List.of(),
                            List.of())))
        .isInstanceOf(SystemException.class);

    verify(insert).close();
    verify(group).close();
  }

  @Test
  void should_NotCommitRollbackCloseOrChangeAutoCommit() throws Exception {
    Connection connection = mock(Connection.class);
    statement(connection, UserRefreshBatchWriter.INSERT_USER_SQL);

    new UserRefreshBatchWriter(connection, 5)
        .apply(plan(List.of(user("one", "data")), List.of()));

    verify(connection, never()).commit();
    verify(connection, never()).rollback();
    verify(connection, never()).close();
    verify(connection, never()).setAutoCommit(anyBoolean());
  }

  private static PreparedStatement statement(Connection connection, String sql)
      throws SQLException {
    PreparedStatement statement = mock(PreparedStatement.class);
    lenient().when(connection.prepareStatement(sql)).thenReturn(statement);
    lenient().when(statement.executeBatch()).thenReturn(new int[] {1});
    return statement;
  }

  private static UserRefreshPlan plan() {
    return plan(List.of(), List.of());
  }

  private static UserRefreshPlan plan(
      List<UserRefreshState> inserts, List<UserRefreshState> updates) {
    return new UserRefreshPlan(
        inserts,
        updates,
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        0,
        0);
  }

  private static UserRefreshPlan plan(
      List<UserRefreshState> inserts,
      List<UserRefreshState> updates,
      List<UserAccessIdRow> groupsToInsert,
      List<UserAccessIdRow> permissionsToInsert,
      List<UserAccessIdRow> orphanGroupsToDelete,
      List<UserAccessIdRow> permissionsToDelete) {
    return new UserRefreshPlan(
        inserts,
        updates,
        List.of(),
        groupsToInsert,
        List.of(),
        permissionsToInsert,
        permissionsToDelete,
        orphanGroupsToDelete,
        List.of(),
        0,
        0);
  }

  private static UserRefreshState user(String id, String data) {
    return new UserRefreshState(
        id,
        "first",
        "last",
        "full",
        "long",
        "mail@example.com",
        "phone",
        "mobile",
        "org-1",
        "org-2",
        "org-3",
        "org-4",
        data,
        Set.of(),
        Set.of());
  }
}
