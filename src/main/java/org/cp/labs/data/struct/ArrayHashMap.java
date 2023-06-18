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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.ObjectUtils;
import org.cp.elements.lang.annotation.NotNull;
import org.cp.elements.lang.annotation.Nullable;
import org.cp.elements.util.MapUtils;

/**
 * Java {@link Map} implementation backed by an array and a sorted, linked, and indexed data structure.
 *
 * @author John Blum
 * @see java.util.Map
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class ArrayHashMap<KEY, VALUE> implements Map<KEY, VALUE> {

  protected static final int DEFAULT_INITIAL_CAPACITY = 2557;

  protected static final float DEFAULT_LOAD_FACTOR = 0.75f;
  protected static final float MAP_ARRAY_INITIAL_CAPACITY = 0.1f; // 10 %

  private final AtomicInteger size = new AtomicInteger(0);

  private final int initialCapacity;

  private final float loadFactor;

  private final MapEntry<KEY, VALUE>[][] map;

  /**
   * Constructs a new {@link ArrayHashMap} with a default {@link Integer initial capacity}
   * and {@link Float load factor}.
   */
  public ArrayHashMap() {
    this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
  }

  /**
   * Constructs a new {@link ArrayHashMap} with the given {@link Integer initial capacity}
   * and default {@link Float load factor}.
   *
   * @param initialCapacity {@link Integer} specifying the {@literal number of buckets} in the new {@literal map}.
   * @throws IllegalArgumentException if the {@link Integer initial capacity} is equal to less than {@literal 0}.
   */
  public ArrayHashMap(int initialCapacity) {
    this(initialCapacity, DEFAULT_LOAD_FACTOR);
  }

  /**
   * Constructs a new {@link ArrayHashMap} with the given {@link Integer initial capacity}
   * and {@link Float load factor}.
   *
   * @param initialCapacity {@link Integer} specifying the {@literal number of buckets} in the new {@literal map}.
   * @param loadFactor {@link Float} specifying the {@literal percentage} of the {@literal capacity} filled
   * before the {@literal map} must be resized.
   * @throws IllegalArgumentException if the {@link Integer initial capacity} is equal to less than {@literal 0}
   * or the {@link Float load factor} is less than equal to {@literal 0.0} or greater than {@literal 1.0}.
   */
  @SuppressWarnings("unchecked")
  public ArrayHashMap(int initialCapacity, float loadFactor) {

    assertInitialCapacityAndLoadFactor(initialCapacity, loadFactor);

    this.map = new MapEntry[initialCapacity][(int) (initialCapacity * MAP_ARRAY_INITIAL_CAPACITY)];
    this.initialCapacity = initialCapacity;
    this.loadFactor = loadFactor;
  }

  private void assertInitialCapacityAndLoadFactor(int initialCapacity, float loadFactor) {

    assertThat(initialCapacity)
      .describedAs("Initial capacity [%d] must be greater than 0", initialCapacity)
      .isGreaterThan(0);

    assertThat(loadFactor)
      .describedAs("Load factor [%d] must be greater than 0.0 and less than equal to 1.0")
      .isGreaterThanAndLessThanEqualTo(0.0f, 1.0f);
  }

  private void assertKeyValue(Object key, Object value) {

    Assert.notNull(key, "Key is required");
    Assert.notNull(value, "Value is required");
  }

  @SuppressWarnings("all")
  private Index2d computeIndex(@NotNull Object key) {

    int keyHashCode = key.hashCode();
    int xIndex = keyHashCode % this.map.length;
    int yIndex = keyHashCode % this.map[xIndex].length;

    return Index2d.of(xIndex, yIndex);
  }

  /**
   * Gets the {@link Integer initial capcity} of {@literal this} {@link Map} when initialized.
   *
   * @return the {@link Integer initial capcity} of {@literal this} {@link Map} when initialized.
   */
  protected int getInitialCapacity() {
    return this.initialCapacity;
  }

  /**
   * Gets the {@link Float load factor} used as the determining threshold for {@literal this} {@link Map}
   * implementation when resizing.
   *
   * @return the {@link Float load factor} used as the determining threshold for {@literal this} {@link Map}
   * implementation when resizing.
   */
  protected float getLoadFactor() {
    return this.loadFactor;
  }

  /**
   * Returns a reference to the ({@literal array of hash mapping entries data structure}) used as
   * the implementation of this {@link Map}.
   *
   * @return a reference to the {@link Map} model.
   */
  protected @NotNull Object[] getMap() {
    return this.map;
  }

  @Override
  public boolean isEmpty() {
    return size() > 0;
  }

  @Override
  public void clear() {
    Arrays.fill(getMap(), null);
  }

  @Override
  public boolean containsKey(@Nullable Object key) {
    return entrySet().stream().anyMatch(entry -> entry.getKey().equals(key));
  }

  @Override
  public boolean containsValue(@Nullable Object value) {
    return entrySet().stream().anyMatch(entry -> ObjectUtils.equalsIgnoreNull(entry.getValue(), value));
  }

  @Override
  public @Nullable VALUE get(@Nullable Object key) {

    if (key != null) {

      Index2d index = computeIndex(key);

      MapEntry<KEY, VALUE> mapEntry = this.map[index.getX()][index.getY()];

      while (mapEntry != null) {
        if (mapEntry.getKey().equals(key)) {
          return mapEntry.getValue();
        }

        mapEntry = mapEntry.next();
      }
    }

    return null;
  }

  @Override
  public Set<Map.Entry<KEY, VALUE>> entrySet() {

    Set<Map.Entry<KEY, VALUE>> entries = new HashSet<>(size());

    for (MapEntry<KEY, VALUE>[] mapEntries : this.map) {
      for (MapEntry<KEY, VALUE> mapEntry : mapEntries) {
        if (mapEntry != null) {
          entries.add(mapEntry.toMapEntry());
        }
      }
    }

    return entries;
  }

  @Override
  public Set<KEY> keySet() {

    Set<KEY> keys = new HashSet<>(size());

    for (MapEntry<KEY, VALUE>[] mapEntries : this.map) {
      for (MapEntry<KEY, VALUE> mapEntry : mapEntries) {
        while (mapEntry != null) {
          keys.add(mapEntry.getKey());
          mapEntry = mapEntry.next();
        }
      }
    }

    return keys;
  }

  @Override
  public @Nullable VALUE put(@NotNull KEY key, @NotNull VALUE value) {

    assertKeyValue(key, value);

    Index2d index = computeIndex(key);

    int x = index.x;
    int y = index.y;

    MapEntry<KEY, VALUE> mapEntry = this.map[x][y];
    MapEntry<KEY, VALUE> pointer = mapEntry;

    while (pointer != null) {
      if (pointer.key.equals(key)) {
        VALUE currentValue = pointer.getValue();
        pointer.setValue(value);
        return currentValue;
      }

      pointer = pointer.next();
    }

    this.map[x][y] = new MapEntry<>(this, key, value)
      .thenNext(mapEntry);

    this.size.incrementAndGet();

    return null;
  }

  @Override
  @SuppressWarnings("all")
  public void putAll(@Nullable Map<? extends KEY, ? extends VALUE> map) {
    MapUtils.nullSafeMap(map).forEach(this::put);
  }

  @Override
  public @Nullable VALUE remove(@NotNull Object key) {

    if (key != null) {

      Index2d index = computeIndex(key);

      int x = index.x;
      int y = index.y;

      MapEntry<KEY, VALUE> mapEntry = this.map[x][y];
      MapEntry<KEY, VALUE> pointer = mapEntry;

      while (pointer != null) {
        if (pointer.key.equals(key)) {
          VALUE value = pointer.getValue();
          if (pointer == mapEntry) {
            this.map[x][y] = pointer.remove();
          }
          return value;
        }

        pointer = pointer.next();
      }
    }

    return null;
  }

  @Override
  public int size() {
    return this.size.get();
  }

  @Override
  public Collection<VALUE> values() {

    List<VALUE> values = new ArrayList<>(size());

    for (MapEntry<KEY, VALUE>[] mapEntries : this.map) {
      for (MapEntry<KEY, VALUE> mapEntry : mapEntries) {
        while (mapEntry != null) {
          values.add(mapEntry.getValue());
          mapEntry = mapEntry.next();
        }
      }
    }

    return values;
  }

  protected static class Index2d {

    protected static @NotNull Index2d of(int x, int y) {

      assertThat(x).describedAs("X [%d] must be greater than equal to 0").isGreaterThanEqualTo(0);
      assertThat(y).describedAs("Y [%d] must be greater than equal to 0").isGreaterThanEqualTo(0);

      return new Index2d(x, y);
    }

    private final int x;
    private final int y;

    private Index2d(int x, int y) {
      this.x = x;
      this.y = y;
    }

    public int getX() {
      return this.x;
    }

    public int getY() {
      return this.y;
    }

    @Override
    public boolean equals(Object obj) {

      if (this == obj) {
        return true;
      }

      if (!(obj instanceof Index2d that)) {
        return false;
      }

      return this.x == that.x
        && this.y == that.y;
    }

    @Override
    public String toString() {
      return String.format("(%d, %d)", getX(), getY());
    }
  }

  protected static class MapEntry<KEY, VALUE> {

    private final ArrayHashMap<KEY, VALUE> owner;

    private final KEY key;

    private VALUE value;

    private MapEntry<KEY, VALUE> next;
    private MapEntry<KEY, VALUE> previous;

    protected MapEntry(@NotNull ArrayHashMap<KEY, VALUE> map, @NotNull KEY key, @NotNull VALUE value) {

      this.owner = ObjectUtils.requireObject(map, "ArrayHashMap owning this MapEntry is required");

      map.assertKeyValue(key, value);

      this.key = key;
      this.value = value;
    }

    protected @NotNull ArrayHashMap<KEY, VALUE> getOwner() {
      return this.owner;
    }

    public @NotNull KEY getKey() {
      return this.key;
    }

    public @Nullable VALUE getValue() {
      return this.value;
    }

    public VALUE setValue(@Nullable VALUE value) {
      VALUE currentValue = this.value;
      this.value = value;
      return currentValue;
    }

    public @Nullable MapEntry<KEY, VALUE> next() {
      return this.next;
    }

    public void setNext(@Nullable MapEntry<KEY, VALUE> next) {

      this.next = next;

      if (next != null) {
        next.setPrevious(this);
      }
    }

    public @Nullable MapEntry<KEY, VALUE> previous() {
      return this.previous;
    }

    void setPrevious(@Nullable MapEntry<KEY, VALUE> previous) {
      this.previous = previous;
    }

    public @Nullable MapEntry<KEY, VALUE> remove() {

      MapEntry<KEY, VALUE> next = next();
      MapEntry<KEY, VALUE> previous = previous();

      if (previous != null) {
        previous.setNext(next);
      }

      setNext(null);
      setPrevious(null);

      return next;
    }

    public @NotNull MapEntry<KEY, VALUE> thenNext(@Nullable MapEntry<KEY, VALUE> next) {
      setNext(next);
      return this;
    }

    public @NotNull Map.Entry<KEY, VALUE> toMapEntry() {

      return new Map.Entry<>() {

        @Override
        public @NotNull KEY getKey() {
          return ArrayHashMap.MapEntry.this.getKey();
        }

        @Override
        public VALUE getValue() {
          return ArrayHashMap.MapEntry.this.getValue();
        }

        @Override
        public VALUE setValue(VALUE value) {
          return ArrayHashMap.MapEntry.this.setValue(value);
        }
      };
    }
  }
}
