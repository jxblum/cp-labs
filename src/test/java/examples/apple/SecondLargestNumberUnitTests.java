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

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Unit Tests for {@literal Apple's Second Largest Number Problem}
 *
 * @author John Blum
 * @since 1.0.0
 */
public class SecondLargestNumberUnitTests {

	private static final int UPPER_BOUND = 100;

	private static final Random randomNumberGenerator = new Random(System.currentTimeMillis());

	private final Solution solution = new SolutionOne();

	private int[] newArray(int size) {
		return IntStream.range(0, size).map(index -> randomNumberGenerator.nextInt(UPPER_BOUND)).toArray();
	}

	@Test
	void testCaseOne() {

		int[] array = { 1, 5, 3, 8, 4, 10, 9 };

		Pair pair = this.solution.findLargestAndSecondLargestNumber(array);

		assertThat(pair).isNotNull();
		assertThat(pair.getLargestNumber()).isEqualTo(10);
		assertThat(pair.getSecondLargestNumber()).isEqualTo(9);
	}

	@Test
	void testCaseTwo() {

		int[] array = { 1, 5, 3, 2, 9, 1, 6, 4, 10, 7 };

		Pair pair = this.solution.findLargestAndSecondLargestNumber(array);

		assertThat(pair).isNotNull();
		assertThat(pair.getLargestNumber()).isEqualTo(10);
		assertThat(pair.getSecondLargestNumber()).isEqualTo(9);
	}

	@Test
	void testCaseThree() {

		int[] array = { 12, 5, 3, 2, 9, 1, 6, 4, 10, 7 };

		Pair pair = this.solution.findLargestAndSecondLargestNumber(array);

		assertThat(pair).isNotNull();
		assertThat(pair.getLargestNumber()).isEqualTo(12);
		assertThat(pair.getSecondLargestNumber()).isEqualTo(10);
	}

	@Test
	void testCaseFour() {

		int[] array = { 12, 5, 16, 5, 3, 2, 9, 1, 6, 4, 10, 7 };

		Pair pair = this.solution.findLargestAndSecondLargestNumber(array);

		assertThat(pair).isNotNull();
		assertThat(pair.getLargestNumber()).isEqualTo(16);
		assertThat(pair.getSecondLargestNumber()).isEqualTo(12);
	}

	@Test
	void testCaseFive() {

		int[] array = { 12, 5, 16, 5, 3, 2, 9, 1, 6, 4, 10, 7, 16 };

		Pair pair = this.solution.findLargestAndSecondLargestNumber(array);

		assertThat(pair).isNotNull();
		assertThat(pair.getLargestNumber()).isEqualTo(16);
		assertThat(pair.getSecondLargestNumber()).isEqualTo(12);
	}

	@Test
	void testCaseSix() {

		int[] array = { 12, 5, 16, 5, 3, 2, 9, 1, 6, 4, 10, 7, 17, 15 };

		Pair pair = this.solution.findLargestAndSecondLargestNumber(array);

		assertThat(pair).isNotNull();
		assertThat(pair.getLargestNumber()).isEqualTo(17);
		assertThat(pair.getSecondLargestNumber()).isEqualTo(16);
	}

	@Test
	void testCaseSeven() {

		int[] array = newArray(100);

		Pair pair = this.solution.findLargestAndSecondLargestNumber(array);
		Pair answer = this.solution.answer(array);

		assertThat(pair).isEqualTo(answer);
	}

	@Test
	void testCaseEight() {

		int[] array = newArray(1_000);

		Pair pair = this.solution.findLargestAndSecondLargestNumber(array);
		Pair answer = this.solution.answer(array);

		assertThat(pair).isEqualTo(answer);
	}

	@Test
	void testCaseNine() {

		int[] array = newArray(10_000);

		Pair pair = this.solution.findLargestAndSecondLargestNumber(array);
		Pair answer = this.solution.answer(array);

		assertThat(pair).isEqualTo(answer);
	}

	@FunctionalInterface
	interface Solution {

		default Pair answer(int[] array) {

			Set<Integer> set = IntStream.of(array).boxed().collect(Collectors.toSet());

			assertThat(set).hasSizeLessThan(array.length); // implies duplicates

			List<Integer> list = set.stream().sorted().toList();

			int listSize = list.size();
			int lastIndex = listSize - 1;

			return Pair.of(list.get(lastIndex), list.get(lastIndex - 1));
		}

		Pair findLargestAndSecondLargestNumber(int[] array);

	}

	static class SolutionOne implements Solution {

		@Override
		public Pair findLargestAndSecondLargestNumber(int[] array) {

			int largestNumber = Integer.MIN_VALUE;
			int secondLargestNumber = Integer.MIN_VALUE;

			for (int number : array) {
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

	@Getter
	@ToString
	@EqualsAndHashCode
	@RequiredArgsConstructor(staticName = "of")
	static class Pair {

		private final int largestNumber;
		private final int secondLargestNumber;

	}
}
