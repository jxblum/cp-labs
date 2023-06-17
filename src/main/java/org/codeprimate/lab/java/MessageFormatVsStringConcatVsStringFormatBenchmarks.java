/*
 * Copyright 2011-Present Author or Authors.
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
package org.codeprimate.lab.java;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

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
import org.openjdk.jmh.infra.Blackhole;

/**
 * Benchmark for Java {@link MessageFormat#format(String, Object...)} vs. {@link String#concat(String)}
 * vs. {@link String#format(String, Object...)}.
 *
 * @author John Blum
 * @see java.lang.String
 * @see java.text.MessageFormat
 * @see org.openjdk.jmh.annotations.Benchmark
 * @since 0.1.0
 */
//@BenchmarkMode({ Mode.Throughput })
@BenchmarkMode({ Mode.Throughput, Mode.AverageTime })
@Fork(1)
@Warmup(iterations = 1, time = 3)
@Measurement(iterations = 3, time = 3)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class MessageFormatVsStringConcatVsStringFormatBenchmarks {

  public static void main(String[] args) throws IOException {
    org.openjdk.jmh.Main.main(args);
  }

  int count = 0;

  @Setup(Level.Invocation)
  public void setup() {
    this.count = 0;
  }

  @Benchmark
  public void messageFormatBench(Blackhole blackhole) {
    blackhole.consume(MessageFormat.format("test {1,number,integer}", this.count++));
  }

  @Benchmark
  public void plusStringConcatBench(Blackhole blackhole) {
    blackhole.consume("test " + this.count++);
  }

  @Benchmark
  public void stringConcatBench(Blackhole blackhole) {
    blackhole.consume("test ".concat(Integer.toString(this.count++)));
  }

  @Benchmark
  public void stringFormatBench(Blackhole blackhole) {
    blackhole.consume(String.format("test %d", this.count++));
  }
}
