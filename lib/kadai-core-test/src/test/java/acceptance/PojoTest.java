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

package acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.Rule;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.NoPublicFieldsRule;
import com.openpojo.validation.rule.impl.NoStaticExceptFinalRule;
import com.openpojo.validation.rule.impl.SetterMustExistRule;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.util.List;
import java.util.stream.Stream;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/** check classes with a custom equals and hashcode implementation for correctness. */
class PojoTest {

  private static final List<? extends Class<?>> POJO_CLASSES =
      new ClassFileImporter()
          .importPackages("io.kadai").stream()
              .filter(javaClass -> javaClass.tryGetMethod("equals", Object.class).isPresent())
              .filter(
                  javaClass ->
                      !javaClass.getSimpleName().equals("TaskHistoryEvent")
                          && !javaClass.getSimpleName().equals("WorkbasketHistoryEvent")
                          && !javaClass.getSimpleName().equals("ClassificationHistoryEvent")
                          && !javaClass.getSimpleName().equals("ComparableVersion")
                          && !javaClass.getSimpleName().equals("StringItem")
                          && !javaClass.getSimpleName().equals("BigIntegerItem")
                          && !javaClass.getSimpleName().equals("IntItem")
                          && !javaClass.getSimpleName().equals("LongItem")
                          // This is a record, it has a getter per definition
                          && !javaClass.getSimpleName().equals("DurationPrioHolder")
                          // This is a record, it has a getter per definition
                          && !javaClass.getSimpleName().equals("CustomHoliday"))
              .map(JavaClass::reflect)
              .toList();

  @Test
  void testsThatPojoClassesAreFound() {
    assertThat(POJO_CLASSES).isNotEmpty();
  }

  @TestFactory
  Stream<DynamicTest> equalsContract() {
    return POJO_CLASSES.stream()
        .map(
            cl ->
                DynamicTest.dynamicTest(
                    "Check Hash and Equals for " + cl.getSimpleName(),
                    () -> verifyHashAndEquals(cl)));
  }

  @TestFactory
  Stream<DynamicTest> validateGetters() {
    return POJO_CLASSES.stream()
        .map(
            cl ->
                DynamicTest.dynamicTest(
                    "Check Getter exist for " + cl.getSimpleName(),
                    () -> validateWithRules(cl, new GetterMustExistRule())));
  }

  @TestFactory
  Stream<DynamicTest> validateSetters() {
    return POJO_CLASSES.stream()
        .map(
            cl ->
                DynamicTest.dynamicTest(
                    "Check Setter for " + cl.getSimpleName(),
                    () -> validateWithRules(cl, new SetterMustExistRule())));
  }

  @TestFactory
  Stream<DynamicTest> validateNoStaticExceptFinalFields() {
    return POJO_CLASSES.stream()
        .map(
            cl ->
                DynamicTest.dynamicTest(
                    "Check static fields for " + cl.getSimpleName(),
                    () -> validateWithRules(cl, new NoStaticExceptFinalRule())));
  }

  @TestFactory
  Stream<DynamicTest> validateNoPublicFields() {
    return POJO_CLASSES.stream()
        .map(
            cl ->
                DynamicTest.dynamicTest(
                    "Check public fields for " + cl.getSimpleName(),
                    () -> validateWithRules(cl, new NoPublicFieldsRule())));
  }

  private void validateWithRules(Class<?> cl, Rule... rules) {
    ValidatorBuilder.create().with(rules).build().validate(PojoClassFactory.getPojoClass(cl));
  }

  private void verifyHashAndEquals(Class<?> cl) {
    EqualsVerifier.forClass(cl)
        .suppress(Warning.NONFINAL_FIELDS, Warning.STRICT_INHERITANCE)
        .withRedefinedSuperclass()
        .verify();
  }
}
