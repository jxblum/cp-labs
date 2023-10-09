/*
 * Copyright 2017-Present Author or Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package examples;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import org.awaitility.Awaitility;
import org.cp.elements.lang.Assert;
import org.cp.elements.lang.ClassUtils;
import org.cp.elements.lang.annotation.NotNull;
import org.cp.elements.lang.concurrent.ThreadUtils;

/**
 * Unit Tests for Java {@link ClassLoader}.
 *
 * @author John Blum
 * @see java.lang.ClassLoader
 * @see org.junit.jupiter.api.Test
 * @since 0.1.0
 */
public class ClassLoadingUnitTests {

  @Test
  void waitIsCorrect() {

    // Causes ClassNotFoundException / NoClassDefFoundError
    //Awaitility.await();

    Waiter waiter = new Waiter();

    long t0 = System.currentTimeMillis();

    waiter.await(Duration.ofMillis(500L));

    assertThat(System.currentTimeMillis() - t0).isGreaterThanOrEqualTo(500L);
  }

  static class Waiter {

    private static final boolean AWAITILITY_PRESENT = ClassUtils.isPresent("org.awaitility.Awaitility");

    void await(@NotNull Duration duration) {

      Consumer<Duration> consumer  = AWAITILITY_PRESENT ? awaitilityAwait() : elementsWait();

      consumer.accept(duration);
    }

    private Consumer<Duration> awaitilityAwait() {

      System.out.println("Awaitility.await()");

      return duration -> Awaitility.await()
        .pollDelay(duration)
        .untilAsserted(() -> Assert.isTrue(true));
    }

    private Consumer<Duration> elementsWait() {
      System.out.println("Elements Timed Wait");
      return duration -> ThreadUtils.waitFor(duration.toMillis()).run();
    }
  }
}
