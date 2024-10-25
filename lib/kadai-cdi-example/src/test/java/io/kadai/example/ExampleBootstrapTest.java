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

package io.kadai.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ExampleBootstrapTest {

  @Deployment(testable = false)
  public static Archive<?> createDeployment() throws IOException {
    Path kadaiH2Data = Path.of(System.getProperty("user.home"), "kadai-h2-data");
    if (Files.exists(kadaiH2Data)) {
      FileUtils.forceDelete(kadaiH2Data.toFile());
    }
    EnterpriseArchive deployment = ShrinkWrap.create(EnterpriseArchive.class, "kadai.ear");

    File[] libs =
        Maven.resolver()
            .loadPomFromFile("pom.xml")
            .importCompileAndRuntimeDependencies()
            .resolve()
            .withTransitivity()
            .asFile();
    deployment.addAsLibraries(libs);

    JavaArchive ejbModule = ShrinkWrap.create(JavaArchive.class, "kadai.jar");
    ejbModule.addClasses(KadaiEjb.class, ExampleBootstrap.class, ExampleStartupException.class);
    ejbModule.addAsResource("kadai.properties");
    deployment.addAsModule(ejbModule);

    deployment.addAsManifestResource("META-INF/beans.xml", "beans.xml");

    return deployment;
  }

  @Test
  public void should_count_tasks_after_example_cdi_application_was_deployed() throws Exception {
    assertThat(countTasksByName("BootstrapTask")).isEqualTo(1);
  }

  private Connection getConnection() throws Exception {
    return DriverManager.getConnection(
        "jdbc:h2:~/kadai-h2-data/testdb;NON_KEYWORDS=KEY,VALUE;AUTO_SERVER=TRUE;"
            + "IGNORECASE=TRUE;LOCK_MODE=0",
        "sa",
        "sa");
  }

  private int countTasksByName(String taskName) throws Exception {

    Class.forName("org.h2.Driver");
    int resultCount = 0;
    try (Connection conn = getConnection();
        PreparedStatement statement =
            conn.prepareStatement("SELECT COUNT(ID) FROM KADAI.TASK WHERE NAME = ?")) {
      statement.setString(1, taskName);
      ResultSet rs = statement.executeQuery();

      while (rs.next()) {
        resultCount = rs.getInt(1);
      }
    }
    return resultCount;
  }
}
