/*
 * Copyright [2026] [envite consulting GmbH]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *
 */

package io.kadai.user.internal.models;

import io.kadai.user.api.models.UserSummary;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/** This entity contains the most important information about a user. */
public class UserSummaryImpl implements UserSummary {

  protected String id;

  protected Set<String> groups = Collections.emptySet();
  protected Set<String> permissions = Collections.emptySet();
  protected String firstName;
  protected String lastName;
  protected String fullName;
  protected String longName;
  protected String email;
  protected String phone;
  protected String mobilePhone;
  protected String orgLevel4;
  protected String orgLevel3;
  protected String orgLevel2;
  protected String orgLevel1;
  protected Set<String> domains = Collections.emptySet();

  public UserSummaryImpl() {}

  protected UserSummaryImpl(UserSummaryImpl copyFrom) {
    this.groups = new HashSet<>(copyFrom.groups);
    this.permissions = new HashSet<>(copyFrom.permissions);
    this.firstName = copyFrom.firstName;
    this.lastName = copyFrom.lastName;
    this.fullName = copyFrom.fullName;
    this.longName = copyFrom.longName;
    this.email = copyFrom.email;
    this.phone = copyFrom.phone;
    this.mobilePhone = copyFrom.mobilePhone;
    this.orgLevel4 = copyFrom.orgLevel4;
    this.orgLevel3 = copyFrom.orgLevel3;
    this.orgLevel2 = copyFrom.orgLevel2;
    this.orgLevel1 = copyFrom.orgLevel1;
    this.domains = new HashSet<>(copyFrom.domains);
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public Set<String> getGroups() {
    return groups;
  }

  public void setGroups(Set<String> groups) {
    this.groups = groups;
  }

  @Override
  public Set<String> getPermissions() {
    return permissions;
  }

  public void setPermissions(Set<String> permissions) {
    this.permissions = permissions;
  }

  @Override
  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  @Override
  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  @Override
  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  @Override
  public String getLongName() {
    return longName;
  }

  public void setLongName(String longName) {
    this.longName = longName;
  }

  @Override
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  @Override
  public String getMobilePhone() {
    return mobilePhone;
  }

  public void setMobilePhone(String mobilePhone) {
    this.mobilePhone = mobilePhone;
  }

  @Override
  public String getOrgLevel4() {
    return orgLevel4;
  }

  public void setOrgLevel4(String orgLevel4) {
    this.orgLevel4 = orgLevel4;
  }

  @Override
  public String getOrgLevel3() {
    return orgLevel3;
  }

  public void setOrgLevel3(String orgLevel3) {
    this.orgLevel3 = orgLevel3;
  }

  @Override
  public String getOrgLevel2() {
    return orgLevel2;
  }

  public void setOrgLevel2(String orgLevel2) {
    this.orgLevel2 = orgLevel2;
  }

  @Override
  public String getOrgLevel1() {
    return orgLevel1;
  }

  public void setOrgLevel1(String orgLevel1) {
    this.orgLevel1 = orgLevel1;
  }

  @Override
  public Set<String> getDomains() {
    return domains;
  }

  @Override
  public UserSummary copy() {
    return new UserSummaryImpl(this);
  }

  public void setDomains(Set<String> domains) {
    this.domains = domains;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id,
        groups,
        permissions,
        firstName,
        lastName,
        fullName,
        longName,
        email,
        phone,
        mobilePhone,
        orgLevel4,
        orgLevel3,
        orgLevel2,
        orgLevel1,
        domains);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    UserSummaryImpl other = (UserSummaryImpl) obj;
    return Objects.equals(id, other.id)
        && Objects.equals(groups, other.groups)
        && Objects.equals(permissions, other.permissions)
        && Objects.equals(firstName, other.firstName)
        && Objects.equals(lastName, other.lastName)
        && Objects.equals(fullName, other.fullName)
        && Objects.equals(longName, other.longName)
        && Objects.equals(email, other.email)
        && Objects.equals(phone, other.phone)
        && Objects.equals(mobilePhone, other.mobilePhone)
        && Objects.equals(orgLevel4, other.orgLevel4)
        && Objects.equals(orgLevel3, other.orgLevel3)
        && Objects.equals(orgLevel2, other.orgLevel2)
        && Objects.equals(orgLevel1, other.orgLevel1)
        && Objects.equals(domains, other.domains);
  }

  @Override
  public String toString() {
    return "UserSummaryImpl [id="
        + id
        + ", groups="
        + groups
        + ", permissions="
        + permissions
        + ", firstName="
        + firstName
        + ", lastName="
        + lastName
        + ", fullName="
        + fullName
        + ", longName="
        + longName
        + ", email="
        + email
        + ", phone="
        + phone
        + ", mobilePhone="
        + mobilePhone
        + ", orgLevel4="
        + orgLevel4
        + ", orgLevel3="
        + orgLevel3
        + ", orgLevel2="
        + orgLevel2
        + ", orgLevel1="
        + orgLevel1
        + ", domains="
        + domains
        + "]";
  }
}
