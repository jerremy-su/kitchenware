package org.kitchenware.object.transport.rpc.flow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.rmi.MarshalledObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import org.kitchenware.express.util.Errors;
import org.kitchenware.reflect.ArrayMemory;
import org.kitchenware.reflect.basic.ClassDescribe;
import org.kitchenware.reflect.basic.FieldDescribe;

public class ObjectSerialize extends ObjectFlow {
	// final Map<Object, Integer> typeDataMap = new HashMap<Object, Integer>();
	final Map<Class, Integer> classDataMap = new HashMap<Class, Integer>();
	final Map<FieldDescribe, Integer> fieldDataMap = new HashMap<FieldDescribe, Integer>();
	final Map<Object, Integer> objectDataMap;

	// final AtomicInteger typeIndex = new AtomicInteger(0);
	final AtomicInteger objectIndex = new AtomicInteger(0);
	final AtomicInteger classIndex = new AtomicInteger(0);
	final AtomicInteger fieldIndex = new AtomicInteger(0);

	// final ByteArrayOutputStream typeDataOut = new ByteArrayOutputStream();
	final ByteArrayOutputStream classesOut = new ByteArrayOutputStream();
	final ByteArrayOutputStream fieldOut = new ByteArrayOutputStream();
	final ByteArrayOutputStream objectDataOut = new ByteArrayOutputStream();

	final short version;
	
	final Object src;
	final FlowBits bits = new FlowBits();
	public ObjectSerialize(Object src) throws Throwable{
		this(src, ObjectFlow.DEFAULT_VERSION);
	}
	
	public ObjectSerialize(Object src, short version) throws Throwable {
		this.version = version;
		objectDataMap = new InternalFlowMap<Object, Integer>(version);
		this.src = src;
		buildObjectData(src);
	}

	public void writeObject(OutputStream out) throws IOException {
		bits.putShort(out, VERSION_0);
		bits.putShort(out, version);
		int len;
		// type data
		// putInt(out, len = typeDataOut.size());
		// putByteArray(out, typeDataOut.toByteArray());
		// class data
		bits.putInt(out, len = classesOut.size());
		bits.putByteArray(out, classesOut.toByteArray());
		// field data
		bits.putInt(out, len = fieldOut.size());
		bits.putByteArray(out, fieldOut.toByteArray());
		// object src data
		bits.putInt(out, len = objectDataOut.size());
		bits.putByteArray(out, objectDataOut.toByteArray());
	}

	private final Integer buildObjectData(Object src) throws Throwable {
		if (src == null) {
			return -1;
		}
		Class clazz = src.getClass();
		byte type = getType(clazz, this.version);
		Integer objIndex = objectDataMap.get(src);
		if (objIndex == null) {
			objIndex = objectIndex.getAndIncrement();
			objectDataMap.put(src, objIndex);
			// index
			bits.putInt(objectDataOut, objIndex);
			// type
			bits.putByte(objectDataOut, type);
			if (type <= DOUBLE) {
				builTypeData(src, type);
			} else {
				if (type == ARRAY) {
					Class componentType = clazz.getComponentType();
					int componentIndex = builClassType(componentType);
					// array component index
					bits.putInt(objectDataOut, componentIndex);
					int arrLen = Array.getLength(src);
					ArrayMemory memory = new ArrayMemory(clazz, componentType, arrLen);
					// array len
					bits.putInt(objectDataOut, arrLen);
					Object element;
					Integer elementIndex;
					for (int i = 0; i < arrLen; i++) {
//						element = Array.get(src, i);
						element = memory.get(src, i);
						elementIndex = objectDataMap.get(element);
						// next
						boolean next = element == null || elementIndex != null;
						bits.putBoolean(objectDataOut, next);
						if (next) {
							bits.putInt(objectDataOut, element == null ? -1 : elementIndex);
						} else {
							buildObjectData(element);
						}
					}
				} else if (type == OBJECT) {
					// class type
					int classIndex = builClassType(clazz);
					bits.putInt(objectDataOut, classIndex);
					Object fieldObj;
					Integer fieldIndex;
					Integer fieldObjIndex;
					List<FieldDescribe> preparedFields = new ArrayList<>();
					List<Object> preparedFieldValue = new ArrayList<>();
					for (FieldDescribe f : ClassDescribe.getDescribe(clazz).getFields()) {
						fieldObj = f.get(src);
						if (fieldObj == null) {
							continue;
						}
						preparedFields.add(f);
						preparedFieldValue.add(fieldObj);
					}
					// field list size;
					bits.putInt(objectDataOut, preparedFields.size());
					for (int i = 0; i < preparedFields.size(); i ++) {
						FieldDescribe f = preparedFields.get(i);
						
						fieldIndex = fieldDataMap.get(f);
						// field
						bits.putInt(objectDataOut, fieldIndex);

						fieldObj = preparedFieldValue.get(i);
						// next
						fieldObjIndex = objectDataMap.get(fieldObj);

						boolean next = fieldObjIndex != null;
						bits.putBoolean(objectDataOut, next);
						if (next) {
							bits.putInt(objectDataOut, fieldObjIndex);
						} else {
							buildObjectData(fieldObj);
						}

					}
				} else if (type == COLLECTION) {
					src = forceClone(src);
					Collection collection = (Collection) src;
					if (src.getClass().getName().equals("java.util.Arrays$ArrayList")) {
						collection = new ArrayList<>(collection);
					}else if(src.getClass().getName().equals("java.util.HashMap$KeySet")) {
						collection = new LinkedHashSet<>(collection);
					}else if(src.getClass().getName().equals("java.util.LinkedHashMap$LinkedKeySet")) {
						collection = new LinkedHashSet<>(collection);
					}else if(src.getClass().getName().equals("java.util.TreeMap$KeySet")){
						collection = new LinkedHashSet<>(collection);
					}
					
					Object[] data = collection.toArray();
					collection.clear();
					// fill object
					putObject(collection);
					// size
					bits.putInt(objectDataOut, data.length);
					Object collectionElement;
					Integer collectionIndex;
					for (int i = 0; i < data.length; i++) {
						collectionElement = data[i];
						collectionIndex = objectDataMap.get(collectionElement);
						boolean next = collectionElement == null || collectionIndex != null;
						// next
						bits.putBoolean(objectDataOut, next);
						if (next) {
							// next id
							bits.putInt(objectDataOut, collectionElement == null ? -1 : collectionIndex);
						} else {
							// buid object
							buildObjectData(collectionElement);
						}
					}
				} else if (type == MAP) {
					src = forceClone(src);
					Map<Object, Object> map = (Map) src;
					List<Entry<Object, Object>> entrys = new ArrayList<>(map.entrySet());
//					for (Entry<Object, Object> en : map.entrySet()) {
////						if (en.getKey() != null && en.getValue() != null) 
//						{
////							entrys.add(new Object[] { en.getKey(), en.getValue() });
//							entrys.add(en);
//						}
//					}
					map.clear();
					putObject(map);
					// size
					bits.putInt(objectDataOut, entrys.size());
					Integer keyIndex;
					Integer valueIndex;
					for (Entry<Object, Object> entry : entrys) {
						Object key = entry.getKey();
						Object value = entry.getValue();
						
						// key next;
						keyIndex = objectDataMap.get(key);
						
						if (key == null) {
							bits.putBoolean(objectDataOut, true);
							bits.putInt(objectDataOut, -1);
						}else {
							boolean keyNext = keyIndex != null;
							bits.putBoolean(objectDataOut, keyNext);
							// key data
							if (keyNext) {
								bits.putInt(objectDataOut, keyIndex);
							} else {
								buildObjectData(key);
							}
						}
						
						
						if (value == null) {
							bits.putBoolean(objectDataOut, true);
							bits.putInt(objectDataOut, -1);
						}else {
							// value next;
							valueIndex = objectDataMap.get(value);
							boolean valueNext = valueIndex != null;
							bits.putBoolean(objectDataOut, valueNext);
							// value data
							if (valueNext) {
								bits.putInt(objectDataOut, valueIndex);
							} else {
								buildObjectData(value);
							}
						}
					
					}
				} else if (type == UTFSTRING) {
					byte[] buf = ((String) src).getBytes("utf8");
					bits.putInt(objectDataOut, buf.length);
					bits.putByteArray(objectDataOut, buf);
				} else if(type == TIMEZONE) { 
					String zoneId = ((TimeZone)src).getID();
					writeObjectField(objectDataOut, zoneId);
				} else if(type == STRING_BUILDER) {
					String stringBuliderValue = src.toString();
					writeObjectField(objectDataOut, stringBuliderValue);
				} else if(type == STRING_BUFFER) {
					String stringBufferValue = src.toString();
					writeObjectField(objectDataOut, stringBufferValue);
				}
				
				else if (type == CLASS) {
					bits.putInt(objectDataOut, builClassType((Class) src));
					/** ------------------E BUG1965(Jerremy 2018.04.08)------------------ */
				} else if(type == ENUMRATION) {

					bits.putInt(objectDataOut, builClassType(src.getClass()));
					
					String enumNamingSrc = String.valueOf(src);
					Integer namingIndex = objectDataMap.get(enumNamingSrc);
					boolean namingNext = namingIndex != null;
					bits.putBoolean(objectDataOut, namingNext);
					if(namingIndex == null) {
						buildObjectData(enumNamingSrc);
					}else {
						bits.putInt(objectDataOut, namingIndex);
					}
					
				
				}else if (type == THROWABLE || type == ENUM || type == MATH || type == LANG || type == DATE
						|| type == UNSUPPORT_PACKAGE/** BUG2156(Jerremy 2018.05.04) */
				) {
					putObject(src);
				}
			}
		}

		return objIndex;
	}

	private void writeObjectField(OutputStream out, String src) throws Throwable{
		Integer index = objectDataMap.get(src);
		if(index == null) {
			putInt(out, -1);
			buildObjectData(src);
		}else {
			putInt(out, index);
		}
	}
	
	private void putObject(Object src) throws Throwable {
		try {
			ByteArrayOutputStream buffOut = new ByteArrayOutputStream();
			ObjectOutput oo = new ObjectOutputStream(buffOut);
			oo.writeObject(new MarshalledObject(src));
			// len
			bits.putInt(objectDataOut, buffOut.size());
			bits.putByteArray(objectDataOut, buffOut.toByteArray());
		} catch (Throwable e) {
			throw e;
		}
		
	}

	private final int builClassType(Class clazz) throws Throwable {
		Integer index = classDataMap.get(clazz);
		byte type = getType(clazz, this.version);
		if (index == null) {
			classDataMap.put(clazz, index = classIndex.getAndIncrement());
			byte[] classNameBuf = clazz.getName().getBytes("utf8");
			// index
			bits.putInt(classesOut, index);
			// name len
			bits.putInt(classesOut, classNameBuf.length);
			// name src
			bits.putByteArray(classesOut, classNameBuf);
		}
		if (type == OBJECT) {
			for (FieldDescribe f : ClassDescribe.getDescribe(clazz).getFields()) {
				builField(index, f);
			}
		}
		return index;
	}

	private final int builField(int classIndex, FieldDescribe f) throws Throwable {
		Integer index = fieldDataMap.get(f);
		if (index == null) {
			fieldDataMap.put(f, index = fieldIndex.getAndIncrement());
			// index
			bits.putInt(fieldOut, index);
			// class ref
			bits.putInt(fieldOut, classIndex);

			byte[] nameBuf = f.getField().getName().getBytes("utf8");
			// name len
			bits.putInt(fieldOut, nameBuf.length);
			// name src
			bits.putByteArray(fieldOut, nameBuf);
		}
		return index;
	}

	private final void builTypeData(Object src, byte type) throws Throwable {
		switch (type) {
		case BYTE:
			bits.putByte(objectDataOut, (Byte) src);
			break;
		case BOOLEAN:
			bits.putBoolean(objectDataOut, (Boolean) src);
			break;
		case SHORT:
			bits.putShort(objectDataOut, (Short) src);
			break;
		case INT:
			bits.putInt(objectDataOut, (Integer) src);
			break;
		case CHAR:
			bits.putChar(objectDataOut, (Character) src);
			break;
		case FLOAT:
			bits.putFloat(objectDataOut, (Float) src);
			break;
		case LONG:
			bits.putLong(objectDataOut, (Long) src);
			break;
		case DOUBLE:
			bits.putDouble(objectDataOut, (Double) src);
			break;
		default:
			break;
		}

	}
	
	
	public static byte [] serialize(Object o) throws IOException{
		try {
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			new ObjectSerialize(o).writeObject(buf);
			return buf.toByteArray();
		} catch (Throwable e) {
			throw Errors.throwIOException(e);
		}
	}
}
