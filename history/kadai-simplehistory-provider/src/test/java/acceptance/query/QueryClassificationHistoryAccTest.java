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

package acceptance.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import acceptance.AbstractAccTest;
import acceptance.ParameterizedQuerySqlCaptureInterceptor;
import io.kadai.classification.api.ClassificationCustomField;
import io.kadai.common.api.BaseQuery.SortDirection;
import io.kadai.common.api.TimeInterval;
import io.kadai.common.internal.KadaiEngineImpl;
import io.kadai.simplehistory.impl.SimpleHistoryServiceImpl;
import io.kadai.simplehistory.impl.classification.ClassificationHistoryQuery;
import io.kadai.simplehistory.impl.classification.ClassificationHistoryQueryColumnName;
import io.kadai.spi.history.api.events.classification.ClassificationHistoryEvent;
import io.kadai.spi.history.api.events.classification.ClassificationHistoryEventType;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.apache.ibatis.session.SqlSessionManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Test for Classification History queries. */
class QueryClassificationHistoryAccTest extends AbstractAccTest {

  private final SimpleHistoryServiceImpl historyService = getHistoryService();

  @Test
  void should_ConfirmEquality_When_UsingListValuesAscendingAndDescending() {
    List<String> defaultList =
        historyService
            .createClassificationHistoryQuery()
            .listValues(ClassificationHistoryQueryColumnName.CREATED, null);
    List<String> ascendingList =
        historyService
            .createClassificationHistoryQuery()
            .listValues(ClassificationHistoryQueryColumnName.CREATED, SortDirection.ASCENDING);

    assertThat(ascendingList).hasSize(11).isEqualTo(defaultList);

    List<String> descendingList =
        historyService
            .createClassificationHistoryQuery()
            .listValues(ClassificationHistoryQueryColumnName.CREATED, SortDirection.DESCENDING);
    Collections.reverse(ascendingList);

    assertThat(ascendingList).isEqualTo(descendingList);
  }

  @Test
  void should_ReturnHistoryEvents_For_ComplexQuery() {

    ClassificationHistoryQuery query =
        historyService
            .createClassificationHistoryQuery()
            .eventTypeIn(ClassificationHistoryEventType.UPDATED.getName())
            .domainLike("%A")
            .customAttributeIn(ClassificationCustomField.CUSTOM_1, "otherCustom1")
            .orderByCreated(SortDirection.DESCENDING);

    List<ClassificationHistoryEvent> results = query.list();
    assertThat(results)
        .extracting(ClassificationHistoryEvent::getUserId)
        .containsOnly("admin", "peter", "sven");
    results = query.orderByUserId(SortDirection.DESCENDING).list();
    assertThat(results)
        .extracting(ClassificationHistoryEvent::getUserId)
        .containsOnly("admin", "peter", "sven");
    assertThat(results).hasSize(5);
  }

  @Test
  void should_ConfirmQueryListOffset_When_ProvidingOffsetAndLimit() {
    List<ClassificationHistoryEvent> offsetAndLimitResult =
        historyService.createClassificationHistoryQuery().list(1, 2);
    List<ClassificationHistoryEvent> regularResult =
        historyService.createClassificationHistoryQuery().list();

    assertThat(offsetAndLimitResult).hasSize(2);
    assertThat(offsetAndLimitResult.get(0))
        .isNotEqualTo(regularResult.get(0))
        .isEqualTo(regularResult.get(1));
  }

  @Test
  void should_ReturnEmptyList_When_ProvidingWrongConstraints() {
    List<ClassificationHistoryEvent> result =
        historyService.createClassificationHistoryQuery().list(1, 1000);
    assertThat(result).hasSize(10);

    result = historyService.createClassificationHistoryQuery().list(100, 1000);
    assertThat(result).isEmpty();
  }

  @Test
  void should_ReturnSingleHistoryEvent_When_UsingSingleMethod() {
    ClassificationHistoryEvent single =
        historyService
            .createClassificationHistoryQuery()
            .userIdIn("peter")
            .classificationIdIn("CLI:000000000000000000000000000000000001")
            .single();
    assertThat(single.getEventType()).isEqualTo(ClassificationHistoryEventType.CREATED.getName());

    single =
        historyService
            .createClassificationHistoryQuery()
            .eventTypeIn(ClassificationHistoryEventType.CREATED.getName(), "xy")
            .classificationIdIn("CLI:000000000000000000000000000000000001")
            .single();
    assertThat(single.getUserId()).isEqualTo("peter");
  }

  @Test
  void should_ThrowException_When_SingleMethodRetrievesMoreThanOneEventFromDatabase() {

    ClassificationHistoryQuery query =
        getHistoryService().createClassificationHistoryQuery().userIdIn("peter");

    assertThatThrownBy(query::single).isInstanceOf(TooManyResultsException.class);
  }

  @Test
  void should_ReturnCountOfEvents_When_UsingCountMethod() {
    long count = historyService.createClassificationHistoryQuery().userIdIn("peter").count();
    assertThat(count).isEqualTo(7);

    count = historyService.createClassificationHistoryQuery().count();
    assertThat(count).isEqualTo(11);

    count =
        historyService
            .createClassificationHistoryQuery()
            .userIdIn("hans", "jürgen", "klaus")
            .count();
    assertThat(count).isZero();
  }

  @Test
  void should_ReturnHistoryEvents_For_DifferentInAttributes() {

    List<ClassificationHistoryEvent> returnValues =
        historyService
            .createClassificationHistoryQuery()
            .eventTypeIn(ClassificationHistoryEventType.CREATED.getName())
            .list();
    assertThat(returnValues).hasSize(6);

    TimeInterval timeInterval =
        new TimeInterval(Instant.parse("2018-01-28T14:55:00Z"), Instant.now());
    returnValues =
        historyService.createClassificationHistoryQuery().createdWithin(timeInterval).list();
    assertThat(returnValues).hasSize(11);

    returnValues = historyService.createClassificationHistoryQuery().userIdIn("peter").list();
    assertThat(returnValues).hasSize(7);

    returnValues =
        historyService
            .createClassificationHistoryQuery()
            .classificationIdIn("CLI:000000000000000000000000000000000002")
            .list();
    assertThat(returnValues).hasSize(2);

    returnValues =
        historyService
            .createClassificationHistoryQuery()
            .applicationEntryPointIn("someEntryPoint")
            .list();
    assertThat(returnValues).hasSize(2);

    returnValues = historyService.createClassificationHistoryQuery().categoryIn("MANUAL").list();
    assertThat(returnValues).hasSize(7);

    returnValues = historyService.createClassificationHistoryQuery().domainIn("DOMAIN_A").list();
    assertThat(returnValues).hasSize(11);

    returnValues = historyService.createClassificationHistoryQuery().keyIn("L10003").list();
    assertThat(returnValues).hasSize(2);

    returnValues =
        historyService.createClassificationHistoryQuery().nameIn("DFG-Leistungsfall").list();
    assertThat(returnValues).hasSize(2);

    returnValues =
        historyService.createClassificationHistoryQuery().parentIdIn("someParentId").list();
    assertThat(returnValues).hasSize(1);

    returnValues =
        historyService.createClassificationHistoryQuery().parentKeyIn("otherParentKey").list();
    assertThat(returnValues).hasSize(1);

    returnValues = historyService.createClassificationHistoryQuery().priorityIn(1).list();
    assertThat(returnValues).hasSize(3);

    returnValues = historyService.createClassificationHistoryQuery().serviceLevelIn("P3D").list();
    assertThat(returnValues).hasSize(2);

    returnValues = historyService.createClassificationHistoryQuery().typeIn("TASK").list();
    assertThat(returnValues).hasSize(7);

    returnValues =
        historyService
            .createClassificationHistoryQuery()
            .customAttributeIn(ClassificationCustomField.CUSTOM_1, "custom1")
            .list();
    assertThat(returnValues).hasSize(6);

    returnValues =
        historyService
            .createClassificationHistoryQuery()
            .customAttributeIn(ClassificationCustomField.CUSTOM_2, "custom2")
            .list();
    assertThat(returnValues).hasSize(6);

    returnValues =
        historyService
            .createClassificationHistoryQuery()
            .customAttributeIn(ClassificationCustomField.CUSTOM_3, "otherCustom3")
            .list();
    assertThat(returnValues).hasSize(5);

    returnValues =
        historyService
            .createClassificationHistoryQuery()
            .customAttributeIn(ClassificationCustomField.CUSTOM_4, "custom4", "otherCustom4")
            .list();
    assertThat(returnValues).hasSize(11);

    returnValues =
        historyService
            .createClassificationHistoryQuery()
            .customAttributeIn(ClassificationCustomField.CUSTOM_5, "custom5")
            .list();
    assertThat(returnValues).hasSize(6);

    returnValues =
        historyService
            .createClassificationHistoryQuery()
            .customAttributeIn(ClassificationCustomField.CUSTOM_6, "custom6")
            .list();
    assertThat(returnValues).hasSize(6);

    returnValues =
        historyService
            .createClassificationHistoryQuery()
            .customAttributeIn(ClassificationCustomField.CUSTOM_7, "otherCustom7")
            .list();
    assertThat(returnValues).hasSize(5);

    returnValues =
        historyService
            .createClassificationHistoryQuery()
            .customAttributeIn(ClassificationCustomField.CUSTOM_8, "custom8", "otherCustom8")
            .list();
    assertThat(returnValues).hasSize(11);
  }

  @Test
  void should_ReturnHistoryEvents_For_DifferentLikeAttributes() {

    List<ClassificationHistoryEvent> returnValues =
        historyService.createClassificationHistoryQuery().eventTypeLike("C%").list();
    assertThat(returnValues).hasSize(6);

    returnValues = historyService.createClassificationHistoryQuery().userIdLike("p%", "c%").list();
    assertThat(returnValues).hasSize(8);

    returnValues =
        historyService.createClassificationHistoryQuery().classificationIdLike("%0004").list();
    assertThat(returnValues).hasSize(2);

    returnValues =
        historyService
            .createClassificationHistoryQuery()
            .applicationEntryPointLike("other%")
            .list();
    assertThat(returnValues).hasSize(1);

    returnValues = historyService.createClassificationHistoryQuery().categoryLike("%ERNAL").list();
    assertThat(returnValues).hasSize(4);

    returnValues = historyService.createClassificationHistoryQuery().domainLike("%_A").list();
    assertThat(returnValues).hasSize(11);

    returnValues = historyService.createClassificationHistoryQuery().keyLike("%004").list();
    assertThat(returnValues).hasSize(2);

    returnValues = historyService.createClassificationHistoryQuery().nameLike("POK%").list();
    assertThat(returnValues).hasSize(2);

    returnValues = historyService.createClassificationHistoryQuery().parentIdLike("other%").list();
    assertThat(returnValues).hasSize(1);

    returnValues = historyService.createClassificationHistoryQuery().parentKeyLike("other%").list();
    assertThat(returnValues).hasSize(1);

    returnValues = historyService.createClassificationHistoryQuery().serviceLevelLike("%1D").list();
    assertThat(returnValues).hasSize(6);

    returnValues = historyService.createClassificationHistoryQuery().typeLike("DOCU%").list();
    assertThat(returnValues).hasSize(4);

    returnValues =
        historyService
            .createClassificationHistoryQuery()
            .customAttributeLike(ClassificationCustomField.CUSTOM_1, "other%")
            .list();
    assertThat(returnValues).hasSize(5);

    returnValues =
        historyService
            .createClassificationHistoryQuery()
            .customAttributeLike(ClassificationCustomField.CUSTOM_2, "cu%")
            .list();
    assertThat(returnValues).hasSize(6);

    returnValues =
        historyService
            .createClassificationHistoryQuery()
            .customAttributeLike(ClassificationCustomField.CUSTOM_3, "other%", "cu%")
            .list();
    assertThat(returnValues).hasSize(11);

    returnValues =
        historyService
            .createClassificationHistoryQuery()
            .customAttributeLike(ClassificationCustomField.CUSTOM_4, "other%")
            .list();
    assertThat(returnValues).hasSize(5);

    returnValues =
        historyService
            .createClassificationHistoryQuery()
            .customAttributeLike(ClassificationCustomField.CUSTOM_5, "cu%")
            .list();
    assertThat(returnValues).hasSize(6);

    returnValues =
        historyService
            .createClassificationHistoryQuery()
            .customAttributeLike(ClassificationCustomField.CUSTOM_6, "other%", "cu%")
            .list();
    assertThat(returnValues).hasSize(11);

    returnValues =
        historyService
            .createClassificationHistoryQuery()
            .customAttributeLike(ClassificationCustomField.CUSTOM_7, "other%")
            .list();
    assertThat(returnValues).hasSize(5);

    returnValues =
        historyService
            .createClassificationHistoryQuery()
            .customAttributeLike(ClassificationCustomField.CUSTOM_8, "other%", "cu%")
            .list();
    assertThat(returnValues).hasSize(11);
  }

  @Test
  void should_ReturnHistoryEvents_When_ProvidingListValues() {
    List<String> returnedList =
        historyService
            .createClassificationHistoryQuery()
            .listValues(ClassificationHistoryQueryColumnName.ID, null);
    assertThat(returnedList).hasSize(11);

    returnedList =
        historyService
            .createClassificationHistoryQuery()
            .listValues(ClassificationHistoryQueryColumnName.EVENT_TYPE, null);
    assertThat(returnedList).hasSize(2);

    returnedList =
        historyService
            .createClassificationHistoryQuery()
            .listValues(ClassificationHistoryQueryColumnName.CREATED, null);
    assertThat(returnedList).hasSize(11);

    returnedList =
        historyService
            .createClassificationHistoryQuery()
            .listValues(ClassificationHistoryQueryColumnName.USER_ID, null);
    assertThat(returnedList).hasSize(4);

    returnedList =
        historyService
            .createClassificationHistoryQuery()
            .listValues(ClassificationHistoryQueryColumnName.CLASSIFICATION_ID, null);
    assertThat(returnedList).hasSize(6);

    returnedList =
        historyService
            .createClassificationHistoryQuery()
            .listValues(ClassificationHistoryQueryColumnName.APPLICATION_ENTRY_POINT, null);
    assertThat(returnedList).hasSize(3);

    returnedList =
        historyService
            .createClassificationHistoryQuery()
            .listValues(ClassificationHistoryQueryColumnName.CATEGORY, null);
    assertThat(returnedList).hasSize(2);

    returnedList =
        historyService
            .createClassificationHistoryQuery()
            .listValues(ClassificationHistoryQueryColumnName.DOMAIN, null);
    assertThat(returnedList).hasSize(1);

    returnedList =
        historyService
            .createClassificationHistoryQuery()
            .listValues(ClassificationHistoryQueryColumnName.KEY, null);
    assertThat(returnedList).hasSize(6);

    returnedList =
        historyService
            .createClassificationHistoryQuery()
            .listValues(ClassificationHistoryQueryColumnName.NAME, null);
    assertThat(returnedList).hasSize(6);

    returnedList =
        historyService
            .createClassificationHistoryQuery()
            .listValues(ClassificationHistoryQueryColumnName.PARENT_ID, null);
    assertThat(returnedList).hasSize(3);

    returnedList =
        historyService
            .createClassificationHistoryQuery()
            .listValues(ClassificationHistoryQueryColumnName.PARENT_KEY, null);
    assertThat(returnedList).hasSize(3);

    returnedList =
        historyService
            .createClassificationHistoryQuery()
            .listValues(ClassificationHistoryQueryColumnName.SERVICE_LEVEL, null);
    assertThat(returnedList).hasSize(4);

    returnedList =
        historyService
            .createClassificationHistoryQuery()
            .listValues(ClassificationHistoryQueryColumnName.TYPE, null);
    assertThat(returnedList).hasSize(2);

    returnedList =
        historyService
            .createClassificationHistoryQuery()
            .listValues(ClassificationHistoryQueryColumnName.CUSTOM_1, null);
    assertThat(returnedList).hasSize(2);

    returnedList =
        historyService
            .createClassificationHistoryQuery()
            .listValues(ClassificationHistoryQueryColumnName.CUSTOM_2, null);
    assertThat(returnedList).hasSize(2);

    returnedList =
        historyService
            .createClassificationHistoryQuery()
            .listValues(ClassificationHistoryQueryColumnName.CUSTOM_3, null);
    assertThat(returnedList).hasSize(2);

    returnedList =
        historyService
            .createClassificationHistoryQuery()
            .listValues(ClassificationHistoryQueryColumnName.CUSTOM_4, null);
    assertThat(returnedList).hasSize(2);

    returnedList =
        historyService
            .createClassificationHistoryQuery()
            .listValues(ClassificationHistoryQueryColumnName.CUSTOM_5, null);
    assertThat(returnedList).hasSize(2);

    returnedList =
        historyService
            .createClassificationHistoryQuery()
            .listValues(ClassificationHistoryQueryColumnName.CUSTOM_6, null);
    assertThat(returnedList).hasSize(2);

    returnedList =
        historyService
            .createClassificationHistoryQuery()
            .listValues(ClassificationHistoryQueryColumnName.CUSTOM_7, null);
    assertThat(returnedList).hasSize(2);

    returnedList =
        historyService
            .createClassificationHistoryQuery()
            .listValues(ClassificationHistoryQueryColumnName.CUSTOM_8, null);
    assertThat(returnedList).hasSize(2);
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class PhysicalPagination {
    @BeforeAll
    void setup() throws Exception {
      Field sessionManagerField = KadaiEngineImpl.class.getDeclaredField("sessionManager");
      sessionManagerField.setAccessible(true);
      SqlSessionManager sessionManager = (SqlSessionManager) sessionManagerField.get(kadaiEngine);
      sessionManager
          .getConfiguration()
          .addInterceptor(new ParameterizedQuerySqlCaptureInterceptor());
    }

    @ParameterizedTest
    @CsvSource({"0,10", "5,10", "0,0", "2,4"})
    void should_UseNativeSql_For_QueryPagination(int offset, int limit) {
      ParameterizedQuerySqlCaptureInterceptor.resetCapturedSql();
      historyService.createClassificationHistoryQuery().list(offset, limit);
      final String sql = ParameterizedQuerySqlCaptureInterceptor.getCapturedSql();
      final String physicalPattern1 = String.format("LIMIT %d OFFSET %d", limit, offset);
      final String physicalPattern2 =
          String.format("OFFSET %d ROWS FETCH FIRST %d ROWS ONLY", offset, limit);

      assertThat(sql).containsAnyOf(physicalPattern1, physicalPattern2);
    }
  }
}
