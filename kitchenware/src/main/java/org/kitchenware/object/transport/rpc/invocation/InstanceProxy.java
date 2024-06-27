package org.kitchenware.object.transport.rpc.invocation;

import java.lang.reflect.Proxy;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.express.annotation.Optional;
import org.kitchenware.reflect.ClassBufferedFactory;

import net.sf.cglib.proxy.Enhancer;

/**
 * 
 *
 */
public class InstanceProxy {
	
	public static <T> T newInstance(Class<T> type, MethodInvokerHandler handler) {
		T instance;
		if (type.isInterface()) {
			instance = (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[] { type }, handler);
		} else {
			Enhancer hancer = new Enhancer();
			hancer.setSuperclass(type);
			hancer.setCallback(handler);
			instance = (T) hancer.create();
		}
		return instance;
	}
	
	public static <T> T newInstance(
			@NotNull Class<T> [] interfaceTypes, MethodInvokerHandler handler) {
		return newInstance(ClassBufferedFactory.getFactory().getContextClassLoader(), interfaceTypes, handler);
	}

	public static <T> T newInstance(
			@Optional ClassLoader classloader, @NotNull Class<T> [] interfaceTypes, MethodInvokerHandler handler) {
		T instance = (T) Proxy.newProxyInstance(classloader, interfaceTypes, handler);
		return instance;
	}

}
