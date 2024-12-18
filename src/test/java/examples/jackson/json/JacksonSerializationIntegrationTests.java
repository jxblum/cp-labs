/*
 * Copyright 2024 Author or Authors.
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import org.junit.jupiter.api.Test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Integration Test using Jackson {@link JsonMapper} to de/serialize and map JSON to a POJO.
 *
 * @see org.junit.jupiter.api.Test
 * @see com.fasterxml.jackson.databind.json.JsonMapper
 * @author John Blum
 */
public class JacksonSerializationIntegrationTests {

	private final JsonMapper objectMapper = JsonMapper.builder()
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
		.build();

	@Test
	void deserializationOfJsonWithUnknownField() throws JsonProcessingException {

		String JSON = """
			{
			  "firstName": "Jon",
			  "middleName": "R",
			  "lastName": "Doe"
		 	}
			""";

		Person person = this.objectMapper.readValue(JSON, Person.class);

		assertThat(person).isNotNull();
		assertThat(person.getFirstName()).isEqualTo("Jon");
		assertThat(person.getLastName()).isEqualTo("Doe");
	}

	@Test
	void deserializationOfJsonWithMissingFields() throws JsonProcessingException {

		String JSON = """
			{
			  "firstName": "Jon"
		  	}
			""";

		Person person = this.objectMapper.readValue(JSON, Person.class);

		assertThat(person).isNotNull();
		assertThat(person.getFirstName()).isEqualTo("Jon");
		assertThat(person.getLastName()).isNull();
	}

	@Test
	void serializingObjectToJsonWithNullValues() throws JsonProcessingException {

		Person jon = Person.builder().firstName("Jon").build();

		String expectedJson = "{\"firstName\":\"%s\"}".formatted(jon.getFirstName());

		String actualJson= this.objectMapper.writeValueAsString(jon);

		assertThat(actualJson).isEqualTo(expectedJson);
	}

	@Getter
	@Builder
	@ToString
	@EqualsAndHashCode
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonInclude(JsonInclude.Include.NON_NULL)
	//@JsonIgnoreProperties(ignoreUnknown = true)
	static class Person {

		private String firstName;
		private String lastName;

	}
}
