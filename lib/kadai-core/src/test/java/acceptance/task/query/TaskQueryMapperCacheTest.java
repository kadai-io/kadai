package acceptance.task.query;

import static org.assertj.core.api.Assertions.assertThat;

import acceptance.AbstractAccTest;
import acceptance.ParameterizedQuerySqlCaptureInterceptor;
import io.kadai.task.internal.TaskMapper;
import io.kadai.task.internal.TaskQueryImpl;
import io.kadai.task.internal.TaskQueryMapper;
import io.kadai.task.internal.models.TaskImpl;
import io.kadai.task.internal.models.TaskSummaryImpl;
import java.time.Instant;
import java.util.List;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TaskQueryMapperCacheTest extends AbstractAccTest {

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
  public void should_Cache_QueryTaskSummaries() {
    List<TaskSummaryImpl> tasks;
    try (SqlSession session = sessionManager.openSession(true)) {
      tasks =
          session
              .getMapper(TaskQueryMapper.class)
              .queryTaskSummaries(
                  (TaskQueryImpl)
                      taskService
                          .createTaskQuery()
                          .idIn("TKI:000000000000000000000000000000000027"));
    }

    assertThat(tasks).isNotNull();
    assertThat(tasks.size()).isEqualTo(1);
    assertThat(cache.getSize()).isEqualTo(1);
  }

  @Test
  public void should_UseCachedTasks_For_QueryTaskSummaries() {
    sessionManager.getConfiguration().addInterceptor(new ParameterizedQuerySqlCaptureInterceptor());

    try (SqlSession session = sessionManager.openSession(true)) {
      List<TaskSummaryImpl> tasks =
          session
              .getMapper(TaskQueryMapper.class)
              .queryTaskSummaries(
                  (TaskQueryImpl)
                      taskService
                          .createTaskQuery()
                          .idIn("TKI:000000000000000000000000000000000027"));
      assertThat(tasks).isNotNull();
    }

    assertThat(ParameterizedQuerySqlCaptureInterceptor.getCapturedSql()).isNotEmpty();
    ParameterizedQuerySqlCaptureInterceptor.resetCapturedSql();

    try (SqlSession session = sessionManager.openSession(true)) {
      List<TaskSummaryImpl> tasks =
          session
              .getMapper(TaskQueryMapper.class)
              .queryTaskSummaries(
                  (TaskQueryImpl)
                      taskService
                          .createTaskQuery()
                          .idIn("TKI:000000000000000000000000000000000027"));
      assertThat(tasks).isNotNull();
    }
    assertThat(ParameterizedQuerySqlCaptureInterceptor.getCapturedSql()).isNull();
  }

  @Test
  public void should_CacheCount_For_CountQueryTasks() {
    Long count;
    try (SqlSession session = sessionManager.openSession(true)) {
      count =
          session
              .getMapper(TaskQueryMapper.class)
              .countQueryTasks(
                  (TaskQueryImpl)
                      taskService
                          .createTaskQuery()
                          .idIn("TKI:000000000000000000000000000000000027"));
    }

    assertThat(count).isNotNull();
    assertThat(count).isEqualTo(1);
    assertThat(cache.getSize()).isEqualTo(1);
  }

  @Test
  public void should_UseCachedCount_For_CountQueryTasks() {
    sessionManager.getConfiguration().addInterceptor(new ParameterizedQuerySqlCaptureInterceptor());

    try (SqlSession session = sessionManager.openSession(true)) {
      Long count =
          session
              .getMapper(TaskQueryMapper.class)
              .countQueryTasks(
                  (TaskQueryImpl)
                      taskService
                          .createTaskQuery()
                          .idIn("TKI:000000000000000000000000000000000027"));
      assertThat(count).isNotNull();
    }

    assertThat(ParameterizedQuerySqlCaptureInterceptor.getCapturedSql()).isNotEmpty();
    ParameterizedQuerySqlCaptureInterceptor.resetCapturedSql();

    try (SqlSession session = sessionManager.openSession(true)) {
      Long count =
          session
              .getMapper(TaskQueryMapper.class)
              .countQueryTasks(
                  (TaskQueryImpl)
                      taskService
                          .createTaskQuery()
                          .idIn("TKI:000000000000000000000000000000000027"));
      assertThat(count).isNotNull();
    }
    assertThat(ParameterizedQuerySqlCaptureInterceptor.getCapturedSql()).isNull();
  }

  @Test
  public void should_InvalidateCache_When_Update() {
    List<TaskSummaryImpl> tasks;
    try (SqlSession session = sessionManager.openSession(true)) {
      tasks =
          session
              .getMapper(TaskQueryMapper.class)
              .queryTaskSummaries(
                  (TaskQueryImpl)
                      taskService
                          .createTaskQuery()
                          .idIn("TKI:000000000000000000000000000000000027"));
    }

    assertThat(tasks).isNotNull();
    assertThat(tasks.size()).isEqualTo(1);
    assertThat(cache.getSize()).isEqualTo(1);

    TaskImpl task;
    try (SqlSession session = sessionManager.openSession(true)) {
      task = session.getMapper(TaskMapper.class).findById(tasks.get(0).getId());
      assertThat(task).isNotNull();
      assertThat(cache.getSize()).isEqualTo(1);
    }

    try (SqlSession session = sessionManager.openSession(true)) {
      task.setModified(Instant.now());
      session.getMapper(TaskMapper.class).update(task);
    }

    assertThat(cache.getSize()).isZero();
  }
}
