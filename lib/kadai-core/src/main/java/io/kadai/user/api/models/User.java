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

package io.kadai.user.api.models;

import java.util.Set;

/** The User holds some relevant information about the KADAI users. */
public interface User extends UserSummary {

  /**
   * Sets the id of the User.
   *
   * @param id the id of the User
   */
  void setId(String id);

  /**
   * Sets the groups of the User.
   *
   * @param groups the groups of the User
   */
  void setGroups(Set<String> groups);

  /**
   * Sets the permissions of the User.
   *
   * @param permissions the permissions of the User
   */
  void setPermissions(Set<String> permissions);

  /**
   * Sets the first name of the User.
   *
   * @param firstName the first name of the User
   */
  void setFirstName(String firstName);

  /**
   * Sets the last name of the User.
   *
   * @param lastName the last name of the User
   */
  void setLastName(String lastName);

  /**
   * Sets the full name of the User.
   *
   * @param fullName the full name of the User
   */
  void setFullName(String fullName);

  /**
   * Sets the long name of the User.
   *
   * @param longName the long name of the User
   */
  void setLongName(String longName);

  /**
   * Sets the email address of the User.
   *
   * @param email the email address of the User
   */
  void setEmail(String email);

  /**
   * Sets the phone number of the User.
   *
   * @param phone the phone number of the User
   */
  void setPhone(String phone);

  /**
   * Sets the mobile phone number of the User.
   *
   * @param mobilePhone the mobile phone number of the User
   */
  void setMobilePhone(String mobilePhone);

  /**
   * Sets the orgLevel4 of the User.
   *
   * @param orgLevel4 the fourth organization level of the User
   */
  void setOrgLevel4(String orgLevel4);

  /**
   * Sets the orgLevel3 of the User.
   *
   * @param orgLevel3 the third organization level of the User
   */
  void setOrgLevel3(String orgLevel3);

  /**
   * Sets the orgLevel2 of the User.
   *
   * @param orgLevel2 the second organization level of the User
   */
  void setOrgLevel2(String orgLevel2);

  /**
   * Sets the orgLevel1 of the User.
   *
   * @param orgLevel1 the first organization level of the User
   */
  void setOrgLevel1(String orgLevel1);

  /**
   * Returns the data of the {@linkplain User}.
   *
   * @return data
   */
  String getData();

  /**
   * Sets the data of the User.
   *
   * @param data the data of the User
   */
  void setData(String data);

  /**
   * Returns a summary of the current User.
   *
   * @return the {@linkplain UserSummary} object for the current User
   */
  UserSummary asSummary();
}
