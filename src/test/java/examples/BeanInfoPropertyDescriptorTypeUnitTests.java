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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import org.cp.elements.lang.Nameable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Unit Tests for Java {@literal JavaBeans} framework and APIs.
 *
 * @author John Blum
 * @see java.beans
 * @since 0.1.0
 */
public class BeanInfoPropertyDescriptorTypeUnitTests {

  @Test
  void customerNamePropertyTypeIsString() throws Exception {

    BeanInfo customerInfo = Introspector.getBeanInfo(Customer.class);

    PropertyDescriptor namePropertyDescriptor = Arrays.stream(customerInfo.getPropertyDescriptors())
      .filter(propertyDescriptor -> "name".equals(propertyDescriptor.getName()))
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("No name property found for Customer"));

    assertThat(namePropertyDescriptor.getPropertyType()).isEqualTo(String.class);
  }

  interface Person extends Nameable<String> {
    //@Override String getName(); // Cause Java (JRE) 21 to fail!
  }

  static class Customer implements Person {

    private final String name;

    Customer(String name) {
      this.name = name;
    }

    @Override
    public String getName() {
      return this.name;
    }

    @Override
    public String toString() {
      return getName();
    }
  }

  @Getter
  @RequiredArgsConstructor(staticName = "as")
  static class Vip implements Person {
    private final String name;
  }
}
