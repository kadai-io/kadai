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
import java.util.Objects;

@Schema(description = "The entityModel class for User")
public class UserRepresentationModel extends UserSummaryRepresentationModel {

  @Schema(
      name = "data",
      description =
          "The data of the User. This field is used for additional information about the User.")
  protected String data;

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
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
        orgLevel1,
        data);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof UserRepresentationModel other)) {
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
        && Objects.equals(orgLevel1, other.orgLevel1)
        && Objects.equals(data, other.data);
  }
}
