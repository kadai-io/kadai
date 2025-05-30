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

package acceptance.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.common.internal.ConfigurationMapper;
import io.kadai.common.internal.ConfigurationServiceImpl;
import io.kadai.common.internal.util.ResourceUtil;
import io.kadai.testapi.KadaiInject;
import io.kadai.testapi.KadaiIntegrationTest;
import java.util.Map;
import java.util.Optional;
import org.json.JSONObject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

@KadaiIntegrationTest
public class ConfigurationServiceImplAccTest {

  @KadaiInject ConfigurationServiceImpl configurationService;
  @KadaiInject ConfigurationMapper configurationMapper;

  @Nested
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @TestInstance(Lifecycle.PER_CLASS)
  class CustomAttribute {

    @Test
    @Order(1)
    void should_SetDefaultCustomAttributesDuringKadaiInitialization_When_NoCustomAttributesAreSet()
        throws Exception {
      Map<String, Object> expectedCustomAttributes =
          new JSONObject(
                  ResourceUtil.readResourceAsString(
                      ConfigurationServiceImpl.class, "defaultCustomAttributes.json"))
              .toMap();

      Map<String, Object> allCustomAttributes = configurationMapper.getAllCustomAttributes(false);

      assertThat(allCustomAttributes).isEqualTo(expectedCustomAttributes);
    }

    @Test
    void should_NotSetDefaultCustomAttributes_When_CustomAttributesAreAlreadySet() {
      Map<String, String> foo = Map.of("foo", "bar");
      configurationService.setAllCustomAttributes(foo);

      configurationService.setupDefaultCustomAttributes();
      Map<String, Object> allCustomAttributes = configurationService.getAllCustomAttributes();

      assertThat(allCustomAttributes).isEqualTo(foo);
    }

    @Test
    void should_SetCustomAttributes() {
      Map<String, String> foo = Map.of("foo1", "bar");
      configurationService.setAllCustomAttributes(foo);

      Map<String, Object> allCustomAttributes = configurationService.getAllCustomAttributes();

      assertThat(allCustomAttributes).isEqualTo(foo);
    }

    @Test
    void should_RetrieveCustomAttributes() {
      Map<String, String> foo = Map.of("foo2", "bar");
      configurationService.setAllCustomAttributes(foo);

      Optional<Object> customAttribute = configurationService.getValue("foo2");

      assertThat(customAttribute).hasValue("bar");
    }

    @Test
    void should_ReturnNull_When_CustomAttributeDoesNotExist() {
      Map<String, String> foo = Map.of("foo3", "bar");
      configurationService.setAllCustomAttributes(foo);

      Optional<Object> customAttribute = configurationService.getValue("doesNotExist");

      assertThat(customAttribute).isEmpty();
    }
  }
}
