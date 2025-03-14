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
    capturedSql = boundSql.getSql();

    return invocation.proceed();
  }

  public static String getCapturedSql() {
    return capturedSql;
  }
}
