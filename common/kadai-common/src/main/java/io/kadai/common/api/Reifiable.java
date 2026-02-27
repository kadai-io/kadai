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

/**
 * Interface specifying how to reify a generic type parameter at runtime.
 *
 * @param <T> the type of the generic parameter to reify
 */
public interface Reifiable<T> {

  /**
   * Returns the class of the reified generic parameter of this.
   *
   * @return class representing the generic parameter of this
   */
  Class<T> reify();
}
