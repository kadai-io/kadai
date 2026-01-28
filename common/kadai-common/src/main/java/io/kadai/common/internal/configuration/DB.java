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

package io.kadai.common.internal.configuration;

import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.api.exceptions.UnsupportedDatabaseException;
import io.kadai.common.internal.pagination.Db2PageDialect;
import io.kadai.common.internal.pagination.H2PageDialect;
import io.kadai.common.internal.pagination.PageDialect;
import io.kadai.common.internal.pagination.PostgresPageDialect;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

/** Supported versions of databases. */
public enum DB {
  H2(
      "H2",
      "h2",
      "/sql/h2/schema-detection-h2.sql",
      "/sql/h2/kadai-schema-h2.sql",
      new H2PageDialect()),
  DB2(
      "DB2",
      "db2",
      "/sql/db2/schema-detection-db2.sql",
      "/sql/db2/kadai-schema-db2.sql",
      new Db2PageDialect()),
  POSTGRES(
      "PostgreSQL",
      "postgres",
      "/sql/postgres/schema-detection-postgres.sql",
      "/sql/postgres/kadai-schema-postgres.sql",
      new PostgresPageDialect()),
  ;

  public final String dbProductName;
  public final String dbProductId;
  public final String detectionScript;
  public final String schemaScript;
  public final PageDialect pageDialect;

  DB(
      String dbProductName,
      String dbProductId,
      String detectionScript,
      String schemaScript,
      PageDialect pageDialect) {
    this.dbProductName = dbProductName;
    this.dbProductId = dbProductId;
    this.detectionScript = detectionScript;
    this.schemaScript = schemaScript;
    this.pageDialect = pageDialect;
  }

  public static DB getDB(String dbProductId) {
    return Arrays.stream(DB.values())
        .filter(db -> dbProductId.contains(db.dbProductId))
        .findFirst()
        .orElseThrow(() -> new UnsupportedDatabaseException(dbProductId));
  }

  public static DB getDB(Connection connection) {
    String dbProductName = DB.getDatabaseProductName(connection);
    return Arrays.stream(DB.values())
        .filter(db -> dbProductName.contains(db.dbProductName))
        .findFirst()
        .orElseThrow(() -> new UnsupportedDatabaseException(dbProductName));
  }

  private static String getDatabaseProductName(Connection connection) {
    try {
      return connection.getMetaData().getDatabaseProductName();
    } catch (SQLException e) {
      throw new SystemException("Could not extract meta data from connection", e);
    }
  }
}
