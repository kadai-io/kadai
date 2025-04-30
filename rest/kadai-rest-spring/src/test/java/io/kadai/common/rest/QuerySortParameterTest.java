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

package io.kadai.common.rest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.kadai.common.api.BaseQuery;
import io.kadai.common.api.BaseQuery.SortDirection;
import io.kadai.common.api.QueryColumnName;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import java.util.List;
import org.junit.jupiter.api.Test;

class QuerySortParameterTest {

  @Test
  void should_ApplySortBy_When_SortByParameterIsCalled() {
    MockQuery query = mock(MockQuery.class);
    MockSortBy sortBy = mock(MockSortBy.class);

    QuerySortParameter<MockQuery, MockSortBy> sortByParameter =
        new QuerySortParameter<>(List.of(sortBy), List.of(SortDirection.ASCENDING));

    sortByParameter.apply(query);

    verify(sortBy).applySortByForQuery(query, SortDirection.ASCENDING);
  }

  @Test
  void should_ApplySortDirectionAsc_When_OrderByIsNull() {
    MockQuery query = mock(MockQuery.class);
    MockSortBy sortBy = mock(MockSortBy.class);

    QuerySortParameter<MockQuery, MockSortBy> sortByParameter =
        new QuerySortParameter<>(List.of(sortBy), null);

    sortByParameter.apply(query);

    verify(sortBy).applySortByForQuery(query, SortDirection.ASCENDING);
  }

  @Test
  void should_ApplySortDirectionAsc_When_OrderByIsEmpty() {
    MockQuery query = mock(MockQuery.class);
    MockSortBy sortBy = mock(MockSortBy.class);

    QuerySortParameter<MockQuery, MockSortBy> sortByParameter =
        new QuerySortParameter<>(List.of(sortBy), List.of());

    sortByParameter.apply(query);

    verify(sortBy).applySortByForQuery(query, SortDirection.ASCENDING);
  }

  @Test
  void should_ApplySortByDesc_When_OrderByIsDesc() {
    MockQuery query = mock(MockQuery.class);
    MockSortBy sortBy = mock(MockSortBy.class);

    QuerySortParameter<MockQuery, MockSortBy> sortByParameter =
        new QuerySortParameter<>(List.of(sortBy), List.of(SortDirection.DESCENDING));

    sortByParameter.apply(query);

    verify(sortBy).applySortByForQuery(query, SortDirection.DESCENDING);
  }

  @Test
  void should_ApplySortByMultipleTimes_When_SortByListContainsMultipleElements() {
    MockQuery query = mock(MockQuery.class);
    MockSortBy sortBy1 = mock(MockSortBy.class);
    MockSortBy sortBy2 = mock(MockSortBy.class);

    QuerySortParameter<MockQuery, MockSortBy> sortByParameter =
        new QuerySortParameter<>(
            List.of(sortBy1, sortBy2), List.of(SortDirection.ASCENDING, SortDirection.ASCENDING));

    sortByParameter.apply(query);

    verify(sortBy1).applySortByForQuery(query, SortDirection.ASCENDING);
    verify(sortBy2).applySortByForQuery(query, SortDirection.ASCENDING);
  }

  @Test
  void should_ThrowException_When_SortByAndOrderByLengthDoesNotMatch() {
    MockSortBy sortBy = mock(MockSortBy.class);
    SortDirection sortDirection = SortDirection.ASCENDING;
    List<MockSortBy> sortBy1 = List.of(sortBy, sortBy);
    List<SortDirection> sortDirection1 = List.of(sortDirection);
    assertThatThrownBy(
            () -> new QuerySortParameter<>(sortBy1, sortDirection1))
        .isInstanceOf(InvalidArgumentException.class);
    List<MockSortBy> sortBy2 = List.of(sortBy);
    List<SortDirection> sortDirection2 = List.of(sortDirection, sortDirection);
    assertThatThrownBy(
            () -> new QuerySortParameter<>(sortBy2, sortDirection2))
        .isInstanceOf(InvalidArgumentException.class);
  }

  @Test
  void should_ThrowException_When_SortByIsNull() {
    List<SortDirection> order = List.of();
    assertThatThrownBy(() -> new QuerySortParameter<>(null, order))
        .isInstanceOf(InvalidArgumentException.class);
  }

  private enum MockColumnNames implements QueryColumnName {}

  private abstract static class MockSortBy implements QuerySortBy<MockQuery> {}

  private abstract static class MockQuery implements BaseQuery<Void, MockColumnNames> {}
}
