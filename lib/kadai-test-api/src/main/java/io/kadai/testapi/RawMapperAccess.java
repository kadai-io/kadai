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

package io.kadai.testapi;

import io.kadai.common.api.KadaiEngine;
import java.util.function.Function;

/** Utility for explicit raw mapper access in tests. */
public final class RawMapperAccess {

  private RawMapperAccess() {
    // Utility class.
  }

  public static void openConnection(KadaiEngine kadaiEngine) throws Exception {
    new KadaiEngineProxy(kadaiEngine).openConnection();
  }

  public static void closeConnection(KadaiEngine kadaiEngine) throws Exception {
    new KadaiEngineProxy(kadaiEngine).returnConnection();
  }

  public static <M, T> T runWithMapper(
      KadaiEngine kadaiEngine, Class<M> mapperClass, Function<M, T> callback) throws Exception {
    KadaiEngineProxy engineProxy = new KadaiEngineProxy(kadaiEngine);
    engineProxy.openConnection();
    try {
      M mapper = engineProxy.getSqlSession().getMapper(mapperClass);
      return callback.apply(mapper);
    } finally {
      engineProxy.returnConnection();
    }
  }
}
