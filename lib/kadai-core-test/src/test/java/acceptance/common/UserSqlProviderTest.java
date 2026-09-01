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

package acceptance.common;

import static io.kadai.common.internal.util.SqlProviderUtil.DB2_WITH_UR_FOR_COLUMN_QUERY;
import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.user.internal.UserMapperSqlProvider;
import io.kadai.user.internal.UserQuerySqlProvider;
import org.junit.jupiter.api.Test;

class UserSqlProviderTest {

  @Test
  void should_NotUseUncommittedRead_ForOrdinaryUserReads() {
    assertThat(UserMapperSqlProvider.findById()).doesNotContainIgnoringCase("with ur");
    assertThat(UserMapperSqlProvider.findByIds()).doesNotContainIgnoringCase("with ur");
    assertThat(UserMapperSqlProvider.findGroupsById()).doesNotContainIgnoringCase("with ur");
    assertThat(UserMapperSqlProvider.findPermissionsById())
        .doesNotContainIgnoringCase("with ur");
    assertThat(UserQuerySqlProvider.queryUsers()).doesNotContainIgnoringCase("with ur");
    assertThat(UserQuerySqlProvider.countQueryUsers()).doesNotContainIgnoringCase("with ur");
  }

  @Test
  void should_UseUncommittedReadOnly_ForColumnValueDiscovery() {
    assertThat(UserQuerySqlProvider.queryUserColumnValues())
        .contains(DB2_WITH_UR_FOR_COLUMN_QUERY);
  }
}
