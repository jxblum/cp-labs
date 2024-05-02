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
package examples.orkes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import org.cp.elements.lang.Assert;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Unit Tests for {@literal Spreadsheet} program & implementation.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see <a href="https://gist.github.com/c4lm/c0df652cad5e9b89ebc451a1a0ecf3cd">Spreadsheet Problem</a>
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public class SpreadsheetUnitTests {

	private final Spreadsheet spreadsheet = new MapSpreadsheet();

	@Test
	void getCellValueWhenUnset() {
		assertThat(this.spreadsheet.getCellValue("A1")).isZero();
	}

	@Test
	void getCellValueStoringInteger() {

		this.spreadsheet.setCellValue("A1", 1);

		assertThat(this.spreadsheet.getCellValue("A1")).isOne();
	}

	@Test
	void getCellValueStoringIntegerExpression() {

		this.spreadsheet.setCellValue("A1", "=42");

		assertThat(this.spreadsheet.getCellValue("A1")).isEqualTo(42);
	}

	@Test
	void getCellValueStoringIntegerString() {

		this.spreadsheet.setCellValue("A1", "101");

		assertThat(this.spreadsheet.getCellValue("A1")).isEqualTo(101);
	}

	@Test
	void getCellValueReferencingCellValue() {

		this.spreadsheet.setCellValue("A1", "2");
		this.spreadsheet.setCellValue("B2", "=A1");

		assertThat(this.spreadsheet.getCellValue("B2")).isEqualTo(2);
	}

	@Test
	void getCellValueWithComplexExpression() {

		this.spreadsheet.setCellValue("A1", "=10/2+35*2-45");

		int expectedValue = 10 / 2 + 35 * 2 - 45;

		assertThat(this.spreadsheet.getCellValue("A1")).isEqualTo(expectedValue);
	}

	@Test
	void getCellValueWithMathematicalOperationAppliedToCellReferences() {

		this.spreadsheet.setCellValue("A1", 10);
		this.spreadsheet.setCellValue("B2", 20);
		this.spreadsheet.setCellValue("D4", "=A1*B2+C3");
		this.spreadsheet.setCellValue("E5", "=B2*C3");

		assertThat(this.spreadsheet.getCellValue("D4")).isEqualTo(200);
		assertThat(this.spreadsheet.getCellValue("E5")).isZero();
	}

	@Test
	void divideByZero() {

		this.spreadsheet.setCellValue("A1", 10);
		this.spreadsheet.setCellValue("B2", "=A1/C3");

		assertThatExceptionOfType(ArithmeticException.class)
			.isThrownBy(() -> this.spreadsheet.getCellValue("B2"))
			.withMessage("/ by zero")
			.withNoCause();
	}

	@Test
	void whenCellValueIsUpdated() {

		this.spreadsheet.setCellValue("A1", 12);
		this.spreadsheet.setCellValue("B2", "=A1+8");

		assertThat(this.spreadsheet.getCellValue("B2")).isEqualTo(20);

		this.spreadsheet.setCellValue("A1", 21);

		assertThat(this.spreadsheet.getCellValue("B2")).isEqualTo(29);
	}

	@Test
	void usingInterviewerTestCase() {

		this.spreadsheet.setCellValue("B12", 14);
		this.spreadsheet.setCellValue("B13", 356);
		this.spreadsheet.setCellValue("B14", "=B12+B13");
		this.spreadsheet.setCellValue("B15", "=B14/10-7");
		this.spreadsheet.setCellValue("C15", "=B15/10+7+B12/2*3+1");

		int expectedValue = ((14 + 356) / 10 - 7) / 10 + 7 + 14 / 2 * 3 + 1;

		assertThat(this.spreadsheet.getCellValue("C15")).isEqualTo(expectedValue);

		this.spreadsheet.setCellValue("B13", 456);

		expectedValue = ((14 + 456) / 10 - 7) / 10 + 7 + 14 / 2 * 3 + 1;

		assertThat(this.spreadsheet.getCellValue("C15")).isEqualTo(expectedValue);

		// TODO: cycle
		this.spreadsheet.setCellValue("B13", "=B15+1");

		assertThatExceptionOfType(CyclicReferenceException.class)
			.isThrownBy(() -> this.spreadsheet.getCellValue("B14"))
			.withMessage("...")
			.withNoCause();
	}

	/*
	PROBLEM:

	Design a spreadsheet backend which can support the following operations:

	void setCellValue(String cellId, Object value)
	int getCellValue(String cellId)
	void clear()

	EXAMPLE:

	setCellValue("A1", 13)
	getCellValue("A1") -> 13

	setCellValue("A2", 14)
	getCellValue("A2") -> 14

	setCellValue("A3", "=A1+A2")
	getCellValue("A3") -> 27

	setCellValue("A4", "=A1+A2+A3")
	getCellValue("A4") -> 54

	setCellValue("A5", "=A1+A2+A3+10")
	getCellValue("A5") -> 64

	REQUIREMENTS:

	* You are NOT allowed to use any "eval" libraries.
	* A spreadsheet cell is identified by 1-1000 capital English alphabet letters followed by 1-1000 digits.
	* If there's a cycle, set all affected cells to Integer.MIN_VALUE or equivalent.
	* If there's a division by zero, set affected cell to Integer.MAX_VALUE or equivalent.    𓐆

 	TESTS CASES:

	setCellValue("B12", 14)
	setCellValue("B13", 356)
	setCellValue("B14", "=B12+B13")
	setCellValue("B15", "=B14/10-7")
	setCellValue("C15", "=B15/10+7+B12/2*3+1")
	getCellValue("C15") -> ?

	setCellValue("B13", 456)
	getCellValue("C15") -> ?

	setCellValue("B13", "=B15+1")
	getCellValue("B14") -> ?
	*/

	interface Spreadsheet {

		int getCellValue(String cellId);

		void setCellValue(String cellId, Object value);

		void clear();

	}

	static abstract class AbstractSpreadsheet implements Spreadsheet {

		static final int DEFAULT_UNSET_CELL_VALUE = 0;

		static final String EXPRESSION_OPERATOR = "=";

		static final Predicate<Character> IS_DIGIT = character -> Character.isDigit(nullSafeChar(character));
		static final Predicate<Character> IS_LETTER = character -> Character.isLetter(nullSafeChar(character));
		static final Predicate<Character> IS_OPERATOR = Operators::isOperator;
		static final Predicate<Character> IS_WHITESPACE = character -> Character.isWhitespace(nullSafeChar(character));
		static final Predicate<Character> IS_DIGIT_OR_LETTER = IS_DIGIT.or(IS_LETTER);
		static final Predicate<Character> IS_DIGIT_LETTER_OR_OPERATOR = IS_DIGIT_OR_LETTER.or(IS_OPERATOR);

		static char nullSafeChar(Character character) {
			return character != null ? character : '\0';
		}

		void assertCellId(String cellId) {

			Assert.hasText(cellId, "Cell ID [%s] is required", cellId);

			for (char character : cellId.toCharArray()) {
				if (IS_DIGIT_OR_LETTER.negate().test(character)) {
					throw new IllegalArgumentException("[%] is not a valid cell ID/reference");
				}
			}
		}

		void assertCellValue(Object value) {

			Assert.notNull(value, "Value is required");

			String stringValue = asString(value);

			if (isExpression(stringValue)) {
				stringValue = stringValue.substring(1);
			}

			for (char character : stringValue.toCharArray()) {
				if (IS_DIGIT_LETTER_OR_OPERATOR.negate().test(character)) {
					throw new IllegalArgumentException("Cell value [%s] is not valid".formatted(stringValue));
				}
			}
		}

		String asString(Object value) {
			Assert.notNull(value, "Value is required");
			return value instanceof String string ? string
				: String.valueOf(value).trim();
		}

		boolean isCellReference(String value) {
			return isSet(value) && IS_LETTER.test(value.charAt(0));
		}

		boolean isExpression(Object value) {
			return asString(value).startsWith(EXPRESSION_OPERATOR);
		}

		boolean isNumericExpression(Object value) {

			if (isSet(value)) {
				for (char character : asString(value).toCharArray()) {
					if (IS_DIGIT.negate().test(character)) {
						return false;
					}
				}

				return true;
			}

			return false;
		}

		boolean isSet(Object value) {
			return value != null;
		}

		boolean isUnset(Object value) {
			return !isSet(value);
		}

		Integer toInteger(String value) {

			try {
				return Integer.parseInt(value.trim());
			}
			catch (Exception e) {
				throw new IllegalArgumentException("Value [%s] is not a valid Integer".formatted(value), e);
			}
		}

		String trimAllWhitespace(String value) {

			Assert.hasText(value, "Value [%s] is required");

			StringBuilder trimmedValue = new StringBuilder();

			for (char element : value.trim().toCharArray()) {
				if (IS_WHITESPACE.negate().test(element)) {
					trimmedValue.append(element);
				}
			}

			return trimmedValue.toString();
		}
	}

	@Getter(AccessLevel.PROTECTED)
	static class MapSpreadsheet extends AbstractSpreadsheet {

		private final Map<String, Object> sheet = new HashMap<>();

		@Override
		public int getCellValue(String cellId) {

			assertCellId(cellId);

			Object cellValue = getSheet().get(cellId);

			if (isUnset(cellValue)) {
				return DEFAULT_UNSET_CELL_VALUE;
			}
			if (cellValue instanceof Integer intValue) {
				return intValue;
			}
			else {

				String stringValue = trimAllWhitespace(asString(cellValue));

				if (isExpression(stringValue)) {
					// trim expression operator "="
					stringValue = stringValue.substring(1);
				}

				// Handle int values and int expressions [ 101, =51 ]
				if (isNumericExpression(stringValue)) {
					return toInteger(stringValue);
				}
				else {
					return parseExpression(stringValue, Operators.ADD).evaluate();
				}
			}
		}

		// Must handle cell references
		// Must handle cyclic references
		// Must handle self references
		// Must handle operator precedence
		private Expression parseExpression(String expressionValue, Operators operator) {

			if (Operators.containsOperator(expressionValue)) {

				String[] operatorSplitExpressions = expressionValue.split(operator.regex());
				Expression expression = null;

				for (String subExpression : operatorSplitExpressions) {
					expression = operator.compose(expression, parseExpression(subExpression, operator.getNext()));
				}

				return expression;
			}

			return isCellReference(expressionValue)
				? new CellReferenceExpression(this, expressionValue)
				: new ValueExpression(toInteger(expressionValue));
		}

		@Override
		public void setCellValue(String cellId, Object value) {

			assertCellId(cellId);
			assertCellValue(value);

			getSheet().put(cellId, value);
		}

		@Override
		public void clear() {
			getSheet().clear();
		}
	}

	@FunctionalInterface
	interface Expression {
		int evaluate();
	}

	record ValueExpression(int value) implements Expression {

		static ValueExpression from(Integer value) {
			int resolvedValue = value != null ? value : 0;
			return new ValueExpression(resolvedValue);
		}

		@Override
		public int evaluate() {
			return value();
		}
	}

	record CellReferenceExpression(Spreadsheet sheet, String cellId) implements Expression {

		@Override
		public int evaluate() {
			return sheet().getCellValue(cellId());
		}
	}

	@Getter(AccessLevel.PROTECTED)
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	static class CompositeExpression implements Expression {

		private final Expression expressionOne;
		private final Expression expressionTwo;

		private final Operators operator;

		static Expression compose(Expression one, Expression two, Operators operator) {

			return one == null ? two
				: two == null ? one
				: new CompositeExpression(one, two, operator);
		}

		@Override
		public int evaluate() {
			return getOperator().evaluate(getExpressionOne().evaluate(), getExpressionTwo().evaluate());
		}
	}

	@RequiredArgsConstructor
	@Getter(AccessLevel.PROTECTED)
	enum Operators {

		DIVIDE('/', (valueOne, valueTwo) -> valueOne / valueTwo, null),
		MULTIPLY('*', (valueOne, valueTwo) -> valueOne * valueTwo, DIVIDE),
		SUBTRACT('-', (valueOne, valueTwo) -> valueOne - valueTwo, MULTIPLY),
		ADD('+', Integer::sum, SUBTRACT);

		private static final Predicate<Character> IS_OPERATOR = Operators.<Character>notNull().and(character ->
			Set.of(ADD, SUBTRACT, MULTIPLY, DIVIDE).stream()
				.map(Operators::getSymbol)
				.toList()
				.contains(character));

		private static final Predicate<String> CONTAINS_OPERATOR = expression ->
			Set.of(ADD, SUBTRACT, MULTIPLY, DIVIDE).stream()
				.map(Operators::getSymbol)
				.map(String::valueOf)
				.anyMatch(operator -> String.valueOf(expression).contains(operator));

		private static <T> Predicate<T> notNull() {
			return Objects::nonNull;
		}

		static boolean isOperator(Character value) {
			return IS_OPERATOR.test(value);
		}

		static boolean containsOperator(String expression) {
			return CONTAINS_OPERATOR.test(expression);
		}

		private final char symbol;

		private final BiFunction<Integer, Integer, Integer> mathFunction;

		private final Operators next;

		Expression compose(Expression one, Expression two) {
			return CompositeExpression.compose(one, two, this);
		}

		int evaluate(int valueOne, int valueTwo) {
			return getMathFunction().apply(valueOne, valueTwo);
		}

		String regex() {
			return Set.of(ADD, MULTIPLY).contains(this) ? "\\".concat(toString()) : toString();
		}

		@Override
		public String toString() {
			return Character.toString(getSymbol());
		}
	}

	// TODO: Include in Codeprimate Elements library
	static class CyclicReferenceException extends RuntimeException {

		CyclicReferenceException() { }

		CyclicReferenceException(String message) {
			super(message);
		}

		CyclicReferenceException(Throwable cause) {
			super(cause);
		}

		CyclicReferenceException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
