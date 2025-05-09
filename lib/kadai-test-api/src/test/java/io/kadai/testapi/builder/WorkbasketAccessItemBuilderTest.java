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

package io.kadai.testapi.builder;

import static io.kadai.common.internal.util.CheckedSupplier.rethrowing;
import static io.kadai.testapi.DefaultTestEntities.defaultTestWorkbasket;
import static io.kadai.testapi.builder.WorkbasketAccessItemBuilder.newWorkbasketAccessItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import io.kadai.common.api.KadaiEngine;
import io.kadai.testapi.KadaiInject;
import io.kadai.testapi.KadaiIntegrationTest;
import io.kadai.testapi.security.WithAccessId;
import io.kadai.workbasket.api.WorkbasketPermission;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.models.Workbasket;
import io.kadai.workbasket.api.models.WorkbasketAccessItem;
import io.kadai.workbasket.internal.models.WorkbasketAccessItemImpl;
import java.util.List;
import org.junit.jupiter.api.Test;

@KadaiIntegrationTest
class WorkbasketAccessItemBuilderTest {

  @KadaiInject WorkbasketService workbasketService;
  @KadaiInject KadaiEngine kadaiEngine;

  @WithAccessId(user = "businessadmin")
  @Test
  void should_PersistWorkbasketAccessItem_When_UsingWorkbasketAccessItemBuilder() throws Exception {
    Workbasket workbasket = defaultTestWorkbasket().key("key0_F").buildAndStore(workbasketService);

    WorkbasketAccessItem workbasketAccessItem =
        newWorkbasketAccessItem()
            .workbasketId(workbasket.getId())
            .accessId("user-1-1")
            .permission(WorkbasketPermission.READ)
            .buildAndStore(workbasketService);

    List<WorkbasketAccessItem> workbasketAccessItems =
        workbasketService.getWorkbasketAccessItems(workbasket.getId());

    assertThat(workbasketAccessItems).containsExactly(workbasketAccessItem);
  }

  @Test
  void should_PersistWorkbasketAccessItemAsUser_When_UsingWorkbasketAccessItemBuilder()
      throws Exception {
    Workbasket workbasket =
        defaultTestWorkbasket().key("key1_F").buildAndStore(workbasketService, "businessadmin");

    WorkbasketAccessItem workbasketAccessItem =
        newWorkbasketAccessItem()
            .workbasketId(workbasket.getId())
            .accessId("user-1-1")
            .permission(WorkbasketPermission.READ)
            .buildAndStore(workbasketService, "businessadmin");

    List<WorkbasketAccessItem> workbasketAccessItems =
        kadaiEngine.runAsAdmin(
            rethrowing(() -> workbasketService.getWorkbasketAccessItems(workbasket.getId())));

    assertThat(workbasketAccessItems).containsExactly(workbasketAccessItem);
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void should_PopulateWorkbasketAccessItem_When_UsingEveryBuilderFunction() throws Exception {
    Workbasket workbasket = defaultTestWorkbasket().key("key2_F").buildAndStore(workbasketService);

    WorkbasketAccessItemImpl expectedWorkbasketAccessItem =
        (WorkbasketAccessItemImpl)
            workbasketService.newWorkbasketAccessItem(workbasket.getId(), "user-1-1");
    expectedWorkbasketAccessItem.setWorkbasketKey(workbasket.getKey());
    expectedWorkbasketAccessItem.setAccessName("Max Mustermann");
    expectedWorkbasketAccessItem.setPermission(WorkbasketPermission.READ, true);
    expectedWorkbasketAccessItem.setPermission(WorkbasketPermission.OPEN, true);
    expectedWorkbasketAccessItem.setPermission(WorkbasketPermission.APPEND, true);
    expectedWorkbasketAccessItem.setPermission(WorkbasketPermission.TRANSFER, true);
    expectedWorkbasketAccessItem.setPermission(WorkbasketPermission.DISTRIBUTE, true);
    expectedWorkbasketAccessItem.setPermission(WorkbasketPermission.CUSTOM_1, true);
    expectedWorkbasketAccessItem.setPermission(WorkbasketPermission.CUSTOM_2, true);
    expectedWorkbasketAccessItem.setPermission(WorkbasketPermission.CUSTOM_3, true);
    expectedWorkbasketAccessItem.setPermission(WorkbasketPermission.CUSTOM_4, true);
    expectedWorkbasketAccessItem.setPermission(WorkbasketPermission.CUSTOM_5, true);
    expectedWorkbasketAccessItem.setPermission(WorkbasketPermission.CUSTOM_6, true);
    expectedWorkbasketAccessItem.setPermission(WorkbasketPermission.CUSTOM_7, true);
    expectedWorkbasketAccessItem.setPermission(WorkbasketPermission.CUSTOM_8, true);
    expectedWorkbasketAccessItem.setPermission(WorkbasketPermission.CUSTOM_9, true);
    expectedWorkbasketAccessItem.setPermission(WorkbasketPermission.CUSTOM_10, true);
    expectedWorkbasketAccessItem.setPermission(WorkbasketPermission.CUSTOM_11, true);
    expectedWorkbasketAccessItem.setPermission(WorkbasketPermission.CUSTOM_12, true);

    WorkbasketAccessItem accessItem =
        newWorkbasketAccessItem()
            .workbasketId(workbasket.getId())
            .accessId("user-1-1")
            .accessName("Max Mustermann")
            .permission(WorkbasketPermission.READ)
            .permission(WorkbasketPermission.OPEN)
            .permission(WorkbasketPermission.APPEND)
            .permission(WorkbasketPermission.TRANSFER)
            .permission(WorkbasketPermission.DISTRIBUTE)
            .permission(WorkbasketPermission.CUSTOM_1)
            .permission(WorkbasketPermission.CUSTOM_2)
            .permission(WorkbasketPermission.CUSTOM_3)
            .permission(WorkbasketPermission.CUSTOM_4)
            .permission(WorkbasketPermission.CUSTOM_5)
            .permission(WorkbasketPermission.CUSTOM_6)
            .permission(WorkbasketPermission.CUSTOM_7)
            .permission(WorkbasketPermission.CUSTOM_8)
            .permission(WorkbasketPermission.CUSTOM_9)
            .permission(WorkbasketPermission.CUSTOM_10)
            .permission(WorkbasketPermission.CUSTOM_11)
            .permission(WorkbasketPermission.CUSTOM_12)
            .buildAndStore(workbasketService);

    assertThat(accessItem)
        .hasNoNullFieldsOrProperties()
        .usingRecursiveComparison()
        .ignoringFields("id")
        .isEqualTo(expectedWorkbasketAccessItem);
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void should_ResetClassificationId_When_StoringClassificationMultipleTimes() throws Exception {
    Workbasket workbasket = defaultTestWorkbasket().key("key3_F").buildAndStore(workbasketService);

    WorkbasketAccessItemBuilder workbasketAccessItemBuilder =
        newWorkbasketAccessItem()
            .workbasketId(workbasket.getId())
            .permission(WorkbasketPermission.READ);

    assertThatCode(
            () -> {
              workbasketAccessItemBuilder.accessId("hanspeter").buildAndStore(workbasketService);
              workbasketAccessItemBuilder.accessId("hanspeter2").buildAndStore(workbasketService);
            })
        .doesNotThrowAnyException();
  }
}
