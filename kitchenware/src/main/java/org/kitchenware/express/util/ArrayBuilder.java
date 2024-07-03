package org.kitchenware.express.util;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jerremy
 * 
 * @param <T>
 */
@SuppressWarnings({ "serial", "unchecked" })
public class ArrayBuilder<T> implements Iterable<T>, Serializable {
	
	static final Logger LOGGER = Logger.getLogger(ArrayBuilder.class.getName());
	
	Object array;

	public ArrayBuilder(Class<?> type, int length) {
		array = Array.newInstance(type, length);
	}

	public ArrayBuilder(Object array) throws Exception {
		if (!array.getClass().isArray()) {
			throw new ClassCastException(String.format(
					"%s Can not cast to Array", array.getClass().getName()));
		}
		this.array = array;
	}

	public T get(int index) {
		if(index < 0 || index >= this.size()) {
			return null;
		}
		return (T) Array.get(array, index);
	}

	public void put(int index, T t) {
		Array.set(array, index, t);
	}

	/**
	 * 
	 * @param t
	 */
	public synchronized ArrayBuilder push(T t) {
		Object temp = Array.newInstance(getType(), size() + 1);
		System.arraycopy(array, 0, temp, 1, Array.getLength(temp) - 1);
		Array.set(temp, 0, t);
		array = temp;
		return ArrayBuilder.this;
	}

	public synchronized ArrayBuilder add(T t) {
		Object temp = Array.newInstance(getType(), size() + 1);
		System.arraycopy(array, 0, temp, 0, Array.getLength(temp) - 1);
		int index = Math.max(0, Array.getLength(temp) - 1);
		Array.set(temp, index, t);
		array = temp;
		return ArrayBuilder.this;
	}

	public synchronized ArrayBuilder insert(int index, T t) {
		Object temp = Array.newInstance(getType(), size() + 1);
		System.arraycopy(array, 0, temp, 0, Array.getLength(temp) - 1);
		System.arraycopy(array, index, temp, index + 1, size() - index);
		Array.set(temp, index, t);
		array = temp;
		return ArrayBuilder.this;
	}

	/**
	 * 
	 * @return
	 */
	public int size() {
		return Array.getLength(array);
	}

	/**
	 * �ж������Ƿ���ֵ
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return size() < 1;
	}

	/**
	 * 
	 * @return
	 */
	public Class<?> getType() {
		return array.getClass().getComponentType();
	}

	/**
	 * 
	 * @param <A>
	 * @return
	 */
	public T[] toArray() {
		return (T[]) array;
	}
	
	public <E> E [] toArray(Class<E> type) {
		Object result = Array.newInstance(type, size());
		for(int i = 0; i < this.size(); i ++) {
			Array.set(result, i, this.get(i));
		}
		return (E[]) result;
	}

	public Object asArray() {
		return array;
	}
	
	public List<T> toArrayList() {
		return Arrays.asList(toArray());
	}

	public boolean equals(ArrayBuilder arg) {
		if (size() != arg.size())
			return false;
		for (int i = 0; i < size(); i++) {
			if (get(i) == null && arg.get(i) != null) {
				return false;
			} else if (get(i) != null && arg.get(i) == null) {
				return false;
			} else if (!get(i).equals(arg.get(i))) {
				return false;
			}
		}
		return true;
	}

	public Iterator<T> iterator() {
		return new ArrayIterator<T>(array);
	}

	public String buildString(String split, String mark) {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < size(); i++) {
			if (i > 0) {
				buff.append(split);
			}
			buff.append(mark).append(get(i)).append(mark);
		}
		return buff.toString();
	}

	public String toString() {
		return buildString(",", "");
	}

	public ArrayBuilder<T> copy() {
		return createArrayEntry(array);
	}

	public synchronized void addAll(Collection<T> collection) {
		for (T t : collection) {
			add(t);
		}
	}
	
	public synchronized void addAll(Object [] elements) {
		for (Object t : elements) {
			add((T)t);
		}
	}

	public synchronized void remove(Object e) {
		int idx = indexOf(e);
		if(idx < 0) {
			return;
		}
		
		remove(idx);
	}
	
	public synchronized void remove(int index) {
		if (index >= size()) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		Object temp = Array.newInstance(getType(), size() - 1);
		System.arraycopy(array, 0, temp, 0, index);
		System.arraycopy(array, index + 1, temp, index, (size() - (index + 1)));
		array = temp;
	}

	public synchronized void clear() {
		array = Array.newInstance(getType(), 0);
	}

	public void positionExchange(Object from, Object to) {
		int fromIndex = indexOf(from);
		if(fromIndex < 0) {
			return;
		}
		
		int toIndex = indexOf(to);
		if(toIndex < 0) {
			return;
		}
		
		Array.set(this.array, fromIndex, to);
		Array.set(this.array, toIndex, from);
	}
	
	public void positionExchange(int fromIndex, int toIndex) {
		Object from = Array.get(this.array, fromIndex);
		Object to = Array.get(this.array, toIndex);
		
		Array.set(this.array, fromIndex, to);
		Array.set(this.array, toIndex, from);
	}
	
	public int indexOf(Object o) {
		int size = size();
		for (int i = 0; i < size; i++) {
			if (Objects.equals(get(i), o))
				return i;
		}
		return -1;
	}

	public boolean contains(T t) {

		boolean result = indexOf(t) >= 0;
		
		return result;
		
	}

	public boolean addAll(ArrayBuilder<T> basicArray) {
		Object a = basicArray.array;
		int numNew = basicArray.size();
		int size = size();
		int len = size + numNew;
		
		Object tmp = Array.newInstance(getType(), len);
		System.arraycopy(array, 0, tmp, 0, size);
		array = tmp;
		System.arraycopy(a, 0, array, size, numNew);
		return numNew != 0;
	}

	/**
	 * 
	 * @param <T>
	 * @param array
	 * @return
	 */
	public static <T> ArrayBuilder<T> createArrayEntry(Object array) {
		return createArrayEntry(array, null);
	}

	/**
	 * 
	 * @param <T>
	 * @param array
	 * @param clazz
	 * @return
	 */
	public static <T> ArrayBuilder<T> createArrayEntry(Object array,
			Class<T> clazz) {
		ArrayBuilder builder = null;
		try {
			builder = new ArrayBuilder<T>(array);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
		return builder;
	}

	/**
	 * ����һ���̶�����,�̶����ȵ����鴦����
	 * 
	 * @param <T>
	 * @param type
	 * @param length
	 * @return
	 */
	public static <T> ArrayBuilder<T> newArrayEntry(Class<T> type, int length) {
		return new ArrayBuilder<T>(type, length);
	}

	/**
	 * ����һ������Ϊ0���̶����͵����鴦����
	 * 
	 * @param <T>
	 * @param type
	 * @return
	 */
	public static <T> ArrayBuilder<T> newArrayEntry(Class<T> type) {
		return new ArrayBuilder<T>(type, 0);
	}
}
