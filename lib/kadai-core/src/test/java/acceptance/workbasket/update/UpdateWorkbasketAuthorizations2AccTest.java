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

package acceptance.workbasket.update;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import acceptance.AbstractAccTest;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.common.test.security.WithAccessId;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.models.WorkbasketAccessItem;
import io.kadai.workbasket.internal.models.WorkbasketAccessItemImpl;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Acceptance test for all "update workbasket" scenarios that need a fresh database. */
@ExtendWith(JaasExtension.class)
class UpdateWorkbasketAuthorizations2AccTest extends AbstractAccTest {

  private static final WorkbasketService WORKBASKET_SERVICE = kadaiEngine.getWorkbasketService();

  @WithAccessId(user = "businessadmin")
  @Test
  void testUpdatedAccessItemListToEmptyList() throws Exception {
    final String wbId = "WBI:100000000000000000000000000000000004";
    List<WorkbasketAccessItem> accessItems = WORKBASKET_SERVICE.getWorkbasketAccessItems(wbId);
    assertThat(accessItems).hasSize(3);

    WORKBASKET_SERVICE.setWorkbasketAccessItems(wbId, List.of());

    List<WorkbasketAccessItem> updatedAccessItems =
        WORKBASKET_SERVICE.getWorkbasketAccessItems(wbId);
    assertThat(updatedAccessItems).isEmpty();
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void testUpdatedAccessItemList_accessId_null() throws Exception {
    final String wbId = "WBI:100000000000000000000000000000000002";
    List<WorkbasketAccessItem> accessItems = WORKBASKET_SERVICE.getWorkbasketAccessItems(wbId);
    assertThat(accessItems).hasSize(1);

    WorkbasketAccessItemImpl workbasketAccessItem = ((WorkbasketAccessItemImpl) accessItems.get(0));
    workbasketAccessItem.setAccessId(null);
    List<WorkbasketAccessItem> workbasketAccessItems = List.of(workbasketAccessItem);

    assertThatThrownBy(
            () -> WORKBASKET_SERVICE.setWorkbasketAccessItems(wbId, workbasketAccessItems))
        .isInstanceOf(InvalidArgumentException.class)
        .hasMessageContaining("accessId is null or empty");
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void testUpdatedAccessItemList_accessId_blank() throws Exception {
    final String wbId = "WBI:100000000000000000000000000000000003";
    List<WorkbasketAccessItem> accessItems = WORKBASKET_SERVICE.getWorkbasketAccessItems(wbId);
    assertThat(accessItems).hasSize(1);

    WorkbasketAccessItemImpl workbasketAccessItem = ((WorkbasketAccessItemImpl) accessItems.get(0));
    workbasketAccessItem.setAccessId("   ");
    List<WorkbasketAccessItem> workbasketAccessItems = List.of(workbasketAccessItem);

    assertThatThrownBy(
            () -> WORKBASKET_SERVICE.setWorkbasketAccessItems(wbId, workbasketAccessItems))
        .isInstanceOf(InvalidArgumentException.class)
        .hasMessageContaining("accessId is null or empty");
  }
}
