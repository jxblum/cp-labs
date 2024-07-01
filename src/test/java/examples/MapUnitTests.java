package examples;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit Tests for {@link java.util.Map}.
 *
 * @author Joh Blum
 * @see java.util.Map
 * @see org.junit.jupiter.api.Test
 */
public class MapUnitTests {

	@Test
	@SuppressWarnings("all")
	void mapOfContainingNullValue() {

		assertThatNullPointerException()
			.isThrownBy(() -> Map.of("keyOne", "valueOne", "keyTwo", null))
			.withNoCause();
	}
}
