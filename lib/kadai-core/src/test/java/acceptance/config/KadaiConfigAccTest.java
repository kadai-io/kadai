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

package acceptance.config;

import static io.kadai.common.api.SharedConstants.MASTER_DOMAIN;
import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.KadaiConfiguration;
import io.kadai.common.test.config.DataSourceGenerator;
import io.kadai.workbasket.api.WorkbasketPermission;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Test kadai configuration without roles. */
class KadaiConfigAccTest {

  @TempDir Path tempDir;
  private KadaiConfiguration kadaiConfiguration;

  @BeforeEach
  void setup() {
    kadaiConfiguration =
        new KadaiConfiguration.Builder(
                DataSourceGenerator.getDataSource(), true, DataSourceGenerator.getSchemaName())
            .initKadaiProperties()
            .build();
  }

  @Test
  void should_ConfigureDomains_For_DefaultPropertiesFile() {
    assertThat(kadaiConfiguration.getDomains())
        .containsExactlyInAnyOrder("DOMAIN_A", "DOMAIN_B", MASTER_DOMAIN);
  }

  @Test
  void should_ConfigureMinimalPermissionsToAssignDomains_For_DefaultPropertiesFile() {
    assertThat(kadaiConfiguration.getMinimalPermissionsToAssignDomains())
        .containsExactlyInAnyOrder(WorkbasketPermission.READ, WorkbasketPermission.OPEN);
  }

  @Test
  void should_ConfigureClassificationTypes_For_DefaultPropertiesFile() {
    assertThat(kadaiConfiguration.getClassificationTypes())
        .containsExactlyInAnyOrder("TASK", "DOCUMENT");
  }

  @Test
  void should_ConfigureClassificationCategories_For_DefaultPropertiesFile() {
    assertThat(kadaiConfiguration.getClassificationCategoriesByType("TASK"))
        .containsExactlyInAnyOrder("EXTERNAL", "MANUAL", "AUTOMATIC", "PROCESS");
  }

  @Test
  void should_ApplyClassificationProperties_When_PropertiesAreDefined() throws Exception {
    String propertiesFileName = createNewConfigFile("dummyTestConfig3.properties", true, true);
    kadaiConfiguration =
        new KadaiConfiguration.Builder(
                DataSourceGenerator.getDataSource(),
                true,
                DataSourceGenerator.getSchemaName(),
                true)
            .initKadaiProperties(propertiesFileName)
            .build();
    assertThat(kadaiConfiguration.getClassificationCategoriesByType())
        .containsExactlyInAnyOrderEntriesOf(
            Map.ofEntries(
                Map.entry("TASK", List.of("EXTERNAL", "MANUAL", "AUTOMATIC", "PROCESS")),
                Map.entry("DOCUMENT", List.of("EXTERNAL"))));
  }

  private String createNewConfigFile(
      String filename, boolean addingTypes, boolean addingClassification)
      throws Exception {
    Path file = Files.createFile(tempDir.resolve(filename));
    List<String> lines =
        Stream.of(
                "kadai.roles.admin[0]=Holger",
                "kadai.roles.admin[1]=Stefan",
                "kadai.roles.business-admin[0]=ebe",
                "kadai.roles.business-admin[1]=konstantin",
                "kadai.roles.user[0]=nobody")
            .collect(Collectors.toList());
    if (addingTypes) {
      lines.add("kadai.classification.types[0]=TASK");
      lines.add("kadai.classification.types[1]=document");
    }
    if (addingClassification) {
      lines.add("kadai.classification.categories.task[0]=EXTERNAL");
      lines.add("kadai.classification.categories.task[1]=manual");
      lines.add("kadai.classification.categories.task[2]=autoMAtic");
      lines.add("kadai.classification.categories.task[3]=Process");
      lines.add("kadai.classification.categories.document[0]=EXTERNAL");
    }

    Files.write(file, lines, StandardCharsets.UTF_8);
    return file.toString();
  }
}
