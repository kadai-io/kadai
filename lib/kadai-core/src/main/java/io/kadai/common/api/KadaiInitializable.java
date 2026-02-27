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

package io.kadai.common.api;

/** Interface specifying an operation for initialization of this object during Kadai startup. */
public interface KadaiInitializable {

  /**
   * Provides the active {@linkplain KadaiEngine} which is initialized for this KADAI installation.
   *
   * <p>This method is called during KADAI startup and allows the implementor to store the active
   * {@linkplain KadaiEngine} for later usage.
   *
   * @param kadaiEngine the active {@linkplain KadaiEngine} which is initialized for this
   *     installation
   */
  void initialize(KadaiEngine kadaiEngine);
}
