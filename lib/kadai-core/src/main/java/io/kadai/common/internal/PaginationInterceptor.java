package io.kadai.common.internal;

import java.sql.Connection;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

@Intercepts({
  @Signature(
      type = StatementHandler.class,
      method = "prepare",
      args = {Connection.class, Integer.class})
})
public class PaginationInterceptor implements Interceptor {
  private static final ThreadLocal<Pagination> PAGINATION_THREAD_LOCAL = new ThreadLocal<>();

  public static void setPagination(int offset, int limit) {
    int safeLimit = Math.max(0, limit);
    int safeOffset = Math.max(0, offset);
    PAGINATION_THREAD_LOCAL.set(new Pagination(safeOffset, safeLimit));
  }

  public static void clearPagination() {
    PAGINATION_THREAD_LOCAL.remove();
  }

  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    if (PAGINATION_THREAD_LOCAL.get() == null) {
      return invocation.proceed();
    }

    StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
    MetaObject metaObject = SystemMetaObject.forObject(statementHandler);

    while (metaObject.hasGetter("delegate")) {
      statementHandler = (StatementHandler) metaObject.getValue("delegate");
      metaObject = SystemMetaObject.forObject(statementHandler);
    }

    BoundSql boundSql = statementHandler.getBoundSql();
    String originalSql = boundSql.getSql();

    if (originalSql.toLowerCase().contains("limit")
        || originalSql.toLowerCase().contains("offset")) {
      return invocation.proceed();
    }

    if (!metaObject.hasGetter("mappedStatement")) {
      throw new IllegalStateException("DatabaseId could not be determined.");
    }

    MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("mappedStatement");
    String databaseId = mappedStatement.getConfiguration().getDatabaseId();

    Pagination pagination = PAGINATION_THREAD_LOCAL.get();
    String paginatedSql = getPagninatedString(originalSql, databaseId, pagination);

    BoundSql newBoundSql =
        new BoundSql(
            mappedStatement.getConfiguration(),
            paginatedSql,
            boundSql.getParameterMappings(),
            boundSql.getParameterObject());

    metaObject.setValue("boundSql", newBoundSql);

    Object result = invocation.proceed();
    clearPagination();
    return result;
  }

  private static String getPagninatedString(
      String originalSql, String databaseId, Pagination pagination) {
    String paginatedSql;

    if (originalSql.toLowerCase().contains("order by")) {
      paginatedSql =
          originalSql
              .replaceAll("(?i)ORDER BY\\s+DUE\\s+DESC", "ORDER BY DUE DESC, ID ASC")
              .replaceAll("(?i)ORDER BY\\s+POR_TYPE\\s+ASC", "ORDER BY POR_TYPE ASC, ID ASC")
              .replaceAll("(?i)ORDER BY\\s+NAME\\s+ASC", "ORDER BY NAME ASC, ID ASC")
              .replaceAll("(?i)ORDER BY\\s+PRIORITY\\s+DESC", "ORDER BY PRIORITY DESC, ID ASC")
              .replaceAll("(?i)ORDER BY\\s+PLANNED\\s+ASC", "ORDER BY PLANNED ASC, ID ASC")
              .replaceAll("(?i)ORDER BY\\s+RECEIVED\\s+DESC", "ORDER BY RECEIVED DESC, ID ASC")
              .replaceAll("(?i)ORDER BY\\s+OWNER\\s+ASC", "ORDER BY OWNER ASC, ID ASC");
    } else {
      paginatedSql = originalSql;
    }

    if ("db2".equalsIgnoreCase(databaseId)) {
      paginatedSql +=
          " OFFSET " + pagination.offset + " ROWS FETCH FIRST " + pagination.limit + " ROWS ONLY";
    } else {
      paginatedSql += " LIMIT " + pagination.limit + " OFFSET " + pagination.offset;
    }
    return paginatedSql;
  }

  private static class Pagination {
    private final int offset;
    private final int limit;

    public Pagination(int offset, int limit) {
      this.offset = offset;
      this.limit = limit;
    }

    public int getOffset() {
      return offset;
    }

    public int getLimit() {
      return limit;
    }
  }
}
