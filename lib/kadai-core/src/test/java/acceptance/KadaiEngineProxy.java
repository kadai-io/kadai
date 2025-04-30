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

package acceptance;

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.internal.InternalKadaiEngine;
import io.kadai.common.internal.KadaiEngineImpl;
import java.lang.reflect.Field;
import org.apache.ibatis.session.SqlSession;

/** Utility class to enable unit tests to access mappers directly. */
public class KadaiEngineProxy {

  private final InternalKadaiEngine engine;

  public KadaiEngineProxy(KadaiEngine kadaiEngine) throws Exception {
    Field internal = KadaiEngineImpl.class.getDeclaredField("internalKadaiEngineImpl");
    internal.setAccessible(true);
    engine = (InternalKadaiEngine) internal.get(kadaiEngine);
  }

  public InternalKadaiEngine getEngine() {
    return engine;
  }

  public SqlSession getSqlSession() {
    return engine.getSqlSession();
  }

  public void openConnection() {
    engine.openConnection();
  }

  public void returnConnection() {
    engine.returnConnection();
  }
}
