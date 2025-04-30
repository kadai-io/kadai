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

package io.kadai.testapi.security;

import io.kadai.user.api.models.User;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation allowing to declare the Jaas-Context on methods in Test-Classes.
 *
 * <p>This annotation requires the surrounding method or class to be extended with the
 * JUnit-Extension {@link JaasExtension}.
 *
 * <p>Usage may look like:
 * <pre>
 *   {@code
 *     @ExtendWith(JaasExtension.class)
 *     class MyTestClass {
 *       @Test
 *       @WithAccessId(user = "foo")
 *       void myTest() {}
 *     }
 *   }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Repeatable(WithAccessId.WithAccessIds.class)
public @interface WithAccessId {

  /**
   * Returns the {@linkplain User#getId() id} of the user for this context.
   *
   * @return the id of the user
   */
  String user();

  /**
   * Returns the {@linkplain User#getGroups() groups} of the user for this context.
   *
   * <p>Defaults to none.
   *
   * @return the groups of the user
   */
  String[] groups() default {};

  /**
   * Returns the {@linkplain User#getPermissions() permissions} of the user for this context.
   *
   * <p>Defaults to none.
   *
   * @return the permissions of the user
   */
  String[] permissions() default {};

  /**
   * Annotation allowing to declare multiple {@link WithAccessId}, executing the annotated method
   * for each provided {@link WithAccessId} separately.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  @interface WithAccessIds {

    /**
     * Returns all {@link WithAccessId} to run the annotated method with.
     *
     * @return the array of all {@link WithAccessId}-Annotations
     */
    WithAccessId[] value();
  }
}
