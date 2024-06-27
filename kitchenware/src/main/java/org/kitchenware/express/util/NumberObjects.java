package org.kitchenware.express.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NumberObjects {

	private static final Logger LOGGER = Logger.getLogger(NumberObjects.class.getName());

	public static boolean assertIntegerValue(Object v) {
		if(Integer.class.isInstance(v) || int.class.isInstance(v)) {
			return true;
		}
		
		try {
			Integer.valueOf(String.valueOf(v));
			return true;
		} catch (Throwable e) {
			return false;
		}
	}
	
	public static boolean assertlongValue(Object v) {
		if(Long.class.isInstance(v) || long.class.isInstance(v)) {
			return true;
		}
		
		try {
			Long.valueOf(String.valueOf(v));
			return true;
		} catch (Throwable e) {
			return false;
		}
	}
	
	public static boolean assertNumberValue(Object v) {
		if(Number.class.isInstance(v)) {
			return true;
		}
		
		try {
			new BigDecimal(String.valueOf(v));
			return true;
		} catch (Throwable e) {
			return false;
		}
	}
	
	public static BigDecimal toBigDecimal(Object o) {
		if(o == null) {
			return null;
		}
		
		if(BigDecimal.class.isInstance(o)) {
			return BigDecimal.class.cast(o);
		}
		if (Double.class.isInstance(o)) {
			return BigDecimal.valueOf((Double) o);
		}
		try {
			String str;
			if (String.class.isInstance(o)) {
				str = ((String) o).replace(",", "").trim();
			} else {
				str = String.valueOf(o);
			}
			return new BigDecimal(str);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static BigDecimal toBigDecimal(Object o, BigDecimal defaultValue) {
		BigDecimal value = toBigDecimal(o);
		return value == null ? defaultValue : value;
	}
	
	public static Integer toInteger(Object o, Integer defaultValue) {
		Integer result = toInteger(o);
		if(result == null) {
			result = defaultValue;
		}
		return result;
	}
	
	public static Integer toInteger(Object o) {
		if(o == null) {
			return null;
		}
		
		if(o instanceof BigDecimal) {
			return ((BigDecimal) o).intValue();
		}
		
		if(Integer.class.isInstance(o) || int.class.isInstance(o)) {
			return (Integer) o;
		}
		
		try {
			return Integer.valueOf(String.valueOf(o));
		} catch (Exception e) {
			return null;
		}
	}
	
	public static int merge(int number, int mergeValue) {
		for(;mergeValue > 0;) {
			number = (number * 10) + (mergeValue % 10);
			mergeValue = mergeValue / 10;
		}
		return number;
	}
	
	public static Double zeroDiv(double nVal1, double nVal2) {
		if (nVal2 != 0) {
			return nVal1 / nVal2;
		} else
			return 0.00;
	}
	/**
	 * 
	 * @rainbow
	 * 
	 * @2009-8-14 ����11:43:31
	 * 
	 * @param nVal1
	 * @param nVal2
	 * @param n
	 * @return
	 */
	public static BigDecimal zeroDiv(Number nVal1, Number nVal2, int n) {
		BigDecimal b1 = new BigDecimal(nVal1.toString());
		BigDecimal b2 = new BigDecimal(nVal2.toString());
		return zeroDiv(b1, b2, n);
	}
	
	public static BigDecimal zeroDiv(Number nVal1, Number nVal2) {
		BigDecimal b1 = new BigDecimal(nVal1.toString());
		BigDecimal b2 = new BigDecimal(nVal2.toString());
		return zeroDiv(b1, b2);
	}
	
	public static BigDecimal zeroDiv(BigDecimal nVal1, BigDecimal nVal2) {
		return zeroDiv(nVal1, nVal2, 2);
	}
	
	public static BigDecimal zeroDiv(BigDecimal nVal1, BigDecimal nVal2, int n) {
		if (nVal2.compareTo(BigDecimal.ZERO) == 0)
			return BigDecimal.ZERO;
		else
			return nVal1.divide(nVal2, n, BigDecimal.ROUND_HALF_UP);
	}
	
	public static Long toLong(Object o, long defaultValue) {
		Long value = toLong(o);
		if(value == null) {
			return defaultValue;
		}
		return value;
	}
	
	public static Long toLong(Object o) {
		if(o == null) {
			return null;
		}

		if(Long.class.isInstance(o) || long.class.isInstance(o)) {
			return (Long) o;
		}

		try {
			return Long.valueOf(String.valueOf(o));
		} catch (Exception e) {
			return null;
		}
	}
	
	public static long merge(long number, long mergeValue) {
		for(;mergeValue > 0;) {
			number = (number * 10) + (mergeValue % 10);
			mergeValue = mergeValue / 10;
		}
		return number;
	}
	
	public static Byte toByte(Object o, byte defaultValue) {
		Byte result = toByte(o);
		if(result == null) {
			result = defaultValue;
		}
		return result;
	}
	
	public static Byte toByte(Object o) {
		if(o == null) {
			return null;
		}
		
		if(Byte.class.isInstance(o) || byte.class.isInstance(o)) {
			return (Byte) o;
		}
		
		try {
			return Byte.valueOf(StringObjects.valueOf(o));
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Short toShort(Object o, short defaultValue) {
		Short value = toShort(o);
		if(value == null) {
			value = defaultValue;
		}
		return value;
	}
	
	public static Short toShort(Object o) {
		if(o == null) {
			return null;
		}
		
		if(Short.class.isInstance(o) || short.class.isInstance(o)) {
			return (Short) o;
		}
		
		try {
			return Short.valueOf(String.valueOf(o));
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Float toFloat(Object o, float defaultValue) {
		Float value = toFloat(o);
		if(value == null) {
			value = defaultValue;
		}
		return value;
	}
	
	public static Float toFloat(Object o) {
		if(o == null) {
			return null;
		}
		
		if(Float.class.isInstance(o) || float.class.isInstance(o)) {
			return (Float) o;
		}
		
		try {
			return Float.valueOf(String.valueOf(0));
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Double toDouble(Object o, double defaultValue) {
		Double value = toDouble(o);
		if(value == null) {
			value = defaultValue;
		}
		return value;
	}
	
	public static Double toDouble(Object o) {
		if(o == null) {
			return null;
		}
		
		if(Double.class.isInstance(o) || double.class.isInstance(o)) {
			return (Double) o;
		}
		
		try {
			return Double.valueOf(String.valueOf(o));
		} catch (Exception e) {
			return null;
		}
	}
	
	/**toPercent(...) * 100
	 * @param value
	 * @param amount
	 * @return
	 */
	public static int toPercent(long value, long amount) {
		return toPercent(BigDecimal.valueOf(value), BigDecimal.valueOf(amount)).intValue();
	}
	
	/**toPercent(...) * 100
	 * @param value
	 * @param amount
	 * @return
	 */
	public static int toPercent(int value, int amount) {
		return toPercent(BigDecimal.valueOf(value), BigDecimal.valueOf(amount)).intValue();
	}
	
	public static BigDecimal toPercent(BigDecimal value, BigDecimal amount) {
		if(amount.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO.setScale(2);
		}
		return value.divide(amount, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2);
	}
	
	public static BigDecimal percentValueOf(BigDecimal percent, BigDecimal amount) {
		if(compareTo(percent, BigDecimal.ZERO) == 0 || compareTo(amount, BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}
		
		BigDecimal value = amount.multiply(percent.divide(BigDecimal.valueOf(100), 4, BigDecimal.ROUND_HALF_UP))
				.setScale(2, BigDecimal.ROUND_HALF_UP);
		return value;
	}
	
	public static Number toPercent(Number value, Number amount) {
		return toPercent(toBigDecimal(value, BigDecimal.ZERO), toBigDecimal(amount, BigDecimal.ZERO));
	}
	
	public static int toRatio(int value, float ratio) {
		int result = BigDecimal.valueOf(value).multiply(BigDecimal.valueOf(ratio)).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
		return result;
	}
	
	public static boolean assertNumberNotZero(Object number) {
		BigDecimal value = toBigDecimal(number);
		if(value == null) {
			return false;
		}
		boolean result = BigDecimal.ZERO.compareTo(value) != 0;
		return result;
	}

	public static Number max(Number o1, Number o2) {
		return max(toBigDecimal(o1, BigDecimal.ZERO), toBigDecimal(o2, BigDecimal.ZERO));
	}
	
	public static BigDecimal max(BigDecimal o1, BigDecimal o2) {
		if(o1.compareTo(o2) > 0) {
			return o1;
		}else {
			return o2;
		}
	}
	
	public static int compareTo(Object o1, Object o2) {
		BigDecimal num1 = toBigDecimal(o1, BigDecimal.ZERO);
		BigDecimal num2 = toBigDecimal(o2, BigDecimal.ZERO);
		return num1.compareTo(num2);
	}
	
	/**
	 * @param decimal 数字
	 * @param groupingUsed 是否使用千位分隔符
	 * @return
	 */
	public static String maximumFractionDigits(BigDecimal decimal, boolean groupingUsed) {
		if (null == decimal) {
			return null;
		}
		NumberFormat format = NumberFormat.getInstance();
		format.setMinimumFractionDigits(decimal.scale());
		format.setGroupingUsed(groupingUsed);
		return format.format(decimal);
	}
	
	public static BigDecimal getBigDecimal(String nums) {
		if(nums==null || nums.trim().isEmpty()) {
			return BigDecimal.ZERO;
		}
		
		NumberFormat format = NumberFormat.getInstance();
		try {
			return new BigDecimal(format.parse(nums).toString());
		} catch (ParseException e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
		
		return BigDecimal.ZERO; 
	}
	
	public static BigDecimal getNotZeroNumber(Object ... nums) {
		if(ArrayObjects.isEmpty(nums)) {
			return BigDecimal.ZERO;
		}
		
		for(Object num : nums) {
			BigDecimal number = toBigDecimal(num, BigDecimal.ZERO);
			if(number.compareTo(BigDecimal.ZERO) != 0) {
				return number;
			}
		}
		return BigDecimal.ZERO;
	}
	
	public static long pow(long src, long increment, int numberOfPow) {
		long result = src;
		for(int i = 0; i < numberOfPow; i ++) {
			result *= increment;
		}
		return result;
	}

	public static int pow(int src, int increment, int numberOfPow) {
		int result = src;
		for(int i = 0; i < numberOfPow; i ++) {
			result *= increment;
		}
		return result;
	}
	
	public static int unPow(long src, long decrement) {
		int result = 0;
		for(;(src = (src / decrement)) > 0;) {
			result ++;
		}
		return result;
	}
	
	public static int unPow(int src, int decrement) {
		int result = 0;
		for(;(src = (src / decrement)) > 0;) {
			result ++;
		}
		return result;
	}
	
	public static int unPow(int src, int decrement, int numberOfPow) {
		int result = 0;
		for(int i = 0; i < numberOfPow; i ++) {
			src = src / decrement;
			if(src <= 0) {
				break;
			}
			result ++;
		}
		return result;
	}
}
