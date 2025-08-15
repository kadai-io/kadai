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

package io.kadai.user.jobs;

import io.kadai.KadaiConfiguration;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.ScheduledJob;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.internal.jobs.AbstractKadaiJob;
import io.kadai.common.internal.transaction.KadaiTransactionProvider;
import io.kadai.common.rest.ldap.LdapClient;
import io.kadai.common.rest.util.ApplicationContextProvider;
import io.kadai.spi.user.internal.RefreshUserPostprocessorManager;
import io.kadai.user.api.exceptions.UserAlreadyExistException;
import io.kadai.user.api.exceptions.UserNotFoundException;
import io.kadai.user.api.models.User;
import io.kadai.user.internal.UserServiceImpl;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Job to refresh all user info after a period of time. */
public class UserInfoRefreshJob extends AbstractKadaiJob {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserInfoRefreshJob.class);
  private final RefreshUserPostprocessorManager refreshUserPostprocessorManager;

  public UserInfoRefreshJob(KadaiEngine kadaiEngine) {
    this(kadaiEngine, null, null);
  }

  public UserInfoRefreshJob(
      KadaiEngine kadaiEngine, KadaiTransactionProvider txProvider, ScheduledJob scheduledJob) {
    super(kadaiEngine, txProvider, scheduledJob, true);
    runEvery = kadaiEngine.getConfiguration().getUserRefreshJobRunEvery();
    firstRun = kadaiEngine.getConfiguration().getUserRefreshJobFirstRun();
    refreshUserPostprocessorManager = new RefreshUserPostprocessorManager();
  }

  public static Duration getLockExpirationPeriod(KadaiConfiguration kadaiConfiguration) {
    return kadaiConfiguration.getUserRefreshJobLockExpirationPeriod();
  }

  @Override
  protected String getType() {
    return UserInfoRefreshJob.class.getName();
  }

  @Override
  protected void execute() {
    LOGGER.info("Running job to refresh all user info");
    try {
      final LdapClient ldapClient =
          ApplicationContextProvider.getApplicationContext()
              .getBean("ldapClient", LdapClient.class);
      List<User> users = ldapClient.searchUsersInUserRole();
      clearExistingUsersAndGroupsAndPermissions();
      users.forEach(this::executeUser);
      LOGGER.info("Job to refresh all user info has finished.");
    } catch (Exception e) {
      throw new SystemException("Error while processing UserRefreshJob.", e);
    }
  }

  private void clearExistingUsersAndGroupsAndPermissions() throws NotAuthorizedException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Trying to delete all users, groups and permissions");
    }

    final UserServiceImpl userServiceImpl = (UserServiceImpl) kadaiEngineImpl.getUserService();
    userServiceImpl.deleteAllUsersGroupsPermissions();

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Successfully deleted all users, groups and permissions");
    }
  }

  private void executeUser(User user) {
    try {
      final User userAfterProcessing =
          refreshUserPostprocessorManager.processUserAfterRefresh(user);
      addExistingConfigurationDataToUser(userAfterProcessing);
      insertNewUser(userAfterProcessing);
    } catch (Exception e) {
      LOGGER.error("Failed refreshing user {}", user, e);
    }
  }

  private void insertNewUser(User user) {
    try {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Trying to insert user {}", user);
      }
      kadaiEngineImpl.getUserService().createUser(user);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Successfully inserted user {}", user);
      }
    } catch (InvalidArgumentException | NotAuthorizedException | UserAlreadyExistException e) {
      throw new SystemException("Caught Exception while trying to insert new User", e);
    }
  }

  private void addExistingConfigurationDataToUser(User user) {
    try {

      String userData = kadaiEngineImpl.getUserService().getUser(user.getId()).getData();
      if (userData != null) {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Trying to set userData {} for user {}", userData, user);
        }
        user.setData(userData);
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Successfully set userData {} for user {}", userData, user);
        }
      }
    } catch (UserNotFoundException e) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(
            String.format(
                "Failed to fetch configuration data for User "
                    + "with ID '%s' because it doesn't exist",
                user.getId()));
      }
    } catch (InvalidArgumentException e) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Failed to fetch configuration data because userId was NULL or empty");
      }
    }
  }
}
