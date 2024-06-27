package org.kitchenware.reflect;

import java.lang.reflect.Array;

import org.kitchenware.express.annotation.NotNull;

public class ArrayMemory {

	final Class type;
	final Class componentType;
	
	final long scale;
	final long offset;
	
	final Getter getter;
	final Setter setter;
	final int length;
	
	public ArrayMemory(Class type, Class componentType, int length) {
		this.type = type;
		this.componentType = componentType;
		this.length = length;
		this.scale = InstanceAllocator.arrayIndexScale(type);
		this.offset = InstanceAllocator.arrayBaseOffset(type);
		
		this.getter = GetterFactory.getter(componentType);
		this.setter = SetterFactory.setter(componentType);
	}
	
	public Object get(Object target, int  i) {
		long offset = offset(i);
		return this.getter.get(target, offset);
	}
	
	public void set(Object target, int i, Object value) {
		long offset = offset(i);
		this.setter.put(target, offset, value);
	}
	
	public long offset(int i) {
		if(i >= this.length || i < 0) {
			throw new ArrayIndexOutOfBoundsException(i);
		}
		long offset = this.offset + (this.scale * i);
		return offset;
	}
	
	public int getLength() {
		return length;
	}
	
	public static ArrayMemory valueOf(@NotNull Object array) {
		if(array == null) {
			return null;
		}
		Class type = array.getClass();
		if(!type.isArray()) {
			return null;
		}
		Class componentType = type.getComponentType();
		int length = Array.getLength(array);
		return new ArrayMemory(type, componentType, length);
	}
}
