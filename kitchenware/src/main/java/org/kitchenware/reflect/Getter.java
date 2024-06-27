package org.kitchenware.reflect;

@FunctionalInterface
public interface Getter<R> {

	R get(Object target, long offset);
}
