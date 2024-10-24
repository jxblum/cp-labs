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
package org.cp.labs.spring.boot;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.ObservationRegistry;

import org.cp.elements.security.model.User;
import org.cp.labs.model.TestUser;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author John Blum
 */
@SpringBootApplication
@SuppressWarnings("unused")
@Profile(SpringBootMicrometerWebApplication.SPRING_APPLICATION_PROFILE)
public class SpringBootMicrometerWebApplication {

	static final String SPRING_APPLICATION_PROFILE = "metrics";

	public static void main(String[] args) {

		new SpringApplicationBuilder(SpringBootMicrometerWebApplication.class)
			.profiles(SPRING_APPLICATION_PROFILE)
			.build()
			.run(args);
	}

	@Bean
	SimpleMeterRegistry simpleMeterRegistry() {
		return new SimpleMeterRegistry();
	}

	@RestController
	@RequestMapping("/api")
	@RequiredArgsConstructor
	@Getter(AccessLevel.PROTECTED)
	static class UserController {

		static final String PONG = "PONG";

		private final MeterRegistry meterRegistry;

		private final ObservationRegistry observationRegistry;

		private final Set<TestUser> users = new ConcurrentSkipListSet<>();

		@GetMapping("/ping")
		public String ping() {
			return PONG;
		}

		@GetMapping("/users")
		public List<TestUser> allUsers() {
			return getUsers().stream().sorted(Comparator.comparing(User::getName)).toList();
		}

		@Counted
		@GetMapping("/users/{name}")
		public TestUser getUser(@PathVariable("name") String username) {

			Counter countUsers = Counter.builder("user.count")
				.register(getMeterRegistry());

			return getUsers().stream()
				.filter(user -> user.getName().equalsIgnoreCase(username))
				.findFirst()
				.orElseGet(() -> {
					TestUser user = TestUser.named(username);
					getUsers().add(user);
					return user;
				});
		}
	}
}
