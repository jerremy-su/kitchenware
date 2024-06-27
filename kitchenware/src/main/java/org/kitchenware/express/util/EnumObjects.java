package org.kitchenware.express.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.express.concurrent.ConcurrentLockFactory;
import org.kitchenware.express.debug.Debug;

public class EnumObjects {

	static final Logger LOGGER = Logger.getLogger(EnumObjects.class.getName());
	
	static final Map<Class, EnumType> enumContext = new ConcurrentHashMap<>();
	static ConcurrentLockFactory LOCKS = new ConcurrentLockFactory();
	
	public static <T extends Enum<T>> T valueOf(
			@NotNull Class<T> enumType, @NotNull Object name) {
		if(name == null) {
			return null;
		}
		return valueOf(enumType, StringObjects.valueOf(name));
	}
	
	public static <T extends Enum<T>> T valueOf(
			@NotNull Class<T> enumType, @NotNull String name) {
		if(StringObjects.isEmptyAfterTrim(name)) {
			return null;
		}
		
		EnumType<T> enumObject = getEnumObject(enumType);
		T result = enumObject.constants.get(name);
		return result;
	}
	
	static <T extends Enum<T>> EnumType<T> getEnumObject(@NotNull Class<T> enumType) {
		EnumType<T> result = enumContext.get(enumType);
		if(result != null) {
			return result;
		}
		
		Lock lock = LOCKS.get(enumContext);
		lock.lock();
		try {
			result = enumContext.get(enumType);
			if(result == null) {
				result = new EnumType<T>(enumType);
				enumContext.put(enumType, result);
			}
		} catch (Throwable e) {
			if(Debug.isDebug()) {
				LOGGER.log(Level.WARNING, e.getMessage(), e);
			}else {
				String warn = String.format("Failed to initial enum objects: %s", enumType.getName());
				LOGGER.warning(warn);
			}
		}finally {
			lock.unlock();
		}
		return result;
	}
	
	static class EnumType<T extends Enum<T>>{
		final Map<String, T> constants = new LinkedHashMap<>();
		
		final Class<T> enumType;
		
		EnumType(Class<T> enumType){
			this.enumType = enumType;
			
			T [] values = enumType.getEnumConstants();
			boolean aliasSupported = EnumAlias.class.isAssignableFrom(enumType);
			for(T en : values) {
				String name = en.name();
				constants.put(name, en);
				if(aliasSupported) {
					EnumAlias alias = (EnumAlias) en;
					constants.put(alias.alias(), en);
				}
			}
		}
	}
	
	/**
	 * Alias supported
	 */
	public static interface EnumAlias{
		String alias();
	}
}
