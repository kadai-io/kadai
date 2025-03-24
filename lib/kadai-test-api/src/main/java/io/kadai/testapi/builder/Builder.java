package io.kadai.testapi.builder;

/**
 * Interface specifying how to build entities.
 *
 * @param <EntityT> the type of entity to build and store
 */
public interface Builder<EntityT> {

  /**
   * Builds the {@linkplain EntityT entity} for this builder.
   *
   * @return the built entity
   */
  EntityT build();
}
