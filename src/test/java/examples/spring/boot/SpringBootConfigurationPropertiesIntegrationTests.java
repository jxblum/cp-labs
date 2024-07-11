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

import org.cp.elements.lang.Constants;
import org.cp.elements.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration Tests asserting and comparing the difference between using {@link Class}-based configuration properties
 * and {@code record}-based configuration properties, along with missing properties and mixed property types.
 *
 * @author John Blum
 * @see org.springframework.boot.SpringBootConfiguration
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @see org.springframework.boot.context.properties.EnableConfigurationProperties
 * @see org.springframework.boot.test.context.SpringBootTest
 */
@SpringBootTest
@ActiveProfiles("at-configuration-properties-test")
@SuppressWarnings("unused")
public class SpringBootConfigurationPropertiesIntegrationTests {

	@Autowired
	private TestProperties testProperties;

	@Test
	void configurationPropertiesAreCorrect() {

		assertThat(this.testProperties.name).isEqualTo("TEST_PROPERTIES");
		assertThat(this.testProperties.auth.username).isEqualTo("jonDoe");
		assertThat(this.testProperties.auth.password).isEqualTo("s3cr3t");
		//assertThat(this.testProperties.data.value).isEqualTo("TEST_VALUE");
	}

	@SpringBootConfiguration
	@Profile("at-configuration-properties-test")
	@EnableConfigurationProperties({ TestProperties.class })
	static class ConfigurationPropertiesTestConfiguration {

	}

	/*
	@Getter
	@ConfigurationProperties("test.properties")
	public static class TestProperties {

		AuthProperties auth;
		String name;

		TestProperties(String name, AuthProperties auth) {
			this.auth = auth;
			this.name = name;
		}

		@Getter
		static class AuthProperties {

			String username;
			String password;

			AuthProperties(String username, String password) {
				this.username = username;
				this.password = password;
			}
		}
	}
	*/

	@ConfigurationProperties("test.properties")
	record TestProperties(String name, AuthProperties auth, DataProperties data) {

		TestProperties {
			name = StringUtils.hasText(name) ? name : Constants.UNKNOWN;
			auth = auth != null ? auth : AuthProperties.defaults();
		}
	}

	record AuthProperties(String username, String password) {

		static AuthProperties defaults() {
			return new AuthProperties("test", "test");
		}
	}

	record DataProperties(String value) {

	}

	enum Passwords {
		AAA, BBB, CCC;
		static final Passwords DEFAULT = AAA;
	}

	record Password(String value) {
		static Password from(String value) {
			return new Password(value);
		}
	}
}
