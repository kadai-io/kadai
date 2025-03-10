package io.kadai.simplehistory.classification.internal;

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.KadaiInitializable;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.internal.InternalKadaiEngine;
import io.kadai.common.internal.KadaiEngineImpl;
import io.kadai.simplehistory.classification.api.ClassificationHistoryQuery;
import io.kadai.simplehistory.classification.api.ClassificationHistoryService;
import io.kadai.spi.history.api.events.classification.ClassificationHistoryEvent;
import io.kadai.spi.history.api.exceptions.ClassificationHistoryEventNotFoundException;
import java.lang.reflect.Field;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassificationHistoryServiceImpl
    implements ClassificationHistoryService, KadaiInitializable {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ClassificationHistoryServiceImpl.class);

  private InternalKadaiEngine kadaiEngine;
  private ClassificationHistoryEventMapper eventMapper;

  @Override
  public ClassificationHistoryEvent createClassificationHistoryEvent(
      ClassificationHistoryEvent event) {
    if (event.getCreated() == null) {
      event.setCreatedNow();
    }
    try {
      kadaiEngine.openConnection();
      eventMapper.insert(event);
    } finally {
      kadaiEngine.returnConnection();
    }
    return event;
  }

  @Override
  public ClassificationHistoryEvent getClassificationHistoryEvent(String eventId)
      throws ClassificationHistoryEventNotFoundException {
    ClassificationHistoryEvent resultEvent = null;
    try {
      kadaiEngine.openConnection();
      resultEvent = eventMapper.findById(eventId);

      if (resultEvent == null) {
        throw new ClassificationHistoryEventNotFoundException(eventId);
      }

      return resultEvent;
    } finally {
      kadaiEngine.returnConnection();
    }
  }

  @Override
  public ClassificationHistoryQuery createClassificationHistoryQuery() {
    return new ClassificationHistoryQueryImpl(kadaiEngine);
  }

  @Override
  public void initialize(KadaiEngine kadaiEngine) {
    LOGGER.info(
        "Classification history service implementation initialized with schemaName: {} ",
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
          .hasMapper(ClassificationHistoryEventMapper.class)) {
        sqlSession.getConfiguration().addMapper(ClassificationHistoryEventMapper.class);
      }

      if (!sqlSession
          .getConfiguration()
          .getMapperRegistry()
          .hasMapper(ClassificationHistoryQueryMapper.class)) {

        sqlSession.getConfiguration().addMapper(ClassificationHistoryQueryMapper.class);
      }

      this.eventMapper = sqlSession.getMapper(ClassificationHistoryEventMapper.class);
    } catch (IllegalAccessException e) {
      throw new SystemException(
          "KADAI engine of Session Manager could not be retrieved. Aborting Startup");
    }
  }
}
