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

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.databind.SerializationFeature;

import org.cp.domain.core.model.People;
import org.cp.domain.core.model.Person;
import org.cp.elements.io.IOUtils;
import org.cp.elements.lang.ObjectUtils;
import org.cp.elements.security.model.User;
import org.cp.labs.model.TestUser;
import org.cp.labs.model.serialization.json.TestUserDeserializer;
import org.slf4j.Logger;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link SpringBootApplication} running a {@link People} and {@link User} REST API service
 * in a Spring Web application.
 *
 * @author John Blum
 * @see org.cp.domain.core.model.People
 * @see org.cp.elements.security.model.User
 * @see org.springframework.boot.SpringBootConfiguration
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @since 1.0.0
 */
@Slf4j
@SpringBootApplication
@Profile(SpringBootJsonWebApplication.SPRING_APPLICATION_PROFILE)
@SuppressWarnings("unused")
public class SpringBootJsonWebApplication {

  private static final boolean DEBUG = false;

  protected static final String SUCCESS_JSON = "{\"status\": \"SUCCESS\"}";

  public static final String SPRING_APPLICATION_PROFILE = "json-spring-web-application";

  public static void main(String[] args) {

    new SpringApplicationBuilder(SpringBootJsonWebApplication.class)
      .profiles(SPRING_APPLICATION_PROFILE)
      .web(WebApplicationType.SERVLET)
      .build()
      .run(args);
  }

  protected static Logger getLogger() {
    return log;
  }

  @SpringBootConfiguration
  @Import(TestUserDeserializer.class)
  static class JsonWebApplicationConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {

      if (DEBUG) {
        registry.addInterceptor(new HandlerInterceptor() {

          @Override
          public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
              @NonNull Object handler) throws IOException {

            String json = new String(IOUtils.toByteArray(request.getInputStream()));

            getLogger().info("JSON [{}]", json);
            getLogger().info("Handler [{}]", ObjectUtils.getClassName(handler));

            if (handler instanceof HandlerMethod handlerMethod) {
              getLogger().info("HandlerMethod bean [{}] of type [{}] calling method [{}]",
                handlerMethod.getBean(), handlerMethod.getBeanType().getName(), handlerMethod.getMethod().getName());
            }

            return true;
          }
        });
      }
    }

    @Bean
    Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperCustomizer() {

      return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder
          .featuresToEnable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
          .indentOutput(true);
    }
  }

  @RestController
  @Getter(AccessLevel.PROTECTED)
  @RequestMapping("/example/rest/api/")
  static class PeopleRestApiController {

    private final ConcurrentMap<String, User<UUID>> userStore = new ConcurrentHashMap<>();

    private final People doeFamily = doeFamily();

    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    public PeopleRestApiController() {

      User<UUID> testUser = TestUser.named("testUser")
          .withToken(UUID.randomUUID().toString())
          .lastAccessedNow()
          .asUser();

      this.userStore.put(testUser.getName(), testUser);
    }

    @GetMapping("/people")
    public People getPeople() {
      return getDoeFamily();
    }

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

    @GetMapping("/users")
    public Iterable<User<UUID>> getUsers() {
      return getUserStore().values().stream().sorted().toList();
    }

    @GetMapping("/users/{username}")
    public User<UUID> getUser(@PathVariable("username") String username) {
      return getUserStore().get(username);
    }

    @DeleteMapping("/users")
    public String removeAllUsers() {
      getUserStore().clear();
      return SUCCESS_JSON;
    }

    @DeleteMapping("/users/{username}")
    public User<UUID> removeUser(@PathVariable("username") String username) {
      return getUserStore().remove(username);
    }

    @PostMapping("/users")
    public String storeUser(@RequestParam(name = "lastAccess", required = false) Long lastAccess,
          @RequestBody User<UUID> user) {

      user = lastAccess != null && user instanceof TestUser testUser
          ? testUser.lastAccessed(lastAccess)
          : user;

      getUserStore().put(user.getName(), user);

      return SUCCESS_JSON;
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
