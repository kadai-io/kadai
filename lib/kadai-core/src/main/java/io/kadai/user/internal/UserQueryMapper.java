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

package io.kadai.user.internal;

import io.kadai.user.internal.models.UserImpl;
import java.util.List;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.SelectProvider;

/**
 * This interface provides a Mapper for querying {@linkplain io.kadai.user.api.models.User Users}.
 */
public interface UserQueryMapper {

  @SelectProvider(type = UserQuerySqlProvider.class, method = "queryUsers")
  @Result(property = "id", column = "USER_ID")
  @Result(
      property = "groups",
      column = "USER_ID",
      many = @Many(select = "io.kadai.user.internal.UserMapper.findGroupsById"))
  @Result(
      property = "permissions",
      column = "USER_ID",
      many = @Many(select = "io.kadai.user.internal.UserMapper.findPermissionsById"))
  @Result(property = "firstName", column = "FIRST_NAME")
  @Result(property = "lastName", column = "LAST_NAME")
  @Result(property = "fullName", column = "FULL_NAME")
  @Result(property = "longName", column = "LONG_NAME")
  @Result(property = "email", column = "E_MAIL")
  @Result(property = "phone", column = "PHONE")
  @Result(property = "mobilePhone", column = "MOBILE_PHONE")
  @Result(property = "orgLevel4", column = "ORG_LEVEL_4")
  @Result(property = "orgLevel3", column = "ORG_LEVEL_3")
  @Result(property = "orgLevel2", column = "ORG_LEVEL_2")
  @Result(property = "orgLevel1", column = "ORG_LEVEL_1")
  @Result(property = "data", column = "DATA")
  List<UserImpl> queryUsers(UserQueryImpl userQuery);

  @SelectProvider(type = UserQuerySqlProvider.class, method = "countQueryUsers")
  Long countQueryUsers(UserQueryImpl userQuery);

  @SelectProvider(type = UserQuerySqlProvider.class, method = "queryUserColumnValues")
  List<String> queryUserColumnValues(UserQueryImpl userQuery);
}
