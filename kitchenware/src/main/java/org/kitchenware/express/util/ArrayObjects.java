package org.kitchenware.express.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.kitchenware.express.annotation.NotNull;

public class ArrayObjects {

	private static final int INSERTIONSORT_THRESHOLD = 7;

	public static boolean equals(Object [] src, Object [] target) {
		if(src == null || target == null) {
			return src == target;
		}
		
		if(src.length != target.length) {
			return false;
		}
		
		int loopCount = src.length;
		for(int i = 0; i < loopCount; i ++) {
			if( ! Objects.equals(src [i], target [i])) {
				return false;
			}
		}
		return true;
	}
	
	public static int size(Object array) {
		if(array == null || !array.getClass().isArray()) {
			return 0;
		}
		return Array.getLength(array);
	}
	
	public static boolean isArray(Object array) {
		return array != null && array.getClass().isArray();
	}

	public static boolean assertArrayNotEmpty(Object array) {
		if (!isArray(array)) {
			return false;
		}

		return Array.getLength(array) > 0;
	}

	public static boolean isEmpty(Object array) {
		return !assertArrayNotEmpty(array);
	}

	public static boolean hasContains(Object[] src, Object... targets) {
		if (!assertArrayNotEmpty(src) || !assertArrayNotEmpty(targets)) {
			return false;
		}

		for (Object s : src) {
			for (Object t : targets) {
				if (t.equals(s)) {
					return true;
				}
			}
		}
		return false;
	}

	public static String toString(Object arr) {
		return buildArrayBuf(arr);
	}
	
	public static String buildArrayBuf(Object arr) {
		if (isEmpty(arr)) {
			return "";
		}

		StringBuilder buf = new StringBuilder();
		int length = Array.getLength(arr);
		for (int i = 0; i < length; i++) {
			Object item = Array.get(arr, i);
			buf.append(",").append(item);
		}

		if (buf.length() > 0) {
			buf.replace(0, 1, "");
		}

		return buf.toString();
	}

	public static int indexOf(Object array, Object src) {
		int length = Array.getLength(array);

		for (int i = 0; i < length; i++) {
			if (Objects.equals(src, Array.get(array, i))) {
				return i;
			}
		}
		return -1;
	}

	public static <K, V, S> Map<K, V> toMap(
			S [] sources, Function<S, K> keySet, Function<S, V> valueSet){
		Map<K, V> result = new LinkedHashMap<>();
		if(isEmpty(sources)) {
			return result;
		}
		
		Arrays.stream(sources).forEach(s -> {
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
	
	public static <T> void sort(T[] a, Comparator<? super T> c) {
		if (c == null) {
			sort(a);
		} else {
			legacyMergeSort(a, c);
		}
	}

	public static void sort(Object[] a) {
		legacyMergeSort(a);
	}

	 /** To be removed in a future release. */
    private static <T> void legacyMergeSort(T[] a, Comparator<? super T> c) {
        T[] aux = a.clone();
        if (c==null)
            mergeSort(aux, a, 0, a.length, 0);
        else
            mergeSort(aux, a, 0, a.length, 0, c);
    }
	
	public static void legacyMergeSort(Object[] a) {
		Object[] aux = a.clone();
		mergeSort(aux, a, 0, a.length, 0);
	}

	public static void mergeSort(Object[] src, Object[] dest, int low, int high, int off) {
		int length = high - low;

		// Insertion sort on smallest arrays
		if (length < INSERTIONSORT_THRESHOLD) {
			for (int i = low; i < high; i++)
				for (int j = i; j > low && ((Comparable) dest[j - 1]).compareTo(dest[j]) > 0; j--)
					swap(dest, j, j - 1);
			return;
		}

		// Recursively sort halves of dest into src
		int destLow = low;
		int destHigh = high;
		low += off;
		high += off;
		int mid = (low + high) >>> 1;
		mergeSort(dest, src, low, mid, -off);
		mergeSort(dest, src, mid, high, -off);

		// If list is already sorted, just copy from src to dest. This is an
		// optimization that results in faster sorts for nearly ordered lists.
		if (((Comparable) src[mid - 1]).compareTo(src[mid]) <= 0) {
			System.arraycopy(src, low, dest, destLow, length);
			return;
		}

		// Merge sorted halves (now in src) into dest
		for (int i = destLow, p = low, q = mid; i < destHigh; i++) {
			if (q >= high || p < mid && ((Comparable) src[p]).compareTo(src[q]) <= 0)
				dest[i] = src[p++];
			else
				dest[i] = src[q++];
		}
	}

	  /**
     * Src is the source array that starts at index 0
     * Dest is the (possibly larger) array destination with a possible offset
     * low is the index in dest to start sorting
     * high is the end index in dest to end sorting
     * off is the offset into src corresponding to low in dest
     * To be removed in a future release.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void mergeSort(Object[] src,
                                  Object[] dest,
                                  int low, int high, int off,
                                  Comparator c) {
        int length = high - low;

        // Insertion sort on smallest arrays
        if (length < INSERTIONSORT_THRESHOLD) {
            for (int i=low; i<high; i++)
                for (int j=i; j>low && c.compare(dest[j-1], dest[j])>0; j--)
                    swap(dest, j, j-1);
            return;
        }

        // Recursively sort halves of dest into src
        int destLow  = low;
        int destHigh = high;
        low  += off;
        high += off;
        int mid = (low + high) >>> 1;
        mergeSort(dest, src, low, mid, -off, c);
        mergeSort(dest, src, mid, high, -off, c);

        // If list is already sorted, just copy from src to dest.  This is an
        // optimization that results in faster sorts for nearly ordered lists.
        if (c.compare(src[mid-1], src[mid]) <= 0) {
           System.arraycopy(src, low, dest, destLow, length);
           return;
        }

        // Merge sorted halves (now in src) into dest
        for(int i = destLow, p = low, q = mid; i < destHigh; i++) {
            if (q >= high || p < mid && c.compare(src[p], src[q]) <= 0)
                dest[i] = src[p++];
            else
                dest[i] = src[q++];
        }
    }
	
	/**
	 * Swaps x[a] with x[b].
	 */
	private static void swap(Object[] x, int a, int b) {
		Object t = x[a];
		x[a] = x[b];
		x[b] = t;
	}
	
	/**
	 * @description: 获取指定下标的数组元素
	 *
	 * @author: calvin.chen
	 * @date: 2023年8月4日 上午11:38:12
	 * @param <T>
	 * @param array
	 * @param index
	 * @param def
	 * @return
	 * T
	 */
	public static <T> T getElement(T[] array, int index, T def) {
		if (array == null || array.length <= index) {
			return def;
		}
		T element = array[index];
		return element == null ? def : element;
	}
	
	public static <T> void foreach(@NotNull T [] src, @NotNull IteratorHandler<T> event) {
		if(event == null || isEmpty(src)) {
			return;
		}
		
		for(int i = 0; i < src.length; i ++) {
			T item = src [i];
			event.handle(i, item);
		}
	}
	
	public static <P, R> R [] collect(
			@NotNull  P [] src, @NotNull Class<R> collectType, @NotNull Function<P, R> event) {
		if(collectType == null || event == null || isEmpty(src)) {
			return null;
		}
		List<R> dataset = new ArrayList<>();
		foreach(src, (index, param)-> {
			R item = event.apply(param);
			if(item != null) {
				dataset.add(item);
			}
		});
		return dataset.toArray(EmptyArray.getInstance().array(collectType));
	}
	
	@FunctionalInterface
	public static interface IteratorHandler<T>{
		void handle(int index, T t);
	}
}
