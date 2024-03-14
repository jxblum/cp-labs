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
package examples.apple;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.cp.elements.lang.StringUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Unit Tests for {@literal Apple's Second Largest Number Problem}.
 * <p>
 * You must adjust the amount of JVM Heap space needed to work with really large arrays, especially massively large
 * two-dimensional arrays.
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisabledIfSystemProperty(named = "cp.jvm.runtime.env", matches = "MAVEN")
public class SecondLargestNumberUnitTests {

	private static final boolean LOG_INFO = true;

	private static final int UPPER_BOUND = 1_000_000;

	private static final ThreadLocalRandom random = ThreadLocalRandom.current();

	//private final Answer answer = new AnswerOne();
	private final Answer answer = new AnswerTwo();

	private final Solution solution = new SolutionOne();
	//private final Solution solution = new MultiThreadedSolution();

	// TODO: Replace with SLF4J
	private static void log(String message) {
		log(StringUtils.EMPTY_STRING, message);
	}

	private static void log(String label, String message) {
		if (LOG_INFO) {
			String resolvedMessage = StringUtils.hasText(label) ? "%s: %s".formatted(label, message) : message;
			System.out.println(resolvedMessage);
			System.out.flush();
		}
	}

	private static int[] newArray(int size) {
		//return IntStream.range(0, size).map(index -> randomNumberGenerator.nextInt(UPPER_BOUND)).toArray();
		int[] array = new int[size];
		for (int index = 0; index < size; index++) {
			array[index] = random.nextInt(UPPER_BOUND);
		}
		return array;
	}

	private static int[][] newDoubleArray(int rows, int cols) {
		return newDoubleArrayOne(rows, cols);
		//return newDoubleArrayTwo(rows, cols);
	}

	private static int[][] newDoubleArrayOne(int rows, int cols) {
		int[][] doubleArray = new int[rows][cols];
		for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
			for (int colIndex = 0; colIndex < cols; colIndex++) {
				doubleArray[rowIndex][colIndex] = random.nextInt(UPPER_BOUND);
			}
		}
		return doubleArray;
	}

	private static int[][] newDoubleArrayTwo(int rows, int cols) {
		int[][] doubleArray = new int[rows][cols];
		for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
			doubleArray[rowIndex] = newArray(cols);
		}
		return doubleArray;
	}

	// TODO: Replace with JHM
	private static <T> T time(String label, Supplier<T> function) {

		long t0 = System.currentTimeMillis();

		T result = function.get();

		Duration duration = Duration.ofMillis(System.currentTimeMillis() - t0);

		log(label, "Duration of [%s] milliseconds; [%s] seconds".formatted(duration.toMillis(), duration.toSeconds()));

		return result;
	}

	@Test
	@Order(1)
	void testCaseOne() {

		int[] array = { 1, 5, 3, 8, 4, 10, 9 };

		Pair pair = this.solution.findLargestAndSecondLargestNumber(array);

		assertThat(pair).isNotNull();
		assertThat(pair.getLargestNumber()).isEqualTo(10);
		assertThat(pair.getSecondLargestNumber()).isEqualTo(9);
	}

	@Test
	@Order(2)
	void testCaseTwo() {

		int[] array = { 1, 5, 3, 2, 9, 1, 6, 4, 10, 7 };

		Pair pair = this.solution.findLargestAndSecondLargestNumber(array);

		assertThat(pair).isNotNull();
		assertThat(pair.getLargestNumber()).isEqualTo(10);
		assertThat(pair.getSecondLargestNumber()).isEqualTo(9);
	}

	@Test
	@Order(3)
	void testCaseThree() {

		int[] array = { 12, 5, 3, 2, 9, 1, 6, 4, 10, 7 };

		Pair pair = this.solution.findLargestAndSecondLargestNumber(array);

		assertThat(pair).isNotNull();
		assertThat(pair.getLargestNumber()).isEqualTo(12);
		assertThat(pair.getSecondLargestNumber()).isEqualTo(10);
	}

	@Test
	@Order(4)
	void testCaseFour() {

		int[] array = { 12, 5, 16, 5, 3, 2, 9, 1, 6, 4, 10, 7 };

		Pair pair = this.solution.findLargestAndSecondLargestNumber(array);

		assertThat(pair).isNotNull();
		assertThat(pair.getLargestNumber()).isEqualTo(16);
		assertThat(pair.getSecondLargestNumber()).isEqualTo(12);
	}

	@Test
	@Order(5)
	void testCaseFive() {

		int[] array = { 12, 5, 16, 5, 3, 2, 9, 1, 6, 4, 10, 7, 16 };

		Pair pair = this.solution.findLargestAndSecondLargestNumber(array);

		assertThat(pair).isNotNull();
		assertThat(pair.getLargestNumber()).isEqualTo(16);
		assertThat(pair.getSecondLargestNumber()).isEqualTo(12);
	}

	@Test
	@Order(6)
	void testCaseSix() {

		int[] array = { 12, 5, 16, 5, 3, 2, 9, 1, 6, 4, 10, 7, 17, 15 };

		Pair pair = this.solution.findLargestAndSecondLargestNumber(array);

		assertThat(pair).isNotNull();
		assertThat(pair.getLargestNumber()).isEqualTo(17);
		assertThat(pair.getSecondLargestNumber()).isEqualTo(16);
	}

	@Test
	@Order(7)
	void testCaseSeven() {

		int[] array = newArray(100);

		Pair pair = this.solution.findLargestAndSecondLargestNumber(array);
		Pair answer = this.answer.answer(array);

		assertThat(pair).isEqualTo(answer);
	}

	@Test
	@Order(8)
	void testCaseEight() {

		int[] array = newArray(1_000);

		Pair pair = this.solution.findLargestAndSecondLargestNumber(array);
		Pair answer = this.answer.answer(array);

		assertThat(pair).isEqualTo(answer);
	}

	@Test
	@Order(9)
	void testCaseNine() {

		int[] array = newArray(10_000);

		Pair pair = this.solution.findLargestAndSecondLargestNumber(array);
		Pair answer = this.answer.answer(array);

		assertThat(pair).isEqualTo(answer);
	}

	// For this test case, using 1 billion for array size on my computer,
	// I needed my JVM Heap max size set to -Xmx16384
	@Test
	@Order(10)
	void testCaseTen() {

		int[] array = time("NEW ARRAY", () -> newArray(1_000_000_000));

		Pair pair = time("RESULT", () -> this.solution.findLargestAndSecondLargestNumber(array));
		Pair answer = time("ANSWER", () -> this.answer.answer(array));

		log("Pair of largest and second largest number [%s]".formatted(pair));
		assertThat(pair).isEqualTo(answer);
	}

	// For this test case, using 1 billion for array size on my computer,
	// I needed to set the JVM Heap max size to -Xmx32768m
	// NOTE: When we start approaching 1 billion element array sizes, I believe the JVM starts thrashing
	// since performance drops through the floor on my computer; investigate later.
	// UPDATE: I think it has to do with how much JVM Heap space is left. Once the data starts approaching
	// max Heap size, the perf significantly (and noticeably) drops; more testing, analysis and measurements
	// are needed.
	// Using JMH would be beneficial so that the JVM does not have a change to warm up between test cases.
	@Test
	@Order(11)
	void testCaseEleven() {

		//int[][] array = time("NEW ARRAY", () -> newDoubleArray(5, 1_000_000_000));
		//int[][] array = time("NEW ARRAY", () -> newDoubleArray(10, 500_000_000));
		int[][] array = time("NEW ARRAY", () -> newDoubleArray(10, 100_000_000));

		Pair pair = time("RESULT", () -> this.solution.findLargestAndSecondLargestNumber(array));
		Pair answer = time("ANSWER", () -> this.answer.answer(array));

		log("Pair of largest and second largest number [%s]".formatted(pair));
		assertThat(pair).isEqualTo(answer);
	}

	@FunctionalInterface
	interface Answer {

		Pair answer(int[] array);

		default Pair answer(int[][] doubleArray) {

			Pair pair = Pair.min();

			for (int[] array : doubleArray) {
				pair.merge(answer(array));
			}

			return pair;
		}
	}

	// Must assume answer is correct for these simple test cases and exercises;
	// will assume we were given this answer to verify our computation.
	// Of course if they match, it is decent evidence, but certainly not conclusive.
	// Use Set with sorted List to find largest and second-largest numbers.
	// O(2 * N log N) assuming sorted() is Quick/Heap sort in Java (which IIRC is... verify, John!)
	// Additionally, the space requirement for Object allocations (Boxed (Big) Integers) is significantly more
	// This is quite frankly a BIG O of disaster; LOL!
	static class AnswerOne implements Answer {

		// This performs extremely poorly! Don't do this! Just rubbish!
		public Pair answer(int[] array) {

			Set<Integer> set = IntStream.of(array).boxed().collect(Collectors.toSet());

			assertThat(set).hasSizeLessThanOrEqualTo(array.length); // implies possible duplicates

			List<Integer> list = set.stream().sorted().toList();

			int listSize = list.size();
			int lastIndex = listSize - 1;

			return Pair.of(list.get(lastIndex), list.get(lastIndex - 1));
		}
	}

	// Must assume answer is correct for these simple test cases and exercises;
	// will assume we were given this answer to verify our computation.
	// Of course if they match, it is decent evidence, but certainly not conclusive.
	// Must assume answer is correct for these simple test cases and exercises.
	// Simply loop through the array twice first finding the largest number then finding the second-largest number.
	// This could also be a solution, but is not nearly as optimal as SolutionOne.
	// O(2 * N) == O(N)
	static class AnswerTwo implements Answer {

		@Override
		public Pair answer(int[] array) {

			int largestNumber = Integer.MIN_VALUE;

			for (int number : array) {
				//largestNumber = Math.max(number, largestNumber);
				if (number > largestNumber) {
					largestNumber = number;
				}
			}

			int secondLargestNumber = Integer.MIN_VALUE;

			for (int number : array) {
				if (number > secondLargestNumber) {
					if (number < largestNumber) {
						secondLargestNumber = number;
					}
				}
			}

			return Pair.of(largestNumber, secondLargestNumber);
		}
	}

	@FunctionalInterface
	interface Solution {


		// Two-dimensional array representing a massively large array,
		// well past the upper bound of an int (Integer.MAX_VALUE, 2+ billion signed elements);
		default Pair findLargestAndSecondLargestNumber(int[][] doubleArray) {

			Pair pair = Pair.min();

			for (int[] array : doubleArray) {
				pair.merge(findLargestAndSecondLargestNumber(array));
			}

			return pair;
		}

		Pair findLargestAndSecondLargestNumber(int[] array);

	}

	// O(N) minimal time & space complexity
	static class SolutionOne implements Solution {

		@Override
		public Pair findLargestAndSecondLargestNumber(int[] array) {
			return findLargestAndSecondLargestNumber(array, 0, array.length);
		}

		protected Pair findLargestAndSecondLargestNumber(int[] array, int startIndex, int endIndexExclusive) {

			int largestNumber = Integer.MIN_VALUE;
			int secondLargestNumber = Integer.MIN_VALUE;

			for (int index = startIndex; index < endIndexExclusive; index++) {
				int number = array[index];
				if (number > secondLargestNumber) {
					if (number > largestNumber) {
						secondLargestNumber = largestNumber;
						largestNumber = number;
					}
					else if (largestNumber > number) {
						secondLargestNumber = number;
					}
				}
			}

			return Pair.of(largestNumber, secondLargestNumber);
		}
	}

	// MultiThreaded Solution with the array sizes that I am working with are meh!
	// The sequential solution is much more efficient in both time and space complexity.
	// It is also possible the array elements are non-contiguous in memory, thereby affecting performance.
	// I would need to test more, but I think the array sizes would need to be ridiculously large
	// for the multi-Threaded solution over the single-Threaded, sequential solution to start showing benefits,
	// For example:
	// 1 billion rows & 1 billion columns
	// int[1_000_000_000][1_000_000_000]
	@Getter
	static class MultiThreadedSolution extends SolutionOne {

		private static final int THRESHOLD = 50_000_000;

		private final int availableProcessors;

		private final ExecutorService executorService;

		public MultiThreadedSolution() {
			OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
			this.availableProcessors = osBean.getAvailableProcessors();
			this.executorService = Executors.newFixedThreadPool(this.availableProcessors);
		}

		@Override
		public Pair findLargestAndSecondLargestNumber(int[] array) {

			if (array.length > THRESHOLD) {
				log("USING CONCURRENT EXECUTION");

				int chunkSize = array.length / getAvailableProcessors();

				Set<Future<Pair>> futurePairs = new HashSet<>();

				for (int startIndex = 0; startIndex < array.length; startIndex += chunkSize) {
					final int finalStartIndex = startIndex;
					int endIndex = Math.min(startIndex + chunkSize, array.length);
					futurePairs.add(getExecutorService().submit(() ->
						findLargestAndSecondLargestNumber(array, finalStartIndex, endIndex)));
				}

				Pair result = Pair.min();

				try {
					for (Future<Pair> futurePair : futurePairs) {
						Pair futureResult = futurePair.get();
						//log("Future Pair [%s]%n".formatted(futureResult));
						result = result.merge(futureResult);
					}
					return result;
				}
				catch (ExecutionException e) {
					throw new RuntimeException("Failed to discover largest and second largest numbers in array", e);
				}
				catch (InterruptedException ignore) {
					Thread.currentThread().interrupt();
					throw new RuntimeException("Thread was interrupted while finding largest and second largest numbers in array");
				}
			}
			else {
				log("USING SEQUENTIAL EXECUTION");
				return super.findLargestAndSecondLargestNumber(array);
			}
		}
	}

	@Getter
	@ToString
	@EqualsAndHashCode
	@RequiredArgsConstructor(staticName = "of")
	static class Pair {

		static Pair min() {
			return Pair.of(Integer.MIN_VALUE, Integer.MIN_VALUE);
		}

		private final int largestNumber;
		private final int secondLargestNumber;

		public Pair merge(Pair that) {
			int largestNumber = Math.max(this.largestNumber, that.largestNumber);
			int secondLargestNumber = Math.max(this.secondLargestNumber, that.secondLargestNumber);
			return Pair.of(largestNumber, secondLargestNumber);
		}
	}
}
