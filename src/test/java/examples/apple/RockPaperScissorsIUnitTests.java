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
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Unit Tests for Apple's {@literal Rock, Paper, Scissors} problem.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @since 0.1.0
 */
public class RockPaperScissorsIUnitTests {

	private static final int WIN_COUNT = 3;

	static void assertPlayer(Player player, String name, int wins) {

		assertThat(player).isNotNull();
		assertThat(player.getName()).isEqualTo(name);
		assertThat(player.winCount()).isEqualTo(wins);
	}

	static void log(String message, Object... args) {
		System.out.printf(message, args);
		System.out.flush();
	}

	@Test
	void playRockPaperScissorsFirstPlayerWithThreeWinsWins() {

		Player jonDoe = Player.as("Jon Doe");
		Player pieDoe = Player.as("Pie Doe");

		assertPlayer(jonDoe, "Jon Doe", 0);
		assertPlayer(pieDoe, "Pie Doe", 0);

		Player winner = null;

		while (winner == null) {
			winner = jonDoe.play(pieDoe);
		}

		if (winner == jonDoe) {
			assertThat(jonDoe.winCount.get()).isGreaterThan(pieDoe.winCount.get());
		}
		else {
			assertThat(pieDoe.winCount.get()).isGreaterThan(jonDoe.winCount.get());
		}

		log("Player [%s] wins!%n", winner);
	}

	@Getter
	@RequiredArgsConstructor(staticName = "as")
	static class Player {

		private final AtomicInteger winCount = new AtomicInteger(0);

		private final String name;

		@Getter(AccessLevel.PRIVATE)
		private final ThreadLocalRandom random = ThreadLocalRandom.current();

		boolean isWinner() {
			return this.winCount.get() >= WIN_COUNT;
		}

		Hand chooseHand() {
			return Hand.ALL_HANDS.get(getRandom().nextInt(Hand.ALL_HANDS.size()));
		}

		Player play(Player player) {

			Hand thisHand = this.chooseHand();
			Hand playerHand = player.chooseHand();

			log("[%s] chose [%s]%n", this.getName(), thisHand);
			log("[%s] chose [%s]%n", player.getName(), playerHand);

			int compareValue = thisHand.compare(playerHand);

			if (compareValue > 0) {
				log("[%s] wins this hand%n", this.getName());
				this.winCount.incrementAndGet();
			}
			else if (compareValue < 0) {
				log("[%s] wins this hand%n", player.getName());
				player.winCount.incrementAndGet();
			}

			log("[%s] has [%d] win(s); [%s] has [%d] win(s)%n", this, this.winCount(), player, player.winCount());

			return this.isWinner() ? this : player.isWinner() ? player : null;
		}

		int winCount() {
			return this.winCount.get();
		}

		@Override
		public String toString() {
			return getName();
		}
	}

	enum Hand {

		ROCK, PAPER, SCISSORS;

		static final List<Hand> ALL_HANDS = List.of(ROCK, PAPER, SCISSORS);

		int compare(Hand hand) {

			if (this.equals(ROCK)) {
				return PAPER.equals(hand) ? -1 : SCISSORS.equals(hand) ? 1 : 0;
			}
			else if (this.equals(PAPER)) {
				return ROCK.equals(hand) ? 1 : SCISSORS.equals(hand) ? -1 : 0;
			}
			else { // SCISSORS
				return ROCK.equals(hand) ? -1 : PAPER.equals(hand) ? 1 : 0;
			}
		}
	}
}
