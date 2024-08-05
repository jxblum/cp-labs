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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.cp.elements.lang.ObjectUtils;
import org.cp.elements.lang.annotation.NotNull;
import org.cp.elements.util.CollectionUtils;

/**
 * Wrapper around a Java {@link Map}.
 *
 * @author John Blum
 * @see java.util.Map
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class MapWrapper<KEY, VALUE> implements Map<KEY, VALUE> {

  /**
   * Factory method used to wrap the given, existing {@link Map} in a wrapper.
   *
   * @param <KEY> {@link Class type} of the {@link Map} {@literal key}.
   * @param <VALUE> {@link Class type} of the {@link Map} {@literal value}.
   * @param map {@link Map} to wrap; must not be {@literal null}.
   * @return the wrapped {@link Map}.
   * @throws IllegalArgumentException if {@link Map} is {@@literal null}.
   * @see java.util.Map
   */
  public static <KEY, VALUE> MapWrapper<KEY, VALUE> wrap(@NotNull Map<KEY, VALUE> map) {
    return new DefaultMapWrapper<>(map);
  }

  private final Map<KEY, VALUE> map;

  /**
   * Constructs a new {@link MapWrapper} initialized with the given, required {@link Map}.
   *
   * @param map {@link Map} to wrap and decorate; must not be {@literal null}.
   * @throws IllegalArgumentException if {@link Map} is {@literal null}.
   * @see java.util.Map
   */
  public MapWrapper(@NotNull Map<KEY, VALUE> map) {
    this.map = ObjectUtils.requireObject(map, "Map is required");
  }

  /**
   * Gets a reference to the configured, wrapped {@link Map}.
   *
   * @return a reference to the configured, wrapped {@link Map}.
   * @see java.util.Map
   */
  protected @NotNull Map<KEY, VALUE> getMap() {
    return this.map;
  }

  @Override
  public boolean isEmpty() {
    return getMap().isEmpty();
  }

  @Override
  public void clear() {
    getMap().clear();
  }

  @Override
  public boolean containsKey(Object key) {
    return getMap().containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return getMap().containsValue(value);
  }

  @Override
  public @NotNull Set<Entry<KEY, VALUE>> entrySet() {
    return CollectionUtils.nullSafeSet(getMap().entrySet());
  }

  @Override
  public VALUE get(Object key) {
    return getMap().get(key);
  }

  @Override
  public Set<KEY> keySet() {
    return CollectionUtils.nullSafeSet(getMap().keySet());
  }

  @Override
  public VALUE put(KEY key, VALUE value) {
    return getMap().put(key, value);
  }

  @Override
  public void putAll(@NotNull Map<? extends KEY, ? extends VALUE> map) {
    getMap().putAll(map);
  }

  @Override
  public VALUE remove(Object key) {
    return getMap().remove(key);
  }

  @Override
  public int size() {
    return getMap().size();
  }

  @Override
  public Collection<VALUE> values() {
    return CollectionUtils.nullSafeCollection(getMap().values());
  }

  @Override
  @SuppressWarnings("all")
  public boolean equals(Object target) {
    return getMap().equals(target);
  }

  @Override
  public int hashCode() {
    return getMap().hashCode();
  }

  @Override
  public String toString() {
    return getMap().toString();
  }

  protected static class DefaultMapWrapper<KEY, VALUE> extends MapWrapper<KEY, VALUE> {

    protected DefaultMapWrapper(@NotNull Map<KEY, VALUE> map) {
      super(map);
    }
  }
}
