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

package io.kadai.user.rest.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import org.springframework.hateoas.RepresentationModel;

@Schema(description = "The entityModel class for UserSummary")
public class UserSummaryRepresentationModel
    extends RepresentationModel<UserSummaryRepresentationModel> {

  @Schema(name = "userId", description = "Unique Id.")
  @NotNull
  protected String userId;

  @Schema(name = "groups", description = "The groups of the User.")
  protected Set<String> groups = Collections.emptySet();

  @Schema(name = "permissions", description = "The permissions of the User.")
  protected Set<String> permissions = Collections.emptySet();

  protected Set<String> domains = Collections.emptySet();

  @Schema(name = "firstName", description = "The first name of the User.")
  protected String firstName;

  @Schema(name = "lastName", description = "The last name of the User.")
  protected String lastName;

  @Schema(name = "fullName", description = "The full name of the User.")
  protected String fullName;

  @Schema(name = "longName", description = "The long name of the User.")
  protected String longName;

  @Schema(name = "email", description = "The email of the User.")
  protected String email;

  @Schema(name = "phone", description = "The phone number of the User.")
  protected String phone;

  @Schema(name = "mobilePhone", description = "The mobile phone number of the User.")
  protected String mobilePhone;

  @Schema(name = "orgLevel4", description = "The fourth organisation level of the User.")
  protected String orgLevel4;

  @Schema(name = "orgLevel3", description = "The third organisation level of the User.")
  protected String orgLevel3;

  @Schema(name = "orgLevel2", description = "The second organisation level of the User.")
  protected String orgLevel2;

  @Schema(name = "orgLevel1", description = "The first organisation level of the User.")
  protected String orgLevel1;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String id) {
    this.userId = id;
  }

  public Set<String> getGroups() {
    return groups;
  }

  public void setGroups(Set<String> groups) {
    this.groups = groups;
  }

  public Set<String> getPermissions() {
    return permissions;
  }

  public void setPermissions(Set<String> permissions) {
    this.permissions = permissions;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getLongName() {
    return longName;
  }

  public void setLongName(String longName) {
    this.longName = longName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getMobilePhone() {
    return mobilePhone;
  }

  public void setMobilePhone(String mobilePhone) {
    this.mobilePhone = mobilePhone;
  }

  public String getOrgLevel4() {
    return orgLevel4;
  }

  public void setOrgLevel4(String orgLevel4) {
    this.orgLevel4 = orgLevel4;
  }

  public String getOrgLevel3() {
    return orgLevel3;
  }

  public void setOrgLevel3(String orgLevel3) {
    this.orgLevel3 = orgLevel3;
  }

  public String getOrgLevel2() {
    return orgLevel2;
  }

  public void setOrgLevel2(String orgLevel2) {
    this.orgLevel2 = orgLevel2;
  }

  public String getOrgLevel1() {
    return orgLevel1;
  }

  public void setOrgLevel1(String orgLevel1) {
    this.orgLevel1 = orgLevel1;
  }

  public Set<String> getDomains() {
    return domains;
  }

  public void setDomains(Set<String> domains) {
    this.domains = domains;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        super.hashCode(),
        userId,
        groups,
        permissions,
        domains,
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
        orgLevel1);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof UserSummaryRepresentationModel other)) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    return Objects.equals(userId, other.userId)
        && Objects.equals(groups, other.groups)
        && Objects.equals(permissions, other.permissions)
        && Objects.equals(domains, other.domains)
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
        && Objects.equals(orgLevel1, other.orgLevel1);
  }
}
