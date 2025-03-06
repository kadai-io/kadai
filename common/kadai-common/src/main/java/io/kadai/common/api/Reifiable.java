package io.kadai.common.api;

public interface Reifiable<T> {

  // TODO: Can we default this using Class::as... ?
  Class<T> reify();
}
