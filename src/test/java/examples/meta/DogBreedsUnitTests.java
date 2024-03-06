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
package examples.meta;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Unit Tests for {@literal Meta's Dog Breeds Problem}
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class DogBreedsUnitTests {

	@Test
	void sampleTestCaseOne() {

		Dog scooby = Dog.named("Scooby Doo").of(Breed.greatDane());
		Dog snoopy = Dog.named("Snoopy").of(Breed.beagle());
		Dog childOne = scooby.breed("Rover", snoopy);

		assertThat(childOne).isNotNull();
		assertThat(childOne.is(Breed.greatDane(bigDecimal(50)), Breed.beagle(bigDecimal(50)))).isTrue();

		Dog whiteFang = Dog.named("White Fang").of(Breed.husky());
		Dog childTwo = whiteFang.breed("Ruff", childOne);

		assertThat(childTwo).isNotNull();
		assertThat(childTwo.is(Breed.husky(bigDecimal(50)), Breed.greatDane(bigDecimal(25)), Breed.beagle(bigDecimal(25)))).isTrue();
	}

	@Test
	void sampleTestCaseTwo() {

		Dog brian = Dog.named("Brian").of(Breed.labradorRetriever(bigDecimal(50)), Breed.beagle(bigDecimal(50)));
		Dog maha = Dog.named("Maha").of(Breed.labradorRetriever(bigDecimal(50)), Breed.spaniel(bigDecimal(50)));
		Dog mutt = brian.breed("Mutt", maha);

		assertThat(mutt).isNotNull();
		assertThat(mutt.is(Breed.labradorRetriever(bigDecimal(50)), Breed.beagle(bigDecimal(25)), Breed.spaniel(bigDecimal(25)))).isTrue();

		Dog benji = Dog.named("Benji").of(Breed.spaniel(bigDecimal(100)));
		Dog scruffy = mutt.breed("Scruffy", benji);

		assertThat(scruffy).isNotNull();
		assertThat(scruffy.is(Breed.labradorRetriever(bigDecimal(25)), Breed.beagle(bigDecimal(12)), Breed.spaniel(bigDecimal(62)))).isTrue();
	}

	/**
	 * Abstract Data Type (ADT) modeling an object that can be named.
	 * @param <T> {@link Class type} of {@link Object} representing the name.
	 */
	interface Nameable<T> {
		T getName();
	}

	/**
	 * Abstract Data Type (ADT) modeling a {@literal Dog}.
	 *
	 * @see Iterable
	 * @see Breed
	 */
	interface Dog extends Iterable<Breed>, Nameable<String> {

		static Dog.Builder named(String name) {
			return new Builder(Assert.hasText(name, "Dog must have a name"));
		}

		default Optional<Breed> getBreed(Breed.Type breedType) {
			return Breeds.of(this).get(breedType);
		}

		default Breed resolveBreed(Breed.Type breedType) {
			return getBreed(breedType)
				.orElseThrow(() -> new IllegalStateException("Dog [%s] is not a [%s]"
					.formatted(this, Utils.formatType(breedType))));
		}

		Dog breed(String name, Dog mate);

		default boolean is(Breed... breeds) {
			return is(Breeds.of(breeds));
		}

		default boolean is(Iterable<Breed> breeds) {

			try {
				Breeds actualBreeds = Breeds.of(this);
				Breeds expectedBreeds = Breeds.of(breeds);

				Supplier<String> message = () -> "Expected [%d] breed(s); but was [%d]"
					.formatted(actualBreeds.size(), expectedBreeds.size());

				Assert.isEqual(actualBreeds.size(), expectedBreeds.size(), message);

				expectedBreeds.forEach(expected -> {
					Breed actual = resolveBreed(expected.getType());
					Assert.state(actual.getPercent().toBigInteger().equals(expected.getPercent().toBigInteger()),
						"Dog [%s] is not a [%s]: %s".formatted(this, Utils.formatType(expected), expected));
				});

				return true;
			}
			catch (IllegalStateException e) {
				Utils.logError(e);
				return false;
			}
		}

		@Getter(AccessLevel.PROTECTED)
		@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
		class Builder {

			private final String name;

			Dog of(Breed.Type breedType) {
				return of(Breed.ONE_HUNDRED_PERCENT, breedType);
			}

			Dog of(BigDecimal percent, Breed.Type breedType) {
				return of(Breed.of(percent, breedType));
			}

			Dog of(Breed... breeds) {
				return new DefaultDog(getName(), Breeds.of(breeds));
			}

			Dog of(Iterable<Breed> breeds) {
				return new DefaultDog(getName(), Breeds.of(breeds));
			}
		}
	}

	/**
	 * Default implementation of {@literal Dog}.
	 *
	 * @see Dog
	 */
	@Getter
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	static class DefaultDog implements Dog {

		private final String name;
		private final Breeds breeds;

		@Override
		@SuppressWarnings("all")
		public Iterator<Breed> iterator() {
			return getBreeds().stream().sorted().toList().iterator();
		}

		@Override
		public Dog breed(String name, Dog mate) {
			Breeds mergedBreeds = getBreeds().merge(Breeds.of(mate));
			return Dog.named(name).of(mergedBreeds);
		}

		@Override
		public String toString() {

			StringBuilder string = new StringBuilder("[%s] is".formatted(getName()));

			Utils.stream(this).forEach(breed -> string.append(" %s".formatted(breed)));

			return string.toString().trim();
		}
	}

	/**
	 * Abstract Data Type (ADT) used to conveniently model a {@literal unique}
	 * {@link Iterable collection} of {@literal Breed Breeds}.
	 *
	 * @see Iterable
	 * @see Breed
	 */
	interface Breeds extends Iterable<Breed> {

		static Breeds empty() {
			return Collections::emptyIterator;
		}

		static Breeds of(Breed... breeds) {
			Assert.notEmpty(breeds, "At least one breed is required");
			return Utils.asSet(breeds)::iterator;
		}

		static Breeds of(Iterable<Breed> breeds) {
			Assert.notEmpty(breeds, "At least one breed is required");
			return Utils.asSet(breeds)::iterator;
		}

		default boolean isPresent(Breed.Type breedType) {
			return get(breedType).isPresent();
		}

		default boolean isPresent(Breed breed) {
			return breed != null && isPresent(breed.getType());
		}

		default boolean isNotPresent(Breed breed) {
			return !isPresent(breed);
		}

		default Optional<Breed> get(Breed.Type breedType) {
			return breedType != null
				? stream().filter(it -> it.getType().equals(breedType)).findFirst()
				: Optional.empty();
		}

		default Breed getResolved(Breed.Type breedType) {
			return get(breedType).orElseThrow(() -> new IllegalStateException("Breed of type [%s] not found"
				.formatted(Utils.formatType(breedType))));
		}

		default Breeds difference(Breeds breeds) {
			return difference(breeds, Function.identity());
		}

		default Breeds difference(Breeds breeds, Function<Breed, Breed> transform) {

			Iterable<Breed> result = stream()
				.filter(breeds::isNotPresent)
				.map(transform)
				.toList();

			return Utils.isNotEmpty(result) ? Breeds.of(result) : Breeds.empty();
		}

		default Breeds intersection(Breeds breeds) {
			return intersection(breeds, (breedOne, breedTwo) -> breedOne);
		}

		default Breeds intersection(Breeds breeds, BiFunction<Breed, Breed, Breed> transform) {

			Iterable<Breed> result = stream()
				.filter(breeds::isPresent)
				.map(it -> transform.apply(it, breeds.getResolved(it.getType())))
				.toList();

			return Utils.isNotEmpty(result) ? Breeds.of(result) : Breeds.empty();
		}

		default Breeds merge(Breeds breeds) {
			return merge(breeds, Breed::split, Breed::combine);
		}

		default Breeds merge(Breeds breeds, Function<Breed, Breed> singleBreed,
				BiFunction<Breed, Breed, Breed> matchingBreeds) {

			Set<Breed> mergedBreeds = new HashSet<>(this.difference(breeds, singleBreed).toSet());

			mergedBreeds.addAll(breeds.difference(this, singleBreed).toSet());
			mergedBreeds.addAll(this.intersection(breeds, matchingBreeds).toSet());

			return Breeds.of(mergedBreeds);
		}

		default Breeds union(Breeds breeds) {
			Set<Breed> union = new HashSet<>(this.toSet());
			union.addAll(breeds.toSet());
			return Breeds.of(union);
		}

		default int size() {
			return Long.valueOf(stream().count()).intValue();
		}

		default Stream<Breed> stream() {
			return Utils.stream(this);
		}

		default Set<Breed> toSet() {
			return stream().collect(Collectors.toSet());
		}
	}

	/**
	 * Abstract Data Type (ADT) modeling an {@link Breed.Type animal's breed}
	 * along with its {@link BigDecimal percentage} of that {@link Breed.Type breed}.
	 *
	 * @see Breed.Type
	 * @see BigDecimal
	 */
	@Getter
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	static class Breed implements Comparable<Breed> {

		static final BigDecimal ONE_HUNDRED_PERCENT = BigDecimal.valueOf(100.0d);
		static final BigDecimal TWO = BigDecimal.valueOf(2.0d);

		static Breed beagle() {
			return beagle(ONE_HUNDRED_PERCENT);
		}

		static Breed beagle(BigDecimal percent) {
			return of(percent, Type.BEAGLE);
		}

		static Breed corgi() {
			return corgi(ONE_HUNDRED_PERCENT);
		}

		static Breed corgi(BigDecimal percent) {
			return of(percent, Type.CORGI);
		}

		static Breed germanShepherd() {
			return germanShepherd(ONE_HUNDRED_PERCENT);
		}

		static Breed germanShepherd(BigDecimal percent) {
			return of(percent, Type.GERMAN_SHEPHERD);
		}

		static Breed greatDane() {
			return greatDane(ONE_HUNDRED_PERCENT);
		}

		static Breed greatDane(BigDecimal percent) {
			return of(percent, Type.GREAT_DANE);
		}

		static Breed husky() {
			return husky(ONE_HUNDRED_PERCENT);
		}

		static Breed husky(BigDecimal percent) {
			return of(percent, Type.HUSKY);
		}

		static Breed labradorRetriever() {
			return labradorRetriever(ONE_HUNDRED_PERCENT);
		}

		static Breed labradorRetriever(BigDecimal percent) {
			return of(percent, Type.LABRADOR_RETRIEVER);
		}

		static Breed pitBull() {
			return pitBull(ONE_HUNDRED_PERCENT);
		}

		static Breed pitBull(BigDecimal percent) {
			return of(percent, Type.PIT_BULL);
		}

		static Breed spaniel() {
			return spaniel(ONE_HUNDRED_PERCENT);
		}

		static Breed spaniel(BigDecimal percent) {
			return of(percent, Type.SPANIEL);
		}

		static Breed of(BigDecimal percent, Breed.Type breedType) {
			return new Breed(Assert.percent(percent), Assert.type(breedType));
		}

		static Breed oneHundredPercentOf(Breed.Type breedType) {
			return of(ONE_HUNDRED_PERCENT, breedType);
		}

		private final BigDecimal percent;

		private final Type type;

		/**
		 * Combines this {@link Breed} with the given {@link Breed}.
		 * @param breed {@link Breed} to combine with this {@link Breed}; must not be {@link null}.
		 * @return a new, combined {@link Breed}.
		 * @throws AssertionError if the given {@link Breed} is {@literal null}
		 * or the given {@link Breed} does not match this {@link Breed} by {@link #getType() type}.
		 * @see #getPercent()
		 * @see #getType()
		 */
		public Breed combine(Breed breed) {

			Assert.isType(breed, getType());

			BigDecimal newPercent =
				Utils.minimumOne(getPercent().add(breed.getPercent()).divide(TWO, RoundingMode.DOWN));

			return of(newPercent, getType());
		}

		/**
		 * Splits this {@link #getPercent() breed's percentage} in half
		 * and returns a new {@link Breed} from the result.
		 * @return a new {@link Breed} with this {@link Breed Breed's} {@link #getPercent() percent}
		 * divided by {@literal 2}.
		 * @see #getPercent()
		 */
		public Breed split() {
			BigDecimal newPercent = getPercent().divide(TWO, RoundingMode.DOWN);
			return of(newPercent, getType());
		}

		@Override
		public int compareTo(Breed that) {
			// order by largest percentage to smallest percentage
			return that.getPercent().compareTo(this.getPercent());
		}

		@Override
		public boolean equals(Object obj) {

			if (this == obj) {
				return true;
			}

			if (!(obj instanceof Breed that)) {
				return false;
			}

			return this.getType().equals(that.getType());
		}

		@Override
		public int hashCode() {
			return Objects.hash(getType());
		}

		@Override
		public String toString() {
			return "%s %s".formatted(Utils.formatPercent(getPercent()), Utils.formatType(getType()));
		}

		/**
		 * {@link Enum Enumeration} of {@literal dog breeds}.
		 *
		 * @see Enum
		 */
		@SuppressWarnings("unused")
		enum Type {
			BEAGLE, CORGI, GERMAN_SHEPHERD, GREAT_DANE, HUSKY, LABRADOR_RETRIEVER, PIT_BULL, SPANIEL
		}
	}

	// Requires JVM Assertions to be enabled (e.g. > java -ea ...).
	static class Assert {

		static String hasText(String value, String message) {
			assert Utils.hasText(value) : message;
			return value;
		}

		static void isEqual(Object valueOne, Object valueTwo, Supplier<String> message) {
			assert Objects.equals(valueOne, valueTwo) : message;
		}

		static Breed isType(Breed breed, Breed.Type breedType) {

			Assert.notNull(breed, "Breed is required");
			Assert.notNull(breedType, "Type of breed to match on is required");

			assert breed.getType().equals(breedType) : "Breed [%s] must match type [%s]"
				.formatted(Utils.formatType(breed), Utils.formatType(breedType));

			return breed;
		}

		static <T> T[] notEmpty(T[] array, String message) {
			assert Utils.isNotEmpty(array) : message;
			return array;
		}

		static <T> Iterable<T> notEmpty(Iterable<T> iterable, String message) {
			assert Utils.isNotEmpty(iterable) : message;
			return iterable;
		}

		static <T> T notNull(T target, String message) {
			assert target != null : message;
			return target;
		}

		static BigDecimal percent(BigDecimal percent) {

			Assert.notNull(percent, "Percent of breed is required");

			assert percent.compareTo(BigDecimal.ZERO) > 0
				: "Percent [%s] must be greater than 0".formatted(percent);

			assert percent.compareTo(Breed.ONE_HUNDRED_PERCENT) <= 0
				: "Percent [%s] must be less than equal to 100".formatted(percent);

			return percent;
		}

		static void state(boolean state, String message) {
			if (!state) {
				throw new IllegalStateException(message);
			}
		}

		static Breed.Type type(Breed.Type breedType) {
			return Assert.notNull(breedType, "Type of breed is required");
		}
	}

	private static BigDecimal bigDecimal(double value) {
		return BigDecimal.valueOf(value);
	}

	static class Utils {

		static final NumberFormat PERCENT_FORMAT = NumberFormat.getPercentInstance();

		static {
			PERCENT_FORMAT.setMaximumFractionDigits(0);
		}

		@SuppressWarnings("unchecked")
		static <T> Set<T> asSet(T... array) {
			return new HashSet<>(Arrays.asList(array));
		}

		static <T> Set<T> asSet(Iterable<T> iterable) {
			return stream(iterable).collect(Collectors.toSet());
		}

		static String formatPercent(BigDecimal value) {

			BigDecimal resolveValue = value.compareTo(BigDecimal.ONE) > 0
				? value.divide(bigDecimal(100), RoundingMode.DOWN)
				: value;

			return PERCENT_FORMAT.format(resolveValue);
		}

		static String formatType(Breed breed) {
			return formatType(breed.getType());
		}

		static String formatType(Breed.Type breedType) {
			return breedType != null ? breedType.name() : null;
		}

		static boolean hasText(String value) {
			return value != null && !value.isBlank();
		}

		static boolean isEmpty(Object[] array) {
			return array == null || array.length == 0;
		}

		static boolean isNotEmpty(Object[] array) {
			return !isEmpty(array);
		}

		static boolean isEmpty(Iterable<?> iterable) {
			return iterable == null || stream(iterable).findAny().isEmpty();
		}

		static boolean isNotEmpty(Iterable<?> iterable) {
			return !isEmpty(iterable);
		}

		static void logError(String message) {
			System.err.println(message);
			System.err.flush();
		}

		static void logError(Throwable error) {
			logError(error.getMessage());
		}

		static BigDecimal minimumOne(BigDecimal value) {
			return value == null || BigDecimal.ONE.compareTo(value) > 0 ? BigDecimal.ONE : value;
		}

		static <T> Stream<T> stream(Iterable<T> iterable) {
			assert iterable != null : "Iterable is required";
			return StreamSupport.stream(iterable.spliterator(), false);
		}
	}

}
