package org.kitchenware.reflect;

@FunctionalInterface
public interface Setter<V> {

	void put(Object target, long offset, V value);
}
