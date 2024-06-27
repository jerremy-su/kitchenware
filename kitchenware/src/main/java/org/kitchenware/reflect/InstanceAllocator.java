package org.kitchenware.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.kitchenware.express.annotation.NotNull;

import sun.misc.Unsafe;

/**
 * jerremy.su 2022-03-20 14:57:43
 * @author jerremy.su
 *
 */
public class InstanceAllocator {

	static final Unsafe unsafe;
	static{
		try {
			Field f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			unsafe = (Unsafe)f.get(null);
		} catch (Throwable e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	public static <T> T newInstance(@NotNull Class<T> type) {
		try {
			return (T) unsafe.allocateInstance(type);
		} catch (Throwable e) {
			try {
				return (T) type.newInstance();
			} catch (Throwable e2) {
				String err = String.format("Could not build instance for type: %s; Error: %s", type.getName(), e2.getMessage());
				throw new RuntimeException(err, e2);
			}
		}
	}
	
	public static <T> T allocate(Class<T> type) throws InstantiationException {
		return (T) unsafe.allocateInstance(type);
	}
	
	public static <T> T allocate(Constructor<T> constructor, Object [] args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if(args == null || args.length < 1) {
			try {
				return (T)allocate(constructor.getDeclaringClass());
			} catch (Exception e) {}
		}
		
		return constructor.newInstance(args);
	}
	
	public static long fieldOffSet(Field field) {
		  if (Modifier.isStatic(field.getModifiers())) {
	            return unsafe.staticFieldOffset(field);
		  }else {
	            return unsafe.objectFieldOffset(field);
		  }
	}
	
	public static void putBoolean(Object target, long offset, boolean value) {
		unsafe.putBoolean(target, offset, value);
	}
	
	public static void putByte(Object target, long offset, byte value) {
		unsafe.putByte(target, offset, value);
	}
	
	public static void putCharacter(Object target, long offset, char value) {
		unsafe.putChar(target, offset, value);
	}
	
	public static void putInteger(Object target, long offset, int value) {
		unsafe.putInt(target, offset, value);
	}
	
	public static void putLong(Object target, long offset, long value) {
		unsafe.putLong(target, offset, value);
	}
	
	public static void putShort(Object target, long offset, short value) {
		unsafe.putShort(target, offset, value);
	}
	
	public static void putFloat(Object target, long offset, float value) {
		unsafe.putFloat(target, offset, value);
	}
	
	public static void putDouble(Object target, long offset, double value) {
		unsafe.putDouble(target, offset, value);
	}
	
	public static void putObject(Object target, long offset, Object value) {
		unsafe.putObject(target, offset, value);
	}
	
	public static boolean getBoolean(Object target, long offset) {
		return unsafe.getBoolean(target, offset);
	}
	
	public static byte getByte(Object target, long offset) {
		return unsafe.getByte(target, offset);
	}
	
	public static char getChar(Object target, long offset) {
		return unsafe.getChar(target, offset	);
	}
	
	public static int getInt(Object target, long offset) {
		return unsafe.getInt(target, offset);
	}
	
	public static long getLong(Object target, long offset) {
		return unsafe.getLong(target, offset);
	}
	
	public static short getShort(Object target, long offset) {
		return unsafe.getShort(target, offset);
	}
	
	public static float getFloat(Object target, long offset) {
		return unsafe.getFloat(target, offset);
	}
	
	public static double getDouble(Object target, long offset) {
		return unsafe.getDouble(target, offset);
	}
	
	public static Object getObject(Object target, long offset) {
		return unsafe.getObject(target, offset);
	}
	
	public static long arrayIndexScale(Class type) {
		return unsafe.arrayIndexScale(type);
	}
	
	public static long arrayBaseOffset(Class type) {
		return unsafe.arrayBaseOffset(type);
	}
}
