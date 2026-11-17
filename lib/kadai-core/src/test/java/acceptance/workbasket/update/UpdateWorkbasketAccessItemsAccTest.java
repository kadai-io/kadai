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

package acceptance.workbasket.update;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import acceptance.AbstractAccTest;
import io.kadai.common.api.exceptions.DomainNotFoundException;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.LogicalDuplicateInPayloadException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.common.test.security.WithAccessId;
import io.kadai.workbasket.api.WorkbasketPermission;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.WorkbasketType;
import io.kadai.workbasket.api.exceptions.WorkbasketAccessItemAlreadyExistException;
import io.kadai.workbasket.api.exceptions.WorkbasketAlreadyExistException;
import io.kadai.workbasket.api.exceptions.WorkbasketNotFoundException;
import io.kadai.workbasket.api.models.Workbasket;
import io.kadai.workbasket.api.models.WorkbasketAccessItem;
import io.kadai.workbasket.internal.models.WorkbasketAccessItemImpl;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Acceptance tests for WorkbasketAccessItem collection replacement. */
@ExtendWith(JaasExtension.class)
class UpdateWorkbasketAccessItemsAccTest extends AbstractAccTest {

  @WithAccessId(user = "businessadmin")
  @Test
  void should_KeepOwnedItemIdentity_When_UpdatingPermissions() throws Exception {
    WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
    Workbasket workbasket = createWorkbasket(workbasketService);
    final WorkbasketAccessItem initial =
        createAccessItem(workbasketService, workbasket, "access-" + UUID.randomUUID());
    WorkbasketAccessItem update =
        workbasketService.getWorkbasketAccessItems(workbasket.getId()).getFirst();
    update.setPermission(WorkbasketPermission.READ, true);
    update.setAccessName("Updated access name");

    workbasketService.setWorkbasketAccessItems(workbasket.getId(), List.of(update));

    WorkbasketAccessItem persisted =
        workbasketService.getWorkbasketAccessItems(workbasket.getId()).getFirst();
    assertThat(persisted.getId()).isEqualTo(initial.getId());
    assertThat(persisted.getPermission(WorkbasketPermission.READ)).isTrue();
    assertThat(persisted.getAccessName()).isEqualTo("Updated access name");
    assertThat(persisted.getWorkbasketKey()).isEqualTo(workbasket.getKey());
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void should_RejectChangedLogicalIdentity_AndLeaveCollectionUnchanged() throws Exception {
    WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
    Workbasket workbasket = createWorkbasket(workbasketService);
    WorkbasketAccessItem initial =
        createAccessItem(workbasketService, workbasket, "access-" + UUID.randomUUID());
    WorkbasketAccessItemImpl changedIdentity = copyWithId(initial);
    changedIdentity.setAccessId("different-" + UUID.randomUUID());
    changedIdentity.setPermission(WorkbasketPermission.OPEN, true);
    String workbasketId = workbasket.getId();
    List<WorkbasketAccessItem> changedItems = List.of(changedIdentity);

    assertThatThrownBy(
            () -> workbasketService.setWorkbasketAccessItems(workbasketId, changedItems))
        .isInstanceOf(InvalidArgumentException.class);

    assertThat(workbasketService.getWorkbasketAccessItems(workbasketId))
        .containsExactly(initial);
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void should_RejectUnknownAndForeignIdsBeforeReplacingAnyRows() throws Exception {
    WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
    Workbasket target = createWorkbasket(workbasketService);
    Workbasket other = createWorkbasket(workbasketService);
    WorkbasketAccessItem targetItem =
        createAccessItem(workbasketService, target, "access-" + UUID.randomUUID());
    final WorkbasketAccessItem otherItem =
        createAccessItem(workbasketService, other, "access-" + UUID.randomUUID());
    WorkbasketAccessItemImpl validUpdate = copyWithId(targetItem);
    validUpdate.setPermission(WorkbasketPermission.OPEN, true);
    WorkbasketAccessItemImpl unknownItem =
        accessItem(workbasketService, target.getId(), "new-" + UUID.randomUUID());
    unknownItem.setId("unknown-access-item-id");
    String targetId = target.getId();
    List<WorkbasketAccessItem> itemsWithUnknownId = List.of(validUpdate, unknownItem);

    assertThatThrownBy(
            () -> workbasketService.setWorkbasketAccessItems(targetId, itemsWithUnknownId))
        .isInstanceOf(InvalidArgumentException.class);

    WorkbasketAccessItemImpl foreignItem = copyWithId(otherItem);
    foreignItem.setWorkbasketId(target.getId());
    foreignItem.setAccessId("foreign-" + UUID.randomUUID());
    List<WorkbasketAccessItem> foreignItems = List.of(foreignItem);
    assertThatThrownBy(
            () -> workbasketService.setWorkbasketAccessItems(targetId, foreignItems))
        .isInstanceOf(InvalidArgumentException.class);

    assertThat(workbasketService.getWorkbasketAccessItems(targetId))
        .containsExactly(targetItem);
    assertThat(workbasketService.getWorkbasketAccessItems(other.getId()))
        .containsExactly(otherItem);
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void should_RejectRepeatedItemIdsAndNormalizedDuplicateAccessIds() throws Exception {
    WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
    Workbasket workbasket = createWorkbasket(workbasketService);
    WorkbasketAccessItem initial =
        createAccessItem(workbasketService, workbasket, "access-" + UUID.randomUUID());
    WorkbasketAccessItemImpl first = copyWithId(initial);
    WorkbasketAccessItemImpl repeated = copyWithId(initial);
    repeated.setAccessId("different-" + UUID.randomUUID());
    String workbasketId = workbasket.getId();
    List<WorkbasketAccessItem> repeatedItems = List.of(first, repeated);

    assertThatThrownBy(
            () -> workbasketService.setWorkbasketAccessItems(workbasketId, repeatedItems))
        .isInstanceOf(InvalidArgumentException.class);

    WorkbasketAccessItemImpl uppercase =
        accessItem(workbasketService, workbasket.getId(), "CaseSensitive-" + UUID.randomUUID());
    String duplicateAccessId = "normalized-" + UUID.randomUUID();
    uppercase.setAccessId(duplicateAccessId.toUpperCase());
    WorkbasketAccessItemImpl lowercase =
        accessItem(workbasketService, workbasket.getId(), duplicateAccessId.toLowerCase());
    List<WorkbasketAccessItem> duplicateAccessItems = List.of(uppercase, lowercase);

    assertThatThrownBy(
            () -> workbasketService.setWorkbasketAccessItems(workbasketId, duplicateAccessItems))
        .isInstanceOf(LogicalDuplicateInPayloadException.class);
    assertThat(workbasketService.getWorkbasketAccessItems(workbasketId))
        .containsExactly(initial);
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void should_GenerateIdentityAndParentFieldsForNewItems_AndAllowEmptyReplacement()
      throws Exception {
    WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
    Workbasket workbasket = createWorkbasket(workbasketService);
    final Workbasket other = createWorkbasket(workbasketService);
    WorkbasketAccessItemImpl newItem =
        accessItem(workbasketService, null, "new-" + UUID.randomUUID());
    final String suppliedId = newItem.getId();
    newItem.setWorkbasketKey("caller-supplied-key");

    workbasketService.setWorkbasketAccessItems(workbasket.getId(), List.of(newItem));

    WorkbasketAccessItem persisted =
        workbasketService.getWorkbasketAccessItems(workbasket.getId()).getFirst();
    assertThat(persisted.getId()).isNotBlank();
    assertThat(persisted.getId()).isNotEqualTo(suppliedId);
    assertThat(persisted.getWorkbasketId()).isEqualTo(workbasket.getId());
    assertThat(persisted.getWorkbasketKey()).isEqualTo(workbasket.getKey());

    WorkbasketAccessItemImpl contradictoryParent =
        accessItem(workbasketService, other.getId(), "contradictory-" + UUID.randomUUID());
    String workbasketId = workbasket.getId();
    List<WorkbasketAccessItem> contradictoryItems = List.of(contradictoryParent);
    assertThatThrownBy(
            () -> workbasketService.setWorkbasketAccessItems(workbasketId, contradictoryItems))
        .isInstanceOf(InvalidArgumentException.class);
    assertThat(workbasketService.getWorkbasketAccessItems(workbasketId))
        .containsExactly(persisted);

    workbasketService.setWorkbasketAccessItems(workbasketId, List.of());
    assertThat(workbasketService.getWorkbasketAccessItems(workbasketId)).isEmpty();
  }

  private Workbasket createWorkbasket(WorkbasketService workbasketService)
      throws InvalidArgumentException,
          WorkbasketAlreadyExistException,
          DomainNotFoundException,
          NotAuthorizedException {
    Workbasket workbasket =
        workbasketService.newWorkbasket("access-items-" + UUID.randomUUID(), "DOMAIN_A");
    workbasket.setName("Access item test workbasket");
    workbasket.setType(WorkbasketType.GROUP);
    return workbasketService.createWorkbasket(workbasket);
  }

  private WorkbasketAccessItem createAccessItem(
      WorkbasketService workbasketService, Workbasket workbasket, String accessId)
      throws InvalidArgumentException,
          WorkbasketNotFoundException,
          WorkbasketAccessItemAlreadyExistException,
          NotAuthorizedException {
    return workbasketService.createWorkbasketAccessItem(
        workbasketService.newWorkbasketAccessItem(workbasket.getId(), accessId));
  }

  private WorkbasketAccessItemImpl accessItem(
      WorkbasketService workbasketService, String workbasketId, String accessId) {
    return (WorkbasketAccessItemImpl)
        workbasketService.newWorkbasketAccessItem(workbasketId, accessId);
  }

  private WorkbasketAccessItemImpl copyWithId(WorkbasketAccessItem workbasketAccessItem) {
    WorkbasketAccessItemImpl copy = (WorkbasketAccessItemImpl) workbasketAccessItem.copy();
    copy.setId(workbasketAccessItem.getId());
    return copy;
  }
}
