package io.kadai.common.internal.util;

import java.util.function.Consumer;
import java.util.function.Function;

public final class LambdaExceptionUtil {

  private LambdaExceptionUtil() {}

  @FunctionalInterface
  public interface ConsumerWithExceptions<T, E extends Exception> {
    void accept(T t) throws E;
  }

  @FunctionalInterface
  public interface FunctionWithExceptions<T, R, E extends Exception> {
    R apply(T t) throws E;
  }

  public static <T, E extends Exception> Consumer<T> rethrowConsumer(
      ConsumerWithExceptions<T, E> consumer) throws E {
    return t -> {
      try {
        consumer.accept(t);
      } catch (Exception exception) {
        throwActualException(exception);
      }
    };
  }

  public static <T, R, E extends Exception> Function<T, R> rethrowFunction(
      FunctionWithExceptions<T, R, E> function) throws E  {
    return t -> {
      try {
        return function.apply(t);
      } catch (Exception exception) {
        throwActualException(exception);
        return null;
      }
    };
  }

  @SuppressWarnings("unchecked")
  private static <E extends Exception> void throwActualException(Exception exception) throws E {
    throw (E) exception;
  }

}
