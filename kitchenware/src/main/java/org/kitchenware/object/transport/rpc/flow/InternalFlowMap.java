package org.kitchenware.object.transport.rpc.flow;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *  
 * @author jerremy
 *
 * @param <K>
 * @param <V>
 */
class InternalFlowMap<K, V> implements Map<K, V>{
	static final Logger logger = Logger.getLogger(InternalFlowMap.class.getName());
	
	Map<Class, Map<Integer, KeyBuff>> entrys = new HashMap<>();
	
	AtomicInteger idGenerator = new AtomicInteger();
	
	short version;
	InternalFlowMap(short version){
		this.version = version;
	}
	@Override
	public int size() {
		return entrys.size();
	}

	@Override
	public boolean isEmpty() {
		return entrys.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		if (key == null) {
			return false;
		}
		
		byte objectType = ObjectFlow.getType(key.getClass(), version);
		int hash = hash(key, objectType);
		return getMap(key.getClass()).containsKey(hash);
	}

	@Override
	public boolean containsValue(Object value) {
		return false;
	}

	@Override
	public V get(Object key) {
		if (key == null) {
			return null;
		}
		
		byte objectType = ObjectFlow.getType(key.getClass(), version);
		int hash = hash(key, objectType);
		
		Map<Integer, KeyBuff> context = getMap(key.getClass());
		
		KeyBuff buff = context.get(hash);
		
		if (buff != null
				&& !assertAccessibleType(key, objectType)
				&& buff.getKey() != key) {
			return null;
		}
		
		return (V) (buff == null ? null : buff.value);
	}

	boolean assertAccessibleType(Object object, int objectType) {
		return objectType < ObjectFlow.OBJECT
				|| (objectType == ObjectFlow.MATH 
						|| objectType == ObjectFlow.DATE
						|| objectType == ObjectFlow.ENUM
						|| objectType == ObjectSerialize.ENUMRATION
						)
				;
	}
	
	int hash(Object object, int objectType) {
		if (assertAccessibleType(object, objectType)) {
			return object.hashCode()
					;
		}else {
			return System.identityHashCode(object);
		}
	}
	
	@Deprecated
	int unsafeHash(Object object) {
		
		return System.identityHashCode(object);
		
//		byte objectType = ToyboxSerializationEngine.getType(object.getClass(), version);
//		if (objectType >= ToyboxSerializationEngine.OBJECT) {
//			if(
//					objectType == ToyboxSerializationEngine.MATH 
//					|| objectType == ToyboxSerializationEngine.DATE
//					|| objectType == ToyboxSerializationEngine.LANG
//					|| objectType == ToyboxSerializationEngine.ENUM
//					) {
//				return object.hashCode();
//			}else {
//				return idGenerator.getAndIncrement();
//			}
//		}else {
//	        return object.hashCode();
//		}
//	
    }
	
	static int hashCodeforString(String src){
			if (src.isEmpty()) {
				return 0;
			}
			byte[] value;
			try {
				value = src.getBytes("utf8");
			} catch (Throwable e) {
				logger.log(Level.WARNING, e.getMessage(), e);
				return 0;
			}
			
	        int h = 0;
            for (int i = 0; i < value.length; i++) {
                h = 31 * h + value[i];
            }
        
	        return h;
	    }
	
	@Override
	public V put(K key, V value) {
		if (key == null || value == null) {
			return null;
		}
		
		byte objectType = ObjectFlow.getType(key.getClass(), version);
		int hash = hash(key, objectType);
		
		Map<Integer, KeyBuff> context = getMap(key.getClass());
		
		KeyBuff buff = context.get(value);
		
		if (buff != null
				&& !assertAccessibleType(key, objectType)
				&& buff != value
				) {
			hash = System.identityHashCode(new Object());
		}
		
		context.put(hash, new KeyBuff(key, value));
		return null;
	}

	@Override
	public V remove(Object key) {
		if (key == null) {
			return null;
		}
		
		byte objectType = ObjectFlow.getType(key.getClass(), version);
		entrys.remove(hash(key, objectType));
		return null;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		throw new UnsupportedOperationException("putAll");
	}

	@Override
	public void clear() {
		entrys.clear();
	}

	@Override
	public Set<K> keySet() {
		Set result = new HashSet<>();
		result.addAll(entrys.values());
		return result;
	}

	@Override
	public Collection<V> values() {
		throw new UnsupportedOperationException("Not support method 'values'");
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		throw new UnsupportedOperationException("Not support method 'entrySet'");
	}
	
	
	@Override
	public String toString() {
		return entrys.toString();
	}
	
	Map<Integer, KeyBuff> getMap(Class type){
		Map<Integer, KeyBuff> map = entrys.get(type);
		if (map == null) {
			entrys.put(type, map = new HashMap<>());
		}
		return map;
	}
	
	class KeyBuff{
		Object key;
		Object value;
		
		KeyBuff(Object key, Object value){
			this.key = key;
			this.value = value;
		}
		
		public Object getKey() {
			return key;
		}
		
		public Object getValue() {
			return value;
		}
	}
}
