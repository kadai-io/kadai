package io.kadai.common.internal.util;

import static io.kadai.common.internal.util.LambdaExceptionUtil.rethrowConsumer;
import static io.kadai.common.internal.util.LambdaExceptionUtil.rethrowFunction;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.io.IOException;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

public class LambdaExceptionUtilTest {

  @Test
  void should_RethrowExactException_WhenConsumerThrowsException() {
    final Exception expected = new Exception("Some message", new IOException("some exception"));

    final ThrowingCallable call =
        () ->
            rethrowConsumer(
                    o -> {
                      throw expected;
                    })
                .accept(new Object());

    assertThatExceptionOfType(Exception.class)
        .isThrownBy(call)
        .isEqualTo(expected);
  }

  @Test
  void should_NotThrowException_WhenConsumerThrowsNoException() {
    final ThrowingCallable call = () -> rethrowConsumer(o -> {}).accept(new Object());

    assertThatNoException().isThrownBy(call);
  }

  @Test
  void should_RethrowExactException_WhenFunctionThrowsException() {
    final Exception expected = new Exception("Some message", new IOException("some exception"));

    final ThrowingCallable call =
        () ->
            rethrowFunction(
                    o -> {
                      throw expected;
                    })
                .apply(new Object());

    assertThatExceptionOfType(Exception.class)
        .isThrownBy(call)
        .isEqualTo(expected);
  }

  @Test
  void should_NotThrowException_WhenFunctionThrowsNoException() {
    final ThrowingCallable call = () -> rethrowFunction(o -> o).apply(new Object());

    assertThatNoException().isThrownBy(call);
  }
}
