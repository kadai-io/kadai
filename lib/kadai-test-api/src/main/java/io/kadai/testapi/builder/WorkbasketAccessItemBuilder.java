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

import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.workbasket.api.WorkbasketPermission;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.exceptions.WorkbasketAccessItemAlreadyExistException;
import io.kadai.workbasket.api.exceptions.WorkbasketNotFoundException;
import io.kadai.workbasket.api.models.WorkbasketAccessItem;
import io.kadai.workbasket.internal.models.WorkbasketAccessItemImpl;

public class WorkbasketAccessItemBuilder
    implements EntityBuilder<WorkbasketAccessItem, WorkbasketService> {

  WorkbasketAccessItemImpl testWorkbasketAccessItem = new WorkbasketAccessItemImpl();

  private WorkbasketAccessItemBuilder() {}

  public static WorkbasketAccessItemBuilder newWorkbasketAccessItem() {
    return new WorkbasketAccessItemBuilder();
  }

  public WorkbasketAccessItemBuilder workbasketId(String workbasketId) {
    testWorkbasketAccessItem.setWorkbasketId(workbasketId);
    return this;
  }

  public WorkbasketAccessItemBuilder accessId(String accessId) {
    testWorkbasketAccessItem.setAccessId(accessId);
    return this;
  }

  public WorkbasketAccessItemBuilder accessName(String accessName) {
    testWorkbasketAccessItem.setAccessName(accessName);
    return this;
  }

  public WorkbasketAccessItemBuilder permission(WorkbasketPermission permission) {
    return permission(permission, true);
  }

  public WorkbasketAccessItemBuilder permission(WorkbasketPermission permission, boolean value) {
    testWorkbasketAccessItem.setPermission(permission, value);
    return this;
  }

  @Override
  public WorkbasketAccessItem buildAndStore(WorkbasketService workbasketService)
      throws InvalidArgumentException,
          WorkbasketAccessItemAlreadyExistException,
          WorkbasketNotFoundException,
          NotAuthorizedException {
    return workbasketService.createWorkbasketAccessItem(testWorkbasketAccessItem);
  }

  @Override
  public WorkbasketAccessItem build() {
    return testWorkbasketAccessItem.copy();
  }
}
