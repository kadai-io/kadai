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

package io.kadai.user.internal;

import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.internal.util.CheckedBiFunction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Applies a refresh plan with JDBC batches on the caller's connection. */
final class UserRefreshBatchWriter {
  static final String INSERT_USER_SQL =
      "INSERT INTO USER_INFO (USER_ID, FIRST_NAME, LAST_NAME, FULL_NAME, LONG_NAME, E_MAIL, "
          + "PHONE, MOBILE_PHONE, ORG_LEVEL_4, ORG_LEVEL_3, ORG_LEVEL_2, ORG_LEVEL_1, DATA) "
          + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
  static final String UPDATE_USER_SQL =
      "UPDATE USER_INFO SET FIRST_NAME = ?, LAST_NAME = ?, FULL_NAME = ?, LONG_NAME = ?, "
          + "E_MAIL = ?, PHONE = ?, MOBILE_PHONE = ?, ORG_LEVEL_4 = ?, ORG_LEVEL_3 = ?, "
          + "ORG_LEVEL_2 = ?, ORG_LEVEL_1 = ?, DATA = ? WHERE USER_ID = ?";
  static final String DELETE_USER_SQL = "DELETE FROM USER_INFO WHERE USER_ID = ?";
  static final String INSERT_GROUP_SQL = "INSERT INTO GROUP_INFO (USER_ID, GROUP_ID) VALUES (?, ?)";
  static final String DELETE_GROUP_SQL =
      "DELETE FROM GROUP_INFO WHERE USER_ID = ? AND GROUP_ID = ?";
  static final String INSERT_PERMISSION_SQL =
      "INSERT INTO PERMISSION_INFO (USER_ID, PERMISSION_ID) VALUES (?, ?)";
  static final String DELETE_PERMISSION_SQL =
      "DELETE FROM PERMISSION_INFO WHERE USER_ID = ? AND PERMISSION_ID = ?";

  private final Connection connection;
  private final int batchSize;
  private final Map<String, PreparedStatement> statements = new LinkedHashMap<>();
  private int pendingOperations;

  UserRefreshBatchWriter(Connection connection, int batchSize) {
    this.connection = connection;
    this.batchSize = batchSize;
    if (connection == null) {
      throw new IllegalArgumentException("connection must not be null");
    }
    if (batchSize <= 0) {
      throw new IllegalArgumentException("batchSize must be positive");
    }
  }

  void apply(UserRefreshPlan plan) {
    try {
      // exact orphan rows must disappear before a desired membership for the same user is added
      addRowsByUserAccessIdRow(plan.orphanGroupsToDelete(), DELETE_GROUP_SQL);
      addRowsByUserAccessIdRow(plan.orphanPermissionsToDelete(), DELETE_PERMISSION_SQL);
      addRowsByUserAccessIdRow(plan.groupsToDelete(), DELETE_GROUP_SQL);
      addRowsByUserAccessIdRow(plan.permissionsToDelete(), DELETE_PERMISSION_SQL);
      addDeleteRows(plan.userIdsToDelete());
      addUsers(plan.usersToInsert(), INSERT_USER_SQL, this::setUserInsertParameters);
      addUsers(plan.usersToUpdate(), UPDATE_USER_SQL, this::setUserUpdateParameters);
      addRowsByUserAccessIdRow(plan.groupsToInsert(), INSERT_GROUP_SQL);
      addRowsByUserAccessIdRow(plan.permissionsToInsert(), INSERT_PERMISSION_SQL);
      flush();
    } catch (SQLException e) {
      throw new SystemException("Could not apply user refresh JDBC batch", e);
    } finally {
      closeStatements();
    }
  }

  private void addUsers(
      List<UserRefreshState> users,
      String sql,
      CheckedBiFunction<PreparedStatement, UserRefreshState, Void, SQLException> setParameters)
      throws SQLException {
    for (UserRefreshState user : users) {
      PreparedStatement statement = statement(sql);
      setParameters.apply(statement, user);
      statement.addBatch();
      operationAdded();
    }
  }

  private void addRowsByUserAccessIdRow(List<UserAccessIdRow> rows, String sql)
      throws SQLException {
    for (UserAccessIdRow row : rows) {
      try (PreparedStatement statement = statement(sql)) {
        statement.setString(1, row.userId());
        statement.setString(2, row.accessId());
        statement.addBatch();
        operationAdded();
      }
    }
  }

  private void addDeleteRows(List<String> rows) throws SQLException {
    for (String value : rows) {
      try (PreparedStatement statement = statement(UserRefreshBatchWriter.DELETE_USER_SQL)) {
        statement.setString(1, value);
        statement.addBatch();
        operationAdded();
      }
    }
  }

  private Void setUserInsertParameters(PreparedStatement statement, UserRefreshState user)
      throws SQLException {
    statement.setString(1, user.id());
    statement.setString(2, user.firstName());
    statement.setString(3, user.lastName());
    statement.setString(4, user.fullName());
    statement.setString(5, user.longName());
    statement.setString(6, user.email());
    statement.setString(7, user.phone());
    statement.setString(8, user.mobilePhone());
    statement.setString(9, user.orgLevel4());
    statement.setString(10, user.orgLevel3());
    statement.setString(11, user.orgLevel2());
    statement.setString(12, user.orgLevel1());
    statement.setString(13, user.data());
    return null;
  }

  private Void setUserUpdateParameters(PreparedStatement statement, UserRefreshState user)
      throws SQLException {
    statement.setString(1, user.firstName());
    statement.setString(2, user.lastName());
    statement.setString(3, user.fullName());
    statement.setString(4, user.longName());
    statement.setString(5, user.email());
    statement.setString(6, user.phone());
    statement.setString(7, user.mobilePhone());
    statement.setString(8, user.orgLevel4());
    statement.setString(9, user.orgLevel3());
    statement.setString(10, user.orgLevel2());
    statement.setString(11, user.orgLevel1());
    statement.setString(12, user.data());
    statement.setString(13, user.id());
    return null;
  }

  private PreparedStatement statement(String sql) throws SQLException {
    PreparedStatement statement = statements.get(sql);
    if (statement == null) {
      statement = connection.prepareStatement(sql);
      statements.put(sql, statement);
    }
    return statement;
  }

  private void operationAdded() throws SQLException {
    pendingOperations++;
    if (pendingOperations >= batchSize) {
      flush();
    }
  }

  private void flush() throws SQLException {
    if (pendingOperations == 0) {
      return;
    }
    for (PreparedStatement statement : statements.values()) {
      int[] counts = statement.executeBatch();
      for (int count : counts) {
        if (count != 1 && count != Statement.SUCCESS_NO_INFO) {
          if (count == Statement.EXECUTE_FAILED) {
            throw new SystemException("User refresh JDBC batch reported an execute failure");
          }
          throw new SystemException(
              "User refresh JDBC batch returned unexpected update count: " + count);
        }
      }
    }
    pendingOperations = 0;
  }

  private void closeStatements() {
    for (PreparedStatement statement : statements.values()) {
      try {
        statement.close();
      } catch (SQLException ignored) {
        // The database operation's original failure is more useful to the caller.
      }
    }
  }
}
