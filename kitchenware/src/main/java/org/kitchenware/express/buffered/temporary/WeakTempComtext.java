package org.kitchenware.express.buffered.temporary;

import java.util.Map;
import java.util.WeakHashMap;

import org.kitchenware.express.buffered.temporary.spi.TempEntry;

public class WeakTempComtext<K, V> extends DefaultTempContext<K, V>{

	final Map<K, TempEntry<K, V>> context = new WeakHashMap<>();
	
	@Override
	public Map<K, TempEntry<K, V>> context() {
		return context;
	}

}
