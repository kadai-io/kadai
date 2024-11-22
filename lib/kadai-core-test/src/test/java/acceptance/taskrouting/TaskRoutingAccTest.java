/*
 * Copyright [2024] [envite consulting GmbH]
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

package acceptance.taskrouting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import io.kadai.KadaiConfiguration.Builder;
import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.spi.routing.api.RoutingTarget;
import io.kadai.spi.routing.api.TaskRoutingProvider;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.models.Task;
import io.kadai.testapi.DefaultTestEntities;
import io.kadai.testapi.KadaiConfigurationModifier;
import io.kadai.testapi.KadaiInject;
import io.kadai.testapi.KadaiIntegrationTest;
import io.kadai.testapi.WithServiceProvider;
import io.kadai.testapi.builder.WorkbasketAccessItemBuilder;
import io.kadai.testapi.security.WithAccessId;
import io.kadai.workbasket.api.WorkbasketPermission;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@KadaiIntegrationTest
class TaskRoutingAccTest {

  ClassificationSummary classificationSummary;
  WorkbasketSummary domainAWorkbasket;

  @WithAccessId(user = "businessadmin")
  @BeforeAll
  void setUp(ClassificationService classificationService, WorkbasketService workbasketService)
      throws Exception {
    classificationSummary =
        DefaultTestEntities.defaultTestClassification()
            .buildAndStoreAsSummary(classificationService);
    domainAWorkbasket =
        DefaultTestEntities.defaultTestWorkbasket()
            .key("DOMAIN_A_WORKBASKET")
            .buildAndStoreAsSummary(workbasketService);

    WorkbasketAccessItemBuilder.newWorkbasketAccessItem()
        .workbasketId(domainAWorkbasket.getId())
        .accessId("user-1-1")
        .permission(WorkbasketPermission.OPEN)
        .permission(WorkbasketPermission.READ)
        .permission(WorkbasketPermission.APPEND)
        .buildAndStore(workbasketService);
  }

  class TaskRoutingProviderForDomainA implements TaskRoutingProvider {

    @Override
    public void initialize(KadaiEngine kadaiEngine) {
    }

    @Override
    public String determineWorkbasketId(Task task) {
      if ("DOMAIN_A".equals(task.getDomain())) {
        return domainAWorkbasket.getId();
      }
      return null;
    }
  }

  class TaskRoutingProviderForRoutingTargetWithOwner implements TaskRoutingProvider {

    @Override
    public void initialize(KadaiEngine kadaiEngine) {
    }

    @Override
    public Optional<RoutingTarget> determineRoutingTarget(Task task) {
      if ("DOMAIN_A".equals(task.getDomain())) {
        RoutingTarget routingTarget = new RoutingTarget(domainAWorkbasket.getId(),
            "user-1-1");
        return Optional.of(routingTarget);
      }
      return Optional.empty();
    }
  }

  class TaskRoutingProviderForRoutingTargetWithoutOwner implements TaskRoutingProvider {

    @Override
    public void initialize(KadaiEngine kadaiEngine) {
    }

    @Override
    public Optional<RoutingTarget> determineRoutingTarget(Task task) {
      if ("DOMAIN_A".equals(task.getDomain())) {
        RoutingTarget routingTarget = new RoutingTarget(domainAWorkbasket.getId());
        return Optional.of(routingTarget);
      }
      return Optional.empty();
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  @WithServiceProvider(
      serviceProviderInterface = TaskRoutingProvider.class,
      serviceProviders = TaskRoutingProviderForDomainA.class)
  class DetermineWorkbasketIdWithServiceProvider {

    @KadaiInject
    TaskService taskService;

    @WithAccessId(user = "user-1-1")
    @Test
    void should_ThrowException_When_TaskRouterDoesNotRouteTask() {
      Task task = taskService.newTask();
      task.setClassificationKey(classificationSummary.getKey());
      task.setPrimaryObjRef(DefaultTestEntities.defaultTestObjectReference().build());

      assertThatThrownBy(() -> taskService.createTask(task))
          .isInstanceOf(InvalidArgumentException.class)
          .hasMessage("Cannot create a Task outside a Workbasket");
    }

    @WithAccessId(user = "user-1-1")
    @Test
    void should_SetWorkbasketForTask_When_TaskRouterDeterminesWorkbasket() throws Exception {
      Task task = taskService.newTask(null, "DOMAIN_A");
      task.setClassificationKey(classificationSummary.getKey());
      task.setPrimaryObjRef(DefaultTestEntities.defaultTestObjectReference().build());

      Task createdTask = taskService.createTask(task);

      assertThat(createdTask.getWorkbasketSummary()).isEqualTo(domainAWorkbasket);
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @WithServiceProvider(
        serviceProviderInterface = TaskRoutingProvider.class,
        serviceProviders = TaskRoutingProviderForRoutingTargetWithOwner.class)
    class RouteTasksUsingRoutingTargetWithOwner implements KadaiConfigurationModifier {

      @KadaiInject TaskService taskService;

      @Override
      public Builder modify(Builder builder) {
        return builder.includeOwnerWhenRouting(true);
      }

      @WithAccessId(user = "user-1-1")
      @Test
      void should_ThrowException_When_TaskRouterReturnsEmptyRoutingTarget() {
        Task task = taskService.newTask();
        task.setClassificationKey(classificationSummary.getKey());
        task.setPrimaryObjRef(DefaultTestEntities.defaultTestObjectReference().build());

        assertThatThrownBy(() -> taskService.createTask(task))
            .isInstanceOf(InvalidArgumentException.class)
            .hasMessage("Cannot create a Task in an empty RoutingTarget");
      }

      @WithAccessId(user = "user-1-1")
      @Test
      void should_SetWorkbasketAndOwnerForTask_When_TaskRouterDeterminesRoutingTarget()
          throws Exception {
        Task task = taskService.newTask(null, "DOMAIN_A");
        task.setOwner("user-1-2");
        task.setClassificationKey(classificationSummary.getKey());
        task.setPrimaryObjRef(DefaultTestEntities.defaultTestObjectReference().build());

        Task createdTask = taskService.createTask(task);

        assertThat(createdTask.getWorkbasketSummary()).isEqualTo(domainAWorkbasket);
        assertThat(createdTask.getOwner()).isEqualTo("user-1-1");

      }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @WithServiceProvider(
        serviceProviderInterface = TaskRoutingProvider.class,
        serviceProviders = TaskRoutingProviderForRoutingTargetWithoutOwner.class)
    class RouteTasksUsingRoutingTargetWithoutOwner implements KadaiConfigurationModifier {

      @KadaiInject TaskService taskService;

      @Override
      public Builder modify(Builder builder) {
        return builder.includeOwnerWhenRouting(true);
      }

      @WithAccessId(user = "user-1-1")
      @Test
      void should_ThrowException_When_TaskRouterReturnsEmptyRoutingTarget() {
        Task task = taskService.newTask();
        task.setClassificationKey(classificationSummary.getKey());
        task.setPrimaryObjRef(DefaultTestEntities.defaultTestObjectReference().build());

        assertThatThrownBy(() -> taskService.createTask(task))
            .isInstanceOf(InvalidArgumentException.class)
            .hasMessage("Cannot create a Task in an empty RoutingTarget");
      }

      @WithAccessId(user = "user-1-1")
      @Test
      void should_SetWorkbasketAndKeepOwnerForTask_When_TaskRouterDeterminesRoutingTarget()
          throws Exception {
        Task task = taskService.newTask(null, "DOMAIN_A");
        task.setOwner("user-1-2");
        task.setClassificationKey(classificationSummary.getKey());
        task.setPrimaryObjRef(DefaultTestEntities.defaultTestObjectReference().build());

        Task createdTask = taskService.createTask(task);

        assertThat(createdTask.getWorkbasketSummary()).isEqualTo(domainAWorkbasket);
        assertThat(createdTask.getOwner()).isEqualTo("user-1-2");

      }
    }
  }
}
