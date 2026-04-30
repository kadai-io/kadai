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

package io.kadai.common.api.security;

import java.util.List;

/** Provides the context information about a user. */
public interface CurrentUserContext {

  /**
   * Returns the id of the current user.
   *
   * @return id of the current user
   */
  String getUserId();

  /**
   * Returns the access-id of the current contexts' proxy.
   *
   * <p>The proxy is the user or group that is used as facade for executing actions disguised as
   * another {@linkplain #getUserId() user}.
   *
   * @return id of the current contexts' proxy
   */
  String getProxyAccessId();

  /**
   * Returns all groupIds of the current user.
   *
   * @return list containing all groupIds of the current user.
   */
  List<String> getGroupIds();

  /**
   * Returns all accessIds of the current user. This combines the userId and all groupIds of the
   * user.
   *
   * @return list containing all accessIds of the current user.
   */
  List<String> getAccessIds();
}
