package acceptance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import io.kadai.common.internal.PaginationInterceptor;
import io.kadai.common.test.security.JaasExtension;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.reflection.MetaObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@ExtendWith(JaasExtension.class)
class PaginationInterceptorTest {

  private PaginationInterceptor paginationInterceptor;

  @Mock private Invocation invocation;

  @Mock private StatementHandler statementHandler;

  @Mock private BoundSql boundSql;

  @Mock private MetaObject metaObject;

  @BeforeEach
  void setUp() throws Throwable {
    MockitoAnnotations.openMocks(this);
    paginationInterceptor = new PaginationInterceptor();

    when(statementHandler.getBoundSql()).thenReturn(boundSql);
    when(invocation.getTarget()).thenReturn(statementHandler);
    when(invocation.proceed()).thenReturn(null);
  }

  @Test
  void should_Proceed_When_LimitOrOffsetIsAlreadySet() throws Throwable {

    String originalSql = "SELECT * FROM TASK ORDER BY DUE ASC LIMIT 10 OFFSET 5";

    when(boundSql.getSql()).thenReturn(originalSql);
    when(invocation.proceed()).thenReturn(originalSql);

    Object result = paginationInterceptor.intercept(invocation);

    assertEquals(originalSql, result);
  }

  @Test
  void should_ThrowException_When_MappedStatementIsMissing() throws Throwable {
    String originalSql = "SELECT * FROM TASK ORDER BY DUE DESC";

    PaginationInterceptor.setPagination(5, 10);

    when(boundSql.getSql()).thenReturn(originalSql);
    when(invocation.proceed()).thenReturn(null);
    when(statementHandler.getBoundSql()).thenReturn(boundSql);
    when(statementHandler.getParameterHandler()).thenReturn(null);
    when(metaObject.getValue("mappedStatement")).thenReturn(null);

    Exception exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              paginationInterceptor.intercept(invocation);
            });

    assertEquals("DatabaseId could not be determined.", exception.getMessage());
  }
}
