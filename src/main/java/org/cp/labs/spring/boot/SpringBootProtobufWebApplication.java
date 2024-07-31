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

import static org.assertj.core.api.Assertions.assertThat;
import static org.cp.elements.lang.RuntimeExceptionsFactory.newIllegalStateException;
import static org.cp.elements.lang.RuntimeExceptionsFactory.newRuntimeException;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.protobuf.Message;

import org.cp.domain.core.enums.Gender;
import org.cp.domain.core.model.Name;
import org.cp.domain.core.model.Person;
import org.cp.domain.core.serialization.protobuf.PersonSerializer;
import org.cp.elements.io.IOUtils;
import org.cp.elements.lang.Assert;
import org.cp.elements.lang.Integers;
import org.cp.elements.lang.ObjectUtils;
import org.cp.elements.lang.StringUtils;
import org.cp.elements.lang.ThrowableOperation;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersSpec.ExchangeFunction;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Example {@link SpringBootApplication} running as a Web Application accepting a Protobuf {@link Message}
 * to a Spring Web MVC {@link RestController} and then returning {@literal JSON}.
 *
 * @author John Blum
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.ApplicationRunner
 * @see org.cp.domain.core.model.Person
 * @see com.google.protobuf.Message
 * @since 0.1.0
 */
@SpringBootApplication
@Profile(SpringBootProtobufWebApplication.SPRING_APPLICATION_PROFILE)
@SuppressWarnings("unused")
public class SpringBootProtobufWebApplication {

  public static final String SPRING_APPLICATION_PROFILE = "protobuf-spring-web-application";

  private static final String APPLICATION_BINARY_MEDIA_TYPE_VALUE = "application/x-binary";
  private static final String SUCCESS_RETURN_VALUE = "SUCCESS";
  private static final String WEB_APPLICATION_BASE_URL = "http://localhost:8080/example";

  private static final PersonSerializer personSerializer = new PersonSerializer();

  private static final MediaType APPLICATION_BINARY_MEDIA_TYPE = MediaType.valueOf(APPLICATION_BINARY_MEDIA_TYPE_VALUE);

  public static void main(String[] args) {

    new SpringApplicationBuilder(SpringBootProtobufWebApplication.class)
      .profiles(SPRING_APPLICATION_PROFILE)
      .headless(false)
      .build()
      .run(args);
  }

  @SpringBootConfiguration
  @Import({ PersonJsonSerializer.class, PersonJsonDeserializer.class })
  static class ApplicationConfiguration implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
      converters.add(personMessageConverter(WriteStrategy.JSON));
    }
  }

  @Bean
  ApplicationRunner programRunner(ObjectMapper objectMapper) {

    return applicationArguments -> {

      Person jonDoe = Person.newPerson("Jon", "Doe")
        .born(birthdate(2000, Month.APRIL, 1))
        .asMale()
        .atVersion(UUID.randomUUID());

      PeopleResourceWriteStrategy resourceWriteStrategy = PeopleResourceWriteStrategy.PROTOBUF;

      RestClient restClient = buildRestClient(objectMapper, resourceWriteStrategy);

      String webApplicationResponse = restClient.post()
        .contentType(resourceWriteStrategy.getMediaType())
        .body(jonDoe)
        .exchange(httpResponseHandler());

      assertThat(webApplicationResponse).isEqualTo(SUCCESS_RETURN_VALUE);

      if (Desktop.isDesktopSupported()) {
        doSafely(ThrowableOperation.fromVoidReturning(arguments ->
          Desktop.getDesktop().browse(toUri("/people/".concat(asResourceIdentifier(jonDoe))))));
      }
    };
  }

  private RestClient buildRestClient(ObjectMapper objectMapper, PeopleResourceWriteStrategy resourceWriteStrategy) {

    return RestClient.builder()
      .baseUrl(WEB_APPLICATION_BASE_URL.concat(resourceWriteStrategy.getResource()))
      .messageConverters(httpMessageConverters ->
        httpMessageConverters.add(0, personMessageConverter(resourceWriteStrategy.getWriteStrategy())))
      .requestInterceptor(httpRequestHeaderInspectingInterceptor(objectMapper))
      .build();
  }

  private static String asResourceIdentifier(Person person) {
    return person.getFirstName().toLowerCase().concat(person.getLastName());
  }

  private static Person assertPerson(Person person, String name) {

    assertThat(person).isNotNull();
    assertThat(person.getName()).hasToString(name);

    return person;
  }

  private static LocalDateTime birthdate(int year, Month month, int day) {
    return LocalDate.of(year, month, day).atStartOfDay();
  }

  private static void log(String message, Object... arguments) {
    System.out.printf(message, arguments);
    System.out.flush();
  }

  private static HttpMessageConverter<Person> personMessageConverter(WriteStrategy writeStrategy) {

    return new AbstractHttpMessageConverter<>() {

      private PersonSerializer getPersonSerializer() {
        return personSerializer;
      }

      @Override
      public @NonNull List<MediaType> getSupportedMediaTypes() {
        return List.of(APPLICATION_BINARY_MEDIA_TYPE, MediaType.APPLICATION_JSON);
      }
      @Override
      protected boolean supports(@NonNull Class<?> type) {
        return Person.class.isAssignableFrom(type);
      }

      @Override
      protected @NonNull Person readInternal(@NonNull Class<? extends Person> type,
          @NonNull HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {

        log("[DEBUG] Reading as Protobuf message");

        // RECEIVE (read) as Protobuf message (server-side)
        byte[] data = IOUtils.toByteArray(inputMessage.getBody());

        return getPersonSerializer().deserialize(ByteBuffer.wrap(data));
      }

      @Override
      protected void writeInternal(@NonNull Person person, @NonNull HttpOutputMessage outputMessage)
          throws IOException, HttpMessageNotWritableException {

        // SEND (write) as either JSON or Protobuf message (client-side)
        writeStrategy.write(person, outputMessage);
      }
    };
  }

  private ClientHttpRequestInterceptor httpRequestHeaderInspectingInterceptor(ObjectMapper objectMapper) {

    return (request, body, execution) -> {

      HttpHeaders httpHeaders = request.getHeaders();

      assertThat(httpHeaders).isNotNull().isNotEmpty();
      assertThat(body).isNotNull().isNotEmpty();
      assertThat(body.length).isEqualTo(Integers.asInteger(httpHeaders.getContentLength()));

      if (APPLICATION_BINARY_MEDIA_TYPE.equals(httpHeaders.getContentType())) {
        Person person = assertPerson(personSerializer.deserialize(ByteBuffer.wrap(body)), "Jon Doe");
        log("[PROTOBUF] Person [%s]%n", person);
      }
      else if (MediaType.APPLICATION_JSON.equals(httpHeaders.getContentType())) {
        Person person = assertPerson(objectMapper.readValue(body, Person.class), "Jon Doe");
        log("[JSON] Person [%s]%n", person);
      }

      return execution.execute(request, body);
    };
  }

  private ExchangeFunction<String> httpResponseHandler() {
    return (request, response) -> response.getStatusCode().is2xxSuccessful() ? toString(response.getBody())
      : throwRuntimeException(request, response);
  }

  private String throwRuntimeException(HttpRequest request, ClientHttpResponse response) throws IOException {
    throw newRuntimeException("Failed to send HTTP POST request to [%s]; status code [%s]",
      request.getURI(), response.getStatusCode());
  }

  private String toString(InputStream body) {
    byte[] data = doSafely(args -> IOUtils.toByteArray(body));
    return new String(data);
  }

  private URI toUri(String resource) {
    return URI.create(WEB_APPLICATION_BASE_URL.concat(resource));
  }

  @RestController
  @RequestMapping("/example")
  static class ProtobufRestController {

    private final ConcurrentMap<String, Person> personByNameCache = new ConcurrentHashMap<>();

    ProtobufRestController() {

      Person tinaBush = Person.newPerson("Tina", "Bush")
        .born(birthdate(1975, Month.APRIL, 16))
        .asFemale()
        .atVersion(UUID.randomUUID());

      getPersonByNameCache().putIfAbsent(asResourceIdentifier(tinaBush), tinaBush);
    }

    protected ConcurrentMap<String, Person> getPersonByNameCache() {
      return this.personByNameCache;
    }

    @GetMapping("/people/{name}")
    Person getPerson(@PathVariable("name") String name) {
      return getPersonByNameCache().get(name);
    }

    @PostMapping("/json/people")
    String storePersonFromJson(@RequestBody Person person) {
      return processPerson(person);
    }

    @PostMapping(path = "/protobuf/people", consumes = MediaType.ALL_VALUE)
    String storePersonFromProtobufMessage(@RequestBody Person person) {
      return processPerson(person);
    }

    private String processPerson(Person person) {
      Assert.notNull(person, "Person is required");
      getPersonByNameCache().putIfAbsent(asResourceIdentifier(person), person);
      return SUCCESS_RETURN_VALUE;
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

  @Getter
  @RequiredArgsConstructor
  enum PeopleResourceWriteStrategy {

    JSON(MediaType.APPLICATION_JSON, "/json/people", WriteStrategy.JSON),
    PROTOBUF(APPLICATION_BINARY_MEDIA_TYPE, "/protobuf/people", WriteStrategy.PROTOBUF);

    private final MediaType mediaType;
    private final String resource;
    private final WriteStrategy writeStrategy;

  }

  enum WriteStrategy {

    JSON {

      @Override
      public void write(Person person, HttpOutputMessage outputMessage) throws IOException {

        String json = newObjectMapper().writeValueAsString(person);
        OutputStream body = outputMessage.getBody();

        body.write(json.getBytes());
        body.flush();
      }

      private ObjectMapper newObjectMapper() {

        SimpleModule personSerializationModule = new SimpleModule("PersonSerializationModule")
          .addSerializer(Person.class, new PersonJsonSerializer())
          .addDeserializer(Person.class, new PersonJsonDeserializer());

        return JsonMapper.builder()
          .addModule(personSerializationModule)
          .build();
      }
    },

    PROTOBUF {

      @Override
      public void write(Person person, HttpOutputMessage outputMessage) throws IOException {

        byte[] data = getPersonSerializer().serialize(person).array();
        OutputStream body = outputMessage.getBody();

        body.write(data);
        body.flush();;
      }

      private PersonSerializer getPersonSerializer() {
        return personSerializer;
      }
    };

    public void write(Person person, HttpOutputMessage outputMessage) throws IOException {
      throw newIllegalStateException("Failed to write Person [%s] and send in HTTP output message", person);
    }
  }

  interface ThrowingRunnable {
    void run() throws Throwable;
  }

  private static <T> T doSafely(ThrowableOperation<T> operation) {
    return ObjectUtils.doOperationSafely(operation);
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
