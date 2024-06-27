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
package examples.couchbase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.cp.elements.util.stream.StreamUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.couchbase.DataCouchbaseTest;
import org.springframework.data.couchbase.core.CouchbaseTemplate;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * @author John Blum
 */
@DataCouchbaseTest(properties = {
	"spring.couchbase.connection-string=couchbase://127.0.0.1",
	"spring.couchbase.username=Administrator",
	"spring.couchbase.password=p@ssw0rd",
	"spring.data.couchbase.bucket-name=users",
})
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
// TODO: Use Testcontainers to start Couchbase server or add @Conditional annotation
//  to disable this test if Couchbase server is not available.
public class CouchbasePersistenceIntegrationTests {

	@Autowired
	private CouchbaseTemplate couchbaseTemplate;

	@Autowired
	private UserRepository userRepository;

	@BeforeEach
	public void flushExistingUsers() {
		getUserRepository().deleteAll();
	}

	@Test
	void saveWithRepositoryFindWithTemplate() {

		User jonDoe = User.named("JonDoe");
		User janeDoe = User.named("JaneDoe");
		User bobDoe = User.named("BobDoe");
		User froDoe = User.named("FroDoe");
		User pieDoe = User.named("PieDoe");

		List<User> expectedUsers = List.of(jonDoe, janeDoe, bobDoe, froDoe, pieDoe);

		getUserRepository().saveAll(identify(expectedUsers));

		//assertThat(getUserRepository().count()).isEqualTo(5);
		await().pollInterval(Duration.ofSeconds(1)).untilAsserted(() ->
			assertThat(getUserRepository().count()).isEqualTo(5));

		List<User> actualUsers = getCouchbaseTemplate().findByQuery(User.class).stream().toList();

		assertThat(actualUsers).isNotNull().hasSize(5);
		assertThat(actualUsers).containsAll(expectedUsers);
	}

	@Test
	void persistUserWithRoles() {

		User jonDoe = User.named("LanDoe")
			.add(Role.asExecuteUser())
			.add(Role.asReadWriteDeveloper());

		getUserRepository().save(identify(jonDoe));

		User loadedJonDoe = getUserRepository().findByName(jonDoe.getName());

		assertThat(loadedJonDoe).isNotNull().isNotSameAs(jonDoe).isEqualTo(jonDoe);
		assertThat(loadedJonDoe.getRoles()).containsAll(List.of(Role.asExecuteUser(), Role.asReadWriteDeveloper()));
	}

	@Test
	void persistUserWithRolesUsingTemplate() {

		User jonDoe = User.named("JoeDoe")
			.add(Role.asExecuteUser())
			.add(Role.asReadWriteDeveloper());

		getCouchbaseTemplate().save(identify(jonDoe));

		User loadedJonDoe = getCouchbaseTemplate().findByQuery(User.class).one().orElse(null);

		assertThat(loadedJonDoe).isNotNull().isNotSameAs(jonDoe).isEqualTo(jonDoe);
		assertThat(loadedJonDoe.getRoles()).containsAll(List.of(Role.asExecuteUser(), Role.asReadWriteDeveloper()));
	}

	private Iterable<User> identify(Iterable<User> users) {
		StreamUtils.stream(users).forEach(this::identify);
		return users;
	}

	private User identify(User user) {
		user.setId(UUID.randomUUID());
		return user;
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	static class CouchbaseTestConfiguration { }

}
