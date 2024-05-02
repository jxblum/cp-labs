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

import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.junit.jupiter.api.Test;

import org.cp.elements.lang.Assert;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Unit Tests for Apple's {@literal Matching Braces} problem.
 *
 * @author John Blum
 * @since 0.1.0
 */
public class MatchingBracesUnitTests {

	private final BracesCompiler compiler = new StackBasedBracesCompiler();

	@Test
	void validSimpleExpression() {
		assertThat(this.compiler.compile("{}")).isTrue();
	}

	@Test
	void validComplexExpression() {
		assertThat(this.compiler.compile("{[[()]]}")).isTrue();
	}

	@Test
	void invalidSimpleExpression() {

		assertThat(this.compiler.compile("{")).isFalse();
		assertThat(this.compiler.compile("}")).isFalse();
	}

	@Test
	void invalidComplexExpressionOne() {
		assertThat(this.compiler.compile("{{{[{}]}")).isFalse();
	}

	@Test
	void invalidComplexExpressionTwo() {
		assertThat(this.compiler.compile("{{[[}]}]")).isFalse();
	}

	@FunctionalInterface
	interface BracesCompiler {

		static char nullSafeChar(Character character) {
			return character != null ? character : '\0';
		}

		boolean compile(String expression);

		default boolean isOpenBrace(Character character) {
			return getSetOfOpenBraces().contains(nullSafeChar(character));
		}

		default boolean isClosedBrace(Character character) {
			return getMapOfClosedToOpenBraces().containsKey(nullSafeChar(character));
		}

		default Map<Character, Character> getMapOfClosedToOpenBraces() {
			return Map.of('}', '{', ']', '[', ')', '(');
		}

		default Set<Character> getSetOfOpenBraces() {
			return Set.of('{', '[', '(');
		}
	}

	@Getter(AccessLevel.PROTECTED)
	static class StackBasedBracesCompiler implements BracesCompiler {

		private final Stack<Character> stack = new Stack<>();

		@Override
		public boolean compile(String expression) {

			Assert.hasText(expression, "Expression '%s' to compile is required", expression);

			try {
				for (char character : expression.trim().toCharArray()) {
					if (isOpenBrace(character)) {
						getStack().push(character);
					}
					else if (isClosedBrace(character)) {

						Assert.isFalse(getStack().isEmpty(),
							new CompilerException("Encounterd Closing brace '%s' without a matching Opening brace"
								.formatted(character)));

						Character actualOpenBrace = getStack().pop();
						Character expectedOpenBrace = getMapOfClosedToOpenBraces().get(character);

						Assert.isTrue(actualOpenBrace.equals(expectedOpenBrace),
							new CompilerException("Opening brace '%s']' did not match Closing brace '%s'; expected '%s'"
								.formatted(actualOpenBrace, character, expectedOpenBrace)));
					}
				}

				Assert.isTrue(getStack().isEmpty(), new CompilerException("No matching Closing braces found for '%s'"
					.formatted(getStack())));

				return true;
			}
			catch (CompilerException e) {
				getStack().clear();
				log(e.getMessage());
				return false;
			}
		}

		private void log(String message, Object... arguments) {
			System.out.printf(message, arguments);
			System.out.flush();
		}
	}

	static class CompilerException extends RuntimeException {

		CompilerException() { }

		CompilerException(String message) {
			super(message);
		}

		CompilerException(Throwable cause) {
			super(cause);
		}

		CompilerException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
