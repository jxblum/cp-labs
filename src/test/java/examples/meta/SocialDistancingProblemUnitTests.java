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

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Unit Tests for {@literal Meta's Social Distancing Problem}.
 * <p>
 * Determine the maximum number of additional diners who can potentially sit at the table without social distancing
 * guidelines being violated for any new or existing diners, assuming that the existing diners cannot move
 * and that the additional diners will cooperate to maximize how many of them can sit down.
 * <p>
 * Please take care to write a solution which runs within the time limit.
 * <p>
 * CONSTRAINTS
 * <p>
 * 1 ≤ N ≤ 10^15
 * 1 ≤ K ≤ N
 * 1 ≤ M ≤ 500,000
 * M ≤ N
 * 1 ≤ S i ≤ N
 * <p>
 * SAMPLE TEST CASE 1: N = 10, K = 1, M = 2, S = [ 2, 6 ]; Expected return value is 3
 * SAMPLE TEST CASE 2: N = 15, K = 2, M = 3, S = [ 11, 6, 14 ]; Expected return value is 1
 * <p>
 * SAMPLE EXPLANATION
 * <p>
 * In the first case, the cafeteria table has N = 10 seats, with two diners currently at seats 2 and 6 respectively.
 * The table initially looks as follows, with brackets covering the [ 0, 1, 0, 0, 0 1, 0, 0, 0, 0 ].
 * <p>
 * Three additional diners may sit at seats 4, 8 and 10 without violating the social distancing guidelines.
 * <p>
 * In the second case, only 1 additional diner is able to join the table, by sitting in any of the first 3 seats.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @since 1.0.0
 */
@Getter(AccessLevel.PROTECTED)
public class SocialDistancingProblemUnitTests {

	private static final boolean DEBUG = true;

	private final Solution solution = new SolutionThree();

	@Test
	void emptyTableWithNoSocialDistancing() {

		int[] seats = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		assertThat(getSolution().countAvailableSeats(seats, 0)).isEqualTo(10);
	}

	@Test
	void emptyTableWithSocialDistancingOfOne() {

		int[] seats = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		assertThat(getSolution().countAvailableSeats(seats, 1)).isEqualTo(5);
	}

	@Test
	void emptyTableWithSocialDistancingOfTwo() {

		int[] seats = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		assertThat(getSolution().countAvailableSeats(seats, 2)).isEqualTo(4);
	}

	@Test
	void emptyTableWithSocialDistancingOfThree() {

		int[] seats = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		assertThat(getSolution().countAvailableSeats(seats, 3)).isEqualTo(3);
	}

	@Test
	void emptyTableWithSocialDistancingOfFour() {

		int[] seats = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		assertThat(getSolution().countAvailableSeats(seats, 4)).isEqualTo(2);
	}

	@Test
	void emptyTableWithSocialDistancingOfFive() {

		int[] seats = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		assertThat(getSolution().countAvailableSeats(seats, 5)).isEqualTo(2);
	}

	@Test
	void emptyTableWithSocialDistancingOfSix() {

		int[] seats = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		assertThat(getSolution().countAvailableSeats(seats, 6)).isEqualTo(2);
	}

	@Test
	void emptyTableWithSocialDistancingOfSeven() {

		int[] seats = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		assertThat(getSolution().countAvailableSeats(seats, 7)).isEqualTo(2);
	}

	@Test
	void emptyTableWithSocialDistancingOfEight() {

		int[] seats = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		assertThat(getSolution().countAvailableSeats(seats, 8)).isEqualTo(2);
	}

	@Test
	void emptyTableWithSocialDistancingOfNine() {

		int[] seats = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		assertThat(getSolution().countAvailableSeats(seats, 9)).isOne();
	}

	@Test
	void fullTableWithSocialDistancingOfZero() {

		int[] seats = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };

		assertThat(getSolution().countAvailableSeats(seats, 0)).isZero();
	}

	@Test
	void fullTableWithSocialDistancingOfOne() {

		int[] seats = { 1, 0, 1, 0, 1, 0, 1, 0, 1, 0 };

		assertThat(getSolution().countAvailableSeats(seats, 1)).isZero();
	}

	@Test
	void occupiedTableWithNoSocialDistancing() {

		int[] seats = { 1, 0, 1, 1, 0, 0, 1, 0, 1, 1 };

		assertThat(getSolution().countAvailableSeats(seats, 0)).isEqualTo(4);
	}

	@Test
	void occupiedTableWithSocialDistancingOfOne() {

		int[] seats = { 0, 1, 0, 0, 1, 0, 1, 0, 0, 0 };

		assertThat(getSolution().countAvailableSeats(seats, 1)).isOne();
	}

	@Test
	void occupiedTableWithSocialDistancingOfOne2() {

		int[] seats = { 0, 0, 1, 0, 1, 0, 1, 0, 0, 0 };

		assertThat(getSolution().countAvailableSeats(seats, 1)).isEqualTo(2);
	}

	@Test
	void occupiedTableWithSocialDistancingOfOne3() {

		int[] seats = { 1, 0, 0, 0, 1, 0, 0, 0, 1, 0 };

		assertThat(getSolution().countAvailableSeats(seats, 1)).isEqualTo(2);
	}

	@Test
	void occupiedTableWithSocialDistancingOfOne4() {

		int[] seats = { 1, 1, 1, 0, 0, 0, 1, 0, 0, 0 };

		assertThat(getSolution().countAvailableSeats(seats, 1)).isEqualTo(2);
	}

	@Test
	void occupiedTableWithSocialDistancingOfTwo() {

		int[] seats = { 0, 0, 1, 0, 0, 0, 1, 0, 0, 0 };

		assertThat(getSolution().countAvailableSeats(seats, 2)).isOne();
	}

	@Test
		// Technically violates social distancing
	void occupiedTableWithSocialDistancingOfTwo2() {

		int[] seats = { 0, 0, 0, 1, 0, 1, 1, 0, 0, 0 };

		assertThat(getSolution().countAvailableSeats(seats, 2)).isEqualTo(2);
	}

	@Test
		// Technically violates social distancing
	void occupiedTableWithSocialDistancingOfTwo3() {

		int[] seats = { 1, 1, 0, 0, 0, 0, 1, 0, 0, 0 };

		assertThat(getSolution().countAvailableSeats(seats, 2)).isOne();
		//assertThat(getSolution().countAvailableSeatsWithLookBehind(seats, 2)).isOne();
	}

	@Test
	void metaSampleOne() {

		int[] seats = { 2, 6 };

		assertThat(getSolution().countAvailableSeats(seats, 10, 1)).isEqualTo(3);
	}

	@Test
	void metaSampleTwo() {

		int[] seats = { 11, 6, 14 };

		assertThat(getSolution().countAvailableSeats(seats, 15, 2)).isOne();
	}

	@Test
	void metaTestOne() {

		int[] seats = { 4, 10, 17 };

		assertThat(getSolution().countAvailableSeats(seats, 20, 2)).isEqualTo(4);
	}

	@Test
	void metaTestTwo() {

		int[] seats = { 4, 10, 17 };

		assertThat(getSolution().countAvailableSeats(seats, 20, 1)).isEqualTo(6);
	}

	@Test
	void metaTestThree() {

		int[] seats = { };

		assertThat(getSolution().countAvailableSeats(seats, 20, 1)).isEqualTo(10);
	}

	@Test
	void metaTestFour() {

		int[] seats = { 9 };

		assertThat(getSolution().countAvailableSeats(seats, 20, 1)).isEqualTo(9);
	}

	@Test
	void metaTestFive() {

		int[] seats = { 4, 11, 18};

		assertThat(getSolution().countAvailableSeats(seats, 20, 1)).isEqualTo(6);
	}

	private static void print(int[] array) {
		if (DEBUG) {
			System.out.printf("%s%n", Arrays.toString(array));
			System.out.flush();
		}
	}

	interface Solution {

		default int countAvailableSeats(int[] seats, int K) {
			return countAvailableSeats(seats, seats.length, K);
		}

		default int countAvailableSeats(int[] seats, int N, int K) {

			int M = 0;

			for (int index = 0; index < seats.length; index++) {
				if (seats[index] != 0) {
					M++;
				}
			}

			return countAvailableSeats(seats, N, M, K);
		}

		int countAvailableSeats(int[] seats, int N, int M, int K);

	}

	static abstract class AbstractSolution implements Solution {

		int[] addSeats(int[] seats, int N) {

			if (seats.length < N) {
				int[] newSeats = new int[N];
				for (int index = 0; index < seats.length; index++) {
					newSeats[seats[index] - 1] = 1;
				}
				return newSeats;
			}

			return seats;
		}

		int[] removeSeats(int[] seats, int M) {

			if (seats.length > M) {
				int newSeatIndex = 0;
				int[] newSeats = new int[M];
				for (int index = 0; index < seats.length; index++) {
					if (seats[index] != 0) {
						newSeats[newSeatIndex++] = index + 1;
					}
				}
				return newSeats;
			}

			return seats;
		}
	}

	static class SolutionOne extends AbstractSolution {

		// Seats is the current seating arrangement where non-zero elements indicate occupied
		// K is the social distance (number of seats between people)
		public int countAvailableSeats(int[] seats, int N, int M, int K) {

			seats = addSeats(seats, N);
			print(seats);

			int count = 0;

			for (int index = 0; index < seats.length; index++) {
				if (seats[index] == 0) { // seat is available
					boolean seated = false;
					LOOK_AHEAD: for (int j = index + 1, stop = Math.min(seats.length, j + K); j < stop; j++) {
						if (seats[j] != 0) {
							seated = true;
							index = j;
						}
					}
					if (!seated) {
						seats[index] = 2;
						count++;
					}
				}
				index += K;
			}

			print(seats);

			return count;
		}
	}

	static class SolutionTwo extends AbstractSolution {

		public int countAvailableSeats(int[] seats, int N, int M, int K) {

			if (M == 0) {
				int count = N / (K + 1);
				return count + (K != 0 && N % (count * (K + 1)) > 0 ? 1 : 0);
			}

			seats = removeSeats(seats, M);
			Arrays.sort(seats);
			print(seats);

			int count = 0;

			count += (seats[0] - 1) / (K + 1);
			count += (N - seats[seats.length - 1]) / (K + 1);

			for (int index = 1; index < seats.length; index++) {
				int availableSeatsBetween = seats[index] - seats[index - 1] - 1;
				count += availableSeatsBetween / (2 * K + 1);
			}

			return count;
		}
	}

	static class SolutionThree extends AbstractSolution {

		@Override
		public int countAvailableSeats(int[] seats, int N, int M, int K) {

			int count = 0;

			// no one is seated at table
			if (M == 0) {
				// 1st person sits in seat 1
				// handle no social distancing (K == 0)
				return (N - 1) / (K + 1) + 1;
			}

			seats = removeSeats(seats, M);
			Arrays.sort(seats);
			print(seats);

			int previousSeat = 0;

			for (int index = 0; index < seats.length; index++) {
				int availableSeats = seats[index] - previousSeat - 1;
				count += availableSeats / (2 * K + 1);
				previousSeat = seats[index];
			}

			count += (N - previousSeat) / (K + 1);

			return count;
		}

	}
}
