package org.kitchenware.express.buffered.temporary;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.kitchenware.express.buffered.temporary.spi.TempEntry;

public class KeepLongTempContext<K, V> extends DefaultTempContext<K, V>{

	final Map<K, TempEntry<K, V>> context = new ConcurrentHashMap<>();
	
	@Override
	public Map<K, TempEntry<K, V>> context() {
		return context;
	}

}
