/*
 * Copyright [2024] [envite consulting GmbH]
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

package io.kadai.common.rest.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueryParamsValidator {

  private QueryParamsValidator() {
    throw new IllegalStateException("Utility class");
  }

  public static void validateParams(HttpServletRequest request, Class<?>... filterOrSortingClazz) {
    Set<String> allowedParams =
        Stream.of(filterOrSortingClazz)
            .flatMap(clazz -> Stream.of(clazz.getDeclaredFields()))
            .map(
                field ->
                    Optional.ofNullable(field.getDeclaredAnnotation(JsonProperty.class))
                        .map(JsonProperty::value)
                        .orElseGet(field::getName))
            .collect(Collectors.toSet());

    Set<String> providedParams = new HashSet<>(request.getParameterMap().keySet());

    providedParams.removeIf(allowedParams::contains);

    if (!providedParams.isEmpty()) {
      throw new IllegalArgumentException("Unknown request parameters found: " + providedParams);
    }
  }

  public static boolean hasQueryParameterValues(HttpServletRequest request, String queryParameter) {

    Map<String, String[]> queryParametersMap = request.getParameterMap();

    if (queryParametersMap.isEmpty()) {
      return false;
    }

    String[] queryParameterValues = queryParametersMap.get(queryParameter);

    if (queryParameterValues == null) {
      return false;
    }

    boolean hasQueryParameterNotEmptyValues =
        Arrays.stream(queryParameterValues).anyMatch(value -> !value.isBlank());

    /* Workaround to manage the case "query-param=".
    It should be safe enough to use because we have checked all other possibilities before. */
    boolean hasQueryParameterEmptyValues = request.getQueryString().contains(queryParameter + "=");

    return hasQueryParameterNotEmptyValues || hasQueryParameterEmptyValues;
  }

  public static boolean hasQueryParameterValuesOrIsNotTrue(
      HttpServletRequest request, String queryParameter) {

    Map<String, String[]> queryParametersMap = request.getParameterMap();

    if (queryParametersMap.isEmpty()) {
      return false;
    }

    String[] queryParameterValues = queryParametersMap.get(queryParameter);

    if (queryParameterValues == null) {
      return false;
    }

    boolean hasQueryParameterProhibitedValues =
        Arrays.stream(queryParameterValues)
            .anyMatch(value -> !value.isBlank() && !Boolean.parseBoolean(value));

    /* Workaround to manage the case "query-param=".
    It should be safe enough to use because we have checked all other possibilities before. */
    boolean hasQueryParameterEmptyValues =
        Arrays.stream(queryParameterValues).allMatch(String::isBlank)
            && request.getQueryString().contains(queryParameter + "=");

    return hasQueryParameterProhibitedValues || hasQueryParameterEmptyValues;
  }

  public static boolean isRelaxedQueryFlagTrue(HttpServletRequest request, String queryFlag) {
    String queryString = request.getQueryString();
    if (queryString == null || queryString.isEmpty()) {
      return false;
    }

    Map<String, String[]> queryParametersMap = request.getParameterMap();
    if (queryParametersMap.isEmpty()) {
      return false;
    }

    String[] arrValues = queryParametersMap.get(queryFlag);
    List<String> values = arrValues == null ? new ArrayList<>() : Arrays.asList(arrValues);

    if (values.stream().anyMatch(v -> v.equalsIgnoreCase("false"))) {
      return false;
    }

    return queryString.contains(queryFlag + "=")
        || queryString.startsWith(queryFlag)
        || queryString.contains("&" + queryFlag);
  }
}
