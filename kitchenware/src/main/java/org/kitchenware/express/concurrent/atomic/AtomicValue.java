package org.kitchenware.express.concurrent.atomic;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AtomicValue<T> {

	static final Logger logger = Logger.getLogger(AtomicValue.class.getName());
	
	volatile T value;
	CountDownLatch cdl;
	
	public AtomicValue() {
		this(null);
	}
	
	public AtomicValue(T value) {
		this.value = value;
	}
	
	public void setValue(T value) {
		this.value = value;
		
		if(cdl != null) {
			cdl.countDown();
		}
	}
	
	public T getValue() {
		return value;
	}
	
	public AtomicValue generateAwaitAction() {
		this.cdl = new CountDownLatch(1);
		return AtomicValue.this;
	}
	
	public void await() {
		try {
			cdl.await();
		} catch (Throwable e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}
	
	public void await(long timeout, TimeUnit unit) {
		try {
			cdl.await(timeout, unit);
		} catch (Throwable e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}
	
	public void remove() {
		this.value = null;
	}
	
	public boolean isNULL() {
		return this.value == null;
	}
	
	public static AtomicValue generateAtomicValue() {
		return new AtomicValue<>();
	}
	
	public static AtomicValue generateAtomicValue(Object defaultValue) {
		return new AtomicValue(defaultValue);
	}
}
