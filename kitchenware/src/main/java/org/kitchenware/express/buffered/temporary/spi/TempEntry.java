package org.kitchenware.express.buffered.temporary.spi;

import java.util.Map.Entry;

public interface TempEntry<K, V> extends Entry<K, V>  {
	
	long setTime();
	
}
