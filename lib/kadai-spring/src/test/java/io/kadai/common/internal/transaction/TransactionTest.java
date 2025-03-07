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

package io.kadai.common.internal.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.task.api.TaskService;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClient;

/** TODO. */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration("classpath:test-applicationContext.xml")
@EnableAutoConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@Disabled
class TransactionTest {

  @Autowired TaskService taskService;
  @LocalServerPort int port;
  private static RestClient restClient;

  @BeforeEach
  void init() throws Exception {
    restClient = RestClient.create();
    Class.forName("org.h2.Driver");
    try (Connection conn = getConnection()) {
      try (Statement statement = conn.createStatement()) {
        statement.executeUpdate("DELETE FROM TASK WHERE 1=1");
        conn.commit();
      }
    }
  }

  @Test
  void testCommit() throws Exception {
    restClient.get().uri("http://127.0.0.1:" + port + "/test").retrieve().toEntity(String.class);

    int resultCount = 0;
    try (Connection conn = getConnection()) {
      try (Statement statement = conn.createStatement()) {
        ResultSet rs = statement.executeQuery("SELECT ID FROM TASK");

        while (rs.next()) {
          resultCount++;
        }
      }
    }

    assertThat(resultCount).isOne();
  }

  @Test
  void testRollback() throws Exception {
    restClient.post().uri("http://127.0.0.1:" + port + "/test").retrieve().body(String.class);

    int resultCount = 0;
    try (Connection conn = getConnection()) {
      try (Statement statement = conn.createStatement()) {
        ResultSet rs = statement.executeQuery("SELECT ID FROM TASK");

        while (rs.next()) {
          resultCount++;
        }
      }

      assertThat(resultCount).isZero();
    }
  }

  private Connection getConnection() throws Exception {
    return DriverManager.getConnection(
        "jdbc:h2:mem:task-engine;NON_KEYWORDS=KEY,VALUE;IGNORECASE=TRUE;LOCK_MODE=0",
        "SA",
        UUID.randomUUID().toString());
  }
}
