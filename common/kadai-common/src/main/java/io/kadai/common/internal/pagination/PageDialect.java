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

package io.kadai.common.internal.pagination;

import org.apache.ibatis.session.RowBounds;

/** Interface specifying operations for a databases pagination-dialect. */
public interface PageDialect {

  /**
   * Returns true if given SQL-String is natively paginated for this dialect.
   *
   * @param sql String to determine pagination for
   * @return true if given SQL is natively paginated for this dialect, false otherwise
   */
  boolean isPaginated(String sql);

  /**
   * Transforms the given SQL-String by natively adding SQL-Syntax such that given {@link RowBounds}
   * is encoded physically inside the SQL-String.
   *
   * @param sql String to transform
   * @param rowBounds bounds to apply physically
   * @return sql with physically applied bounds
   */
  String transform(String sql, RowBounds rowBounds);
}
