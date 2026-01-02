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

package io.kadai.example;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    classes = KadaiConfigTestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"inmemorydb", "dev"})
@Import({TransactionalJobsConfiguration.class})
// This BootstrapTest must be executed before all other tests
// especially before KadaiTransactionIntTest (There is everything deleted...
// here we only test the execution of PostConstruct method
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@Order(1)
class ExampleBootstrapTest {

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void should_count_tasks_after_psotConstruc_method_was_executed() {
    Integer actualNumberOfTasks =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(ID) FROM TASK WHERE NAME = ?", Integer.class, "Spring example task");

    assertThat(actualNumberOfTasks).isEqualTo(1);
  }
}
