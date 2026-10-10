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

package acceptance.user.query;

import static org.assertj.core.api.Assertions.assertThat;

import acceptance.AbstractAccTest;
import io.kadai.common.internal.KadaiEngineImpl;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.user.api.UserQueryColumnName;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JaasExtension.class)
class UserNPlusOneAccTest extends AbstractAccTest {

  private QueryCountingInterceptor queryCounter;

  @BeforeEach
  void installQueryCounter() throws Exception {
    queryCounter = new QueryCountingInterceptor();

    Field sessionManagerField = KadaiEngineImpl.class.getDeclaredField("sessionManager");
    sessionManagerField.setAccessible(true);

    SqlSessionManager sqlSessionManager =
        (SqlSessionManager) sessionManagerField.get(kadaiEngine);
    sqlSessionManager.getConfiguration().addInterceptor(queryCounter);
  }

  @Test
  void should_CountNestedMybatisQueries() throws Exception {
    queryCounter.reset();

    kadaiEngine.getUserService().getUser("user-1-1");

    assertThat(queryCounter.snapshot().statementIds())
        .contains(
            "io.kadai.user.internal.UserMapper.findGroupsById",
            "io.kadai.user.internal.UserMapper.findPermissionsById");
  }

  @Test
  void should_UseConstantNumberOfQueries_When_GettingUsersByIds() throws Exception {
    List<String> userIds =
        kadaiEngine
            .getUserService()
            .createUserQuery()
            .listValues(UserQueryColumnName.USER_ID, null);

    assertThat(userIds).hasSizeGreaterThanOrEqualTo(10);

    queryCounter.reset();
    kadaiEngine.getUserService().getUsers(Set.of(userIds.get(0)));
    QueryStats oneUserStats = queryCounter.snapshot();

    queryCounter.reset();
    kadaiEngine
        .getUserService()
        .getUsers(new LinkedHashSet<>(userIds.subList(0, 10)));
    QueryStats tenUserStats = queryCounter.snapshot();

    assertThat(tenUserStats.count())
        .withFailMessage(
            "Query count must not grow with the number of users.%n"
                + "1 user: %s%n"
                + "10 users: %s",
            oneUserStats,
            tenUserStats)
        .isEqualTo(oneUserStats.count())
        .isLessThanOrEqualTo(4);
    assertThat(tenUserStats.statementIds())
        .containsExactly(
            "io.kadai.user.internal.UserMapper.findByIds",
            "io.kadai.user.internal.UserMapper.findGroupsByIds",
            "io.kadai.user.internal.UserMapper.findPermissionsByIds",
            "io.kadai.user.internal.UserMapper.findDomainsByIds");
  }

  @Test
  void should_UseConstantNumberOfQueries_When_QueryingUsers() {
    List<String> userIds =
        kadaiEngine
            .getUserService()
            .createUserQuery()
            .listValues(UserQueryColumnName.USER_ID, null);

    assertThat(userIds).hasSizeGreaterThanOrEqualTo(10);

    queryCounter.reset();
    kadaiEngine.getUserService().createUserQuery().idIn(userIds.get(0)).list();
    QueryStats oneUserStats = queryCounter.snapshot();

    queryCounter.reset();
    String[] tenUserIds = userIds.subList(0, 10).toArray(String[]::new);
    kadaiEngine.getUserService().createUserQuery().idIn(tenUserIds).list();
    QueryStats tenUserStats = queryCounter.snapshot();

    assertThat(tenUserStats.count())
        .withFailMessage(
            "Query count must not grow with the number of users.%n"
                + "1 user: %s%n"
                + "10 users: %s",
            oneUserStats,
            tenUserStats)
        .isEqualTo(oneUserStats.count())
        .isLessThanOrEqualTo(4);
    assertThat(tenUserStats.statementIds())
        .containsExactly(
            "io.kadai.user.internal.UserQueryMapper.queryUsers",
            "io.kadai.user.internal.UserMapper.findGroupsByIds",
            "io.kadai.user.internal.UserMapper.findPermissionsByIds",
            "io.kadai.user.internal.UserMapper.findDomainsByIds");
  }

  @Test
  void should_NotRunEnrichmentQueries_When_NoUsersAreFound() {
    queryCounter.reset();

    List<?> users =
        kadaiEngine.getUserService().createUserQuery().idIn("definitely-not-existing").list();

    assertThat(users).isEmpty();
    assertThat(queryCounter.snapshot().statementIds())
        .containsExactly("io.kadai.user.internal.UserQueryMapper.queryUsers");
  }

  @Test
  void should_NotRunEnrichmentQueries_When_NoUsersAreFoundByIds() {
    queryCounter.reset();

    List<?> users = kadaiEngine.getUserService().getUsers(Set.of("definitely-not-existing"));

    assertThat(users).isEmpty();
    assertThat(queryCounter.snapshot().statementIds())
        .containsExactly("io.kadai.user.internal.UserMapper.findByIds");
  }

  private record QueryStats(int count, List<String> statementIds) {}

  @Intercepts({
    @Signature(
        type = Executor.class,
        method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
    @Signature(
        type = Executor.class,
        method = "query",
        args = {
          MappedStatement.class,
          Object.class,
          RowBounds.class,
          ResultHandler.class,
          CacheKey.class,
          BoundSql.class
        })
  })
  private static class QueryCountingInterceptor implements Interceptor {

    private final ArrayList<String> statementIds = new ArrayList<>();

    @Override
    public Object plugin(Object target) {
      Object plugin = Interceptor.super.plugin(target);
      // CachingExecutor gives ResultLoader its unproxied wrapper. Route that wrapper through this
      // proxy so nested six-argument queries are counted as well.
      Executor executor = unwrapCachingExecutor(target);
      if (executor != null) {
        try {
          Field delegateField = CachingExecutor.class.getDeclaredField("delegate");
          delegateField.setAccessible(true);
          ((Executor) delegateField.get(executor)).setExecutorWrapper((Executor) plugin);
        } catch (ReflectiveOperationException e) {
          throw new IllegalStateException("Could not instrument the nested-query executor", e);
        }
      }
      return plugin;
    }

    private static Executor unwrapCachingExecutor(Object target) {
      Object current = target;
      try {
        while (Proxy.isProxyClass(current.getClass())) {
          InvocationHandler invocationHandler = Proxy.getInvocationHandler(current);
          if (!(invocationHandler instanceof Plugin)) {
            return null;
          }
          Field targetField = Plugin.class.getDeclaredField("target");
          targetField.setAccessible(true);
          current = targetField.get(invocationHandler);
        }
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException("Could not inspect the MyBatis executor proxy", e);
      }
      return current instanceof CachingExecutor ? (Executor) current : null;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
      MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
      statementIds.add(mappedStatement.getId());
      return invocation.proceed();
    }

    void reset() {
      statementIds.clear();
    }

    QueryStats snapshot() {
      return new QueryStats(statementIds.size(), List.copyOf(statementIds));
    }
  }
}
