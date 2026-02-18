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
public class ParameterizedQuerySqlCaptureInterceptor implements Interceptor {

  private static String capturedSql;

  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    final StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
    final BoundSql boundSql = statementHandler.getBoundSql();

    if (capturedSql == null) {
      capturedSql = boundSql.getSql();
    }

    return invocation.proceed();
  }

  public static String getCapturedSql() {
    return capturedSql;
  }

  public static void resetCapturedSql() {
    capturedSql = null;
  }
}
