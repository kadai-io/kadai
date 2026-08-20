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

package io.kadai.user.jobs;

import io.kadai.KadaiConfiguration;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.ScheduledJob;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.internal.JobServiceImpl;
import io.kadai.common.internal.jobs.AbstractKadaiJob;
import io.kadai.common.internal.jobs.JobTransactionPolicy;
import io.kadai.common.internal.transaction.KadaiTransactionProvider;
import io.kadai.common.internal.util.LogSanitizer;
import io.kadai.common.rest.ldap.LdapClient;
import io.kadai.common.rest.ldap.LdapUserSnapshot;
import io.kadai.common.rest.util.ApplicationContextProvider;
import io.kadai.spi.user.internal.RefreshUserPostprocessorManager;
import io.kadai.user.api.models.User;
import io.kadai.user.internal.PreparedUserRefreshInput;
import io.kadai.user.internal.UserRefreshResult;
import io.kadai.user.internal.UserServiceImpl;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Job to refresh all user info after a period of time. */
public class UserInfoRefreshJob extends AbstractKadaiJob {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserInfoRefreshJob.class);
  private final RefreshUserPostprocessorManager refreshUserPostprocessorManager;
  private final int batchSize;

  public UserInfoRefreshJob(KadaiEngine kadaiEngine) {
    this(kadaiEngine, null, null);
  }

  public UserInfoRefreshJob(
      KadaiEngine kadaiEngine, KadaiTransactionProvider txProvider, ScheduledJob scheduledJob) {
    super(kadaiEngine, txProvider, scheduledJob, true);
    runEvery = kadaiEngine.getConfiguration().getUserRefreshJobRunEvery();
    firstRun = kadaiEngine.getConfiguration().getUserRefreshJobFirstRun();
    refreshUserPostprocessorManager = new RefreshUserPostprocessorManager();
    batchSize = kadaiEngine.getConfiguration().getUserRefreshJobBatchSize();
  }

  public static Duration getLockExpirationPeriod(KadaiConfiguration kadaiConfiguration) {
    return kadaiConfiguration.getUserRefreshJobLockExpirationPeriod();
  }

  @Override
  protected String getType() {
    return UserInfoRefreshJob.class.getName();
  }

  @Override
  public JobTransactionPolicy getTransactionPolicy() {
    return JobTransactionPolicy.JOB_MANAGED;
  }

  @Override
  protected void execute() {
    long startedAt = System.nanoTime();
    LOGGER.info("Running differential job to refresh user info");
    try {
      final LdapClient ldapClient =
          ApplicationContextProvider.getApplicationContext()
              .getBean("ldapClient", LdapClient.class);
      LdapUserSnapshot snapshot = ldapClient.searchAllUsersInUserRole();
      List<User> processedUsers = new ArrayList<>(snapshot.users().size());
      int postprocessorFailures = 0;
      for (User user : snapshot.users()) {
        try {
          User processed = refreshUserPostprocessorManager.processUserAfterRefresh(user);
          if (processed == null) {
            throw new IllegalArgumentException("Refresh user postprocessor returned null");
          }
          processedUsers.add(processed);
        } catch (Exception e) {
          postprocessorFailures++;
          LOGGER.error(
              "Failed to prepare LDAP user '{}' for refresh",
              LogSanitizer.stripLineBreakingChars(user == null ? null : user.getId()),
              e);
        }
      }
      UserServiceImpl userService = (UserServiceImpl) kadaiEngineImpl.getUserService();
      PreparedUserRefreshInput input = userService.prepareUserRefresh(processedUsers);
      UserRefreshResult result = synchronizeTransactionally(userService, input);
      LOGGER.info(
          String.format(
              "Finished user refresh: ldapUsers=%d, pages=%d, accepted=%d, rejected=%d, "
                  + "inserted=%d, updated=%d, removed=%d, unchanged=%d, groupAdds=%d, "
                  + "groupRemoves=%d, permissionAdds=%d, permissionRemoves=%d, totalMs=%d",
              snapshot.resultCount(),
              snapshot.pageCount(),
              result.acceptedUsers(),
              result.rejectedUsers() + postprocessorFailures,
              result.insertedUsers(),
              result.updatedUsers(),
              result.removedUsers(),
              result.unchangedUsers(),
              result.addedGroups(),
              result.removedGroups(),
              result.addedPermissions(),
              result.removedPermissions(),
              Duration.ofNanos(System.nanoTime() - startedAt).toMillis()));
    } catch (Exception e) {
      throw new SystemException("Error while processing UserInfoRefreshJob.", e);
    }
  }

  private UserRefreshResult synchronizeTransactionally(
      UserServiceImpl userService, PreparedUserRefreshInput input) {
    return KadaiTransactionProvider.executeInTransactionIfPossible(
        txProvider,
        () -> {
          renewLockOrFail();
          UserRefreshResult result;
          try {
            result = userService.synchronizeUsers(input, batchSize);
          } catch (Exception e) {
            throw new SystemException("Could not synchronize LDAP user snapshot", e);
          }
          renewLockOrFail();
          return result;
        });
  }

  private void renewLockOrFail() {
    if (scheduledJob == null) {
      return;
    }
    boolean renewed =
        ((JobServiceImpl) kadaiEngineImpl.getJobService())
            .renewLock(
                scheduledJob,
                kadaiEngineImpl.getConfiguration().getUserRefreshJobLockExpirationPeriod());
    if (!renewed) {
      throw new SystemException(
          "User info refresh job lock was lost. Stopping refresh to avoid concurrent processing.");
    }
  }
}
