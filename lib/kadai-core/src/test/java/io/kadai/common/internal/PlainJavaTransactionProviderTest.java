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

package io.kadai.common.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.kadai.common.api.KadaiEngine.ConnectionManagementMode;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.internal.jobs.PlainJavaTransactionProvider;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlainJavaTransactionProviderTest {

  @Mock KadaiEngineImpl kadaiEngine;
  @Mock InternalKadaiEngine internalKadaiEngine;
  @Mock DataSource dataSource;
  @Mock Connection connection;

  PlainJavaTransactionProvider provider;

  @BeforeEach
  void setUp() throws Exception {
    when(kadaiEngine.getConnectionManagementMode()).thenReturn(ConnectionManagementMode.AUTOCOMMIT);
    when(kadaiEngine.getInternalKadaiEngine()).thenReturn(internalKadaiEngine);
    when(dataSource.getConnection()).thenReturn(connection);
    provider = new PlainJavaTransactionProvider(kadaiEngine, dataSource);
  }

  @Test
  void should_AbortConnectionBeforeSessionCleanupWhenRollbackFails() throws Exception {
    IllegalStateException failure = new IllegalStateException("Refresh failed");
    SQLException rollbackFailure = new SQLException("Rollback failed");
    doThrow(rollbackFailure).when(connection).rollback();

    assertThatThrownBy(
            () -> provider.executeInTransaction(() -> {
              throw failure;
            }))
        .isSameAs(failure)
        .hasSuppressedException(rollbackFailure);

    var order = inOrder(connection, kadaiEngine);
    order.verify(connection).rollback();
    order.verify(connection).abort(any());
    order.verify(kadaiEngine).closeConnection();
    verify(connection, never()).commit();
  }

  @Test
  void should_CloseConnectionBeforeSessionCleanupWhenAbortFails() throws Exception {
    IllegalStateException failure = new IllegalStateException("Refresh failed");
    SQLException rollbackFailure = new SQLException("Rollback failed");
    SQLException abortFailure = new SQLException("Abort failed");
    doThrow(rollbackFailure).when(connection).rollback();
    doThrow(abortFailure).when(connection).abort(any());

    assertThatThrownBy(
            () -> provider.executeInTransaction(() -> {
              throw failure;
            }))
        .isSameAs(failure)
        .hasSuppressedException(rollbackFailure)
        .hasSuppressedException(abortFailure);

    var order = inOrder(connection, kadaiEngine);
    order.verify(connection).rollback();
    order.verify(connection).abort(any());
    order.verify(connection).close();
    order.verify(kadaiEngine).closeConnection();
    verify(connection, never()).commit();
  }

  @Test
  void should_PreserveCloseFailureWhenRollbackAndAbortFail() throws Exception {
    IllegalStateException failure = new IllegalStateException("Refresh failed");
    SQLException rollbackFailure = new SQLException("Rollback failed");
    SQLException abortFailure = new SQLException("Abort failed");
    SQLException closeFailure = new SQLException("Close failed");
    doThrow(rollbackFailure).when(connection).rollback();
    doThrow(abortFailure).when(connection).abort(any());
    doThrow(closeFailure).doNothing().when(connection).close();

    assertThatThrownBy(
            () -> provider.executeInTransaction(() -> {
              throw failure;
            }))
        .isSameAs(failure)
        .hasSuppressedException(rollbackFailure)
        .hasSuppressedException(abortFailure)
        .hasSuppressedException(closeFailure);

    var order = inOrder(connection, kadaiEngine);
    order.verify(connection).rollback();
    order.verify(connection).abort(any());
    order.verify(connection).close();
    order.verify(kadaiEngine).closeConnection();
    verify(connection, never()).commit();
  }

  @Test
  void should_RollBackAndPreserveCommitFailure() throws Exception {
    SQLException failure = new SQLException("Commit failed");
    doThrow(failure).when(connection).commit();

    assertThatThrownBy(() -> provider.executeInTransaction(() -> "result"))
        .isInstanceOf(SystemException.class)
        .hasCause(failure);

    var order = inOrder(connection, kadaiEngine);
    order.verify(connection).commit();
    order.verify(connection).rollback();
    order.verify(kadaiEngine).closeConnection();
    verify(connection, never()).abort(any());
  }
}
