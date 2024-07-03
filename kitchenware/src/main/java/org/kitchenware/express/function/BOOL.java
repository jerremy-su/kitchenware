package org.kitchenware.express.function;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import org.kitchenware.express.util.ArrayObjects;
import org.kitchenware.express.util.BoolObjects;
import org.kitchenware.express.util.StringObjects;

public interface BOOL {

	default boolean NOT(boolean b) {
		return !b;
	}
	
	default boolean IS(boolean b) {
		return b;
	}
	
	default boolean NOT(Supplier<Boolean> p) {
		Boolean b = p.get();
		return this.NOT(BoolObjects.valueOf(b));
	}
	
	default boolean IS(Supplier<Boolean> p) {
		Boolean b = p.get();
		return this.IS(BoolObjects.valueOf(b));
	}
	
	default boolean notNull(Object any) {
		return ! NULL(any);
	}
	
	default boolean NULL(Object any) {
		return any == null;
	}
	
	default boolean notEmpty(Object src) {
		return this.NOT(EMPTY(src));
	}
	
	default boolean EMPTY(Object src) {
		return isEmpty(src);
	}
	
	public static boolean isEmpty(Object src) {
		if(src == null) {
			return true;
		}
		if(String.class.isInstance(src)) {
			return StringObjects.isEmpty(String.class.cast(src));
		}
		if(Collection.class.isInstance(src)) {
			return Collection.class.cast(src).isEmpty();
		}
		if(Map.class.isInstance(src)) {
			return Map.class.cast(src).isEmpty();
		}
		if(src.getClass().isArray()) {
			return ArrayObjects.isEmpty(src);
		}
		return false;
	}
}
