package org.kitchenware.unsafe;

import java.lang.reflect.Field;

public class Unsafe {
	static final sun.misc.Unsafe unsafe;
	static{
		try {
			Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			unsafe = (sun.misc.Unsafe)f.get(null);
		} catch (Throwable e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	public static sun.misc.Unsafe getUnsafe() {
		return unsafe;
	}
}
