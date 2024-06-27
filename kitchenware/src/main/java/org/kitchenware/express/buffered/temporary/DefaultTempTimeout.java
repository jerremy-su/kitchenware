package org.kitchenware.express.buffered.temporary;

import org.kitchenware.express.buffered.temporary.spi.TempTimeout;

public class DefaultTempTimeout implements TempTimeout{

	long timeout;
	
	DefaultTempTimeout(long timeout) {
		this.timeout = timeout;
	}
	
	@Override
	public boolean isTimeout(long setTime) {
		return System.currentTimeMillis() - setTime >= timeout;
	}

}
