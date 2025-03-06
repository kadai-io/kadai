package io.kadai.common.internal.util;

public interface Reifiable<T> {

  // TODO: Can we default this using Class::as... ?
  Class<T> reify();
}
