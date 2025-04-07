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

package io.kadai.common.internal.pagination;

import static java.lang.Math.max;

import org.apache.ibatis.session.RowBounds;

public class PostgresPageDialect implements PageDialect {

  @Override
  public boolean isPaginated(String sql) {
    final String lowerSql = sql.toLowerCase();
    return lowerSql.contains("offset") && lowerSql.contains("limit");
  }

  @Override
  public String transform(String sql, RowBounds rowBounds) {
    return String.format(
        "%s LIMIT %d OFFSET %d", sql, max(0, rowBounds.getLimit()), max(0, rowBounds.getOffset()));
  }
}
