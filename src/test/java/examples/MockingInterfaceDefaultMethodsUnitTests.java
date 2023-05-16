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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

/**
 * Unit Tests for JUnit 5 and Mockito 5.3.x mocking default methods on a interface.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see org.mockito.Mockito
 * @since 1.0.0
 */
public class MockingInterfaceDefaultMethodsUnitTests {

  @Test
  @SuppressWarnings("unchecked")
  public void containsAllKeysReturnsTrue() {

    Cache<String, Object> mockCache = mock(Cache.class);

    doReturn(true).when(mockCache).contains(any());
    doCallRealMethod().when(mockCache).containsAll(any());

    assertThat(mockCache.containsAll("KeyOne", "KeyTwo")).isTrue();

    verify(mockCache, times(1)).containsAll(eq("KeyOne"), eq("KeyTwo"));

    Arrays.asList("KeyOne", "KeyTwo").forEach(key ->
      verify(mockCache, times(1)).contains(eq(key)));

    verifyNoMoreInteractions(mockCache);
  }

  @SuppressWarnings("unused")
  interface Cache<KEY extends Comparable<KEY>, VALUE> extends Iterable<KEY> {

    default boolean contains(KEY key) {
      return key != null && StreamSupport.stream(this.spliterator(), false)
        .anyMatch(it -> it.equals(key));
    }

    @SuppressWarnings("unchecked")
    default boolean containsAll(KEY... keys) {
      return Arrays.stream(keys).allMatch(this::contains);
    }
  }
}
