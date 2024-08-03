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
package org.cp.labs.model;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.cp.elements.lang.CodeBlocks;
import org.cp.elements.security.model.User;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Abstract Data Type (ADT) modeling a {@link User} for testing purposes.
 *
 * @author John Blum
 * @see org.cp.elements.security.model.User
 * @see java.time.Instant
 * @see java.util.UUID
 */
@Getter
@Setter(AccessLevel.PROTECTED)
@JsonIgnoreProperties({ "new", "notNew" })
@RequiredArgsConstructor(staticName = "named")
@SuppressWarnings("unused")
public class TestUser implements User<UUID> {

	public static final String LAST_ACCESS_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS";

	private long lastAccess;

	private final String name;

	private String token = "#{token}";

	@Setter(AccessLevel.PUBLIC)
	private UUID id;

	@JsonProperty
	@JsonFormat(pattern = LAST_ACCESS_PATTERN)
	public ZonedDateTime getLastAccessDateTime() {
		return getLastAccessInstant().atZone(ZoneOffset.systemDefault());
	}

	@JsonIgnore
	public Instant getLastAccessInstant() {
		return CodeBlocks.ifElse(this.lastAccess, t -> t > 0L, Instant::ofEpochMilli, t -> Instant.now());
	}

	@SuppressWarnings("unchecked")
	public TestUser identifiedBy(UUID id) {
		setId(id);
		return this;
	}

	public TestUser lastAccessedNow() {
		return lastAccessed(Instant.now());
	}

	public TestUser lastAccessed(long timestamp) {
		setLastAccess(timestamp);
		return this;
	}

	public TestUser lastAccessed(Instant lastAccess) {
		setLastAccess(lastAccess.toEpochMilli());
		return this;
	}

	public TestUser lastAccessed(ZonedDateTime dateTime) {
		return lastAccessed(dateTime.toInstant());
	}

	public TestUser withToken(String token) {
		setToken(token);
		return this;
	}
}
