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

package acceptance;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.kadai.KadaiConfiguration;
import io.kadai.KadaiConfiguration.Builder;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.KadaiEngine.ConnectionManagementMode;
import io.kadai.common.internal.KadaiEngineImpl;
import io.kadai.spi.priority.api.PriorityServiceProvider;
import io.kadai.spi.priority.internal.PriorityServiceManager;
import io.kadai.task.api.models.TaskSummary;
import io.kadai.testapi.KadaiInject;
import io.kadai.testapi.KadaiIntegrationTest;
import io.kadai.testapi.WithServiceProvider;
import io.kadai.testapi.extensions.TestContainerExtension;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
@KadaiIntegrationTest
public class KadaiEngineInitializationTest {

  static class MyPriorityServiceProvider implements PriorityServiceProvider {

    private KadaiEngine kadaiEngine;

    @Override
    public void initialize(KadaiEngine kadaiEngine) {
      this.kadaiEngine = kadaiEngine;
    }

    @Override
    public OptionalInt calculatePriority(TaskSummary taskSummary) {
      return OptionalInt.empty();
    }
  }

  @Test
  void should_FilterKadaiProperties_When_Logging(CapturedOutput output) throws SQLException {
    // should filter out properties that are not kadai.* and also sensitive properties containing
    // passwords
    KadaiConfiguration kadaiConfiguration =
        new Builder(TestContainerExtension.createDataSourceForH2(), true, "KADAI")
            .initKadaiProperties("/fullKadai.properties")
            .build();

    KadaiEngineImpl.createKadaiEngine(
        kadaiConfiguration, ConnectionManagementMode.AUTOCOMMIT, null);
    assertThat(output.getAll()).isNotEmpty();
    String[] properties = extractPropertiesSection(output.getAll()).split(",");
    for (String property : properties) {

      assertThat(property.trim().split("=")[0].trim()).startsWith("kadai.");
      assertThat(property.toLowerCase()).doesNotContain("password");
    }
  }

  private String extractPropertiesSection(String logOutput) {
    String regex = "properties=\\{([^}]+)\\}";
    Matcher matcher = Pattern.compile(regex).matcher(logOutput);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return null;
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  @WithServiceProvider(
      serviceProviderInterface = PriorityServiceProvider.class,
      serviceProviders = MyPriorityServiceProvider.class)
  class PriorityServiceManagerTest {
    @KadaiInject KadaiEngineImpl kadaiEngine;

    @Test
    @SuppressWarnings("unchecked")
    void should_InitializePriorityServiceProviders() throws Exception {
      PriorityServiceManager priorityServiceManager = kadaiEngine.getPriorityServiceManager();
      Field priorityServiceProvidersField =
          PriorityServiceManager.class.getDeclaredField("priorityServiceProviders");
      priorityServiceProvidersField.setAccessible(true);
      List<PriorityServiceProvider> serviceProviders =
          (List<PriorityServiceProvider>) priorityServiceProvidersField.get(priorityServiceManager);

      assertThat(priorityServiceManager.isEnabled()).isTrue();
      assertThat(serviceProviders)
          .asInstanceOf(InstanceOfAssertFactories.LIST)
          .hasOnlyElementsOfType(MyPriorityServiceProvider.class)
          .extracting(MyPriorityServiceProvider.class::cast)
          .extracting(sp -> sp.kadaiEngine)
          .containsOnly(kadaiEngine);
    }
  }
}
