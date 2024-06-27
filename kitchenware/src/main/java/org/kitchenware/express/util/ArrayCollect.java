package org.kitchenware.express.util;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.kitchenware.express.annotation.NotNull;

public class ArrayCollect<T> {

	static final Map<Class, ArrayCollect> CONTEXT = new ConcurrentHashMap<>();
	
	public static final ArrayCollect<Object> JOBJECT = get(Object.class);
	public static final ArrayCollect<String> STRING = get(String.class);
	public static final ArrayCollect<Integer> INTEGER = get(Integer.class);
	public static final ArrayCollect<Long> LONG = get(Long.class);
	public static final ArrayCollect<Character> CHAR = get(Character.class);
	
	public static <T> ArrayCollect<T> get(Class<T> type) {
		ArrayCollect<T> collect = CONTEXT.get(type);
		if(collect == null) {
			CONTEXT.put(type, collect = new ArrayCollect<T>(type));
		}
		return collect;
	}
	
	final Class<T> type;
	final T [] template;
	
	ArrayCollect(@NotNull Class<T> type){
		this.type = type;
		this.template = EmptyArray.getInstance().array(type);
	}
	
	public T [] toArray(@NotNull Collection<T> source) {
		if(source == null) {
			return null;
		}
		return source.toArray(template);
	}
}
