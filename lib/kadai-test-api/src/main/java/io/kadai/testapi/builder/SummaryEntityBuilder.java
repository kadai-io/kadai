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

import io.kadai.user.api.models.User;

/**
 * Interface specifying how to build and store summary-entities in the database for integration
 * tests.
 *
 * @param <SummaryEntityT> the type of the summary-entity to build and store
 * @param <EntityT> the type of entity to build and store
 * @param <ServiceT> the type of the service for storing the entity
 */
interface SummaryEntityBuilder<SummaryEntityT, EntityT extends SummaryEntityT, ServiceT>
    extends EntityBuilder<EntityT, ServiceT> {

  /**
   * Converts an {@linkplain EntityT entity} to it's {@linkplain SummaryEntityT summary}.
   *
   * @param entity the entity to convert
   * @return the summary of the entity
   */
  SummaryEntityT entityToSummary(EntityT entity);

  /**
   * Builds the {@linkplain SummaryEntityT summary} for this builder and stores it in the
   * database.
   *
   * @param service the {@linkplain ServiceT service} storing the built summary
   * @return the stored summary
   * @throws Exception if building or storing the summary fails
   */
  default SummaryEntityT buildAndStoreAsSummary(ServiceT service) throws Exception {
    return entityToSummary(buildAndStore(service));
  }

  /**
   * Builds the {@linkplain SummaryEntityT summary} for this builder and stores it in the
   * database.
   *
   * @param service the {@linkplain ServiceT service} storing the built summary
   * @param userId the {@linkplain User#getId() id} of the user to store the built summary as
   * @return the stored summary
   * @throws Exception if building or storing the summary fails
   */
  default SummaryEntityT buildAndStoreAsSummary(ServiceT service, String userId)
      throws Exception {
    return entityToSummary(buildAndStore(service, userId));
  }
}
