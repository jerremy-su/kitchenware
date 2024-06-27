package org.kitchenware.express.buffered.temporary;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.kitchenware.express.buffered.temporary.spi.TempContext;
import org.kitchenware.express.buffered.temporary.spi.TempEntry;
import org.kitchenware.express.buffered.temporary.spi.TempTimeout;

public class DefaultTempFactory {
	static DefaultTempFactory owner = new DefaultTempFactory();
	public static DefaultTempFactory owner() {
		return owner;
	}
	
	Map<String, TempContext> weakContext = new ConcurrentHashMap<>();
	Map<String, TempContext> keepLongContext = new ConcurrentHashMap<>();
	
	private DefaultTempFactory() {}
	
	public TempContext getTemporary(String naming) {
		TempContext result = weakContext.get(naming);
		if (result == null) {
			weakContext.put(naming, result = new WeakTempComtext<>());
		}
		return result;
	}
	
	public TempContext getKeepLongTemporary(String naming) {
		TempContext result = keepLongContext.get(naming);
		if (result == null) {
			keepLongContext.put(naming, result = new KeepLongTempContext<>());
		}
		return result;
	}
	
	public TempTimeout createTimeout(long timeout) {
		return new DefaultTempTimeout(timeout);
	}
	
	public TempEntry createTempEntry(Object key, Object value) {
		return new DefaultTempEntry(key, value, System.currentTimeMillis());
	}
}
