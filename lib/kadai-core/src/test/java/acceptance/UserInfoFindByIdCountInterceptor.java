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

package acceptance;

import java.sql.Statement;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

@Intercepts({
  @Signature(
      type = StatementHandler.class,
      method = "parameterize",
      args = {Statement.class})
})
public class UserInfoFindByIdCountInterceptor implements Interceptor {

  private static final AtomicInteger USER_INFO_FIND_BY_ID_COUNT = new AtomicInteger();

  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    final StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
    final BoundSql boundSql = statementHandler.getBoundSql();
    String sql = boundSql.getSql().toUpperCase(Locale.ENGLISH);

    if (sql.contains("FROM USER_INFO") && sql.contains("WHERE USER_ID")) {
      USER_INFO_FIND_BY_ID_COUNT.incrementAndGet();
    }

    return invocation.proceed();
  }

  public static int getUserInfoFindByIdCount() {
    return USER_INFO_FIND_BY_ID_COUNT.get();
  }

  public static void resetUserInfoFindByIdCount() {
    USER_INFO_FIND_BY_ID_COUNT.set(0);
  }
}
