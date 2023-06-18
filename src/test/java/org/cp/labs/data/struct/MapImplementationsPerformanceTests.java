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
package org.cp.labs.data.struct;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Performance Tests comparing the Java {@link HashMap} to the Codeprimate {@link TwoDimensionalMap}.
 *
 * @author John Blum
 * @see java.util.Map
 * @see java.util.HashMap
 * @see org.cp.labs.data.struct.TwoDimensionalMap
 * @since 1.0.0
 */
public class MapImplementationsPerformanceTests {

  //private static final int INITIAL_CAPACITY = 501;
  //private static final int INITIAL_CAPACITY = 1024;
  private static final int INITIAL_CAPACITY = 1067;
  //private static final int INITIAL_CAPACITY = 2048;
  //private static final int INITIAL_CAPACITY = 2161;
  //private static final int INITIAL_CAPACITY = 4096;
  //private static final int INITIAL_CAPACITY = 4097;
  //private static final int INITIAL_CAPACITY = 8192;
  //private static final int INITIAL_CAPACITY = 8211;
  //private static final int INITIAL_CAPACITY = 16_384;
  //private static final int INITIAL_CAPACITY = 32_7658;
  //private static final int INITIAL_CAPACITY = 65_536;

  //private static final int SAMPLE_SIZE = 100_000;
  private static final int SAMPLE_SIZE = 250_000;
  //private static final int SAMPLE_SIZE = 500_000;
  //private static final int SAMPLE_SIZE = 1_000_000;
  //private static final int SAMPLE_SIZE = 5_000_000;
  //private static final int SAMPLE_SIZE = 10_000_000;

  private static final List<Integer> numbers = new ArrayList<>(SAMPLE_SIZE);

  private static final Map<Integer, Integer> arrayMap = new ArrayHashMap<>(INITIAL_CAPACITY);

  private static final Map<Integer, Integer> hashMap = new HashMap<>();

  private static final Map<Integer, Integer> map2d = TwoDimensionalMap.usingHashMapArray(INITIAL_CAPACITY);

  private static final Map<Integer, Integer> treeMap = new TreeMap<>();

  @BeforeAll
  public static void initializeMaps() {

    Random random = new Random(System.nanoTime());

    for (int index = 0; index < SAMPLE_SIZE; index++) {
      int number = random.nextInt(SAMPLE_SIZE);
      numbers.add(number);
      arrayMap.put(number, number);
      hashMap.put(number, number);
      map2d.put(number, number);
      treeMap.put(number, number);
    }

    assertThat(arrayMap.size()).isEqualTo(hashMap.size());
    assertThat(map2d).hasSize(hashMap.size());
  }

  @SuppressWarnings("rawtypes")
  private void measureMapGetPerformance(Function<Integer, Integer> mapGetFunction, Class<? extends Map> mapType) {

    long t0 = System.currentTimeMillis();

    numbers.forEach(number ->
      assertThat(mapGetFunction.apply(number)).isEqualTo(number));

    long t1 = System.currentTimeMillis();

    System.out.printf("Getting [%d] values from Map of type [%s] took [%d] milliseconds%n",
      numbers.size(), mapType.getSimpleName(), t1 - t0);

    System.out.flush();
  }

  @Test
  public void measureArrayHashMapGet() {
    measureMapGetPerformance(arrayMap::get, ArrayHashMap.class);
  }

  @Test
  public void measureHashMapGet() {
    measureMapGetPerformance(hashMap::get, HashMap.class);
  }

  @Test
  public void measureTreeMapGet() {
    measureMapGetPerformance(treeMap::get, TreeMap.class);
  }

  @Test
  public void measureTwoDimensionalMapGet() {
    measureMapGetPerformance(map2d::get, TwoDimensionalMap.class);
  }
}
