package io.kadai.user.api;

import io.kadai.common.api.QueryColumnName;
import io.kadai.user.internal.UserQueryImpl;

/**
 * Enum containing the column names for {@linkplain
 * io.kadai.user.internal.UserQueryMapper#queryUserColumnValues(UserQueryImpl)
 * UserQueryMapper#queryUserColumnValues}.
 */
public enum UserQueryColumnName implements QueryColumnName {
  USER_ID("user_id"),
  GROUPS("groups"),
  PERMISSIONS("permissions"),
  FIRST_NAME("first_name"),
  LASTNAME("lastname"),
  FULL_NAME("full_name"),
  LONG_NAME("long_name"),
  E_MAIL("e_mail"),
  PHONE("phone"),
  MOBILE_PHONE("mobile_phone"),
  ORG_LEVEL_4("org_level_4"),
  ORG_LEVEL_3("org_level_3"),
  ORG_LEVEL_2("org_level_2"),
  ORG_LEVEL_1("org_level_1"),
  DATA("data");

  private final String name;

  UserQueryColumnName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
