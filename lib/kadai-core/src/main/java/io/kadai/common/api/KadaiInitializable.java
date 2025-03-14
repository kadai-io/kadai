package io.kadai.common.api;

/** Interface specifying an operation for initialization of this object during Kadai startup. */
public interface KadaiInitializable {

  /**
   * Provides the active {@linkplain KadaiEngine} which is initialized for this KADAI installation.
   *
   * <p>This method is called during KADAI startup and allows the implementor to store the active
   * {@linkplain KadaiEngine} for later usage.
   *
   * @param kadaiEngine the active {@linkplain KadaiEngine} which is initialized for this
   *     installation
   */
  void initialize(KadaiEngine kadaiEngine);
}
