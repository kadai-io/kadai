package io.kadai.common.rest.util;

import static io.kadai.common.rest.util.QueryParamsValidator.isRelaxedQueryFlagTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

public class QueryParamsValidatorTest {

  @ParameterizedTest
  @NullAndEmptySource
  void should_ReturnFalse_For_NoQuery(String noQuery) {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    when(request.getQueryString()).thenReturn(noQuery);

    boolean actual = isRelaxedQueryFlagTrue(request, "some-flag");

    assertThat(actual).isFalse();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
          "some-flag=false", "foo=bar&some-flag=false", "some-flag=false&foo=bar",
          "some-flag=False", "foo=bar&some-flag=False", "some-flag=False&foo=bar",
          "some-flag=FALSE", "foo=bar&some-flag=FALSE", "some-flag=FALSE&foo=bar"
      })
  void should_ReturnFalse_When_QueryFlagHasStringValueFalse(String queryString) {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    when(request.getQueryString()).thenReturn(queryString);
    when(request.getParameterMap()).thenReturn(Map.of("some-flag", new String[] {"false"}));

    boolean actual = isRelaxedQueryFlagTrue(request, "some-flag");

    assertThat(actual).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {"some-flag=", "foo=bar&some-flag=", "some-flag=&foo=bar"})
  void should_ReturnTrue_When_QueryStringHasQueryFlagWithEmptyValue(String queryString) {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    when(request.getQueryString()).thenReturn(queryString);
    when(request.getParameterMap()).thenReturn(Map.of("some-flag", new String[] {}));

    boolean actual = isRelaxedQueryFlagTrue(request, "some-flag");

    assertThat(actual).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {"some-flag", "foo=bar&some-flag", "some-flag&foo=bar"})
  void should_ReturnTrue_When_QueryStringHasQueryFlagWithoutValue(String queryString) {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    when(request.getQueryString()).thenReturn(queryString);
    when(request.getParameterMap()).thenReturn(Map.of("some-flag", new String[] {}));

    boolean actual = isRelaxedQueryFlagTrue(request, "some-flag");

    assertThat(actual).isTrue();
  }
}
