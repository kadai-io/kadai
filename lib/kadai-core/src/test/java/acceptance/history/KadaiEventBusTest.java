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

package acceptance.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import acceptance.AbstractAccTest;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.KadaiRole;
import io.kadai.spi.history.api.KadaiEventConsumer;
import io.kadai.spi.history.api.events.KadaiEvent;
import io.kadai.spi.history.api.events.task.TaskCreatedEvent;
import io.kadai.spi.history.api.events.task.TaskHistoryEvent;
import io.kadai.spi.history.internal.KadaiEventBus;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Acceptance test for KadaiEventBus class. */
class KadaiEventBusTest extends AbstractAccTest {

  @Test
  void testKadaiEventBrokerIsNotEnabled() {
    assertThat(kadaiEngine.isHistoryEnabled()).isFalse();
  }

  @Test
  void should_AddConsumer_When_Subscribes() {
    final KadaiEventBus eventBus = new KadaiEventBus(kadaiEngine);
    final var taskConsumer = new TaskHistoryEventConsumer();

    eventBus.subscribes(taskConsumer);
    final var consumers = eventBus.getConsumers(TaskHistoryEvent.class);

    assertThat(consumers).containsExactly(taskConsumer);
  }

  @Test
  void should_SetIsEnabledTrue_When_Subscribes() {
    final KadaiEventBus eventBus = new KadaiEventBus(kadaiEngine);
    final var taskConsumer = new TaskHistoryEventConsumer();

    eventBus.subscribes(taskConsumer);

    assertThat(eventBus.isEnabled()).isTrue();
  }

  @Test
  void should_RemoveConsumer_When_Unsubscribes() {
    final KadaiEventBus eventBus = new KadaiEventBus(kadaiEngine);
    final var taskConsumer = new TaskHistoryEventConsumer();
    eventBus.subscribes(taskConsumer);

    eventBus.unsubscribes(taskConsumer);
    final var consumers = eventBus.getConsumers(TaskHistoryEvent.class);

    assertThat(consumers).isEmpty();
  }

  @Test
  void should_SetIsEnabledFalse_When_LastConsumerUnsubscribes() {
    final KadaiEventBus eventBus = new KadaiEventBus(kadaiEngine);
    final var taskConsumer = new TaskHistoryEventConsumer();
    eventBus.subscribes(taskConsumer);

    eventBus.unsubscribes(taskConsumer);

    assertThat(eventBus.isEnabled()).isFalse();
  }

  @Test
  void should_KeepIsEnabledTrue_When_ConsumerUnsubscribesAndOtherConsumersRemain() {
    final KadaiEventBus eventBus = new KadaiEventBus(kadaiEngine);
    final var taskConsumer = new TaskHistoryEventConsumer();
    eventBus.subscribes(taskConsumer);
    eventBus.subscribes(new TaskCreatedEventConsumer());

    eventBus.unsubscribes(taskConsumer);

    assertThat(eventBus.isEnabled()).isTrue();
  }

  @Test
  void should_ReturnAllMostSpecificAndMoreGeneralConsumers_For_SpecializedEvent() {
    final KadaiEventBus eventBus = new KadaiEventBus(kadaiEngine);
    final var taskConsumer = new TaskHistoryEventConsumer();
    final var taskCreatedConsumer = new TaskCreatedEventConsumer();
    eventBus.subscribes(taskConsumer);
    eventBus.subscribes(taskCreatedConsumer);

    final List<KadaiEventConsumer<? super TaskCreatedEvent>> retrieved =
        eventBus.getConsumers(TaskCreatedEvent.class);

    assertThat(retrieved).containsExactlyInAnyOrder(taskCreatedConsumer, taskConsumer);
  }

  @Test
  void should_OnlyReturnMostSpecificAndMoreGeneralConsumers_For_GeneralEvent() {
    final KadaiEventBus eventBus = new KadaiEventBus(kadaiEngine);
    final var taskConsumer = new TaskHistoryEventConsumer();
    final var taskCreatedConsumer = new TaskCreatedEventConsumer();
    eventBus.subscribes(taskConsumer);
    eventBus.subscribes(taskCreatedConsumer);

    final List<KadaiEventConsumer<? super TaskHistoryEvent>> retrieved =
        eventBus.getConsumers(TaskHistoryEvent.class);

    assertThat(retrieved).containsExactly(taskConsumer);
  }

  @Test
  void should_dispatchToMostSpecificAndMoreGeneralConsumers_For_SpecializedEvent() {
    final KadaiEventBus eventBus = new KadaiEventBus(kadaiEngine);
    final var taskConsumer = Mockito.spy(new TaskHistoryEventConsumer());
    final var taskCreatedConsumer = Mockito.spy(new TaskCreatedEventConsumer());
    eventBus.subscribes(taskConsumer);
    eventBus.subscribes(taskCreatedConsumer);

    eventBus.dispatch(mock(TaskCreatedEvent.class));

    verify(taskConsumer, times(1)).consume(any());
    verify(taskCreatedConsumer, times(1)).consume(any());
  }

  @Test
  void should_OnlyDispatchToMostSpecificAndMoreGeneralConsumers_For_GeneralEvent() {
    final KadaiEventBus eventBus = new KadaiEventBus(kadaiEngine);
    final var taskConsumer = Mockito.spy(new TaskHistoryEventConsumer());
    final var taskCreatedConsumer = Mockito.spy(new TaskCreatedEventConsumer());
    eventBus.subscribes(taskConsumer);
    eventBus.subscribes(taskCreatedConsumer);

    eventBus.dispatch(new TaskHistoryEvent());

    verify(taskConsumer, times(1)).consume(any());
    verify(taskCreatedConsumer, never()).consume(any());
  }

  @Test
  void should_dispatchEventExactlyOnceToEveryEligibleConsumer() {
    final KadaiEventBus eventBus = new KadaiEventBus(kadaiEngine);
    final var taskConsumer = Mockito.spy(new TaskHistoryEventConsumer());
    eventBus.subscribes(taskConsumer);

    eventBus.dispatch(new TaskHistoryEvent());

    verify(taskConsumer, times(1)).consume(any());
  }

  @Test
  void should_EnrichUserAndProxyAccessId_For_Dispatch() {
    final KadaiEventBus eventBus = new KadaiEventBus(kadaiEngine);
    final KadaiEvent event = new TaskHistoryEvent();

    kadaiEngine.runAs(() -> eventBus.dispatch(event), KadaiRole.ADMIN, "roberto");

    assertThat(event.getUserId()).isEqualTo("roberto");
    assertThat(event.getProxyAccessId()).isEqualTo("uid=admin,cn=users,ou=test,o=kadai");
  }

  private static class TaskHistoryEventConsumer implements KadaiEventConsumer<TaskHistoryEvent> {
    @Override
    public void consume(TaskHistoryEvent ignore) {}

    @Override
    public void initialize(KadaiEngine ignore) {}

    @Override
    public Class<TaskHistoryEvent> reify() {
      return TaskHistoryEvent.class;
    }
  }

  private static class TaskCreatedEventConsumer implements KadaiEventConsumer<TaskCreatedEvent> {
    @Override
    public void consume(TaskCreatedEvent ignore) {}

    @Override
    public void initialize(KadaiEngine ignore) {}

    @Override
    public Class<TaskCreatedEvent> reify() {
      return TaskCreatedEvent.class;
    }
  }
}
