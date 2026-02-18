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

package io.kadai.simplehistory.task.internal;

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.KadaiInitializable;
import io.kadai.common.api.KadaiRole;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.internal.InternalKadaiEngine;
import io.kadai.common.internal.KadaiEngineImpl;
import io.kadai.simplehistory.task.api.TaskHistoryQuery;
import io.kadai.simplehistory.task.api.TaskHistoryService;
import io.kadai.spi.history.api.events.task.TaskHistoryEvent;
import io.kadai.spi.history.api.exceptions.TaskHistoryEventNotFoundException;
import io.kadai.user.api.models.User;
import io.kadai.user.internal.UserMapper;
import java.lang.reflect.Field;
import java.util.List;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskHistoryServiceImpl implements TaskHistoryService, KadaiInitializable {

  private static final Logger LOGGER = LoggerFactory.getLogger(TaskHistoryServiceImpl.class);

  private InternalKadaiEngine kadaiEngine;
  private TaskHistoryEventMapper eventMapper;
  private UserMapper userMapper;

  @Override
  public TaskHistoryEvent createTaskHistoryEvent(TaskHistoryEvent event) {
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
  public TaskHistoryEvent getTaskHistoryEvent(String eventId)
      throws TaskHistoryEventNotFoundException {
    TaskHistoryEvent resultEvent = null;
    try {
      kadaiEngine.openConnection();
      resultEvent = eventMapper.findById(eventId);

      if (resultEvent == null) {
        throw new TaskHistoryEventNotFoundException(eventId);
      }

      if (kadaiEngine.getEngine().getConfiguration().isAddAdditionalUserInfo()) {
        User user = userMapper.findById(resultEvent.getUserId());
        if (user != null) {
          resultEvent.setUserLongName(user.getLongName());
        }
      }
      return resultEvent;

    } finally {
      kadaiEngine.returnConnection();
    }
  }

  @Override
  public void deleteTaskHistoryEventsByTaskIds(List<String> taskIds)
      throws NotAuthorizedException, InvalidArgumentException {
    kadaiEngine.getEngine().checkRoleMembership(KadaiRole.ADMIN);

    if (taskIds == null) {
      throw new InvalidArgumentException("List of taskIds must not be null.");
    }
    kadaiEngine.executeInDatabaseConnection(() -> eventMapper.deleteMultipleByTaskIds(taskIds));
  }

  @Override
  public TaskHistoryQuery createTaskHistoryQuery() {
    return new TaskHistoryQueryImpl(kadaiEngine);
  }

  @Override
  public void initialize(KadaiEngine kadaiEngine) {
    LOGGER.info(
        "Task history service implementation initialized with schemaName: {} ",
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
          .hasMapper(TaskHistoryEventMapper.class)) {
        sqlSession.getConfiguration().addMapper(TaskHistoryEventMapper.class);
      }

      if (!sqlSession
          .getConfiguration()
          .getMapperRegistry()
          .hasMapper(TaskHistoryQueryMapper.class)) {

        sqlSession.getConfiguration().addMapper(TaskHistoryQueryMapper.class);
      }

      this.eventMapper = sqlSession.getMapper(TaskHistoryEventMapper.class);
      this.userMapper = sqlSession.getMapper(UserMapper.class);
    } catch (IllegalAccessException e) {
      throw new SystemException(
          "KADAI engine of Session Manager could not be retrieved. Aborting Startup");
    }
  }
}
