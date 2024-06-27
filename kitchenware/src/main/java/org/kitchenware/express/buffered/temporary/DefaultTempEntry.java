package org.kitchenware.express.buffered.temporary;

import org.kitchenware.express.buffered.temporary.spi.TempEntry;

public class DefaultTempEntry<K, V> implements TempEntry<K, V>{

	K k;
	V v;
	long setTime;
	
	DefaultTempEntry(K k, V v, long setTime) {
		this.k = k;
		this.v = v;
		this.setTime = setTime;
	}
	
	@Override
	public K getKey() {
		return k;
	}

	@Override
	public V getValue() {
		return v;
	}

	@Override
	public V setValue(V value) {
		return null;
	}

	@Override
	public long setTime() {
		return setTime;
	}
	
	@Override
	public int hashCode() {
		return v.hashCode();
	}

}
