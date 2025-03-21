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

import io.kadai.KadaiConfiguration;
import java.util.Set;

/**
 * Interface for UserSummary. This is a specific short model-object which only contains the most
 * important information.
 */
public interface UserSummary {
  /**
   * Returns the id of the {@linkplain User}.
   *
   * @return userId
   */
  String getId();

  /**
   * Returns the groups of the {@linkplain User}.
   *
   * @return userGroups
   */
  Set<String> getGroups();

  /**
   * Returns the permissions of the {@linkplain User}.
   *
   * @return permissions
   */
  Set<String> getPermissions();

  /**
   * Returns the first name of the {@linkplain User}.
   *
   * @return firstName
   */
  String getFirstName();

  /**
   * Returns the last name of the {@linkplain User}.
   *
   * @return lastName
   */
  String getLastName();

  /**
   * Returns the full name of the {@linkplain User}.
   *
   * @return fullName
   */
  String getFullName();

  /**
   * Returns the long name of the {@linkplain User}.
   *
   * @return longName
   */
  String getLongName();

  /**
   * Returns the email address of the {@linkplain User}.
   *
   * @return email
   */
  String getEmail();

  /**
   * Returns the phone number of the {@linkplain User}.
   *
   * @return phone
   */
  String getPhone();

  /**
   * Returns the mobile phone number of the {@linkplain User}.
   *
   * @return mobilePhone
   */
  String getMobilePhone();

  /**
   * Returns the orgLevel4 of the {@linkplain User}.
   *
   * @return orgLevel4
   */
  String getOrgLevel4();

  /**
   * Returns the orgLevel3 of the {@linkplain User}.
   *
   * @return orgLevel3
   */
  String getOrgLevel3();

  /**
   * Returns the orgLevel2 of the {@linkplain User}.
   *
   * @return orgLevel2
   */
  String getOrgLevel2();

  /**
   * Returns the orgLevel1 of the {@linkplain User}.
   *
   * @return orgLevel1
   */
  String getOrgLevel1();

  /**
   * Returns the domains of the {@linkplain User}.
   *
   * <p>The domains are derived from the {@linkplain io.kadai.workbasket.api.WorkbasketPermission
   * WorkbasketPermissions} and the according KADAI property {@linkplain
   * KadaiConfiguration#getMinimalPermissionsToAssignDomains()}.
   *
   * @return domains
   */
  Set<String> getDomains();

  /**
   * Duplicates this UserSummary.
   *
   * @return a copy of this UserSummary
   */
  UserSummary copy();
}
