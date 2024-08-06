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
package examples.gatling;

import static io.gatling.javaapi.core.CoreDsl.ElFileBody;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;
import static org.cp.elements.lang.RuntimeExceptionsFactory.newIllegalStateException;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.ObjectUtils;
import org.cp.elements.security.model.User;
import org.cp.labs.model.TestUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * Gatling {@link Simulation} test for the {@link org.cp.labs.spring.boot.SpringBootJsonWebApplication}.
 *
 * @author John Blum
 * @see io.gatling.javaapi.core.Simulation
 * @since 0.3.0
 */
@SuppressWarnings("unused")
public class SpringBootJsonWebApplicationGatlingSimulation extends Simulation {

  private static final ObjectMapper objectMapper = JsonMapper.builder()
      .addModule(new Jdk8Module())
      .addModule(new JavaTimeModule())
      .configure(SerializationFeature.INDENT_OUTPUT, true)
      .configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true)
      .build();

  private static final String USER_DATA = "users.csv";
  private static final String USER_JSON_TEMPLATE = "user-partial.json.tmpl";

  private static final ThreadLocalRandom random = ThreadLocalRandom.current();

  // TEST
  {
    setUp(runUsersScenarioTests().injectOpen(constantUsersPerSec(10).during(Duration.ofSeconds(60))))
      .protocols(httpProtocol());
  }

  private static HttpProtocolBuilder httpProtocol() {

    return http.baseUrl("http://localhost:8080/example/rest/api")
      .acceptHeader(MediaType.APPLICATION_JSON_VALUE)
      .acceptLanguageHeader("en-US,en;q=0.5")
      .contentTypeHeader(MediaType.APPLICATION_JSON_VALUE)
      .userAgentHeader("Mozilla/5.0 (Macintosh; ARM Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/119.0");
  }

  @SuppressWarnings("all")
  private static <T> T log(T target) {
    System.out.printf("%s%n", target);
    System.out.flush();
    return target;
  }

  private static Function<Session, Session> logSessionAttribute(String attribute) {

    return session -> {
      log("Session attribute [%s] is [%s]".formatted(attribute, session.get(attribute)));
      return session;
    };
  }

  private static Function<Session, Session> setSessionAttribute(String attributeName, Object value) {
    return session -> session.set(attributeName, value);
  }

  private static User<UUID> newUser(String username) {

    Assert.hasText(username, "Username [%s] is required", username);
    Assert.isFalse("#{username}".equals(username), "Username [%s] was not set", username);

    return TestUser.named(username).lastAccessedNow();
  }

  private static ObjectMapper objectMapper() {
    return objectMapper;
  }

  private static TestUser.Role randomRole() {
    TestUser.Role[] roles = TestUser.Role.values();
    return roles[random.nextInt(roles.length)];
  }

  private static String toJson(Object target) {

    return ObjectUtils.<String>doOperationSafely(args -> objectMapper().writeValueAsString(target), cause -> {
      throw newIllegalStateException(cause, "Failed to serialize Object [%s] as JSON", target);
    });
  }

  // TEST SCENARIOS

  private static ScenarioBuilder runPeopleScenarioTests() {

    FeederBuilder<String> peopleFeeder = csv("people.csv").random();

    return scenario("Get Person")
      .feed(peopleFeeder)
      .exec(http("Get Person").get("/people/#{id}")
        .check(status().is(200),
          jsonPath("$.firstName").is(session -> session.get("#{firstName}")),
          jsonPath("$.lastName").is(session -> session.get("#{lastName}"))));
  }

  @SuppressWarnings("all")
  private static ScenarioBuilder runUsersScenarioTests() {

    FeederBuilder<String> userFeeder = csv(USER_DATA).eager().random();

    Supplier<Map<String, Object>> userMetadataSupplier = () -> Map.of(
    "lastAccess", Instant.now().toEpochMilli(),
    "role", randomRole(),
    "token", UUID.randomUUID().toString()
    );

    Iterator<Map<String, Object>> userMetadataIterator = Stream.generate(userMetadataSupplier).iterator();

    Instant now = Instant.now();

    // NOTE: Cannot assert User lastAccess, role and token properties when testing with multiple users (sessions).
    // lastAccess, role and token values are dynamically generated, constantly changing per HTTP (GET) request
    // and per (concurrent) user.
    // Assertions to work correctly when testing with a single user (session).
    return scenario("POST and GET Users")
        .feed(userFeeder)
        .feed(userMetadataIterator)
        //.exec(logSessionAttribute("token"))
        .exec(http("POST user").post("/users")
            .queryParam("lastAccess", "#{lastAccess}")
            //.body(StringBody(session -> toJson(newUser(session.get("username"))))).asJson()
            .body(ElFileBody(USER_JSON_TEMPLATE)).asJson()
            .check(status().is(HttpStatus.OK.value())))
        .pause(Duration.ofMillis(500))
        .exec(http("GET user").get("/users/#{username}")
            .check(status().is(HttpStatus.OK.value()),
                //jsonPath("$.name").saveAs("responseUsername"),
                jsonPath("$.name").is(session -> session.get("username"))));
                //jsonPath("$.lastAccess").ofLong().gt(now.toEpochMilli()),
                //jsonPath("$.role").isEL("#{role}"),
                //jsonPath("$.token").is(session -> session.get("token"))));
        //.exec(logSessionAttribute("responseUsername"));
  }
}
