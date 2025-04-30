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

package io.kadai.common.internal.configuration.parser;

import static java.util.function.Predicate.not;

import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.internal.util.ReflectionUtil;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

public class SimpleParser<T> implements PropertyParser<T> {

  private final Class<?> targetClass;
  private final Function<String, T> parseFunction;

  public SimpleParser(Class<?> targetClass, Function<String, T> parseFunction) {
    this.targetClass = targetClass;
    this.parseFunction = parseFunction;
  }

  protected static List<String> splitStringAndTrimElements(String str, String separator) {
    return Arrays.stream(str.split(Pattern.quote(separator)))
        .filter(not(String::isEmpty))
        .map(String::trim)
        .toList();
  }

  @Override
  public Optional<T> parse(String value, String separator, Type type) {
    Class<?> rawClass = ReflectionUtil.getRawClass(type);
    if (!getTargetClass().isAssignableFrom(ReflectionUtil.wrap(rawClass))) {
      throw new SystemException(
          String.format(
              "Cannot initialize '%s' because target type '%s' is not a '%s'",
              value, rawClass, getTargetClass()));
    }
    return Optional.ofNullable(value).map(it -> parseFunction.apply(value));
  }

  @Override
  public Class<?> getTargetClass() {
    return targetClass;
  }
}
