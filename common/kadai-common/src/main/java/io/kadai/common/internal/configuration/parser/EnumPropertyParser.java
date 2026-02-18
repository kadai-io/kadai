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

package io.kadai.common.internal.configuration.parser;

import io.kadai.common.api.exceptions.SystemException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EnumPropertyParser implements PropertyParser<Enum<?>> {
  @Override
  public Class<?> getTargetClass() {
    return Enum.class;
  }

  @Override
  public boolean validateTargetField(Field field) {
    return field.getType().isEnum();
  }

  @Override
  public Optional<Enum<?>> parse(String value, String separator, Type type) {
    Class<?> targetClass = (Class<?>) type;
    if (!targetClass.isEnum()) {
      throw new SystemException(
          String.format(
              "Cannot initialize '%s' because target type '%s' is not a '%s'",
              value, targetClass, getTargetClass()));
    }
    return Optional.ofNullable(value)
        .map(
            v -> {
              Map<String, ?> enumConstantsByLowerCaseName =
                  Arrays.stream(targetClass.getEnumConstants())
                      .collect(
                          Collectors.toMap(e -> e.toString().toLowerCase(), Function.identity()));
              Enum<?> enumConstant = (Enum<?>) enumConstantsByLowerCaseName.get(v.toLowerCase());
              if (enumConstant == null) {
                throw new SystemException(
                    String.format(
                        "Invalid property value '%s': Valid values are '%s' or '%s",
                        v,
                        enumConstantsByLowerCaseName.keySet(),
                        Arrays.toString(targetClass.getEnumConstants())));
              }
              return enumConstant;
            });
  }
}
