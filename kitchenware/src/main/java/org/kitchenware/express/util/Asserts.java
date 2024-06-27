package org.kitchenware.express.util;

public class Asserts {

	public static void assertStringNotEmptyAfterTrim(String s, String err) {
		if(StringObjects.isEmptyAfterTrim(s)) {
			throw new NullPointerException(err);
		}
	}
	
	public static void assertArrayEmpty(Object array, String err) {
		if(ArrayObjects.isEmpty(array)) {
			throw new NullPointerException(err);
		}
	}
	
	public static void assertNotNull(Object o, String err) {
		if(o == null) {
			throw new NullPointerException(err);
		}
	}
}
