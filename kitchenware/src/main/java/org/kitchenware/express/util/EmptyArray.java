package org.kitchenware.express.util;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.express.concurrent.ConcurrentLockFactory;
import org.kitchenware.express.concurrent.ConcurrentOptional;

public class EmptyArray {
	public static final String [] STRING = {};
	public static final Integer [] INTEGER = {};
	public static final int [] INT = {};
	public static final Object [] JOBJECT = {};
	

	static EmptyArray instance = new EmptyArray();
	public static EmptyArray getInstance() {
		return instance;
	}
	
	final Map<Class, Object> _context = new ConcurrentHashMap<>();
	final ConcurrentLockFactory LOCKS = new ConcurrentLockFactory();
	
	private EmptyArray() {}
	
	public <T> T [] array(
			@NotNull Class<T> type) {
		return ConcurrentOptional.optional(LOCKS.get(type))
		.ofNullable(() -> (T[]) _context.get(type))
		.orElseGet(()->{
			Object src = Array.newInstance(type, 0);
			_context.put(type, src);
			return (T []) src;
		});
	}
	
}
