package acceptance.task;

import static org.assertj.core.api.Assertions.assertThat;

import acceptance.AbstractAccTest;
import acceptance.ParameterizedQuerySqlCaptureInterceptor;
import io.kadai.task.internal.TaskMapper;
import io.kadai.task.internal.models.TaskImpl;
import java.time.Instant;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TaskMapperCacheTest extends AbstractAccTest {

  private SqlSessionManager sessionManager;
  private Cache cache;

  @BeforeEach
  public void setUp() throws Exception {
    this.sessionManager = getSessionManager(kadaiEngine);
    this.cache = sessionManager.getConfiguration().getCache(TaskMapper.class.getName());

    assertThat(cache.getSize()).isZero();
  }

  @AfterEach
  public void tearDown() {
    this.cache.clear();
  }

  @Test
  public void should_Cache_FindById() {
    TaskImpl task;
    try (SqlSession session = sessionManager.openSession(true)) {
      task =
          session.getMapper(TaskMapper.class).findById("TKI:000000000000000000000000000000000027");
    }

    assertThat(task).isNotNull();
    assertThat(cache.getSize()).isEqualTo(1);
  }

  @Test
  public void should_UseCachedTask_For_FindById() {
    sessionManager.getConfiguration().addInterceptor(new ParameterizedQuerySqlCaptureInterceptor());

    try (SqlSession session = sessionManager.openSession(true)) {
      TaskImpl task =
          session.getMapper(TaskMapper.class).findById("TKI:000000000000000000000000000000000027");
      assertThat(task).isNotNull();
    }

    assertThat(ParameterizedQuerySqlCaptureInterceptor.getCapturedSql()).isNotEmpty();
    ParameterizedQuerySqlCaptureInterceptor.resetCapturedSql();

    try (SqlSession session = sessionManager.openSession(true)) {
      session.getMapper(TaskMapper.class).findById("TKI:000000000000000000000000000000000027");
    }
    assertThat(ParameterizedQuerySqlCaptureInterceptor.getCapturedSql()).isNull();
  }

  @Test
  public void should_InvalidateCache_For_Update() {
    TaskImpl task;
    try (SqlSession session = sessionManager.openSession(true)) {
      task =
          session.getMapper(TaskMapper.class).findById("TKI:000000000000000000000000000000000027");
    }

    assertThat(task).isNotNull();
    assertThat(cache.getSize()).isEqualTo(1);

    try (SqlSession session = sessionManager.openSession(true)) {
      task.setModified(Instant.now());
      session.getMapper(TaskMapper.class).update(task);
    }

    assertThat(cache.getSize()).isZero();
  }
}
