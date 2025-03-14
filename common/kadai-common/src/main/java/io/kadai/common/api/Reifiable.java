package io.kadai.common.api;

/**
 * Interface specifying how to reify a generic type parameter at runtime.
 *
 * @param <T> the type of the generic parameter to reify
 */
public interface Reifiable<T> {

  /**
   * Returns the class of the reified generic parameter of this.
   *
   * @return class representing the generic parameter of this
   */
  Class<T> reify();
}
