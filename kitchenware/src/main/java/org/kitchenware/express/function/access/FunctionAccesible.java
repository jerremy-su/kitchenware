package org.kitchenware.express.function.access;

@FunctionalInterface
public interface FunctionAccesible<T> {
	
	T access() throws Throwable;
	
}
