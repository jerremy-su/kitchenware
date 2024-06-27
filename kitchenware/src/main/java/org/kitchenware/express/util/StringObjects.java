package org.kitchenware.express.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.express.debug.Debug;
import org.kitchenware.express.function.ConsumerAccepter;
import org.kitchenware.express.function.access.UserAccessor;

public class StringObjects {
	
	static final Logger LOGGER = Logger.getLogger(StringObjects.class.getName());
	
	static final Map<Integer, Character> digits = new LinkedHashMap<>();
	static {
		digits.put(Integer.valueOf(0), Character.valueOf('0'));
		digits.put(Integer.valueOf(1), Character.valueOf('1'));
		digits.put(Integer.valueOf(2), Character.valueOf('2'));
		digits.put(Integer.valueOf(3), Character.valueOf('3'));
		digits.put(Integer.valueOf(4), Character.valueOf('4'));
		digits.put(Integer.valueOf(5), Character.valueOf('5'));
		digits.put(Integer.valueOf(6), Character.valueOf('6'));
		digits.put(Integer.valueOf(7), Character.valueOf('7'));
		digits.put(Integer.valueOf(8), Character.valueOf('8'));
		digits.put(Integer.valueOf(9), Character.valueOf('9'));
	}
	
	public static final String LINE_SEPARATOR;
	static {
		String separator = System.getProperty("line.separator");
		if(separator == null) {
			separator = "\n";
		}
		LINE_SEPARATOR = separator;
	}
	
	public static int hash(@NotNull String src) {
		if(src == null) {
			return 0;
		}
		return src.hashCode();
	}
	
	public static int readLine(
			@NotNull final java.io.Reader reader, @NotNull final ConsumerAccepter<String> event) {
		
		Asserts.assertNotNull(reader, "'reader' cannot be null.");
		Asserts.assertNotNull(event, "'event' cannot be null.");
		
		AtomicInteger counter = new AtomicInteger();
		
		try(BufferedReader bufferedReader = new BufferedReader(reader)) {
			
			for(String line; (line = bufferedReader.readLine()) != null;) {
				event.accept(line);
				
				counter.incrementAndGet();
			}
			
		} catch (Throwable e) {
			if(Debug.isDebug()) {
				LOGGER.log(Level.WARNING, e.getMessage(), e);
			}
			event.error(e);
		}
		
		return counter.intValue();
	}
	
	public static Character toDigitChar(int singleNumber) {
		Character value =  digits.get(Integer.valueOf(singleNumber));
		return value;
	}
	
	public static String format(String pattern, Object ... args) {
		try {
			return String.format(pattern, args);
		} catch (Throwable e) {
			return pattern;
		}
	}
	
	public static Set<String> analyzeCodes(String codes, String splitRegex, Map<String, String> codeMap) {
		Set<String> result = analyzeCodes("", codes, splitRegex, codeMap, new HashMap<>());
		result.removeAll(codeMap.keySet());
		return result;
	}

	private static Set<String> analyzeCodes(String pre, String codes, String splitRegex, Map<String, String> codeMap,
			Map<String, Set<String>> paths) {
		Set<String> result = new TreeSet<String>();
		if (null != codes) {
			// >>B[12247]sdjen20170829
			String[] acodes = codes.split(splitRegex);
			Arrays.sort(acodes, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					// 让-排后面，使可以从已存在中扣除
					if (o1.startsWith("-"))
						return 1;
					else if (o2.startsWith("-"))
						return -1;
					else
						return 0;
				}
			});
			for (String code : acodes) {
//				System.out.println(pre + code);
				code = code.trim();
				if (code.isEmpty())
					continue;
				if (code.startsWith("-")) {
					code = code.substring(1);
					if (codeMap.containsKey(code)) {
						if (!paths.containsKey(code)) {
							paths.put(code, null);
							paths.put(code, analyzeCodes(pre + "	", codeMap.get(code), splitRegex, codeMap, paths));
						}
						if (null != paths.get(code)) {
							result.removeAll(paths.get(code));
						}
					} else {
						result.remove(code);
					}
				} else {
					if (codeMap.containsKey(code)) {
						if (!paths.containsKey(code)) {
							paths.put(code, null);
							paths.put(code, analyzeCodes(pre + "	", codeMap.get(code), splitRegex, codeMap, paths));
						}
						if (null != paths.get(code)) {
							result.addAll(paths.get(code));
						}
					} else {
						result.add(code);
					}
				}
			}
		}
//		System.out.println(pre + "[C]:" + codes + "	:	" + result + "		" + paths);
		return result;
	}

	public static boolean assertNotNull(String src) {
		return src != null; 
	}

	public static boolean isEmpty(String src) {
		return !assertNotEmpty(src);
	}

	public static boolean assertNotEmpty(String src) {
		return assertNotNull(src) && src.length() > 0;
	}

	public static boolean isEmptyAfterTrim(String src) {
		return !assertNotEmptyAfterTrim(src);
	}

	public static boolean isEmptyAfterTrimAll(String ... src) {
		if(src == null || src.length < 1) {
			return true;
		}

		for(String s : src) {
			if(StringObjects.assertNotEmptyAfterTrim(s)) {
				return false;
			}
		}
		return true;
	}

	public static boolean assertNotEmptyAfterTrim(String src) {
		return assertNotNull(src) && src.trim().length() > 0;
	}
	
	public static boolean assertNotEmptyAndSet(String src, Consumer<String> function) {
		boolean doNext = StringObjects.assertNotEmptyAfterTrim(src); 
		if(doNext) {
			function.accept(src);
		}
		return doNext;
	}

	public static boolean assertNoEmptyAfterTrim(String ... all) {
		if(all == null || all.length < 1) {
			return false;
		}
		for(String src : all) {
			if(isEmptyAfterTrim(src)) {
				return false;
			}
		}
		return true;
	}

	public static boolean assertEqualsIgnoreCase(String a, String b) {
		return (a == b) || (a != null && a.equalsIgnoreCase(b));
	}

	public static boolean assertEqualsIgnoreCase(String src, String ... patterns) {
		if(patterns == null || patterns.length < 1) {
			return false;
		}

		for(String pattern : patterns) {
			if(!assertEqualsIgnoreCase(src, pattern)) {
				return false;
			}
		}

		return true;
	}

	public static boolean hasEqualsIgnoreCase(String src, String ... patterns) {
		if(patterns == null || patterns.length < 1) {
			return false;
		}

		for(String pattern : patterns) {
			if(assertEqualsIgnoreCase(src, pattern)) {
				return true;
			}
		}

		return false;
	}

	public static boolean assertEquals(String a, String b) {
		return (a == b) || (a != null && a.equals(b));
	}
	
	public static boolean assertNoEquals(String a, String b) {
		a = valueOf(a, "");
		b = valueOf(b, "");
		
		return !a.equals(b);
	}

	public static boolean assertAllTheAsciiCharacter(String s) {
		if(s == null) {
			return true;
		}
		for(char c : s.toCharArray()) {
			int hash = Character.hashCode(c);
			if(hash < 0x00 || hash > 0x7f) {
				return false;
			}
		}
		return true;
	}

	public static boolean hasAsciiCharacter(String s) {
		if(s == null) {
			return true;
		}
		for(char c : s.toCharArray()) {
			int hash = Character.hashCode(c);
			if(hash >= 0x00 && hash <= 0x7f) {
				return true;
			}
		}
		return false;
	}

	public static final String getNotEmptyValue(String ... args) {
		if(ArrayObjects.isEmpty(args)) {
			return null;
		}
		
		for(String src : args) {
			if(StringObjects.assertNotEmptyAfterTrim(src)) {
				return src;
			}
		}
		
		return null;
	}
	
	public static String notNoneString(String src) {
		return src == null ? "" : src;
	}

	public static String clipString(String src, int length) {
		if(src == null || length < 0 || src.length() <= length) {
			return src;
		}

		return new String(Arrays.copyOf(src.toCharArray(), length));
	}

	public static int compareIgnoreCase(String s1, String s2) {

		if(s1 == s2) {
			return 0;
		}

		if(s1 == null) {
			return -1;
		}

		if(s2 == null) {
			return 1;
		}

		return s1.toUpperCase().compareTo(s2.toUpperCase());
	}

	public static String toUTF8String(byte [] src) {
		return toString(src, "utf8");
	}

	public static String toString(byte [] src, String charset) {
		if(assertNotEmptyAfterTrim(charset) && !Charset.isSupported(charset)) {
			throw new RuntimeException(String.format("Not supported charset encoding: %s", charset));
		}

		if(src == null) {
			return null;
		}
		
		if(assertNotEmptyAfterTrim(charset)){
			return UserAccessor.functionAccess(()->{
				return new String(src, charset);
			});
		}

		return new String(src);	
	}

	public static byte [] toUTF8Binaries(String src) {
		return toBinaries(src, "utf8");
	}

	public static byte [] toBinaries(String src, String charset) {
		if(src == null) {
			return new byte [0];
		}
		
		if(assertNotEmptyAfterTrim(charset) && !Charset.isSupported(charset)) {
			throw new RuntimeException(String.format("Not supported charset encoding: %s", charset));
		}
		if(assertNotEmptyAfterTrim(charset)){
			return UserAccessor.functionAccess(()->{
				return src.getBytes(charset);
			});
		}

		return src.getBytes();

	}

	public static String valueOf(Object src) {
		return src == null ? null : String.valueOf(src);
	}
	
	public static String valueOf(Object src, String defaultValue) {
		String result = valueOf(src);
		if(result == null) {
			result = defaultValue;
		}
		return result;
	}

	public static String valueOfAndTrim(Object src) {
		return src == null ? null : String.valueOf(src).trim();
	}
	
	public static String [] split(String src, String pattern) {
		if(src == null) {
			return null;
		}
		if(StringObjects.isEmptyAfterTrim(src) || StringObjects.isEmpty(pattern)) {
			return new String [] {src};
		}
		
		CharStreamClipIterator stream = new CharStreamClipIterator(src.toCharArray());
		char [] separator = pattern.toCharArray();
		List<String> result = new ArrayList<>();
		for(char [] item; (item = stream.clip(separator)) != null && item.length > 0;) {
			result.add(new String(item));
		}
		if(stream.size() > 0) {
			result.add(new String(stream.getCharArrays()));
		}
		return result.toArray(new String [0]);
	}
	
	//-------------------------------------------------------------------------------------------------------------

	public static boolean isExInLinkStr(String search,String str){
		Pattern p=Pattern.compile("(?<!\\w|[\\u4e00-\\u9fa5])"+search+"(?!\\w|[\\u4e00-\\u9fa5])");
		Matcher match=p.matcher(str);
		if(match.find())
			return true;
		else
			return false;
	}

	public static String filterStr(String str) {
		if (str == null || str.isEmpty())
			return "";
		char[] charArr = str.toCharArray();
		StringBuilder builder = new StringBuilder();
		for (char c : charArr) {
			if (c == 0){
				break;//bug9718 calvin 2015-07-09 ��ascii��Ϊ0�ĺ����ַ����ص�
			}
			builder.append(c);
		}
		String result = builder.toString();
		return result;
	}

	public static String forceTrim(String s) {
		if(s == null) {
			return s;
		}

		s = s.replace("\r\n", "").replace("\n", "").replace(" ", "").replace("\t", "");
		return s;
	}

	public static String trim(String s) {
		if(s == null) {
			return s;
		}
		s = s.trim();
		return s;
	}

	public static byte [] hexToBinaries(String hex){
		if(hex == null) {
			return null;
		}
		byte [] b = new byte [hex.length() / 2];
		char [] chArr = hex.toCharArray();
		int off = 0;
		for (int i = 0; i < chArr.length; i++) {
			b [off ++] = (byte)Integer.parseInt(new String(new char [] {chArr [i ++], chArr [i]}), 16);
		}
		return b;
	}

	public static String binariesToHex(byte[] b) { 
		if(b == null) {
			return null;
		}

		StringBuilder buf = new StringBuilder();
		String stmp = "";
		for (int i = 0; i < b.length; i++) {
			stmp = (Integer.toHexString(b[i] & 0XFF));
			if (stmp.length() == 1) {
				buf.append("0");
			} 
			buf.append(stmp);
		}
		return buf.toString().toUpperCase();
	}

	public static String [] getAvailableCharsets() {
		Set<String> result = new LinkedHashSet<>();
		for(java.util.Map.Entry<String, Charset> en : Charset.availableCharsets().entrySet()) {
			result.add(en.getKey().toLowerCase());
			for(String s : en.getValue().aliases()) {
				result.add(s.toLowerCase());
			}
		}
		return result.toArray(new String [0]);
	}

	public static String toLowerCase(Object o) {
		String value = valueOf(o);
		return value == null ? null : value.toLowerCase();
	}

	public static String toUpperCase(Object o) {
		String value = valueOf(o);
		return value == null ? null : value.toUpperCase();
	}

	public static boolean contains(String s, String ... src) {
		if(ArrayObjects.isEmpty(src)) {
			return false;
		}

		for(String item : src) {
			if(StringObjects.assertEquals(s, item)) {
				return true;
			}
		}

		return false;
	}

	public static boolean containsIgnoreCase(String s, String ... src) {
		if(ArrayObjects.isEmpty(src)) {
			return false;
		}

		for(String item : src) {
			if(StringObjects.assertEqualsIgnoreCase(s, item)) {
				return true;
			}
		}

		return false;
	}
	
	public static int lengthOf(String src) {
		if(StringObjects.isEmptyAfterTrim(src)) {
			return 0;
		}
		return src.length();
	}
	
	// Java and JavaScript  
	//--------------------------------------------------------------------------  
	/** 
	 * Escapes the characters in a String using Java String rules. 
	 * 
	 * Deals correctly with quotes and control-chars (tab, backslash, cr, ff, etc.)  
	 * 
	 * So a tab becomes the characters '\\' and 
	 * 't'. 
	 * 
	 * The only difference between Java strings and JavaScript strings 
	 * is that in JavaScript, a single quote must be escaped. 
	 * 
	 * Example: 
	 *  
	 * input string: He didn't say, "Stop!" 
	 * output string: He didn't say, \"Stop!\" 
	 *  
	 *  
	 * 
	 * @param str  String to escape values in, may be null 
	 * @return String with escaped values, null if null string input 
	 */  
	public static String escapeJava(String str) {  
		return escapeJavaStyleString(str, false, false);  
	}  

	private static String escapeJavaStyleString(String str, boolean escapeSingleQuotes, boolean includeUnicode)
	{
		if (str == null)
			return null;
		try
		{
			StringWriter writer = new StringWriter(str.length() * 2);
			escapeJavaStyleString(writer, str, escapeSingleQuotes,includeUnicode);
			return writer.toString();
		}
		catch (IOException ioe) {
			ioe.printStackTrace(); }
		return null;
	}

	private static void escapeJavaStyleString(Writer out, String str, boolean escapeSingleQuote, boolean includeUnicode) throws IOException {  
		if (out == null) {  
			throw new IllegalArgumentException("The Writer must not be null");  
		}  
		if (str == null) {  
			return;  
		}  
		int sz;  
		sz = str.length();  
		for (int i = 0; i < sz; i++) {  
			char ch = str.charAt(i);  

			// handle unicode  
			if (includeUnicode && ch > 0xfff) {  
				out.write("\\u" + hex(ch));  
			} else if (includeUnicode && ch > 0xff) {  
				out.write("\\u0" + hex(ch));  
			} else if (includeUnicode && ch > 0x7f) {  
				out.write("\\u00" + hex(ch));  
			} else if (ch < 32) {  
				switch (ch) {  
				case '\b':  
					out.write('\\');  
					out.write('b');  
					break;  
				case '\n':  
					out.write('\\');  
					out.write('n');  
					break;  
				case '\t':  
					out.write('\\');  
					out.write('t');  
					break;  
				case '\f':  
					out.write('\\');  
					out.write('f');  
					break;  
				case '\r':  
					out.write('\\');  
					out.write('r');  
					break;  
				default :  
					if(includeUnicode) {
						if (ch > 0xf) {  
							out.write("\\u00" + hex(ch));  
						} else {  
							out.write("\\u000" + hex(ch));  
						}  
					}
					else {
						out.write(ch);  
					}
					break;  
				}  
			} else {  
				switch (ch) {  
				case '\'':  
					if (escapeSingleQuote) out.write('\\');  
					out.write('\'');  
					break;  
				case '"':  
					out.write('\\');  
					out.write('"');  
					break;  
				case '\\':  
					out.write('\\');  
					out.write('\\');  
					break;  
				default :  
					out.write(ch);  
					break;  
				}  
			}  
		}  
	}  
	/**  
	 * Unescapes any Java literals found in the String.  
	 * For example, it will turn a sequence of '\' and  
	 *  * 'n' into a newline character, unless the '\'  
	 * is preceded by another '\'.  
	 * @param str  the String to unescape, may be null  
	 * @return a new unescaped String, null if null string input  
	 */  
	public static String unescapeJava(String str) {  
		return unescapeJava(str,false);
	}  
	
	public static String unescapeJava(String str,boolean includeUnicode) {  
		if (str == null) {  
			return null;  
		}  
		try {  
			StringPrintWriter writer = new StringPrintWriter(str.length());  
			unescapeJava(writer, str,includeUnicode);  
			writer.flush();
			return writer.getString();  
		} catch (Exception ioe) {  
			// this should never ever happen while writing to a StringWriter  
			ioe.printStackTrace();  
			return null;  
		}  
	}  

	public static void unescapeJava(Writer out, String str,boolean includeUnicode) throws Exception {  
		if (out == null) {  
			throw new IllegalArgumentException("The Writer must not be null");  
		}  
		if (str == null) {  
			return;  
		}  
		int sz = str.length();  
		StringBuffer unicode = new StringBuffer(4);  
		boolean hadSlash = false;  
		boolean inUnicode = false;  
		for (int i = 0; i < sz; i++) {  
			char ch = str.charAt(i);  
			if (includeUnicode && inUnicode) {  
				// if in unicode, then we're reading unicode  
				// values in somehow  
				unicode.append(ch);  
				if (unicode.length() == 4) {  
					// unicode now contains the four hex digits  
					// which represents our unicode chacater  
					try {  
						int value = Integer.parseInt(unicode.toString(), 16);  
						out.write((char) value);  
						unicode.setLength(0);  
						inUnicode = false;  
						hadSlash = false;  
					} catch (NumberFormatException nfe) {  
						throw new Exception("Unable to parse unicode value: " + unicode, nfe);  
					}  
				}  
				continue;  
			}  
			if (hadSlash) {  
				// handle an escaped value  
				hadSlash = false;  
				switch (ch) {  
				case '\\':  
					out.write('\\');  
					break;  
				case '\'':  
					out.write('\'');  
					break;  
				case '\"':  
					out.write('"');  
					break;  
				case 'r':  
					out.write('\r');  
					break;  
				case 'f':  
					out.write('\f');  
					break;  
				case 't':  
					out.write('\t');  
					break;  
				case 'n':  
					out.write('\n');  
					break;  
				case 'b':  
					out.write('\b');  
					break;  
				case 'u':  
				{  
					if(includeUnicode) {
						// uh-oh, we're in unicode country....  
						inUnicode = true;  
					}
					else {
						out.write(ch);  
					}
					break; 
				}  
				default :  
					out.write(ch);  
					break;  
				}  
				continue;  
			} else if (ch == '\\') {  
				hadSlash = true;  
				continue;  
			}  
			out.write(ch);  
		}  
		if (hadSlash) {  
			// then we're in the weird case of a \ at the end of the  
			// string, let's output it anyway.  
			out.write('\\');  
		}  
	}  
	/** 
	 * Returns an upper case hexadecimal String for the given 
	 * character. 
	 *  
	 * @param ch The character to convert. 
	 * @return An upper case hexadecimal String 
	 */  
	private static String hex(char ch){
		return Integer.toHexString(ch).toUpperCase();
	}

	/**
	 * @deprecated com.shiji.java.CollectionObjects.toString(Collection<E> array)
	 *             instead
	 * @param <E>
	 * @param array
	 * @return
	 */
	public static <E> String collection2string(Collection<E> array) {
		return CollectionObjects.toString(array);
	}

	/**
	 * @deprecated com.shiji.java.CollectionObjects.fillCollection(Collection<String>
	 *             collection, String list) instead
	 * @param collection
	 * @param list
	 * @return
	 */
	public static Collection<String> fillCollection(Collection<String> collection, String list) {
		return CollectionObjects.fillCollection(collection, list);
	}
	
	public static <E> String getDiffMsg(E org, E curr, String format, Function<E, String> func) {
		String s_org = func.apply(org), s_curr = func.apply(curr);
		if (StringObjects.assertEquals(s_org, s_curr)) {
			return "";
		} else {
			return MessageFormat.format(format, s_org, s_curr);
		}
	}

}

class StringPrintWriter extends PrintWriter {

	public StringPrintWriter() {
		super(new StringWriter());
	}

	public StringPrintWriter(int initialSize) {
		super(new StringWriter(initialSize));
	}

	/**
	 * <p>Since toString() returns information *about* this object, we
	 * want a separate method to extract just the contents of the
	 * internal buffer as a String.</p>
	 *
	 * @return the contents of the internal string buffer
	 */
	public String getString() {
		flush();
		return ((StringWriter) out).toString();
	}

}
