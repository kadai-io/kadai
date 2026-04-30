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

import static org.assertj.core.api.Assertions.assertThat;

import acceptance.AbstractAccTest;
import io.kadai.KadaiConfiguration;
import io.kadai.KadaiConfiguration.Builder;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.user.api.models.UserSummary;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(JaasExtension.class)
class QueryUsersAccTest extends AbstractAccTest {

  private static final int ALL_USERS_COUNT = 18;
  private static final int ENVITE_USERS_COUNT = 14;

  private KadaiEngine kadaiEngine;

  @BeforeEach
  void setupKadaiEngine() throws Exception {
    KadaiConfiguration kadaiConfiguration =
        new Builder(AbstractAccTest.kadaiConfiguration).addAdditionalUserInfo(false).build();
    this.kadaiEngine = KadaiEngine.buildKadaiEngine(kadaiConfiguration);
  }

  @Test
  void should_ReturnAllUsers_When_QueryHasNoConstraints() {
    List<UserSummary> users = kadaiEngine.getUserService().createUserQuery().list();

    assertThat(users).hasSize(ALL_USERS_COUNT);
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = ';',
      value = {"user-1-1", "user-1-1,user-1-2", "user-1-1,user-1-2,teamlead-1"})
  void should_ReturnExistingUsers_For_GivenIds(String userIdsString) {
    String[] userIds = userIdsString.split(",");

    List<UserSummary> users = kadaiEngine.getUserService().createUserQuery().idIn(userIds).list();

    assertThat(users)
        .hasSize(userIds.length)
        .extracting(UserSummary::getId)
        .containsExactlyInAnyOrder(userIds);
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = ';',
      value = {
        "KADAI",
        "KADAI, non-existent",
        "KADAI, non-existent, non-existent2",
      })
  void should_ReturnExistingUsers_For_GivenOrgLevel1s(String orgLevel1s) {
    String[] orgLevels = orgLevel1s.split(",");

    List<UserSummary> users =
        kadaiEngine.getUserService().createUserQuery().orgLevel1In(orgLevels).list();

    assertThat(users)
        .hasSize(ENVITE_USERS_COUNT - 1)
        .extracting(UserSummary::getOrgLevel1)
        .allSatisfy(orgLevel -> assertThat(orgLevels).contains(orgLevel));
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = ';',
      value = {
        "Human Workflow",
        "Human Workflow, non-existent",
        "Human Workflow, non-existent, non-existent2",
      })
  void should_ReturnExistingUsers_For_GivenOrgLevel2s(String orgLevel2s) {
    String[] orgLevels = orgLevel2s.split(",");

    List<UserSummary> users =
        kadaiEngine.getUserService().createUserQuery().orgLevel2In(orgLevels).list();

    assertThat(users)
        .hasSize(ENVITE_USERS_COUNT - 1)
        .extracting(UserSummary::getOrgLevel2)
        .allSatisfy(orgLevel -> assertThat(orgLevels).contains(orgLevel));
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = ';',
      value = {
        "BPM",
        "BPM, non-existent",
        "BPM, non-existent, non-existent2",
      })
  void should_ReturnExistingUsers_For_GivenOrgLevel3s(String orgLevel3s) {
    String[] orgLevels = orgLevel3s.split(",");

    List<UserSummary> users =
        kadaiEngine.getUserService().createUserQuery().orgLevel3In(orgLevels).list();

    assertThat(users)
        .hasSize(ENVITE_USERS_COUNT - 1)
        .extracting(UserSummary::getOrgLevel3)
        .allSatisfy(orgLevel -> assertThat(orgLevels).contains(orgLevel));
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = ';',
      value = {
        "Envite",
        "Envite, non-existent",
        "Envite, non-existent, non-existent2",
      })
  void should_ReturnExistingUsers_For_GivenOrgLevel4s(String orgLevel4s) {
    String[] orgLevels = orgLevel4s.split(",");

    List<UserSummary> users =
        kadaiEngine.getUserService().createUserQuery().orgLevel4In(orgLevels).list();

    assertThat(users)
        .hasSize(ENVITE_USERS_COUNT - 1)
        .extracting(UserSummary::getOrgLevel4)
        .allSatisfy(orgLevel -> assertThat(orgLevels).contains(orgLevel));
  }

  @Test
  void should_ReturnExistingUsers_For_CombinationOfSameOrgLevel() {
    List<UserSummary> users =
        kadaiEngine.getUserService().createUserQuery().orgLevel4In("Envite", "Foo").list();

    assertThat(users)
        .hasSize(ENVITE_USERS_COUNT)
        .extracting(UserSummary::getOrgLevel4)
        .allSatisfy(orgLevel -> assertThat(orgLevel).isIn("Envite", "Foo"));
  }

  @Test
  void should_ReturnExistingUsers_For_CombinationOfDifferentOrgLevelsWithNonEmptyCut() {
    List<UserSummary> users =
        kadaiEngine
            .getUserService()
            .createUserQuery()
            .orgLevel4In("Envite")
            .orgLevel3In("BPM")
            .list();

    assertThat(users).hasSize(ENVITE_USERS_COUNT - 1);
    assertThat(users)
        .extracting(UserSummary::getOrgLevel4)
        .allSatisfy(orgLevel -> assertThat(orgLevel).isEqualTo("Envite"));
    assertThat(users)
        .extracting(UserSummary::getOrgLevel3)
        .allSatisfy(orgLevel -> assertThat(orgLevel).isEqualTo("BPM"));
  }

  @Test
  void should_ReturnNoUsers_For_CombinationOfDifferentOrgLevelsWithEmptyCut() {
    List<UserSummary> users =
        kadaiEngine
            .getUserService()
            .createUserQuery()
            .orgLevel4In("Envite")
            .orgLevel3In("Bar")
            .list();

    assertThat(users).isEmpty();
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = ';',
      value = {"user-1-1", "user-1-1,user-1-2", "user-1-1,user-1-2,teamlead-1"})
  void should_ReturnExistingUsers_For_CombinationsOfUserIdsAndOrgLevelWithNonEmptyCut(
      String userIdsString) {
    String[] userIds = userIdsString.split(",");

    List<UserSummary> users =
        kadaiEngine.getUserService().createUserQuery().idIn(userIds).orgLevel1In("KADAI").list();

    assertThat(users)
        .hasSize(userIds.length)
        .extracting(UserSummary::getId)
        .containsExactlyInAnyOrder(userIds);
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = ';',
      value = {"user-1-1", "user-1-1,user-1-2", "user-1-1,user-1-2,teamlead-1"})
  void should_ReturnExistingUsers_For_CombinationOfsUserIdsAndOrgLevelWithEmptyCut(
      String userIdsString) {
    String[] userIds = userIdsString.split(",");

    List<UserSummary> users =
        kadaiEngine.getUserService().createUserQuery().idIn(userIds).orgLevel1In("Bat").list();

    assertThat(users).isEmpty();
  }
}
