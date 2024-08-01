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

import static org.awaitility.Awaitility.await;
import static org.cp.elements.lang.RuntimeExceptionsFactory.newIllegalArgumentException;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;

import org.cp.domain.core.model.People;
import org.cp.domain.core.model.Person;
import org.cp.elements.security.model.User;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * The SpringBootJsonWebApplication class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SpringBootApplication
@Profile(SpringBootJsonWebApplication.SPRING_APPLICATION_PROFILE)
@SuppressWarnings("unused")
public class SpringBootJsonWebApplication {

  public static final String SPRING_APPLICATION_PROFILE = "json-spring-web-application";

  public static void main(String[] args) {

    new SpringApplicationBuilder(SpringBootJsonWebApplication.class)
      .profiles(SPRING_APPLICATION_PROFILE)
      .web(WebApplicationType.SERVLET)
      .build()
      .run(args);
  }

  @RestController
  @Getter(AccessLevel.PROTECTED)
  @RequestMapping("/example/rest/api/")
  static class PeopleRestApiController {

    private final ConcurrentMap<String, User> userStore = new ConcurrentHashMap<>();

    private final People doeFamily = doeFamily();

    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    @GetMapping("/people/{id}")
    public Person getPerson(@PathVariable("id") Long id) {

      long milliseconds = getRandom().nextLong(2_000);
      long timeout = System.currentTimeMillis() + milliseconds;

      await().pollInterval(Duration.ofMillis(50)).until(() -> System.currentTimeMillis() > timeout);

      return getDoeFamily().stream()
        .filter(person -> person.getId().equals(id))
        .findFirst()
        .orElseThrow(() -> newIllegalArgumentException("Person with ID [%s] not found", id));
    }

    @GetMapping("/users/{username}")
    public User<UUID> getUser(@PathVariable("username") String username) {
      return getUserStore().get(username);
    }

    @PostMapping("/users")
    public String storeUser(@RequestBody User<UUID> user) {
      getUserStore().put(user.getName(), user);
      return "{\"status\": \"SUCCESS\"";
    }
  }

  private static People doeFamily() {

    return People.of(
      Person.newPerson("Jon", "Doe").identifiedBy(1L),
      Person.newPerson("Jane", "Doe").identifiedBy(2L),
      Person.newPerson("Bob", "Doe").identifiedBy(3L),
      Person.newPerson("Cookie", "Doe").identifiedBy(4L),
      Person.newPerson("Dill", "Doe").identifiedBy(5L),
      Person.newPerson("Fro", "Doe").identifiedBy(6L),
      Person.newPerson("Hoe", "Doe").identifiedBy(7L),
      Person.newPerson("Joe", "Doe").identifiedBy(8L),
      Person.newPerson("Lan", "Doe").identifiedBy(9L),
      Person.newPerson("Moe", "Doe").identifiedBy(10L),
      Person.newPerson("Pie", "Doe").identifiedBy(11L),
      Person.newPerson("Sour", "Doe").identifiedBy(12L)
    );
  }
}
