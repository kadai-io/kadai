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

import io.kadai.KadaiConfiguration;

/**
 * Interface specifying operations allowing to modify the {@link KadaiConfiguration} of an
 * integration test extended with
 * {@linkplain
 * io.kadai.testapi.extensions.KadaiInitializationExtension @KadaiInitializationExtension}.
 *
 * <p>Usage may look like:
 * <pre>
 *   {@code
 *     @ExtendWith(KadaiInitializationExtension.class)
 *     class MyTestClass implements KadaiEngineConfigurationModifier {
 *       @Override
 *       public KadaiConfiguration.Builder modify(KadaiConfiguration.Builder builder) {
 *         return builder.foo();
 *       }
 *
 *       @Test
 *       void myTest() {}
 *     }
 *   }
 *  * </pre>
 */
public interface KadaiConfigurationModifier {

  /**
   * Modifies the {@link KadaiConfiguration.Builder}.
   *
   * @param builder the builder to modify
   * @return the modified builder
   */
  KadaiConfiguration.Builder modify(KadaiConfiguration.Builder builder);
}
