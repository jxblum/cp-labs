/*
 * Copyright 2011-Present Author or Authors.
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

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

/**
 * Unit Tests for Regular Expressions (REGEX).
 *
 * @author John Blum
 * @see java.util.regex.Matcher
 * @see java.util.regex.Pattern
 * @since 1.0.0
 */
public class RegexUnitTests {

  @Test
  public void matchesRegex() {

    //Pattern pattern = Pattern.compile("^Abstract.+$");
    //Pattern pattern = Pattern.compile("^Abstract.+(?<!Exception|Util)$");
    Pattern pattern = Pattern.compile("^(?!.+Exception|.+Util)(Abstract.+)$");

    assertThat(pattern.matcher("AbstractClassName").matches()).isTrue();
    assertThat(pattern.matcher("ConcreteClassName").matches()).isFalse();
    assertThat(pattern.matcher("AbstractException").matches()).isFalse();
    assertThat(pattern.matcher("RuntimeException").matches()).isFalse();
    assertThat(pattern.matcher("AbstractItUtil").matches()).isFalse();
    assertThat(pattern.matcher("ZipUtil").matches()).isFalse();
  }
}
