package org.kitchenware.express.buffered.temporary;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.kitchenware.express.buffered.temporary.spi.TempContext;
import org.kitchenware.express.buffered.temporary.spi.TempEntry;
import org.kitchenware.express.buffered.temporary.spi.TempTimeout;
import org.kitchenware.express.util.Errors;

public abstract class DefaultTempContext<K, V> implements TempContext<K, V>{

//	WeakHashMap<K, TempEntry<K, V>> context = new WeakHashMap<>();
	ReentrantReadWriteLock contextLock = new ReentrantReadWriteLock();
	
	Lock readLock = contextLock.readLock();
	Lock writeLock = contextLock.writeLock();
	
	DefaultTempContext(){
		
	}
	
	@Override
	public TempEntry<K, V> get(K k, TempTimeout timeout) {
		return get(k, true, timeout);
	}
	
	@Override
	public synchronized TempEntry<K, V> get(K k, boolean removeTimeOut, TempTimeout timeout) {
		readLock.lock();
		TempEntry<K, V> entry;
		try {
			entry = context().get(k);
		} catch (Throwable e) {
			throw Errors.throwRuntimeable(e);
		}finally {
			readLock.unlock();
		}
		
		if (entry != null && timeout.isTimeout(entry.setTime())) {
			if(removeTimeOut) {
				remove(k);
			}
			entry = null;
		}
		
		return entry;
	}

	@Override
	public void remove(K k) {
		writeLock.lock();
		try {
			context().remove(k);
		} catch (Throwable e) {
			throw Errors.throwRuntimeable(e);
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public void put(TempEntry<K, V> entry) {
		writeLock.lock();
		try {
			context().put(entry.getKey(), entry);
		} catch (Throwable e) {
			throw Errors.throwRuntimeable(e);
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public void clear() {
		writeLock.lock();
		try {
			context().clear();
		} catch (Throwable e) {
			throw Errors.throwRuntimeable(e);
		} finally {
			writeLock.unlock();
		}
	}
	
	public abstract Map<K, TempEntry<K, V>> context();
}
