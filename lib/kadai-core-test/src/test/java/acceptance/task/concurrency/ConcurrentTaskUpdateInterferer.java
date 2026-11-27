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

package acceptance.task.concurrency;

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.internal.KadaiEngineImpl;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.SqlSessionManager;

/**
 * Simulates a concurrent process which modifies a Task while another operation is in flight.
 *
 * <p>Once armed, the given interference is executed in a separate thread (and therefore in a
 * separate database transaction) right before the armed thread executes an UPDATE statement of the
 * TaskMapper. At that point the operation under test has already read and validated the Task but
 * has not written it yet. The interference is committed before the operation under test continues,
 * which deterministically reproduces the race between two parallel processes.
 */
@Intercepts(
    @Signature(
        type = Executor.class,
        method = "update",
        args = {MappedStatement.class, Object.class}))
public class ConcurrentTaskUpdateInterferer implements Interceptor {

  private static final String TASK_MAPPER_PREFIX = "io.kadai.task.internal.TaskMapper.";
  private static final long INTERFERENCE_TIMEOUT_MILLIS = 30_000;
  private static final String CONCURRENT_USER = "user-1-2";

  private final KadaiEngine kadaiEngine;
  private final AtomicInteger interferenceCount = new AtomicInteger();
  private final AtomicInteger remainingInterferences = new AtomicInteger();
  private final AtomicReference<Thread> armedThread = new AtomicReference<>();
  private final AtomicReference<Interference> interference = new AtomicReference<>();

  private ConcurrentTaskUpdateInterferer(KadaiEngine kadaiEngine) {
    this.kadaiEngine = kadaiEngine;
  }

  public static ConcurrentTaskUpdateInterferer registerOn(KadaiEngine kadaiEngine)
      throws ReflectiveOperationException {
    ConcurrentTaskUpdateInterferer interferer = new ConcurrentTaskUpdateInterferer(kadaiEngine);
    Field sessionManagerField = KadaiEngineImpl.class.getDeclaredField("sessionManager");
    sessionManagerField.setAccessible(true);
    SqlSessionManager sessionManager = (SqlSessionManager) sessionManagerField.get(kadaiEngine);
    sessionManager.getConfiguration().addInterceptor(interferer);
    return interferer;
  }

  /**
   * Executes the given interference (as user-1-2 with admin rights, in a separate thread) before
   * the next TaskMapper UPDATE statement of the calling thread.
   *
   * @param interference the concurrent modification
   */
  public void interfereOnce(Interference interference) {
    arm(interference, 1);
  }

  /**
   * Executes the given interference (as user-1-2 with admin rights, in a separate thread) before
   * every TaskMapper UPDATE statement of the calling thread.
   *
   * @param interference the concurrent modification
   */
  public void interfereAlways(Interference interference) {
    arm(interference, Integer.MAX_VALUE);
  }

  public void disarm() {
    armedThread.set(null);
    interference.set(null);
    remainingInterferences.set(0);
    interferenceCount.set(0);
  }

  public int getInterferenceCount() {
    return interferenceCount.get();
  }

  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
    if (shouldInterfere(mappedStatement)) {
      runInSeparateThread(interference.get());
    }
    return invocation.proceed();
  }

  private void arm(Interference interference, int times) {
    this.interference.set(interference);
    this.remainingInterferences.set(times);
    this.interferenceCount.set(0);
    this.armedThread.set(Thread.currentThread());
  }

  private boolean shouldInterfere(MappedStatement mappedStatement) {
    return Thread.currentThread() == armedThread.get()
        && mappedStatement.getSqlCommandType() == SqlCommandType.UPDATE
        && mappedStatement.getId().startsWith(TASK_MAPPER_PREFIX)
        && remainingInterferences.getAndUpdate(remaining -> Math.max(0, remaining - 1)) > 0;
  }

  private void runInSeparateThread(Interference interference) throws InterruptedException {
    AtomicReference<Throwable> failure = new AtomicReference<>();
    Thread thread =
        new Thread(
            () -> {
              try {
                kadaiEngine.runAsAdmin(() -> run(interference), CONCURRENT_USER);
              } catch (RuntimeException | AssertionError e) {
                failure.set(e);
              }
            },
            "concurrent-task-modification");
    thread.start();
    thread.join(INTERFERENCE_TIMEOUT_MILLIS);
    if (thread.isAlive()) {
      thread.interrupt();
      throw new IllegalStateException(
          "The concurrent Task modification did not finish in time. Is it blocked by a lock?");
    }
    if (failure.get() != null) {
      throw new IllegalStateException("The concurrent Task modification failed", failure.get());
    }
    interferenceCount.incrementAndGet();
  }

  private static Void run(Interference interference) {
    try {
      interference.run();
      return null;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  /** A modification which is executed concurrently to the operation under test. */
  @FunctionalInterface
  public interface Interference {
    void run() throws Exception;
  }
}
