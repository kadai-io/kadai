package io.kadai.common.rest;

import io.kadai.common.api.exceptions.InvalidArgumentException;

public interface QueryParameterValidation<E, R, Q extends QueryParameter<E, R>> {
  void validate(Q query) throws InvalidArgumentException;
}
