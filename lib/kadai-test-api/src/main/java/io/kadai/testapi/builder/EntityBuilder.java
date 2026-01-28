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

package io.kadai.testapi.builder;

import io.kadai.common.api.security.UserPrincipal;
import io.kadai.user.api.models.User;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import javax.security.auth.Subject;

/**
 * Interface specifying how to build and store entities in the database for integration tests.
 *
 * @param <EntityT> the type of entity to build and store
 * @param <ServiceT> the type of the service for storing the entity
 */
public interface EntityBuilder<EntityT, ServiceT> extends Builder<EntityT> {

  /**
   * Builds the {@linkplain EntityT entity} for this builder and stores it in the database.
   *
   * @param service the {@linkplain ServiceT service} storing the built entity
   * @return the stored entity
   * @throws Exception if building or storing the entity fails
   */
  EntityT buildAndStore(ServiceT service) throws Exception;

  /**
   * Builds the {@linkplain EntityT entity} for this builder and stores it in the database.
   *
   * @param service the {@linkplain ServiceT service} storing the built entity
   * @param userId the {@linkplain User#getId() id} of the user to store the built entity as
   * @return the stored entity
   * @throws Exception if building or storing the entity fails
   */
  default EntityT buildAndStore(ServiceT service, String userId) throws Exception {
    return execAsUser(userId, () -> buildAndStore(service));
  }

  private <T> T execAsUser(String userId, PrivilegedExceptionAction<T> runnable)
      throws PrivilegedActionException {
    Subject subject = new Subject();
    subject.getPrincipals().add(new UserPrincipal(userId));

    return Subject.doAs(subject, runnable);
  }
}
