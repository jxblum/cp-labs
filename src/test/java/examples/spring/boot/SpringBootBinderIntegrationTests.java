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
package examples.spring.boot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

/**
 * @author John Blum
 */
@SpringBootTest(properties = {
	"test.properties.app.name=MyApp",
	"test.properties.app.auth.user.name=JonDoe",
	"test.properties.app.auth.user.password=s3cr3t"
})
@SuppressWarnings("unused")
public class SpringBootBinderIntegrationTests {

	@Autowired
	private Environment environment;

	@Test
	void binderBindsPropertiesToRecords() {

		BindResult<TestProperties> result = Binder.get(environment)
			.bind("test.properties.app", TestProperties.class);

		assertThat(result).isNotNull();
		assertThat(result.isBound()).isTrue();

		TestProperties properties = result.get();

		assertThat(properties).isNotNull();
		assertThat(properties.name()).isEqualTo("MyApp");
		assertThat(properties.auth().user().name()).isEqualTo("JonDoe");
		assertThat(properties.auth().user().password()).isEqualTo("s3cr3t");
	}

	@SpringBootConfiguration
	static class BinderTestConfiguration {

	}

	record TestProperties(String name, AuthProperties auth) {

	}

	record AuthProperties(User user) {

	}

	record User(String name, String password) {

	}
}
