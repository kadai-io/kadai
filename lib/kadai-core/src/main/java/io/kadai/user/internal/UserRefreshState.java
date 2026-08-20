package io.kadai.user.internal;

import java.util.Set;

/** Canonical persisted user state used exclusively by the LDAP refresh. */
public record UserRefreshState(
    String id,
    String firstName,
    String lastName,
    String fullName,
    String longName,
    String email,
    String phone,
    String mobilePhone,
    String orgLevel1,
    String orgLevel2,
    String orgLevel3,
    String orgLevel4,
    String data,
    Set<String> groups,
    Set<String> permissions) {

  public boolean hasSameScalars(UserRefreshState other) {
    return java.util.Objects.equals(firstName, other.firstName)
        && java.util.Objects.equals(lastName, other.lastName)
        && java.util.Objects.equals(fullName, other.fullName)
        && java.util.Objects.equals(longName, other.longName)
        && java.util.Objects.equals(email, other.email)
        && java.util.Objects.equals(phone, other.phone)
        && java.util.Objects.equals(mobilePhone, other.mobilePhone)
        && java.util.Objects.equals(orgLevel1, other.orgLevel1)
        && java.util.Objects.equals(orgLevel2, other.orgLevel2)
        && java.util.Objects.equals(orgLevel3, other.orgLevel3)
        && java.util.Objects.equals(orgLevel4, other.orgLevel4)
        && java.util.Objects.equals(data, other.data);
  }
}
