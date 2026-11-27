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

package io.kadai.task.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import io.kadai.classification.api.ClassificationCustomField;
import io.kadai.task.api.TaskQuery;
import java.beans.ConstructorProperties;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.ThrowingConsumer;

/** Test for {@link TaskQueryFilterClassificationCustomFields}. */
class TaskQueryFilterClassificationCustomFieldsTest {

  private static final List<Operation> OPERATIONS =
      List.of(
          new Operation("", "In"),
          new Operation("-not", "NotIn"),
          new Operation("-like", "Like"),
          new Operation("-not-like", "NotLike"));

  @TestFactory
  Stream<DynamicTest> should_ApplyEachParameter_To_TheMatchingClassificationCustomAttribute() {
    Stream<Parameter> parameters =
        Arrays.stream(ClassificationCustomField.values())
            .flatMap(customField ->
                OPERATIONS.stream().map(operation -> new Parameter(customField, operation)));

    ThrowingConsumer<Parameter> test =
        parameter -> {
          Constructor<?> constructor =
              TaskQueryFilterClassificationCustomFields.class.getConstructors()[0];
          String[] propertyNames = constructor.getAnnotation(ConstructorProperties.class).value();
          int index = parameter.index();
          String[] values = {"Neuschaden", "Vorlagen"};
          Object[] arguments = new Object[constructor.getParameterCount()];
          arguments[index] = values;

          TaskQueryFilterClassificationCustomFields filter =
              (TaskQueryFilterClassificationCustomFields) constructor.newInstance(arguments);
          TaskQuery query = mock(TaskQuery.class);
          filter.apply(query);

          assertThat(propertyNames[index]).isEqualTo(parameter.restName());
          assertThat(
                  TaskQueryFilterClassificationCustomFields.class
                      .getMethod(
                          "getClassificationCustom" + parameter.number() + parameter.suffix())
                      .invoke(filter))
              .isSameAs(values);
          verifyQueryCall(query, parameter, values);
          verifyNoMoreInteractions(query);
        };

    return DynamicTest.stream(parameters, Parameter::restName, test);
  }

  private static void verifyQueryCall(TaskQuery query, Parameter parameter, String[] values) {
    ClassificationCustomField customField = parameter.customField();
    String[] likeValues = Arrays.stream(values).map(v -> "%" + v + "%").toArray(String[]::new);
    switch (parameter.operation().suffix()) {
      case "In" -> verify(query).classificationCustomAttributeIn(customField, values);
      case "NotIn" -> verify(query).classificationCustomAttributeNotIn(customField, values);
      case "Like" -> verify(query).classificationCustomAttributeLike(customField, likeValues);
      case "NotLike" -> verify(query).classificationCustomAttributeNotLike(customField, likeValues);
      default -> throw new IllegalStateException("Unknown operation " + parameter.operation());
    }
  }

  private record Operation(String restSuffix, String suffix) {}

  private record Parameter(ClassificationCustomField customField, Operation operation) {

    int number() {
      return customField.ordinal() + 1;
    }

    int index() {
      return customField.ordinal() * OPERATIONS.size() + OPERATIONS.indexOf(operation);
    }

    String suffix() {
      return operation.suffix();
    }

    String restName() {
      return "classification-custom-" + number() + operation.restSuffix();
    }
  }
}
