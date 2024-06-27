package org.kitchenware.reflect;

public class SetterFactory {

	static final Setter<Boolean> BOOLEAN = new BooleanSetter();
	static final Setter<Byte> BYTE = new ByteSetter();
	static final Setter<Character> CHAR = new CharacterSetter();
	static final Setter<Integer> INTEGER = new IntegerSetter();
	static final Setter<Long> LONG = new LongSetter();
	static final Setter<Short> SHORT = new ShortSetter();
	static final Setter<Float> FLOAT = new FloatSetter();
	static final Setter<Double> DOUBLE = new DoubleSetter();
	static final Setter<Object> OBJECT = new ObjectSetter();
	
	public static final Setter setter(Class type) {
		if(type.isPrimitive()) {
			if(type == Boolean.TYPE) {
				return BOOLEAN;
			}else if(type == Byte.TYPE) {
				return BYTE;
			}else if(type == Character.TYPE) {
				return CHAR;
			}else if(type == Integer.TYPE) {
				return INTEGER;
			}else if(type == Long.TYPE) {
				return LONG;
			}else if(type == Short.TYPE) {
				return SHORT;
			}else if(type == Float.TYPE) {
				return FLOAT;
			}else if(type == Double.TYPE) {
				return DOUBLE;
			}
		}
		return OBJECT;
	}
	
	
	static final class BooleanSetter implements Setter<Boolean>{
		@Override
		public void put(Object target, long offset, Boolean value) {
			InstanceAllocator.putBoolean(target, offset, value);
		}
	}
	
	static final class ByteSetter implements Setter<Byte>{
		@Override
		public void put(Object target, long offset, Byte value) {
			InstanceAllocator.putByte(target, offset, value);
		}
	}
	
	static final class CharacterSetter implements Setter<Character>{
		@Override
		public void put(Object target, long offset, Character value) {
			InstanceAllocator.putCharacter(target, offset, value);
		}
	}
	
	static final class IntegerSetter implements Setter<Integer>{
		@Override
		public void put(Object target, long offset, Integer value) {
			InstanceAllocator.putInteger(target, offset, value);
		}
	}
	
	static final class LongSetter implements Setter<Long>{
		@Override
		public void put(Object target, long offset, Long value) {
			InstanceAllocator.putLong(target, offset, value);
		}
	}
	
	static final class ShortSetter implements Setter<Short>{
		@Override
		public void put(Object target, long offset, Short value) {
			InstanceAllocator.putShort(target, offset, value);
		}
	}
	
	static final class FloatSetter implements Setter<Float>{
		@Override
		public void put(Object target, long offset, Float value) {
			InstanceAllocator.putFloat(target, offset, value);
		}
	}
	
	static final class DoubleSetter implements Setter<Double>{
		@Override
		public void put(Object target, long offset, Double value) {
			InstanceAllocator.putDouble(target, offset, value);
		}
	}
	
	static final class ObjectSetter implements Setter<Object>{
		@Override
		public void put(Object target, long offset, Object value) {
			InstanceAllocator.putObject(target, offset, value);
		}
	}
}
