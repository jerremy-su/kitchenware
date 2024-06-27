package org.kitchenware.express.util;

public class BoolObjects {

	public static boolean valueOf(Object b) {
		if(b == null) {
			return false;	
		}
		
		if(Boolean.class.isInstance(b)) {
			return (Boolean) b;
		}
		if(boolean.class.isInstance(b)) {
			return boolean.class.cast(b);
		}
		
		return valueOf(String.valueOf(b));
	}
	
	public static boolean valueOf(String src) {
		if(StringObjects.isEmpty(src)) {
			return false;
		}
		
		return StringObjects.assertEqualsIgnoreCase("true", src)
				|| StringObjects.assertEqualsIgnoreCase("1", src)
				|| StringObjects.assertEqualsIgnoreCase("Y", src)
				|| StringObjects.assertEqualsIgnoreCase("on", src)
			;
	}
	
	public static char toChar(boolean b) {
		return b ? 'Y' : 'N';
	}
	
	public static Integer toInteger(Object src) {
		return toInteger(valueOf(src));
	}
	
	public static Integer toInteger(boolean b) {
		return b ? 1 : 0;
	}
}
