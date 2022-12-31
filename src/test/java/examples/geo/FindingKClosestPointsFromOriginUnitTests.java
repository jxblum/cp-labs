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
package examples.geo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import org.cp.elements.lang.MathUtils;
import org.cp.elements.lang.annotation.NotNull;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Unit Tests for finding the closest K {@link Point points} from the {@link Point origin}.
 *
 * @author John Blum
 * @since 1.0.0
 */
public class FindingKClosestPointsFromOriginUnitTests {

  private final Point origin = Point.at(0, 0);

  private final List<Point> points = Arrays.asList(
    Point.at(11, 5),
    Point.at(1, 20),
    Point.at(2, 3),
    Point.at(4, 7),
    Point.at(16, 2),
    Point.at(1, 2),
    Point.at(5, 8)
  );

  @Test
  public void findClosestKPoints() {

    List<Point> orderedByDistancePoints = this.points.stream()
      .map(point -> point.computeDistanceFromOrigin(this.origin))
      .sorted()
      .collect(Collectors.toList());

    assertThat(orderedByDistancePoints).isNotNull();
    assertThat(orderedByDistancePoints).hasSize(this.points.size());
    assertThat(orderedByDistancePoints).containsExactlyInAnyOrder(this.points.toArray(new Point[0]));

    Point closestPoint = orderedByDistancePoints.get(0);

    assertThat(closestPoint).isNotNull();
    assertThat(closestPoint).isEqualTo(Point.at(1, 2));
  }


  @Getter
  @ToString(of = { "x", "y" })
  @EqualsAndHashCode(of = { "x", "y" })
  @RequiredArgsConstructor(staticName = "at")
  public static class Point implements Comparable<Point> {

    @lombok.NonNull
    private final int x;

    @lombok.NonNull
    private final int y;

    @Setter(AccessLevel.PACKAGE)
    private int distanceFromOrigin;

    public @NotNull Point computeDistanceFromOrigin(@NotNull Point origin) {

      int xlength = this.getX() - origin.getX();
      int ylength = this.getY() - origin.getY();
      int hypotenuse = Double.valueOf(MathUtils.pythagoreanTheorem(xlength, ylength)).intValue();

      return withDistanceFromOrigin(hypotenuse);
    }

    public @NotNull Point withDistanceFromOrigin(int distance) {
      setDistanceFromOrigin(distance);
      return this;
    }

    @Override
    public int compareTo(Point that) {
      return this.getDistanceFromOrigin() - that.getDistanceFromOrigin();
    }
  }
}
