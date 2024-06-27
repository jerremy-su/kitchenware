package org.kitchenware.object.transport.rpc.flow;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.rmi.MarshalledObject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kitchenware.express.util.Errors;
import org.kitchenware.reflect.ArrayMemory;


public class ObjectDeserialize extends ObjectFlow{
	static final Logger logger = Logger.getLogger(ObjectDeserialize.class.getName());
	
	final Map<Integer, Class> classDataMap = new HashMap<>();
	final Map<Integer, FieldFlow> fieldDataMap = new HashMap<>();
	final Map<Integer, Object> objectDataMap = new HashMap<>();
	
	Object obj;
	
	final short version;
	
	final FlowBits bits = new FlowBits();
	public ObjectDeserialize(InputStream in) throws Throwable{
		if(bits.getShort(in) != VERSION_0 ) {
			throw new IllegalAccessException("Not a toybox marshaller.");
		}
		this.version = bits.getShort(in);
		if (
				version != VERSION_POS
				&& version != VERSION_A1
				&& version != VERSION_A2
				) {
			throw new IllegalAccessException("Not a toybox marshaller.");
		}
		int len;
		byte [] buf;
		//type data
//		{
//			len = getInt(in);
//			buf = new byte [len];
//			in.read(buf);
//			decodeTypeDatas(new ByteArrayInputStream(buf));
//		}
		//class data
		{
			len = bits.getInt(in);
			buf = new byte [len];
			in.read(buf);
			decodeClassData(new ByteArrayInputStream(buf));
		}
		//field data
		{
			len = bits.getInt(in);
			buf = new byte [len];
			in.read(buf);
			decodeFieldData(new ByteArrayInputStream(buf));
		}
		//object data
		{
			len = bits.getInt(in);
			if(len > 0) {
				buf = new byte [len];
				in.read(buf);
				obj = readObjectData(new ByteArrayInputStream(buf));
			}
		}
	}
	
	public <T> T readObject(){
		return (T) obj;
	}
	
	private Object readObjectData(InputStream in) throws Throwable{
		//index
		int index = bits.getInt(in);
		if (index == -1) {
			return null;
		}
		
		Object result = objectDataMap.get(index);
		if (result != null) {
			return result;
		}
		//type
		byte type = bits.getByte(in);
		if (type <= DOUBLE) {
			result = decodeTypeData(in, type);
			objectDataMap.put(index, result);
		}else{
			if(type == ARRAY) {
				//array component index
				int componentIndex = bits.getInt(in);
				Class componentType = classDataMap.get(componentIndex);
				//array len
				int arrLen = bits.getInt(in);
				result = Array.newInstance(componentType, arrLen);
				ArrayMemory memory = new ArrayMemory(result.getClass(), componentType, arrLen);
				objectDataMap.put(index, result);
				int elementIndex;
				Object element;
				for (int i = 0; i < arrLen; i++) {
					boolean next = bits.getBoolean(in);
					if (next) {
						elementIndex = bits.getInt(in);
						element = objectDataMap.get(elementIndex);
					}else{
						element = readObjectData(in);
					}
					try {
//						Array.set(result, i, element);
						memory.set(result, i, element);
					} catch (Throwable e) {
						throw Errors.throwRuntimeable(e);
					}
				}
			}else if (type == OBJECT) {
				int classIndex = bits.getInt(in);
				Class objectType = classDataMap.get(classIndex);
				ClassFlow md = getToyBoxMetadata(objectType);
//				result = md.newInstance();
				result = null;
				if(md != null) {
					result = md.unsafeNewInstance();
				}
				
				if(result != null) {
					objectDataMap.put(index, result);
				}
				//field list size;
				int fieldListSize = bits.getInt(in);
				Object fieldObj;
				Integer fieldIndex;
				Integer fieldObjIndex;
				FieldFlow field;
				for (int i = 0; i < fieldListSize; i++) {
					//field
					fieldIndex = bits.getInt(in);
					field = fieldDataMap.get(fieldIndex);
					
					//next
					boolean next = bits.getBoolean(in);
					fieldObj = null;
					if (next) {
						fieldObjIndex = bits.getInt(in);
						fieldObj = objectDataMap.get(fieldObjIndex);
					}else{
						fieldObj = readObjectData(in);
					}
					
					if (field != null) {
						try {
							field.set(result, fieldObj);
						} catch (Throwable e) {
							logger.logp(Level.WARNING, getClass().getName(), "readObjectData", 
									String.format("[%s] : cannot set field value [%s]"
											, objectType.getName(), field));
							
							throw Errors.throwRuntimeable(e);
						}
						
					}
				}
			}else if(type == COLLECTION){
				//class index
				Collection collection = (Collection) (result = readObject(in)); 
				objectDataMap.put(index, result);
				//size
				Integer size = bits.getInt(in);
				Integer collectionIndex;
				Object elementObject;
				for (int i = 0; i < size; i++) {
					boolean next = bits.getBoolean(in);
					if (next) {
						collectionIndex = bits.getInt(in);
						collection.add(objectDataMap.get(collectionIndex));
					}else{
						elementObject = readObjectData(in);
						collection.add(elementObject);
					}
				}
			}else if(type == MAP){
				Map map = (Map) (result = readObject(in));
				
				//jerremy.su 2020-08-19 17:47:11
				boolean notSupportNullKey = 
						Hashtable.class.isInstance(map)
						;
				
				objectDataMap.put(index, result);
				//size
				int size = bits.getInt(in);
				Object keyObj;
				Object valueObj;
				for (int i = 0; i < size; i++) {
					//key next
					boolean keyNext = bits.getBoolean(in);
					//key data
					if (keyNext) {
						keyObj = objectDataMap.get(bits.getInt(in));
					}else{
						keyObj = readObjectData(in);
					}
					
					//value next
					boolean valueNext = bits.getBoolean(in);
					//value data
					if (valueNext) {
						valueObj = objectDataMap.get(bits.getInt(in));
					}else{
						valueObj = readObjectData(in);
					}
					
					if(keyObj == null && notSupportNullKey) {
						// jerremy.su 2020-08-19 17:47:11 部分枚举新旧兼容无法正常序列化,null处理,某些map实现会报错
						continue;
					}
					
					try {
						map.put(keyObj, valueObj);
					} catch (Throwable e) {
						throw Errors.throwRuntimeable(e);
					}
				}
			}else if(type == UTFSTRING){
				byte [] buf = new byte [bits.getInt(in)];
				in.read(buf);
				objectDataMap.put(index, result = new String(buf, "utf8"));
			}else if(type == TIMEZONE) {
				String zoneId = readObjectField(in);
				TimeZone zone = TimeZone.getTimeZone(zoneId);
				objectDataMap.put(index, result = zone);
			}else if(type == STRING_BUILDER) {
				String stringBuilderValue = readObjectField(in);
				StringBuilder stringBuilder = new StringBuilder(stringBuilderValue);
				objectDataMap.put(index, result = stringBuilder);
			}else if(type == STRING_BUFFER) {
				String stringBufferValue = readObjectField(in);
				StringBuffer stringBuffer = new StringBuffer(stringBufferValue);
				objectDataMap.put(index, result = stringBuffer);
			}
			
			else if(type == CLASS){
				objectDataMap.put(index, result = classDataMap.get(bits.getInt(in)));
				/**------------------E BUG1965(Jerremy 2018.04.08)------------------*/
			}else if(type == ENUMRATION) {

				Class enumrationType = classDataMap.get(bits.getInt(in));
				boolean namingNext = bits.getBoolean(in);
				String enumNamingSrc;
				if(namingNext) {
					enumNamingSrc = (String) objectDataMap.get(bits.getInt(in));
				}else {
					enumNamingSrc = (String)readObjectData(in);
				}

				//
				try {
					
					result = Enum.valueOf(enumrationType, enumNamingSrc);
				} catch (Throwable e) {
					
					String err = String.format("Enumeration Type[%s]: Constanst value not bound [%s]", enumrationType, enumNamingSrc);
					logger.log(Level.WARNING, err, e);
					
					result = null;
				}
				objectDataMap.put(index, result);
			
			}	else if(type == THROWABLE 
					|| type == ENUM
					|| type == MATH
					|| type == LANG
					|| type == DATE
					|| type == UNSUPPORT_PACKAGE/**BUG2156(Jerremy 2018.05.04)*/
					){
				result = readObject(in);
				objectDataMap.put(index, result);
			}
		}
		return result;
	}
	
	private <T> T readObjectField(InputStream in) throws Throwable{
		int index = bits.getInt(in);
		Object src;
		if(index < 0) {
			src = readObjectData(in);
		}else {
			src = this.objectDataMap.get(index);
		}
		return (T)src;
	}
	
	private Object readObject(InputStream in) throws Throwable{
		//len
		byte [] buf = new byte [bits.getInt(in)];
		in.read(buf);
		MarshalledObject src = (MarshalledObject) new ObjectInputStream(new ByteArrayInputStream(buf)).readObject();
		return src.get();
	}
	
	private void decodeFieldData(InputStream in) throws Throwable{
		while(in.available() > 0){
			Integer index = bits.getInt(in);
			Integer classid = bits.getInt(in);
			
			ClassFlow md = null;
			Class type = classDataMap.get(classid);
			if(type != null) {
				md = ObjectFlow.getToyBoxMetadata(classDataMap.get(classid));
			}
			byte [] buf = new byte [bits.getInt(in)];
			in.read(buf);
			
			if(md != null) {
				FieldFlow f = md.getField(new String(buf, "utf8"));
				if(f != null){
					fieldDataMap.put(index, f);
				}
			}
		}
	}
	
	private void decodeClassData(InputStream in) throws Throwable{
		while(in.available() > 0){
			Integer index = bits.getInt(in);
			byte [] buf = new byte [bits.getInt(in)];
			in.read(buf);
			try {
				classDataMap.put(index, loadClass(new String (buf, "utf8")));
			} catch (Throwable e) {
				//jerremy.su 忽略当前程序不存在的类,作新旧兼容处理
			}
		}
	}
	
	private final Object decodeTypeData(InputStream in, byte type) throws Throwable{
		switch (type) {
		case BYTE:
			return bits.getByte(in);
		case BOOLEAN:
			return bits.getBoolean(in);
		case SHORT:
			return bits.getShort(in);
		case INT:
			return bits.getInt(in);
		case CHAR:
			return bits.getChar(in);
		case FLOAT:
			return bits.getFloat(in);
		case LONG:
			return bits.getLong(in);
		case DOUBLE:
			return bits.getDouble(in);
		default:
			return null;
		}
	}
	
	public static <T> T deserialize(byte [] src) throws IOException{
		try {
			return new ObjectDeserialize(new ByteArrayInputStream(src))
					.readObject();
		} catch (Throwable e) {
			throw Errors.throwIOException(e);
		}
	}
	
	public short getVersion() {
		return version;
	}
}
