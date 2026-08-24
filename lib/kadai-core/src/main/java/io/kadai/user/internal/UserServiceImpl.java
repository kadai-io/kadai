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

package io.kadai.user.internal;

import static io.kadai.common.internal.util.CheckedSupplier.wrapping;

import io.kadai.common.api.BaseQuery.SortDirection;
import io.kadai.common.api.KadaiRole;
import io.kadai.common.api.exceptions.DuplicateUserRefreshIdException;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.internal.InternalKadaiEngine;
import io.kadai.common.internal.util.LogSanitizer;
import io.kadai.user.api.UserQuery;
import io.kadai.user.api.UserService;
import io.kadai.user.api.exceptions.UserAlreadyExistException;
import io.kadai.user.api.exceptions.UserNotFoundException;
import io.kadai.user.api.models.User;
import io.kadai.user.internal.models.UserImpl;
import io.kadai.workbasket.api.WorkbasketPermission;
import io.kadai.workbasket.api.WorkbasketQueryColumnName;
import io.kadai.workbasket.api.WorkbasketService;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.ibatis.exceptions.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserServiceImpl implements UserService {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
  private final InternalKadaiEngine internalKadaiEngine;
  private final UserMapper userMapper;
  private final WorkbasketService workbasketService;
  private final List<WorkbasketPermission> minimalWorkbasketPermissions;

  public UserServiceImpl(InternalKadaiEngine internalKadaiEngine, UserMapper userMapper) {
    this.internalKadaiEngine = internalKadaiEngine;
    this.userMapper = userMapper;
    this.workbasketService = internalKadaiEngine.getEngine().getWorkbasketService();
    minimalWorkbasketPermissions =
        List.copyOf(
            internalKadaiEngine
                .getEngine()
                .getConfiguration()
                .getMinimalPermissionsToAssignDomains());
  }

  @Override
  public User newUser() {
    return new UserImpl();
  }

  @Override
  public User getUser(String userId) throws UserNotFoundException, InvalidArgumentException {
    if (userId == null || userId.isEmpty()) {
      throw new InvalidArgumentException("UserId can't be used as NULL-Parameter.");
    }
    String finalUserId = userId.toLowerCase();

    UserImpl user =
        internalKadaiEngine.executeInDatabaseConnection(() -> userMapper.findById(finalUserId));
    if (user == null) {
      throw new UserNotFoundException(userId);
    }

    user.setDomains(determineDomains(user));
    return user;
  }

  @Override
  public List<User> getUsers(Set<String> userIds) throws InvalidArgumentException {
    if (userIds == null || userIds.isEmpty()) {
      throw new InvalidArgumentException("UserIds can't be used as NULL-Parameter.");
    }
    Set<String> finalUserIds =
        userIds.stream().map(String::toLowerCase).collect(Collectors.toSet());

    List<UserImpl> users =
        internalKadaiEngine.executeInDatabaseConnection(() -> userMapper.findByIds(finalUserIds));

    users.forEach(user -> user.setDomains(determineDomains(user)));

    return users.stream().map(User.class::cast).toList();
  }

  @Override
  public User createUser(User userToCreate)
      throws InvalidArgumentException, UserAlreadyExistException, NotAuthorizedException {
    internalKadaiEngine.getEngine().checkRoleMembership(KadaiRole.BUSINESS_ADMIN, KadaiRole.ADMIN);
    validateFields(userToCreate);
    standardCreateActions(userToCreate);
    insertIntoDatabase(userToCreate);
    ((UserImpl) userToCreate).setDomains(determineDomains(userToCreate));

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "Method createUser() created User '{}'.",
          LogSanitizer.stripLineBreakingChars(userToCreate));
    }
    return userToCreate;
  }

  @Override
  public User updateUser(User userToUpdate)
      throws UserNotFoundException, InvalidArgumentException, NotAuthorizedException {
    internalKadaiEngine.getEngine().checkRoleMembership(KadaiRole.BUSINESS_ADMIN, KadaiRole.ADMIN);
    validateFields(userToUpdate);
    standardUpdateActions(getUser(userToUpdate.getId()), userToUpdate);

    internalKadaiEngine.executeInDatabaseConnection(() -> userMapper.update(userToUpdate));
    internalKadaiEngine.executeInDatabaseConnection(
        () -> {
          userMapper.deleteGroups(userToUpdate.getId());
          userMapper.deletePermissions(userToUpdate.getId());
        });
    if (userToUpdate.getGroups() != null && !userToUpdate.getGroups().isEmpty()) {
      internalKadaiEngine.executeInDatabaseConnection(() -> userMapper.insertGroups(userToUpdate));
    }
    if (userToUpdate.getPermissions() != null && !userToUpdate.getPermissions().isEmpty()) {
      internalKadaiEngine.executeInDatabaseConnection(
          () -> userMapper.insertPermissions(userToUpdate));
    }
    ((UserImpl) userToUpdate).setDomains(determineDomains(userToUpdate));

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "Method updateUser() updated User '{}'.",
          LogSanitizer.stripLineBreakingChars(userToUpdate));
    }

    return userToUpdate;
  }

  @Override
  public void deleteUser(String id)
      throws UserNotFoundException, InvalidArgumentException, NotAuthorizedException {

    internalKadaiEngine.getEngine().checkRoleMembership(KadaiRole.BUSINESS_ADMIN, KadaiRole.ADMIN);

    User user = getUser(id);
    String userId = user.getId();

    internalKadaiEngine.executeInDatabaseConnection(
        () -> {
          userMapper.delete(userId);
          userMapper.deleteGroups(userId);
          userMapper.deletePermissions(userId);
        });
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Method deleteUser() deleted User with id '{}'.", userId);
    }
  }

  @Override
  public UserQuery createUserQuery() {
    return new UserQueryImpl(internalKadaiEngine);
  }

  public void deleteAllUsersGroupsPermissions() throws NotAuthorizedException {
    internalKadaiEngine.getEngine().checkRoleMembership(KadaiRole.BUSINESS_ADMIN, KadaiRole.ADMIN);
    internalKadaiEngine.executeInDatabaseConnection(
        () -> {
          userMapper.deleteAll();
          userMapper.deleteAllGroups();
          userMapper.deleteAllPermissions();
        });
  }

  /**
   * Normalizes the source outside the refresh transaction. Invalid individual source records are
   * excluded; duplicate canonical ids are a generation-wide error because choosing an arbitrary
   * LDAP result would make removals unsafe.
   *
   * @param sourceUsers postprocessed LDAP users
   * @return valid canonical users and source counts
   * @throws InvalidArgumentException if the source is null or contains duplicate canonical ids
   * @throws DuplicateUserRefreshIdException if two valid source entries have the same normalized id
   */
  public PreparedUserRefreshInput prepareUserRefresh(Collection<User> sourceUsers) {
    if (sourceUsers == null) {
      throw new InvalidArgumentException("User refresh source must not be null");
    }
    Map<String, UserRefreshState> users = new HashMap<>();
    int rejected = 0;
    for (User user : sourceUsers) {
      UserRefreshState state;
      try {
        state = toRefreshState(user);
      } catch (InvalidArgumentException e) {
        rejected++;
        continue;
      }
      if (users.putIfAbsent(state.id(), state) != null) {
        throw new DuplicateUserRefreshIdException(state.id());
      }
    }
    return new PreparedUserRefreshInput(
        Map.copyOf(users), sourceUsers.size(), users.size(), rejected);
  }

  /**
   * Reconciles a prepared authoritative source using one engine connection. The caller supplies the
   * outer transaction for scheduled execution; direct callers still get an engine connection scope.
   *
   * @param input prepared authoritative source
   * @param batchSize configured JDBC batch flush size
   * @return reconciliation counts
   * @throws NotAuthorizedException if the caller lacks the required role
   * @throws InvalidArgumentException if the batch size is invalid
   */
  public UserRefreshResult synchronizeUsers(PreparedUserRefreshInput input, int batchSize)
      throws NotAuthorizedException {
    if (batchSize <= 0) {
      throw new InvalidArgumentException("User refresh batch size must be positive");
    }
    if (input == null) {
      throw new InvalidArgumentException("User refresh input must not be null");
    }
    internalKadaiEngine.getEngine().checkRoleMembership(KadaiRole.BUSINESS_ADMIN, KadaiRole.ADMIN);
    return internalKadaiEngine.executeInDatabaseConnection(
        () -> synchronizeUsersInConnection(input, batchSize));
  }

  /**
   * Loads the current snapshot through exactly three flat mapper queries.
   *
   * @return the current database snapshot
   */
  UserDatabaseSnapshot loadUserDatabaseSnapshot() {
    return new UserRefreshSnapshotLoader(userMapper).load();
  }

  private UserRefreshResult synchronizeUsersInConnection(
      PreparedUserRefreshInput input, int batchSize) {
    Connection connection = internalKadaiEngine.getConnection();
    // makes direct execution rollback-safe without opening or committing another connection
    Savepoint savepoint = null;
    try {
      savepoint = connection.setSavepoint("KADAI_USER_REFRESH");
      UserDatabaseSnapshot current = loadUserDatabaseSnapshot();
      UserRefreshPlan plan = UserRefreshDiffCalculator.calculate(current, input.usersById());
      if (!plan.isEmpty()) {
        new UserRefreshBatchWriter(connection, batchSize).apply(plan);
        internalKadaiEngine.getSqlSession().clearCache();
      }
      connection.releaseSavepoint(savepoint);
      return new UserRefreshResult(
          input.inputUsers(),
          input.acceptedUsers(),
          input.rejectedUsers(),
          plan.usersToInsert().size(),
          plan.updatedUsers(),
          plan.userIdsToDelete().size(),
          plan.unchangedUsers(),
          plan.groupsToInsert().size(),
          plan.groupsToDelete().size(),
          plan.permissionsToInsert().size(),
          plan.permissionsToDelete().size(),
          plan.orphanGroupsToDelete().size(),
          plan.orphanPermissionsToDelete().size());
    } catch (RuntimeException | Error e) {
      rollbackToSavepoint(connection, savepoint, e);
      throw e;
    } catch (SQLException e) {
      rollbackToSavepoint(connection, savepoint, e);
      throw new io.kadai.common.api.exceptions.SystemException(
          "Could not synchronize the LDAP user snapshot", e);
    }
  }

  private static void rollbackToSavepoint(
      Connection connection, Savepoint savepoint, Throwable failure) {
    if (savepoint == null) {
      return;
    }
    try {
      connection.rollback(savepoint);
    } catch (SQLException rollbackFailure) {
      failure.addSuppressed(rollbackFailure);
    }
  }

  private static UserRefreshState toRefreshState(User user) {
    if (user == null
        || user.getId() == null
        || user.getId().isEmpty()
        || user.getFirstName() == null
        || user.getLastName() == null
        || user.getGroups() == null
        || user.getPermissions() == null) {
      throw new InvalidArgumentException("Invalid LDAP user refresh entry");
    }
    String id = user.getId().toLowerCase(Locale.ROOT);
    String fullName = user.getFullName();
    if (fullName == null || fullName.isEmpty()) {
      fullName = String.format("%s, %s", user.getLastName(), user.getFirstName());
    }
    String longName = user.getLongName();
    if (longName == null || longName.isEmpty()) {
      longName = String.format("%s - (%s)", fullName, id);
    }
    Set<String> groups = normalize(user.getGroups());
    Set<String> permissions = normalize(user.getPermissions());
    validatePersistedLengths(
        id,
        user.getFirstName(),
        user.getLastName(),
        fullName,
        longName,
        user.getEmail(),
        user.getPhone(),
        user.getMobilePhone(),
        user.getOrgLevel1(),
        user.getOrgLevel2(),
        user.getOrgLevel3(),
        user.getOrgLevel4(),
        groups,
        permissions);
    return new UserRefreshState(
        id,
        user.getFirstName(),
        user.getLastName(),
        fullName,
        longName,
        user.getEmail(),
        user.getPhone(),
        user.getMobilePhone(),
        user.getOrgLevel1(),
        user.getOrgLevel2(),
        user.getOrgLevel3(),
        user.getOrgLevel4(),
        user.getData(),
        groups,
        permissions);
  }

  private static Set<String> normalize(Set<String> values) {
    Set<String> result = new HashSet<>();
    for (String value : values) {
      if (value == null) {
        throw new InvalidArgumentException("Invalid LDAP membership");
      }
      result.add(value.toLowerCase(Locale.ROOT));
    }
    return Set.copyOf(result);
  }

  private static void validatePersistedLengths(
      String id,
      String firstName,
      String lastName,
      String fullName,
      String longName,
      String email,
      String phone,
      String mobilePhone,
      String orgLevel1,
      String orgLevel2,
      String orgLevel3,
      String orgLevel4,
      Set<String> groups,
      Set<String> permissions) {
    validateLength("user ID", id, 32);
    validateLength("first name", firstName, 32);
    validateLength("last name", lastName, 32);
    validateLength("full name", fullName, 64);
    validateLength("long name", longName, 64);
    validateLength("email", email, 64);
    validateLength("phone", phone, 32);
    validateLength("mobile phone", mobilePhone, 32);
    validateLength("org level 1", orgLevel1, 32);
    validateLength("org level 2", orgLevel2, 32);
    validateLength("org level 3", orgLevel3, 32);
    validateLength("org level 4", orgLevel4, 32);
    groups.forEach(group -> validateLength("group ID", group, 256));
    permissions.forEach(permission -> validateLength("permission ID", permission, 256));
  }

  private static void validateLength(String field, String value, int maximum) {
    if (value != null && value.length() > maximum) {
      throw new InvalidArgumentException(
          String.format(
              "Invalid LDAP user refresh entry: %s exceeds %d characters", field, maximum));
    }
  }

  Set<String> determineDomains(User user) {
    Set<String> accessIds = new HashSet<>(user.getGroups());
    accessIds.addAll(user.getPermissions());
    accessIds.add(user.getId());
    if (minimalWorkbasketPermissions != null && !minimalWorkbasketPermissions.isEmpty()) {
      // since WorkbasketService#accessIdsHavePermissions requires some role permissions we have to
      // execute this query as an admin. Since we're only determining the domains of a given user
      // (and any user can request information on any other user) this query is "harmless".
      return new HashSet<>(
          internalKadaiEngine
              .getEngine()
              .runAsAdmin(
                  wrapping(
                      () ->
                          workbasketService
                              .createWorkbasketQuery()
                              .accessIdsHavePermissions(
                                  minimalWorkbasketPermissions, accessIds.toArray(String[]::new))
                              .listValues(
                                  WorkbasketQueryColumnName.DOMAIN, SortDirection.ASCENDING))));
    }
    return Collections.emptySet();
  }

  private void insertIntoDatabase(User userToCreate) throws UserAlreadyExistException {
    try {
      internalKadaiEngine.openConnection();
      userMapper.insert(userToCreate);
      if (userToCreate.getGroups() != null && !userToCreate.getGroups().isEmpty()) {
        userMapper.insertGroups(userToCreate);
      }
      if (userToCreate.getPermissions() != null && !userToCreate.getPermissions().isEmpty()) {
        userMapper.insertPermissions(userToCreate);
      }
    } catch (PersistenceException e) {
      throw new UserAlreadyExistException(userToCreate.getId(), e);
    } finally {
      internalKadaiEngine.returnConnection();
    }
  }

  private void validateFields(User userToValidate) throws InvalidArgumentException {
    if (userToValidate.getId() == null || userToValidate.getId().isEmpty()) {
      throw new InvalidArgumentException(
          "UserId must not be empty when creating or updating User.");
    }
    if (userToValidate.getFirstName() == null || userToValidate.getLastName() == null) {
      throw new InvalidArgumentException("First and last name of User must be set or empty.");
    }
  }

  private void standardCreateActions(User user) {
    if (user.getFullName() == null || user.getFullName().isEmpty()) {
      user.setFullName(String.format("%s, %s", user.getLastName(), user.getFirstName()));
    }
    if (user.getLongName() == null || user.getLongName().isEmpty()) {
      user.setLongName(String.format("%s - (%s)", user.getFullName(), user.getId()));
    }
    user.setId(user.getId().toLowerCase());
    user.setGroups(
        user.getGroups().stream().map((String::toLowerCase)).collect(Collectors.toSet()));
    user.setPermissions(
        user.getPermissions().stream().map((String::toLowerCase)).collect(Collectors.toSet()));
  }

  private void standardUpdateActions(User oldUser, User newUser) {
    if (!newUser.getFirstName().equals(oldUser.getFirstName())
        || !newUser.getLastName().equals(oldUser.getLastName())) {
      if (newUser.getFullName() == null
          || newUser.getFullName().isEmpty()
          || newUser.getFullName().equals(oldUser.getFullName())) {
        newUser.setFullName(String.format("%s, %s", newUser.getLastName(), newUser.getFirstName()));
      }
      if (newUser.getLongName() == null
          || newUser.getLongName().isEmpty()
          || newUser.getLongName().equals(oldUser.getLongName())) {
        newUser.setLongName(String.format("%s - (%s)", newUser.getFullName(), newUser.getId()));
      }
    }
    newUser.setId(newUser.getId().toLowerCase());
    newUser.setGroups(
        newUser.getGroups().stream().map((String::toLowerCase)).collect(Collectors.toSet()));
    newUser.setPermissions(
        newUser.getPermissions().stream().map((String::toLowerCase)).collect(Collectors.toSet()));
  }

}
