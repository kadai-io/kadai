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

package io.kadai.testapi;

import io.kadai.testapi.extensions.KadaiDependencyInjectionExtension;
import io.kadai.testapi.extensions.KadaiInitializationExtension;
import io.kadai.testapi.extensions.TestContainerExtension;
import io.kadai.testapi.security.JaasExtension;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Meta annotation for Kadai integration tests.
 *
 * <p>This annotation enables the following JUnit-Extensions:
 *
 * <ul>
 *   <li>{@link JaasExtension}
 *   <li>{@link TestContainerExtension}
 *   <li>{@link KadaiInitializationExtension}
 *   <li>{@link KadaiDependencyInjectionExtension}
 * </ul>
 *
 * <p>Usage may look like:
 * <pre>
 *   {@code
 *     @KadaiIntegrationTest
 *     class MyTestClass implements KadaiEngineConfigurationModifier {
 *       @KadaiInject InternalKadaiEngine internalKadaiEngine;
 *
 *       @Override
 *       public KadaiConfiguration.Builder modify(KadaiConfiguration.Builder builder) {
 *         return builder.foo();
 *       }
 *
 *       @Test
 *       @WithAccessId(user = "bar")
 *       void myTest() {
 *         internalKadaiEngine.baz(...);
 *       }
 *     }
 *   }
 * </pre>
 */
@ExtendWith({
  // ORDER IS IMPORTANT!
  JaasExtension.class,
  TestContainerExtension.class,
  KadaiInitializationExtension.class,
  KadaiDependencyInjectionExtension.class,
})
@TestInstance(Lifecycle.PER_CLASS)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface KadaiIntegrationTest {}
