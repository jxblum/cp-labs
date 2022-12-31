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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Test;

/**
 * Unit Tests testing and asserting the behavior of Mockito 4.11 vs. 4.10 when mocking interfaces with default methods.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @since 1.0.0
 */
public class MockitoUnitTests {

  @Test
  @SuppressWarnings("unchecked")
  public void functionAsConsumerIsCorrect() {

    Object arguments = new Object[] { "mock", "test" };

    FunctionExtension<Object, Object> mockFunction = mock(FunctionExtension.class);

    doCallRealMethod().when(mockFunction).asConsumer();

    Consumer<Object> consumer = mockFunction.asConsumer();

    assertThat(consumer).isNotNull();

    consumer.accept(arguments);

    verify(mockFunction, times(1)).asConsumer();
    verify(mockFunction, times(1)).apply(eq(arguments));
    verifyNoMoreInteractions(mockFunction);
  }

  interface FunctionExtension<T, R> extends Function<T, R> {

    default Consumer<T> asConsumer() {
      return this::apply;
    }
  }
}
