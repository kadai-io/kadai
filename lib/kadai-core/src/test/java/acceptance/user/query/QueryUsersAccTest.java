package acceptance.user.query;

import static org.assertj.core.api.Assertions.assertThat;

import acceptance.AbstractAccTest;
import io.kadai.KadaiConfiguration;
import io.kadai.KadaiConfiguration.Builder;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.user.api.models.User;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(JaasExtension.class)
public class QueryUsersAccTest extends AbstractAccTest {

  private static final int ALL_USERS_COUNT = 14;

  private KadaiEngine kadaiEngine;

  @BeforeEach
  public void setupKadaiEngine() throws Exception {
    KadaiConfiguration kadaiConfiguration =
        new Builder(AbstractAccTest.kadaiConfiguration).addAdditionalUserInfo(false).build();
    this.kadaiEngine = KadaiEngine.buildKadaiEngine(kadaiConfiguration);
  }

  @Test
  void should_ReturnAllUsers_When_QueryHasNoConstraints() {
    List<User> users = kadaiEngine.getUserService().createUserQuery().list();

    assertThat(users).hasSize(ALL_USERS_COUNT);
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = ';',
      value = {"user-1-1", "user-1-1,user-1-2", "user-1-1,user-1-2,teamlead-1"})
  void should_ReturnExistingUsers_For_GivenIds(String userIdsString) {
    String[] userIds = userIdsString.split(",");

    List<User> users = kadaiEngine.getUserService().createUserQuery().idIn(userIds).list();

    assertThat(users)
        .hasSize(userIds.length)
        .extracting(User::getId)
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

    List<User> users = kadaiEngine.getUserService().createUserQuery().orgLevel1In(orgLevels).list();

    assertThat(users)
        .hasSize(ALL_USERS_COUNT - 1)
        .extracting(User::getOrgLevel1)
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

    List<User> users = kadaiEngine.getUserService().createUserQuery().orgLevel2In(orgLevels).list();

    assertThat(users)
        .hasSize(ALL_USERS_COUNT - 1)
        .extracting(User::getOrgLevel2)
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

    List<User> users = kadaiEngine.getUserService().createUserQuery().orgLevel3In(orgLevels).list();

    assertThat(users)
        .hasSize(ALL_USERS_COUNT - 1)
        .extracting(User::getOrgLevel3)
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

    List<User> users = kadaiEngine.getUserService().createUserQuery().orgLevel4In(orgLevels).list();

    assertThat(users)
        .hasSize(ALL_USERS_COUNT - 1)
        .extracting(User::getOrgLevel4)
        .allSatisfy(orgLevel -> assertThat(orgLevels).contains(orgLevel));
  }

  @Test
  void should_ReturnExistingUsers_For_CombinationOfSameOrgLevel() {
    List<User> users = kadaiEngine.getUserService()
        .createUserQuery()
        .orgLevel4In("Envite", "Foo")
        .list();

    assertThat(users)
        .hasSize(ALL_USERS_COUNT)
        .extracting(User::getOrgLevel4)
        .allSatisfy(orgLevel -> assertThat(orgLevel).isIn("Envite", "Foo"));
  }

  @Test
  void should_ReturnExistingUsers_For_CombinationOfDifferentOrgLevelsWithNonEmptyCut() {
    List<User> users = kadaiEngine.getUserService()
        .createUserQuery()
        .orgLevel4In("Envite")
        .orgLevel3In("BPM")
        .list();

    assertThat(users).hasSize(ALL_USERS_COUNT - 1);
    assertThat(users).extracting(User::getOrgLevel4)
        .allSatisfy(orgLevel -> assertThat(orgLevel).isEqualTo("Envite"));
    assertThat(users).extracting(User::getOrgLevel3)
        .allSatisfy(orgLevel -> assertThat(orgLevel).isEqualTo("BPM"));
  }

  @Test
  void should_ReturnNoUsers_For_CombinationOfDifferentOrgLevelsWithEmptyCut() {
    List<User> users = kadaiEngine.getUserService()
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

    List<User> users = kadaiEngine.getUserService()
        .createUserQuery()
        .idIn(userIds)
        .orgLevel1In("KADAI")
        .list();

    assertThat(users)
        .hasSize(userIds.length)
        .extracting(User::getId)
        .containsExactlyInAnyOrder(userIds);
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = ';',
      value = {"user-1-1", "user-1-1,user-1-2", "user-1-1,user-1-2,teamlead-1"})
  void should_ReturnExistingUsers_For_CombinationOfsUserIdsAndOrgLevelWithEmptyCut(
      String userIdsString) {
    String[] userIds = userIdsString.split(",");

    List<User> users = kadaiEngine.getUserService()
        .createUserQuery()
        .idIn(userIds)
        .orgLevel1In("Bat")
        .list();

    assertThat(users).isEmpty();
  }
}
