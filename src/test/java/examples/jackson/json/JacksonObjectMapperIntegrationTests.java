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
package examples.jackson.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cp.elements.lang.RuntimeExceptionsFactory.newRuntimeException;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.junit.jupiter.api.Test;

import org.cp.domain.core.enums.Gender;
import org.cp.domain.core.model.Name;
import org.cp.domain.core.model.Person;
import org.cp.elements.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.annotation.Bean;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Integration Test asserting the function of Jackson's {@link ObjectMapper}.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @since 0.2.0
 */
@JsonTest
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class JacksonObjectMapperIntegrationTests {

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void personJsonSerialization() throws Exception {

    Person jonDoe = Person.newPerson("Jon", "Doe")
      .born(LocalDateTime.of(2024, Month.JULY, 14, 1, 7, 30))
      .asMale()
      .atVersion(UUID.randomUUID());

    String json = getObjectMapper().writeValueAsString(jonDoe);

    assertThat(json).isNotNull().isNotBlank();

    Person deserializedJonDoe = getObjectMapper().readValue(json, Person.class);

    assertThat(deserializedJonDoe).isNotNull()
      .isNotSameAs(jonDoe)
      .isEqualTo(jonDoe);
  }

  @SpringBootConfiguration
  static class JacksonObjectMapperConfiguration {

    @Bean
    PersonJsonSerializer personJsonSerializer() {
      return new PersonJsonSerializer();
    }

    @Bean
    PersonJsonDeserializer personJsonDeserializer() {
      return new PersonJsonDeserializer();
    }
  }

  @JsonComponent
  static class PersonJsonSerializer extends JsonSerializer<Person> {

    @Override
    public void serialize(Person person, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
        throws IOException {

      jsonGenerator.writeStartObject();
      jsonGenerator.writeStringField("firstName", person.getFirstName());
      jsonGenerator.writeStringField("lastName", person.getLastName());

      person.getBirthDate().ifPresent(birthdate -> runSafely(() ->
        jsonGenerator.writeNumberField("birthdate", toTimestamp(birthdate))));

      person.getGender().ifPresent(gender -> runSafely(() -> jsonGenerator.writeStringField("gender", gender.name())));

      person.getMiddleName().filter(StringUtils::hasText).ifPresent(middleName -> runSafely(() ->
        jsonGenerator.writeStringField("middleName", middleName)));

      jsonGenerator.writeEndObject();
    }

    private long toTimestamp(LocalDateTime dateTime) {
      return dateTime.atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli();
    }
  }

  @JsonComponent
  static class PersonJsonDeserializer extends JsonDeserializer<Person> {

    @Override
    public Person deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

      ObjectCodec objectCodec = jsonParser.getCodec();
      JsonNode jsonNode = objectCodec.readTree(jsonParser);

      String firstName = jsonNode.get("firstName").asText();
      String middleName = jsonNode.has("middleName") ? jsonNode.get("middleName").asText() : null;
      String lastName = jsonNode.get("lastName").asText();

      LocalDateTime birthdate = jsonNode.has("birthdate") ? fromTimestamp(jsonNode.get("birthdate").asLong()) : null;

      Gender gender = jsonNode.has("gender") ? Gender.valueOf(jsonNode.get("gender").asText()) : null;

      return Person.newPerson(Name.of(firstName, middleName, lastName))
        .born(birthdate)
        .as(gender);
    }

    private LocalDateTime fromTimestamp(long timestamp) {
      return Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.systemDefault()).toLocalDateTime();
    }
  }

  interface ThrowingRunnable {
    void run() throws Throwable;
  }

  private static void runSafely(ThrowingRunnable runnable) {

    try {
      runnable.run();
    }
    catch (Throwable e) {
      throw newRuntimeException(e);
    }
  }
}
