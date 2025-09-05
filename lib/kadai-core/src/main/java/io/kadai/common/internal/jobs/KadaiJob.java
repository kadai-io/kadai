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

package io.kadai.common.internal.jobs;

import io.kadai.common.api.exceptions.KadaiException;

/** Interface for all background KADAI jobs. */
public interface KadaiJob {

  /**
   * Execute the KadaiJob.
   *
   * @throws KadaiException if any exception occurs during the execution.
   */
  void run() throws KadaiException;
}
