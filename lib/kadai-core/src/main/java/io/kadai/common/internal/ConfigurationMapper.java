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

package io.kadai.common.internal;

import static io.kadai.common.internal.util.SqlProviderUtil.CLOSING_SCRIPT_TAG;
import static io.kadai.common.internal.util.SqlProviderUtil.OPENING_SCRIPT_TAG;

import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface ConfigurationMapper {

  @Select(
      OPENING_SCRIPT_TAG
          + "SELECT ENFORCE_SECURITY FROM CONFIGURATION "
          + "<if test='lockForUpdate == true'>"
          + "FETCH FIRST ROW ONLY FOR UPDATE "
          + "<if test=\"_databaseId == 'db2'\">WITH RS USE AND KEEP UPDATE LOCKS </if> "
          + "</if>"
          + CLOSING_SCRIPT_TAG)
  Boolean isSecurityEnabled(boolean lockForUpdate);

  @Update("UPDATE CONFIGURATION SET ENFORCE_SECURITY = #{securityEnabled} WHERE NAME = 'MASTER'")
  void setSecurityEnabled(@Param("securityEnabled") boolean securityEnabled);

  @Select(
      OPENING_SCRIPT_TAG
          + "SELECT CUSTOM_ATTRIBUTES FROM CONFIGURATION "
          + "<if test='lockForUpdate == true'>"
          + "FETCH FIRST ROW ONLY FOR UPDATE"
          + "<if test=\"_databaseId == 'db2'\">WITH RS USE AND KEEP UPDATE LOCKS </if> "
          + "</if>"
          + CLOSING_SCRIPT_TAG)
  Map<String, Object> getAllCustomAttributes(boolean lockForUpdate);

  @Update("UPDATE CONFIGURATION SET CUSTOM_ATTRIBUTES = #{customAttributes} WHERE NAME = 'MASTER'")
  void setAllCustomAttributes(@Param("customAttributes") Map<String, ?> customAttributes);
}
