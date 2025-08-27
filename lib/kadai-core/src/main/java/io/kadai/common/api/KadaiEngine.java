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

package io.kadai.common.api;

import io.kadai.KadaiConfiguration;
import io.kadai.classification.api.ClassificationService;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.api.security.CurrentUserContext;
import io.kadai.common.internal.KadaiEngineImpl;
import io.kadai.common.internal.workingtime.WorkingTimeCalculatorImpl;
import io.kadai.monitor.api.MonitorService;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.models.Task;
import io.kadai.user.api.UserService;
import io.kadai.user.api.models.User;
import io.kadai.workbasket.api.WorkbasketService;
import java.sql.SQLException;
import java.util.function.Supplier;
import org.apache.ibatis.transaction.TransactionFactory;

/** The KadaiEngine represents an overall set of all needed services. */
public interface KadaiEngine {
  String MINIMAL_KADAI_SCHEMA_VERSION = "11.0.0";

  /**
   * This method creates the {@linkplain KadaiEngine} with {@linkplain
   * ConnectionManagementMode#PARTICIPATE}.
   *
   * @see KadaiEngine#buildKadaiEngine(KadaiConfiguration, ConnectionManagementMode)
   */
  @SuppressWarnings("checkstyle:JavadocMethod")
  static KadaiEngine buildKadaiEngine(KadaiConfiguration configuration) throws SQLException {
    return buildKadaiEngine(configuration, ConnectionManagementMode.PARTICIPATE, null);
  }

  /**
   * Builds an {@linkplain KadaiEngine} based on {@linkplain KadaiConfiguration} and
   * SqlConnectionMode.
   *
   * @param configuration complete kadaiConfig to build the engine
   * @param connectionManagementMode connectionMode for the SqlSession
   * @return a {@linkplain KadaiEngineImpl}
   * @throws SQLException when the db schema could not be initialized
   */
  static KadaiEngine buildKadaiEngine(
      KadaiConfiguration configuration, ConnectionManagementMode connectionManagementMode)
      throws SQLException {
    return buildKadaiEngine(configuration, connectionManagementMode, null);
  }

  /**
   * Builds an {@linkplain KadaiEngine} based on {@linkplain KadaiConfiguration}, SqlConnectionMode
   * and TransactionFactory.
   *
   * @param configuration complete kadaiConfig to build the engine
   * @param connectionManagementMode connectionMode for the SqlSession
   * @param transactionFactory the TransactionFactory
   * @return a {@linkplain KadaiEngineImpl}
   * @throws SQLException when the db schema could not be initialized
   */
  static KadaiEngine buildKadaiEngine(
      KadaiConfiguration configuration,
      ConnectionManagementMode connectionManagementMode,
      TransactionFactory transactionFactory)
      throws SQLException {
    return KadaiEngineImpl.createKadaiEngine(
        configuration, connectionManagementMode, transactionFactory);
  }

  /**
   * Returns a {@linkplain TaskService} initialized with the current KadaiEngine. {@linkplain
   * TaskService} can be used for operations on all {@linkplain Task Tasks}.
   *
   * @return an instance of {@linkplain TaskService}
   */
  TaskService getTaskService();

  /**
   * Returns a {@linkplain MonitorService} initialized with the current KadaiEngine. {@linkplain
   * MonitorService} can be used for monitoring {@linkplain Task Tasks}.
   *
   * @return an instance of {@linkplain MonitorService}
   */
  MonitorService getMonitorService();

  /**
   * Returns a {@linkplain WorkbasketService} initialized with the current KadaiEngine. The
   * {@linkplain WorkbasketService} can be used for operations on all {@linkplain
   * io.kadai.workbasket.api.models.Workbasket Workbaskets}.
   *
   * @return an instance of {@linkplain WorkbasketService}
   */
  WorkbasketService getWorkbasketService();

  /**
   * Returns a {@linkplain ClassificationService} initialized with the current KadaiEngine. The
   * {@linkplain ClassificationService} can be used for operations on all {@linkplain
   * io.kadai.classification.api.models.Classification Classifications}.
   *
   * @return an instance of {@linkplain ClassificationService}
   */
  ClassificationService getClassificationService();

  /**
   * Returns a {@linkplain JobService} initialized with the current KadaiEngine. The {@linkplain
   * JobService} can be used for all operations on {@linkplain
   * io.kadai.common.internal.jobs.KadaiJob KadaiJobs}.
   *
   * @return an instance of {@linkplain JobService}
   */
  JobService getJobService();

  /**
   * Returns a {@linkplain UserService} initialized with the current KadaiEngine. The {@linkplain
   * UserService} can be used for all operations on {@linkplain io.kadai.user.api.models.User
   * Users}.
   *
   * @return an instance of {@linkplain UserService}
   */
  UserService getUserService();

  /**
   * Returns a {@linkplain ConfigurationService} initialized with the current KadaiEngine. The
   * {@linkplain ConfigurationService} can be used to manage custom configuration options.
   *
   * @return an instance of {@linkplain ConfigurationService}
   */
  ConfigurationService getConfigurationService();

  /**
   * Returns the {@linkplain KadaiConfiguration configuration} of the KadaiEngine.
   *
   * @return {@linkplain KadaiConfiguration configuration}
   */
  KadaiConfiguration getConfiguration();

  /**
   * Returns the {@linkplain WorkingTimeCalculator} of the KadaiEngine. The {@linkplain
   * WorkingTimeCalculator} is used to add or subtract working time from Instants according to a
   * working time schedule or to calculate the working time between Instants.
   *
   * @return {@linkplain WorkingTimeCalculatorImpl}
   */
  WorkingTimeCalculator getWorkingTimeCalculator();

  /**
   * Checks if the {@linkplain io.kadai.spi.history.api.KadaiHistory KadaiHistory} plugin is
   * enabled.
   *
   * @return true if the history is enabled; otherwise false
   */
  boolean isHistoryEnabled();

  /**
   * Returns the {@linkplain ConnectionManagementMode ConnectionManagementMode} of the KadaiEngine.
   *
   * @return {@linkplain ConnectionManagementMode ConnectionManagementMode}
   */
  ConnectionManagementMode getConnectionManagementMode();

  /**
   * Sets {@linkplain ConnectionManagementMode ConnectionManagementMode} of the KadaiEngine.
   *
   * @param mode the valid values for the {@linkplain ConnectionManagementMode} are:
   *     <ul>
   *       <li>{@linkplain ConnectionManagementMode#PARTICIPATE PARTICIPATE} - kadai participates in
   *           global transaction; this is the default mode
   *       <li>{@linkplain ConnectionManagementMode#AUTOCOMMIT AUTOCOMMIT} - kadai commits each API
   *           call separately
   *       <li>{@linkplain ConnectionManagementMode#EXPLICIT EXPLICIT} - commit processing is
   *           managed explicitly by the client
   *     </ul>
   */
  void setConnectionManagementMode(ConnectionManagementMode mode);

  /**
   * Set the {@code Connection} to be used by KADAI in mode {@linkplain
   * ConnectionManagementMode#EXPLICIT EXPLICIT}. If this API is called, KADAI uses the {@code
   * Connection} passed by the client for all subsequent API calls until the client resets this
   * {@code Connection}. Control over commit and rollback of the {@code Connection} is the
   * responsibility of the client. In order to close the {@code Connection}, {@code
   * closeConnection()} or {@code setConnection(null)} has to be called.
   *
   * @param connection - The {@code java.sql.Connection} that is controlled by the client
   * @throws SQLException if a database access error occurs
   */
  void setConnection(java.sql.Connection connection) throws SQLException;

  /**
   * Closes the client's connection, sets it to null and switches to mode {@linkplain
   * ConnectionManagementMode#PARTICIPATE PARTICIPATE}. Only applicable in mode {@linkplain
   * ConnectionManagementMode#EXPLICIT EXPLICIT}. Has the same effect as {@code
   * setConnection(null)}.
   */
  void closeConnection();

  /**
   * Check whether the current user is member of one of the {@linkplain KadaiRole KadaiRoles}
   * specified.
   *
   * @param roles The {@linkplain KadaiRole KadaiRoles} that are checked for membership of the
   *     current user
   * @return true if the current user is a member of at least one of the specified {@linkplain
   *     KadaiRole KadaiRole}
   */
  boolean isUserInRole(KadaiRole... roles);

  /**
   * Checks whether current user is member of the specified {@linkplain KadaiRole KadaiRoles}.
   *
   * @param roles The {@linkplain KadaiRole KadaiRoles} that are checked for membership of the
   *     current user
   * @throws NotAuthorizedException If the current user is not member of any specified {@linkplain
   *     KadaiRole KadaiRole}
   */
  void checkRoleMembership(KadaiRole... roles) throws NotAuthorizedException;

  /**
   * Execute an action for a user with the privileges of another {@link KadaiRole}.
   *
   * <p>This can be thought of as temporarily <i>lifting</i> the user into the given role - the role
   * therefore is acting as proxy.
   *
   * @param supplier the action to execute
   * @param proxy the role to temporarily lift the user to - acting as proxy
   * @param userId the {@linkplain User#getId() id} of the user to be lifted
   * @return the return value of the action
   * @param <T> the return value of the action
   */
  <T> T runAs(Supplier<T> supplier, KadaiRole proxy, String userId);

  /**
   * This is a convenience-method for {@link #runAs(Supplier, KadaiRole, String)}.
   *
   * @param runnable the action to execute
   * @param proxy the role to temporarily lift the user to - acting as proxy
   * @param userId the {@linkplain User#getId() id} of the user to be lifted
   * @see #runAs(Supplier, KadaiRole, String)
   */
  default void runAs(Runnable runnable, KadaiRole proxy, String userId) {
    runAs(
        () -> {
          runnable.run();
          return null;
        },
        proxy,
        userId);
  }

  /**
   * This is a convenience-method for {@link #runAs(Supplier, KadaiRole, String)}.
   *
   * <p>It <b>overrides</b> the {@linkplain CurrentUserContext#getUserId() current userId} with one
   * of an admin, leaving the <b>proxy empty</b>.
   *
   * @param supplier the action to execute
   * @param <T> the return value of the action
   * @return the return value of the action
   * @see #runAs(Supplier, KadaiRole, String)
   */
  default <T> T runAsAdmin(Supplier<T> supplier) {
    if (isUserInRole(KadaiRole.ADMIN)) {
      return supplier.get();
    }

    String adminName =
        this.getConfiguration().getRoleMap().get(KadaiRole.ADMIN).stream()
            .findFirst()
            .orElseThrow(() -> new SystemException("There is no admin configured"));
    return runAs(supplier, null, adminName);
  }

  /**
   * This is a convenience-method for {@link #runAs(Supplier, KadaiRole, String)}.
   *
   * @param supplier the action to execute
   * @param userId the {@linkplain User#getId() id} of the user to be lifted
   * @param <T> the return value of the action
   * @return the return value of the action
   * @see #runAs(Supplier, KadaiRole, String)
   */
  default <T> T runAsAdmin(Supplier<T> supplier, String userId) {
    return runAs(supplier, KadaiRole.ADMIN, userId);
  }

  /**
   * This is a convenience-method for {@link #runAs(Supplier, KadaiRole, String)}.
   *
   * <p>It <b>overrides</b> the {@linkplain CurrentUserContext#getUserId() current userId} with one
   * of an admin, leaving the <b>proxy empty</b>.
   *
   * @param runnable the action to execute
   * @see #runAs(Supplier, KadaiRole, String)
   */
  default void runAsAdmin(Runnable runnable) {
    runAsAdmin(
        () -> {
          runnable.run();
          return null;
        });
  }

  /**
   * This is a convenience-method for {@link #runAs(Runnable, KadaiRole, String)}.
   *
   * @param runnable the action to execute
   * @param userId the {@linkplain User#getId() id} of the user to be lifted
   * @see #runAs(Supplier, KadaiRole, String)
   */
  default void runAsAdmin(Runnable runnable, String userId) {
    runAsAdmin(
        () -> {
          runnable.run();
          return null;
        },
        userId);
  }

  /**
   * Returns the {@linkplain CurrentUserContext} of the KadaiEngine.
   *
   * @return {@linkplain CurrentUserContext}
   */
  CurrentUserContext getCurrentUserContext();

  /** Clears the cache of the underlying local SQL session. */
  void clearSqlSessionCache();

  /**
   * Connection management mode. Controls the connection handling of kadai
   *
   * <ul>
   *   <li>{@linkplain ConnectionManagementMode#PARTICIPATE PARTICIPATE} - kadai participates * in
   *       global transaction; this is the default mode *
   *   <li>{@linkplain ConnectionManagementMode#AUTOCOMMIT AUTOCOMMIT} - kadai commits each * API
   *       call separately *
   *   <li>{@linkplain ConnectionManagementMode#EXPLICIT EXPLICIT} - commit processing is * managed
   *       explicitly by the client
   * </ul>
   */
  enum ConnectionManagementMode {
    PARTICIPATE,
    AUTOCOMMIT,
    EXPLICIT
  }
}
