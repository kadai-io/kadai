/*
 * Copyright [2025] [envite consulting GmbH]
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

package io.kadai.task.internal;

import static io.kadai.common.api.BaseQuery.toLowerCopy;

import io.kadai.common.api.KadaiRole;
import io.kadai.common.api.TimeInterval;
import io.kadai.common.internal.InternalKadaiEngine;
import io.kadai.task.api.TaskCommentQuery;
import io.kadai.task.api.TaskCommentQueryColumnName;
import io.kadai.task.api.exceptions.TaskNotFoundException;
import io.kadai.task.api.models.TaskComment;
import io.kadai.workbasket.api.exceptions.NotAuthorizedOnWorkbasketException;
import io.kadai.workbasket.api.exceptions.NotAuthorizedToQueryWorkbasketException;
import io.kadai.workbasket.internal.WorkbasketQueryImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** TaskCommentQuery for generating dynamic sql. */
public class TaskCommentQueryImpl implements TaskCommentQuery {

  private static final Logger LOGGER = LoggerFactory.getLogger(TaskCommentQueryImpl.class);

  private static final String LINK_TO_MAPPER =
      "io.kadai.task.internal.TaskCommentQueryMapper.queryTaskComments";

  private static final String LINK_TO_VALUE_MAPPER =
      "io.kadai.task.internal.TaskCommentQueryMapper.queryTaskCommentColumnValues";

  private static final String LINK_TO_COUNTER =
      "io.kadai.task.internal.TaskCommentQueryMapper.countQueryTaskComments";

  private final InternalKadaiEngine kadaiEngine;
  private final TaskServiceImpl taskService;
  private final List<String> orderBy;
  private final List<String> orderColumns;
  private TaskCommentQueryColumnName queryColumnName;
  private String[] idIn;
  private String[] idNotIn;
  private String[] idLike;
  private String[] idNotLike;
  private String[] taskIdIn;
  private String[] creatorIn;
  private String[] creatorNotIn;
  private String[] creatorLike;
  private String[] creatorNotLike;
  private String[] textFieldLike;
  private String[] textFieldNotLike;
  private TimeInterval[] modifiedIn;
  private TimeInterval[] modifiedNotIn;
  private TimeInterval[] createdIn;
  private TimeInterval[] createdNotIn;

  private String[] accessIdIn;
  private boolean joinWithUserInfo;

  TaskCommentQueryImpl(InternalKadaiEngine kadaiEngine) {
    this.kadaiEngine = kadaiEngine;
    this.taskService = (TaskServiceImpl) kadaiEngine.getEngine().getTaskService();
    this.orderBy = new ArrayList<>();
    this.orderColumns = new ArrayList<>();
    this.joinWithUserInfo = kadaiEngine.getEngine().getConfiguration().isAddAdditionalUserInfo();
  }

  @Override
  public TaskCommentQuery idIn(String... taskCommentIds) {
    this.idIn = taskCommentIds;
    return this;
  }

  @Override
  public TaskCommentQuery idNotIn(String... taskCommentIds) {
    this.idNotIn = taskCommentIds;
    return this;
  }

  @Override
  public TaskCommentQuery idLike(String... taskCommentIds) {
    this.idLike = toLowerCopy(taskCommentIds);
    return this;
  }

  @Override
  public TaskCommentQuery idNotLike(String... taskCommentIds) {
    this.idNotLike = toLowerCopy(taskCommentIds);
    return this;
  }

  @Override
  public TaskCommentQuery taskIdIn(String... taskIds) {
    this.taskIdIn = taskIds;
    return this;
  }

  @Override
  public TaskCommentQuery textFieldLike(String... texts) {
    this.textFieldLike = toLowerCopy(texts);
    return this;
  }

  @Override
  public TaskCommentQuery textFieldNotLike(String... texts) {
    this.textFieldNotLike = toLowerCopy(texts);
    return this;
  }

  @Override
  public TaskCommentQuery creatorIn(String... creators) {
    this.creatorIn = creators;
    return this;
  }

  @Override
  public TaskCommentQuery creatorNotIn(String... creators) {
    this.creatorNotIn = creators;
    return this;
  }

  @Override
  public TaskCommentQuery creatorLike(String... creators) {
    this.creatorLike = toLowerCopy(creators);
    return this;
  }

  @Override
  public TaskCommentQuery creatorNotLike(String... creators) {
    this.creatorNotLike = toLowerCopy(creators);
    return this;
  }

  @Override
  public TaskCommentQuery createdWithin(TimeInterval... intervals) {
    validateAllIntervals(intervals);
    this.createdIn = intervals;
    return this;
  }

  @Override
  public TaskCommentQuery createdNotWithin(TimeInterval... intervals) {
    this.createdNotIn = intervals;
    return this;
  }

  @Override
  public TaskCommentQuery modifiedWithin(TimeInterval... intervals) {
    validateAllIntervals(intervals);
    this.modifiedIn = intervals;
    return this;
  }

  @Override
  public TaskCommentQuery modifiedNotWithin(TimeInterval... intervals) {
    this.modifiedNotIn = intervals;
    return this;
  }

  @Override
  public List<TaskComment> list() {
    checkTaskPermission();
    setupAccessIds();
    return kadaiEngine.executeInDatabaseConnection(
        () -> kadaiEngine.getSqlSession().selectList(LINK_TO_MAPPER, this));
  }

  @Override
  public List<TaskComment> list(int offset, int limit) {
    checkTaskPermission();
    setupAccessIds();
    RowBounds rowBounds = new RowBounds(offset, limit);
    return kadaiEngine.executeInDatabaseConnection(
        () -> kadaiEngine.getSqlSession().selectList(LINK_TO_MAPPER, this, rowBounds));
  }

  @Override
  public List<String> listValues(
      TaskCommentQueryColumnName columnName, SortDirection sortDirection) {
    checkTaskPermission();
    setupAccessIds();
    queryColumnName = columnName;
    // TO-DO: order?
    if (columnName == TaskCommentQueryColumnName.CREATOR_FULL_NAME) {
      joinWithUserInfo = true;
    }

    return kadaiEngine.executeInDatabaseConnection(
        () -> kadaiEngine.getSqlSession().selectList(LINK_TO_VALUE_MAPPER, this));
  }

  @Override
  public TaskComment single() {
    checkTaskPermission();
    setupAccessIds();
    return kadaiEngine.executeInDatabaseConnection(
        () -> kadaiEngine.getSqlSession().selectOne(LINK_TO_MAPPER, this));
  }

  @Override
  public long count() {
    checkTaskPermission();
    setupAccessIds();
    Long rowCount =
        kadaiEngine.executeInDatabaseConnection(
            () -> kadaiEngine.getSqlSession().selectOne(LINK_TO_COUNTER, this));
    return (rowCount == null) ? 0L : rowCount;
  }

  public TaskCommentQueryColumnName getQueryColumnName() {
    return queryColumnName;
  }

  public String[] getIdIn() {
    return idIn;
  }

  public String[] getIdNotIn() {
    return idNotIn;
  }

  public String[] getIdLike() {
    return idLike;
  }

  public String[] getIdNotLike() {
    return idNotLike;
  }

  public String[] getTaskIdIn() {
    return taskIdIn;
  }

  public String[] getCreatorIn() {
    return creatorIn;
  }

  public String[] getCreatorNotIn() {
    return creatorNotIn;
  }

  public String[] getCreatorLike() {
    return creatorLike;
  }

  public String[] getCreatorNotLike() {
    return creatorNotLike;
  }

  public String[] getTextFieldLike() {
    return textFieldLike;
  }

  public String[] getTextFieldNotLike() {
    return textFieldNotLike;
  }

  public TimeInterval[] getModifiedIn() {
    return modifiedIn;
  }

  public TimeInterval[] getModifiedNotIn() {
    return modifiedNotIn;
  }

  public TimeInterval[] getCreatedIn() {
    return createdIn;
  }

  public TimeInterval[] getCreatedNotIn() {
    return createdNotIn;
  }

  public String[] getAccessIdIn() {
    return accessIdIn;
  }

  public boolean isIncludeLongName() {
    return joinWithUserInfo;
  }

  public void setIncludeLongName(boolean joinWithUserInfo) {
    this.joinWithUserInfo = joinWithUserInfo;
  }

  @Override
  public TaskCommentQuery orderByCreated(SortDirection sortDirection) {
    return addOrderCriteria("CREATED", sortDirection);
  }

  @Override
  public TaskCommentQuery orderByModified(SortDirection sortDirection) {
    return addOrderCriteria("MODIFIED", sortDirection);
  }

  private void checkTaskPermission() {

    if (taskIdIn != null) {
      if (kadaiEngine.getEngine().isUserInRole(KadaiRole.ADMIN, KadaiRole.TASK_ADMIN)) {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Skipping permissions check since user is in role ADMIN or TASK_ADMIN.");
        }
        return;
      }

      Arrays.stream(taskIdIn)
          .forEach(
              taskId -> {
                try {
                  taskService.getTask(taskId);
                } catch (NotAuthorizedOnWorkbasketException e) {
                  throw new NotAuthorizedToQueryWorkbasketException(
                      e.getMessage(), e.getErrorCode(), e);
                } catch (TaskNotFoundException e) {
                  LOGGER.warn(
                      String.format("The Task with the ID ' %s ' does not exist.", taskId), e);
                }
              });
    }
  }

  private TaskCommentQuery addOrderCriteria(String columnName, SortDirection sortDirection) {
    String orderByDirection =
        " " + (sortDirection == null ? SortDirection.ASCENDING : sortDirection);
    orderBy.add(columnName + orderByDirection);
    orderColumns.add(columnName);
    return this;
  }

  private void setupAccessIds() {
    if (kadaiEngine.getEngine().isUserInRole(KadaiRole.ADMIN, KadaiRole.TASK_ADMIN)) {
      this.accessIdIn = null;
    } else if (this.accessIdIn == null) {
      String[] accessIds = new String[0];
      List<String> ucAccessIds = kadaiEngine.getEngine().getCurrentUserContext().getAccessIds();
      if (!ucAccessIds.isEmpty()) {
        accessIds = new String[ucAccessIds.size()];
        accessIds = ucAccessIds.toArray(accessIds);
      }
      this.accessIdIn = accessIds;
      WorkbasketQueryImpl.lowercaseAccessIds(this.accessIdIn);
    }
  }

  private void validateAllIntervals(TimeInterval[] intervals) {
    for (TimeInterval ti : intervals) {
      if (!ti.isValid()) {
        throw new IllegalArgumentException("TimeInterval " + ti + " is invalid.");
      }
    }
  }

  @Override
  public String toString() {
    return "TaskCommentQueryImpl [kadaiEngine="
        + kadaiEngine
        + ", taskService="
        + taskService
        + ", queryColumnName="
        + queryColumnName
        + ", idIn="
        + Arrays.toString(idIn)
        + ", idNotIn="
        + Arrays.toString(idNotIn)
        + ", idLike="
        + Arrays.toString(idLike)
        + ", idNotLike="
        + Arrays.toString(idNotLike)
        + ", taskIdIn="
        + Arrays.toString(taskIdIn)
        + ", creatorIn="
        + Arrays.toString(creatorIn)
        + ", creatorNotIn="
        + Arrays.toString(creatorNotIn)
        + ", creatorLike="
        + Arrays.toString(creatorLike)
        + ", creatorNotLike="
        + Arrays.toString(creatorNotLike)
        + ", textFieldLike="
        + Arrays.toString(textFieldLike)
        + ", textFieldNotLike="
        + Arrays.toString(textFieldNotLike)
        + ", modifiedIn="
        + Arrays.toString(modifiedIn)
        + ", modifiedNotIn="
        + Arrays.toString(modifiedNotIn)
        + ", createdIn="
        + Arrays.toString(createdIn)
        + ", createdNotIn="
        + Arrays.toString(createdNotIn)
        + ", accessIdIn="
        + Arrays.toString(accessIdIn)
        + ", joinWithUserInfo="
        + joinWithUserInfo
        + "]";
  }
}
