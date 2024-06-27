package org.kitchenware.object.transport.rpc.flow;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.reflect.ClassBufferedFactory;

public abstract class ObjectFlow {
	
	static final String mathPackage = "java.math";
	static final String langPackage = "java.lang";
	
	public static final short VERSION_0 = 0x01;
	public static final short VERSION_POS = 0xff;
	public static final short VERSION_A1 = 0xA1;
	
	/**
	 * JDK17 兼容性修改
	 * jerremy.su 2022-10-15 00:40:45
	 */
	public static final short VERSION_A2 = 0xA2;
	
	// jerremy.su 2022-10-15 00:43:01 更新版本号到 VERSION_A2
	public static final short DEFAULT_VERSION = VERSION_A2;
//	public static final short DEFAULT_VERSION = 0xA1;
//	public static short DEFAULT_VERSION = 
//			"true".equalsIgnoreCase(System.getProperty("version_pos")) ?
//					VERSION_POS
//					: VERSION_A1
//					;
	
	private static Method CLONE_METHOD;
	static{
		try {
			CLONE_METHOD = Object.class.getDeclaredMethod("clone");
			CLONE_METHOD.setAccessible(true);
		} catch (Throwable e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	public static Object forceClone(Object src) throws Throwable{
		if (src instanceof Cloneable) {
			return CLONE_METHOD.invoke(src);
		}else{
			return src;
		}
	}
	
//	static final Map<String, Class> basicClassContext = new LinkedHashMap<String, Class>();
//	static final Map<String, Class> classBufferedContext = new LinkedHashMap<String, Class>();
//	static final Class [] standardJAVATypes = {
//		byte.class, boolean.class, 
//		short.class, int.class, char.class,
//		float.class, long.class, double.class
//		,
//		Byte.class, Boolean.class, 
//		Short.class, Integer.class, Character.class,
//		Float.class, Long.class, Double.class
//	};
	
//	static{
//		for (Class c : standardJAVATypes) {
//			basicClassContext.put(c.getName(), c);
//		}
//	}
	/**------------------B BUG2156(Jerremy 2018.05.04)------------------*/
	static final String [] unsupportPackages;
	static {
		unsupportPackages = new String [] {
				"javax.sql"
				, "java.net"
				};
	}
	
	/**------------------E BUG2156(Jerremy 2018.05.04)------------------*/
	public static final byte BYTE = 0x00;
	public static final byte BOOLEAN = 0x01;
	public static final byte SHORT = 0x02;
	public static final byte INT = 0x03;
	public static final byte CHAR = 0x04;
	public static final byte FLOAT = 0x05;
	public static final byte LONG = 0x06;
	public static final byte DOUBLE = 0x07;
	
	public static final byte OBJECT = 0x10;
	public static final byte ARRAY = 0x11;
	
	@Deprecated
	public static final byte ENUM = 0x12;
	
	public static final byte UTFSTRING = 0x13;
	public static final byte CLASS = 0x14;/**BUG1965(Jerremy 2018.04.08)*/
	public static final byte ENUMRATION = 0x15;
	
	
	public static final byte COLLECTION = 0x20;
	public static final byte MAP = 0x21;
	public static final byte THROWABLE = 0x22;
	public static final byte LANG = 0x23;
	public static final byte MATH = 0x24;
	public static final byte DATE = 0x25;
	public static final byte UNSUPPORT_PACKAGE = 0x26; /**BUG2156(Jerremy 2018.05.04)*/
	
	/**
	 *  jerremy.su 2022-10-16 02:41:25
	 */
	public static final byte TIMEZONE = 0x27;
	public static final byte STRING_BUILDER = 0x28;
	public static final byte STRING_BUFFER = 0x29;
	
	private static Map<Short, Map<Class, Byte>> TYPE_MAPPING = new ConcurrentHashMap<>();
	static{
		TYPE_MAPPING.put(VERSION_0, new ConcurrentHashMap<>());
		TYPE_MAPPING.put(VERSION_POS, new ConcurrentHashMap<>());
		TYPE_MAPPING.put(VERSION_A1, new ConcurrentHashMap<>());
		TYPE_MAPPING.put(VERSION_A2, new ConcurrentHashMap<>());
	}
	
	public static byte getType(Class type) {
		return getType(type, DEFAULT_VERSION);
	}
	
//	public static byte getType(Class type, short version) {
//		Map<Class, Byte> mapping = TYPE_MAPPING.get(version);
//		Byte b = mapping.get(type);
//		if(b != null) {
//			return b.byteValue();
//		}
//		
//		b = getType0(type, version);
//		mapping.put(type, b);
//		return b;
//	}
	
	public static byte getType(Class type, short version){
		Map<Class, Byte> typeMap = TYPE_MAPPING.get(version);
		if(typeMap == null) {
			TYPE_MAPPING.put(version, typeMap);
		}
		Byte typeCode = typeMap.get(type);
		if(typeCode != null) {
			return typeCode.byteValue();
		}
		typeCode = getType0(type, version);
		typeMap.put(type, typeCode);
		return typeCode.byteValue();
	}
	
	static byte getType0(Class type, short version){
		if (type == null) {
			return UNSUPPORT_PACKAGE;
		}
		
		if (type.isArray()) {
			return ARRAY;
		}
		if (byte.class.equals(type) || Byte.class.equals(type)) {
			return BYTE;
		}
		if (boolean.class.equals(type) || Boolean.class.equals(type)) {
			return BOOLEAN;
		}
		if (short.class.equals(type) || Short.class.equals(type)) {
			return SHORT;
		}
		if (int.class.equals(type) || Integer.class.equals(type)) {
			return INT;
		}
		if (char.class.equals(type) || Character.class.equals(type)) {
			return CHAR;
		}
		if (float.class.equals(type) || Float.class.equals(type)) {
			return FLOAT;
		}
		if (long.class.equals(type) || Long.class.equals(type)) {
			return LONG;
		}
		if (double.class.equals(type) || Double.class.equals(type)) {
			return DOUBLE;
		}
		if (Collection.class.isAssignableFrom(type)) {
			return COLLECTION;
		}
		if (Map.class.isAssignableFrom(type)) {
			return MAP;
		}
		if (Throwable.class.isAssignableFrom(type)) {
			return THROWABLE;
		}
		if (type.isEnum()) {
			return VERSION_A1 == version ? ENUMRATION : ENUM;
		}
		
		if (VERSION_A2 == version
				&& String.class.isAssignableFrom(type)) {
			//jerremy.su 2022-10-16 02:41:40 JAVA17 适应性修改
			return UTFSTRING;
		}
		
		if(VERSION_A2 == version
				&& TimeZone.class.isAssignableFrom(type)
				) {
			//jerremy.su 2022-10-16 02:41:40 JAVA17 适应性修改
			return TIMEZONE;
		}
		
		if(VERSION_A2 == version
				&& StringBuilder.class.isAssignableFrom(type)
				) {
			// jerremy.su 2022-10-16 02:41:40 JAVA17 适应性修改
			return STRING_BUILDER;
		}
			
		if(VERSION_A2 == version
				&& StringBuffer.class.isAssignableFrom(type)
				) {
			//jerremy.su 2022-10-16 02:41:40 JAVA17 适应性修改
			return STRING_BUFFER;
		}
			
			
		
		/**------------------B BUG1965(Jerremy 2018.04.08)------------------*/
		if (Class.class.isAssignableFrom(type)) {
			return CLASS;
		}
		/**------------------E BUG1965(Jerremy 2018.04.08)------------------*/
//		if (type.getPackage().equals(langPackage)) {
//			return LANG;
//		}
		
		if (Date.class.isAssignableFrom(type)) {
			return DATE;
		}
		
		String packageName = packageName(type);

		if (packageName.length() > 0) {
			if (mathPackage.equals(packageName)) {
				return MATH;
			}
			
			
			for(String usps : unsupportPackages) {
				if (packageName.startsWith(usps)) {
					return UNSUPPORT_PACKAGE;
				}
			}
		}
		
		return OBJECT;
	}
	
	public static String packageName(Class type) {
		String tmp = type.getName();
		int index = tmp.lastIndexOf(".");
		if (index < 0) {
			return "";
		}
		return tmp.substring(0, index);
	}
	
	final static Map<Class, ClassFlow> tbmdContext = new ConcurrentHashMap<>();
	
	public static ClassFlow getToyBoxMetadata(
			@NotNull Class clazz){
		if(clazz == null) {
			return null;
		}
		ClassFlow md = tbmdContext.get(clazz);
		if (md == null) {
			tbmdContext.put(clazz, md = new ClassFlow(clazz));
		}
		return md;
	}
	
	public static final Class loadClass(String typeName) throws Throwable{
		
		return ClassBufferedFactory.getFactory().forName(typeName);
		
//		Class type = basicClassContext.get(typeName);
//		if (type == null) {
//			type = classBufferedContext.get(typeName);
//		}
//		if (type == null) {
//			ClassLoader loader = Thread.currentThread().getContextClassLoader();
//			try {
//				type = loader == null ? Class.forName(typeName) : loader.loadClass(typeName);
//			} catch (Throwable e) {}
//			if (type == null && loader != null) {
//				type = Class.forName(typeName);
//			}
//			if (type != null) {
//				classBufferedContext.put(typeName, type);
//			}
//		}
//		return type;
	}
	
	public static byte getByte(InputStream in) throws IOException {
		byte[] b = new byte[1];
		in.read(b);
		return b[0];
	}
	
	public static boolean getBoolean(InputStream in) throws IOException {
		byte[] b = new byte[1];
		in.read(b);
		return b[0] != 0;
	}

	public static char getChar(InputStream in) throws IOException {
		byte[] b = new byte[2];
		in.read(b);
		return (char) (((b[1] & 0xFF) << 0) + ((b[0]) << 8));
	}

	public static short getShort(InputStream in) throws IOException {
		byte[] b = new byte[2];
		in.read(b);
		return (short) (((b[1] & 0xFF) << 0) + ((b[0]) << 8));
	}

	public static int getInt(InputStream in) throws IOException {
		byte[] b = new byte[4];
		in.read(b);
		return ((b[3] & 0xFF) << 0) + ((b[2] & 0xFF) << 8)
				+ ((b[1] & 0xFF) << 16) + ((b[0]) << 24);
	}

	public static float getFloat(InputStream in) throws IOException {
		byte[] b = new byte[4];
		in.read(b);
		int i = ((b[3] & 0xFF) << 0) + ((b[2] & 0xFF) << 8)
				+ ((b[1] & 0xFF) << 16) + ((b[0]) << 24);
		return Float.intBitsToFloat(i);
	}

	public static long getLong(InputStream in) throws IOException {
		byte[] b = new byte[8];
		in.read(b);
		return ((b[7] & 0xFFL) << 0) + ((b[6] & 0xFFL) << 8)
				+ ((b[5] & 0xFFL) << 16) + ((b[4] & 0xFFL) << 24)
				+ ((b[3] & 0xFFL) << 32) + ((b[2] & 0xFFL) << 40)
				+ ((b[1] & 0xFFL) << 48) + (((long) b[0]) << 56);
	}

	public static double getDouble(InputStream in) throws IOException {
		byte[] b = new byte[8];
		in.read(b);
		long j = ((b[7] & 0xFFL) << 0) + ((b[6] & 0xFFL) << 8)
				+ ((b[5] & 0xFFL) << 16) + ((b[4] & 0xFFL) << 24)
				+ ((b[3] & 0xFFL) << 32) + ((b[2] & 0xFFL) << 40)
				+ ((b[1] & 0xFFL) << 48) + (((long) b[0]) << 56);
		return Double.longBitsToDouble(j);
	}

	/*
	 * Methods for packing primitive values into byte arrays starting at given
	 * offsets.
	 */

	public static void putByte(OutputStream out, byte val) throws IOException{
		out.write(new byte [] {val});
	}
	
	public static void putBoolean(OutputStream out, boolean val)
			throws IOException {
		byte[] b = new byte[1];
		b[0] = (byte) (val ? 1 : 0);
		out.write(b);
	}

	public static void putChar(OutputStream out, char val) throws IOException {
		byte[] b1 = new byte[2];
		b1[1] = (byte) (val >>> 0);
		b1[0] = (byte) (val >>> 8);
		out.write(b1);
	}

	public static void putShort(OutputStream out, short val)
			throws IOException {
		byte[] b = new byte[2];
		b[1] = (byte) (val >>> 0);
		b[0] = (byte) (val >>> 8);
		out.write(b);
	}

	public static void putInt(OutputStream out, int val) throws IOException {
		byte[] b = new byte[4];
		b[3] = (byte) (val >>> 0);
		b[2] = (byte) (val >>> 8);
		b[1] = (byte) (val >>> 16);
		b[0] = (byte) (val >>> 24);
		out.write(b);
	}

	public static void putFloat(OutputStream out, float val)
			throws IOException {
		byte[] b = new byte[4];
		int i = Float.floatToIntBits(val);
		b[3] = (byte) (i >>> 0);
		b[2] = (byte) (i >>> 8);
		b[1] = (byte) (i >>> 16);
		b[0] = (byte) (i >>> 24);
		out.write(b);
	}

	public static void putLong(OutputStream out, long val) throws IOException {
		byte[] b = new byte[8];
		b[7] = (byte) (val >>> 0);
		b[6] = (byte) (val >>> 8);
		b[5] = (byte) (val >>> 16);
		b[4] = (byte) (val >>> 24);
		b[3] = (byte) (val >>> 32);
		b[2] = (byte) (val >>> 40);
		b[1] = (byte) (val >>> 48);
		b[0] = (byte) (val >>> 56);
		out.write(b);
	}

	public static void putDouble(OutputStream out, double val)
			throws IOException {
		byte[] b = new byte[8];
		long j = Double.doubleToLongBits(val);
		b[7] = (byte) (j >>> 0);
		b[6] = (byte) (j >>> 8);
		b[5] = (byte) (j >>> 16);
		b[4] = (byte) (j >>> 24);
		b[3] = (byte) (j >>> 32);
		b[2] = (byte) (j >>> 40);
		b[1] = (byte) (j >>> 48);
		b[0] = (byte) (j >>> 56);
		out.write(b);
	}
	
	public static void putByteArray(OutputStream out, byte [] b) throws IOException{
		out.write(b);
	}
	
	public static void putByteArray(OutputStream out, Byte [] b) throws IOException{
		byte [] buff = new byte [b.length];
		for(int i = 0; i < b.length; i ++){
			buff [i] = Byte.valueOf(b [i]);
		}
		out.write(buff);
	}
}
