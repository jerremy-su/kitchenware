package org.kitchenware.express.concurrent;

import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kitchenware.express.annotation.NotNull;

public class ConcurrentOptional {
	
	static final Logger LOGGER = Logger.getLogger(ConcurrentOptional.class.getName());
	
	final Lock lock;
	
	Supplier originSupplier;
	
	ConcurrentOptional(@NotNull final Lock lock) {
		this.lock = lock;
	}
	
	public ConcurrentOptional ofNullable(@NotNull Supplier originSupplier) {
		this.originSupplier = originSupplier;
		return ConcurrentOptional.this; 
	}
	
	public <T> T orElseGet(@NotNull Supplier<T> function){
		Object value = originSupplier.get();
		if(value != null) {
			return (T)value;
		}
		
		lock.lock();
		
		try {
			value = originSupplier.get();
			if(value == null && function != null) {
				value = function.get();
			}
		} catch (Throwable e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}finally {
			lock.unlock();
		}
		
		value = function.get();
		return (T)value;
	}
	
	
	public static final ConcurrentOptional optional(Lock lock) {
		return new ConcurrentOptional(lock);
	}
}
