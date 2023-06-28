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
package examples;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.cp.elements.test.TestException;

/**
 * Unit Tests to test, assert and verify Java's {@link Exception} handling mechanism
 * using {@literal try-catch-finally} blocks.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @since 1.0.0
 */
public class ExceptionHandlingUnitTests {

  private final ExceptionThrowingCode code = new ExceptionThrowingCode();

  @BeforeEach
  public void setup() {

    this.code.insideTryBlock.set(false);
    this.code.insideCatchBlock.set(false);
    this.code.insideFinallyBlock.set(false);
    this.code.insideCatchIllegalArgumentExceptionBlock.set(false);
  }

  @Test
  public void exceptionThrownOutsideTryTriggersNoBlocks() {

    assertThatExceptionOfType(TestException.class)
      .isThrownBy(() -> this.code.exceptionThrowingMethod(true, false, false))
      .withMessage("Before Try-Catch-Finally")
      .withNoCause();

    assertThat(this.code.insideTryBlock).isFalse();
    assertThat(this.code.insideCatchBlock).isFalse();
    assertThat(this.code.insideFinallyBlock).isFalse();
    assertThat(this.code.insideCatchIllegalArgumentExceptionBlock).isFalse();
  }

  @Test
  public void exceptionThrownInsideTryTriggersCatchAndFinallyBlocks() {

    this.code.exceptionThrowingMethod(false, false, false);

    assertThat(this.code.insideTryBlock).isTrue();
    assertThat(this.code.insideCatchBlock).isTrue();
    assertThat(this.code.insideFinallyBlock).isTrue();
    assertThat(this.code.insideCatchIllegalArgumentExceptionBlock).isFalse();
  }

  @Test
  public void unhandledExceptionThrownInsideTryTriggersFinallyBlock() {

    assertThatExceptionOfType(RuntimeException.class)
      .isThrownBy(() -> this.code.exceptionThrowingMethod(false, true, false))
      .withMessage("Unhandled Exception Thrown Inside Try")
      .withNoCause();

    assertThat(this.code.insideTryBlock).isTrue();
    assertThat(this.code.insideCatchBlock).isFalse();
    assertThat(this.code.insideFinallyBlock).isTrue();
    assertThat(this.code.insideCatchIllegalArgumentExceptionBlock).isFalse();
  }

  @Test
  public void unhandledExceptionThrownInsideCatchTriggersFinallyBlock() {

    assertThatExceptionOfType(RuntimeException.class)
      .isThrownBy(() -> this.code.exceptionThrowingMethod(false, false, true))
      .withMessage("Unhandled Exception Thrown Inside Catch")
      .withNoCause();

    assertThat(this.code.insideTryBlock).isTrue();
    assertThat(this.code.insideCatchBlock).isTrue();
    assertThat(this.code.insideFinallyBlock).isTrue();
    assertThat(this.code.insideCatchIllegalArgumentExceptionBlock).isFalse();
  }

  static class ExceptionThrowingCode {

    AtomicBoolean insideTryBlock = new AtomicBoolean(false);
    AtomicBoolean insideCatchBlock = new AtomicBoolean(false);
    AtomicBoolean insideFinallyBlock = new AtomicBoolean(false);
    AtomicBoolean insideCatchIllegalArgumentExceptionBlock = new AtomicBoolean(false);

    void exceptionThrowingMethod(boolean throwExceptionOutsideTryCatchFinally, boolean throwUnhandledExceptionInsideTry,
        boolean throwUnhandledExceptionInsideCatch) {

      if (throwExceptionOutsideTryCatchFinally) {
        throw new TestException("Before Try-Catch-Finally");
      }

      try {
        this.insideTryBlock.set(true);
        if (throwUnhandledExceptionInsideTry) {
          throw new RuntimeException("Unhandled Exception Thrown Inside Try");
        }
        throw new TestException("Inside Try");
      }
      catch (TestException cause) {
        this.insideCatchBlock.set(true);
        if (throwUnhandledExceptionInsideCatch) {
          // Finally block will still be executed, but catch (IllegalStateException) block will not!
          throw new IllegalStateException("Unhandled Exception Thrown Inside Catch");
        }
      }
      catch (IllegalStateException cause) {
        this.insideCatchIllegalArgumentExceptionBlock.set(true);
      }
      finally {
        this.insideFinallyBlock.set(true);
      }
    }
  }
}
