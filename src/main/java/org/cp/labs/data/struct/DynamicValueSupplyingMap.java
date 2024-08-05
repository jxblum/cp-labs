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
package org.cp.labs.data.struct;

import static org.cp.elements.lang.RuntimeExceptionsFactory.newUnsupportedOperationException;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.cp.elements.lang.CodeBlocks;
import org.cp.elements.lang.ObjectUtils;
import org.cp.elements.lang.annotation.NotNull;

/**
 * {@link AbstractMap} implementation that dynamically computes {@link Supplier supplies}) values
 * each time {@link Map#get(Object) Map.get(key)} is called.
 *
 * @author John Blum
 * @see java.util.AbstractMap
 * @see java.util.Map
 */
@SuppressWarnings("unused")
public class DynamicValueSupplyingMap<KEY, VALUE> extends AbstractMap<KEY, VALUE> {

	public static <KEY, VALUE> DynamicValueSupplyingMap<KEY, VALUE> create() {
		return new DynamicValueSupplyingMap<>();
	}

	private final Map<KEY, Supplier<VALUE>> map = new HashMap<>();

	protected Map<KEY, Supplier<VALUE>> getMap() {
		return this.map;
	}

	@Override
	public VALUE get(Object key) {
		return CodeBlocks.ifElse(getMap().get(key), Objects::nonNull, Supplier::get, supplier -> null);
	}

	public DynamicValueSupplyingMap<KEY, VALUE> put(KEY key, Supplier<VALUE> value) {
		getMap().put(key, value);
		return this;
	}

	@Override
	public @NotNull Set<Entry<KEY, VALUE>> entrySet() {

		return getMap().entrySet().stream()
			.map(DynamicValueSupplyingMapEntry::wrap)
			.collect(Collectors.toSet());
	}

	protected static class DynamicValueSupplyingMapEntry<KEY, VALUE> implements Map.Entry<KEY, VALUE> {

		protected static <KEY, VALUE> Map.Entry<KEY, VALUE> wrap(Map.Entry<KEY, Supplier<VALUE>> mapEntry) {
			return new DynamicValueSupplyingMapEntry<>(mapEntry);
		}

		private final KEY key;
		private final Supplier<VALUE> valueSupplier;

		protected DynamicValueSupplyingMapEntry(KEY key, Supplier<VALUE> valueSupplier) {
			this.key = ObjectUtils.requireObject(key, "KEY is required");
			this.valueSupplier = ObjectUtils.requireObject(valueSupplier, "VALUE Supplier is required");
		}

		protected DynamicValueSupplyingMapEntry(Map.Entry<KEY, Supplier<VALUE>> mapEntry) {
			this(mapEntry.getKey(), mapEntry.getValue());
		}

		@Override
		public KEY getKey() {
			return this.key;
		}

		@Override
		public VALUE getValue() {
			return this.valueSupplier.get();
		}

		@Override
		public VALUE setValue(VALUE value) {
			throw newUnsupportedOperationException("Setting value of K#Y [%s] is not supported".formatted(getKey()));
		}
	}
}
