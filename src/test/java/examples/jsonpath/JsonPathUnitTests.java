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
package examples.jsonpath;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;

import org.junit.jupiter.api.Test;

/**
 * Unit Tests for {@literal JSONPath}.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see com.jayway.jsonpath.JsonPath
 */
public class JsonPathUnitTests {

	@SuppressWarnings("all")
	private static final String JSON = """
		{
			"name": "Create Person",
		  	"method": "POST",
		  	"url": "http://example.com/api/people",
		  	"response": {
  				"message": "test",
				"status": {
					"code": 200,
					"description": "OK"
				}
			}
		}
		""";

	private static void log(String message, Object... arguments) {
		System.out.printf(message, arguments);
		System.out.flush();
	}

	@Test
	void basicJsonPathAndDeepScanAreCorrect() {

		Configuration configuration = Configuration.builder()
			.jsonProvider(new JacksonJsonProvider())
			//.jsonProvider(new GsonJsonProvider())
			.build();

		ParseContext parseContext = JsonPath.using(configuration);

		DocumentContext documentContext = parseContext.parse(JSON);

		String name = documentContext.read("$.name");

		assertThat(name).isEqualTo("Create Person");

		String responseMessage = documentContext.read("$.response.message");

		assertThat(responseMessage).isEqualTo("test");

		// TODO: See https://github.com/json-path/JsonPath?tab=readme-ov-file#what-is-returned-when
		// TODO: See https://rungutan.com/blog/extract-value-json-path-expression/
		//Object statusDescription = documentContext.read("$.response.status.description");
		Object statusDescription = documentContext.read("$..description"); // returns JSONArray or LinkedList with Jackson
		//Object statusDescription = documentContext.read("$..description[0]"); // returns empty array
		//String statusDescription = documentContext.read("$..description[?(@.length-1)]"); // returns empty array
		//String statusDescription = documentContext.read("$.response.status.description", String.class);
		//String statusDescription = documentContext.read("$..description", String.class); // returns null

		//log("Status is [%s] of type [%s]%n", statusDescription, ObjectUtils.getClassName(statusDescription));

		//assertThat(statusDescription).isEqualTo("OK");
		assertThat(resolveSingleValueAsString(statusDescription)).isEqualTo("OK");
		//assertThat(statusDescription.toString()).contains("OK");
	}


	@SuppressWarnings("unchecked")
	private <T> T resolveSingleValue(Object target) {
		return (T) (target instanceof List<?> list ? list.get(0) : target);
	}

	private String resolveSingleValueAsString(Object target) {
		return resolveSingleValue(target);
	}
}
