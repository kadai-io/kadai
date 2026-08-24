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

package io.kadai.user.jobs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.ScheduledJob;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.internal.JobServiceImpl;
import io.kadai.common.internal.jobs.JobRunner;
import io.kadai.common.internal.jobs.PlainJavaTransactionProvider;
import io.kadai.common.internal.transaction.KadaiTransactionProvider;
import io.kadai.common.rest.ldap.LdapClient;
import io.kadai.common.rest.ldap.LdapUserSnapshot;
import io.kadai.rest.test.KadaiSpringBootTest;
import io.kadai.testapi.security.JaasExtension;
import io.kadai.testapi.security.WithAccessId;
import io.kadai.user.api.UserService;
import io.kadai.user.internal.UserServiceImpl;
import io.kadai.user.internal.models.UserImpl;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@KadaiSpringBootTest
@ExtendWith(JaasExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserInfoRefreshJobSchedulerAccTest {

  @Autowired KadaiEngine kadaiEngine;
  @Autowired UserService userService;
  @MockitoSpyBean LdapClient ldapClient;

  private JobServiceImpl jobService;
  private UserServiceImpl userServiceImpl;

  @BeforeEach
  @WithAccessId(user = "businessadmin")
  void setUp() throws Exception {
    reset(ldapClient);
    jobService = (JobServiceImpl) kadaiEngine.getJobService();
    userServiceImpl = (UserServiceImpl) userService;
    jobService.deleteJobs(UserInfoRefreshJob.class.getName());
    clearScheduledRefreshJobs();
    userServiceImpl.deleteAllUsersGroupsPermissions();
  }

  @AfterEach
  @WithAccessId(user = "businessadmin")
  void tearDown() throws Exception {
    jobService.deleteJobs(UserInfoRefreshJob.class.getName());
    clearScheduledRefreshJobs();
    userServiceImpl.deleteAllUsersGroupsPermissions();
    reset(ldapClient);
  }

  @Test
  @WithAccessId(user = "admin")
  void should_UseThreeTransactionsAndCreateOneSuccessor() throws Exception {
    doReturn(snapshot(user("scheduled-user"))).when(ldapClient).searchAllUsersInUserRole();
    createDueJob();
    CountingProvider provider = new CountingProvider(delegate());

    runJobs(provider);

    assertThat(provider.invocations()).isEqualTo(3);
    assertThat(userIds()).containsExactly("scheduled-user");
    assertThat(scheduledJobs()).hasSize(1);
  }

  @Test
  @WithAccessId(user = "admin")
  void should_NotFinalizeWhenCompleteLdapSnapshotCannotBeRead() throws Exception {
    doThrow(new SystemException("paging response control missing"))
        .when(ldapClient)
        .searchAllUsersInUserRole();
    final ScheduledJob original = createDueJob();
    CountingProvider provider = new CountingProvider(delegate());

    runJobs(provider);

    assertThat(provider.invocations()).isEqualTo(1);
    assertThat(scheduledJobs()).extracting(ScheduledJob::getJobId)
        .containsExactly(original.getJobId());
    assertThat(userIds()).isEmpty();
  }

  @Test
  @WithAccessId(user = "admin")
  void should_FenceBeforeWritingWhenOwnershipWasLostDuringLdapRead() throws Exception {
    userService.createUser(user("existing"));
    ScheduledJob original = createDueJob();
    doAnswer(
            ignored -> {
              stealJob(original.getJobId());
              return snapshot(user("existing"));
            })
        .when(ldapClient)
        .searchAllUsersInUserRole();
    CountingProvider provider = new CountingProvider(delegate());

    runJobs(provider);

    assertThat(provider.invocations()).isEqualTo(2);
    assertThat(userService.getUser("existing").getFirstName()).isEqualTo("First");
    assertThat(scheduledJobs()).extracting(ScheduledJob::getJobId)
        .containsExactly(original.getJobId());
  }

  private void runJobs(KadaiTransactionProvider provider) {
    JobRunner runner = new JobRunner(kadaiEngine);
    runner.registerTransactionProvider(provider);
    runner.runJobs();
  }

  private KadaiTransactionProvider delegate() {
    return new PlainJavaTransactionProvider(
        kadaiEngine, kadaiEngine.getConfiguration().getDataSource());
  }

  private ScheduledJob createDueJob() {
    ScheduledJob job = new ScheduledJob();
    job.setType(UserInfoRefreshJob.class.getName());
    job.setDue(Instant.now().minus(1, ChronoUnit.MINUTES));
    return jobService.createJob(job);
  }

  private void stealJob(int jobId) throws Exception {
    try (Connection connection = connection(); Statement statement = connection.createStatement()) {
      try (var prepared = connection.prepareStatement(
          "UPDATE SCHEDULED_JOB SET LOCKED_BY = ? WHERE JOB_ID = ?")) {
        prepared.setString(1, "another-owner");
        prepared.setInt(2, jobId);
        prepared.executeUpdate();
        connection.commit();
      }
    }
  }

  private void clearScheduledRefreshJobs() throws Exception {
    try (Connection connection = connection(); Statement statement = connection.createStatement()) {
      statement.executeUpdate(
          "DELETE FROM SCHEDULED_JOB WHERE TYPE = '" + UserInfoRefreshJob.class.getName() + "'");
      connection.commit();
    }
  }

  private List<ScheduledJob> scheduledJobs() throws Exception {
    try (Connection connection = connection();
        Statement statement = connection.createStatement();
        ResultSet resultSet =
            statement.executeQuery(
                "SELECT JOB_ID FROM SCHEDULED_JOB WHERE TYPE = '"
                    + UserInfoRefreshJob.class.getName()
                    + "' ORDER BY JOB_ID")) {
      List<ScheduledJob> jobs = new ArrayList<>();
      while (resultSet.next()) {
        ScheduledJob job = new ScheduledJob();
        job.setJobId(resultSet.getInt(1));
        jobs.add(job);
      }
      return jobs;
    }
  }

  private List<String> userIds() throws Exception {
    try (Connection connection = connection();
        Statement statement = connection.createStatement();
        ResultSet resultSet =
            statement.executeQuery("SELECT USER_ID FROM USER_INFO ORDER BY USER_ID")) {
      List<String> ids = new ArrayList<>();
      while (resultSet.next()) {
        ids.add(resultSet.getString(1));
      }
      return ids;
    }
  }

  private Connection connection() throws Exception {
    Connection connection =
        kadaiEngine.getConfiguration().getDataSource().getConnection();
    connection.setSchema(kadaiEngine.getConfiguration().getSchemaName());
    return connection;
  }

  private LdapUserSnapshot snapshot(UserImpl... users) {
    return new LdapUserSnapshot(List.of(users), 1, users.length);
  }

  private UserImpl user(String id) {
    UserImpl user = new UserImpl();
    user.setId(id);
    user.setFirstName("First");
    user.setLastName("Last");
    user.setFullName("First Last");
    user.setLongName("First Last - (" + id + ")");
    user.setGroups(java.util.Set.of("group"));
    user.setPermissions(java.util.Set.of("permission"));
    return user;
  }

  private static final class CountingProvider implements KadaiTransactionProvider {
    private final KadaiTransactionProvider delegate;
    private final AtomicInteger invocations = new AtomicInteger();

    private CountingProvider(KadaiTransactionProvider delegate) {
      this.delegate = delegate;
    }

    @Override
    public <T> T executeInTransaction(Supplier<T> supplier) {
      invocations.incrementAndGet();
      return delegate.executeInTransaction(supplier);
    }

    private int invocations() {
      return invocations.get();
    }
  }

}
