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
package examples.spring.framework;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

import org.cp.elements.service.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Integration Tests asserting the auto-wiring behavior of the Spring container.
 * </p>
 * Tests the default auto-wire by type functionality, which falls back to auto-wire by name, as long as:
 * </p>
 * 1. The application code was compiled with {@literal javac -parameters} (debug mode), and...
 * 2. The name of the property declared in the dependent bean refers to the collaborating bean
 * by id or name as defined in Spring configuration.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see org.springframework.boot.SpringBootConfiguration
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.context.ApplicationContext
 */
@SpringBootTest
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class SpringContainerAutowiringIntegrationTests {

	@Autowired
	private TestService barService;

	@Autowired
	private TestService fooService;

	@Test
	void autowiring() {
		assertThat(getBarService().doSomething()).isEqualTo("Bar");
		assertThat(getFooService().doSomething()).isEqualTo("Foo");
	}

	@SpringBootConfiguration
	static class SpringContainerAutowiringTestConfiguration {

		@Bean
		BarService barService() {
			return new BarService();
		}

		@Bean
		FooService fooService() {
			return new FooService();
		}
	}

	@Service
	@FunctionalInterface
	interface TestService {
		String doSomething();
	}

	static class BarService implements TestService {

		@Override
		public String doSomething() {
			return "Bar";
		}
	}

	static class FooService implements TestService {

		@Override
		public String doSomething() {
			return "Foo";
		}
	}
}
