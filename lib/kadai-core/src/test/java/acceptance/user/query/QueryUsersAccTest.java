package acceptance.user.query;

import static org.assertj.core.api.Assertions.assertThat;

import acceptance.AbstractAccTest;
import io.kadai.KadaiConfiguration;
import io.kadai.KadaiConfiguration.Builder;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.user.api.models.User;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(JaasExtension.class)
public class QueryUsersAccTest extends AbstractAccTest {

  private static final int ALL_USERS_COUNT = 14;

  @Test
  void should_ReturnAllUsers_When_QueryHasNoConstraints() throws Exception {
    KadaiConfiguration kadaiConfiguration =
        new Builder(AbstractAccTest.kadaiConfiguration).addAdditionalUserInfo(false).build();
    KadaiEngine kadaiEngine = KadaiEngine.buildKadaiEngine(kadaiConfiguration);

    List<User> users = kadaiEngine.getUserService().createUserQuery().list();

    assertThat(users).hasSize(ALL_USERS_COUNT);
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = ';',
      value = {"user-1-1", "user-1-1,user-1-2", "user-1-1,user-1-2,teamlead-1"})
  void should_ReturnExistingUsers_For_GivenIds(String userIdsString) throws Exception {
    String[] userIds = userIdsString.split(",");

    KadaiConfiguration kadaiConfiguration =
        new Builder(AbstractAccTest.kadaiConfiguration).addAdditionalUserInfo(false).build();
    KadaiEngine kadaiEngine = KadaiEngine.buildKadaiEngine(kadaiConfiguration);

    List<User> users = kadaiEngine.getUserService().createUserQuery().idIn(userIds).list();

    assertThat(users).allSatisfy(user -> assertThat(userIds).contains(user.getId()));
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = ';',
      value = {
        "KADAI",
        "KADAI, non-existent",
        "KADAI, non-existent, non-existent2",
      })
  void should_ReturnAllUsers_For_GivenOrgLevel1s(String orgLevel1s) throws Exception {
    String[] orgLevels = orgLevel1s.split(",");

    KadaiConfiguration kadaiConfiguration =
        new Builder(AbstractAccTest.kadaiConfiguration).addAdditionalUserInfo(false).build();
    KadaiEngine kadaiEngine = KadaiEngine.buildKadaiEngine(kadaiConfiguration);

    List<User> users = kadaiEngine.getUserService().createUserQuery().orgLevel1In(orgLevels).list();

    assertThat(users).allSatisfy(user -> assertThat(orgLevels).contains(user.getOrgLevel1()));
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = ';',
      value = {
        "KADAI",
        "KADAI, non-existent",
        "KADAI, non-existent, non-existent2",
      })
  void should_ReturnAllUsers_For_GivenOrgLevel2s(String orgLevel2s) throws Exception {
    String[] orgLevels = orgLevel2s.split(",");

    KadaiConfiguration kadaiConfiguration =
        new Builder(AbstractAccTest.kadaiConfiguration).addAdditionalUserInfo(false).build();
    KadaiEngine kadaiEngine = KadaiEngine.buildKadaiEngine(kadaiConfiguration);

    List<User> users = kadaiEngine.getUserService().createUserQuery().orgLevel2In(orgLevels).list();

    assertThat(users).allSatisfy(user -> assertThat(orgLevels).contains(user.getOrgLevel2()));
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = ';',
      value = {
        "KADAI",
        "KADAI, non-existent",
        "KADAI, non-existent, non-existent2",
      })
  void should_ReturnAllUsers_For_GivenOrgLevel3s(String orgLevel3s) throws Exception {
    String[] orgLevels = orgLevel3s.split(",");

    KadaiConfiguration kadaiConfiguration =
        new Builder(AbstractAccTest.kadaiConfiguration).addAdditionalUserInfo(false).build();
    KadaiEngine kadaiEngine = KadaiEngine.buildKadaiEngine(kadaiConfiguration);

    List<User> users = kadaiEngine.getUserService().createUserQuery().orgLevel3In(orgLevels).list();

    assertThat(users).allSatisfy(user -> assertThat(orgLevels).contains(user.getOrgLevel3()));
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = ';',
      value = {
        "KADAI",
        "KADAI, non-existent",
        "KADAI, non-existent, non-existent2",
      })
  void should_ReturnAllUsers_For_GivenOrgLevel4s(String orgLevel4s) throws Exception {
    String[] orgLevels = orgLevel4s.split(",");

    KadaiConfiguration kadaiConfiguration =
        new Builder(AbstractAccTest.kadaiConfiguration).addAdditionalUserInfo(false).build();
    KadaiEngine kadaiEngine = KadaiEngine.buildKadaiEngine(kadaiConfiguration);

    List<User> users = kadaiEngine.getUserService().createUserQuery().orgLevel4In(orgLevels).list();

    assertThat(users).allSatisfy(user -> assertThat(orgLevels).contains(user.getOrgLevel4()));
  }
}
