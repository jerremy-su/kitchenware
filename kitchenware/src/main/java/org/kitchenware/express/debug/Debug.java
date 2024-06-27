package org.kitchenware.express.debug;

import org.kitchenware.express.util.BoolObjects;

public class Debug {

	static final boolean DEBUG = BoolObjects.valueOf(System.getProperty("debug"));
	static final ThreadLocal<Boolean> processDebug = new ThreadLocal<>();
	
	public static final boolean isDebug() {
		return DEBUG;
	}
	
	public static final void setProcessDebug(boolean b) {
		processDebug.set(b);
	}
	
	public static final void removeProcessDebug() {
		processDebug.remove();
	}
	
	public static final boolean isProcessDebug() {
		return BoolObjects.valueOf(processDebug.get());
	}
	
}
