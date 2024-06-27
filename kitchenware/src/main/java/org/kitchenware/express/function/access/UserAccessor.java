package org.kitchenware.express.function.access;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UserAccessor {
	static final Logger logger = Logger.getLogger(UserAccessor.class.getName());
	
	public static <T> T functionAccess(FunctionAccesible<T> functionAccesible) {
		return functionAccess(1, functionAccesible);
	}
	
	public static <T> T functionAccess(int retry, FunctionAccesible<T> functionAccesible) {
		if(retry < 1) {
			retry = 1;
		}
		for (int i = 0; i < retry; i++) {
			try {
				return functionAccesible.access();
			} catch (Throwable e) {
				
				Throwable exception = new RuntimeException(
						String.format("Process failed : %s; retry : %s", e.getMessage(), i + 1)
						, e
						);
				
				logger.log(Level.WARNING, exception.getMessage(), exception);
				continue;
			}
		}
		return null;
	}
	
	public static void functionProcess(FunctionProcessble functionProcessble) {
		functionProcess(1, functionProcessble);
	}
	
	public static void functionProcess(int retry, FunctionProcessble functionProcessble) {
		if(retry < 1) {
			retry = 1;
		}
		for (int i = 0; i < retry; i++) {
			try {
				functionProcessble.process();
				
				break;
			} catch (Throwable e) {
				
				Throwable exception = new RuntimeException(
						String.format("Process failed : %s; retry : %s", e.getMessage(), i + 1)
						, e
						);
				
				logger.log(Level.WARNING, exception.getMessage(), exception);
			}
		}
	}
}
