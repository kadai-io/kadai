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

package io.kadai.common.internal.configuration;

import io.kadai.common.internal.util.ComparableVersion;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.ibatis.jdbc.RuntimeSqlException;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.jdbc.SqlRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class create the schema for kadai. */
public class DbSchemaCreator {

  private static final Logger LOGGER = LoggerFactory.getLogger(DbSchemaCreator.class);
  private final String schemaName;
  private final StringWriter outWriter = new StringWriter();
  private final PrintWriter logWriter = new PrintWriter(outWriter);
  private final StringWriter errorWriter = new StringWriter();
  private final PrintWriter errorLogWriter = new PrintWriter(errorWriter);
  private DataSource dataSource;

  public DbSchemaCreator(DataSource dataSource, String schema) {
    this.dataSource = dataSource;
    this.schemaName = schema;
  }

  /**
   * Run all db scripts.
   *
   * @return true when schema was created, false when no schema created because already existing
   * @throws SQLException will be thrown if there will be some incorrect SQL statements invoked.
   */
  public boolean run() throws SQLException {
    try (Connection connection = dataSource.getConnection()) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(
            "Using database of type {} with url '{}'",
            DB.getDB(connection).dbProductName,
            connection.getMetaData().getURL());
      }
      DB db = DB.getDB(connection);

      ScriptRunner runner = getScriptRunnerInstance(connection);

      if (!isSchemaPreexisting(connection, db)) {
        String scriptPath = db.schemaScript;
        InputStream resourceAsStream = DbSchemaCreator.class.getResourceAsStream(scriptPath);
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8));
        runner.runScript(getSqlSchemaNameParsed(reader));
        return true;
      }
    }
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(outWriter.toString());
    }
    if (!errorWriter.getBuffer().isEmpty()) {
      String errorLogMessage = errorWriter.toString();
      LOGGER.error(errorLogMessage);
    }
    return false;
  }

  public boolean isValidSchemaVersion(String expectedMinVersion) {
    try (Connection connection = dataSource.getConnection()) {
      connection.setSchema(this.schemaName);
      SqlRunner runner = new SqlRunner(connection);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("{}", connection.getMetaData());
      }

      ComparableVersion actualVersion = getActualMaxVersion(runner);
      ComparableVersion minVersion = ComparableVersion.of(expectedMinVersion);

      if (actualVersion.compareTo(minVersion) < 0) {
        LOGGER.error(
            "Schema version not valid. The VERSION property in table KADAI_SCHEMA_VERSION "
                + "has not the expected min value {}",
            expectedMinVersion);
        return false;
      } else {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Schema version is valid.");
        }
        return true;
      }

    } catch (RuntimeSqlException | SQLException e) {
      LOGGER.error(
          "Schema version not valid. The VERSION property in table KADAI_SCHEMA_VERSION "
              + "has not the expected min value {}",
          expectedMinVersion);
      return false;
    }
  }

  /**
   * Retrieves the maximum version from the KADAI_SCHEMA_VERSION table.
   *
   * <p>This method executes a SQL query to fetch all version entries from the
   * KADAI_SCHEMA_VERSION table, converts them to ComparableVersion objects, and
   * determines the maximum version. If no versions are found, an IllegalStateException
   * is thrown.
   *
   * @param runner the SqlRunner instance used to execute the query
   * @return the maximum version as a ComparableVersion object
   * @throws SQLException if a database access error occurs
   * @throws IllegalStateException if no version exists in the KADAI_SCHEMA_VERSION table
   */
  public ComparableVersion getActualMaxVersion(SqlRunner runner) throws SQLException {
    String query = "select VERSION from KADAI_SCHEMA_VERSION";
    return runner.selectAll(query).stream()
        .map(res -> (String) res.get("VERSION"))
        .map(ComparableVersion::of)
        .max(ComparableVersion::compareTo)
        .orElseThrow(
            () -> new IllegalStateException("There exists no version in KADAI_SCHEMA_VERSION"));
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public String getSchemaName() {
    return schemaName;
  }

  private ScriptRunner getScriptRunnerInstance(Connection connection) {
    ScriptRunner runner = new ScriptRunner(connection);
    runner.setStopOnError(true);
    runner.setLogWriter(logWriter);
    runner.setErrorLogWriter(errorLogWriter);
    return runner;
  }

  private boolean isSchemaPreexisting(Connection connection, DB db) {
    ScriptRunner runner = getScriptRunnerInstance(connection);
    runner.setErrorLogWriter(errorLogWriter);

    String scriptPath = db.detectionScript;
    try (InputStream resource = DbSchemaCreator.class.getResourceAsStream(scriptPath);
        InputStreamReader inputReader = new InputStreamReader(resource, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(inputReader)) {
      runner.runScript(getSqlSchemaNameParsed(reader));
    } catch (RuntimeSqlException | IOException e) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Schema does not exist.");
        if (!errorWriter.toString().trim().isEmpty()) {
          LOGGER.debug(errorWriter.toString());
        }
      }
      return false;
    }
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Schema does exist.");
    }
    return true;
  }

  private StringReader getSqlSchemaNameParsed(BufferedReader reader) {

    StringBuilder content = new StringBuilder();
    try {
      String line = "";
      while (line != null) {
        line = reader.readLine();
        if (line != null) {
          content.append(line.replace("%schemaName%", schemaName)).append(System.lineSeparator());
        }
      }
    } catch (IOException e) {
      LOGGER.error("SchemaName sql parsing failed for schemaName {}", schemaName, e);
    }
    return new StringReader(content.toString());
  }
}
