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

package io.kadai.testapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation allowing to declare the cleansing of the KadaiContext in {@linkplain
 * org.junit.jupiter.api.Nested @Nested} Test-Classes.
 *
 * <p>It makes the annotated class reuse the surrounding classes' {@linkplain javax.sql.DataSource
 * DataSource} while generating a new {@linkplain io.kadai.common.api.KadaiEngine KadaiEngine} and
 * schema.
 *
 * <p>Usage may look like:
 *
 * <pre>{@code
 * @ExtendWith(KadaiInitializationExtension.class)
 * class MyTestClass {
 *
 *   @Nested
 *   @CleanKadaiContext
 *   @TestInstance(Lifecycle.PER_CLASS)
 *   class MyNestedTestClass {}
 * }
 *
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CleanKadaiContext {}
