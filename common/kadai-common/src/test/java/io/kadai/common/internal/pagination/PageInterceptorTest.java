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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.kadai.common.internal.configuration.DB;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PageInterceptorTest {

  private AutoCloseable closeable;
  private PageInterceptor pageInterceptor;

  @Mock private MappedStatement mappedStatement;
  @Mock private ResultHandler<?> resultHandler;

  @BeforeEach
  public void setup() {
    closeable = MockitoAnnotations.openMocks(this);
    pageInterceptor = new PageInterceptor();
  }

  @AfterEach
  public void tearDown() throws Exception {
    closeable.close();
  }

  @NullSource
  @ParameterizedTest
  @MethodSource("defaultRowBoundsSource")
  public void should_Proceed_When_RowBoundsIsIrrelevant(RowBounds rowBounds) throws Throwable {
    Object[] args = {mappedStatement, new Object(), rowBounds, resultHandler};

    Invocation invocation = mock(Invocation.class);
    when(invocation.getArgs()).thenReturn(args);

    pageInterceptor.intercept(invocation);

    verify(invocation).proceed();
  }

  @SuppressWarnings("rawtypes")
  @ParameterizedTest
  @CsvSource({
    "H2, SELECT * FROM FOO LIMIT 42 OFFSET 7",
    "DB2, SELECT * FROM FOO OFFSET 7 ROWS FETCH FIRST 42 ROWS ONLY",
    "POSTGRES, SELECT * FROM FOO LIMIT 42 OFFSET 7",
  })
  public void should_RewriteSql_When_SqlIsNotNativelyPaginatedAndValidRowBounds(
      DB db, String expectedSql) throws Throwable {
    Object parameter = new Object();
    RowBounds rowBounds = new RowBounds(7, 42);
    Object[] args = {mappedStatement, parameter, rowBounds, resultHandler};

    Invocation invocation = mock(Invocation.class);
    when(invocation.getArgs()).thenReturn(args);

    Executor executor = mock(Executor.class);
    when(invocation.getTarget()).thenReturn(executor);

    Configuration configuration = mock(Configuration.class);
    when(mappedStatement.getConfiguration()).thenReturn(configuration);
    when(configuration.getDatabaseId()).thenReturn(db.dbProductId);

    BoundSql boundSql = mock(BoundSql.class);
    when(mappedStatement.getBoundSql(any())).thenReturn(boundSql);
    when(boundSql.getSql()).thenReturn("SELECT * FROM FOO");
    List<ParameterMapping> parameterMappings = Collections.emptyList();
    when(boundSql.getParameterMappings()).thenReturn(parameterMappings);
    when(boundSql.getAdditionalParameters()).thenReturn(Collections.emptyMap());
    Object parameterObject = new Object();
    when(boundSql.getParameterObject()).thenReturn(parameterObject);

    pageInterceptor.intercept(invocation);

    BoundSql expected =
        new BoundSql(configuration, expectedSql, parameterMappings, parameterObject);

    ArgumentCaptor<MappedStatement> mappedStatementCaptor =
        ArgumentCaptor.forClass(MappedStatement.class);
    ArgumentCaptor<Object> parameterCaptor = ArgumentCaptor.forClass(Object.class);
    ArgumentCaptor<RowBounds> rowBoundsCaptor = ArgumentCaptor.forClass(RowBounds.class);
    ArgumentCaptor<ResultHandler> resultHandlerCaptor =
        ArgumentCaptor.forClass(ResultHandler.class);
    ArgumentCaptor<CacheKey> cacheKeyCaptor = ArgumentCaptor.forClass(CacheKey.class);
    ArgumentCaptor<BoundSql> boundSqlCaptor = ArgumentCaptor.forClass(BoundSql.class);

    verify(executor)
        .query(
            mappedStatementCaptor.capture(),
            parameterCaptor.capture(),
            rowBoundsCaptor.capture(),
            resultHandlerCaptor.capture(),
            cacheKeyCaptor.capture(),
            boundSqlCaptor.capture());

    assertThat(mappedStatementCaptor.getValue()).isEqualTo(mappedStatement);
    assertThat(parameterCaptor.getValue()).isEqualTo(parameter);
    assertThat(rowBoundsCaptor.getValue()).isEqualTo(RowBounds.DEFAULT);
    assertThat(resultHandlerCaptor.getValue()).isEqualTo(resultHandler);
    assertThat(cacheKeyCaptor.getValue()).isNull();
    assertThat(boundSqlCaptor.getValue()).usingRecursiveComparison().isEqualTo(expected);
  }

  @SuppressWarnings("rawtypes")
  @ParameterizedTest
  @CsvSource({
      "H2, SELECT * FROM FOO LIMIT 42 OFFSET 0, -1, 42",
      "H2, SELECT * FROM FOO LIMIT 0 OFFSET 42, 42, -1",
      "H2, SELECT * FROM FOO LIMIT 0 OFFSET 0, -2, -1",
      "DB2, SELECT * FROM FOO OFFSET 0 ROWS FETCH FIRST 42 ROWS ONLY, -1, 42",
      "DB2, SELECT * FROM FOO OFFSET 42 ROWS FETCH FIRST 0 ROWS ONLY, 42, -1",
      "DB2, SELECT * FROM FOO OFFSET 0 ROWS FETCH FIRST 0 ROWS ONLY, -2, -1",
      "POSTGRES, SELECT * FROM FOO LIMIT 42 OFFSET 0, -1, 42",
      "POSTGRES, SELECT * FROM FOO LIMIT 0 OFFSET 42, 42, -1",
      "POSTGRES, SELECT * FROM FOO LIMIT 0 OFFSET 0, -2, -1",
  })
  public void should_RewriteSqlWithZeroBound_When_SqlIsNotNativelyPaginatedAndNegativeRowBounds(
      DB db, String expectedSql, int offset, int limit) throws Throwable {
    Object parameter = new Object();
    RowBounds rowBounds = new RowBounds(offset, limit);
    Object[] args = {mappedStatement, parameter, rowBounds, resultHandler};

    Invocation invocation = mock(Invocation.class);
    when(invocation.getArgs()).thenReturn(args);

    Executor executor = mock(Executor.class);
    when(invocation.getTarget()).thenReturn(executor);

    Configuration configuration = mock(Configuration.class);
    when(mappedStatement.getConfiguration()).thenReturn(configuration);
    when(configuration.getDatabaseId()).thenReturn(db.dbProductId);

    BoundSql boundSql = mock(BoundSql.class);
    when(mappedStatement.getBoundSql(any())).thenReturn(boundSql);
    when(boundSql.getSql()).thenReturn("SELECT * FROM FOO");
    List<ParameterMapping> parameterMappings = Collections.emptyList();
    when(boundSql.getParameterMappings()).thenReturn(parameterMappings);
    when(boundSql.getAdditionalParameters()).thenReturn(Collections.emptyMap());
    Object parameterObject = new Object();
    when(boundSql.getParameterObject()).thenReturn(parameterObject);

    pageInterceptor.intercept(invocation);

    BoundSql expected =
        new BoundSql(configuration, expectedSql, parameterMappings, parameterObject);

    ArgumentCaptor<MappedStatement> mappedStatementCaptor =
        ArgumentCaptor.forClass(MappedStatement.class);
    ArgumentCaptor<Object> parameterCaptor = ArgumentCaptor.forClass(Object.class);
    ArgumentCaptor<RowBounds> rowBoundsCaptor = ArgumentCaptor.forClass(RowBounds.class);
    ArgumentCaptor<ResultHandler> resultHandlerCaptor =
        ArgumentCaptor.forClass(ResultHandler.class);
    ArgumentCaptor<CacheKey> cacheKeyCaptor = ArgumentCaptor.forClass(CacheKey.class);
    ArgumentCaptor<BoundSql> boundSqlCaptor = ArgumentCaptor.forClass(BoundSql.class);

    verify(executor)
        .query(
            mappedStatementCaptor.capture(),
            parameterCaptor.capture(),
            rowBoundsCaptor.capture(),
            resultHandlerCaptor.capture(),
            cacheKeyCaptor.capture(),
            boundSqlCaptor.capture());

    assertThat(mappedStatementCaptor.getValue()).isEqualTo(mappedStatement);
    assertThat(parameterCaptor.getValue()).isEqualTo(parameter);
    assertThat(rowBoundsCaptor.getValue()).isEqualTo(RowBounds.DEFAULT);
    assertThat(resultHandlerCaptor.getValue()).isEqualTo(resultHandler);
    assertThat(cacheKeyCaptor.getValue()).isNull();
    assertThat(boundSqlCaptor.getValue()).usingRecursiveComparison().isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
    "H2, SELECT * FROM FOO LIMIT 42 OFFSET 7",
    "DB2, SELECT * FROM FOO OFFSET 7 ROWS FETCH FIRST 42 ROWS ONLY",
    "POSTGRES, SELECT * FROM FOO LIMIT 42 OFFSET 7",
  })
  public void should_Proceed_When_SqlIsNativelyPaginatedAlready(DB db, String sql)
      throws Throwable {
    Object[] args = {mappedStatement, new Object(), new RowBounds(1, 5), resultHandler};

    Invocation invocation = mock(Invocation.class);
    when(invocation.getArgs()).thenReturn(args);

    Configuration configuration = mock(Configuration.class);
    when(mappedStatement.getConfiguration()).thenReturn(configuration);
    when(configuration.getDatabaseId()).thenReturn(db.dbProductId);

    BoundSql boundSql = mock(BoundSql.class);
    when(mappedStatement.getBoundSql(any())).thenReturn(boundSql);
    when(boundSql.getSql()).thenReturn(sql);

    pageInterceptor.intercept(invocation);

    verify(invocation).proceed();
  }

  private static Stream<Arguments> defaultRowBoundsSource() {
    return Stream.of(Arguments.of(RowBounds.DEFAULT));
  }
}
