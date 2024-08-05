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
package org.cp.labs.model.serialization.json;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import org.cp.elements.security.model.User;
import org.cp.labs.model.TestUser;
import org.springframework.boot.jackson.JsonComponent;

/**
 * {@link JsonDeserializer} for {@link TestUser}.
 *
 * @author John Blum
 * @see org.cp.labs.model.TestUser
 * @see com.fasterxml.jackson.core.JsonParser
 * @see com.fasterxml.jackson.databind.JsonDeserializer
 * @see org.springframework.boot.jackson.JsonComponent
 */
@JsonComponent
@SuppressWarnings("unused")
public class TestUserDeserializer extends JsonDeserializer<User> {

	@Override
	public User<UUID> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException {

		JsonNode jsonTree = jsonParser.getCodec().readTree(jsonParser);

		return TestUser.named(jsonTree.get("name").asText())
			.lastAccessed(resolveLastAccessed(jsonTree))
			.withRole(TestUser.Role.valueOf(jsonTree.get("role").asText()))
			.withToken(jsonTree.get("token").asText());
	}

	private static long resolveLastAccessed(JsonNode jsonTree) {

		return jsonTree.has("lastAccess")
			? jsonTree.get("lastAccess").asLong()
			: Instant.EPOCH.toEpochMilli();
	}
}
