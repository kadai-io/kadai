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

import io.kadai.testapi.WithServiceProvider.WithServiceProviders;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation allowing to declare, define and register an SPI into the KadaiEngine of a Test-Class
 * extended with
 * {@linkplain
 * io.kadai.testapi.extensions.KadaiInitializationExtension @KadaiInitializationExtension}.
 *
 * <p>Usage may look like:
 * <pre>
 *   {@code
 *     @WithServiceProvider(
 *       serviceProviderInterface = PriorityServiceProvider.class,
 *       serviceProviders = TestStaticValuePriorityServiceProvider.class)
 *     @ExtendWith(KadaiInitializationExtension.class)
 *     class MyTestClass {
 *       static class TestStaticValuePriorityServiceProvider implements PriorityServiceProvider {
 *          @Override
 *          public OptionalInt calculatePriority(TaskSummary taskSummary) {
 *            return OptionalInt.of(10);
 *          }
 *        }
 *
 *       @Test
 *       void myTest() {
 *         // trigger some action that fires registered SPI
 *       }
 *     }
 *   }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(WithServiceProviders.class)
public @interface WithServiceProvider {

  /**
   * Returns the type of the interface of the SPI to implement.
   *
   * @return the type of the interface of the SPI to implement
   */
  Class<?> serviceProviderInterface();

  /**
   * Returns the types of the classes implementing the declared
   * {@linkplain #serviceProviderInterface() ServiceProviderInterface}.
   *
   * @return the types of the classes implementing the declared
   *     {@linkplain #serviceProviderInterface() ServiceProviderInterface}
   */
  Class<?>[] serviceProviders();

  /**
   * Annotation allowing to declare multiple types of
   * {@linkplain WithServiceProvider @WithServiceProvider}.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @interface WithServiceProviders {

    /**
     * Returns all declared {@linkplain WithServiceProvider @WithServiceProvider}.
     *
     * @return the array of all declared {@linkplain WithServiceProvider @WithServiceProvider}
     */
    WithServiceProvider[] value();
  }
}
