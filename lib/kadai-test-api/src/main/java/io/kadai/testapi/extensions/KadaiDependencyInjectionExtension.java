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

package io.kadai.testapi.extensions;

import static io.kadai.testapi.util.ExtensionCommunicator.getClassLevelStore;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotatedFields;

import io.kadai.testapi.KadaiInject;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.platform.commons.JUnitException;

/**
 * JUnit-Extension for injecting Kadai components via {@linkplain KadaiInject @KadaiInject} or
 * supplying them as parameter to test-functions.
 *
 * <p>Usage may look like:
 * <pre>
 *   {@code
 *     @ExtendWith(KadaiDependencyInjectionExtension.class)
 *     class MyTestClass {
 *
 *       @KadaiInject KadaiEngine kadaiEngine;
 *
 *       @Test
 *       void myTest(TaskService taskService) {}
 *     }
 *   }
 *  * </pre>
 */
public class KadaiDependencyInjectionExtension
    implements ParameterResolver, TestInstancePostProcessor {

  @SuppressWarnings("unchecked")
  private static Map<Class<?>, Object> getKadaiEntityMap(ExtensionContext extensionContext) {
    return (Map<Class<?>, Object>)
        getClassLevelStore(extensionContext)
            .get(KadaiInitializationExtension.STORE_KADAI_ENTITY_MAP);
  }

  @Override
  public boolean supportsParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    Map<Class<?>, Object> instanceByClass = getKadaiEntityMap(extensionContext);
    return instanceByClass != null
        && instanceByClass.containsKey(parameterContext.getParameter().getType());
  }

  @Override
  public Object resolveParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return getKadaiEntityMap(extensionContext).get(parameterContext.getParameter().getType());
  }

  @Override
  public void postProcessTestInstance(Object testInstance, ExtensionContext context)
      throws Exception {
    Map<Class<?>, Object> instanceByClass = getKadaiEntityMap(context);
    if (instanceByClass == null) {
      throw new JUnitException("Something went wrong! Could not find KADAI entity Map in store.");
    }

    for (Field field : findAnnotatedFields(testInstance.getClass(), KadaiInject.class)) {
      Object toInject = instanceByClass.get(field.getType());
      if (toInject != null) {
        field.setAccessible(true);
        field.set(testInstance, toInject);
      } else {
        throw new JUnitException(
            String.format(
                "Cannot inject field '%s'. " + "Type '%s' is not an injectable KADAI type",
                field.getName(), field.getType()));
      }
    }
  }
}
