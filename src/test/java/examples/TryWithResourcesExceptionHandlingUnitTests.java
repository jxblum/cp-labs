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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.Closeable;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import org.cp.elements.test.TestException;
import org.mockito.InOrder;

/**
 * Unit Tests testing the behavior of {@literal try-with-resources} when an {@link Exception} is thrown.
 *
 * @author John Blum
 * @see java.io.Closeable
 * @see org.junit.jupiter.api.Test
 * @see org.mockito.Mockito
 * @since 0.1.0
 */
public class TryWithResourcesExceptionHandlingUnitTests {

  @Test
  @SuppressWarnings("all")
  public void tryWithResourcesProcessesNormally() throws IOException {

    Connection mockConnection = mock(Connection.class);

    Resource mockResource = mock(Resource.class);

    doReturn(mockConnection).when(mockResource).getConnection();

    try (Connection connection = mockResource.getConnection()) {
      connection.use();
    }
    catch (IOException ignore) { }

    InOrder order = inOrder(mockResource, mockConnection);

    order.verify(mockResource, times(1)).getConnection();
    order.verify(mockConnection, times(1)).use();
    order.verify(mockConnection, times(1)).close();

    verifyNoMoreInteractions(mockResource, mockConnection);
  }

  @Test
  @SuppressWarnings("all")
  public void tryWithResourcesThrowsException() throws IOException {

    Connection mockConnection = mock(Connection.class);

    doThrow(new TestException("BOOM")).when(mockConnection).init();

    Resource mockResource = mock(Resource.class);

    doAnswer(invocation -> {
      mockConnection.init();
      return mockConnection;
    }).when(mockResource).getConnection();

    try (Connection connection = mockResource.getConnection()) {
      connection.use();
    }
    catch (Exception expected) {
      assertThat(expected).isInstanceOf(TestException.class)
        .hasMessage("BOOM")
        .hasNoCause();
    }

    InOrder order = inOrder(mockResource, mockConnection);

    order.verify(mockResource, times(1)).getConnection();
    order.verify(mockConnection, times(1)).init();
    order.verify(mockConnection, never()).close();

    verify(mockConnection, never()).use();
    verifyNoMoreInteractions(mockResource, mockConnection);
  }

  interface Resource {
    Connection getConnection();
  }

  interface Connection extends Closeable {
    void init();
    Object use(Object... arguments);
  }
}
