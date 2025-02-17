package io.kadai.user.api;

import io.kadai.common.api.BaseQuery;
import io.kadai.user.api.models.User;

/**
 * The UserQuery allows for a custom search across all {@linkplain User Users}.
 */
public interface UserQuery extends BaseQuery<User, UserQueryColumnName> {

  /**
   * Selects only {@linkplain User Users} which have an {@linkplain
   * User#getId() id} equal to any of the passed values.
   *
   * @param ids the values of interest
   * @return this query
   */
  UserQuery idIn(String... ids);

  /**
   * Selects only {@linkplain User Users} which have a {@linkplain
   * User#getOrgLevel1() orgLevel1} equal to any of the passed values.
   *
   * @param orgLevel1s the values of interest
   * @return this query
   */
  UserQuery orgLevel1In(String... orgLevel1s);

  /**
   * Selects only {@linkplain User Users} which have a {@linkplain
   * User#getOrgLevel2() orgLevel2} equal to any of the passed values.
   *
   * @param orgLevel2s the values of interest
   * @return this query
   */
  UserQuery orgLevel2In(String... orgLevel2s);

  /**
   * Selects only {@linkplain User Users} which have a {@linkplain
   * User#getOrgLevel3() orgLevel3} equal to any of the passed values.
   *
   * @param orgLevel3s the values of interest
   * @return this query
   */
  UserQuery orgLevel3In(String... orgLevel3s);

  /**
   * Selects only {@linkplain User Users} which have a {@linkplain
   * User#getOrgLevel4() orgLevel4} equal to any of the passed values.
   *
   * @param orgLevel4s the values of interest
   * @return this query
   */
  UserQuery orgLevel4In(String... orgLevel4s);
}
