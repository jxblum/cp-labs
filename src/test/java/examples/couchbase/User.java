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
package examples.couchbase;

import java.util.UUID;

import org.cp.elements.lang.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.Field;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author John Blum
 */
@Getter
@Document
@ToString(of = "name")
@EqualsAndHashCode(of = { "name" })
@SuppressWarnings("unused")
public class User {

	public static User named(String name) {
		User user = new User();
		user.name = StringUtils.requireText(name, "Name [%s] is required");
		return user;
	}

	@Id
	@Setter
	private UUID id;

	@Field
	@Setter
	private Boolean active = true;

	private String name;

}
