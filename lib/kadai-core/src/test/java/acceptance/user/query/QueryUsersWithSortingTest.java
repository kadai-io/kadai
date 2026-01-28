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

package acceptance.user.query;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

import acceptance.AbstractAccTest;
import io.kadai.KadaiConfiguration;
import io.kadai.KadaiConfiguration.Builder;
import io.kadai.common.api.BaseQuery.SortDirection;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.user.api.models.UserSummary;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(JaasExtension.class)
class QueryUsersWithSortingTest extends AbstractAccTest {

  private static final int ALL_USERS_COUNT = 18;

  private KadaiEngine kadaiEngine;

  @BeforeEach
  void setupKadaiEngine() throws Exception {
    KadaiConfiguration kadaiConfiguration =
        new Builder(AbstractAccTest.kadaiConfiguration).addAdditionalUserInfo(false).build();
    this.kadaiEngine = KadaiEngine.buildKadaiEngine(kadaiConfiguration);
  }

  @ParameterizedTest
  @EnumSource(SortDirection.class)
  void should_SortUsersByFirstName_For_SortDirection(SortDirection sortDirection) {
    List<UserSummary> users =
        kadaiEngine.getUserService().createUserQuery().orderByFirstName(sortDirection).list();

    assertThat(users)
        .hasSize(ALL_USERS_COUNT)
        .extracting(UserSummary::getFirstName)
        .map(u -> u.replace("ü", "ue").replace("ä", "ae").replace("ö", "oe"))
        .isSortedAccordingTo(
            sortDirection == SortDirection.ASCENDING
                ? CASE_INSENSITIVE_ORDER
                : CASE_INSENSITIVE_ORDER.reversed());
  }

  @ParameterizedTest
  @EnumSource(SortDirection.class)
  void should_SortUsersByLastName_For_SortDirection(SortDirection sortDirection) {
    List<UserSummary> users =
        kadaiEngine.getUserService().createUserQuery().orderByLastName(sortDirection).list();

    assertThat(users)
        .hasSize(ALL_USERS_COUNT)
        .extracting(UserSummary::getLastName)
        .map(u -> u.replace("ü", "ue").replace("ä", "ae").replace("ö", "oe"))
        .isSortedAccordingTo(
            sortDirection == SortDirection.ASCENDING
                ? CASE_INSENSITIVE_ORDER
                : CASE_INSENSITIVE_ORDER.reversed());
  }

  @ParameterizedTest
  @EnumSource(SortDirection.class)
  void should_SortUsersByOrgLevel1_For_SortDirection(SortDirection sortDirection) {
    List<UserSummary> users =
        kadaiEngine.getUserService().createUserQuery().orderByOrgLevel1(sortDirection).list();

    assertThat(users)
        .hasSize(ALL_USERS_COUNT)
        .extracting(UserSummary::getOrgLevel1)
        .map(u -> u.replace("ü", "ue").replace("ä", "ae").replace("ö", "oe"))
        .isSortedAccordingTo(
            sortDirection == SortDirection.ASCENDING
                ? CASE_INSENSITIVE_ORDER
                : CASE_INSENSITIVE_ORDER.reversed());
  }

  @ParameterizedTest
  @EnumSource(SortDirection.class)
  void should_SortUsersByOrgLevel2_For_SortDirection(SortDirection sortDirection) {
    List<UserSummary> users =
        kadaiEngine.getUserService().createUserQuery().orderByOrgLevel2(sortDirection).list();

    assertThat(users)
        .hasSize(ALL_USERS_COUNT)
        .extracting(UserSummary::getOrgLevel2)
        .map(u -> u.replace("ü", "ue").replace("ä", "ae").replace("ö", "oe"))
        .isSortedAccordingTo(
            sortDirection == SortDirection.ASCENDING
                ? CASE_INSENSITIVE_ORDER
                : CASE_INSENSITIVE_ORDER.reversed());
  }

  @ParameterizedTest
  @EnumSource(SortDirection.class)
  void should_SortUsersByOrgLevel3_For_SortDirection(SortDirection sortDirection) {
    List<UserSummary> users =
        kadaiEngine.getUserService().createUserQuery().orderByOrgLevel3(sortDirection).list();

    assertThat(users)
        .hasSize(ALL_USERS_COUNT)
        .extracting(UserSummary::getOrgLevel3)
        .map(u -> u.replace("ü", "ue").replace("ä", "ae").replace("ö", "oe"))
        .isSortedAccordingTo(
            sortDirection == SortDirection.ASCENDING
                ? CASE_INSENSITIVE_ORDER
                : CASE_INSENSITIVE_ORDER.reversed());
  }

  @ParameterizedTest
  @EnumSource(SortDirection.class)
  void should_SortUsersByOrgLevel4_For_SortDirection(SortDirection sortDirection) {
    List<UserSummary> users =
        kadaiEngine.getUserService().createUserQuery().orderByOrgLevel4(sortDirection).list();

    assertThat(users)
        .hasSize(ALL_USERS_COUNT)
        .extracting(UserSummary::getOrgLevel4)
        .map(u -> u.replace("ü", "ue").replace("ä", "ae").replace("ö", "oe"))
        .isSortedAccordingTo(
            sortDirection == SortDirection.ASCENDING
                ? CASE_INSENSITIVE_ORDER
                : CASE_INSENSITIVE_ORDER.reversed());
  }
}
