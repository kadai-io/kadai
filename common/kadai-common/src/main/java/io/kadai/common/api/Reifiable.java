package io.kadai.common.api;

public interface Reifiable<T> {

  Class<T> reify();
}
