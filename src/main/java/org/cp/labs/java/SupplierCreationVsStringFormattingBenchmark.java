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
package org.cp.labs.java;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.cp.elements.lang.Assert;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Java Benchmark measuring the performance of {@link String} formatting vs lazy construction and formatting
 * using a {@link Supplier}.
 *
 * @author John Blum
 * @see java.util.function.Supplier
 * @since 0.1.0
 */
@BenchmarkMode({ Mode.Throughput, Mode.AverageTime })
@Fork(1)
@Warmup(iterations = 1, time = 3)
@Measurement(iterations = 3, time = 3)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class SupplierCreationVsStringFormattingBenchmark {

  public static void main(String[] args) throws IOException {
    org.openjdk.jmh.Main.main(args);
  }

  int count = 0;

  @Setup(Level.Invocation)
  public void setup() {
    this.count = 0;
  }

  @Benchmark
  public void stringFormattedAssertion() {
    runAssertion(() -> Assert.isTrue(++this.count % 100 != 0, "Count [%d] was not zero".formatted(this.count)));
  }

  @Benchmark
  public void supplierStringFormattedAssertion() {
    runAssertion(() -> Assert.isTrue(++this.count % 100 != 0, () -> "Count [%d] was not zero".formatted(this.count)));
  }

  private void runAssertion(Runnable assertionRunnable) {

    try {
      assertionRunnable.run();
    }
    catch (Exception ignore) {
    }
  }
}
