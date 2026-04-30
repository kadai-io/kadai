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

package io.kadai.common.internal.pagination;

import io.kadai.common.internal.configuration.DB;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * MyBatis-Interceptor transforming logical pagination with {@link RowBounds} to physical pagination
 * by rewriting the internal {@linkplain BoundSql#getSql() BoundSql}.
 */
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
      }),
})
public class PageInterceptor implements Interceptor {

  @Override
  @SuppressWarnings("rawtypes")
  public Object intercept(Invocation invocation) throws Throwable {
    final Object[] args = invocation.getArgs();
    final RowBounds rowBounds = (RowBounds) args[2];
    if (rowBounds == null || rowBounds == RowBounds.DEFAULT) {
      return invocation.proceed();
    }

    final MappedStatement ms = (MappedStatement) args[0];
    final Object parameter = args[1];
    final BoundSql oldBoundSql = ms.getBoundSql(parameter);
    final String oldSql = oldBoundSql.getSql();
    final PageDialect pageDialect = DB.getDB(ms.getConfiguration().getDatabaseId()).pageDialect;

    if (pageDialect.isPaginated(oldSql)) {
      return invocation.proceed();
    }

    final BoundSql newBoundSql =
        new BoundSql(
            ms.getConfiguration(),
            pageDialect.transform(oldSql, rowBounds),
            oldBoundSql.getParameterMappings(),
            oldBoundSql.getParameterObject());
    oldBoundSql.getAdditionalParameters().forEach(newBoundSql::setAdditionalParameter);

    final Executor executor = (Executor) invocation.getTarget();
    final CacheKey cacheKey =
        args.length > 4 && args[4] != null
            ? (CacheKey) args[4]
            : executor.createCacheKey(ms, parameter, RowBounds.DEFAULT, newBoundSql);

    return executor.query(
        ms, parameter, RowBounds.DEFAULT, (ResultHandler) args[3], cacheKey, newBoundSql);
  }
}
