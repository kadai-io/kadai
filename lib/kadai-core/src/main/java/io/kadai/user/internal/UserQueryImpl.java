package io.kadai.user.internal;

import io.kadai.common.api.exceptions.KadaiRuntimeException;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.internal.InternalKadaiEngine;
import io.kadai.common.internal.PaginationInterceptor;
import io.kadai.user.api.UserQuery;
import io.kadai.user.api.UserQueryColumnName;
import io.kadai.user.api.models.User;
import io.kadai.user.internal.models.UserImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.ibatis.exceptions.PersistenceException;

public class UserQueryImpl implements UserQuery {

  private static final String LINK_TO_USER_MAPPER =
      "io.kadai.user.internal.UserQueryMapper.queryUsers";

  private static final String LINK_TO_VALUE_MAPPER =
      "io.kadai.user.internal.UserQueryMapper.queryUserColumnValues";

  private static final String LINK_TO_COUNTER =
      "io.kadai.user.internal.UserQueryMapper.countQueryUsers";

  private final InternalKadaiEngine kadaiEngine;
  private final List<String> orderColumns = new ArrayList<>();
  private final List<String> orderBy = new ArrayList<>();
  private UserQueryColumnName columnName;
  private String[] idIn;
  private String[] orgLevel1In;
  private String[] orgLevel2In;
  private String[] orgLevel3In;
  private String[] orgLevel4In;

  public UserQueryImpl(InternalKadaiEngine kadaiEngine) {
    this.kadaiEngine = kadaiEngine;
  }

  @Override
  public UserQuery idIn(String... ids) {
    this.idIn = ids;
    return this;
  }

  @Override
  public UserQuery orgLevel1In(String... orgLevel1s) {
    this.orgLevel1In = orgLevel1s;
    return this;
  }

  @Override
  public UserQuery orgLevel2In(String... orgLevel2s) {
    this.orgLevel2In = orgLevel2s;
    return this;
  }

  @Override
  public UserQuery orgLevel3In(String... orgLevel3s) {
    this.orgLevel3In = orgLevel3s;
    return this;
  }

  @Override
  public UserQuery orgLevel4In(String... orgLevel4s) {
    this.orgLevel4In = orgLevel4s;
    return this;
  }

  @Override
  public List<User> list() {
    UserServiceImpl userService = (UserServiceImpl) kadaiEngine.getEngine().getUserService();
    return kadaiEngine.executeInDatabaseConnection(
        () ->
            kadaiEngine.getSqlSession().<UserImpl>selectList(LINK_TO_USER_MAPPER, this).stream()
                .map(
                    user -> {
                      user.setDomains(userService.determineDomains(user));
                      return (User) user;
                    })
                .toList());
  }

  @Override
  public List<User> list(int offset, int limit) {
    try {
      UserServiceImpl userService = (UserServiceImpl) kadaiEngine.getEngine().getUserService();
      kadaiEngine.openConnection();

      PaginationInterceptor.setPagination(offset, limit);

      return kadaiEngine.getSqlSession().<UserImpl>selectList(LINK_TO_USER_MAPPER, this).stream()
          .map(
              user -> {
                user.setDomains(userService.determineDomains(user));
                return (User) user;
              })
          .toList();
    } catch (PersistenceException e) {
      if (e.getMessage().contains("ERRORCODE=-4470")) {
        KadaiRuntimeException ex =
            new SystemException(
                "The offset beginning was set over the amount of result-rows.", e.getCause());
        ex.setStackTrace(e.getStackTrace());
        throw ex;
      }
      throw e;
    } finally {
      kadaiEngine.returnConnection();
    }
  }

  @Override
  public List<String> listValues(UserQueryColumnName columnName, SortDirection sortDirection) {
    try {
      kadaiEngine.openConnection();
      this.columnName = columnName;
      this.orderBy.clear();
      this.addOrderCriteria(columnName.toString(), sortDirection);
      return kadaiEngine.getSqlSession().selectList(LINK_TO_VALUE_MAPPER, this);
    } finally {
      kadaiEngine.returnConnection();
    }
  }

  @Override
  public User single() {
    try {
      UserServiceImpl userService = (UserServiceImpl) kadaiEngine.getEngine().getUserService();
      kadaiEngine.openConnection();
      UserImpl user = kadaiEngine.getSqlSession().selectOne(LINK_TO_USER_MAPPER, this);
      user.setDomains(userService.determineDomains(user));
      return user;
    } finally {
      kadaiEngine.returnConnection();
    }
  }

  @Override
  public long count() {
    Long rowCount = null;
    try {
      kadaiEngine.openConnection();
      rowCount = kadaiEngine.getSqlSession().selectOne(LINK_TO_COUNTER, this);
      return (rowCount == null) ? 0L : rowCount;
    } finally {
      kadaiEngine.returnConnection();
    }
  }

  public UserQueryColumnName getColumnName() {
    return columnName;
  }

  public void setColumnName(UserQueryColumnName columnName) {
    this.columnName = columnName;
  }

  public List<String> getOrderColumns() {
    return orderColumns;
  }

  public List<String> getOrderBy() {
    return orderBy;
  }

  public String[] getIdIn() {
    return idIn;
  }

  public void setIdIn(String[] idIn) {
    this.idIn = idIn;
  }

  public String[] getOrgLevel1In() {
    return orgLevel1In;
  }

  public void setOrgLevel1In(String[] orgLevel1In) {
    this.orgLevel1In = orgLevel1In;
  }

  public String[] getOrgLevel2In() {
    return orgLevel2In;
  }

  public void setOrgLevel2In(String[] orgLevel2In) {
    this.orgLevel2In = orgLevel2In;
  }

  public String[] getOrgLevel3In() {
    return orgLevel3In;
  }

  public void setOrgLevel3In(String[] orgLevel3In) {
    this.orgLevel3In = orgLevel3In;
  }

  public String[] getOrgLevel4In() {
    return orgLevel4In;
  }

  public void setOrgLevel4In(String[] orgLevel4In) {
    this.orgLevel4In = orgLevel4In;
  }

  private UserQuery addOrderCriteria(String columnName, SortDirection sortDirection) {
    String orderByDirection =
        " " + (sortDirection == null ? SortDirection.ASCENDING : sortDirection);
    orderBy.add(columnName + orderByDirection);
    orderColumns.add(columnName);
    return this;
  }

  @Override
  public String toString() {
    return "UserQueryImpl [kadaiEngine="
        + kadaiEngine
        + ", columnName="
        + columnName
        + ", orderColumns="
        + orderColumns
        + ", orderBy="
        + orderBy
        + ", idIn="
        + Arrays.toString(idIn)
        + ", orgLevel1In="
        + Arrays.toString(orgLevel1In)
        + ", orgLevel2In="
        + Arrays.toString(orgLevel2In)
        + ", orgLevel3In="
        + Arrays.toString(orgLevel3In)
        + ", orgLevel4In="
        + Arrays.toString(orgLevel4In)
        + "]";
  }
}
