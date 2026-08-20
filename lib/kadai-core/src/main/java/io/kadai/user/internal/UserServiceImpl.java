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
   */
  public PreparedUserRefreshInput prepareUserRefresh(Collection<User> sourceUsers) {
    Map<String, UserRefreshState> users = new HashMap<>();
    int rejected = 0;
    for (User user : sourceUsers) {
      UserRefreshState state;
      try {
        state = toRefreshState(user);
      } catch (InvalidUserRefreshEntryException e) {
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
    internalKadaiEngine.getEngine().checkRoleMembership(KadaiRole.BUSINESS_ADMIN, KadaiRole.ADMIN);
    return internalKadaiEngine.executeInDatabaseConnection(
        () -> synchronizeUsersInConnection(input));
  }

  private UserRefreshResult synchronizeUsersInConnection(PreparedUserRefreshInput input) {
    Map<String, UserRefreshState> current = new HashMap<>();
    for (UserImpl user : userMapper.findAllUsersForRefresh()) {
      UserRefreshState state = toRefreshState(user);
      if (current.putIfAbsent(state.id(), state) != null) {
        throw new InvalidArgumentException("Duplicate user id in database refresh snapshot");
      }
    }
    Set<String> orphanGroupUsers = new HashSet<>();
    Set<String> orphanPermissionUsers = new HashSet<>();
    for (UserAccessIdRow row : userMapper.findAllGroupsForRefresh()) {
      UserRefreshState user = current.get(row.getUserId());
      if (user == null) {
        orphanGroupUsers.add(row.getUserId());
      } else {
        current.put(user.id(), withGroups(user, add(user.groups(), row.getAccessId())));
      }
    }
    for (UserAccessIdRow row : userMapper.findAllPermissionsForRefresh()) {
      UserRefreshState user = current.get(row.getUserId());
      if (user == null) {
        orphanPermissionUsers.add(row.getUserId());
      } else {
        current.put(user.id(), withPermissions(user, add(user.permissions(), row.getAccessId())));
      }
    }

    int inserted = 0;
    int updated = 0;
    int removed = 0;
    int unchanged = 0;
    int groupAdds = 0;
    int groupRemoves = 0;
    int permissionAdds = 0;
    int permissionRemoves = 0;
    Map<String, UserRefreshState> remaining = new HashMap<>(current);
    for (UserRefreshState desired : input.usersById().values()) {
      UserRefreshState existing = remaining.remove(desired.id());
      if (existing == null) {
        // A pre-existing orphan for this id would otherwise conflict with the membership PK.
        userMapper.deleteGroups(desired.id());
        userMapper.deletePermissions(desired.id());
        insertRefreshState(desired);
        inserted++;
        groupAdds += desired.groups().size();
        permissionAdds += desired.permissions().size();
      } else {
        boolean scalarChanged = !desired.hasSameScalars(existing);
        boolean groupsChanged = !desired.groups().equals(existing.groups());
        boolean permissionsChanged = !desired.permissions().equals(existing.permissions());
        if (!scalarChanged && !groupsChanged && !permissionsChanged) {
          unchanged++;
          continue;
        }
        if (scalarChanged) {
          userMapper.update(asUser(desired));
          updated++;
        }
        if (groupsChanged) {
          groupAdds += difference(desired.groups(), existing.groups()).size();
          groupRemoves += difference(existing.groups(), desired.groups()).size();
          userMapper.deleteGroups(desired.id());
          if (!desired.groups().isEmpty()) {
            userMapper.insertGroups(asUser(desired));
          }
        }
        if (permissionsChanged) {
          permissionAdds += difference(desired.permissions(), existing.permissions()).size();
          permissionRemoves += difference(existing.permissions(), desired.permissions()).size();
          userMapper.deletePermissions(desired.id());
          if (!desired.permissions().isEmpty()) {
            userMapper.insertPermissions(asUser(desired));
          }
        }
      }
    }
    for (UserRefreshState stale : remaining.values()) {
      userMapper.deleteGroups(stale.id());
      userMapper.deletePermissions(stale.id());
      userMapper.delete(stale.id());
      removed++;
      groupRemoves += stale.groups().size();
      permissionRemoves += stale.permissions().size();
    }
    orphanGroupUsers.forEach(userMapper::deleteGroups);
    orphanPermissionUsers.forEach(userMapper::deletePermissions);
    internalKadaiEngine.getSqlSession().clearCache();
    return new UserRefreshResult(
        input.inputUsers(),
        input.acceptedUsers(),
        input.rejectedUsers(),
        inserted,
        updated,
        removed,
        unchanged,
        groupAdds,
        groupRemoves,
        permissionAdds,
        permissionRemoves,
        orphanGroupUsers.size(),
        orphanPermissionUsers.size());
  }

  private void insertRefreshState(UserRefreshState state) {
    User user = asUser(state);
    userMapper.insert(user);
    if (!state.groups().isEmpty()) {
      userMapper.insertGroups(user);
    }
    if (!state.permissions().isEmpty()) {
      userMapper.insertPermissions(user);
    }
  }

  private static Set<String> add(Set<String> values, String value) {
    Set<String> result = new HashSet<>(values);
    result.add(value);
    return result;
  }

  private static Set<String> difference(Set<String> left, Set<String> right) {
    Set<String> result = new HashSet<>(left);
    result.removeAll(right);
    return result;
  }

  private static UserRefreshState withGroups(UserRefreshState user, Set<String> groups) {
    return new UserRefreshState(
        user.id(),
        user.firstName(),
        user.lastName(),
        user.fullName(),
        user.longName(),
        user.email(),
        user.phone(),
        user.mobilePhone(),
        user.orgLevel1(),
        user.orgLevel2(),
        user.orgLevel3(),
        user.orgLevel4(),
        user.data(),
        groups,
        user.permissions());
  }

  private static UserRefreshState withPermissions(UserRefreshState user, Set<String> permissions) {
    return new UserRefreshState(
        user.id(),
        user.firstName(),
        user.lastName(),
        user.fullName(),
        user.longName(),
        user.email(),
        user.phone(),
        user.mobilePhone(),
        user.orgLevel1(),
        user.orgLevel2(),
        user.orgLevel3(),
        user.orgLevel4(),
        user.data(),
        user.groups(),
        permissions);
  }

  private static UserRefreshState toRefreshState(User user) {
    if (user == null
        || user.getId() == null
        || user.getId().isEmpty()
        || user.getFirstName() == null
        || user.getLastName() == null
        || user.getGroups() == null
        || user.getPermissions() == null) {
      throw new InvalidUserRefreshEntryException("Invalid LDAP user refresh entry");
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
        normalize(user.getGroups()),
        normalize(user.getPermissions()));
  }

  private static Set<String> normalize(Set<String> values) {
    Set<String> result = new HashSet<>();
    for (String value : values) {
      if (value == null) {
        throw new InvalidUserRefreshEntryException("Invalid LDAP membership");
      }
      result.add(value.toLowerCase(Locale.ROOT));
    }
    return Set.copyOf(result);
  }

  private static User asUser(UserRefreshState state) {
    UserImpl user = new UserImpl();
    user.setId(state.id());
    user.setFirstName(state.firstName());
    user.setLastName(state.lastName());
    user.setFullName(state.fullName());
    user.setLongName(state.longName());
    user.setEmail(state.email());
    user.setPhone(state.phone());
    user.setMobilePhone(state.mobilePhone());
    user.setOrgLevel1(state.orgLevel1());
    user.setOrgLevel2(state.orgLevel2());
    user.setOrgLevel3(state.orgLevel3());
    user.setOrgLevel4(state.orgLevel4());
    user.setData(state.data());
    user.setGroups(state.groups());
    user.setPermissions(state.permissions());
    return user;
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

  private static final class DuplicateUserRefreshIdException extends InvalidArgumentException {
    private DuplicateUserRefreshIdException(String userId) {
      super(String.format("Duplicate normalized user id in LDAP refresh: %s", userId));
    }
  }

  private static final class InvalidUserRefreshEntryException extends InvalidArgumentException {
    private InvalidUserRefreshEntryException(String message) {
      super(message);
    }
  }
}
