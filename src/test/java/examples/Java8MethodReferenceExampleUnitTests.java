/*
 * Copyright 2017 Author or Authors.
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
package examples;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Optional;
import java.util.function.Supplier;

import org.cp.elements.io.IOUtils;
import org.cp.elements.lang.ObjectUtils;
import org.cp.elements.lang.StringUtils;
import org.cp.elements.lang.annotation.Immutable;
import org.junit.Test;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * The {@link Java8MethodReferenceExampleUnitTests} class is an example of perhaps one fundamental misconception
 * with Java 8 Lambdas vs. Method References.
 *
 * Below, I have defined a simple {@link Person} class representing a named person.  The {@link Person} class
 * is deliberately not {@link Serializable}.  1 reason for this is that the {@link Person} class could very well
 * have represented an object that should not be serialized, like a network {@link Socket}.
 *
 * I have also defined a {@link PersonWrapper} to "wrap" a {@link Person} and make it {@link Serializable}. 1 way to
 * fundamentally do this is to serialize the {@link Person Person's} constituent parts (i.e. properties),
 * like {@code name}.  When the {@link PersonWrapper} is deserialized, of course, the {@link Person} reference
 * will be {@literal null}.
 *
 * To guard agains {@literal null} when the {@link Person} object referenece is {@literal null} in the wrapper
 * is to use Java 8's new {@link Optional} class.  For example, inside the {@link PersonWrapper} class...
 *
 * <code>
 *     public String getName() {
 *         return Optional.ofNullable(this.name).orElseGet(this.person::getName);
 *     }
 * </code>
 *
 * However, the problem with this use of {@link Optional} is that the Java 8 Method Reference is not directly
 * substitutable as a Java 8 Lambda and therefore results in a {@link NullPointerException} when {@code this.person}
 * is {@literal null}.
 *
 * As such, a developer must take extra care to further protect against a possible {@link NullPointerException}
 * that s/he was probably looking to avoid with the use of the {@link Optional} class (though not necessarily)
 * in the first place...
 *
 * <code>
 *     public String getName() {
 *         return Optional.ofNullable(this.name).orElseGet(() -> this.person.getName());
 *     }
 * </code>
 *
 * Your IDE will most likely inform you that you can simplify the Lambda (i.e. {@code this.person.getName()})
 * into a Method Reference {@code this.person::getName}, but that is simply NOT the case!
 *
 * This example goes to show that no amount of tests can prove a code correct, but only 1 test can prove it wrong!
 *
 * TODO do more (Java) research on Method References vs. Lambdas and look into the exact reason for this.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see java.util.Optional
 * @since 1.0.0
 */
public class Java8MethodReferenceExampleUnitTests {

	@Test
	public void withLambdaIsSafe() {

		PersonWrapper personWrapper = new PersonWrapper().with("Jon Doe");

		assertThat(personWrapper).isNotNull();
		assertThat(personWrapper.getPerson()).isNull();
		assertThat(personWrapper.getSafeName()).isEqualTo("Jon Doe");
	}

	@Test(expected = NullPointerException.class)
	public void withMethodReferenceThrowsNullPointerException() {

		PersonWrapper personWrapper = new PersonWrapper().with("Jon Doe");

		assertThat(personWrapper).isNotNull();
		assertThat(personWrapper.getPerson()).isNull();
		assertThat(personWrapper.getName()).isEqualTo("Jon Doe");

		personWrapper.getUnsafeName();
	}

	@Test(expected = NullPointerException.class)
	public void serializeDeserializeIsCorrect() throws IOException, ClassNotFoundException {

		Person jonDoe = Person.as("Jon Doe");

		PersonWrapper personWrapper = PersonWrapper.from(jonDoe);

		assertThat(personWrapper).isNotNull();
		assertThat(personWrapper.getPerson()).isSameAs(jonDoe);
		assertThat(personWrapper.getName()).isNull();
		assertThat(personWrapper.getSafeName()).isEqualTo("Jon Doe");
		assertThat(personWrapper.getUnsafeName()).isEqualTo("Jon Doe");

		byte[] personWrapperBytes = IOUtils.serialize(personWrapper);

		PersonWrapper deserializedPersonWrapper = IOUtils.deserialize(personWrapperBytes);

		assertThat(deserializedPersonWrapper).isNotNull();
		assertThat(deserializedPersonWrapper).isNotSameAs(personWrapper);
		assertThat(deserializedPersonWrapper.getPerson()).isNull();
		assertThat(deserializedPersonWrapper.getName()).isEqualTo("Jon Doe");
		assertThat(deserializedPersonWrapper.getSafeName()).isEqualTo("Jon Doe");

		// Throws NullPointerException
		deserializedPersonWrapper.getUnsafeName();
	}

	@Getter
	@Immutable
	@EqualsAndHashCode
	@RequiredArgsConstructor(staticName = "as")
	static class Person {

		@NonNull
		private final String name;

		/**
		 * @inheritDoc
		 */
		@Override
		public String toString() {
			return getName();
		}
	}

	static class PersonWrapper implements Serializable {

		private final transient Person person;

		private String name;

		public static PersonWrapper from(Person person) {
			return new PersonWrapper(person);
		}

		private PersonWrapper() {
			this.person = null;
		}

		private PersonWrapper(Person person) {
			this.person = ObjectUtils.requireObject(person, "Person is required");
		}

		Person getPerson() {
			return this.person;
		}

		public String getName() {
			return this.name;
		}

		/**
		 * Using a Java 8 Lambda is safe.
		 *
		 * @see #getUnsafeName()
		 */
		public String getSafeName() {

			return Optional.ofNullable(this.name)
				.filter(StringUtils::hasText)
				.orElseGet(() -> this.person.getName());
		}

		/**
		 * A Java 8 Method Reference will throw a {@link NullPointerException} when {@code this.person}
		 * is {@literal null}.  Therefore, a Java 8 Method Reference is not treated exactly like a Lambda
		 * expression by the Java compiler when passed to a method (e.g. {@link Optional#orElseGet(Supplier)}
		 * that accepts a {@link FunctionalInterface}.
		 *
		 * @see #getSafeName()
		 */
		public String getUnsafeName() {
			return Optional.ofNullable(this.name)
				.filter(StringUtils::hasText)
				.orElseGet(this.person::getName);
		}

		private void writeObject(ObjectOutputStream outputStream) throws IOException {
			outputStream.writeUTF(getUnsafeName());
		}

		private void readObject(ObjectInputStream inputStream) throws IOException {
			this.name = inputStream.readUTF();
		}

		private PersonWrapper with(String name) {
			this.name = name;
			return this;
		}
	}
}
