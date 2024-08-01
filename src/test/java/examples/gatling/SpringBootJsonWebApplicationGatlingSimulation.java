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

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.cp.elements.security.model.User;
import org.springframework.http.MediaType;

import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Gatling {@link Simulation} test for the {@link org.cp.labs.spring.boot.SpringBootJsonWebApplication}.
 *
 * @author John Blum
 * @see io.gatling.javaapi.core.Simulation
 * @since 0.3.0
 */
@SuppressWarnings("unused")
public class SpringBootJsonWebApplicationGatlingSimulation extends Simulation {

  {
    setUp(runPeopleScenarioTests().injectOpen(constantUsersPerSec(5).during(Duration.ofMinutes(1))))
      .protocols(withHttpProtocol());
  }

  private static HttpProtocolBuilder withHttpProtocol() {

    return http.baseUrl("http://localhost:8080/example/rest/api")
      .acceptHeader(MediaType.APPLICATION_JSON_VALUE)
      .acceptLanguageHeader("en-US,en;q=0.5")
      .contentTypeHeader(MediaType.APPLICATION_JSON_VALUE)
      .userAgentHeader("Mozilla/5.0 (Macintosh; ARM Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/119.0");
  }

  private static ScenarioBuilder runPeopleScenarioTests() {

    FeederBuilder<String> peopleFeeder = csv("people.csv").random();

    return scenario("Get Person")
      .feed(peopleFeeder)
      .exec(http("Get Person").get("/people/#{id}")
        .check(status().is(200),
          jsonPath("$.firstName").is(session -> session.get("#{firstName}")),
          jsonPath("$.lastName").is(session -> session.get("#{lastName}"))));

  }

  private static ScenarioBuilder runUsersScenarioTests() {

    FeederBuilder<String> userFeeder = csv("users.csv").random();

    return scenario("Post and Get Users")
      .feed(userFeeder)
      .exec(http("POST user").post("/users/#{username}")
        .check(status().is(200)));
  }

  @Getter
  @Setter(AccessLevel.PROTECTED)
  @RequiredArgsConstructor(staticName = "name")
  static class TestUser implements User<UUID> {

    private Instant lastAccess;

    private final String name;

    @Setter(AccessLevel.PUBLIC)
    private UUID id;

    @SuppressWarnings("unchecked")
    public TestUser identifiedBy(UUID id) {
      setId(id);
      return this;
    }


    public TestUser lastAccessNow() {
      return lastAccessed(Instant.now());
    }

    public TestUser lastAccessed(long timestamp) {
      return lastAccessed(Instant.ofEpochSecond(timestamp));
    }

    public TestUser lastAccessed(Instant lastAccess) {
      setLastAccess(lastAccess);
      return this;
    }
  }
}
