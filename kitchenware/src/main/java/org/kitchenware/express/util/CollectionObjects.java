package org.kitchenware.express.util;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.express.concurrent.atomic.AtomicValue;

public class CollectionObjects {
	
	public static <T> void foreach(
			@NotNull Collection<T> src, @NotNull ArrayObjects.IteratorHandler<T> event) {
		if(event == null || isEmpty(src)) {
			return;
		}
		
		Object [] arr = src.toArray();
		ArrayObjects.foreach(arr, (i, item) -> {
			event.handle(i, (T) item);
		});
	}
	
	public static int size(Collection c) {
		if(isEmpty(c)) {
			return 0;
		}
		return c.size();
	}
	
	public static boolean isEmpty(Collection c) {
		return c == null || c.isEmpty();
	}
	
	public static boolean assertNotNull(Collection c) {
		return c != null;
	}
	
	public static boolean assertNotEmpty(Collection c) {
		return assertNotNull(c) && c.size() > 0;
	}
	
	public static <E> String toString(Collection<E> array) {
		return toString(array, ",", null);
	}

	public static <E> String toString(Collection<E> array, String regex, Function<E, String> valueAdapter) {
		StringBuilder result = new StringBuilder();
		if (null != array) {
			array.forEach(o -> {
				if (result.length() > 0) {
					result.append(regex);
				}
				result.append(null == valueAdapter ? o : valueAdapter.apply(o));
			});
		}
		return result.toString();
	}

	public static <R extends Collection<String>> R fillCollection(R collection, String list) {
		return fillCollection(collection, list, ",", e -> e);
	}

	public static <E, R extends Collection<E>> R fillCollection(R collection, String list, String regex,
			Function<String, E> valueAdapter) {
		if (!StringObjects.isEmpty(list)) {
			for (String e : list.split(regex)) {
				collection.add(valueAdapter.apply(e));
			}
		}
		return collection;
	}

	public static Set<String> doSet(Object src){
		return doSet(StringObjects.valueOf(src));
	}

	public static Set<String> doSet(String[] src){
		Set<String> result = new LinkedHashSet<>();
		if(ArrayObjects.isEmpty(src)) {
			return result;
		}
		result.addAll(Arrays.asList(src));
		return result;
	}
	
	public static <T> Set<T> doSet(Class<T> validType, Object [] array){
		Set<T> result = new LinkedHashSet<>();
		if(ArrayObjects.assertArrayNotEmpty(array)) {
			for(Object item : array) {
				if(item == null) {
					continue;
				}
				result.add(validType.cast(item));
			}
		}
		return result;
	}

	public static Set<String> doSet(Collection<String> src){
		return new LinkedHashSet<>(src);
	}
	
	public static boolean contains(Collection src, Collection subSet) {
		if(isEmpty(src) || isEmpty(subSet)) {
			return false;
		}
		for(Object item : src) {
			for(Object subItem : subSet) {
				if(Objects.equals(item, subItem)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean contains(Set src, Collection validSet) {
		if(isEmpty(src) || isEmpty(validSet)) {
			return false;
		}
		for(Object subItem : validSet) {
			if(src.contains(subItem)) {
				return true;
			}
		}
		return false;
	}
	
	public static Set<String> doSet(String src){
		return doSet(src, false);
	}
	
	public static Set<String> doSet(String src, boolean excludeEmptyItem){
		return doSet(src, ",", excludeEmptyItem);
	}
	
	public static Set<String> doSet(String src, String splitMark, boolean excludeEmptyItem){
		return doSet(src, splitMark, excludeEmptyItem, null);
	}
	
	public static Set<String> doSet(String src, String splitMark, boolean excludeEmptyItem, Function<String, String> itemHandler){
		Set<String> result = new LinkedHashSet<>();
		
		if(!StringObjects.assertNotEmptyAfterTrim(src)) {
			return result;
		}
		
		Arrays.stream(src.split(splitMark))
		.forEach(s -> {
			if(excludeEmptyItem && StringObjects.isEmptyAfterTrim(s)) {
				return;
			}
			if (itemHandler != null) {
				s = itemHandler.apply(s);
			}
			result.add(s.trim());
		});
		return result;
	}
	
	public static String buildArrayBuf(Collection src) {
		if(!assertNotEmpty(src)) {
			return ""	;
		}
		
		StringBuilder buf = new StringBuilder();
		for(Object item : src) {
			buf.append(",").append(item);
		}
		
		if(buf.length() > 0) {
			buf.replace(0, 1, "");
		}
		
		return buf.toString();
	}
	
	
	public static String buildArrayBuf(Collection src, String prefix, String suffix) {
		if(!assertNotEmpty(src)) {
			return ""	;
		}
		
		StringBuilder buf = new StringBuilder();
		for(Object item : src) {
			buf.append(",");
			if(prefix != null) {
				buf.append(prefix);
			}
			buf.append(item);
			
			if(suffix != null) {
				buf.append(suffix);
			}
		}
		
		if(buf.length() > 0) {
			buf.replace(0, 1, "");
		}
		
		return buf.toString();
	}
	
	public static <T> Set<T> asSet(T ... arr){
		Set<T> result = new LinkedHashSet<>();
		if(ArrayObjects.assertArrayNotEmpty(arr)) {
			Arrays.stream(arr).forEach(result::add);
		}
		return result;
	}
	
	public static <T> List<T> asList(T ... arr){
		List<T> result = new ArrayList<>();
		if(ArrayObjects.assertArrayNotEmpty(arr)) {
			Arrays.stream(arr).forEach(result::add);
		}
		return result;
	}
	
	public static <T> List<T> asList(Collection<T> src){
		if(src == null) {
			return null;
		}
		List<T> result = new ArrayList<>();
		if(isEmpty(src)) {
			return result;
		}
		result.addAll(src);
		return result;
	}
	
	public static <T> T getFirstNotNullItem(Collection<T> collection) {
		if(isEmpty(collection)) {
			return null;
		}
		
		for(T t : collection) {
			if(t != null) {
				return t;
			}
		}
		return null;
	}
	

	public static int indexOf(Collection collection, Object src) {
		Object [] arr = collection.toArray(new Object [0]);
		for(int i = 0; i < arr.length; i ++) {
			if(Objects.equals(src, arr [i])) {
				return i;
			}
		}
		return -1;
	}
	
	public static <T> T find(Collection<T> collection, Predicate<T> predicate) {
		if(isEmpty(collection)) {
			return null;
		}
		for(T t : collection) {
			if(t == null) {
				continue;
			}
			
			if(predicate.test(t)) {
				return t;
			}
		}
		
		return null;
	}
	
	public static Set compareToRemoves(Collection src, Collection target){
		AtomicValue<Collection> callback = new AtomicValue<>();
		compareTo(src, target, null, callback);
		
		return (Set)callback.getValue();
	}
	
	public static Set compareToAppends(Collection src, Collection target){
		AtomicValue<Collection> callback = new AtomicValue<>();
		compareTo(src, target, callback, null);
		
		return (Set)callback.getValue();
	}
	
	public static void compareTo(Collection src, Collection target, AtomicValue<Collection> appends, AtomicValue<Collection> removes) {
		if(appends != null) {
			Set appendsSet = new LinkedHashSet<>();
			if(CollectionObjects.isEmpty(target)) {
				//
			}else if(CollectionObjects.isEmpty(src)) {
				appendsSet.addAll(target);
			}else {
				Object [] targetArr = target.toArray(new Object [0]);
				for(int i = 0; i < targetArr.length; i ++) {
					Object item = targetArr [i];
					if(!src.contains(item)) {
						appendsSet.add(item);
					}
				}
			}
			appends.setValue(appendsSet);
		}
		
		if(removes != null) {
			Set removesSet = new LinkedHashSet<>();
			if(CollectionObjects.isEmpty(src)) {
				//
			}else if(CollectionObjects.isEmpty(target)){
				removesSet.addAll(src);
			}else {
				Object [] srcArr = src.toArray(new Object [0]);
				for(int i = 0; i < srcArr.length; i ++) {
					Object item = srcArr [i];
					if(!target.contains(item)) {
						removesSet.add(item);
					}
				}
			}
			
			removes.setValue(removesSet);
		}
	}
	
	public static <K, V, S> Map<K, V> toMap(
			Collection<S> sources, Function<S, K> keySet, Function<S, V> valueSet){
		Map<K, V> result = new LinkedHashMap<>();
		if(isEmpty(sources)) {
			return result;
		}
		
		sources.forEach(s -> {
			K key = keySet.apply(s);
			if(key == null) {
				return;
			}
			
			V value = valueSet.apply(s);
			if(value == null) {
				return;
			}
			
			result.put(key, value);
		});
		return result;
	}
	
	public static <T> T search(
			@NotNull Collection<T> src, @NotNull Predicate<T> filter) {
		if(isEmpty(src) || filter == null) {
			return null;
		}
		for(T t : src) {
			if(filter.test(t)) {
				return t;
			}
		}
		return null;
	}
	
	public static <P, R> R [] collect(
			@NotNull  Collection<P> src, @NotNull Class<R> collectType, @NotNull Function<P, R> event) {
		if(collectType == null || event == null || isEmpty(src)) {
			return null;
		}
		List<R> dataset = new ArrayList<>();
		src.forEach(param -> {
			R item = event.apply(param);
			if(item != null) {
				dataset.add(item);
			}
		});
		return dataset.toArray(EmptyArray.getInstance().array(collectType));
	}
	
	public static Class getActualArgumentType(java.lang.reflect.Type type) {
		if(ParameterizedType.class.isInstance(type)) {
			java.lang.reflect.Type [] argumentTypes = ((ParameterizedType) type).getActualTypeArguments();
			if(ArrayObjects.isEmpty(argumentTypes)) {
				return null;
			}
			java.lang.reflect.Type argumentType = argumentTypes [0];
			if(Class.class.isInstance(argumentType)) {
				return (Class) argumentType;
			}
		}
		return null;
	}
	
	public static <E> Collection<E> firstNotEmptyCollection(Collection<E>... cs) {
		for (Collection<E> c : cs) {
			if (assertNotEmpty(c)) {
				return c;
			}
		}
		return null;
	}
	
	public static <E, C extends Collection<E>> C addAll(C collection, Collection<E>... adds) {
		if (null != adds) {
			for (Collection<E> c : adds) {
				collection.addAll(c);
			}
		}
		return collection;
	}

	public static <E, C extends Collection<E>> C addAll(C collection, E... adds) {
		if (null != adds) {
			for (E e : adds) {
				collection.add(e);
			}
		}
		return collection;
	}

	public static <E, C extends Collection<E>> C removeAll(C collection, Collection<E>... removes) {
		if (null != removes) {
			for (Collection<E> c : removes) {
				collection.removeAll(c);
			}
		}
		return collection;
	}

	public static <E, C extends Collection<E>> C removeAll(C collection, E... removes) {
		if (null != removes) {
			for (E e : removes) {
				collection.remove(e);
			}
		}
		return collection;
	}
}
