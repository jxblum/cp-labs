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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Unit Tests for {@literal Meta's Uniform Integers Problem}.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @since 1.0.0
 */
@Getter(AccessLevel.PROTECTED)
public class UniformIntegersUnitTests {

	private static final boolean DEBUG = false;

	private final Solution solution = new SolutionTwo();

	@BeforeAll
	static void setup() {
		if (DEBUG) {
			print(Math.log10(Long.valueOf(0L).doubleValue()));
			print(Math.log10(Long.valueOf(1L).doubleValue()));
			print(Math.log10(Long.valueOf(2L).doubleValue()));
			print(Math.log10(Long.valueOf(9L).doubleValue()));
			print(Math.log10(Long.valueOf(10L).doubleValue()));
			print(Math.log10(Long.valueOf(100L).doubleValue()));
			print(Math.floor(Math.log10(Long.valueOf(2000L).doubleValue())));
		}
	}

	private static void print(Number number) {
		if (DEBUG) {
			System.out.println(number);
			System.out.flush();
		}
	}

	@Test
	void metaSampleOne() {
		assertThat(getSolution().getUniformIntegerCountInInterval(75, 300)).isEqualTo(5);
	}

	@Test
	void metaSampleTwo() {
		assertThat(getSolution().getUniformIntegerCountInInterval(1, 9)).isEqualTo(9);
	}

	@Test
	void metaSampleThree() {
		assertThat(getSolution().getUniformIntegerCountInInterval(999999999999L, 999999999999L)).isOne();
	}

	@Test
	void sampleFour() {
		assertThat(getSolution().getUniformIntegerCountInInterval(0, 9)).isEqualTo(10);
	}

	@Test
	void sampleFive() {
		assertThat(getSolution().getUniformIntegerCountInInterval(0, 10)).isEqualTo(10);
	}

	@Test
	void sampleSix() {
		assertThat(getSolution().getUniformIntegerCountInInterval(50, 2000)).isEqualTo(15);
	}

	@Test
	void sampleSeven() {
		assertThat(getSolution().getUniformIntegerCountInInterval(56, 65)).isZero();
	}

	@Test
	void sampleEight() {
		assertThat(getSolution().getUniformIntegerCountInInterval(55, 66)).isEqualTo(2);
	}

	@Test
	void sampleNine() {
		assertThat(getSolution().getUniformIntegerCountInInterval(55, 65)).isOne();
		assertThat(getSolution().getUniformIntegerCountInInterval(56, 66)).isOne();
	}

	interface Solution {
		int getUniformIntegerCountInInterval(long A, long B);
	}

	static abstract class AbstractSolution implements Solution {

		boolean isSameDigit(long number) {
			char[] digits = String.valueOf(number).toCharArray();
			return isSameDigit(digits);
		}

		boolean isSameDigit(char[] digits) {
			char digitOne = digits[0];
			for (int index = 1; index < digits.length; index++) {
				if (digits[index] != digitOne) {
					return false;
				}
			}
			return true;
		}

		long increment(long number) {
			double exponent = Math.log10(Long.valueOf(Math.max(number, 1)).doubleValue());
			double increment = Math.pow(10, exponent);
			return number + Double.valueOf(increment).longValue();
		}
	}

	static class SolutionOne extends AbstractSolution {

		@Override
		public int getUniformIntegerCountInInterval(long A, long B) {

			int count = 0;
			long number = A;

			while (number <= B) {
				if (isSameDigit(number)) {
					print(number);
					count++;
				}
				number++;
			}

			return count;
		}
	}

	static class SolutionTwo extends AbstractSolution {

		@Override
		public int getUniformIntegerCountInInterval(long A, long B) {

			int count = 0;
			long number = A;

			while (number <= B) {
				number = getNextUniformNumber(number);
				if (number <= B) {
					count++;
				}
				number++;
			}

			return count;
		}

		private int getExponent(long number) {
			return Math.max(Double.valueOf(Math.floor(Math.log10(Long.valueOf(number).doubleValue()))).intValue(), 0);
		}

		private long getFirstDigit(long number) {
			return Long.parseLong(String.valueOf(String.valueOf(number).charAt(0)));
		}

		private long getMultiplier(int exponent) {
			double multiplier = 1.0d;
			while (exponent > 0) {
				multiplier += Math.pow(10.0d, exponent);
				exponent--;
			}
			return Double.valueOf(multiplier).longValue();
		}

		private long getNextUniformNumber(long number) {
			long uniformNumber = getUniformNumber(number);
			if (uniformNumber < number) {
				long firstDigit = getFirstDigit(number);
				long firstDigitPlusOne = firstDigit + 1;
				long nextNumber = firstDigitPlusOne * getTenToThePowerOf(getExponent(number));
				return getNextUniformNumber(nextNumber);

			}
			return uniformNumber;
		}

		private long getTenToThePowerOf(int exponent) {
			return Double.valueOf(Math.pow(10, exponent)).longValue();
		}

		private long getUniformNumber(long number) {
			long firstDigit = getFirstDigit(number);
			long multiplier = getMultiplier(getExponent(number));
			return firstDigit * multiplier;
		}
	}
}
