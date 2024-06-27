package org.kitchenware.express.concurrent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kitchenware.express.debug.Debug;

public class ConcurrentLockFactory {

	static final Logger LOGGER = Logger.getLogger(ConcurrentLockFactory.class.getName());
	
	final Map<Object, Lock> context = new ConcurrentHashMap<>();
	final Lock mainLock = new ReentrantLock();
	
	public Lock get(Object key) {
		if(key == null) {
			throw new NullPointerException("Parameter 'key' cannot be null.");
		}
		Lock lock = context.get(key);
		if(lock != null) {
			return lock;
		}
		
		mainLock.lock();
		try {
			lock = context.get(key);
			if(lock == null) {
				context.put(key, lock = new ReentrantLock());
			}
		} catch (Throwable e) {
			if(Debug.isDebug()) {
				LOGGER.log(Level.WARNING, e.getMessage(), e);
			}
		}finally {
			mainLock.unlock();
		}
		
		return lock;
	}
}
