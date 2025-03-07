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

package acceptance.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import acceptance.AbstractAccTest;
import io.kadai.common.api.BaseQuery.SortDirection;
import io.kadai.common.api.TimeInterval;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.simplehistory.workbasket.api.WorkbasketHistoryQuery;
import io.kadai.simplehistory.workbasket.api.WorkbasketHistoryQueryColumnName;
import io.kadai.spi.history.api.events.workbasket.WorkbasketHistoryEvent;
import io.kadai.spi.history.api.events.workbasket.WorkbasketHistoryEventType;
import io.kadai.workbasket.api.WorkbasketCustomField;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.junit.jupiter.api.Test;

class QueryWorkbasketHistoryAccTest extends AbstractAccTest {

  @Test
  void should_ConfirmEquality_When_UsingListValuesAscendingAndDescending() {
    List<String> defaultList =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .listValues(WorkbasketHistoryQueryColumnName.CREATED, null);
    List<String> ascendingList =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .listValues(WorkbasketHistoryQueryColumnName.CREATED, SortDirection.ASCENDING);

    assertThat(ascendingList).hasSize(10).isEqualTo(defaultList);

    List<String> descendingList =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .listValues(WorkbasketHistoryQueryColumnName.CREATED, SortDirection.DESCENDING);
    Collections.reverse(ascendingList);

    assertThat(ascendingList).isEqualTo(descendingList);
  }

  @Test
  void should_ReturnHistoryEvents_For_ComplexQuery() {
    WorkbasketHistoryQuery query =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .eventTypeIn(WorkbasketHistoryEventType.CREATED.getName())
            .domainLike("%A")
            .customAttributeIn(WorkbasketCustomField.CUSTOM_1, "otherCustom1")
            .orderByCreated(SortDirection.DESCENDING);

    List<WorkbasketHistoryEvent> results = query.list();
    assertThat(results)
        .extracting(WorkbasketHistoryEvent::getUserId)
        .containsOnly("claudia", "peter", "sven");
    results = query.orderByUserId(SortDirection.DESCENDING).list();
    assertThat(results)
        .extracting(WorkbasketHistoryEvent::getUserId)
        .containsOnly("claudia", "peter", "sven");
    assertThat(results).hasSize(3);
  }

  @Test
  void should_ConfirmQueryListOffset_When_ProvidingOffsetAndLimit() {
    List<WorkbasketHistoryEvent> offsetAndLimitResult =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .orderById(SortDirection.ASCENDING)
            .list(1, 2);
    List<WorkbasketHistoryEvent> regularResult =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .orderById(SortDirection.ASCENDING)
            .list();

    assertThat(offsetAndLimitResult).hasSize(2);
    assertThat(offsetAndLimitResult.get(0))
        .isNotEqualTo(regularResult.get(0))
        .isEqualTo(regularResult.get(1));
  }

  @Test
  void should_ReturnEmptyList_When_ProvidingWrongConstraints() {
    List<WorkbasketHistoryEvent> result =
        workbasketHistoryService.createWorkbasketHistoryQuery().list(1, 1000);
    assertThat(result).hasSize(9);

    result = workbasketHistoryService.createWorkbasketHistoryQuery().list(100, 1000);
    assertThat(result).isEmpty();
  }

  @Test
  void should_ReturnSingleHistoryEvent_When_UsingSingleMethod() {
    WorkbasketHistoryEvent single =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .userIdIn("peter")
            .idIn("WHI:000000000000000000000000000000000000")
            .single();
    assertThat(single.getEventType()).isEqualTo(WorkbasketHistoryEventType.CREATED.getName());

    single =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .eventTypeIn(WorkbasketHistoryEventType.CREATED.getName(), "xy")
            .idIn("WHI:000000000000000000000000000000000000")
            .single();
    assertThat(single.getUserId()).isEqualTo("peter");
  }

  @Test
  void should_ThrowException_When_SingleMethodRetrievesMoreThanOneEventFromDatabase() {
    WorkbasketHistoryQuery query =
        workbasketHistoryService.createWorkbasketHistoryQuery().userIdIn("peter");

    assertThatThrownBy(query::single).isInstanceOf(TooManyResultsException.class);
  }

  @Test
  void should_ReturnCountOfEvents_When_UsingCountMethod() {
    long count = workbasketHistoryService.createWorkbasketHistoryQuery().userIdIn("peter").count();
    assertThat(count).isEqualTo(6);

    count = workbasketHistoryService.createWorkbasketHistoryQuery().count();
    assertThat(count).isEqualTo(10);

    count =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .userIdIn("hans", "jürgen", "klaus")
            .count();
    assertThat(count).isZero();
  }

  @Test
  void should_ReturnHistoryEvents_For_DifferentInAttributes() {

    List<WorkbasketHistoryEvent> returnValues =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .workbasketIdIn("WBI:000000000000000000000000000000000903")
            .list();
    assertThat(returnValues).hasSize(5);

    returnValues =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .eventTypeIn(WorkbasketHistoryEventType.CREATED.getName())
            .list();
    assertThat(returnValues).hasSize(6);

    TimeInterval timeInterval = new TimeInterval(Instant.now().minusSeconds(10), Instant.now());
    returnValues =
        workbasketHistoryService.createWorkbasketHistoryQuery().createdWithin(timeInterval).list();
    assertThat(returnValues).isEmpty();

    returnValues = workbasketHistoryService.createWorkbasketHistoryQuery().userIdIn("peter").list();
    assertThat(returnValues).hasSize(6);

    returnValues =
        workbasketHistoryService.createWorkbasketHistoryQuery().domainIn("DOMAIN_A").list();
    assertThat(returnValues).hasSize(10);

    returnValues = workbasketHistoryService.createWorkbasketHistoryQuery().keyIn("soRt003").list();
    assertThat(returnValues).hasSize(5);

    returnValues = workbasketHistoryService.createWorkbasketHistoryQuery().ownerIn("admin").list();
    assertThat(returnValues).hasSize(10);

    returnValues = workbasketHistoryService.createWorkbasketHistoryQuery().typeIn("TOPIC").list();
    assertThat(returnValues).hasSize(10);

    returnValues =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .customAttributeIn(WorkbasketCustomField.CUSTOM_1, "custom1")
            .list();
    assertThat(returnValues).hasSize(5);

    returnValues =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .customAttributeIn(WorkbasketCustomField.CUSTOM_2, "custom2")
            .list();
    assertThat(returnValues).hasSize(5);

    returnValues =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .customAttributeIn(WorkbasketCustomField.CUSTOM_3, "custom3")
            .list();
    assertThat(returnValues).hasSize(5);

    returnValues =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .customAttributeIn(WorkbasketCustomField.CUSTOM_4, "custom4")
            .list();
    assertThat(returnValues).hasSize(5);

    returnValues =
        workbasketHistoryService.createWorkbasketHistoryQuery().orgLevel1In("orgLevel1").list();
    assertThat(returnValues).hasSize(5);

    returnValues =
        workbasketHistoryService.createWorkbasketHistoryQuery().orgLevel2In("orgLevel2").list();
    assertThat(returnValues).hasSize(5);

    returnValues =
        workbasketHistoryService.createWorkbasketHistoryQuery().orgLevel3In("orgLevel3").list();
    assertThat(returnValues).hasSize(5);

    returnValues =
        workbasketHistoryService.createWorkbasketHistoryQuery().orgLevel4In("orgLevel4").list();
    assertThat(returnValues).hasSize(5);
  }

  @Test
  void should_ReturnHistoryEvents_In_DifferentOrders() throws InvalidArgumentException {
    List<WorkbasketHistoryEvent> results =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .orderByWorkbasketId(SortDirection.ASCENDING)
            .listPage(0, 3);
    assertThat(results)
        .extracting(WorkbasketHistoryEvent::getWorkbasketId)
        .containsExactly(
            "WBI:000000000000000000000000000000000803",
            "WBI:000000000000000000000000000000000803",
            "WBI:000000000000000000000000000000000803");

    results =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .orderByEventType(SortDirection.ASCENDING)
            .listPage(0, 3);
    assertThat(results)
        .extracting(WorkbasketHistoryEvent::getEventType)
        .containsExactly("CREATED", "CREATED", "CREATED");

    results =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .orderByOrgLevel(1, SortDirection.ASCENDING)
            .listPage(0, 3);
    assertThat(results)
        .extracting(WorkbasketHistoryEvent::getOrgLevel1)
        .containsExactly("orgLevel1", "orgLevel1", "orgLevel1");

    results =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .orderByOrgLevel(2, SortDirection.ASCENDING)
            .listPage(0, 3);
    assertThat(results)
        .extracting(WorkbasketHistoryEvent::getOrgLevel2)
        .containsExactly("orgLevel2", "orgLevel2", "orgLevel2");

    results =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .orderByOrgLevel(3, SortDirection.ASCENDING)
            .listPage(0, 3);
    assertThat(results)
        .extracting(WorkbasketHistoryEvent::getOrgLevel3)
        .containsExactly("orgLevel3", "orgLevel3", "orgLevel3");

    results =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .orderByOrgLevel(4, SortDirection.ASCENDING)
            .listPage(0, 3);
    assertThat(results)
        .extracting(WorkbasketHistoryEvent::getOrgLevel4)
        .containsExactly("orgLevel4", "orgLevel4", "orgLevel4");

    results =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .orderByCustomAttribute(1, SortDirection.ASCENDING)
            // values of custom1 do not differ and are larger than 3 (n=5).
            // Therefore, in order to guarantee the sort order,
            // we have to define a secondary (unique) sort option
            .orderById(SortDirection.ASCENDING)
            .listPage(0, 3);
    assertThat(results)
        .extracting(WorkbasketHistoryEvent::getId)
        .containsExactly(
            "WHI:000000000000000000000000000000000000",
            "WHI:000000000000000000000000000000000002",
            "WHI:000000000000000000000000000000000004");

    results =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .orderByCustomAttribute(2, SortDirection.ASCENDING)
            // values of custom1 do not differ and are larger than 3 (n=5).
            // Therefore, in order to guarantee the sort order,
            // we have to define a secondary (unique) sort option
            .orderById(SortDirection.ASCENDING)
            .listPage(0, 3);
    assertThat(results)
        .extracting(WorkbasketHistoryEvent::getId)
        .containsExactly(
            "WHI:000000000000000000000000000000000000",
            "WHI:000000000000000000000000000000000002",
            "WHI:000000000000000000000000000000000004");

    results =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .orderByCustomAttribute(3, SortDirection.ASCENDING)
            // values of custom1 do not differ and are larger than 3 (n=5).
            // Therefore, in order to guarantee the sort order,
            // we have to define a secondary (unique) sort option
            .orderById(SortDirection.ASCENDING)
            .listPage(0, 3);
    assertThat(results)
        .extracting(WorkbasketHistoryEvent::getId)
        .containsExactly(
            "WHI:000000000000000000000000000000000000",
            "WHI:000000000000000000000000000000000002",
            "WHI:000000000000000000000000000000000004");

    results =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .orderByCustomAttribute(4, SortDirection.ASCENDING)
            // values of custom1 do not differ and are larger than 3 (n=5).
            // Therefore, in order to guarantee the sort order,
            // we have to define a secondary (unique) sort option
            .orderById(SortDirection.ASCENDING)
            .listPage(0, 3);
    assertThat(results)
        .extracting(WorkbasketHistoryEvent::getId)
        .containsExactly(
            "WHI:000000000000000000000000000000000000",
            "WHI:000000000000000000000000000000000002",
            "WHI:000000000000000000000000000000000004");
  }

  @Test
  void should_ReturnHistoryEvents_For_DifferentLikeAttributes() {
    List<WorkbasketHistoryEvent> returnValues =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .workbasketIdLike("WBI:000000000000000%")
            .list();
    assertThat(returnValues).hasSize(10);

    returnValues =
        workbasketHistoryService.createWorkbasketHistoryQuery().eventTypeLike("C%").list();
    assertThat(returnValues).hasSize(6);

    returnValues =
        workbasketHistoryService.createWorkbasketHistoryQuery().userIdLike("p%", "c%").list();
    assertThat(returnValues).hasSize(8);

    returnValues = workbasketHistoryService.createWorkbasketHistoryQuery().domainLike("%_A").list();
    assertThat(returnValues).hasSize(10);

    returnValues =
        workbasketHistoryService.createWorkbasketHistoryQuery().workbasketKeyLike("%Rt0%").list();
    assertThat(returnValues).hasSize(10);

    returnValues =
        workbasketHistoryService.createWorkbasketHistoryQuery().workbasketTypeLike("TOP%").list();
    assertThat(returnValues).hasSize(10);

    returnValues = workbasketHistoryService.createWorkbasketHistoryQuery().ownerLike("adm%").list();
    assertThat(returnValues).hasSize(10);

    returnValues =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .customAttributeLike(WorkbasketCustomField.CUSTOM_1, "other%1")
            .list();
    assertThat(returnValues).hasSize(5);

    returnValues =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .customAttributeLike(WorkbasketCustomField.CUSTOM_2, "other%2")
            .list();
    assertThat(returnValues).hasSize(5);

    returnValues =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .customAttributeLike(WorkbasketCustomField.CUSTOM_3, "other%3")
            .list();
    assertThat(returnValues).hasSize(5);

    returnValues =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .customAttributeLike(WorkbasketCustomField.CUSTOM_4, "other%4")
            .list();
    assertThat(returnValues).hasSize(5);

    returnValues =
        workbasketHistoryService.createWorkbasketHistoryQuery().orgLevel1Like("org%1").list();
    assertThat(returnValues).hasSize(5);

    returnValues =
        workbasketHistoryService.createWorkbasketHistoryQuery().orgLevel2Like("org%2").list();
    assertThat(returnValues).hasSize(5);

    returnValues =
        workbasketHistoryService.createWorkbasketHistoryQuery().orgLevel3Like("org%3").list();
    assertThat(returnValues).hasSize(5);

    returnValues =
        workbasketHistoryService.createWorkbasketHistoryQuery().orgLevel4Like("org%4").list();
    assertThat(returnValues).hasSize(5);
  }

  @Test
  void should_ReturnHistoryEvents_When_ProvidingListValues() {
    List<String> returnedList =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .listValues(WorkbasketHistoryQueryColumnName.ID, null);
    assertThat(returnedList).hasSize(10);

    returnedList =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .listValues(WorkbasketHistoryQueryColumnName.WORKBASKET_ID, null);
    assertThat(returnedList).hasSize(2);

    returnedList =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .listValues(WorkbasketHistoryQueryColumnName.EVENT_TYPE, null);
    assertThat(returnedList).hasSize(3);

    returnedList =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .listValues(WorkbasketHistoryQueryColumnName.CREATED, null);
    assertThat(returnedList).hasSize(10);

    returnedList =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .listValues(WorkbasketHistoryQueryColumnName.USER_ID, null);
    assertThat(returnedList).hasSize(3);

    returnedList =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .listValues(WorkbasketHistoryQueryColumnName.DOMAIN, null);
    assertThat(returnedList).hasSize(1);

    returnedList =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .listValues(WorkbasketHistoryQueryColumnName.KEY, null);
    assertThat(returnedList).hasSize(2);

    returnedList =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .listValues(WorkbasketHistoryQueryColumnName.TYPE, null);
    assertThat(returnedList).hasSize(1);

    returnedList =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .listValues(WorkbasketHistoryQueryColumnName.OWNER, null);
    assertThat(returnedList).hasSize(1);

    returnedList =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .listValues(WorkbasketHistoryQueryColumnName.CUSTOM_1, null);
    assertThat(returnedList).hasSize(2);

    returnedList =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .listValues(WorkbasketHistoryQueryColumnName.CUSTOM_2, null);
    assertThat(returnedList).hasSize(2);

    returnedList =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .listValues(WorkbasketHistoryQueryColumnName.CUSTOM_3, null);
    assertThat(returnedList).hasSize(2);

    returnedList =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .listValues(WorkbasketHistoryQueryColumnName.CUSTOM_4, null);
    assertThat(returnedList).hasSize(2);

    returnedList =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .listValues(WorkbasketHistoryQueryColumnName.ORGLEVEL_1, null);
    assertThat(returnedList).hasSize(2);

    returnedList =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .listValues(WorkbasketHistoryQueryColumnName.ORGLEVEL_2, null);
    assertThat(returnedList).hasSize(2);

    returnedList =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .listValues(WorkbasketHistoryQueryColumnName.ORGLEVEL_3, null);
    assertThat(returnedList).hasSize(2);

    returnedList =
        workbasketHistoryService
            .createWorkbasketHistoryQuery()
            .listValues(WorkbasketHistoryQueryColumnName.ORGLEVEL_4, null);
    assertThat(returnedList).hasSize(2);
  }
}
