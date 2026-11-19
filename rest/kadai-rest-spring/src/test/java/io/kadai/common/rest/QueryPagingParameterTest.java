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

import io.kadai.common.api.BaseQuery;
import io.kadai.common.api.QueryColumnName;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class QueryPagingParameterTest {

  @ParameterizedTest
  @ValueSource(ints = {0, -1, -5, Integer.MIN_VALUE})
  void should_ThrowException_When_PageIsLessThanOne(int page) {
    assertThatThrownBy(() -> new QueryPagingParameter<Void, MockQuery>(page, 5))
        .isInstanceOf(InvalidArgumentException.class)
        .hasMessageContaining("'page'")
        .hasMessageContaining(String.valueOf(page));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1, -5, Integer.MIN_VALUE})
  void should_ThrowException_When_PageSizeIsLessThanOne(int pageSize) {
    assertThatThrownBy(() -> new QueryPagingParameter<Void, MockQuery>(1, pageSize))
        .isInstanceOf(InvalidArgumentException.class)
        .hasMessageContaining("'page-size'")
        .hasMessageContaining(String.valueOf(pageSize));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -5})
  void should_ThrowException_When_PageSizeIsLessThanOneAndPageIsMissing(int pageSize) {
    assertThatThrownBy(() -> new QueryPagingParameter<Void, MockQuery>(null, pageSize))
        .isInstanceOf(InvalidArgumentException.class)
        .hasMessageContaining("'page-size'");
  }

  @Test
  void should_AcceptPageAndPageSize_When_BothAreOne() throws Exception {
    QueryPagingParameter<Void, MockQuery> pagingParameter = new QueryPagingParameter<>(1, 1);

    assertThat(pagingParameter.getPage()).isOne();
    assertThat(pagingParameter.getPageSize()).isOne();
  }

  @Test
  void should_DefaultPageSizeToMaxInteger_When_PageSizeIsMissing() throws Exception {
    QueryPagingParameter<Void, MockQuery> pagingParameter = new QueryPagingParameter<>(null, null);

    assertThat(pagingParameter.getPage()).isNull();
    assertThat(pagingParameter.getPageSize()).isEqualTo(Integer.MAX_VALUE);
  }

  private enum MockColumnNames implements QueryColumnName {}

  private abstract static class MockQuery implements BaseQuery<Void, MockColumnNames> {}
}
