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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation allowing to inject Kadai components in Test-Classes.
 *
 * <p>This annotation requires the surrounding class to be extended with the JUnit-Extension
 * {@linkplain
 * io.kadai.testapi.extensions.KadaiDependencyInjectionExtension KadaiDependencyInjectionExtension}.
 *
 * <p>Usage may look like:
 * <pre>
 *   {@code
 *     @ExtendWith(KadaiDependencyInjectionExtension.class)
 *     class MyTestClass {
 *       @KadaiInject InternalKadaiEngine internalKadaiEngine;
 *
 *       @Test
 *       void myTest() {
 *         internalKadaiEngine.foo(...);
 *       }
 *     }
 *   }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface KadaiInject {}
