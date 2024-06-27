package org.kitchenware.reflect;

public class GetterFactory {

	static final Getter<Boolean> BOOLEAN = new BooleanGetter();
	static final Getter<Byte> BYTE = new ByteGetter();
	static final Getter<Character> CHAR = new CharacterGetter();
	static final Getter<Integer> INTEGER = new IntegerGetter();
	static final Getter<Long> LONG = new LongGetter();
	static final Getter<Short> SHORT = new ShortGetter();
	static final Getter<Float> FLOAT = new FloatGetter();
	static final Getter<Double> DOUBLE = new DoubleGetter();
	static final Getter<Object> OBJECT = new ObjectGetter();
	
	public static final Getter getter(Class type) {
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
	
	final static class BooleanGetter implements Getter<Boolean>{
		@Override
		public Boolean get(Object target, long offset) {
			return InstanceAllocator.getBoolean(target, offset);
		}
	}
	
	final static class ByteGetter implements Getter<Byte>{
		@Override
		public Byte get(Object target, long offset) {
			return InstanceAllocator.getByte(target, offset);
		}
	}
	
	final static class CharacterGetter implements Getter<Character>{
		@Override
		public Character get(Object target, long offset) {
			return InstanceAllocator.getChar(target, offset);
		}
	}
	
	final static class IntegerGetter implements Getter<Integer> {
		@Override
		public Integer get(Object target, long offset) {
			return InstanceAllocator.getInt(target, offset);
		}
	}
	
	final static class LongGetter implements Getter<Long>{
		@Override
		public Long get(Object target, long offset) {
			return InstanceAllocator.getLong(target, offset);
		}
	}
	
	final static class ShortGetter implements Getter<Short>{

		@Override
		public Short get(Object target, long offset) {
			return InstanceAllocator.getShort(target, offset);
		}
	}
	
	final static class FloatGetter implements Getter<Float>{
		@Override
		public Float get(Object target, long offset) {
			return InstanceAllocator.getFloat(target, offset);
		}
	}
	
	final static class DoubleGetter implements Getter<Double>{
		@Override
		public Double get(Object target, long offset) {
			return InstanceAllocator.getDouble(target, offset);
		}
	}
	
	final static class ObjectGetter implements Getter<Object>{
		@Override
		public Object get(Object target, long offset) {
			return InstanceAllocator.getObject(target, offset);
		}
	}
}
