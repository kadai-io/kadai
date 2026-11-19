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

package io.kadai.common.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.kadai.common.api.BaseQuery;
import io.kadai.common.api.QueryColumnName;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class QueryPagingParameterTest {

  @ParameterizedTest
  @ValueSource(ints = {0, -1, -5, Integer.MIN_VALUE})
  void should_ThrowException_When_PageIsLessThanOne(int page) {
    assertThatThrownBy(() -> new QueryPagingParameter<String, MockQuery>(page, 5))
        .isInstanceOf(InvalidArgumentException.class)
        .hasMessageContaining("'page'")
        .hasMessageContaining(String.valueOf(page));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1, -5, Integer.MIN_VALUE})
  void should_ThrowException_When_PageSizeIsLessThanOne(int pageSize) {
    assertThatThrownBy(() -> new QueryPagingParameter<String, MockQuery>(1, pageSize))
        .isInstanceOf(InvalidArgumentException.class)
        .hasMessageContaining("'page-size'")
        .hasMessageContaining(String.valueOf(pageSize));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -5})
  void should_ThrowException_When_PageSizeIsLessThanOneAndPageIsMissing(int pageSize) {
    assertThatThrownBy(() -> new QueryPagingParameter<String, MockQuery>(null, pageSize))
        .isInstanceOf(InvalidArgumentException.class)
        .hasMessageContaining("'page-size'");
  }

  @Test
  void should_AcceptPageAndPageSize_When_BothAreOne() {
    QueryPagingParameter<String, MockQuery> pagingParameter = new QueryPagingParameter<>(1, 1);

    assertThat(pagingParameter.getPage()).isOne();
    assertThat(pagingParameter.getPageSize()).isOne();
  }

  @Test
  void should_DefaultPageSizeToMaxInteger_When_PageSizeIsMissing() {
    QueryPagingParameter<String, MockQuery> pagingParameter =
        new QueryPagingParameter<>(null, null);

    assertThat(pagingParameter.getPage()).isNull();
    assertThat(pagingParameter.getPageSize()).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  void should_ListRequestedPage_When_PageIsWithinResults() {
    MockQuery query = mock(MockQuery.class);
    when(query.count()).thenReturn(10L);
    when(query.listPage(2, 5)).thenReturn(List.of("a", "b", "c", "d", "e"));
    QueryPagingParameter<String, MockQuery> pagingParameter = new QueryPagingParameter<>(2, 5);

    List<String> result = pagingParameter.apply(query);

    assertThat(result).containsExactly("a", "b", "c", "d", "e");
    assertThat(pagingParameter.getPageMetadata().getTotalPages()).isEqualTo(2);
  }

  @Test
  void should_ReturnEmptyList_When_PageIsBeyondLastPage() {
    MockQuery query = mock(MockQuery.class);
    when(query.count()).thenReturn(10L);
    QueryPagingParameter<String, MockQuery> pagingParameter = new QueryPagingParameter<>(3, 5);

    List<String> result = pagingParameter.apply(query);

    assertThat(result).isEmpty();
    verify(query, never()).listPage(anyInt(), anyInt());
  }

  @Test
  void should_ReturnEmptyList_When_OffsetExceedsIntegerRange() {
    MockQuery query = mock(MockQuery.class);
    when(query.count()).thenReturn(10L);
    // without page-size the page size defaults to Integer.MAX_VALUE, so the offset of page 3
    // would overflow when computed as int
    QueryPagingParameter<String, MockQuery> pagingParameter = new QueryPagingParameter<>(3, null);

    List<String> result = pagingParameter.apply(query);

    assertThat(result).isEmpty();
    verify(query, never()).listPage(anyInt(), anyInt());
  }

  private enum MockColumnNames implements QueryColumnName {}

  private abstract static class MockQuery implements BaseQuery<String, MockColumnNames> {}
}
