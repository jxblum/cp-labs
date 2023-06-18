/*
 * Copyright 2017 Author or Authors.
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
package org.cp.labs.data.struct;

import static org.cp.elements.lang.LangExtensions.assertThat;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.annotation.NotNull;
import org.cp.elements.lang.annotation.Nullable;

/**
 * Java {@link Map} implementation that is two-dimensional (2D).
 *
 * @author John Blum
 * @see java.util.AbstractMap
 * @see java.util.HashMap
 * @see java.util.Map
 * @see org.cp.labs.data.struct.MapWrapper
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class TwoDimensionalMap<KEY, VALUE> extends MapWrapper<KEY, VALUE> {

  protected static final int DEFAULT_INITIAL_CAPACITY = 1067;

  protected static final float DEFAULT_LOAD_FACTOR = 0.75f;

  protected static final BinaryOperator<Integer> SUM = Integer::sum;

  public static @NotNull <KEY, VALUE> TwoDimensionalMap<KEY, VALUE> usingHashMapArray() {
    return new TwoDimensionalMap<>(new HashMapArray<>(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR));
  }

  public static @NotNull <KEY, VALUE> TwoDimensionalMap<KEY, VALUE> usingHashMapArray(int initialCapacity) {
    return new TwoDimensionalMap<>(new HashMapArray<>(initialCapacity, DEFAULT_LOAD_FACTOR));
  }

  public static @NotNull <KEY, VALUE> TwoDimensionalMap<KEY, VALUE> usingHashMapArray(float loadFactor) {
    return new TwoDimensionalMap<>(new HashMapArray<>(DEFAULT_INITIAL_CAPACITY, loadFactor));
  }

  public static @NotNull <KEY, VALUE> TwoDimensionalMap<KEY, VALUE> usingHashMapArray(
      int initialCapacity, float loadFactor) {

    return new TwoDimensionalMap<>(new HashMapArray<>(initialCapacity, loadFactor));
  }

  protected TwoDimensionalMap(@NotNull Map<KEY, VALUE> map) {
    super(map);
  }

  protected static class HashMapArray<KEY, VALUE> extends AbstractMap<KEY, VALUE> {

    private final int initialCapacity;

    private final float loadFactor;

    private final Object[] maps;

    protected HashMapArray() {
      this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    protected HashMapArray(int initialCapacity) {
      this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    protected HashMapArray(float loadFactor) {
      this(DEFAULT_INITIAL_CAPACITY, loadFactor);
    }

    protected HashMapArray(int initialCapacity, float loadFactor) {

      this.initialCapacity = initialCapacity;
      this.loadFactor = loadFactor;
      this.maps = new Object[initialCapacity];
    }

    protected int getInitialCapacity() {
      return this.initialCapacity;
    }

    protected float getLoadFactor() {
      return this.loadFactor;
    }

    protected @NotNull Object[] getMaps() {
      return this.maps;
    }

    private void assertKeyValue(KEY key, VALUE value) {

      Assert.notNull(key, "Key is required");
      Assert.notNull(value, "Value is required");
    }

    protected <K, V> Map<K, V> newMap(int initialCapacity, float loadFactor) {
      return new HashMap<>(initialCapacity, loadFactor);
    }

    private @Nullable VALUE nullSafeMapGet(@Nullable Map<KEY, VALUE> map, @NotNull KEY key) {
      return map != null ? map.get(key) : null;
    }

    private @Nullable VALUE nullSafeMapRemove(@Nullable Map<KEY, VALUE> map, @NotNull KEY key) {
      return map != null ? map.remove(key) : null;
    }

    @SuppressWarnings("all")
    private int resolveIndex(Object key) {

      int mapsLength = getMaps().length;
      int keyHashCode = key.hashCode();
      int index = keyHashCode % mapsLength;

      return index;
    }

    @SuppressWarnings("unchecked")
    private Map<KEY, VALUE> resolveIndexedMap(int index) {

      Object[] maps = getMaps();

      int mapsLength = maps.length;

      assertThat(index)
        .describedAs("Index [%1$d] must be greater than equal to 0 and less than [%2$d]", index, mapsLength)
        .isGreaterThanEqualToAndLessThan(0, mapsLength);

      return (Map<KEY, VALUE>) maps[index];
    }

    @Override
    public boolean isEmpty() {
      return size() < 1;
    }

    @Override
    public void clear() {
      Arrays.fill(getMaps(), null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable VALUE get(@Nullable Object key) {

      return key != null
        ? nullSafeMapGet(resolveIndexedMap(resolveIndex(key)), (KEY) key)
        : null;
    }

    @Override
    public Set<Entry<KEY, VALUE>> entrySet() {

      Set<Entry<KEY, VALUE>> mapsEntries = new HashSet<>();

      Arrays.stream(getMaps())
        .filter(Map.class::isInstance)
        .map(Map.class::cast)
        .map(Map::entrySet)
        .forEach(mapsEntries::addAll);

      return Collections.unmodifiableSet(mapsEntries);
    }

    @Override
    public @Nullable VALUE put(@NotNull KEY key, @NotNull VALUE value) {

      assertKeyValue(key, value);

      int index = resolveIndex(key);

      Map<KEY, VALUE> map = resolveIndexedMap(index);

      if (map == null) {
        map = newMap(getInitialCapacity() / 2, getLoadFactor());
        getMaps()[index] = map;
      }

      return map.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable VALUE remove(@NotNull Object key) {
      return key != null
        ? nullSafeMapRemove(resolveIndexedMap(resolveIndex(key)), (KEY) key)
        : null;
    }

    @Override
    public int size() {

      return Arrays.stream(getMaps())
        .filter(Map.class::isInstance)
        .map(Map.class::cast)
        .map(Map::size)
        .reduce(SUM)
        .orElse(0);
    }
  }
}
