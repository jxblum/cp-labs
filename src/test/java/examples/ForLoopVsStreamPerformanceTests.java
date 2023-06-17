/*
 * Copyright 2017 Author or Authors.
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

import java.util.Arrays;
import java.util.function.IntPredicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Performance Tests comparing a {@literal for loop} with a {@link java.util.stream.Stream}.
 *
 * @author John Blum
 * @see java.util.stream.Stream
 * @since 1.0.0
 */
public class ForLoopVsStreamPerformanceTests {

  private static final int[] numbers = new int[100_000];

  private static final IntPredicate multipleOfFour = number -> Math.floorMod(number, 4) == 0;

  @BeforeAll
  public static void initializeNumbers() {

    for (int number = 0; number < numbers.length; number++) {
      numbers[number] = number;
    }
  }

  private void measurePerformance(Supplier<Integer> countSupplier, String tag) {

    long t0 = System.nanoTime();

    int count = countSupplier.get();

    long t1 = System.nanoTime();

    assertThat(count).isGreaterThan(0);

    System.out.printf("Count of Numbers divisible by 4 using %s == %d; Total Time [%d]%n", tag, count, t1 - t0);
    System.out.flush();
  }

  @Test
  public void performanceOfForLoop() {

    Supplier<Integer> countSupplier = () -> {

      int count = 0;

      for (int number : numbers) {
        if (multipleOfFour.test(number)) {
          count++;
        }
      }

      return count;
    };

    measurePerformance(countSupplier, "For Loop");
  }

  @Test
  public void performanceOfStream() {

    Supplier<Integer> countSupplier = () -> (int) Arrays.stream(numbers)
      .filter(multipleOfFour)
      .count();

    measurePerformance(countSupplier, "Stream");
  }
}
