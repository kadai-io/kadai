package io.kadai.simplehistory.workbasket.internal;

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.internal.InternalKadaiEngine;
import io.kadai.common.internal.KadaiEngineImpl;
import io.kadai.simplehistory.workbasket.api.WorkbasketHistoryQuery;
import io.kadai.simplehistory.workbasket.api.WorkbasketHistoryService;
import io.kadai.spi.history.api.KadaiEventConsumer;
import io.kadai.spi.history.api.events.workbasket.WorkbasketHistoryEvent;
import io.kadai.spi.history.api.exceptions.WorkbasketHistoryEventNotFoundException;
import java.lang.reflect.Field;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbasketHistoryServiceImpl
    implements WorkbasketHistoryService, KadaiEventConsumer<WorkbasketHistoryEvent> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkbasketHistoryServiceImpl.class);

  private InternalKadaiEngine kadaiEngine;
  private WorkbasketHistoryEventMapper eventMapper;

  @Override
  public WorkbasketHistoryEvent createWorkbasketHistoryEvent(WorkbasketHistoryEvent event) {
    if (event.getCreated() == null) {
      event.setCreatedNow();
    }
    try {
      kadaiEngine.openConnection();
      eventMapper.insert(event);

      return event;
    } finally {
      kadaiEngine.returnConnection();
    }
  }

  @Override
  public WorkbasketHistoryEvent getWorkbasketHistoryEvent(String eventId)
      throws WorkbasketHistoryEventNotFoundException {
    WorkbasketHistoryEvent resultEvent = null;
    try {
      kadaiEngine.openConnection();
      resultEvent = eventMapper.findById(eventId);

      if (resultEvent == null) {
        throw new WorkbasketHistoryEventNotFoundException(eventId);
      }

      return resultEvent;
    } finally {
      kadaiEngine.returnConnection();
    }
  }

  @Override
  public WorkbasketHistoryQuery createWorkbasketHistoryQuery() {
    return new WorkbasketHistoryQueryImpl(kadaiEngine);
  }

  @Override
  public void consume(WorkbasketHistoryEvent event) {
    createWorkbasketHistoryEvent(event);
  }

  @Override
  public void initialize(KadaiEngine kadaiEngine) {
    LOGGER.info(
        "Workbasket history service implementation initialized with schemaName: {} ",
        kadaiEngine.getConfiguration().getSchemaName());

    Field sessionManager = null;
    try {
      Field internalKadaiEngineImpl =
          KadaiEngineImpl.class.getDeclaredField("internalKadaiEngineImpl");
      internalKadaiEngineImpl.setAccessible(true);
      this.kadaiEngine = (InternalKadaiEngine) internalKadaiEngineImpl.get(kadaiEngine);
      sessionManager = KadaiEngineImpl.class.getDeclaredField("sessionManager");
      sessionManager.setAccessible(true);
    } catch (NoSuchFieldException e) {
      throw new SystemException("SQL Session could not be retrieved. Aborting Startup");
    } catch (IllegalAccessException e) {
      throw new SystemException(e.getMessage());
    }
    try {
      SqlSession sqlSession = (SqlSession) sessionManager.get(kadaiEngine);
      if (!sqlSession
          .getConfiguration()
          .getMapperRegistry()
          .hasMapper(WorkbasketHistoryEventMapper.class)) {
        sqlSession.getConfiguration().addMapper(WorkbasketHistoryEventMapper.class);
      }

      if (!sqlSession
          .getConfiguration()
          .getMapperRegistry()
          .hasMapper(WorkbasketHistoryQueryMapper.class)) {

        sqlSession.getConfiguration().addMapper(WorkbasketHistoryQueryMapper.class);
      }

      this.eventMapper = sqlSession.getMapper(WorkbasketHistoryEventMapper.class);
    } catch (IllegalAccessException e) {
      throw new SystemException(
          "KADAI engine of Session Manager could not be retrieved. Aborting Startup");
    }
  }

  @Override
  public Class<WorkbasketHistoryEvent> reify() {
    return WorkbasketHistoryEvent.class;
  }
}
