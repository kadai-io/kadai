package acceptance.classification.query;

import static org.assertj.core.api.Assertions.assertThat;

import acceptance.AbstractAccTest;
import acceptance.ParameterizedQuerySqlCaptureInterceptor;
import io.kadai.common.internal.KadaiEngineImpl;
import java.lang.reflect.Field;
import org.apache.ibatis.session.SqlSessionManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class QueryClassificationsWithPaginationAccTest extends AbstractAccTest {

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class PhysicalPagination {

    @BeforeAll
    void setup() throws Exception {
      Field sessionManagerField = KadaiEngineImpl.class.getDeclaredField("sessionManager");
      sessionManagerField.setAccessible(true);
      SqlSessionManager sessionManager = (SqlSessionManager) sessionManagerField.get(kadaiEngine);
      sessionManager
          .getConfiguration()
          .addInterceptor(new ParameterizedQuerySqlCaptureInterceptor());
    }

    @ParameterizedTest
    @CsvSource({"0,10", "5,10", "0,0", "2,4"})
    void should_UseNativeSql_For_QueryPagination(int offset, int limit) {
      ParameterizedQuerySqlCaptureInterceptor.resetCapturedSql();
      kadaiEngine.getClassificationService().createClassificationQuery().list(offset, limit);
      final String sql = ParameterizedQuerySqlCaptureInterceptor.getCapturedSql();
      final String physicalPattern1 = String.format("LIMIT %d OFFSET %d", limit, offset);
      final String physicalPattern2 =
          String.format("OFFSET %d ROWS FETCH FIRST %d ROWS ONLY", offset, limit);

      assertThat(sql).containsAnyOf(physicalPattern1, physicalPattern2);
    }
  }
}
