package org.kitchenware.express.util;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Iterator;

@SuppressWarnings({ "serial", "unchecked" })
public class ArrayIterator<T> implements Iterator<T>, Serializable {
	Object array;
	int nextIndex = 0;
	int size;

	public ArrayIterator(Object array) {
		this.array = array;
		size = Array.getLength(array);
	}

	public boolean hasNext() {
		return nextIndex < size;
	}

	public T next() {
		return (T) Array.get(array, nextIndex++);
	}

	public void remove() {
		throw new RuntimeException("Illegality Method : 'Remove()'");
	}
}
