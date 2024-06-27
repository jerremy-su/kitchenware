package org.kitchenware.express.buffered.temporary.spi;

public interface TempContext<K, V> {
	
	TempEntry<K, V> get(K k, TempTimeout timeout);
	
	TempEntry<K, V> get(K k, boolean removeTimeOut, TempTimeout timeout);
	
	void remove(K k);
	
	void put(TempEntry<K, V> entry);
	
	void clear();
}
