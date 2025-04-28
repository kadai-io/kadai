/*
 * Copyright [2025] [envite consulting GmbH]
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

import io.kadai.user.api.models.User;
import io.kadai.user.api.models.UserSummary;
import java.util.Objects;

public class UserImpl extends UserSummaryImpl implements User {

  protected String data;

  public UserImpl() {}

  protected UserImpl(UserImpl copyFrom) {
    super(copyFrom);
    this.data = copyFrom.data;
  }

  @Override
  public UserImpl copy() {
    return new UserImpl(this);
  }

  @Override
  public String getData() {
    return data;
  }

  @Override
  public void setData(String data) {
    this.data = data;
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
        data,
        domains);
  }

  @Override
  public UserSummary asSummary() {
    UserSummaryImpl result = new UserSummaryImpl();
    result.setId(this.getId());
    result.setFirstName(this.getFirstName());
    result.setLastName(this.getLastName());
    result.setFullName(this.getFullName());
    result.setLongName(this.getLongName());
    result.setEmail(this.getEmail());
    result.setPhone(this.getPhone());
    result.setMobilePhone(this.getMobilePhone());
    result.setOrgLevel1(this.getOrgLevel1());
    result.setOrgLevel2(this.getOrgLevel2());
    result.setOrgLevel3(this.getOrgLevel3());
    result.setOrgLevel4(this.getOrgLevel4());
    result.setDomains(this.getDomains());
    return result;
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
    UserImpl other = (UserImpl) obj;
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
        && Objects.equals(data, other.data)
        && Objects.equals(domains, other.domains);
  }

  @Override
  public String toString() {
    return "UserImpl [id="
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
        + ", data="
        + data
        + ", domains="
        + domains
        + "]";
  }
}
