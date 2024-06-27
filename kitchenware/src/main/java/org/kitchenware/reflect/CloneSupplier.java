package org.kitchenware.reflect;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface CloneSupplier<T> extends Cloneable{

	static final CloneableHandler handler = new CloneableHandler();
	
	default T cloneTo() {
		return (T) CloneableHandler.invokeClone(this);
	}

	static final class CloneableHandler{
		static final Logger LOGGER = Logger.getLogger(CloneableHandler.class.getName());
		static final Method SYSTEM_CLONE_Method;
		static {
			SYSTEM_CLONE_Method = installSystemCloneMethod();
		}
		
		private static Method installSystemCloneMethod() {
			try {
				Method method = Object.class.getDeclaredMethod("clone");
				method.setAccessible(true);
				return method;
			} catch (Throwable e) {
				LOGGER.log(Level.WARNING, String.format("Could not install clone method : %s", e.getMessage()), e);
				return null;
			}
		}
		
		public static <T> T invokeClone(T src) {
			if(src == null) {
				return src;
			}
			T result = null;
			if(SYSTEM_CLONE_Method != null && Cloneable.class.isInstance(src)) {
				try {
					result = (T) SYSTEM_CLONE_Method.invoke(src);
				} catch (Throwable e) {
					LOGGER.log(Level.WARNING, String.format("[%s] : Could not invoke clone method : %s", src, e.getMessage()), e);
				}
			}
			return result;
		}
	}
}
